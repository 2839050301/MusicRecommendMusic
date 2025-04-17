package com.server.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Singer {
    @JsonIgnore
    private int _id;
    private long singerId;
    private String sname;

    // 构造函数、getter和setter
    public Singer() {}

    public Singer(long singerId, String sname) {
        this.singerId = singerId;
        this.sname = sname;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    // getter和setter方法
    public long getSingerId() {
        return singerId;
    }

    public void setSingerId(long singerId) {
        this.singerId = singerId;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }
}