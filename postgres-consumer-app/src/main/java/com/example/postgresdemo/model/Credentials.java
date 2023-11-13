package com.example.postgresdemo.model;

import java.util.List;

public class Credentials {
    private List<String> hosts;
    private String password;
    private long port;
    private long serviceGatewayAccessPort;
    private boolean serviceGatewayEnabled;

    private String user;
    private String db;
    public String uri;

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

    public boolean isServiceGatewayEnabled() {
        return serviceGatewayEnabled;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public long getServiceGatewayAccessPort() {
        return serviceGatewayAccessPort;
    }

    public void setServiceGatewayAccessPort(long value) {
        this.serviceGatewayAccessPort = value;
    }


    public boolean getServiceGatewayEnabled() {
        return serviceGatewayEnabled;
    }

    public void setServiceGatewayEnabled(boolean value) {
        this.serviceGatewayEnabled = value;
    }



}
