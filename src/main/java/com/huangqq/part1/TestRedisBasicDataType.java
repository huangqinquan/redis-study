package com.huangqq.part1;

import com.huangqq.config.BeanConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * 测试redis的基础数据类型
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BeanConfig.class)
public class TestRedisBasicDataType {

    @Autowired
    private JedisPool jedisPool;

    private Jedis jedis;

    /**
     * 每个测试开始之前都要获取连接
     */
    @Before
    public void beforeTest(){
        jedis = jedisPool.getResource();
    }

    @Test
    public void testRedisString(){
        // 清空当前数据的所有数据
        jedis.flushDB();

        // 保存一个字符串
        jedis.set("language","java");

        // 在原来key对应的值追加一个字符串，如果key不存在，则创建一个新的key
        jedis.append("language","c++");

        // 获取一个字符串
        String s = jedis.get("language");
        Assert.assertEquals("javac++",s);

        // 自增1，redis会转换成整型，再进行加操作。等价于count++。默认为0
        jedis.incr("count");
        //count = 1

        // 在原有值的基础增加指定的值，等价于count+=10;
        jedis.incrBy("count",10);
        //count = 11

        // 浮点数自增 对不存在的数自增相当于 price += 1.2
        Double price = jedis.incrByFloat("price",1.2);
        Assert.assertTrue(price == 1.2);

        // 自减1，等价于count--
        jedis.decr("count");
        //count = 10

        // 减指定数量的值,等价于count-=5
        jedis.decrBy("count",5);
        //count = 5
        Assert.assertEquals(jedis.get("count"),"5");

        // 获取key旧的值，并设置一个新的值
        s = jedis.getSet("count","100");
        Assert.assertEquals(s,"5");

        // 获取指定范围的字符串，0：起始偏移  3：结束位置   类似substr这样的操作
        s = jedis.getrange("language",0,2);
        Assert.assertEquals(s, "jav");

        // 替换，0：替换起始位置，php：从0开始替换的字符串
        jedis.setrange("language",0,"php");
        Assert.assertEquals(jedis.get("language"),"phpac++");

        // 只有key不存在才保存,key不存在返回1，存在返回0
        long r = jedis.setnx("count","200");
        Assert.assertTrue(r == 0);

        // 获取字符串的长度
        long len = jedis.strlen("language");
        Assert.assertTrue("phpac++".length() == len);

        // 同时设置多个key的值
        jedis.mset("language","java", "count","2000");

        // 获取多个key的值
        List<String> list = jedis.mget("language","count");
        System.out.println(list);
        Assert.assertTrue(list.size() == 2 && list.get(0).equals("java") && list.get(1).equals("2000"));

        // 同时设置多个key，且key不存在
        r = jedis.msetnx("auth","yangxin","age","18");
        Assert.assertTrue(r == 1);

        // 保存带有效期的key，单位为毫秒
        jedis.psetex("expir_key",2000,"abcde");
        try {
            Thread.sleep(2100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean b = jedis.exists("expir_key");
        Assert.assertTrue(!b);

        // 保存带有效期的key，单位为秒
        jedis.setex("expir_key2",3,"test");
    }

    /**
     * 每个测试结束之前要关闭连接
     */
    @After
    public void  afterTest(){
        jedis.close();
    }
}
