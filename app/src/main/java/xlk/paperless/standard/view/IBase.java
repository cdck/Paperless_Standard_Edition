package xlk.paperless.standard.view;

import xlk.paperless.standard.data.EventMessage;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public interface IBase {
    void register();
    void unregister();
    void BusEvent(EventMessage msg);
}
