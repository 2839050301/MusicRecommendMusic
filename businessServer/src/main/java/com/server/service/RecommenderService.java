package com.server.service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.server.model.recom.Recommendation;
import com.server.model.request.*;
import com.server.utils.Constant;
import org.bson.Document;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//用于推荐服务
@Service
public class RecommenderService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private TransportClient esClient;
    /**
     * 获取混合推荐结果
     * @param request
     * @return
     */
    public List<Recommendation> getHybridRecommendations(GetHybirdRecommendationRequest request) {
        //获得实时推荐结果
        //List<Recommendation> streamRecs = getStreamRecsSongs(new GetStreamRecsRequest(request.getUserId(),request.getNum()));

        //获得ALS离线推荐结果
        //List<Recommendation> userRecs = getUserCFSongs(new GetUserCFRequest(request.getUserId(),request.getNum()));

        //获得基于内容推荐结果


        //返回结果
        return null;
    }

    /**
     * 获取基于内容的推荐结果
     * @param request
     * @return
     */
    public List<Recommendation> getContentBasedRecommendation(GetContentBasedRecommendationRequest request){
        MoreLikeThisQueryBuilder queryBuilder= QueryBuilders.moreLikeThisQuery(
                new MoreLikeThisQueryBuilder.Item[]{
                        new MoreLikeThisQueryBuilder.Item(Constant.ES_INDEX,Constant.ES_TYPE,String.valueOf(request.getSid()))
                });
        SearchResponse response=esClient.prepareSearch(Constant.ES_INDEX).setQuery(queryBuilder).setSize(request.getSum()).execute().actionGet();
        return paraseESResponse(response);
    }

    //用于解析Elasticsearch的查询响应
    private List<Recommendation> paraseESResponse(SearchResponse response) {
        List<Recommendation> recommendations=new ArrayList<>();
        for(SearchHit hit:response.getHits()) {
            Map<String,Object> hitContens=hit.getSourceAsMap();
            recommendations.add(new Recommendation((long)hitContens.get("sid"),0D));
        }
        return recommendations;
    }

    /**
     * 用于获取ALS算法中用户推荐矩阵
     * @param request
     * @return
     */
    public List<Recommendation> getUserCFSongs(GetUserCFRequest request){
        MongoCollection<Document> userCFCollection=mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_USER_RECS_COLLECTION);
        Document document=userCFCollection.find(new Document("userId",request.getUserId())).first();
        return paraseDocument(document,request.getSum());
    }

    //用于解析这个document
    private List<Recommendation> paraseDocument(Document document,int sum) {
        List<Recommendation> result=new ArrayList<>();
        if(null==document||document.isEmpty())
            return result;
        ArrayList<Document> documents=document.get("recs",ArrayList.class);
        for(Document item:documents){
            result.add(new Recommendation(item.getLong("songId"),item.getDouble("score")));
        }
        return result.subList(0,result.size()>sum?sum:result.size());
    }

    /**
     * 实时推荐结果
     * @param request
     * @return
     */
    public List<Recommendation> getStreamRecsSongs(GetStreamRecsRequest request){
        MongoCollection<Document> streamRecsCollection = mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_STREAMRECS_COLLECTION);
        Document document=streamRecsCollection.find(new Document("userId",request.getUserId())).first();
        return paraseDocument(document,request.getSum());
    }

    /**
     * 获取男女Top榜 用于处理冷启动问题
     * @param request
     * @return
     */
    public List<Recommendation> getGenderTopSongs( GetGenderTopSongsRequest request){
        Document genderDocument=mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_GENDERTOPSONGS_COLLECTION).find(new Document("gender",request.getGender())).first();
        List<Recommendation> recommendations=new ArrayList<>();
        if(genderDocument==null||genderDocument.isEmpty())
            return recommendations;
        return paraseDocument(genderDocument,request.getNum());
    }
}
