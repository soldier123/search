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
 * Date: 13-3-26
 * Time: 下午2:30
 * 功能描述: 信息查询处理器
 *
 */
@InjectSupport
@annotations.Process(name = Protocol.CmdName.COMMON_INFO_SEARCH)
public class SearchInfomationProcessor implements Processor {
    @Inject
    static NewsService newsService;
    @Override
    public ResponseModel process() {
        //1. 构造查询结构
        Map<String,Object> map = new HashMap<String,Object>();

        map.put("keyword",RequestContext.getString(Protocol.InfomationSearchFieldName.KEYWORD));
        map.put("startDate",RequestContext.getString(Protocol.InfomationSearchFieldName.STARTTIME));
        map.put("endDate",RequestContext.getString(Protocol.InfomationSearchFieldName.ENDTTIME));
        map.put("pageSize",RequestContext.getString(Protocol.GlobalFieldName.PAGESIZE));
        map.put("pageNo",RequestContext.getString(Protocol.GlobalFieldName.PAGENO));
        map.put("type",RequestContext.getString(Protocol.InfomationSearchFieldName.TYPE));

        //2. 查询
        F.T3<List<InfomationModel>,Long,Integer>  result = newsService.searchNews(map);

        //3. 构造响应对像
        DefaultResponseModel<List<InfomationModel>> responseModel = new DefaultResponseModel<List<InfomationModel>>(result._1);
        responseModel.total = result._2;
        responseModel.size  = result._3;
        responseModel.pageNo = ValueUtil.getIfEmpty(map.get("pageNo"), 1, ValueUtil.ValueType.INTEGER);

        return responseModel;
    }
}
