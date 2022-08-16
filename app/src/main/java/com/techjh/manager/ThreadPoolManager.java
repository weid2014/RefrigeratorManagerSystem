package com.techjh.manager;

/**
 * @author wade
 * @Description:(线程池管理类)
 * @date 2022/8/13 9:31
 */
public class ThreadPoolManager {
    /*private final String TAG = this.getClass().getSimpleName();
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2; // 核心线程数为 CPU数＊2
    private static final int MAXIMUM_POOL_SIZE = 64;    // 线程队列最大线程数
    private static final int KEEP_ALIVE_TIME = 1;    // 保持存活时间 1秒

    private final BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<>(128);

    private final ThreadFactory DEFAULT_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @NonNull
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, TAG + " #" + mCount.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    };

    @NonNull
    private ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, mWorkQueue, DEFAULT_THREAD_FACTORY,
            new ThreadPoolExecutor.DiscardOldestPolicy());

    @NonNull
    private static volatile ThreadPoolManager mInstance = new ThreadPoolManager();

    @NonNull
    public static ThreadPoolManager getInstance() {
        return mInstance;
    }

    public void addTask(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    @Deprecated
    public void shutdownNow() {
        mExecutor.shutdownNow();
    }*/
}
