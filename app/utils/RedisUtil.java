package utils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import play.Play;
import play.modules.redis.Redis;
import redis.clients.util.SafeEncoder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * redis工具类.
 * User: wenzhihong
 * Date: 12-9-11
 * Time: 下午12:49
 */
public abstract class RedisUtil {
    public static void pureSet(String key, String val){
        if(key != null && val != null){
            Redis.set(key, val);
        }
    }

    public static void set(String key, Object obj){
        set(key, obj, null);
    }

    public static void set(String key, Object obj, Gson gson){
        if(key == null || obj == null){
            return ;
        }
        if(gson == null){
            gson = CommonUtils.createGson();
        }
        String val = gson.toJson(obj);
        Redis.set(key, val);
    }

    /**
     * 把gson字符串进行压缩后放入redis
     */
    public static void setGsonWithCompress(String key, Object obj){
        setGsonWithCompress(key, obj, null);
    }

    /**
     * 把gson字符串进行压缩后放入redis
     */
    public static void setGsonWithCompress(String key, Object obj, Gson gson){
        if (key != null && obj != null) {
            if (gson == null) {
                gson = CommonUtils.createGson();
            }

            String json = gson.toJson(obj);
            try {
                Redis.set(SafeEncoder.encode(key), CommonUtils.compress(json.getBytes("utf-8")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("按utf-8编码取字节时失败");
            }
        }
    }

    public static String get(String key){
        return key == null ? null : Redis.get(key);
    }

    /**
     * 从redis上取出, 解压缩并按utf-8编码字符串
     */
    public static String getWithDecompress(String key){
        byte[] bytes = Redis.get(SafeEncoder.encode(key));
        if(bytes != null && bytes.length > 0){
            try {
                return new String(CommonUtils.decompress(bytes), "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("不能转成utf-8编码");
            }
        }

        return null;
    }

    /**
     * 压缩后放入redis
     *
     * @param key
     */
    public static void setWithCompress(String key, String data) {
        if (key != null && data != null) {
            try {
                Redis.set(SafeEncoder.encode(key), CommonUtils.compress(data.getBytes("utf-8")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("按utf-8编码取字节时失败");
            }
        }
    }

    /**
     * 压缩后放入redis
     * @param key
     */
    public static void setWithCompress(String key, byte[] data){
        if(key != null && data != null){
            Redis.set(SafeEncoder.encode(key), CommonUtils.compress(data));
        }
    }

    /**
     * 获取key的value, 把值(json对象)转成map
     * @param key
     * @return
     */
    public static Map<String, Object> fetchJsonMap(String key) {
        String val = Redis.get(key);
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = CommonUtils.createGson().fromJson(val, type);
        return map;
    }

    /**
     * 从redis上取值, 并转成相应的类型, 如果在redis上不存在这个值, 则返回null. 转换错误也返回null
     *
     * @param key    redis key值
     * @param tclass 类型
     * @return
     */
    public static <T> T fetchFromRedis(String key, Class<T> tclass) {
        String val = Redis.get(key);
        return jsonStr2Object(tclass, val, null);
    }


    public static <T> T fetchFromRedisWithDecompress(String key, Class<T> tclass) {
        String val = decompress2Str(key);
        return jsonStr2Object(tclass, val, null);
    }

    /**
     * 从redis上取值, 并转成相应的类型, 如果在redis上不存在这个值, 则返回null. 转换错误也返回null
     * 使用指定的redis实例
     *
     * @param key    redis key值
     * @param tclass 类型
     * @param gson   gson实例
     * @return
     */
    public static <T> T fetchFromRedis(String key, Class<T> tclass, Gson gson) {
        String val = Redis.get(key);
        return jsonStr2Object(tclass, val, gson);
    }

    public static <T> T fetchFromRedisWithDecompress(String key, Class<T> tclass, Gson gson) {
        String val = decompress2Str(key);
        return jsonStr2Object(tclass, val, gson);
    }

    /**
     * 从redis上取值, 并转成相应的类型, 如果在redis上不存在这个值, 则返回null. 转换错误也返回null
     *
     * @param key  redis key值
     * @param type 类型
     * @return
     */
    public static <T> T fetchFromRedis(String key, Type type) {
        String val = Redis.get(key);
        return jsonStr2Object(type, val, null);
    }

    public static <T> T fetchFromRedisWithDecompress(String key, Type type) {
        String val = decompress2Str(key);
        return jsonStr2Object(type, val, null);
    }

    /**
     * 从redis上取值, 并转成相应的类型, 如果在redis上不存在这个值, 则返回null. 转换错误也返回null
     * 使用指定的redis实例
     *
     * @param key  redis key值
     * @param type 类型
     * @param gson gson实例
     * @return
     */
    public static <T> T fetchFromRedis(String key, Type type, Gson gson) {
        String val = Redis.get(key);
        return jsonStr2Object(type, val, gson);
    }

    public static <T> T fetchFromRedisWithDecompress(String key, Type type, Gson gson) {
        String val = decompress2Str(key);
        return jsonStr2Object(type, val, gson);
    }

    /**
     * 返回redis的list数据结构的key值的从start到end的值
     * @param key
     * @param tclass
     * @param gson
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    public static <T> List<T> lrange(String key, Class<T> tclass, Gson gson, int start, int end){
        List<String> strList = Redis.lrange(key, start, end);
        if(strList != null && strList.size() > 0){
            List<T> list = Lists.newLinkedList();
            for (String s : strList) {
                list.add(gson.fromJson(s, tclass));
            }
            return list;
        }else{
            return Lists.newLinkedList();
        }
    }

    /**
     * 把list的值放入到redis的List里.放之前先清除掉之前的值
     * @param key
     * @param list
     * @param gson
     * @param <T>
     */
    public static <T> void rpushWithDel(String key, List<T> list, Gson gson){
        if(list != null && list.size() > 0){
            Redis.del(new String[]{key});
            for (T t : list) {
                if(t != null){
                    Redis.rpush(key, gson.toJson(t));
                }
            }
        }
    }

    /**
     * 删除key值
     * @param keys
     */
    public static void del(String... keys) {
        if (keys.length > 0) {
            Redis.del(keys);
        }
    }

    /**
     * 删除key
     * @param keys  key的集合
     * @param preKey key的前缀
     */
    public  static void del(Collection<?> keys, String preKey){
        if(keys == null || keys.size() == 0){
            return ;
        }

        List<String> clearIdList = Lists.newArrayListWithCapacity(50);
        for (Object id : keys) {
            if (id != null) {
                clearIdList.add(preKey + id.toString());
                if(clearIdList.size() >= 50){
                    del(clearIdList.toArray(new String[clearIdList.size()]));
                    clearIdList.clear();
                }
            }
        }
        if(clearIdList.size() > 0){
            del(clearIdList.toArray(new String[clearIdList.size()]));
        }
    }

    /**
     * 把json字符串转成Object
     */
    private static <T> T jsonStr2Object(Class<T> tclass, String val, Gson gson) {
        if (val == null || "".equals(val)) {
            return null;
        } else {
            if (gson == null) {
                gson = CommonUtils.createGson();
            }
            try {
                return gson.fromJson(val, tclass);
            } catch (JsonSyntaxException e) {
                if (Play.mode.isDev()) {
                    throw e;
                }
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 把json字符串转成Object
     */
    private static <T> T jsonStr2Object(Type type, String val, Gson gson) {
        if (val == null || "".equals(val)) {
            return null;
        } else {
            if (gson == null) {
                gson = CommonUtils.createGson();
            }
            try {
                return gson.fromJson(val, type);
            } catch (JsonSyntaxException e) {
                if (Play.mode.isDev()) {
                    throw e;
                }
                e.printStackTrace();
            }
        }
        return null;
    }

    //解码且转为utf-8编码的信息
    private static String decompress2Str(String key) {
        byte[] bytes = Redis.get(SafeEncoder.encode(key));
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String val = null;
        try {
            val = new String(CommonUtils.decompress(bytes), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("不能转成utf-8编码");
        }
        return val;
    }
}
