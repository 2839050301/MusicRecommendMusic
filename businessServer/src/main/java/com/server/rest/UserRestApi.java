package com.server.rest;


import com.server.model.core.User;
import com.server.model.request.LoginUserRequest;
import com.server.model.request.RegisterUserRequest;
import com.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

//用于处理User相关的内容

@CrossOrigin(origins = "*")  // 确保类上有这个注解
@Controller
@RequestMapping("/rest/users")
public class UserRestApi {

    @Autowired
    private UserService userService;

    /**
     * 需要提供注册功能
     * 访问url:/rest/users/register?username=abc&password=abc&gender=femle
     * 返回:{success:true}
     * @param username
     * @param password
     * @param gender
     * @param
     * @return
     */
    @RequestMapping(path="/register",produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> registerUser(@RequestParam("username") String username,
                                            @RequestParam("password") String password,
                                            @RequestParam("gender") String gender) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", userService.registerUser(new RegisterUserRequest(username,password,gender)));
        return result;
    }

    /**
     * 需要提供用户登录功能
     * 访问:url:/rest/users/login/?username=abc&password=abc
     * 返回:(success:true)
     * @param username
     * @param password
     * @param
     * @return
     */
    @RequestMapping(path = "/login",produces = "application/json",method = RequestMethod.GET)
    @ResponseBody 
    public Map<String, Object> login(@RequestParam("username") String username,
                                   @RequestParam("password") String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", userService.loginUser(new LoginUserRequest(username,password)));
        return result;
    }

    /**
     * 获取用户ID
     */
    @RequestMapping(path = "/userId", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getUserId(@RequestParam("username") String username) {
        Map<String, Object> result = new HashMap<>();
        int userId = userService.getUserIdByUsername(username);
        result.put("success", userId != -1);
        result.put("userId", userId);
        return result;
    }
}
