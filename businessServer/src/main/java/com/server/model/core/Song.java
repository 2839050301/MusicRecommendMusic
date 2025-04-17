package com.server.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

//歌曲类
public class Song {

    @JsonIgnore
    private int _id;

    private long songId;

    private String sname;

    private  long SingerId;

    private int hot;

    private int genre;

    private String url;

    private String tags;

    private String languages;

    // 添加无参构造函数
    public Song() {
    }

    public Song(long songId, String sname, long singerId, int hot, int genre, String url, String tags, String languages) {
        this.songId = songId;
        this.sname = sname;
        SingerId = singerId;
        this.hot = hot;
        this.genre = genre;
        this.url = url;
        this.tags = tags;
        this.languages = languages;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public long getSingerId() {
        return SingerId;
    }

    public void setSingerId(long singerId) {
        SingerId = singerId;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}
