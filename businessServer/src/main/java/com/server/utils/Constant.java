package com.server.utils;


//定义整个业务常量
public class Constant {
    //************* MongoDB中的表名 *************
    public static final String MONGO_DATABASE = "recommender";

        //************* 歌曲表名 *************
    public static final String MONGO_SONG_COLLECTION = "Songs";

        //************* 歌手表 *************
    public static final String MONGO_SINGER_COLLECTION = "Singer";

        //************* 歌曲标签表 *************
    public static final String MONGO_TAG_COLLECTION = "Tag";

        //************* 歌曲流派表名 *************
    public static final String MONGO_GENRE_COLLECTTION = "Genre";

        //************* 用户表 *************
    public static final String MONGO_USER_COLLECTION = "User";

         //************* 用户喜欢表 *************
    public static final String MONGO_USER_LIKE_COLLECTION = "User_like";

         //************* 热门音乐表 *************
    public static final String MONGO_POPULARSONGS_COLLECTION = "PopularSongs";

        //************* 歌曲类别Top30 *************
    public static final String MONGO_GENRESTOPSONGS_COLLECTION = "Genre_topSongs";

        //************* 男女喜欢Top30 *************
    public static final String MONGO_GENDERTOPSONGS_COLLECTION = "GenderTopSongs";

        //************* 用户的推荐矩阵 *************
    public static final String MONGO_USER_RECS_COLLECTION = "UserRecs";

         //************* 歌曲相似度矩阵 *************
    public static final String MONGO_SONGRECS_COLLECTION = "SongRecs";

        //************* 实时推荐歌曲表 *************
    public static final String MONGO_STREAMRECS_COLLECTION = "StreamRecs";


    //************* ES *************
    public static final String ES_INDEX = "recommender";
    public static final String ES_TYPE = "Songs";

        //************* Redis *************
    public static final int USER_RATING_QUEUE_SIZE=20;

        //************* LOG *************
    public static final String USER_RATING_LOG_PREFIX="USER_RATING_LOG_PREFIX";
}
