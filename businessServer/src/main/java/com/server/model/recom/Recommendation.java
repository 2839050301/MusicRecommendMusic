package com.server.model.recom;

//推荐的单个歌曲的包装
public class Recommendation {

    private long songId;
    private double score;

    public Recommendation(long songId,double score) {
        this.score = score;
        this.songId = songId;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
