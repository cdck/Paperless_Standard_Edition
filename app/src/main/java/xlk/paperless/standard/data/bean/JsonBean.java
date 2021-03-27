package xlk.paperless.standard.data.bean;

import java.util.List;

/**
 * @author Created by xlk on 2020/11/12.
 * @desc 收到远程配置的通知
 */
public class JsonBean {

    /**
     * restart : 0
     * item : [{"section":"areaaddr","key":"area0ip","value":"10.248.6.118"},{"section":"areaaddr","key":"area0port","value":"2160"},{"section":"Buffer Dir","key":"configdir","value":"/storage/emulated/0/PaperlessStandardEdition/"},{"section":"Buffer Dir","key":"mediadirsize","value":"5000"},{"section":"debug","key":"hwencode","value":"0"},{"section":"debug","key":"hwdecode","value":"0"},{"section":"debug","key":"console","value":"0"},{"section":"debug","key":"camaracap","value":"0"},{"section":"debug","key":"disablemulticast","value":"1"},{"section":"debug","key":"mediatranscode","value":"0"},{"section":"selfinfo","key":"streamprotol","value":"1"},{"section":"debug","key":"encmode","value":"2"},{"section":"debug","key":"stream2width","value":"1920"},{"section":"debug","key":"stream2height","value":"1080"},{"section":"debug","key":"video0","value":"0"},{"section":"selfinfo","key":"stream3width","value":"1280"},{"section":"selfinfo","key":"stream3height","value":"720"},{"section":"debug","key":"video1","value":"0"}]
     */

    private int restart;
    private List<ItemBean> item;

    public int getRestart() {
        return restart;
    }

    public void setRestart(int restart) {
        this.restart = restart;
    }

    public List<ItemBean> getItem() {
        return item;
    }

    public void setItem(List<ItemBean> item) {
        this.item = item;
    }

    public static class ItemBean {
        /**
         * section : areaaddr
         * key : area0ip
         * value : 10.248.6.118
         */

        private String section;
        private String key;
        private String value;

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
