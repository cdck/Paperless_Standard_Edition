package xlk.paperless.standard.view.admin.fragment.pre.tablecard;

/**
 * @author Created by xlk on 2020/11/5.
 * @desc
 */
public class TableCardBean {
    String fontName;
    int fontSize;
    int fontColor;
    float lx;
    float ly;
    float rx;
    float ry;
    int flag;
    int align;
    int type;

    public TableCardBean(String fontName, int fontSize, int fontColor, float lx, float ly, float rx, float ry, int flag, int align, int type) {
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontColor = fontColor;
        this.lx = lx;
        this.ly = ly;
        this.rx = rx;
        this.ry = ry;
        this.flag = flag;
        this.align = align;
        this.type = type;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public float getLx() {
        return lx;
    }

    public void setLx(float lx) {
        this.lx = lx;
    }

    public float getLy() {
        return ly;
    }

    public void setLy(float ly) {
        this.ly = ly;
    }

    public float getRx() {
        return rx;
    }

    public void setRx(float rx) {
        this.rx = rx;
    }

    public float getRy() {
        return ry;
    }

    public void setRy(float ry) {
        this.ry = ry;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TableCardBean{" +
                "fontName='" + fontName + '\'' +
                ", fontSize=" + fontSize +
                ", fontColor=" + fontColor +
                ", lx=" + lx +
                ", ly=" + ly +
                ", rx=" + rx +
                ", ry=" + ry +
                ", flag=" + flag +
                ", align=" + align +
                ", type=" + type +
                '}';
    }
}
