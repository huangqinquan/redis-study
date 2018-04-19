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
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 测试字符串数据类型
     */
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
     * 测试redis的hash类型
     * 相当于k->map对
     */
    @Test
    public void testRedisHash(){
        jedis.flushDB();
        // 添加元素，如果key存在则修改，且返回0.如果key不存在，则添加，返回1
        long r = jedis.hset("user", "name","jiesi");
        jedis.hset("user","age","18");
        Assert.assertEquals(r,1);

        // 如果key不存在则添加，否则操作失败。key存在返回0,否则返回1
        r = jedis.hsetnx("user","name","郭德纲");
        Assert.assertTrue(r == 0);

        // 获取某一key的值
        String s = jedis.hget("user","name");
        System.out.println(s);
        Assert.assertEquals(s,"jiesi");

        // 获取所有值
        Map<String, String> map = jedis.hgetAll("user");
        for (Map.Entry<String,String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        Assert.assertTrue(map.size() == 2);

        // 判断某个key的字段是否存在
        boolean b = jedis.hexists("user","sex");
        Assert.assertTrue(!b);

        // 对某个field累加指定的整数值，返回累加后的值
        r = jedis.hincrBy("user","age",10);
        System.out.println(r);
        Assert.assertTrue(r == 28);

        // 对某个field累加指定的浮点数值，返回累加后的值
        double f = jedis.hincrByFloat("user","height",1.75);
        System.out.println(f);
        Assert.assertTrue(f == 1.75);

        // 获取hash的字段数量
        r = jedis.hlen("user");
        Assert.assertEquals(r,3);

        // 获取多个字段的值
        List<String> list = jedis.hmget("user","name","age");
        System.out.println(list);
        Assert.assertTrue(list.size() == 2);

        // 获取所有字段的值
        list = jedis.hvals("user");
        System.out.println(list);
        Assert.assertTrue(list.size() == 3);

        // 删除字段
        r = jedis.hdel("user","name","age");
        System.out.println(r);
        Assert.assertTrue(r == 2);

        // 查找字段
        jedis.hset("user","js1","value1");
        jedis.hset("user","xxxjs2","value2");
        jedis.hset("user","abcjs3","value3");
        jedis.hset("user","1abcjs","value4");
        jedis.hset("user","2abc2js3","value5");
        jedis.hset("user","name","yangxin");
        ScanParams scanParams = new ScanParams();
        scanParams.match("*js*");    // 查找包含js的字段
        ScanResult<Map.Entry<String, String>> sr = jedis.hscan("user","0",scanParams);
        System.out.println(sr.getResult().size());
        Assert.assertTrue(sr.getResult().size() == 5);
        for (Iterator<Map.Entry<String, String>> iterator = sr.getResult().iterator(); iterator.hasNext();) {
            Map.Entry<String, String> entry = iterator.next();
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

    }


    /**
     * 测试redis的list数据结构
     */
    @Test
    public void testRedisList(){
        jedis.flushDB();
        // 向头添加元素，返回链表的长度
        long r = jedis.lpush("users","yangxin","yangli","yangfang","huge","guodegang");
        System.out.println(r);
        Assert.assertTrue(r == 5);

        // 向尾添加元素，返回链表的长度
        r = jedis.rpush("users","likaifu","mayun","mahuateng");
        System.out.println(r);
        Assert.assertTrue(r == 8);

        // 返回链表长度
        r = jedis.llen("users");
        System.out.println(r);
        Assert.assertTrue(r == 8);

        // 获取链表元素,0:开始索引，-1：结束索引，-1表示取链表长度
        List<String> list = jedis.lrange("users",0,-1);
        System.out.println(list);
        Assert.assertTrue(list.size() == 8);

        // 从存于 key 的列表里移除前 count 次出现的值为 value 的元素 count>0 正向 count<0 反向
        jedis.rpush("users","mahuateng","mahuateng");
        r = jedis.lrem("users",2,"mahuateng");
        System.out.println(r);
        Assert.assertTrue(r == 2);

        // 弹出列表中第一个元素（删除并返回第一个元素）
        String s = jedis.lpop("users");
        System.out.println(s);
        Assert.assertEquals(s,"guodegang");

        // 弹出列表中最后一个元素
        s = jedis.rpop("users");
        System.out.println(s);
        Assert.assertEquals(s,"mahuateng");

        // 往队列头部插入元素，列表必须存在
        r = jedis.lpushx("users2","laoyang");
        System.out.println(r == 0);

        // 修改队列中指定索引的值
        s = jedis.lset("users",0,"yangxin");
        System.out.println(s);
        Assert.assertEquals(s,"OK");

        // 截取并保存链表中指定范围的元素
        s = jedis.ltrim("users",0,5);
        System.out.println(s);

        // 返回指定索引的元素
        s = jedis.lindex("users",0);
        System.out.println(s);
        Assert.assertEquals(s,"yangxin");

        System.out.println("users: " + jedis.lrange("users",0,-1));

        // 弹出users最后一个元素，保存到user2链表中，并返回
        s = jedis.rpoplpush("users","users2");
        System.out.println(jedis.lrange("users2",0,-1));

        // 同上，只是有阻塞超时时长，单位：毫秒
        s = jedis.brpoplpush("users","users2",1);
        System.out.println(jedis.lrange("users2",0,-1));
    }


    /**
     * 集合
     * 1> 元素没有顺序
     * 2> 元素不可重复
     * 文档：127.0.0.1:6384> help @set
     */
    @Test
    public void testRedisSet() {
        jedis.flushDB();
        // 添加元素，返回添加的元素个数
        long r = jedis.sadd("hobbys","吃","喝","玩","乐","看书","旅游");
        Assert.assertTrue(r == 6);

        // 查询集合元数数量
        r = jedis.scard("hobbys");
        Assert.assertTrue(r == 6);

        // 取多个集合的差集
        r = jedis.sadd("hobbys2","吃","旅游");
        Assert.assertTrue(r == 2);
        Set<String> s = jedis.sdiff("hobbys","hobbys2");
        System.out.println(s);
        Assert.assertTrue(s.size() == 4);

        // 取hoobys和hobbys2的差集，将结果存在hobbys3集合中
        r = jedis.sdiffstore("hobbys3","hobbys","hobbys2");
        Assert.assertTrue(jedis.scard("hobbys3") == r);

        // 取多个集合的交集
        s = jedis.sinter("hobbys","hobbys2");
        System.out.println(s);
        Assert.assertTrue(s.size() == 2);

        // 取多个集合的交集，并将结果存储到一个新的key中
        r = jedis.sinterstore("hobbys4","hobbys","hobbys2");
        Assert.assertTrue(jedis.scard("hobbys4") == r);

        // 判断某个元素是否在集合当中
        boolean b = jedis.sismember("hobbys","吃");
        Assert.assertTrue(b);

        // 获取集合中的的元素
        s = jedis.smembers("hobbys");
        System.out.println(s);
        Assert.assertTrue(s.size() == 6);

        // 从一个集合中移动指定的元素到另外一个集合当中
        r = jedis.smove("hobbys","hobbys2","乐");
        Assert.assertTrue(r == 1);
        Assert.assertTrue(jedis.sismember("hobbys2","乐"));

        // 从集合中随机获取2个元素
        List<String> list = jedis.srandmember("hobbys",2);
        System.out.println("随机获取2个元素：" + list);
        Assert.assertTrue(list.size() == 2);

        // 从集合中随机移除一个或多个元素，返回移除的元素。因为集合中的元素是没有顺序的
        s = jedis.spop("hobbys",2);
        System.out.println(s);
        Assert.assertTrue(s.size() == 2);

        // 从集合中删除一个或多个元素，返回删除的个数
        r = jedis.srem("hobbys","吃","旅游");
        System.out.println(r);
        Assert.assertTrue(!jedis.sismember("hobbys","吃") && !jedis.sismember("hobbys","旅游"));

        // 根据查询关键搜索集合中的元素，0：搜索开始位置，scanParams：搜索规则
        ScanParams scanParams = new ScanParams();
        jedis.sadd("hobbys","java","javascript","php","c++","c","objective-c","node.js","python");
        scanParams.match("java*");
        ScanResult<String> sr = jedis.sscan("hobbys",0,scanParams);
        list = sr.getResult();
        System.out.println(list);
        Assert.assertTrue(list.size() == 2);

        // 取多个集合的并集，如果元素相同，则会覆盖
        Set<String> us = jedis.sunion("hobbys2","hobbys3");
        System.out.println(us);

        // 将多个集合的并集存储到一个新的集合中
        r = jedis.sunionstore("hobbys5","hobbys2","hobbys3");
        Assert.assertTrue(r == jedis.scard("hobbys5"));
        System.out.println(jedis.smembers("hobbys5"));
    }

    /**
     * 每个测试结束之前要关闭连接
     */
    @After
    public void  afterTest(){
        jedis.close();
    }
}
