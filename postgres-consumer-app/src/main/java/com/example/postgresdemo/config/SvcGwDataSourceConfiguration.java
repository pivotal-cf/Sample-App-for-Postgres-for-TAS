package com.example.postgresdemo.config;

import com.example.postgresdemo.model.Credentials;
import com.example.postgresdemo.model.CredsEnvironment;
import com.google.gson.Gson;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
import java.util.Collections;

@Configuration
@Profile("service_gateway")
public class SvcGwDataSourceConfiguration {

    Logger logger = LoggerFactory.getLogger(DataSourceConfiguration.class);
    @Value("${SERVICE_GW_CREDENTIALS}")
    private String svcGwCredentials;

    @Value("${SSL_MODE}")
    private String sslMode;

    private static final Gson gson = new Gson();

    @Bean
    @Qualifier("cfInstanceCert")
    public String getCFInstanceCert() {
        return "";
    }

    @Bean
    @Qualifier("cfInstanceKey")
    public String getCFInstanceKey() {
        return "";
    }

    @Bean
    public Credentials getCredentials() {
        CredsEnvironment credentials = gson.fromJson(svcGwCredentials, CredsEnvironment.class);
        credentials.credentials.setHosts(Collections.singletonList(credentials.credentials.getService_gateway().getHost()));
        return credentials.credentials;
    }

    @Bean
    public DataSource dataSource() {
        //svcGwCredentials = svcGwCredentials.replaceAll("\"", "\\\\\"");
        Credentials credentials = this.getCredentials();
        String jdbcUri=credentials.getService_gateway().getJdbcUrl();

        logger.info("-------------POSTGRES URL: {}", jdbcUri);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUri);
        hikariConfig.setUsername(credentials.getUser());
        hikariConfig.setPassword(credentials.getPassword());
        hikariConfig.setInitializationFailTimeout(300000); //5 minutes
        return new HikariDataSource(hikariConfig);

    }

}