package com.example.common;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;

public class TestRedis {
    private static Jedis jedis;

    //启动本地的Redis服务
    @Before
    public void startUpRedis(){
        //连接本地的Redis服务
        jedis = new Jedis("127.0.0.1",6379);
        jedis.auth("123456");
        System.out.println("Redis连接成功！");
        //查看服务是否运行
        System.out.println("服务器正在运行: "+jedis.ping());
    }

    //字符串存储实例
    @Test
    public void RedisStringDemo(){
        // 设置redis字符串数据
        jedis.set("name", "xiao");

        // 获取存储的数据并输出
        System.out.println("字符串存储: " + jedis.get("name"));
    }

    @Test
    public void RedisListDemo(){
        //存储到数据中
        jedis.lpush("list", "Redis");
        jedis.lpush("list", "Java");
        jedis.lpush("list", "Node.js");
        //读取出来
        List<String> list = jedis.lrange("list", 0, 2);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("redis数组:"+list.get(i));
        }
    }



    public static void main(String[] args){
        TestRedis test=new TestRedis();
        //test.startUpRedis();
       // RedisStringDemo();
       test.RedisListDemo();
    }

}
