package com.example.redis;

import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CacheKit {

    private static Logger logger= LoggerFactory.getLogger(CacheKit.class);
    private List<JSONObject> resultList;
    private static JedisPool pool;

    /**
     * 初始化redis连接池
     */
    public static void initializePool(){
        //redis与residPort的配置文件
        JedisPoolConfig config=new JedisPoolConfig();
        //设置最大连接数(100足以)
        config.setMaxTotal(100);
        //设置最大空闲连接数
        config.setMaxIdle(10);
        //获取Jedis的等待时间(50s)
        config.setMaxWaitMillis(50*1000);
        //获取Jedis连接时，自动检验连接是否可用
        config.setTestOnBorrow(true);
        //链接放回池中，自动检验连接是否有效
        config.setTestOnReturn(true);
        //自动测试池中的空闲连接是否都是可用链接
        config.setTestWhileIdle(true);
        //创建连接池
        pool=new JedisPool(config,"127.0.0.1",6379);
    }
    /**
     * 多线程环境同步初始化
     */
    private static synchronized void poolInit(){
        if( null == pool){
            initializePool();
        }
    }

    /**
     * 获取Jedis实例
     */
    private static Jedis getJedis(){
        if( null == pool){
            poolInit();
        }
        int timeoutCount=0;
        while(true){
            try {
                if( null != pool){
                    return pool.getResource();
                }
            } catch (Exception e) {
                if(e instanceof JedisConnectionException){
                    timeoutCount++;
                    logger.warn("getJedis timeoutCount={}:",timeoutCount);
                    if(timeoutCount > 3){
                        break;
                    }
                }else{
                     /* logger.warn("jedisInfo ... NumActive=" + pool.getResource().get("")
                            + ", NumIdle=" + pool.getNumIdle()
                            + ", NumWaiters=" + pool.getNumWaiters()
                            + ", isClosed=" + pool.isClosed());  */
                    logger.warn(pool.getResource()+"//"+pool);
                    logger.error("GetJedis error,", e);
                    break;
                }
            }
            break;
        }
        return null;
    }

    /**
     * 释放Jedis资源
     *
     * @param Jedis
     */
    private static void returnResource(Jedis jedis) {
        if (null != jedis) {
            pool.returnResourceObject(jedis);
        }
    }

    /**
     * 绝对获取方法（保证一定能够使用可用的连接获取到 目标数据）
     * Jedis连接使用后放回
     * @param key
     * @return
     */
    private String safeGet(String key) {
        Jedis jedis = getJedis();
        while (true) {
            if (null != jedis) {
                break;
            } else {
                jedis = getJedis();
            }
        }
        String value = jedis.get(key);
        returnResource(jedis);
        return value;
    }

    /**
     * 绝对设置方法（保证一定能够使用可用的链接设置 数据）
     * Jedis连接使用后返回连接池
     * @param key
     * @param time
     * @param value
     */
    public void safeSet(String key, int time, String value) {
        Jedis jedis = getJedis();
        while (true) {
            if (null != jedis) {
                break;
            } else {
                jedis = getJedis();
            }
        }
        jedis.setex(key, time, value);
        returnResource(jedis);
    }

    /**
     * 绝对删除方法（保证删除绝对有效）
     * Jedis连接使用后返回连接池</span>
     * @param key
     */
    private void safeDel(String key) {
        Jedis jedis = getJedis();
        while (true) {
            if (null != jedis) {
                break;
            } else {
                jedis = getJedis();
            }
        }
        jedis.del(key);
        returnResource(jedis);
    }

    /**
     * 清除所有缓存
     */
    private static void clearCache() {
        CacheKit kit = new CacheKit();
        Jedis jedis = getJedis();
        while (true) {
            if (null != jedis) {
                break;
            } else {
                jedis = getJedis();
            }
        }
        //PS:redis默认有一些key已经存在里面，不能删除，所以后面在添加key的时候用一个统一的标识符这样将自己添加的删除
        Iterator it = jedis.keys("redis*").iterator();	//带*号清除所有，如果写有前缀就匹配出来sy*
        while (it.hasNext()) {
            String key = (String) it.next();
            kit.delByCache(key);	//删除key
            logger.info(new Date()+"：将redis缓存"+key+"值删除成功！");
        }
        returnResource(jedis);
    }


    /**自定义的一些 get set del 方法，方便使用  在其他地方直接调用**/
    public JSONObject getByCache(String key) {
        String result = safeGet(key);
        if (result != null) {
            return (JSONObject) JSONObject.parse(result);
        }
        return null;
    }

    public String getByCacheToString(String key) {
        String result = safeGet(key);
        if (result != null) {
            return result;
        }
        return null;

    }

    public List<JSONObject> getArrayByCache(String key) {
        String result = safeGet(key);
        if (result != null) {
            resultList = JSONArray.parseArray(result, JSONObject.class);
            return resultList;
        }
        return null;
    }

    public JSONArray getJSONArrayByCache(String key) {
        String result = safeGet(key);
        if (result != null) {
            return JSONArray.parseArray(result);
        }
        return null;
    }

    public void setByCache(String key, String s) {
        safeSet(key, 86400, s);
    }

    public void setByCacheOneHour(String key, String s) {
        safeSet(key, 3600, s);
    }

    public void setByCacheOneHour(String key, List<JSONObject> json) {
        safeSet(key, 86400, JSONObject.toJSONString(json));
        resultList = json;
    }

    public void setByCache(String key, JSONObject json) {
        safeSet(key, 86400, JSONObject.toJSONString(json));
    }

    public void setByCache(String key, List<JSONObject> list) {
        safeSet(key, 86400, JSONObject.toJSONString(list));
        resultList = list;
    }

    public void setByCache(String key, JSONArray array) {
        safeSet(key, 86400, JSONArray.toJSONString(array));
    }

    public void setByCacheCusTime(String key, String s, int time) {
        safeSet(key, time, s);
    }

    public void delByCache(String key) {
        //该方法删除指定的key
        if (null != safeGet(key)) {
            safeDel(key);
        }
    }

    //该方法用来清除所有相关redis的key
    public void delRedisRelevantKey(){
        clearCache();
    }

    public JSONObject toJSON(JSONObject db) {
        return (JSONObject) JSONObject.toJSON(db);
    }

    public List<JSONObject> toJSON(List<JSONObject> list) {
        List<JSONObject> json = new ArrayList<>();
        for (JSONObject aList : list) {
            json.add((JSONObject) JSONObject.toJSON(aList));
        }
        return json;
    }

    public boolean notNull() {
        return resultList != null && resultList.size() > 0;
    }

    public List<JSONObject> getResult() {
        return resultList;
    }

    public static void main(String[] args) {
        //clearCache();  到这里自己去测试一下是否可以
    }

}
