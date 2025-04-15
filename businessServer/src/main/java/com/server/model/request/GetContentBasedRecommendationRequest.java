package com.server.model.request;

public class GetContentBasedRecommendationRequest {
    private long sid;
    private int sum;

    public GetContentBasedRecommendationRequest(long sid, int sum) {
        this.sid = sid;
        this.sum = sum;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}
