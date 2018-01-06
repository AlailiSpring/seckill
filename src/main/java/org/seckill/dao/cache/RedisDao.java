package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by Spring on 2017/5/7.
 */
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //类似于数据库连接池的connection pool
    private final JedisPool jedisPool;

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String id, int port) {
        jedisPool = new JedisPool(id, port);
    }

    public Seckill getSeckill(long seckillId) {

        //redis逻辑操作
        try {
            //类似于数据库的connection
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckillId;
                //redis中存储的是二进制方式，所以需要采用protostuff进行自定义序列化
                byte[] bytes = jedis.get(key.getBytes());
                //缓存获取
                if (bytes != null) {
                    //先获取到一个空对象
                    Seckill seckill = schema.newMessage();
                    //调用此工具类完成反序列化，并存储到seckill对象中
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    return seckill;
                }

            }finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            e.printStackTrace();
        }
        return null;
    }

    //redis存储seckill对象，存储的是二进制对象（字节数组）
    public String putSeckill(Seckill seckill) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            try{
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存，1小时
                int timeout = 60 * 60;
                String result=jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            }finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            e.printStackTrace();
        }
        return null;
    }
}
