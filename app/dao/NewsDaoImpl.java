package dao;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import models.InfomationModel;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.collect.Maps;
import play.Logger;
import play.Play;
import service.DefaultNewsServiceImpl;
import utils.CommonUtils;

import java.io.IOException;
import java.util.*;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 下午3:35
 * 功能描述:
 */
public class NewsDaoImpl implements NewsDao {

    /**
    * 新旧行业编码对应表. 在处理行业研报时要用到. 把之前的旧编码转成新编码
    * 用 sql语句提取, 因为比较固定, 为了方便, 先把它的结果导成csv文件, 在处理
    * SELECT newCode AS 新编码, oldCode AS 旧编码
    FROM

        (
        SELECT a.INDUSTRYCODE AS newCode,  a.INDUSTRYID
        FROM gta_data.pub_indclassifysets a
        WHERE a.INDCLASSIFYSYSTEMCODE IN ('P0207') AND a.INDUSTRYID IS NOT NULL
        ) AS newt
    INNER JOIN
        (
            SELECT GROUP_CONCAT(a.INDUSTRYCODE) AS oldCode,  a.INDUSTRYID
            FROM gta_data.pub_indclassifysets a
            WHERE a.INDCLASSIFYSYSTEMCODE IN ('P0201') AND a.INDUSTRYID IS NOT NULL
            GROUP BY a.INDUSTRYID
        ) AS oldt

    ON newt.INDUSTRYID = oldt.INDUSTRYID
    ORDER BY oldCode
    */
    static Map<String, String> oldNewInduCodeMap = Maps.newHashMap();

    static {
        //初始新旧行业对应表.
        try {
            CharSource charSource = Files.asCharSource(Play.getFile("conf/newOldInduCodeTable.csv"), Charsets.UTF_8);
            ImmutableList<String> lines = charSource.readLines();
            CharMatcher quotationMatcher = CharMatcher.is('"');
            Splitter commaSplitter = Splitter.on(',');
            for (String line : lines) {
                line = quotationMatcher.removeFrom(line);
                Iterable<String> iterable = commaSplitter.split(line);
                boolean isFirst = true;
                String value = "";
                for (String item : iterable) {
                    if (isFirst) {
                        value = item;
                        isFirst = false;
                    } else {
                        oldNewInduCodeMap.put(item, value);
                    }
                }
            }
        } catch (IOException e) {
            Logger.error(e, "新旧行业对应表配制文件conf/newOldInduCodeTable.csv出错");
        }
    }


