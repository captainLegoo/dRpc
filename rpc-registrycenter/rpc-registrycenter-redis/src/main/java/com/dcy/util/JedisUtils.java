package com.dcy.util;

import com.dcy.factory.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kyle
 * @date 2024/05/24
 * @description Jedis Utils
 */
public class JedisUtils {
    private final Jedis jedis;

    public JedisUtils() {
        this.jedis = JedisConnectionFactory.getJedis();
    }

    // =============================common============================

    /**
     * Specify cache expiration time
     * @param key  key
     * @param time time(second)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                jedis.expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get expiration time based on key
     * @param key key cannot be null
     * @return Time (seconds) Returning 0 means it is permanently valid
     */
    public long getExpire(String key) {
        long expirationTime = jedis.ttl(key);
        return (expirationTime == -1 || expirationTime == -2) ? 0 : expirationTime;
    }

    /**
     * Determine whether the key exists
     *
     * @param key key name
     * @return true:exist false:does not exist
     */
    public boolean hasKey(String key) {
        try {
            return jedis.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete cache
     * @param key can pass one value or multiple
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            try {
                if (key.length == 1) {
                    jedis.del(key[0]);
                } else {
                    jedis.del(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ============================String=============================

    /**
     * Get the value of the key
     * @param key key
     * @return value
     */
    public Object get(String key) {
        return key == null ? null : jedis.get(key);
    }

    /**
     * Cache into
     * @param key   key
     * @param value value
     * @return true false
     */
    public boolean set(String key, Object value) {
        try {
            jedis.set(key, String.valueOf(value));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * cache put in and set time
     * @param key   key
     * @param value value
     * @param time  Time (seconds) time must be greater than 0. If time is less than or equal to 0, it will be set indefinitely.
     * @return true false
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                jedis.set(key, String.valueOf(value), SetParams.setParams().ex(time));
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increment
     * @param key   key
     * @param delta How much to increase (greater than 0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("The increment factor must be greater than 0");
        }
        return jedis.incrBy(key, delta);
    }

    /**
     * Decrease
     * @param key   key
     * @param delta How much to reduce (less than 0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("The decrement factor must be greater than 0");
        }
        return jedis.decrBy(key, delta);
    }

    // ================================Hash Map=================================

    /**
     * HashGet
     * @param key  key can not be null
     * @param item item cam not be null
     * @return value
     */
    public Object hget(String key, String item) {
        return jedis.hget(key, item);
    }

    /**
     * Get all key values corresponding to hashKey
     * @param key key
     * @return Corresponding multiple key values
     */
    public Map<String, String> hmget(String key) {
        try {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * HashSet
     * @param key key
     * @param map Corresponding multiple key values
     * @return true false
     */
    public boolean hmset(String key, Map<String, String> map) {
        try {
            jedis.hmset(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet and set the time
     * @param key  key
     * @param map  Corresponding multiple key values
     * @param time time (seconds)
     * @return true false
     */
    public boolean hmset(String key, Map<String, String> map, long time) {
        try {
            String result = jedis.hmset(key, map);
            if ("OK".equals(result)) {
                jedis.expire(key, time);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Put data into a hash table. If it does not exist, it will be created.
     * @param key   jey
     * @param item  item
     * @param value value
     * @return true false
     */
    public boolean hset(String key, String item, Object value) {
        try {
            jedis.hset(key, item, String.valueOf(value));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Put data into a hash table. If it does not exist, it will be created.
     * @param key   key
     * @param item  item
     * @param value value
     * @param time  Time (seconds) Note: If the existing hash table has time, the original time will be replaced here.
     * @return true false
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            jedis.hset(key, item, String.valueOf(value));
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete values in hash table
     * @param key  key cannot be null
     * @param item Item can be multiple and cannot be null
     */
    public void hdel(String key, Object... item) {
        jedis.hdel(key, Arrays.toString(item));
    }

    /**
     * Determine whether there is a value for the item in the hash table
     *
     * @param key  key cannot be null
     * @param item item cannot be null
     * @return true false
     */
    public boolean hHasKey(String key, String item) {
        return jedis.hexists(key, item);
    }

    /**
     * hash increment If it does not exist, it will create one and return the new value.
     * @param key  key
     * @param item item
     * @param by   How much to increase (greater than 0)
     * @return
     */
    public double hincr(String key, String item, double by) {
        if (by <= 0) {
            throw new RuntimeException("The increment factor must be greater than 0");
        }
        try {
            return jedis.hincrByFloat(key, item, by);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * hash decrementing
     * @param key  key
     * @param item item
     * @param by   To reduce the number (less than 0)
     * @return
     */
    public double hdecr(String key, String item, double by) {
        if (by >= 0) {
            throw new RuntimeException("The decrement factor must be greater than 0");
        }
        try {
            return jedis.hincrByFloat(key, item, by);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ============================set=============================

    /**
     * Get all values in Set based on key
     * @param key key
     * @return
     */
    public Set<String> sGet(String key) {
        try {
            return jedis.smembers(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Query from a set based on value to see if it exists
     * @param key   key
     * @param value item
     * @return true false
     */
    public boolean sHasKey(String key, String value) {
        try {
            return jedis.sismember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Put data into set cache
     * @param key    key
     * @param values value (can be multiple)
     * @return 成功个数
     */
    public long sSet(String key, String... values) {
        try {
            return jedis.sadd(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Put set data into cache
     * @param key    key
     * @param time   time (seconds)
     * @param values value (can be multiple)
     * @return Number of successes
     */
    public long sSetAndTime(String key, long time, String... values) {
        try {
            long count = jedis.sadd(key, values);
            if (time > 0) {
                jedis.expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get the length of the set cache
     * @param key key
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return jedis.scard(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Remove the value of value
     * @param key    key
     * @param values value (can be multiple)
     * @return Number of items removed
     */
    public long setRemove(String key, String... values) {
        try {
            return jedis.srem(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * Get the first value of the list
     * @param key key
     * @return value
     */
    public Object leftPop(String key){
        try {
            return jedis.lpop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the last value of the list
     * @param key key
     * @return value
     */
    public Object rightPop(String key){
        try {
            return jedis.rpop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Insert single data from the left side of the queue
     * @param key
     * @param values
     * @return
     */
    public Object leftPush(String key, String values){
        try {
            return jedis.lpush(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Insert single data from the right side of the queue
     * @param key
     * @param values
     * @return
     */
    public Object rightPush(String key, String values){
        try {
            return jedis.rpush(key,values);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Insert multiple data from the left side of the queue
     * @param key
     * @param values
     * @return
     */
    public Object leftPushAll(String key, String... values){
        try {
            return jedis.lpush(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Insert multiple data from the right side of the queue
     * @param key
     * @param values
     * @return
     */
    public Object rightPushAll(String key, String... values){
        try {
            return jedis.rpush(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the contents of the list cache
     * @param key   key
     * @param start start
     * @param end   Ending 0 to -1 represents all values
     * @return
     */
    public List<String> lrange(String key, long start, long end) {
        try {
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Get the length of the list cache
     * @param key key
     * @return
     */
    public long lsize(String key) {
        try {
            return jedis.llen(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get the value in the list by index
     * @param key   key
     * @param index When index index>=0, 0 is the head of the table,
     *             is the second element, and so on; when index<0,
     *              -1 is the tail of the table,
     *              -2 is the penultimate element, and so on.
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return jedis.lindex(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * put list into left side of cache
     * @param key   key
     * @param value value
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            jedis.rpush(key, value.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * put list into left side of cache with expire time
     * @param key   key
     * @param value value
     * @param time  time(second)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            jedis.rpush(key, value.toString());
            if (time > 0)
                jedis.expire(key, (int) time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * put list into left side of cache
     * @param key   key
     * @param value value
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            for (Object val : value) {
                jedis.rpush(key, val.toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * put list into cache
     * @param key   key
     * @param value value
     * @param time  time(second)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            for (Object val : value) {
                jedis.rpush(key, val.toString());
            }
            if (time > 0)
                jedis.expire(key, (int) time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modify a piece of data in the list based on the index
     * @param key   key
     * @param index index
     * @param value value
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            jedis.lset(key, index, value.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove N values as value
     * @param key   key
     * @param count How many to remove
     * @param value value
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return jedis.lrem(key, count, value.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
