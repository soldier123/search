package utils;

/**
 * 放redis的key的前缀常量定义
 * User: wenzhihong
 * Date: 12-9-11
 * Time: 下午1:49
 */
public class RedisKey {
    /**
     * 全局
     */
    public static class Global{
        public static final String backup_record_change_list = "backup_rcl"; //备份消息队列. List结构. 用队列
    }

    /**
     * 资讯类
     */
    public static class News{
        public static final String sec_news_date_map = "ne1";  //各股新闻(最大时间, 最小时间 (用数字表示)). 对应于redis的map. secId做为key. (最大时间, 最小时间) 作为value

        public static final String indu_news_date_map = "ne2";  //行业新闻(最大时间, 最小时间 (用数字表示)). 对应于redis的map. 一级行业代码做为key. (最大时间, 最小时间) 作为value

        public static final String sec_bulletin_date_map = "ne3";  //各股公告(最大时间, 最小时间 (用数字表示)). 对应于redis的map. secId做为key. (最大时间, 最小时间) 作为value

        public static final String sec_report_date_map = "ne4";  //各股研报(最大时间, 最小时间 (用数字表示)). 对应于redis的map. secId做为key. (最大时间, 最小时间) 作为value

        public static final String indu_report_date_map = "ne5";  //行业研报(最大时间, 最小时间 (用数字表示)). 对应于redis的map. 一级行业代码做为key. (最大时间, 最小时间) 作为value

        //----
        public static final String company_bulletin = "11_"; // 加上 secId  redis的list  CompanyBulletin

        public static final String company_news = "12_"; //加上 secId  redis的list  News

        public static final String indu_news = "13_"; //加上证监会一级行业代码  redis的list News

        public static final String company_report = "14_"; //加上 secId redis的list  ReportInfo

        public static final String indu_report = "15_";//加上证监会一级行业代码  redis的list ReportInfo

        //--
        public static final String rebuild_sec_news = "ne6"; //要重新生成公司新闻的secId map. 对应于 redis的map secId做为key值

        public static final String rebuild_sec_bulletin = "ne7"; //要重新生成公司公告的secId map. 对应于 redis的map secId做为key值

        public static final String rebuild_sec_report = "ne8"; //要重新生成公司研报的secId map. 对应于 redis的map secId做为key值


        public static final String recently_news = "ne9"; //最新新闻. 对应于redis的 list结构  对应于News类
    }


    /**
     * 最新动态
     */
    public static class NewestInfo {
        //涨幅_股票
        public static final String markup_stock = "5_";  //QuoteMarkup结构  加上 scode(股票代码)

        //涨幅_指数
        public static final String markup_index = "6_";  //QuoteMarkup结构  加上 scode(指数代码)

        //概念板块
        public static final String concept_plate = "16_"; //加上 scode  List<PlateDto>的json数据.

        //投资要点
        public static final String invest_Mainpoint = "17_";//加上scode, value为string

        //大事提醒
        public static final String great_InventRemind = "18_";  //加上secId  List<GreatInventRemind>的json数据

        //三大主营业务
        public static final String top3MainBusiness = "19_"; //加上 institutionId List<Top3MainBusinessDto>的数据

        //财务比率(短)
        public static final String financialRatios_short = "20_"; //加上 institutionId List<FinancialRatiosDto>.

        //财务画图数据
        public static final String financial_draw_data = "21_"; //加上 institutionId List<FinanceDataShortDto>, 时间从小到大.

        //研报综合评级
        public static final String report_rating = "22_";   //加上secId  ReportRatingStatisticDto

    }

    /**
     * 公司概况
     */
    public static class CompanyInfo {
        public static final String institutioninfo = "23_"; //公司基本信息, 来源于公司表. 加上 institutionId   InstitutionInfoDto

        public static final String eqIpoInfo = "24_"; //ipo信息, 加上 secId   EqIpoInfoDto

        public static final String eqIpoResult = "25_"; //ipo result 加上 secId  EqIpoResultDto

        public static final String sharesStructureInfo = "26_"; //股本信息, 加上 institutionId   SharesStructureInfoDto

        public static final String marketQuotation = "27_"; //市场行情. 加上secId   MarketQuotationDto

        public static final String ipoMarketQuotation = "28_"; //市场行情. 加上secId  MarketQuotationDto

        public static final String agencyOrg = "29_"; //代理机构. 加上secId   AgencyOrgDto
    }