    public InfomationModel findNewsById(long id) {
        String sql = SqlLoader.getSqlById("findNewsById");
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, id);
    }
    public InfomationModel findAnnById(long id) {
        String sql = SqlLoader.getSqlById("findAnnById");
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, id);
    }
    public InfomationModel findReportById(long id) {
        String sql = SqlLoader.getSqlById("findReportById");
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, id);
    }

    public List<InfomationModel> findNews(int begain, int size) {
        String sql = SqlLoader.getSqlById("findNews");
        return DbUtil.queryExtractDBBeanList(sql, InfomationModel.class, begain, size);
    }

    @Override
    public List<InfomationModel> findNewsBefore2000Year() {
        String sql = SqlLoader.getSqlById("findNewsBefore2000Year");
        return DbUtil.queryExtractDBBeanList(sql, InfomationModel.class);
    }

    @Override
    public List<InfomationModel> findNewsInDay(Date date) {
        if (date == null) {
            return Collections.EMPTY_LIST;
        }
        String dateStr = CommonUtils.getFormatDate("yyyy-MM-dd", date);
        String sql = SqlLoader.getSqlById("findNewsInDay");
        sql = sql.replace("#today#", dateStr);
        return DbUtil.queryExtractDBBeanList(sql, InfomationModel.class);
    }

    @Override
    public Date findMaxNewsDate() {
        String sql = SqlLoader.getSqlById("findMaxNewsDate");
        Date today = new Date();
        Date maxDate = DbUtil.queryExtractDbWithHandler(sql, new ScalarHandler<Date>(), today);
        if (maxDate == null) {
            maxDate = today;
        }
        return maxDate;
    }


    @Override
    public List<String> findNewsClassifyIds(long newsId) {
        String sql = SqlLoader.getSqlById("findNewsClassifyIds");
        return DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>(), newsId);
    }

    @Override
    public List<String> findNewsSecurityIds(long newsId) {
        String sql = SqlLoader.getSqlById("findNewsSecurityIds");
        return DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>(), newsId);
    }

    @Override
    public List<String> findNewsIndustryIds(long newsId) {
        String sql = SqlLoader.getSqlById("findNewsIndustryIds");
        List<Map<String,Object>> mapList = DbUtil.queryMapList(sql, newsId);
        Set<String> induCodeSet = Sets.newHashSet();
        for (Map<String, Object> map : mapList) {
            String code = map.get("code") == null ? "" : (String)map.get("code");
            String type = map.get("type") == null ? "" : (String)map.get("type");
            if ("P0201".equalsIgnoreCase(type)) { //老的分类
                String newCode = oldNewInduCodeMap.get(code);
                if (StringUtils.isNotBlank(newCode)) {
                    code = newCode;
                }else{
                    Logger.warn("newsId[%d]的旧行业分类[%s]没有找到对应的新行业分类", newsId, code);
                }
            }

            code = code + "    "; //先加长在说
            //制造业二级分类
/*            if (code.startsWith("C")) { //制造业, 取前面三位
                code = code.substring(0, 3);
            }else{ //其它行业,则取1位
                code = code.substring(0, 1);
            }*/
            //现在只取一级分类
            code = code.substring(0, 1);

            induCodeSet.add(code.trim());
        }

        return Lists.newArrayList(induCodeSet);
    }

    @Override
    public List<InfomationModel> findAnnInfos(int begainIndex, int size) {
        String sql = SqlLoader.getSqlById("findAnnInfos");
        return DbUtil.queryExtractDBBeanList(sql, InfomationModel.class, begainIndex, size);
    }

    @Override
    public List<InfomationModel> findReports(int begainIndex, int size) {
        String sql = SqlLoader.getSqlById("findReports");
        return DbUtil.queryExtractDBBeanList(sql, InfomationModel.class, begainIndex, size);
    }
    public List<DefaultNewsServiceImpl.SymbolMapIndustry>  findSymbolIndustry(){
        String sql = SqlLoader.getSqlById("findSymbolIndustry");
        return DbUtil.queryExtractDBBeanList(sql, DefaultNewsServiceImpl.SymbolMapIndustry.class);
    }

    @Override
    public List<String> findAnnSecurityIds(long annId) {
        String sql = SqlLoader.getSqlById("findAnnSecurityIds");
        return DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>(), annId);
    }

    @Override
    public List<String> findAnnClassifyIds(long annId) {
        String sql = SqlLoader.getSqlById("findAnnClassifyIds");
        return DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>(), annId);
    }

    @Override
    public List<String> findReportSecurityIds(long repId) {
        String sql = SqlLoader.getSqlById("findReportSecurityIds");
        return DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>(), repId);
    }

    @Override
    public List<String> findReportClassifyIds(long repId) {
        String sql = SqlLoader.getSqlById("findReportClassifyIds");
        List<String> classifyList = DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>(), repId);
        //这里还要加上在行业上的分类
        classifyList.addAll(findRepIndustryIds(repId));
        return classifyList;
    }

    @Override
    public List<String> findReportSourceIds(long repId) {
        String sql = SqlLoader.getSqlById("findReportSourceIds");
        return DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>(), repId);
    }

    @Override
    public List<String> findRepIndustryIds(long repId) {
        String sql = SqlLoader.getSqlById("findRepIndustryIds");
        List<Map<String,Object>> mapList = DbUtil.queryMapList(sql, repId);
        Set<String> induCodeSet = Sets.newHashSet();
        for (Map<String, Object> map : mapList) {
            String code = map.get("code") == null ? "" : (String)map.get("code");
            String type = map.get("type") == null ? "" : (String)map.get("type");
            if ("P0201".equalsIgnoreCase(type)) { //老的分类
                String newCode = oldNewInduCodeMap.get(code);
                if (StringUtils.isNotBlank(newCode)) {
                    code = newCode;
                }else{
                    Logger.warn("repId[%d]的旧行业分类[%s]没有找到对应的新行业分类", repId, code);
                }
            }

            code = code + "    "; //先加长在说
            //制造业二级分类
/*            if (code.startsWith("C")) { //制造业, 取前面三位
                code = code.substring(0, 3);
            }else{ //其它行业,则取1位
                code = code.substring(0, 1);
            }*/
            //现在只取一级分类
            code = code.substring(0, 1);

            induCodeSet.add(code.trim());
        }

        return Lists.newArrayList(induCodeSet);
    }

    @Override
    public InfomationModel findNewsByClassifyUTSId(long utsId) {
        String sql = SqlLoader.getSqlById("findNewsByClassifyUTSId");
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, utsId);
    }

    @Override
    public InfomationModel findNewsByIndustryUTSId(long utsId) {
        String sql = SqlLoader.getSqlById("findNewsByIndustryUTSId");
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, utsId);
    }

    @Override
    public InfomationModel findNewsBySecurityUTSId(long utsId) {
        String sql = SqlLoader.getSqlById("findNewsBySecurityUTSId");
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, utsId);
    }

    @Override
    public InfomationModel findNewsByUTSId(long utsId, String tableName) {
        String sql = SqlLoader.getSqlById("findNewsByUTSId");
        sql = sql.replace("#tableName#",tableName);
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, utsId);
    }

    @Override
    public InfomationModel findAnnByUTSId(long utsId, String tableName) {
        String sql = SqlLoader.getSqlById("findAnnByUTSId");
        sql = sql.replace("#tableName#",tableName);
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, utsId);
    }

    @Override
    public InfomationModel findReportByUTSId(long utsId, String tableName) {
        String sql = SqlLoader.getSqlById("findReportByUTSId");
        sql = sql.replace("#tableName#",tableName);
        return DbUtil.queryExtractDBSingleBean(sql, InfomationModel.class, utsId);
    }

    @Override
    public List<String> findSecurityByPlateId(String[] typeIdArr) {

        Set<String> codeSet = Sets.newHashSet();
        for (String typeid : typeIdArr) {
            if (StringUtils.isNotBlank(typeid)) {
                String sql = SqlLoader.getSqlById("getSymbolByPlateId");
                sql = sql.replaceAll("#typeid#", typeid);
                List<String> codeList  = DbUtil.queryExtractDbWithHandler(sql, new ColumnListHandler<String>());
                if(codeList!=null && codeList.size()>0){
                  codeSet.addAll(codeList);
                }
            }
        }
        return Lists.newArrayList(codeSet.iterator());
    }
}
