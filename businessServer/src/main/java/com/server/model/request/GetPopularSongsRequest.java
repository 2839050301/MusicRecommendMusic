package com.server.model.request;

//获取当前最热的电影
public class GetPopularSongsRequest {
    private int num;

    public GetPopularSongsRequest(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