    /**
     * 股东股本
     */
    public static class StockHolderCapital {
        public static final String stockHolderTop10 = "30_"; //10大股东 加上 institutionId, List<StockHolderDto> 结果集

        public static final String orgStockHolder = "31_"; //机构股东 加上 institutionId, List<StockHolderDto> 结果集

        public static final String stockFlowHolderTop10 = "32_"; //10大流通股东 加上 institutionId, List<StockHolderDto> 结果集

        public static final String orgStockFlowHolder = "33_"; //机构流通股东 加上 institutionId, List<StockHolderDto> 结果集

        public static final String stockOrgGroupSumHolder = "34_"; //机构类型合计持股 加上 institutionId List<OrgGroupSumHolder> 里面是按endDate倒序排列

        public static final String capitalStructure = "35_"; //股本结构 加上 institutionId. CapitalStructure 结果

        public static final String limitAndLift = "36_"; //限售解禁. 加上 institutionId  List<LimitedAndLift> 结果集
    }

    /**
     * 高管研究
     */
    public static class TopManager {
        public static final String topManager = "37_";  //高管信息(包含了高管人员, 董事会, 监事会) 加上 institutionId,  List<TopManager>结果集, 压缩存储

        public static final String leaveOffice = "38_"; //离职高管信息 加上 institutionId,  List<TopManager>结果集, 压缩存储

        public static final String holdShare = "39_"; //高管持股信息 加上 institutionId List<HoldShare> 按 日期 降序, 然后按 持股数量 降序,

        public static final String holdingchange = "40_"; //高管持股变动 加上 institutionId List<HoldingChange> 按日期 降序
    }

    /**
     * 分红融资
     */
    public static class Dividend {
        public static final String cashBonusAndRaiseFund = "41_"; //个股的 分红融资 加上 secId. RaiseFundOverall 结果
        public static final String allMarketCashBonusAndRaiseFund = "42"; //整个市场的 分红融资 RaiseFundOverall 结果
        public static final String cashBonusDetail = "43_"; //分红明细, 加上 secId List<CashBonusDetail> 结果
        public static final String addIssuingDetail = "44_";//增发明细. 加上 secId. List<AddIssuingDetail>
        public static final String allotmentDetail = "45_"; //配股明细. 加上 secId. List<AllotmentDetail>
    }

    /**
     * 行业分析
     */
    public static class IndustryAna {
        public static final String sec_lastedMarketPerfor = "46_"; //股票最近市场表现, 加上 secId. SecMarketPerfor
        public static final String idx_lastedMarketPerfor = "47_"; //指数最近市场表现, 加上 secId. SecMarketPerfor

        public static final String companyscale = "48_"; //公司规模 , 加上行业代码, 取这个行业的前6  List<CompanyScale>
        public static final String company_sec_scale = "49_";//每只股票的公司规模排名. 加上 institutionId  CompanyScale

        public static final String appraisement = "50_"; //估值水平, 加上行业代码, 取这个行业的前6  List<AppraisementRankItem>
        public static final String appraisement_sec = "51_"; //每只股票的估值水平排名. 加上 secId  AppraisementRankItem

        public static final String appr_full_indu = "a50"; //估值水平, 加上行业代码 AppraisementFullIndu

        public static final String financeStatus = "52_"; //财务状况. 加上行业代码, 取这个行业的前6 List<SecEps>
        public static final String financeStatus_sec = "53_"; //第只股票的财务状况. 加上  institutionId,  SecEps
    }

    /**
     * 财务分析
     */
    public static class FinanceAna {
        public static final String fullView = "54_"; //加上 institutionId FullView

        public static final String debtPay = "55_"; //加上 institutionId List<DebtPayItem>  按时间降序排列
        public static final String earnPower = "56_";//加上 institutionId List<EarnPowerItem>  按时间降序排列
        public static final String perShare = "57_"; //加上 institutionId List<PerShareItem>  按时间降序排列
        public static final String lcDiscloseIndex = "58_"; //加上 institutionId List<LcDiscloseIndexItem>  按时间降序排列

