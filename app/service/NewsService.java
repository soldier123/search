package service;

import models.InfomationModel;
import play.libs.F;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 上午9:46
 * 功能描述:
 */
public interface NewsService {
    public final static  List<String> actions = Arrays.asList("delete", "update", "insert");
    public static final String index_name = "gta_data_test";
    public static final String index_type_news_info = "news_info_test";

    /**
     * 普通搜索
     * @param map
     * @return
     */
    public F.T3<List<InfomationModel> ,Long,Integer> searchNews(Map<String,Object> map);

    /**
     * 高级搜索
     * @param map{
     *           key说明
     * }
     * @return
     */
    public F.T3<List<InfomationModel> ,Long,Integer> advanceSearchNews(Map<String,Object> map);
    public F.T3<List<InfomationModel> ,Long,Integer> advanceSearchNewsFilterWithType(Map<String,Object> map);

    public void createNewsInfoMapping() throws Exception;

    /**
     * 创建索引库,相当于创建数据库
     */
    public void createIndexLib();
    /**
     * 重建所有新闻索引
     */
    public void indexNewsInfo();
    /**
     * 重建所有公告索引
     */
    public  void indexAnnInfo();
    /**
     * 重建所有研报索引
     */
    public  void indexReportInfo();
    /**
     * 重建新闻索引
     */
    public void indexSingleNewsInfo(long newsId);
    /**
     * 重建公告索引
     */
    public  void indexSingleAnnInfo(long annId);
    /**
     * 重建研报索引
     */
    public  void indexSingleReportInfo(long reportId);
    /**
     * 批量重建新闻索引
     */
    public void indexNewsInfoBatch(List<Long> newsIds);
    /**
     * 批量重建公告索引
     */
    public  void indexAnnInfoBatch(List<Long> annIds);
    /**
     * 批量重建研报索引
     */
    public  void indexReportInfoBatch(List<Long> reportIds);

    /**
     *表数据变化通知接口，更新索引
     * 更新策略  目前是删除之前的索引  再新增索引
     * @param utsId
     * @param tableName
     */
    public  void updateIndex(long utsId,String tableName,String action) throws Exception;

    /**
     * 按股票代号搜索资讯
     * @param map{
     *    pageSize:要查询的条数
     *    pageNo :查询第几页
     *    type[] :类型 当数组第一个为0时代表全部类型
     *    symbol[] :股票代码
     * }
     * @return
     */
    public F.T3<List<InfomationModel> ,Long,Integer> searchNewsBySymbol(Map<String,Object> map);
    /**
     * 索引字段名描述
     */
    public static class InfomationFieldMapping {
        /**
         * 附件
         */
        public static final String ATTACH = "attach";
        /**
         * 新闻id
         */
        public static final String NEWS_ID = "newsid";
        /**
         * 公告id
         */
        public static final String ANN_ID = "annid";
        /**
         * 研报id
         */
        public static final String REPORT_ID = "repid";
        /**
         * 标题
         */
        public static final String TITLE = "title";
        /**
         * 股票代码
         */
        public static final String SYMBOL = "symbols";
        /**
         * 新闻来源
         */
        public static final String NEWS_SOURCE = "nsource";
        /**
         * 发布日期
         */
        public static final String DECLAREDATE = "declaredate";
        /**
         * 更新时间
         */
        public static final String UPDATEDATE = "updatedate";
        /**
         * 新闻分类id
         */
        public static final String NEWS_CLASSIFY_ID = "ncids";
        /**
         * 公告分类id
         */
        public static final String ANN_CLASSIFY_ID = "acids";
        /**
         * 公告行业id
         */
        public static final String ANN_INDUSTRY_ID = "aiids";
        /**
         * 研报来源id
         */
        public static final String REPORT_SOURCE_ID= "rsids";
        /**
         * 研报行业id
         */
        public static final String REPORT_INDUSTRY_ID = "riids";
        /**
         * 研报分类id
         */
        public static final String REPORT_CLASSIFY_ID = "rcids";
        /**
         * 文档类型
         */
        public static final String DOCUMENT_TYPE = "itype";
        /**
         * utsid
         */
        public static final String UTSID = "utsid";
        /**
         * 新闻行业id
         */
        public static final String NEWS_INDUSTRY_ID = "niids";
    }
}
