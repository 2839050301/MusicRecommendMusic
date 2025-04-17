package com.server.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.server.model.core.User;
import com.server.model.request.LoginUserRequest;
import com.server.model.request.RegisterUserRequest;
import com.server.utils.Constant;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

//对于用户具体处理业务的服务类
@Service
public class UserService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    private MongoCollection<Document> userCollection;

    //用于获取User表连接
    private MongoCollection<Document> getUserCollection() {
        if (userCollection == null)
            this.userCollection=mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_USER_COLLECTION);
        return this.userCollection;
    }

    //将User装成一个Document
    private Document userToDocument(User user) {
        try {
            Document document =Document.parse(objectMapper.writeValueAsString(user));
            return document;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    //将Document转换成User
    private User documentToUser(Document document) {
        try {
            User user = objectMapper.readValue(JSON.serialize(document), User.class);
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 提供用于注册的服务
     * @param request
     * @return
     */
    public boolean registerUser(RegisterUserRequest request) {
        //判断是否有相同的用户名已经注册
        if (getUserCollection().find(new Document("username", request.getUsername())).first() != null)
            return false;

        //创建一个用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setGender(request.getGender());

        //插入一个用户
        Document document = userToDocument(user);
        if(null == document)
            return false;
        getUserCollection().insertOne(document);
        return true;
    }

    /**
     * 用于提供用户的登录
     * @param request
     * @return
     */
    public boolean loginUser(LoginUserRequest request){

        //需要找到这个用户
        Document document=getUserCollection().find(new Document("username",request.getUsername())).first();
        if(null == document)
            return false;
        User user = documentToUser(document);
        //验证密码
        if(null == user)
            return false;
       return request.getPassword().compareTo(user.getPassword())==0;
    }

    //用于帮我们通过用户名查询用户
    public User findUserByUsername(String username){
        Document document=getUserCollection().find(new Document("username",username)).first();
        if(null == document||document.isEmpty())
            return null;
        return documentToUser(document);
    }

    /**
     * 通过用户名获取用户ID
     * @param username 用户名
     * @return 用户ID，如果用户不存在返回-1
     */
    public int getUserIdByUsername(String username) {
        Document document = getUserCollection()
            .find(new Document("username", username))
            .projection(new Document("userId", 1))
            .first();
        return document != null ? document.getInteger("userId", -1) : -1;
    }
}
