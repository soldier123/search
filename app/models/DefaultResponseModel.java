package models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import processor.Protocol;
import utils.CommonUtils;

import java.util.List;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-26
 * Time: 下午2:15
 * 功能描述: 响应模型
 */
public class DefaultResponseModel<T extends List<? extends DataModel>> extends PaginationSupportResponseModel {
    public DefaultResponseModel() {

    }

    public DefaultResponseModel(T data) {
        this.data = data;
    }
    @SerializedName( Protocol.GlobalFieldName.DATA)
    @Expose
    public T data;

    public String encode(){
      return   CommonUtils.createIncludeNulls().toJson(this);
    }
}
