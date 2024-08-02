package com.example.postgresdemo.config;

import com.example.postgresdemo.model.Credentials;
import com.example.postgresdemo.model.CredsEnvironment;
import com.example.postgresdemo.model.VcapServices;
import com.google.gson.Gson;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Profile("cloud")
public class DataSourceConfiguration {

    @Bean
    public Cloud cloud() {
        return new CloudFactory().getCloud();
    }

    Logger logger = LoggerFactory.getLogger(DataSourceConfiguration.class);
    @Value("${VCAP_SERVICES}")
    private String vsJson;

    @Value("${SSL_MODE}")
    private String sslMode;

    @Value("${SERVICE_GW_CREDENTIALS}")
    private String svcGwCredentials;

    @Value("${CF_INSTANCE_CERT}")
    private String cfInstanceCert;

    @Value("${CF_INSTANCE_KEY}")
    private String cfInstanceKey;

    @Bean
    @Qualifier("cfInstanceCert")
    public String getCFInstanceCert() {
        return this.cfInstanceCert;
    }

    @Bean
    @Qualifier("cfInstanceKey")
    public String getCFInstanceKey() {
        return this.cfInstanceKey;
    }

    @Bean
    public Credentials getCredentials() {
        if (StringUtils.isEmpty(svcGwCredentials)) {
            logger.info("-------------On platform case-----------");
            VcapServices vcapServices = gson.fromJson(vsJson, VcapServices.class);
            return vcapServices.getPostgres().get(0).getCredentials();
        } else {
            logger.info("-------------Off platform case-----------");
            CredsEnvironment credentials = gson.fromJson(svcGwCredentials, CredsEnvironment.class);
            return credentials.credentials;
        }
    }

    private static final Gson gson = new Gson();

    private String getJDBCUrl(Credentials credentials) {
        if (StringUtils.isEmpty(svcGwCredentials)) {
            StringBuilder jdbcUriBuilder = new StringBuilder("jdbc:postgresql://");
            
            List<String> hosts = credentials.getHosts();
            Long port = credentials.getPort();
            
            Stream<String>withPort=hosts.stream().map(s-> String.format("%s:%d",s,port));
            jdbcUriBuilder.append(withPort.collect(Collectors.joining(",")));
            jdbcUriBuilder.append("/")
                .append(credentials.getDb())
                .append("?targetServerType=master");
            if(!StringUtils.isEmpty(sslMode)) {
                jdbcUriBuilder.append("&sslmode="+sslMode);
                jdbcUriBuilder.append("&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory");
            }
            logger.info("-------------POSTGRES URL-----------" + jdbcUriBuilder);
            return jdbcUriBuilder.toString();
        } else {
            return credentials.getService_gateway().getJdbcUrl();
        }
    }

    @Bean
    public DataSource dataSource() {
        Credentials credentials = this.getCredentials();
        String jdbcUrl = this.getJDBCUrl(credentials);
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(credentials.getUser());
        hikariConfig.setPassword(credentials.getPassword());
        hikariConfig.setInitializationFailTimeout(300000); //5 minutes
        return new HikariDataSource(hikariConfig);
    }

}