package com.server.rest;


import com.server.model.core.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

//用于处理User相关的内容
@Controller
@RequestMapping("/rest/users")
public class UserRestApi {

    //需要提供注册功能
    @RequestMapping(path="/register",produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public Model registerUser(String username, String password,String sex,Model model) {

        return null;
    }

    //需要提供用户登录功能
    public Model login(String username, String password, Model model) {
        User user = new User();

        return null;
    }


}
