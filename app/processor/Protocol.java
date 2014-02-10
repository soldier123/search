package processor;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-27
 * Time: 上午10:35
 * 功能描述:
 */
public final class Protocol {

    public  static final String PROTCOL_VERSION = "1.0";
    public static final int STATUS_FAILRE = -1;
    public static final int STATU_SSUCCESS = 0;
    public static final String DEFAULT_ERROR_MESSAGE = "ERR_CODE-1000001";
    /**
     * 命令名称约定
     */
    public static class CmdName{

        public static  final String  COMMON_INFO_SEARCH = "commonInfoSearch";
        public static  final String  ADVANCE_INFO_SEARCH = "advanceInfoSearch";
        public static  final String  SYMBOL_INFO_SEARCH = "symbolInfoSearch";

    }

    /**
     * 全局共用字段名称约定
     */
    public static class GlobalFieldName{

        public static  final String  CMD = "cmd";
        public static  final String  DATA = "data";
        public static  final String  PAGESIZE = "pageSize";
        public static  final String  PAGENO= "pageNo";
        public static  final String  STATUS = "status";
        public static  final String  TOTAL= "total";
        public static  final String  MESSAGE= "message";

    }
    //资讯检索相关协议字段约束
    public static class InfomationSearchFieldName{

        public static  final String  TYPE = "itype";
        public static  final String  STARTTIME= "startTime";
        public static  final String  ENDTTIME= "endTime";
        public static  final String  KEYWORD = "keyword";
        public static  final String  NSOURCE = "nsource";
        public static  final String  NCIDS= "ncids";
        public static  final String  ACIDS = "acids";
        public static  final String  AIIDS= "aiids";
        public static  final String  RSOURCE= "rsource";
        public static  final String  RCIDS= "rcids";
        public static  final String  RIIDS= "riids";

        public static  final String  NEWSID= "newsid";
        public static  final String  ANNID = "annid";
        public static  final String  REPORTID= "repid";
        public static  final String  SYMBOL= "symbols";
        public static  final String  ATTACH= "attach";
        public static  final String  UPDATEDATE= "updatedate";
        public static  final String  DECLAREDATE= "declaredate";
        public static  final String  TITLE= "title";



    }


}
