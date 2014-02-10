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
 * Date: 13-3-29
 * Time: 下午4:49
 * 功能描述:
 */
@InjectSupport
@annotations.Process(name = Protocol.CmdName.SYMBOL_INFO_SEARCH)
public class SearchSymbolInfomationProcessor implements Processor {
    @Inject
    static NewsService newsService;
    @Override
    public ResponseModel process() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("pageSize",RequestContext.getString(Protocol.GlobalFieldName.PAGESIZE));
        map.put("pageNo",RequestContext.getString(Protocol.GlobalFieldName.PAGENO));
        map.put("symbol",RequestContext.getArray(Protocol.InfomationSearchFieldName.SYMBOL));
        map.put("type",RequestContext.getArray(Protocol.InfomationSearchFieldName.TYPE));

        F.T3<List<InfomationModel>,Long,Integer>  result = newsService.searchNewsBySymbol(map);
        //3. 构造响应对像
        DefaultResponseModel<List<InfomationModel>> responseModel = new DefaultResponseModel<List<InfomationModel>>(result._1);
        responseModel.total = result._2;
        responseModel.size  = result._3;
        responseModel.pageNo = ValueUtil.getIfEmpty(map.get("pageNo"), 1, ValueUtil.ValueType.INTEGER);
        return responseModel;
    }
}
