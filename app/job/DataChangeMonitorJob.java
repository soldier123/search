package job;

import com.google.gson.Gson;
import play.Logger;
import play.Play;
import play.db.DB;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.redis.Redis;
import play.modules.redis.RedisConnectionManager;
import service.NewsService;
import utils.CommonUtils;
import utils.RedisKey;

import javax.inject.Inject;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-29
 * Time: 上午11:26
 * 功能描述:
 * <p/>
 * 从redis队列出有变化的表和数据所对应的utsid
 */
@OnApplicationStart(async = true)
public class DataChangeMonitorJob extends Job {
    @Inject
    static NewsService newsService;

    public void doJob() throws Exception {
        //doHack();
        if ("false".equals(Play.configuration.getProperty("redis.msg.monitor", "false"))) {
            return;
        }

        Gson gson = CommonUtils.createGson();
        String message = null;
       while(true){
           try {
                if (JobSwitchConsts.openDataChangeMonitor.get() && Redis.llen(RedisKey.Global.backup_record_change_list) > 0) {

                        message = Redis.lpop(RedisKey.Global.backup_record_change_list);
                        if (message != null) {
                            RecordChangeMsg recordChangeInfo = gson.fromJson(message, RecordChangeMsg.class);
                            //更新索引
                            newsService.updateIndex(recordChangeInfo.tid, recordChangeInfo.table.toLowerCase(), recordChangeInfo.action.toLowerCase());
                            Logger.info(message + ":DataChangeMonitorJob done\r\n");
                        }

                } else {
                   // Logger.info("threre is no data  need deal with,have a rest:-)");
                    Thread.sleep(3 * 1000);
                }
           } catch (Exception e) {
                e.printStackTrace();
                Logger.error(message + "\r\n" + e.getMessage(), e);
                Thread.sleep(1000);//出异常了  休息几秒再试
               try{
                   if(message !=null){
                   Redis.rpush(RedisKey.Global.backup_record_change_list,message);
                   }
               }catch (Exception ex){
                   Logger.error("将信息放回队列失败:%s",message);
               }
           }finally {
               RedisConnectionManager.closeConnection();
               //因为ThreadLocal这里要进行关闭，将连接还回到连接池 否则长时间不操作后再进行操作会报:"operation not allowed after connection close"
               DB.close();
           }
       }


    }

    public void doHack() throws Exception {
        Logger.info("start hack");
        newsService.updateIndex(8453047, "news_newsinfo", "insert");
        newsService.updateIndex(8454206, "news_newsinfo", "insert");
        newsService.updateIndex(8456686, "news_newsinfo", "insert");
        Logger.info("finish hack");
    }

    public class RecordChangeMsg {
        //表更新的记录id utsid
        public long tid;   //对应到每张数据表的 utsid 字段.

        public String schema; //数据库名

        public String table;

        public String action;  //insert 插入, update 修改, delete 删除.

        //消息生成的时间, 这里用long 型表示. 要转成java的Date类型. 直接用 new Date(long d) 从1970.1.1 00:00:00 GMT
        public long time;
    }


}
