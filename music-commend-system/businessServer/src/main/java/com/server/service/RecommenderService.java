
    /**
     * 获取混合推荐结果
     * @param request
     * @return
     */
    public List<Recommendation> getHybridRecommendations(GetHybirdRecommendationRequest request) {
        // 获得实时推荐结果
        List<Recommendation> streamRecs = getStreamRecsSongs(new GetStreamRecsRequest(request.getUserId(), request.getNum()));

        // 获得ALS离线推荐结果
        List<Recommendation> userRecs = getUserCFSongs(new GetUserCFRequest(request.getUserId(), request.getNum()));

        // 获得基于内容推荐结果
        List<Recommendation> contentRecs = getContentBasedRecommendation(new GetContentBasedRecommendationRequest(request.getUserId(), request.getNum()));

        // 混合推荐逻辑
        List<Recommendation> hybridRecs = new ArrayList<>();

        // 添加实时推荐结果
        for (Recommendation rec : streamRecs) {
            hybridRecs.add(new Recommendation(rec.getSongId(), rec.getScore() * request.getStreamShare()));
        }

        // 添加ALS离线推荐结果
        for (Recommendation rec : userRecs) {
            hybridRecs.add(new Recommendation(rec.getSongId(), rec.getScore() * request.getAlsShare()));
        }

        // 添加基于内容推荐结果
        for (Recommendation rec : contentRecs) {
            hybridRecs.add(new Recommendation(rec.getSongId(), rec.getScore() * request.getContextShare()));
        }

        // 按得分排序并取前N个结果
        hybridRecs.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));
        return hybridRecs.stream().limit(request.getNum()).collect(Collectors.toList());
    }