        public static final String dupont = "59_"; //杜邦分析  加上 institutionId DupontVal
        public static final String balanceSheet = "60_"; //资产负债表.  加上 institutionId 里面存放在的是  List<BalanceSheet>, 按时间降序排列
        public static final String cashFlowSheet = "61_"; //  现金流量表. 加上 institutionId 里面存放的是  List<CashFlowSheet>, 按时间降序排列
        public static final String cashFlowSheetSingle = "61s_"; //  现金流量表. 加上 institutionId 里面存放的是  List<CashFlowSheet>, 按时间降序排列
        public static final String incomeSheet = "62_"; //利润表. 加上 institutionId 里面存放的是  List<IncomeSheet>, 按时间降序排列
        public static final String incomeSheetSingle = "62s_"; //利润表. 加上 institutionId 里面存放的是  List<IncomeSheet>, 按时间降序排列

        public static final String assetImpairment = "63_"; //资产负债表附注—资产减值准备 加上 institutionId List<AssetImpairment> 按时间降序排列
        public static final String equityInvest = "64_"; //资产负债表附注—长期股权投资 加上 institutionId List<EquityInvest> 按时间降序排列
        public static final String longtermPrepaidFee = "65_"; //资产负债表附注—长期待摊费用 加上 institutionId List<LongtermPrepaidFee> 按时间降序排列
        public static final String deferredIncomeTax = "66_"; //资产负债表附注—递延所得税资产和递延所得税负债  加上 institutionId List<DeferredIncomeTax> 按时间降序排列
        public static final String operateIncomeCosts = "67_"; //利润表附注—营业收入、营业成本 加上 institutionId List<OperateIncomeCosts> 按时间降序排列
        public static final String financeCosts = "68_"; //利润表附注—财务费用 加上 institutionId List<FinanceCosts> 按时间降序排列
        public static final String businessTaxAppend = "69_"; //利润表附注—营业税金及附加 加上 institutionId List<BusinessTaxAppend> 按时间降序排列

        public static final String reportItem1 = "f5_";//一季报. 加上 secId. List<FinanceReportItem> 按时间降序
        public static final String reportItem2 = "f6_";//中报. 加上 secId. List<FinanceReportItem> 按时间降序
        public static final String reportItem3 = "f7_";//三季报. 加上 secId. List<FinanceReportItem> 按时间降序
        public static final String reportItem4 = "f8_";//年报. 加上 secId. List<FinanceReportItem> 按时间降序
    }

    /**
     * 盈利预测
     */
    public static class PredictProfit {
        public static final String ratingChange = "70_";            //评级变动 加上 secId   List<RatingChange> 按时间降序
        public static final String targetPrice = "71_";              //预测目标价. 加上 secId  List<PriceItem>  按时间升序
        public static final String actualPrice1Month = "72_";       //近一个月的价格 加上 secId  List<PriceItem>       按时间升序
        public static final String last5YearEps = "73_";            //近5年的年报eps 加上 institutionId List<PriceIt2>   按时间升序
        public static final String last5YearNetProfit = "74_";      //近5年的年报的 净利润 加上 institutionId List<PriceIt2>  按时间升序
        public static final String forecast3YearEps = "75_";        //三年的预测eps值 加上 secId   List<PriceIt2> 按时间升序
        public static final String forecast3YearNetProfit = "76_"; //三年的 净利润 值 加上 secId   List<PriceIt2> 按时间升序
        public static final String f3yearEpsDetail = "77_";          //三年的预测eps值 明细 加上 secId   List<DetailByReport>
        public static final String f3yearNetProfitDetail = "78_";  //三年的 净利润 值 明细 加上 secId   List<DetailByReport>
    }

    /**
     * 重大事项
     */
    public static class MajorMatter {
        public static final String guarantee = "79_"; // 对外担保, 加上 institutionId List<Guarantee>
        public static final String violation = "80_"; //违规处理 , 加上 institutionId List<Violation>
        public static final String blocktrade = "81_"; //大宗交易 加上secId, 使用redis的list数据结构, 里面放的每一项是 BlockTrade
    }

    /**
     * 关联个股
     */
    public static class RelatedStock {
        public static final String top10Holders = "82_"; //10大股东. 加上 institutionId List<HolderItem>

        public static final String sameshareholder = "83_"; //同股东持股 加上 institutionId List<SameShareHolder>

        public static final String sameindustryholder = "84"; //同行业个股eps 加上 行业代码. induCode  List<SameInduItem>
    }

    /**
     * 融资融券
     */
    public static class Financing {
        public static final String allFinancingSecList = "85"; //融资融券的secId数组.  List<Long>

        public static final String financing = "86_"; //融资融券 加上 secId  redis的list结构  Financing 项
    }
}
