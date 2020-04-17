package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceIM;

/**
 * @author xlk
 * @date 2020/3/17
 * @Description: 会议交流的自定义信息
 */
public class ChatMessage {
    int type;
    InterfaceIM.pbui_Type_MeetIM message;

    public ChatMessage(int type, InterfaceIM.pbui_Type_MeetIM message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public InterfaceIM.pbui_Type_MeetIM getMessage() {
        return message;
    }
}
