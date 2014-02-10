package controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import models.ErrorResponseModel;
import models.ResponseModel;
import play.Logger;
import processor.AbstractProcessorFactory;
import processor.Processor;
import processor.Protocol;
import processor.RequestContext;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-26
 * Time: 上午11:08
 * 功能描述:  请求处理
 */
public class ServiceCt extends ReadBodySupportCt {
    static AbstractProcessorFactory processorFactory  = AbstractProcessorFactory.buildDefaultProcessorFacoty();

    public static void onMessageReceive() {
        try {

            //1. 解析协议------->协议层
            JSONObject obj = JSON.parseObject(getBody());

            //存储客户端数据到当前请求上下文
            RequestContext.setData(obj);

            //查找请求命令代号
            String processorName = obj.getString(Protocol.GlobalFieldName.CMD);

            //根据代号寻找相应的业务处理器
            Processor processor = processorFactory.lookup(processorName);

            //2.处理请求 返回响应数据-------------->业务层
            long stime = System.currentTimeMillis();
            ResponseModel response = processor.process();
            Logger.info("搜索用时:%s",(System.currentTimeMillis()-stime));
            //发送处理结果数据
            renderJSON(response.encode());

        } catch (Exception e) {
            Logger.info(e.getMessage(), e);
            renderJSON(new ErrorResponseModel().encode());


        }finally {
            RequestContext.clear();
            Logger.debug("do finally");
        }
    }


}
