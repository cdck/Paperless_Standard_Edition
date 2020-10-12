package xlk.paperless.standard.helper;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Created by xlk on 2020/10/8.
 * @desc 自定义的线程池拒绝策略
 * 当要创建的线程数量大于线程池的最大线程数的时候，新的任务就会被拒绝，就会调用这个接口里的这个方法
 */
public class MyRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        //ThreadPoolExecutor.CallerRunsPolicy 的实现方式
        if (!executor.isShutdown()) {
            r.run();
        }
    }
}
