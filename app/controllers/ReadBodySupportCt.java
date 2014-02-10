package controllers;

import play.Logger;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import processor.RequestContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-26
 * Time: 下午3:52
 * 功能描述:
 */
public class ReadBodySupportCt extends Controller {
   @Before(priority = 1)
    public static void doBefore(){
        //Logger.debug("检查身份证");
    }

    public static String getBody() {
        InputStream is = request.body;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        StringBuffer sb = new StringBuffer();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            Logger.error("请取数据出错:\n%s" ,  sb.toString());
        }
        Logger.debug("收到请求数据:\n%s" , sb.toString());
        return sb.toString();
    }
    @After(priority = 1)
    public static void doAfter(){
        RequestContext.clear();
        //Logger.debug("在这里处理后事吧:)");
    }
}
