package com.example.postgresdemo.model;

import java.util.List;
import java.util.UUID;

public class Postgres {
    @Override
    public String toString() {
        return "Postgres{" +
                "label='" + label + '\'' +
                ", provider=" + provider +
                ", plan='" + plan + '\'' +
                ", name='" + name + '\'' +
                ", tags=" + tags +
                ", instanceGUID=" + instanceGUID +
                ", instanceName='" + instanceName + '\'' +
                ", bindingGUID=" + bindingGUID +
                ", bindingName=" + bindingName +
                ", credentials=" + credentials +
                ", syslogDrainURL=" + syslogDrainURL +
                ", volumeMounts=" + volumeMounts +
                '}';
    }

    private String label;
    private Object provider;
    private String plan;
    private String name;
    private List<String> tags;
    private UUID instanceGUID;
    private String instanceName;
    private UUID bindingGUID;
    private Object bindingName;
    private Credentials credentials;
    private Object syslogDrainURL;
    private List<Object> volumeMounts;

    public String getLabel() { return label; }
    public void setLabel(String value) { this.label = value; }

    public Object getProvider() { return provider; }
    public void setProvider(Object value) { this.provider = value; }

    public String getPlan() { return plan; }
    public void setPlan(String value) { this.plan = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> value) { this.tags = value; }

    public UUID getInstanceGUID() { return instanceGUID; }
    public void setInstanceGUID(UUID value) { this.instanceGUID = value; }

    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String value) { this.instanceName = value; }

    public UUID getBindingGUID() { return bindingGUID; }
    public void setBindingGUID(UUID value) { this.bindingGUID = value; }

    public Object getBindingName() { return bindingName; }
    public void setBindingName(Object value) { this.bindingName = value; }

    public Credentials getCredentials() { return credentials; }
    public void setCredentials(Credentials value) { this.credentials = value; }

    public Object getSyslogDrainURL() { return syslogDrainURL; }
    public void setSyslogDrainURL(Object value) { this.syslogDrainURL = value; }

    public List<Object> getVolumeMounts() { return volumeMounts; }
    public void setVolumeMounts(List<Object> value) { this.volumeMounts = value; }
}
