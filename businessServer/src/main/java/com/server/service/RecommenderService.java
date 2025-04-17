package com.server.service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.server.model.recom.Recommendation;
import com.server.model.request.*;
import com.server.utils.Constant;
import org.bson.Document;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.*;


//用于推荐服务
@Service
public class RecommenderService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private TransportClient esClient;


    private MongoDatabase mongoDatabase;

    private MongoDatabase getMongoDatabase(){
        if(mongoDatabase==null)
            this.mongoDatabase=mongoClient.getDatabase(Constant.MONGO_DATABASE);
        return mongoDatabase;
    }



    // 模糊查询方法
    // 修改后的模糊查询方法
    public List<Map<String, Object>> fuzzySearch(String query) {
        try {
            String decodedQuery = URLDecoder.decode(query, "UTF-8");
            
            System.out.println("========= 查询调试信息 =========");
            System.out.println("原始查询: " + query);
            System.out.println("解码后查询: " + decodedQuery);
            
            // 添加查询分析调试
            AnalyzeResponse analyzeResponse = esClient.admin().indices()
                .prepareAnalyze(decodedQuery)
                .setAnalyzer("ik_smart")
                .get();
            System.out.println("分词结果: " + analyzeResponse.getTokens());

            // 构建多条件查询
            SearchResponse response = esClient.prepareSearch(Constant.ES_INDEX)
                .setTypes(Constant.ES_TYPE)
                .setQuery(QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchQuery("sname_song", decodedQuery)
                        .analyzer("ik_smart")  // 明确指定分词器
                        .boost(3))
                    .should(QueryBuilders.matchPhraseQuery("sname_song", decodedQuery)
                        .slop(2)  // 适当减小词间距
                        .analyzer("ik_smart")
                        .boost(5))
                    .should(QueryBuilders.termQuery("sname_song.keyword", decodedQuery)
                        .boost(7))  // 完全匹配权重最高
                    .minimumShouldMatch(1))
                .setSize(10)
                .get();

            System.out.println("命中结果数: " + response.getHits().getTotalHits());
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println("匹配结果: " + hit.getSourceAsString());
                Map<String, Object> source = hit.getSourceAsMap();
                Map<String, Object> songInfo = new HashMap<>();
                songInfo.put("songId", source.get("songId"));
                songInfo.put("sname", source.get("sname_song"));
                songInfo.put("hot", source.get("hot"));
                songInfo.put("url", source.get("url"));
                songInfo.put("singerName", source.get("SingerName"));
                songInfo.put("genre", source.get("GenreName"));
                songInfo.put("tags", source.get("tags"));
                songInfo.put("languages", source.get("languages"));
                results.add(songInfo);
            }
            return results;
        } catch (Exception e) {
            System.err.println("模糊查询异常: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // 初始化ES客户端方法
    public void initEsClient() {
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "es-cluster") // 确保与ES服务端配置一致
                    .build();
            esClient = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName("192.168.187.131"), 9300)); // 使用transport端口9300
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化Elasticsearch客户端失败", e);
        }
    }
}