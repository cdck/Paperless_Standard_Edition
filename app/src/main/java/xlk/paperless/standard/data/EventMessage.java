package xlk.paperless.standard.data;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc EventBus消息结构体
 */
public class EventMessage {
    private int type;
    private int method;
    private Object object;
    private Object[] objects;

    private EventMessage(Builder builder) {
        this.type = builder.type;
        this.method = builder.method;
        this.object = builder.object;
        this.objects = builder.objects;
    }

    public int getType() {
        return type;
    }

    public int getMethod() {
        return method;
    }

    public Object getObject() {
        return object;
    }

    public Object[] getObjects() {
        return objects;
    }

    public static class Builder {
        private int type;
        private int method;
        private Object object;
        private Object[] objects;

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder method(int method) {
            this.method = method;
            return this;
        }

        public Builder object(Object object) {
            this.object = object;
            return this;
        }

        public Builder objects(Object... objects) {
            this.objects = objects;
            return this;
        }

        public EventMessage build() {
            return new EventMessage(this);
        }
    }
}
