package job;

import dao.SqlLoader;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 上午9:36
 * 功能描述:
 */
@OnApplicationStart
public class InitJob extends Job {


    public void doJob()throws Exception{
        SqlLoader.init();
    }
}
