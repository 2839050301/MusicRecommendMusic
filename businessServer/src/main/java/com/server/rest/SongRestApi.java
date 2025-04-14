package com.server.rest;

import org.springframework.ui.Model;

import javax.jws.WebParam;

//用于处理电影相关的功能
public class SongRestApi {

    //首页
    //提供获取实时推荐的接口
    public Model getRealtimeRecommendation(String username,Model model){
        return null;
    }

    //提供离线推荐信息的接口
    public Model getOfflineRecommendation(String username,Model model){
        return null;
    }

    //提供热门推荐信息的接口
    public Model geyHotRecommendation(Model model){
        return null;
    }


    //*********** 模糊检索 ***********
    public Model getFuzzySearchSongs(String query,Model model){
        return null;
    }

    //获取单个歌曲信息
    public Model getSongInfo(long songId,Model model){
        return null;
    }

    //提供歌曲收藏的功能
    public Model songLike(long songId,Model model){
        return null;
    }

    //提供歌曲类别的查找
    public Model getGenresSongs(String genres,Model model){
        return null;
    }

    //用户收藏页面
    public Model getUserLikeSongs(String username,Model model){
        return null;
    }
}
