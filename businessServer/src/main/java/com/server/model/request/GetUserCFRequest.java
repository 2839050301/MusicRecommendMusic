package com.server.model.request;

//获取 ALS算法下的用户推荐矩阵
public class GetUserCFRequest {
    private int userId;

    private int sum;

    public GetUserCFRequest(int userId, int sum) {
        this.userId = userId;
        this.sum = sum;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}
