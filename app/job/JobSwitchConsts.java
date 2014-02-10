package job;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-6-5
 * Time: 下午2:48
 * 功能描述:
 */
public class JobSwitchConsts {

    //DataChangeMonitorJob开关
    public static AtomicBoolean openDataChangeMonitor = new AtomicBoolean(false);
}
