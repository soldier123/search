package models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import processor.Protocol;

import java.util.Date;
import java.util.List;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 上午9:22
 * 功能描述: 新闻结构体
 */
//资讯 包含 新闻  建索引类型

public class InfomationModel extends DataModelAdaptor {
    /**
     * 主键 es自动创建id
     */
    @Expose(serialize = false)
    public long id;
    /**
     * 新闻ID
     */
    @SerializedName(Protocol.InfomationSearchFieldName.NEWSID)
    @Expose
    public long newsId;
    /**
     * 公告ID
     */
    @SerializedName(Protocol.InfomationSearchFieldName.ANNID)
    @Expose
    public long annId;
    /**
     * 研报ID
     */
    @SerializedName(Protocol.InfomationSearchFieldName.REPORTID)
    @Expose
    public long reportId;

    /**
     * 发表时间
     */
    @SerializedName(Protocol.InfomationSearchFieldName.DECLAREDATE)
    @Expose
    public Date declareDate;
    /**
     * 新闻 公告 研报 标题
     */
    @SerializedName(Protocol.InfomationSearchFieldName.TITLE)
    @Expose
    public String title;
    /**
     * 股票ID
     */
    @SerializedName(Protocol.InfomationSearchFieldName.SYMBOL)
    @Expose
    public List<String> symbols;
    /**
     * 新闻来源
     */
    public String nSource;
    /**
     * 新闻所属类型ID
     */
    @SerializedName(Protocol.InfomationSearchFieldName.NCIDS)
    public List<String> newsCids;
    /**
     * 公告类型id
     */
    @SerializedName(Protocol.InfomationSearchFieldName.ACIDS)
    public List<String> annCids;
    /**
     * 公告行业ID
     */
    @SerializedName(Protocol.InfomationSearchFieldName.AIIDS)
    public List<String> annIids;
    /**
     * 研报来源
     */
    public List<String> rSourceIds;
    /**
     * 研报所属类型ID
     */
    public List<String> reportCids;
    /**
     * 研报所属行业id
     */
    public List<String> rIids;

    /**
     * 新闻所属行业id
     */
    public List<String> nIids;
    /**
     * 类型
     */
    @SerializedName(Protocol.InfomationSearchFieldName.TYPE)
    @Expose
    public int type;
    /**
     * 附件
     */
    @SerializedName(Protocol.InfomationSearchFieldName.ATTACH)
    @Expose
    public String attach;
    /**
     * 更新时间
     */
    @SerializedName(Protocol.InfomationSearchFieldName.UPDATEDATE)
    @Expose
    public Date updateDate;
    public long utsId;

    public enum NewsType {
        NEWS,
        ANN,
        REPORT;

        public int getValue() {
            switch (this) {
                case NEWS:
                    return 1;
                case ANN:
                    return 2;
                case REPORT:
                    return 3;
            }
            return -1;
        }
    }
}
