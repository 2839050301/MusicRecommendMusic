package com.server.model.request;

//实时推荐请求
public class GetStreamRecsRequest {

    private long userId;
    private int sum;

    public GetStreamRecsRequest(long userId, int sum) {
        this.userId = userId;
        this.sum = sum;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

}
