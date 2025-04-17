/*
 Navicat Premium Dump Script

 Source Server         : 192.168.187.131_27017
 Source Server Type    : MongoDB
 Source Server Version : 30403 (3.4.3)
 Source Host           : 192.168.187.131:27017
 Source Schema         : recommender

 Target Server Type    : MongoDB
 Target Server Version : 30403 (3.4.3)
 File Encoding         : 65001

 Date: 16/04/2025 12:37:01
*/


// ----------------------------
// Collection structure for GenderTopSongs
// ----------------------------
db.getCollection("GenderTopSongs").drop();
db.createCollection("GenderTopSongs");

// ----------------------------
// Collection structure for Genre
// ----------------------------
db.getCollection("Genre").drop();
db.createCollection("Genre");
db.getCollection("Genre").createIndex({
    genre_id: Int32("1")
}, {
    name: "genre_id_1"
});

// ----------------------------
// Collection structure for GenresTopSongs
// ----------------------------
db.getCollection("GenresTopSongs").drop();
db.createCollection("GenresTopSongs");

// ----------------------------
// Collection structure for PopularSongs
// ----------------------------
db.getCollection("PopularSongs").drop();
db.createCollection("PopularSongs");

// ----------------------------
// Collection structure for Singer
// ----------------------------
db.getCollection("Singer").drop();
db.createCollection("Singer");
db.getCollection("Singer").createIndex({
    Singerid: Int32("1")
}, {
    name: "Singerid_1"
});

// ----------------------------
// Collection structure for SongRecs
// ----------------------------
db.getCollection("SongRecs").drop();
db.createCollection("SongRecs");

// ----------------------------
// Collection structure for Songs
// ----------------------------
db.getCollection("Songs").drop();
db.createCollection("Songs");
db.getCollection("Songs").createIndex({
    sid: Int32("1")
}, {
    name: "sid_1"
});

// ----------------------------
// Collection structure for StreamRecs
// ----------------------------
db.getCollection("StreamRecs").drop();
db.createCollection("StreamRecs");

// ----------------------------
// Collection structure for Tag
// ----------------------------
db.getCollection("Tag").drop();
db.createCollection("Tag");
db.getCollection("Tag").createIndex({
    song_id: Int32("1")
}, {
    name: "song_id_1"
});

// ----------------------------
// Collection structure for User
// ----------------------------
db.getCollection("User").drop();
db.createCollection("User");
db.getCollection("User").createIndex({
    userId: Int32("1")
}, {
    name: "userId_1"
});

// ----------------------------
// Collection structure for UserRecs
// ----------------------------
db.getCollection("UserRecs").drop();
db.createCollection("UserRecs");

// ----------------------------
// Collection structure for User_like
// ----------------------------
db.getCollection("User_like").drop();
db.createCollection("User_like");
db.getCollection("User_like").createIndex({
    userId: Int32("1")
}, {
    name: "userId_1"
});
