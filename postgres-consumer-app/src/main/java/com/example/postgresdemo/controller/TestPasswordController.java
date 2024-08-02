package com.example.postgresdemo.controller;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public TestPasswordController(Credentials credentials, @Qualifier("cfInstanceCert") String cfInstanceCert,
                                  @Qualifier("cfInstanceKey") String cfInstanceKey) {
        this.credentials=credentials;
        this.cfInstanceCert=cfInstanceCert;
        this.cfInstanceKey=cfInstanceKey;
    }

    @Value("${SSL_MODE}")
    private String envSslMode;

    private String cfInstanceCert;
    private String cfInstanceKey;
    private Credentials credentials;

    Logger logger = LoggerFactory.getLogger(TestPasswordController.class);
    private static final Gson gson = new Gson();

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

        boolean failed = results.stream().anyMatch( s -> s.contains("failed :"));
        HttpStatus status = failed ? HttpStatus.EXPECTATION_FAILED : HttpStatus.OK;
        return ResponseEntity.status(status).body(results);
    }

    private String testSslClientCertWrongPassword(String mode, String user) {
        return testSslClientCertWrongPassword(mode, user, false);
    }

    private String testGoodPassword(){
        return testConnection(envSslMode, this.credentials.getUser(), this.credentials.getPassword(), true);
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
        Pair<Boolean, Exception> testConnectionResultPair = testConnection(dataSource);
        boolean testConnectionResult = testConnectionResultPair.getLeft();
        Exception testConnectionException = testConnectionResultPair.getRight();

        String result = testConnectionResult == expected ? "passed" : "failed";
        String reason = testConnectionException == null ? "n/a" : testConnectionException.getMessage();

        return String.format(
            "%s : test(expected: %b, actual: %b, reason: %s, mode:%s, u:%s, p:'%s', sslcert:'%s', sslkey:'%s')",
            result, expected, testConnectionResult, reason, p.sslMode, p.user, p.password,
            p.clientCertPath, p.clientKeyPath);
    }

    private Pair<Boolean, Exception> testConnection(DataSource dataSource) {
        try{
            Connection conn = dataSource.getConnection();
            conn.close();
        }
        catch (Exception sqlException) {
            return Pair.of(false, sqlException);
        }
        return Pair.of(true, null);
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
        List<String> hosts = this.credentials.getHosts();
        Long port = this.credentials.getPort();
        StringBuilder jdbcUri = new StringBuilder("jdbc:postgresql://");
        Stream<String>hostsWithPort=hosts.stream().map(s-> String.format("%s:%d",s,port));
        jdbcUri.append(hostsWithPort.collect(Collectors.joining(",")));
        jdbcUri.append("/")
            .append(this.credentials.getDb())
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

    private class TestConnectionParams {
        public String sslMode;
        public String user;
        public String password;
        public String clientCertPath;
        public String clientKeyPath;
    }
}
