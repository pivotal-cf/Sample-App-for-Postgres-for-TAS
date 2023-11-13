package com.example.postgresdemo.controller;

import org.springframework.web.bind.annotation.*;

import com.example.postgresdemo.model.VcapServices;
import com.example.postgresdemo.model.Credentials;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
public class TestPasswordController {

    @Value("${VCAP_SERVICES}")
    private String vsJson;

    @Value("${SSL_MODE}")
    private String envSslMode;

    @Value("${CF_INSTANCE_CERT}")
    private String cfInstanceCert;

    @Value("${CF_INSTANCE_KEY}")
    private String cfInstanceKey;

    Logger logger = LoggerFactory.getLogger(TestPasswordController.class);
    private static Gson gson = new Gson();

    @RequestMapping(value="/testPasswordConnections", method=RequestMethod.GET)
    public ResponseEntity<List<String>> runTests(){
        List<String> results = new ArrayList<String>();

        results.add(testGoodPassword());

        results.add(testWrongPassword("disable", "vcap"));
        results.add(testWrongPassword("verify-ca", "vcap"));
        results.add(testWrongPassword("verify-full", "vcap"));
        results.add(testSslClientCertWrongPassword("verify-full", "vcap"));

        results.add(testWrongPassword("disable", "pgadmin"));
        results.add(testWrongPassword("verify-ca", "pgadmin"));
        results.add(testWrongPassword("verify-full", "pgadmin"));
        results.add(testSslClientCertWrongPassword("verify-full", "pgadmin"));

        results.add(testWrongPassword("disable", "postgres"));
        results.add(testWrongPassword("verify-ca", "postgres"));
        results.add(testWrongPassword("verify-full", "postgres"));
        results.add(testSslClientCertWrongPassword("verify-full", "postgres"));

        boolean failed = results.stream().anyMatch( s -> s.contains("failed"));
        HttpStatus status = failed ? HttpStatus.EXPECTATION_FAILED : HttpStatus.OK;
        return ResponseEntity.status(status).body(results);
    }

    private String testSslClientCertWrongPassword(String mode, String user) {
        return testSslClientCertWrongPassword(mode, user, false);
    }

    private String testGoodPassword(){
        return testConnection(envSslMode, getCreds().getUser(), getCreds().getPassword(), true);
    }

    private String testWrongPassword(String sslMode, String user){
        return testConnection(sslMode, user, "wrong-password-123", false);
    }

    private String testConnection(String sslMode, String user, String password, boolean expected) {
        TestConnectionParams p = new TestConnectionParams();
        p.sslMode = sslMode;
        p.user = user;
        p.password = password;
        return test(expected, p);
    }

    private String testSslClientCertWrongPassword(String sslMode, String user, boolean expected) {
        TestConnectionParams params = new TestConnectionParams();
        params.sslMode = sslMode;
        params.user = user;
        params.clientCertPath = cfInstanceCert;
        params.clientKeyPath = cfInstanceKey;
        params.password = "wrong-password-123";
        return test(expected, params);
    }

    private String test(boolean expected, TestConnectionParams p) {
        DataSource dataSource = buildDataSource(p);
        boolean actual = testConnection(dataSource);
        String result = actual == expected ? "passed" : "failed";
        return String.format(
            "%s : test(expected: %b, actual: %b, mode:%s, u:%s, p:'%s', sslcert:'%s', sslkey:'%s')",
            result, expected, actual, p.sslMode, p.user, p.password, p.clientCertPath, p.clientKeyPath);
    }

    private boolean testConnection(DataSource dataSource) {
        boolean result = true;
        try{
            dataSource.setLoginTimeout(5);
            Connection conn = dataSource.getConnection();
            conn.close();
        }
        catch (Exception sqlException) {
            result = false;
        }
        return result;
    }

    private DataSource buildDataSource(TestConnectionParams p) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        String url = getBaseJdbc(p.sslMode, p.clientCertPath, p.clientKeyPath);
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(p.user);
        dataSourceBuilder.password(p.password);
        return dataSourceBuilder.build();
    }

    private String getBaseJdbc(String sslMode, String certPath, String keyPath){
        List<String> hosts = getCreds().getHosts();
        Long port = getCreds().getPort();
        StringBuilder jdbcUri = new StringBuilder("jdbc:postgresql://");
        Stream<String>hostsWithPort=hosts.stream().map(s-> String.format("%s:%d",s,port));
        jdbcUri.append(hostsWithPort.collect(Collectors.joining(",")));
        jdbcUri.append("/")
            .append(getCreds().getDb())
            .append("?sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory");
        if(StringUtils.isNotEmpty(sslMode)) {
            jdbcUri.append("&sslmode=").append(sslMode);
        }
        if(StringUtils.isNotEmpty(certPath) && StringUtils.isNotEmpty(keyPath)) {
            jdbcUri.append("&sslcert=").append(certPath);
            jdbcUri.append("&sslkey=").append(keyPath);
        }
        return jdbcUri.toString();
    }

    private Credentials getCreds() {
        Credentials creds = getVcap().getPostgres().get(0).getCredentials();
        return creds;
    }

    private VcapServices vcap;
    private VcapServices getVcap() {
        if(vcap == null) {
            vcap = gson.fromJson(vsJson, VcapServices.class);
        }
        return vcap;
    }
    private class TestConnectionParams {
        public String sslMode;
        public String user;
        public String password;
        public String clientCertPath;
        public String clientKeyPath;
    }
}
