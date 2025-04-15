package com.server.rest;


import com.server.model.core.User;
import com.server.model.request.LoginUserRequest;
import com.server.model.request.RegisterUserRequest;
import com.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//用于处理User相关的内容
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
     * @param model
     * @return
     */
    @RequestMapping(path="/register",produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public Model registerUser(@RequestParam("username") String username,@RequestParam("password") String password,@RequestParam("gender") String gender, Model model) {
        model.addAttribute("success", userService.registerUser(new RegisterUserRequest(username,password,gender)));
        return null;
    }

    /**
     * 需要提供用户登录功能
     * 访问:url:/rest/users/login/?username=abc&password=abc
     * 返回:(success:true)
     * @param username
     * @param password
     * @param model
     * @return
     */
    @RequestMapping(path = "/login",produces = "application/json",method = RequestMethod.GET)
    public Model login(@RequestParam("username") String username,@RequestParam("password") String password, Model model) {
        model.addAttribute("success",userService.loginUser(new LoginUserRequest(username,password)));
        return model;

    }


}
