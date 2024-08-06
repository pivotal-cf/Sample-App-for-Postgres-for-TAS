package com.example.postgresdemo.model;

import java.util.List;

public class Credentials {
    private List<String> hosts;
    private String password;
    private long port;
    private ServiceGatewayCredentials service_gateway;

    private String user;
    private String db;
    public String uri;    
    public String jdbcUrl;

    public ServiceGatewayCredentials getService_gateway() {
        return service_gateway;
    }

    public void setService_gateway(ServiceGatewayCredentials service_gateway) {
        this.service_gateway = service_gateway;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public long getPort() {
        return port;
    }

    public void setPort(long value) {
        this.port = value;
    }

}
