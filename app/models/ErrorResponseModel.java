package models;

import com.google.gson.annotations.Expose;
import processor.Protocol;
import utils.CommonUtils;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-28
 * Time: 下午3:05
 * 功能描述:
 */
public class ErrorResponseModel  extends ResponseModel {


      public ErrorResponseModel(){
          this.status = Protocol.STATUS_FAILRE;
      }
     @Expose
     public String message = Protocol.DEFAULT_ERROR_MESSAGE;

    public String encode(){
       return  CommonUtils.createIncludeNulls().toJson(this);
    }


}
