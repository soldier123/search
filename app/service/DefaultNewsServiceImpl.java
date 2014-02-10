package service;

import com.tom.springutil.StopWatch;
import dao.NewsDao;
import models.InfomationModel;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import play.Logger;
import play.Play;
import play.libs.F;
import play.modules.guice.GuicePlugin;
import play.modules.guice.InjectSupport;
import utils.CommonUtils;
import utils.ElasticsearchHelper;
import utils.ValueUtil;

import javax.inject.Inject;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 上午9:48
 * 功能描述:
 */
// 加上这个注解  否则dao不会进行行注入
@InjectSupport
public class DefaultNewsServiceImpl  implements NewsService {
    @Inject
    public static NewsDao newsDao;
    private static Map<String, String> symbol2IndustryMap = new HashMap<String, String>();

    static {
        List<SymbolMapIndustry> list = Play.plugin(GuicePlugin.class).getBeanOfType(NewsDao.class).findSymbolIndustry();
        for (SymbolMapIndustry t : list) {
            symbol2IndustryMap.put(t.symbol, t.industry);
        }
    }


    public F.T3<List<InfomationModel>, Long, Integer> searchNews(Map<String, Object> map) {
        BoolQueryBuilder qb = boolQuery();
        qb.must(QueryBuilders.queryString(ValueUtil.getIfEmpty(map.get("keyword"),"*",ValueUtil.ValueType.STRING)).autoGeneratePhraseQueries(true));
        if (map.get("startDate") != null && !"".equals(map.get("startDate"))) {
            String dateTmp = String.valueOf(map.get("startDate")).replaceAll("-", "") + "000000";
            qb.must(rangeQuery(InfomationFieldMapping.DECLAREDATE).gte(Long.valueOf(dateTmp)));
        }
        if (map.get("endDate") != null && !"".equals(map.get("endDate"))) {
            String dateTmp = String.valueOf(map.get("endDate")).replaceAll("-", "") + "235959";
            qb.must(rangeQuery(InfomationFieldMapping.DECLAREDATE).lte(Long.valueOf(dateTmp)));
        }
        int type = ValueUtil.getIfEmpty(map.get("type"), 0, ValueUtil.ValueType.INTEGER);
        if (type > 0) {
            qb.must(QueryBuilders.termQuery(InfomationFieldMapping.DOCUMENT_TYPE, type));
        }
        int pageNo = ValueUtil.getIfEmpty(map.get("pageNo"), ElasticsearchHelper.DEFAULT_PAGE_NO, ValueUtil.ValueType.INTEGER);
        int pageSize = ValueUtil.getIfEmpty(map.get("pageSize"), ElasticsearchHelper.DEFAULT_PAGE_SIZE, ValueUtil.ValueType.INTEGER);
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.DECLAREDATE).order(SortOrder.DESC);
        SearchResponse searchResponse =  ElasticsearchHelper.doSearchByQueryWithSort(index_name, qb, sortBuilder, pageNo, pageSize, index_type_news_info);
        List<InfomationModel> list =  ElasticsearchHelper.parseHits2List(searchResponse.hits(), InfomationModel.class);
        return F.T3(list,searchResponse.getHits().totalHits(), searchResponse.hits().hits().length);
    }

    @Override
    public F.T3<List<InfomationModel>, Long, Integer> advanceSearchNews(Map<String, Object> map) {
        // 从新闻 || 公告 || 研报中检索数据
        // (field1=1 && field2=2 && filed3=3)or(field1=4 && field2=6 && filed3=6)or(field1=7 && field2=8 && filed3=9)
          //拼装查询语句
        //新闻查询器
        AndFilterBuilder  newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
         newsFileFilterBuilder.add(FilterBuilders.termFilter(InfomationFieldMapping.DOCUMENT_TYPE,InfomationModel.NewsType.NEWS.getValue()));
            Object[] newsSource = (Object[]) map.get("nsource");
             if(newsSource.length>0){//mark-------把length为零的数组加入查询语句为导致查询无结果
                 newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.NEWS_SOURCE, newsSource));
             }
            Object[] newsClassifyIds = (Object[]) map.get("ncids");
            if(newsClassifyIds.length > 0){
                newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.NEWS_CLASSIFY_ID,newsClassifyIds));
            }
        //公告查询器
       AndFilterBuilder annFilterBuilder = FilterBuilders.andFilter().cache(false);
        annFilterBuilder.add(FilterBuilders.termFilter(InfomationFieldMapping.DOCUMENT_TYPE,InfomationModel.NewsType.ANN.getValue()));
           Object[] annClassifyId = (Object[]) map.get("acids");
            if(annClassifyId.length > 0){
            }
        annFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.ANN_CLASSIFY_ID,annClassifyId));
        Object[] annIndustryId = (Object[]) map.get("aiids");
            if(annIndustryId.length > 0){
                 annFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.ANN_INDUSTRY_ID, annIndustryId));
            }
      //研报过滤器
      AndFilterBuilder repFilterBuilder = FilterBuilders.andFilter().cache(false);
        repFilterBuilder.add(FilterBuilders.termFilter(InfomationFieldMapping.DOCUMENT_TYPE,InfomationModel.NewsType.REPORT.getValue()));
          Object[] reportSource = (Object[]) map.get("rsource");
            if(reportSource.length > 0){
                repFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.REPORT_SOURCE_ID, reportSource));
            }
         Object[] reportClassifId = (Object[]) map.get("rcids");
            if(reportClassifId.length > 0){
                repFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.REPORT_CLASSIFY_ID, reportClassifId));
            }
         Object[] reportIndustryId = (Object[]) map.get("riids");
            if(reportIndustryId.length > 0){
                repFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.REPORT_INDUSTRY_ID, (Object[])map.get("riids")));
            }

      OrFilterBuilder OrFilterBuilder =  FilterBuilders.orFilter(newsFileFilterBuilder, annFilterBuilder,repFilterBuilder);
        int pageNo = ValueUtil.getIfEmpty(map.get("pageNo"), ElasticsearchHelper.DEFAULT_PAGE_NO, ValueUtil.ValueType.INTEGER);
        int pageSize = ValueUtil.getIfEmpty(map.get("pageSize"), ElasticsearchHelper.DEFAULT_PAGE_SIZE, ValueUtil.ValueType.INTEGER);
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.DECLAREDATE).order(SortOrder.DESC);
        SearchResponse searchResponse = ElasticsearchHelper.doSearchByFilterWithSort(index_name, OrFilterBuilder, sortBuilder, pageNo, pageSize, index_type_news_info);
        List<InfomationModel> list =  ElasticsearchHelper.parseHits2List(searchResponse.hits(), InfomationModel.class);

        return F.T3(list, searchResponse.getHits().totalHits(), searchResponse.hits().hits().length);

    }

    public F.T3<List<InfomationModel>, Long, Integer> advanceSearchNewsFilterWithType(Map<String, Object> map) {
        // 从新闻 || 公告 || 研报中检索数据
        // (field1=1 && field2=2 && filed3=3)or(field1=4 && field2=6 && filed3=6)or(field1=7 && field2=8 && filed3=9)
        //拼装查询语句
        //新闻查询器
        Object[] types  = (Object[]) map.get("itype");
        List<Object> typeList = Arrays.asList(types);
        if(typeList.size() == 0){
            typeList.add("1");
            typeList.add("2");
            typeList.add("3");
        }
        //添加时间条件时间
        AndFilterBuilder andFilterBuilder = FilterBuilders.andFilter();
        if (map.get("startDate") != null && !"".equals(map.get("startDate"))) {
            String dateTmp = String.valueOf(map.get("startDate")).replaceAll("-", "") + "000000";
            andFilterBuilder.add(FilterBuilders.rangeFilter(InfomationFieldMapping.DECLAREDATE).gte(Long.valueOf(dateTmp)));
        }
        if (map.get("endDate") != null && !"".equals(map.get("endDate"))) {
            String dateTmp = String.valueOf(map.get("endDate")).replaceAll("-", "") + "235959";
            andFilterBuilder.add(FilterBuilders.rangeFilter(InfomationFieldMapping.DECLAREDATE).lte(Long.valueOf(dateTmp)));
        }
        OrFilterBuilder orFilterBuilder = FilterBuilders.orFilter();
        if(typeList.contains(String.valueOf(InfomationModel.NewsType.NEWS.getValue()))){
            AndFilterBuilder  newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
            newsFileFilterBuilder.add(FilterBuilders.termFilter(InfomationFieldMapping.DOCUMENT_TYPE,InfomationModel.NewsType.NEWS.getValue()));
            Object[] newsSource = (Object[]) map.get("nsource");
            if(newsSource.length > 0){//mark-------把length为零的数组加入查询语句为导致查询无结果
                newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.NEWS_SOURCE, newsSource));
            }
            Object[] newsClassifyIds = (Object[]) map.get("ncids");
            if(newsClassifyIds.length > 0){
                newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.NEWS_CLASSIFY_ID,newsClassifyIds));
            }
            Object[] newsIndustryIds = (Object[]) map.get("niids");
            if(newsIndustryIds.length > 0){//注意这里没有像其它
                newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.NEWS_INDUSTRY_ID,newsIndustryIds));
            }
            orFilterBuilder.add(newsFileFilterBuilder);
        }
        //公告查询器
        if(typeList.contains(String.valueOf(InfomationModel.NewsType.ANN.getValue()))){
            OrFilterBuilder annOrFilterBuiler = FilterBuilders.orFilter();
            AndFilterBuilder annFilterBuilderWithoutKeyword = FilterBuilders.andFilter().cache(false);
            AndFilterBuilder annFilterBuilderWithKeyword = FilterBuilders.andFilter().cache(false);
            annFilterBuilderWithoutKeyword.add(FilterBuilders.termFilter(InfomationFieldMapping.DOCUMENT_TYPE,InfomationModel.NewsType.ANN.getValue()));
            annFilterBuilderWithKeyword.add(FilterBuilders.termFilter(InfomationFieldMapping.DOCUMENT_TYPE,InfomationModel.NewsType.ANN.getValue()));

            Object[] annIndustryId = (Object[]) map.get("aiids"); //这个是板块代码
            if(annIndustryId.length > 0){ //有板块代码的话, 要转化为相应的股票代码集合查询
                List<String> symbolList = bordCode2Symbols(annIndustryId);
                annFilterBuilderWithoutKeyword.add(FilterBuilders.inFilter(InfomationFieldMapping.SYMBOL, symbolList.toArray()));
                annFilterBuilderWithKeyword.add(FilterBuilders.inFilter(InfomationFieldMapping.SYMBOL, symbolList.toArray()));
            }
            Object[] annClassifyId = (Object[]) map.get("acids");
            if(annClassifyId.length > 0){
                annFilterBuilderWithoutKeyword.add(FilterBuilders.inFilter(InfomationFieldMapping.ANN_CLASSIFY_ID,annClassifyId));
                annOrFilterBuiler.add(annFilterBuilderWithoutKeyword);
            }
            String keyword = ValueUtil.getIfEmpty(map.get("keyword"),null, ValueUtil.ValueType.STRING);
            if(keyword != null){
                if(annClassifyId.length > 0){
                    annFilterBuilderWithKeyword.add(FilterBuilders.notFilter(FilterBuilders.inFilter(InfomationFieldMapping.ANN_CLASSIFY_ID,annClassifyId)));
                    annFilterBuilderWithoutKeyword.add(FilterBuilders.notFilter(FilterBuilders.queryFilter(QueryBuilders.queryString(keyword).autoGeneratePhraseQueries(true).field(InfomationFieldMapping.TITLE))));
                }
                //自动分成短语查询 清确度更高一些
                annFilterBuilderWithKeyword.add(FilterBuilders.queryFilter(QueryBuilders.queryString(keyword).autoGeneratePhraseQueries(true).field(InfomationFieldMapping.TITLE)));
                annOrFilterBuiler.add(annFilterBuilderWithKeyword);
            }
            if(keyword == null && annClassifyId.length == 0){
                annOrFilterBuiler.add(annFilterBuilderWithoutKeyword);
            }
            orFilterBuilder.add(annOrFilterBuiler);
        }
        //研报过滤器
        if(typeList.contains(String.valueOf(InfomationModel.NewsType.REPORT.getValue()))){
            AndFilterBuilder repFilterBuilder = FilterBuilders.andFilter().cache(false);
            repFilterBuilder.add(FilterBuilders.termFilter(InfomationFieldMapping.DOCUMENT_TYPE,InfomationModel.NewsType.REPORT.getValue()));
            Object[] reportSource = (Object[]) map.get("rsource");
            if(reportSource.length > 0){
                repFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.REPORT_SOURCE_ID, reportSource));
            }
            Object[] reportClassifId = (Object[]) map.get("rcids");
            if(reportClassifId.length > 0){
                repFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.REPORT_CLASSIFY_ID, reportClassifId));
            }
            Object[] reportIndustryId = (Object[]) map.get("riids"); //这个是板块代码组
            if(reportIndustryId.length > 0){ //有板块代码的话, 要转化为相应的股票代码集合查询
                List<String> symbolList = bordCode2Symbols(reportIndustryId);

                repFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.SYMBOL, symbolList.toArray()));
            }
            orFilterBuilder.add(repFilterBuilder);
        }
        //最后归总的查询条件是 :用户设置的时间限制和用户的设置类型的交集
        andFilterBuilder.add(orFilterBuilder);
        int pageNo = ValueUtil.getIfEmpty(map.get("pageNo"), ElasticsearchHelper.DEFAULT_PAGE_NO, ValueUtil.ValueType.INTEGER);
        int pageSize = ValueUtil.getIfEmpty(map.get("pageSize"), ElasticsearchHelper.DEFAULT_PAGE_SIZE, ValueUtil.ValueType.INTEGER);
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.DECLAREDATE).order(SortOrder.DESC);
        SearchResponse searchResponse = ElasticsearchHelper.doSearchByFilterWithSort(index_name, andFilterBuilder,sortBuilder, pageNo, pageSize, index_type_news_info);
        List<InfomationModel> list =  ElasticsearchHelper.parseHits2List(searchResponse.hits(), InfomationModel.class);

        return F.T3(list, searchResponse.getHits().totalHits(), searchResponse.hits().hits().length);

    }

    private List<String> bordCode2Symbols(Object[] reportIndustryId) {
        String[] bordCodeArr = new String[reportIndustryId.length];//板块代码
        for (int i = 0; i < reportIndustryId.length; i++) {
            bordCodeArr[i] = reportIndustryId[i].toString();
        }

        return newsDao.findSecurityByPlateId(bordCodeArr);
    }

    @Override
    public F.T3<List<InfomationModel>, Long, Integer> searchNewsBySymbol(Map<String, Object> map) {

        Object[] symbols = (Object[])map.get("symbol");
        Object[] types = (Object[])map.get("type");
        int pageNo = ValueUtil.getIfEmpty(map.get("pageNo"), ElasticsearchHelper.DEFAULT_PAGE_NO, ValueUtil.ValueType.INTEGER);
        int pageSize = ValueUtil.getIfEmpty(map.get("pageSize"), ElasticsearchHelper.DEFAULT_PAGE_SIZE, ValueUtil.ValueType.INTEGER);
        AndFilterBuilder  newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
        if(types != null && types.length> 0 && ValueUtil.getIfEmpty(types[0],0, ValueUtil.ValueType.INTEGER) != 0){
            newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.DOCUMENT_TYPE,types));
        }
        if(symbols != null && symbols.length > 0 ){
            newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.SYMBOL,symbols));
        }
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.DECLAREDATE).order(SortOrder.DESC);
        SearchResponse searchResponse =  ElasticsearchHelper.doSearchByFilterWithSort(index_name, newsFileFilterBuilder, sortBuilder,pageNo, pageSize, index_type_news_info);
        List<InfomationModel> list =  ElasticsearchHelper.parseHits2List(searchResponse.hits(), InfomationModel.class);
        return F.T3(list, searchResponse.getHits().totalHits(), searchResponse.hits().hits().length);
    }
    /**
     * 建新闻索引
     */
    public void indexNewsInfo() {
        indexNewsInformation();
        //indexInformation(InfomationModel.NewsType.NEWS);
    }

    /**
     * 建公告索引
     */
    public void indexAnnInfo() {
        indexInformation(InfomationModel.NewsType.ANN);
    }

    /**
     * 建研报索引
     */
    public void indexReportInfo() {
        indexInformation(InfomationModel.NewsType.REPORT);

    }

    @Override
    public void indexSingleNewsInfo(long newsId) {
        indexNewsInfoBatch(ValueUtil.wrapSingleToList(newsId));
    }

    @Override
    public void indexSingleAnnInfo(long annId) {
        indexAnnInfoBatch(ValueUtil.wrapSingleToList(annId));
    }

    @Override
    public void indexSingleReportInfo(long reportId) {
        indexReportInfoBatch(ValueUtil.wrapSingleToList(reportId));
    }

    @Override
    public void indexNewsInfoBatch(List<Long> newsIds) {
        if(newsIds == null || newsIds.size() == 0){
            return ;
        }
        List<InfomationModel> list = new ArrayList<InfomationModel>(newsIds.size());
        try{
            for(long newsId :newsIds){
                list.add(newsDao.findNewsById(newsId));
            }
           doIndex(list,InfomationModel.NewsType.NEWS);
        }catch (Exception e){
            Logger.error(e.getMessage(),e);
        }

    }

    @Override
    public void indexAnnInfoBatch(List<Long> annIds) {
        if(annIds == null || annIds.size() == 0){
            return ;
        }
        List<InfomationModel> list = new ArrayList<InfomationModel>(annIds.size());
        try{
            for(long annId :annIds){
                list.add(newsDao.findAnnById(annId));
            }
            doIndex(list,InfomationModel.NewsType.ANN);
        }catch (Exception e){
            Logger.error(e.getMessage(),e);
        }
    }

    @Override
    public void indexReportInfoBatch(List<Long> reportIds) {
        if(reportIds == null || reportIds.size() == 0){
            return ;
        }
        List<InfomationModel> list = new ArrayList<InfomationModel>(reportIds.size());
        try{
            for(long reportId :reportIds){
                list.add(newsDao.findReportById(reportId));
            }
            doIndex(list,InfomationModel.NewsType.REPORT);
        }catch (Exception e){
            Logger.error(e.getMessage(),e);
        }
    }

    @Override
    public void updateIndex(long utsId, String tableName,String action) throws Exception {
        if(!actions.contains(action)){
            Logger.warn("%s为非法操作类型:",action);
            return;
        }
        //删除主表数据
        if(NewsDao.mainRelatedTable.contains(tableName) && "delete".equals(action)){
            BoolQueryBuilder qb = boolQuery();
            qb.must(termQuery(InfomationFieldMapping.UTSID, utsId)).must(termQuery(InfomationFieldMapping.DOCUMENT_TYPE, getInfoTypeByTableName(tableName).getValue()));
            ElasticsearchHelper.deleteByQuery(index_name,index_type_news_info,qb);
            Logger.info("删除表[%s],utsId[%s]",tableName,utsId);
            return ;

        }
        InfomationModel.NewsType newsType = null;
        InfomationModel model = null;
        if(NewsDao.newsRelatedTable.contains(tableName)){
             model = newsDao.findNewsByUTSId(utsId,tableName);
            if(model == null){
                return;
            }
            deleteIndexByNewsId(model.newsId);
            newsType = InfomationModel.NewsType.NEWS;

        }else if(NewsDao.annRelatedTable.contains(tableName)){
             model = newsDao.findAnnByUTSId(utsId,tableName);
            if(model == null){
               return;
            }
            deleteIndexByAnnId(model.annId);
            newsType = InfomationModel.NewsType.ANN;

        }else if(NewsDao.reportRelatedTable.contains(tableName)){
             model = newsDao.findReportByUTSId(utsId,tableName);
            if(model == null){
                return;
            }
            deleteIndexByReportId(model.reportId);
            newsType = InfomationModel.NewsType.REPORT;

        }else{
          //  Logger.warn("表%s不在更新索引范围内，请勿打扰",tableName);
              return ;
        }
        if("delete".equals(action)){
               return ;
        }
        List<InfomationModel> list = ValueUtil.wrapSingleToList(model);
        doIndex(list,newsType);
       // Logger.warn("utsId为:%d表名为:%s重建索引成功",utsId,tableName);
    }

    private void deleteIndexByReportId(long reportId){
        ElasticsearchHelper.deleteByQuery(index_name,index_type_news_info,QueryBuilders.termQuery(InfomationFieldMapping.REPORT_ID,reportId));
    }
    private void deleteIndexByAnnId(long annId){
        ElasticsearchHelper.deleteByQuery(index_name,index_type_news_info,QueryBuilders.termQuery(InfomationFieldMapping.ANN_ID,annId));
    }
    private void deleteIndexByNewsId(long newsId){
        ElasticsearchHelper.deleteByQuery(index_name,index_type_news_info,QueryBuilders.termQuery(InfomationFieldMapping.NEWS_ID,newsId));

    }

    /**
     * 新闻表建索引
     */
    private void indexNewsInformation() {
        long numCount = 0;
        List<InfomationModel> infomationList = null;
        infomationList = newsDao.findNewsBefore2000Year();
        try {
            doIndex(infomationList, InfomationModel.NewsType.NEWS);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        numCount += infomationList.size();

        Date startDate = CommonUtils.parseDate("2000-01-01");
        Date maxDate = newsDao.findMaxNewsDate();
        maxDate = DateUtils.addDays(maxDate, 10); //在最大日期的基础上在加上10天, 总可以是最大值了吧. 如果建全部索引要用上10天, 那就会出错. game voer!!
        while (startDate.before(maxDate)) {
            StopWatch sw = new StopWatch("处理新闻日期:" + CommonUtils.getFormatDate("yyyy-MM-dd", startDate));
            sw.start("查询数据库");
            infomationList = newsDao.findNewsInDay(startDate);
            sw.stop();
            sw.start("索引记录条数:" + infomationList.size() + ",已索引新闻总条数:" + numCount);
            try {
                doIndex(infomationList, InfomationModel.NewsType.NEWS);
            } catch (Exception e) {
                Logger.error(e.getMessage());
            }
            sw.stop();
            if (infomationList.size() > 0) {
                Logger.info(sw.prettyPrint());
            }
            sw = null;
            numCount += infomationList.size();
            startDate = DateUtils.addDays(startDate, 1);
        }
    }

    private void indexInformation(InfomationModel.NewsType type) {
        List<InfomationModel> infomationList = null;
        int index = 0;
        for (; ; ) {
            switch (type) {
                case NEWS:
                    infomationList = newsDao.findNews(index, 1000);
                    break;
                case ANN:
                    infomationList = newsDao.findAnnInfos(index, 1000);
                    break;
                case REPORT:
                    infomationList = newsDao.findReports(index, 1000);
                    break;
                default:
                    break;
            }
            if (infomationList == null || infomationList.size() == 0) {
                break;
            }
            index += 1000;
            try {
                long stime = System.currentTimeMillis();
                Logger.info("type :" + type + "---->start index :" + index);
                doIndex(infomationList, type);
                long cost = System.currentTimeMillis() - stime;
                Logger.info("finish :" + type + "---->finish index :" + index + "=---->cost time:" + cost);

            } catch (Exception e) {
                Logger.error(e.getMessage());
            }
        }

    }

    private void doIndex(List<InfomationModel> newsList, InfomationModel.NewsType type) throws Exception {
        if (newsList == null || newsList.size() < 1) {
            return ;
        }
        int index = 0;
        BulkRequestBuilder bulkRequestBuilder = ElasticsearchHelper.getClient().prepareBulk();
        for (InfomationModel news : newsList) {
            switch (type) {
                case NEWS:
                    news.newsCids = newsDao.findNewsClassifyIds(news.newsId);
                    news.symbols = newsDao.findNewsSecurityIds(news.newsId);
                    news.nIids = newsDao.findNewsIndustryIds(news.newsId);
                    break;
                case ANN:
                    news.annCids = newsDao.findAnnClassifyIds(news.annId);
                    news.symbols = newsDao.findAnnSecurityIds(news.annId);
                    if (news.symbols.size() == 0) { //取不到相关的证券信息, 则认为是无效公告, 跳过
                        continue;
                    }
                    List<String> anniid = new ArrayList<String>(news.symbols.size());
                    for (String str : news.annCids) {
                        anniid.add(symbol2IndustryMap.get(str));
                    }
                    news.annIids = anniid;
                    break;
                case REPORT:
                    news.reportCids = newsDao.findReportClassifyIds(news.reportId);
                    news.symbols = newsDao.findReportSecurityIds(news.reportId);
                    news.rSourceIds = newsDao.findReportSourceIds(news.reportId);
                    news.rIids = newsDao.findRepIndustryIds(news.reportId);
                    break;
                default:
            }
            bulkRequestBuilder.add(ElasticsearchHelper.getClient().prepareIndex(index_name, index_type_news_info)
                    .setSource(jsonBuilder()
                            .startObject()
                                .field(InfomationFieldMapping.NEWS_ID, news.newsId)
                                .field(InfomationFieldMapping.ANN_ID, news.annId)
                                .field(InfomationFieldMapping.REPORT_ID, news.reportId)
                                .field(InfomationFieldMapping.TITLE, news.title)
                                .array(InfomationFieldMapping.SYMBOL, news.symbols.toArray())
                                .field(InfomationFieldMapping.NEWS_SOURCE, news.nSource)
                                .field(InfomationFieldMapping.DECLAREDATE, Long.parseLong(DateFormatUtils.format(news.declareDate, "yyyyMMddHHmmss")))
                                .array(InfomationFieldMapping.NEWS_CLASSIFY_ID, toArray(news.newsCids))
                                .array(InfomationFieldMapping.ANN_CLASSIFY_ID, toArray(news.annCids))
                                .array(InfomationFieldMapping.ANN_INDUSTRY_ID, toArray(news.annIids))
                                .array(InfomationFieldMapping.REPORT_SOURCE_ID, toArray(news.rSourceIds))
                                .array(InfomationFieldMapping.REPORT_CLASSIFY_ID, toArray(news.reportCids))
                                .array(InfomationFieldMapping.REPORT_INDUSTRY_ID, toArray(news.rIids))
                                .field(InfomationFieldMapping.DOCUMENT_TYPE, type.getValue())
                                .field(InfomationFieldMapping.ATTACH, news.attach)
                                .field(InfomationFieldMapping.UPDATEDATE, Long.parseLong(DateFormatUtils.format(news.updateDate, "yyyyMMddHHmmss")))
                                .field(InfomationFieldMapping.UTSID, news.utsId)
                                .array(InfomationFieldMapping.NEWS_INDUSTRY_ID, news.nIids)
                            .endObject()
                    ));
        }

        if (bulkRequestBuilder.numberOfActions() > 0) {
            ElasticsearchHelper.indexByBulk(bulkRequestBuilder);
        } else {
            Logger.info("这个批次没有相应的记录要索引");
        }
    }

    private Object[] toArray(List list) {
        if (list == null) {
            return new Object[]{};
        }
        Object[] obj = list.toArray();
        return obj;
    }

    public void createIndexLib() {
        ElasticsearchHelper. createIndex(index_name);
    }

    //mapping
    public void createNewsInfoMapping() throws Exception {
        XContentBuilder  mapping= XContentFactory.jsonBuilder()
                .startObject()
                .startObject(index_type_news_info)
                .startObject("properties")
                .startObject(InfomationFieldMapping.NEWS_ID)    //新闻ID
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.ANN_ID)    //公告ID
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.REPORT_ID)    //研报ID主键
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.TITLE)        //标题
                .field("type", "string")
                .field("store", "yes")
                .field("indexAnalyzer", "ik")
                .field("searchAnalyzer", "ik")
                .endObject()
                        //股票ID
                .startObject(InfomationFieldMapping.SYMBOL)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()

                .startObject(InfomationFieldMapping.NEWS_SOURCE)        //新闻来源
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.DECLAREDATE)        //发表时间
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.UPDATEDATE)        //发表时间
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                        //新闻分类ID
                .startObject(InfomationFieldMapping.NEWS_CLASSIFY_ID)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()

                        //公告分类ID
                .startObject(InfomationFieldMapping.ANN_CLASSIFY_ID)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()

                        //公告行业ID
                .startObject(InfomationFieldMapping.ANN_INDUSTRY_ID)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                        //研报来源id
                .startObject(InfomationFieldMapping.REPORT_SOURCE_ID)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()

                        //新闻行业id
                .startObject(InfomationFieldMapping.NEWS_INDUSTRY_ID)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()

                .startObject(InfomationFieldMapping.REPORT_CLASSIFY_ID)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                        //研报行业ID
                .startObject(InfomationFieldMapping.REPORT_INDUSTRY_ID)
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.DOCUMENT_TYPE)    //类型 1 新闻 2 公告  3研报
                .field("type", "integer")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.UTSID)    //utsid
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.ATTACH)    //附件
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .endObject()
                .endObject()
                .endObject();
        ElasticsearchHelper.createMapping(index_name, index_type_news_info, mapping);
    }
    private InfomationModel.NewsType getInfoTypeByTableName(String taleName){

        if("ann_announcementinfo".equals(taleName)){
            return InfomationModel.NewsType.ANN;
        }else if("news_newsinfo".equals(taleName)){
            return InfomationModel.NewsType.NEWS;
        }else{
           return   InfomationModel.NewsType.REPORT;
        }

    }

    /**
     * User: 刘建力(liujianli@gtadata.com))
     * Date: 13-3-22
     * Time: 下午6:33
     * 功能描述:
     */
    public static class SymbolMapIndustry {

        public String symbol;
        public String industry;
    }

}
