package xlk.paperless.standard.data.exportbean;

import java.util.List;

import xlk.paperless.standard.data.bean.SubmitMember;

/**
 * @author xlk
 * @date 2020/4/3
 * @Description: 导出成Excel文件
 */
public class ExportSubmitMember {
    String title;//投票/选举标题
    String createTime;//生成文件的时间
    String yd;
    String sd;
    String yt;
    String wt;
    List<SubmitMember> submitMembers;

    public ExportSubmitMember(String title, String createTime, String yd, String sd, String yt, String wt, List<SubmitMember> submitMembers) {
        this.title = title;
        this.createTime = createTime;
        this.yd = yd;
        this.sd = sd;
        this.yt = yt;
        this.wt = wt;
        this.submitMembers = submitMembers;
    }

    public String getTitle() {
        return title;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getYd() {
        return yd;
    }

    public String getSd() {
        return sd;
    }

    public String getYt() {
        return yt;
    }

    public String getWt() {
        return wt;
    }

    public List<SubmitMember> getSubmitMembers() {
        return submitMembers;
    }
}
