package job;

import com.tom.springutil.StopWatch;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import service.NewsService;
import utils.ElasticsearchHelper;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 上午10:12
 * 功能描述: 创建索引库
 */
@OnApplicationStart(async = true)
public class CreateESMappingJob extends Job {
    @Inject
    static NewsService newsService;

    public void doJob()throws Exception {
        try{
           if(ElasticsearchHelper.isIndexExist(newsService.index_name)){
               Logger.info("索引已存在，不创建，任务退出");
           }else{
            doMappingNewsInfo();
           }
            Logger.info("开启数据库信息变化监控Job");
           JobSwitchConsts.openDataChangeMonitor.set(true);
        }catch (Exception e){
           System.out.println("job=== :" + e.getMessage());
        }
    }

    private void doMappingNewsInfo()throws Exception {
        final StopWatch sw1 = new StopWatch("索引新闻");
        final StopWatch sw2 = new StopWatch("索引公告");
        final StopWatch sw3 = new StopWatch("索引研报");
        StopWatch sw = new StopWatch("索引数据");
        sw.start("建索引库");
        newsService.createIndexLib();
        sw.stop();
        sw.start("建索引表");
        newsService.createNewsInfoMapping();
        sw.stop();
     final CountDownLatch latch = new CountDownLatch(3);
        new Thread( new Runnable(){
             public void run(){
                 sw1.start("索引新闻");
                 newsService.indexNewsInfo();
                 sw1.stop();
                 latch.countDown();
             }
         }).start();
        new Thread( new Runnable(){
            public void run(){
                sw2.start("索引公告");
                newsService.indexAnnInfo();
                sw2.stop();
                latch.countDown();
            }
        }).start();
        new Thread( new Runnable(){
            public void run(){
                sw3.start("索引研报");
                newsService.indexReportInfo();
                sw3.stop();
                latch.countDown();
            }
        }).start();
        latch.await();
        Logger.info(sw1.prettyPrint());
        Logger.info(sw2.prettyPrint());
        Logger.info(sw3.prettyPrint());
    }
}
