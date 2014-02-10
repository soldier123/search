package models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import processor.Protocol;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-28
 * Time: 下午2:11
 * 功能描述:
 */
public class PaginationSupportResponseModel extends ResponseModel {

    @SerializedName(Protocol.GlobalFieldName.TOTAL)
    @Expose
    public long total;
    @SerializedName(Protocol.GlobalFieldName.PAGESIZE)
    @Expose
    public int size;
    @SerializedName(Protocol.GlobalFieldName.PAGENO)
    @Expose
    public int pageNo;

}
