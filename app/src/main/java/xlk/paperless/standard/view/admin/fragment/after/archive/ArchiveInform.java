package xlk.paperless.standard.view.admin.fragment.after.archive;

/**
 * @author Created by xlk on 2020/10/27.
 * @desc
 */
public class ArchiveInform {
    int id;
    String content;
    String result;

    public ArchiveInform(String content, String result) {
        this.content = content;
        this.result = result;
    }

    public ArchiveInform(int id, String content, String result) {
        this.id = id;
        this.content = content;
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
