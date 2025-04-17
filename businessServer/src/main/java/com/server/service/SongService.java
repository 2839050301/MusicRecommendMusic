package com.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.util.JSON;
import com.server.model.core.Singer;
import com.server.model.core.Song;
import com.server.model.core.User_like;
import com.server.model.recom.Recommendation;
import com.server.utils.Constant;
import com.server.utils.PinyinUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService {

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> songCollection;

    private MongoCollection<Document> getSongCollection(){
        if(null==songCollection)
            this.songCollection=mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_SONG_COLLECTION);
        return this.songCollection;
    }

    public List<Singer> getSingers(int num) {
        try {
            FindIterable<Document> documents = getSingerCollection()
                .find()
                .limit(num);
                
            List<Singer> result = new ArrayList<>();
            for(Document document : documents) {
                result.add(new Singer(
                    document.getLong("SingerId"),
                    document.getString("sname")
                ));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private MongoCollection<Document> getSingerCollection() {
        return mongoClient.getDatabase(Constant.MONGO_DATABASE)
                .getCollection(Constant.MONGO_SINGER_COLLECTION);
    }




    private Object documentToSinger(Document document) {
            Singer singer = new Singer();
            singer.setSingerId(document.getLong("singerId"));
            singer.setSname(document.getString("sname"));
            return singer;
    }

    private Song documentToSong(Document document) {
        try {
            Song song = new Song();
            song.setSongId(document.getLong("songId"));
            song.setSname(document.getString("sname"));
            song.setSingerId(document.getLong("SingerId")); // 注意这里是大写S
            song.setHot(document.getInteger("hot"));
            song.setGenre(document.getInteger("genre"));
            song.setUrl(document.getString("url"));
            song.setTags(document.getString("tags"));
            song.setLanguages(document.getString("languages"));
            return song;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Document songToDocument(Song song){
        try {
            Document document=Document.parse(objectMapper.writeValueAsString(song));
            return document;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Song> getSongsBySid(List<Long> ids){
        List<Song> result=new ArrayList<>();
        FindIterable<Document> documents=getSongCollection().find(Filters.in("songId",ids));

        for(Document item:documents){
            result.add(documentToSong(item));
        }
        return result;
    }

    public List<Song> getHotSongs(int num) {
        try {
            FindIterable<Document> documents = getSongCollection()
                .find()
                .sort(new Document("hot", -1))
                .limit(num);
                
            List<Song> result = new ArrayList<>();
            for(Document document : documents) {
                result.add(documentToSong(document));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Singer> getSingersByInitial(String initial) {
        try {
            // 获取所有歌手数据
// 由于 getAllSingers 方法未定义，使用已有的 getSingers 方法获取所有歌手，这里传入一个较大的数作为参数
List<Singer> allSingers = getSingers(Integer.MAX_VALUE);
            
            // 过滤匹配首字母或拼音首字母的歌手
            return allSingers.stream()
                .filter(singer -> {
                    String name = singer.getSname();
                    String pinyinInitial = PinyinUtils.getPinyinInitial(name);
                    return name.toUpperCase().startsWith(initial.toUpperCase()) || 
                           pinyinInitial.startsWith(initial.toUpperCase());
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Song> getSongsBySingerId(long singerId) {
        try {
            Document query = new Document("SingerId", singerId);
            FindIterable<Document> documents = getSongCollection().find(query);
            
            List<Song> result = new ArrayList<>();
            for(Document document : documents) {
                result.add(new Song(
                    document.getLong("songId"),
                    document.getString("sname"),
                    document.getLong("SingerId"),
                    document.getInteger("hot"),
                    document.getInteger("genre"),
                    document.getString("url"),
                    document.getString("tags"),
                    document.getString("languages")
                ));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Singer getSingerById(long singerId) {
        try {
            Document query = new Document("SingerId", singerId);
            FindIterable<Document> documents = getSingerCollection().find(query);
            List<Singer> result = new ArrayList<>();
            for(Document doc : documents){
                result.add(new Singer(
                        doc.getLong("SingerId"),
                        doc.getString("sname")

                ));
            }
            return result.size() > 0 ? result.get(0) : null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<User_like> getUserLikedSongs(int userId) {
        Document doc = new Document("userId",userId);
        FindIterable<Document> documents = mongoClient.getDatabase(Constant.MONGO_DATABASE)
            .getCollection(Constant.MONGO_USER_LIKE_COLLECTION).find(doc);
        
        List<User_like> user_likes = new ArrayList<>();
        for(Document document:documents){
            user_likes.add(new User_like(
                document.getInteger("userId"),
                document.getLong("songId")
            ));
        }
        return user_likes;
    }

    public Song getSongById(long songId) {
        try {
            Document query = new Document("songId", songId);
            Document document = getSongCollection().find(query).first();
            return documentToSong(document);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean likeSong(int userId, long songId) {
        try {
            Document query = new Document()
                    .append("userId", userId)
                    .append("songId", songId);
            
            // 检查是否已存在
            if (mongoClient.getDatabase(Constant.MONGO_DATABASE)
                    .getCollection(Constant.MONGO_USER_LIKE_COLLECTION)
                    .count(query) > 0) {
                return false;
            }
            
            // 添加新记录
            Document doc = new Document()
                    .append("userId", userId)
                    .append("songId", songId);
            
            mongoClient.getDatabase(Constant.MONGO_DATABASE)
                    .getCollection(Constant.MONGO_USER_LIKE_COLLECTION)
                    .insertOne(doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unlikeSong(int userId, long songId) {
        try {
            Document query = new Document()
                    .append("userId", userId)
                    .append("songId", songId);

            DeleteResult result = mongoClient.getDatabase(Constant.MONGO_DATABASE)
                    .getCollection(Constant.MONGO_USER_LIKE_COLLECTION)
                    .deleteOne(query);

            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
