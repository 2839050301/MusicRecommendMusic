package com.server.model.request;

//混合推荐
public class GetHybirdRecommendationRequest {
    //实时推荐结果的占比
    private double streamShare;

    //基于ALS的离线推荐的占比
    private double alsShare;

    //基于ES的内容结果的占比
    private double contextShare;

    private int userId;

    private int num;

    public GetHybirdRecommendationRequest(double streamShare, double alsShare, int userId, double contextShare, int num) {
        this.streamShare = streamShare;
        this.alsShare = alsShare;
        this.userId = userId;
        this.contextShare = contextShare;
        this.num = num;
    }

    public double getStreamShare() {
        return streamShare;
    }

    public void setStreamShare(double streamShare) {
        this.streamShare = streamShare;
    }

    public double getAlsShare() {
        return alsShare;
    }

    public void setAlsShare(double alsShare) {
        this.alsShare = alsShare;
    }

    public double getContextShare() {
        return contextShare;
    }

    public void setContextShare(double contextShare) {
        this.contextShare = contextShare;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
