package com.example.redis;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * redis缓存公共方法
 * @author Liangth
 */

public class RedisUtil {

    private static Logger logger= LoggerFactory.getLogger(RedisUtil.class);

    //redisSyIndexBrand    命名分解  redis（用来清除缓存时候，查找） SY（代表首页比如商城：SC） 后面随机变化
    private static final String index_Brand = "redisSyIndexBrand";

    private static CacheKit kit = new CacheKit();



}
