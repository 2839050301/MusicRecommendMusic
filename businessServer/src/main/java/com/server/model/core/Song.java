package com.server.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

//歌曲类
public class Song {

    @JsonIgnore
    private int _id;

    private long sid;

    private String sname;

    private long SingerId;

    private int hot;

    private int genre;

    private String url;

    private String tags;

    private String languages;

    public Song(long sid, String sname, long singerId, int hot, String url, int genre, String tags, String languages) {
        this.sid = sid;
        this.sname = sname;
        SingerId = singerId;
        this.hot = hot;
        this.url = url;
        this.genre = genre;
        this.tags = tags;
        this.languages = languages;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public long getSingerId() {
        return SingerId;
    }

    public void setSingerId(long singerId) {
        SingerId = singerId;
    }

    public int getGenre() {
        return genre;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }
}
