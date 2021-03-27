package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceIM;

/**
 * @author xlk
 * @date 2020/3/17
 * @desc 会议交流的自定义信息
 */
public class ChatMessage {
    /**
     * =0收到的信息，=1自己发送的信息
     */
    private int type;
    private InterfaceIM.pbui_Type_MeetIM message;

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
