package com.server.model.request;


//新建用户的请求封装
public class RegisterUserRequest {

    private String username;
    private String password;
    private String gender;

    public RegisterUserRequest(String username, String password, String gender) {
        this.username = username;
        this.password = password;
        this.gender= gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getgender() {
        return gender;
    }

    public void setgenre(String gender) {
        this.gender = gender;
    }
}
