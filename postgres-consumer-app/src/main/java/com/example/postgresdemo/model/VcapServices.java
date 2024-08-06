package com.example.postgresdemo.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VcapServices {
    @Override
    public String toString() {
        return "VcapServices{" +
                "postgres=" + postgres +
                '}';
    }

    @SerializedName(value = "postgres")
    private List<Postgres> postgres;

    public List<Postgres> getPostgres() {
        return postgres;
    }

    public void setPostgres(List<Postgres> value) {
        this.postgres = value;
    }

}


