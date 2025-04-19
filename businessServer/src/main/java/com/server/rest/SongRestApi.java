package com.server.rest;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.server.model.core.Singer;
import com.server.model.core.Song;
import com.server.model.core.User;
import com.server.model.core.User_like;
import com.server.model.recom.Recommendation;
import com.server.model.request.GetGenderTopSongsRequest;
import com.server.model.request.GetPopularSongsRequest;
import com.server.model.request.GetStreamRecsRequest;
import com.server.model.request.GetUserCFRequest;
import com.server.service.RecommenderService;
import com.server.service.SongService;
import com.server.service.UserService;
import com.server.utils.Constant;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.WebParam;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//用于处理电影相关的功能
@Controller
@RequestMapping("/rest/songs")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class SongRestApi {

    private static final Logger LOGGER = Logger.getLogger(SongRestApi.class.getName());

    @Autowired
    private RecommenderService recommenderService;

    @Autowired
    private UserService userService;

    @Autowired
    private SongService songService;



    //首页










    
    //确保这个方法是实际实现
    @RequestMapping(path = "/fuzzy", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model fuzzySearch(@RequestParam("query") String query, Model model) {
        try {
            List<Map<String, Object>> results = recommenderService.fuzzySearch(query);
            model.addAttribute("success", true);
            model.addAttribute("songs", results); // 确保前端使用相同的字段名
            if (results.isEmpty()) {
                LOGGER.info("未找到匹配歌曲: " + query);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "模糊查询失败: " + query, e);
            model.addAttribute("success", false);
            model.addAttribute("message", "查询失败: " + e.getMessage());
        }
        return model;
    }

    //获取单个歌曲信息
    @RequestMapping(path = "/song", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSongInfo(@RequestParam("songId") long songId, Model model) {
        try {
            Song song = songService.getSongById(songId);
            model.addAttribute("success", song != null);
            model.addAttribute("song", song);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while getting song info", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "An error occurred: " + (e.getMessage() != null? e.getMessage() : "unknown error"));
        }
        return model;
    }

    //提供歌曲类别的查找
    public Model getGenresSongs(String genres, Model model) {
        return null;
    }

    // 提供热门歌曲榜单接口
    @RequestMapping(path = "/hotSongs", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getHotSongs(@RequestParam("num") int num, Model model) {
        try {
            List<Song> songs = songService.getHotSongs(num);
            model.addAttribute("success", true);
            model.addAttribute("songs", songs);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while getting hot songs", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "An error occurred: " + (e.getMessage() != null? e.getMessage() : "unknown error"));
        }
        return model;
    }

    @RequestMapping(path = "/singers", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSingers(@RequestParam("num") int num, Model model) {
        try {
            List<Singer> singers = songService.getSingers(num);
            model.addAttribute("success", true);
            model.addAttribute("singers", singers);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while getting singers", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "An error occurred: " + (e.getMessage() != null? e.getMessage() : "unknown error"));
        }
        return model;
    }

    /**
     * 提供歌手首字母的查找
     * @param initial
     * @param model
     * @return
     */
    @RequestMapping(path = "/singers/initial", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSingersByInitial(@RequestParam("initial") String initial, Model model) {
        try {
            List<Singer> singers = songService.getSingersByInitial(initial);
            model.addAttribute("success", true);
            model.addAttribute("singers", singers);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while getting singers by initial", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "An error occurred: " + (e.getMessage() != null? e.getMessage() : "unknown error"));
        }
        return model;
    }

    @RequestMapping(path = "/singer", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSinger(@RequestParam("singerId") long singerId, Model model) {
        try {
            Singer singer = songService.getSingerById(singerId);
            model.addAttribute("success", singer != null);
            model.addAttribute("singer", singer);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while getting singer", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "An error occurred: " + (e.getMessage() != null? e.getMessage() : "unknown error"));
        }
        return model;
    }

    // 确保getSongsBySingerId方法正确实现
    @RequestMapping(path = "/singer/songs", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getSongsBySingerId(@RequestParam("singerId") long singerId, Model model) {
        try {
            List<Song> songs = songService.getSongsBySingerId(singerId);
            model.addAttribute("success", true);
            model.addAttribute("songs", songs);  // 确保字段名是"songs"
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while getting songs by singer id", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "An error occurred: " + (e.getMessage() != null? e.getMessage() : "unknown error"));
        }
        return model;
    }

    @RequestMapping(path = "/user/liked-songs", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getUserLikedSongs(@RequestParam("userId") int userId, Model model) {
        try {
            List<User_like> userLikes = songService.getUserLikedSongs(userId);
            model.addAttribute("success", true);
            model.addAttribute("User_like", userLikes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "获取用户喜欢歌曲失败", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "获取用户喜欢歌曲失败");
        }
        return model;
    }

    @RequestMapping(path = "/unlike", produces = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public Model unlikeSong(@RequestParam("userId") int userId, 
                       @RequestParam("songId") long songId, 
                       Model model) {
        try {
            boolean success = songService.unlikeSong(userId, songId);
            model.addAttribute("success", success);
            model.addAttribute("message", success ? "取消喜欢成功" : "取消喜欢失败");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "取消喜欢歌曲失败", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "取消喜欢歌曲失败");
        }
        return model;
    }

    @RequestMapping(path = "/like", produces = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public Model likeSong(
        @RequestParam("userId") int userId, 
        @RequestParam("songId") long songId, 
        Model model) {
        try {
            boolean success = songService.likeSong(userId, songId);
            model.addAttribute("success", success);
            model.addAttribute("message", success ? "添加喜欢成功" : "添加喜欢失败");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "添加喜欢歌曲失败", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "参数错误: " + e.getMessage());
        }
        return model;
    }

    // 基于内容的推荐接口
    @RequestMapping(path = "/content-based", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getContentBasedRecommendation(
            @RequestParam("songId") long songId,
            @RequestParam(value = "num", defaultValue = "10") int num,
            Model model) {
        try {
            List<Recommendation> recommendations = recommenderService.getContentBasedRecommendations(songId, num);
            model.addAttribute("success", true);
            model.addAttribute("recommendations", recommendations);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "获取基于内容的推荐失败", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "获取推荐失败: " + e.getMessage());
        }
        return model;
    }

    @RequestMapping(path = "/es/song", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getSongFromES(@RequestParam("songId") long songId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> songDetail = recommenderService.getSongDetailFromES(songId);
            
            if (songDetail == null) {
                result.put("success", false);
                result.put("message", "歌曲不存在");
                return result;
            }
            
            // 确保返回字段名统一
            Map<String, Object> formattedSong = new HashMap<>();
            formattedSong.put("songId", songDetail.get("songId"));
            formattedSong.put("sname_song", songDetail.get("sname_song"));
            formattedSong.put("SingerName", songDetail.get("SingerName"));
            formattedSong.put("GenreName", songDetail.get("GenreName"));
            formattedSong.put("hot", songDetail.get("hot"));
            formattedSong.put("url", songDetail.get("url"));
            
            result.put("success", true);
            result.put("song", formattedSong);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "从ES获取歌曲详情失败", e);
            result.put("success", false);
            result.put("message", "获取歌曲详情失败");
        }
        return result;
    }

    @RequestMapping(path = "/detail", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getSongDetail(
            @RequestParam("songId") long songId,
            @RequestParam(value = "num", defaultValue = "5") int num) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从ES获取歌曲详情
            Map<String, Object> songDetail = recommenderService.getSongDetailFromES(songId);
            
            if (songDetail == null) {
                result.put("success", false);
                result.put("message", "歌曲不存在");
                return result;
            }
            
            // 获取相关音乐推荐
            List<Recommendation> recommendations = recommenderService.getContentBasedRecommendations(songId, num);
            
            result.put("success", true);
            result.put("song", songDetail);
            result.put("recommendations", recommendations);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "获取歌曲详情失败", e);
            result.put("success", false);
            result.put("message", "获取歌曲详情失败");
        }
        return result;
    }

}