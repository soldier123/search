package job;

import play.jobs.Job;
import play.jobs.OnApplicationStop;
import utils.ElasticsearchHelper;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-25
 * Time: 上午10:41
 * 功能描述:
 */
@OnApplicationStop
public class ApplicationCloseJob extends Job {

    public void doJob()throws Exception {
        ElasticsearchHelper.close();
    }
}
