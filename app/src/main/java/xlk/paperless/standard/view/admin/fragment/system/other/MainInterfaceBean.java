package xlk.paperless.standard.view.admin.fragment.system.other;

/**
 * @author Created by xlk on 2020/11/7.
 * @desc
 */
public class MainInterfaceBean {
    int faceId;
    int flag;
    int fontSize;
    int color;
    int align;
    int fontFlag;
    String fontName;
    /**
     * 百分比数，使用时需要除于100
     */
    float lx;
    float ly;
    float bx;
    float by;

    public MainInterfaceBean(int faceId, int flag) {
        this.faceId = faceId;
        this.flag = flag;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public int getFontFlag() {
        return fontFlag;
    }

    public void setFontFlag(int fontFlag) {
        this.fontFlag = fontFlag;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
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

    public float getBx() {
        return bx;
    }

    public void setBx(float bx) {
        this.bx = bx;
    }

    public float getBy() {
        return by;
    }

    public void setBy(float by) {
        this.by = by;
    }

    @Override
    public String toString() {
        return "MainInterfaceBean{" +
                "faceId=" + faceId +
                ", flag=" + flag +
                ", fontSize=" + fontSize +
                ", color=" + color +
                ", align=" + align +
                ", fontFlag=" + fontFlag +
                ", fontName='" + fontName + '\'' +
                ", lx=" + lx +
                ", ly=" + ly +
                ", bx=" + bx +
                ", by=" + by +
                '}';
    }
}
