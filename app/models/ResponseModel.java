package models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import processor.Protocol;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-28
 * Time: 下午2:08
 * 功能描述:
 */
public class ResponseModel {
    @SerializedName(Protocol.GlobalFieldName.CMD)
    public String cmd;
    @SerializedName( Protocol.GlobalFieldName.STATUS)
    @Expose
    public int status;

    public String encode(){
        return null;
    }
    public String decode(){
        return null;
    }


}
