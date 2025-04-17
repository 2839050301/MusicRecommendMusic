package com.server.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User_like {
    @JsonIgnore
    private int _id;

    private int userId;

    private long songId;

    public User_like(int userId, long songId) {
        this.userId = userId;
        this.songId = songId;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }
}
