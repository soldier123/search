package processor;

import models.DefaultResponseModel;
import models.InfomationModel;
import models.ResponseModel;
import play.libs.F;
import play.modules.guice.InjectSupport;
import service.NewsService;
import utils.ValueUtil;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-27
 * Time: 上午9:20
 * 功能描述: 高级搜索
 */
@InjectSupport
@annotations.Process(name = Protocol.CmdName.ADVANCE_INFO_SEARCH)
public class AdvanceInfomationSearchProcessor implements Processor{
    @Inject
    static NewsService newsService;
    @Override
    public ResponseModel process() {
         // 新闻来源【】  新闻分类【】
        //分行公告类型 【】  公告行业【】
        //研报来源【】  研报类型 【】 研报行业【】
        Map<String,Object> paramMap = new HashMap<String,Object>();

        paramMap.put("startDate",RequestContext.getString(Protocol.InfomationSearchFieldName.STARTTIME));
        paramMap.put("endDate",RequestContext.getString(Protocol.InfomationSearchFieldName.ENDTTIME));

        paramMap.put("itype",RequestContext.getArray(Protocol.InfomationSearchFieldName.TYPE));

        paramMap.put("nsource",RequestContext.getArray(Protocol.InfomationSearchFieldName.NSOURCE));
        paramMap.put("ncids",RequestContext.getArray(Protocol.InfomationSearchFieldName.NCIDS));

        paramMap.put("acids",RequestContext.getArray(Protocol.InfomationSearchFieldName.ACIDS));
        paramMap.put("aiids",RequestContext.getArray(Protocol.InfomationSearchFieldName.AIIDS));
        paramMap.put("keyword",RequestContext.getString(Protocol.InfomationSearchFieldName.KEYWORD));

        paramMap.put("rsource",RequestContext.getArray(Protocol.InfomationSearchFieldName.RSOURCE));
        paramMap.put("rcids",RequestContext.getArray(Protocol.InfomationSearchFieldName.RCIDS));
        paramMap.put("riids",RequestContext.getArray(Protocol.InfomationSearchFieldName.RIIDS));

        paramMap.put("pageSize",RequestContext.getString(Protocol.GlobalFieldName.PAGESIZE));
        paramMap.put("pageNo",RequestContext.getString(Protocol.GlobalFieldName.PAGENO));

        F.T3<List<InfomationModel>,Long,Integer> result = newsService.advanceSearchNewsFilterWithType(paramMap);

        //3. 构造响应对像
        DefaultResponseModel<List<InfomationModel>> responseModel = new DefaultResponseModel<List<InfomationModel>>(result._1);
        responseModel.total = result._2;
        responseModel.size  = result._3;
        responseModel.pageNo = ValueUtil.getIfEmpty(paramMap.get("pageNo"), 1, ValueUtil.ValueType.INTEGER);

        return responseModel;
    }
}
