package com.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import com.server.model.core.Song;
import com.server.model.recom.Recommendation;
import com.server.utils.Constant;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private Song documentToSong(Document document){
        try {
            Song song=objectMapper.readValue(JSON.serialize(document),Song.class);
            return song;
        } catch (IOException e) {
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
        FindIterable<Document> documents=getSongCollection().find(Filters.in("sid",ids));

        for(Document item:documents){
            result.add(documentToSong(item));
        }
        return result;
  }
}
