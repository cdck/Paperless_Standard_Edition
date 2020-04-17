package xlk.paperless.standard.data;

/**
 * @author xlk
 * @date 2020/3/9
 * @Description:
 */
public class EventMessage {
    private int type;
    private int method;
    private Object[] objs;

    public EventMessage(Builder builder) {
        this.type = builder.type;
        this.method = builder.method;
        this.objs = builder.objs;
    }

    public int getType() {
        return type;
    }

    public int getMethod() {
        return method;
    }

    public Object[] getObjs() {
        return objs;
    }

    public static class Builder {
        private int type;
        private int method;
        private Object[] objs;

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder method(int method) {
            this.method = method;
            return this;
        }

        public Builder objs(Object... objs) {
            this.objs = objs;
            return this;
        }

        public EventMessage build() {
            return new EventMessage(this);
        }
    }
}
