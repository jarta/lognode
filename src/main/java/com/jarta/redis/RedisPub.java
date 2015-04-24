package com.jarta.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by wei on 2015/4/20.
 */
public class RedisPub extends JedisPubSub {

    static Logger logger = LoggerFactory.getLogger(RedisPub.class);

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        logger.info("onSubscribe - channel={}, subChannels={}",channel, subscribedChannels);
    }

    @Override
    public void onMessage(String channel, String message) {
       logger.info("receive message from {}, msg = {}", channel, message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        logger.info("receive message from P-> {}, msg = {}", channel, message);
    }

    public static void main(String[] args) {
        JedisPool pool = new JedisPool("localhost");
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set("a","test");
            logger.info("b={},a={}",jedis.get("b"),jedis.get("a"));

            jedis.psubscribe(new RedisPub(), new String[]{"*"});

            logger.info("end..");

        } finally {
            if(jedis!=null) {
                pool.returnResourceObject(jedis);
            }
        }

    }
}
