package com.server.model.request;

//获取男女Top歌曲
public class GetGenderTopSongsRequest {
    private String gender;

    private int num;

    public GetGenderTopSongsRequest(String gender, int num) {
        this.gender = gender;
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
