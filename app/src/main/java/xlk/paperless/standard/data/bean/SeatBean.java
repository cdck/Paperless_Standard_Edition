package xlk.paperless.standard.data.bean;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc
 */
public class SeatBean {
    int devId;
    String devName;
    /**
     * 左上角x坐标百分比
     */
    float x;
    /**
     * 左上角y坐标百分比
     */
    float y;
    /**
     * 朝向
     */
    int direction;
    int memberId;
    String memberName;
    int issignin;
    int role;
    int facestate;

    public SeatBean(int devId, String devName, float x, float y, int direction, int memberId, String memberName, int issignin, int role, int facestate) {
        this.devId = devId;
        this.devName = devName;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.memberId = memberId;
        this.memberName = memberName;
        this.issignin = issignin;
        this.role = role;
        this.facestate = facestate;
    }

    public int getDevId() {
        return devId;
    }

    public void setDevId(int devId) {
        this.devId = devId;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getIssignin() {
        return issignin;
    }

    public void setIssignin(int issignin) {
        this.issignin = issignin;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getFacestate() {
        return facestate;
    }

    public void setFacestate(int facestate) {
        this.facestate = facestate;
    }

    @Override
    public String toString() {
        return "SeatBean{" +
                "devId=" + devId +
                ", devName='" + devName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", direction=" + direction +
                ", memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", issignin=" + issignin +
                ", role=" + role +
                ", facestate=" + facestate +
                '}';
    }
}

