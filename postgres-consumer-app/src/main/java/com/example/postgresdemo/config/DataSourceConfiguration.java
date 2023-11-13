package com.example.postgresdemo.config;

import com.example.postgresdemo.model.VcapServices;
import com.google.gson.Gson;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
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

    private static Gson gson = new Gson();


    @Bean
    public DataSource dataSource() {
        VcapServices vcapServices = gson.fromJson(vsJson, VcapServices.class);





        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        List<String> hosts = vcapServices.getPostgres().get(0).getCredentials().getHosts();
        Long port = vcapServices.getPostgres().get(0).getCredentials().getPort();
        StringBuilder jdbcUri = new StringBuilder("jdbc:postgresql://");
        Stream<String>withPort=hosts.stream().map(s-> String.format("%s:%d",s,port));
        jdbcUri.append(withPort.collect(Collectors.joining(",")));
        jdbcUri.append("/")
            .append(vcapServices.getPostgres().get(0).getCredentials().getDb())
            .append("?targetServerType=master");
        if(!StringUtils.isEmpty(sslMode)) {
            jdbcUri.append("&sslmode="+sslMode);
            jdbcUri.append("&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory");
        }

        logger.info("-------------POSTGRES URL-----------" + jdbcUri);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUri.toString());
        hikariConfig.setUsername(vcapServices.getPostgres().get(0).getCredentials().getUser());
        hikariConfig.setPassword(vcapServices.getPostgres().get(0).getCredentials().getPassword());
        hikariConfig.setInitializationFailTimeout(300000); //5 minutes
        return new HikariDataSource(hikariConfig);

    }

}