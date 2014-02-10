package dao;

import models.InfomationModel;
import service.DefaultNewsServiceImpl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 下午3:33
 * 功能描述:
 */
public interface NewsDao {
    public final static  List<String> newsRelatedTable = Arrays.asList("news_newsinfo", "news_classify", "news_industry", "news_security");
    public final static List<String> annRelatedTable = Arrays.asList("ann_announcementinfo","ann_classify","ann_security");
    public final static List<String> reportRelatedTable = Arrays.asList("rep_reportinfo","rep_category","rep_industry","rep_institution","rep_security");
    public final static List<String> mainRelatedTable = Arrays.asList("ann_announcementinfo","news_newsinfo","rep_reportinfo");

    /**
     * 根据板块树节点id查询相应的成份股
     * @param typeIdArr
     * @return
     */
    List<String> findSecurityByPlateId(String[] typeIdArr);

    /**
     * 根据ID查找新闻
     * @param id
     * @return
     */
    public InfomationModel findNewsById(long id);
    /**
     * 根据ID查找公告
     * @param id
     * @return
     */
    public InfomationModel findAnnById(long id);
    /**
     * 根据ID查找研报
     * @param id
     * @return
     */
    public InfomationModel findReportById(long id);

    /**
     * 分页查找新闻
     * @param begainIndex
     * @param size
     * @return
     */
    public List<InfomationModel>  findNews(int begainIndex, int size);

    /**
     * 查找2000年之前的新闻
     * @return
     */
    public List<InfomationModel>  findNewsBefore2000Year();

    /**
     * 查找指定日期的新闻
     * @param date 指定日期
     * @return
     */
    public List<InfomationModel>  findNewsInDay(Date date);

    /**
     * 查找新闻表的最大日期
     * 注意:返回的最大日期只能说明这个日期大于新闻表的最大日期, 不是这个表真正的最大日期
     * @return
     */
    public Date findMaxNewsDate();

    /**
     * 查找新闻类型
     * @param newsId
     * @return
     */
    public List<String> findNewsClassifyIds(long newsId);

    /**
     * 查找新闻所属证券ID
     * @param newsId
     * @return
     */
    public List<String> findNewsSecurityIds(long newsId);

    /**
     * 所属行业ID
     * @param newsId
     * @return
     */
    public List<String> findNewsIndustryIds(long newsId);
    public  List<InfomationModel> findAnnInfos(int begainIndex, int size);
    public  List<InfomationModel> findReports(int begainIndex, int size);
    public List<DefaultNewsServiceImpl.SymbolMapIndustry>  findSymbolIndustry();




    public List<String> findAnnSecurityIds(long annId);
    public List<String> findAnnClassifyIds(long annId);

    public List<String> findReportSecurityIds(long repId);
    public List<String> findReportClassifyIds(long repId);
    public List<String> findReportSourceIds(long repId);
    public List<String> findRepIndustryIds(long repId);

    public InfomationModel findNewsByClassifyUTSId(long utsId);
    public InfomationModel findNewsByIndustryUTSId(long utsId);
    public InfomationModel findNewsBySecurityUTSId(long utsId);

    public InfomationModel findNewsByUTSId(long utsId, String tableName);

    public InfomationModel findAnnByUTSId(long utsId, String tableName);

    public InfomationModel findReportByUTSId(long utsId, String tableName);

}
