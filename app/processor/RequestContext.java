package processor;

import com.alibaba.fastjson.JSONObject;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-28
 * Time: 下午4:11
 * 功能描述:
 */
public class RequestContext {

    public static ThreadLocal<RequestContext> current = new ThreadLocal<RequestContext>(){
        protected RequestContext initialValue(){
            return new RequestContext();
        }
    };

    private JSONObject data;
    public static  void setData(JSONObject data){
        current.get().data = data;
    }
    public  RequestContext(){
    }
    public  RequestContext(JSONObject data){
        setData(data);
    }
    public static  String getString(String key){
        return current.get().data.getString(key);
    }
    public static  int getInt(String key){
        return current.get().data.getIntValue(key);
    }
    public static  double getDouble(String key){
        return current.get().data.getDoubleValue(key);
    }
    public static  long getLong(String key){
        return current.get().data.getLongValue(key);
    }
    public static  Object[] getArray(String key){
        return current.get().data.getJSONArray(key).toArray();
    }
    private static  void removeAll(){
        if(current.get().data != null){
            current.get().data.clear();
        }

    }
    public static   void clear(){
        current.get().removeAll();
    }
}
