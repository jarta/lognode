package com.jarta.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by wei on 2015/4/20.
 */
public class RedisSub  {

    static Logger logger = LoggerFactory.getLogger(RedisSub.class);

    public static void main(String[] args) {
        JedisPool pool = new JedisPool("localhost");
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            for (int i = 0; i < 10; i++) {
                long n = jedis.publish("news.2", "test 2");
                logger.debug("publish {}", n);
                Thread.sleep(20);
            }

        } catch (InterruptedException e) {
           logger.warn("thread interrupted", e);
        } finally {
            if(jedis != null) {
                pool.returnResourceObject(jedis);
            }
        }
    }
}
