package com.server.rest;

import com.server.model.core.Song;
import com.server.model.core.User;
import com.server.model.recom.Recommendation;
import com.server.model.request.GetGenderTopSongsRequest;
import com.server.model.request.GetStreamRecsRequest;
import com.server.model.request.GetUserCFRequest;
import com.server.service.RecommenderService;
import com.server.service.SongService;
import com.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//用于处理电影相关的功能
@Controller
@RequestMapping("/rest/songs")
public class SongRestApi {

    @Autowired
    private RecommenderService recommenderService;

    @Autowired
    private UserService userService;

    @Autowired
    private SongService songService;
    //首页

    /**
     * 提供获取实时推荐的接口
     * 访问url /rest/songs/stream?userId=123
     * 返回:{success:true,songs[]}
     * @param username
     * @param model
     * @return
     */
    @RequestMapping(path = "/stream",produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public Model getRealtimeRecommendation(@RequestParam("username") String username, @RequestParam("num") int sum,Model model){
        User user=userService.findUserByUsername(username);
        List<Recommendation> recommendations=recommenderService.getStreamRecsSongs(new GetStreamRecsRequest(user.getUserId(),sum));
        //处理冷启动
        if(recommendations.size()==0){
            recommendations=recommenderService.getGenderTopSongs(new GetGenderTopSongsRequest(user.getGender(),sum));
        }
        List<Long> ids=new ArrayList<>();
        for(Recommendation recom:recommendations){
            ids.add(recom.getSongId());
        }
        List<Song> result=songService.getSongsBySid(ids);
        model.addAttribute("success",true);
        model.addAttribute("songs",result);
        return null;
    }

    //提供离线推荐信息的接口
    @RequestMapping(path = "/offline",produces = "application/json",method = RequestMethod.GET)
    @ResponseBody
    public Model getOfflineRecommendation(@RequestParam("username") String username,@RequestParam("num") int num,Model model){
        User user=userService.findUserByUsername(username);
        List<Recommendation> recommendations=recommenderService.getUserCFSongs(new GetUserCFRequest(user.getUserId(),num));
        if(recommendations.size()==0){
            recommendations=recommenderService.getGenderTopSongs(new GetGenderTopSongsRequest(user.getGender(),num));
        }
        List<Long> ids=new ArrayList<>();
        for(Recommendation recom:recommendations){
            ids.add(recom.getSongId());
        }
        List<Song> result=songService.getSongsBySid(ids);
        model.addAttribute("success",true);
        model.addAttribute("songs",result);
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
