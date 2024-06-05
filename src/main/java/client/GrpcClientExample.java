package client;

import java.lang.ref.WeakReference;
import java.util.concurrent.*;


/**
 * Java 多线程模型
 * 一个线程池：   执行 RPC 调用
 * 一个守护线程： 执行记时，超时后调用 Cancel
 */
public class GrpcClientExample {



    public static void main(String[] args) {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);
        ExecutorService executorService = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS, queue,
                new ThreadPoolExecutor.AbortPolicy());

        while (true){
            final CompletableFuture<Object> evaluationFuture = new CompletableFuture<>();
            final FutureTask<Void> evalFuture = new FutureTask<>(() -> {
                try {
                    ClientDemo clientDemo = new ClientDemo();
                    clientDemo.remoteCall("【JackyGraph】");

                    evaluationFuture.complete("success");
                } catch (Throwable ex) {

                }

                return null;
            });

            long scriptEvalTimeOut = 1000;
            final WeakReference<CompletableFuture<Object>> evaluationFutureRef = new WeakReference<>(evaluationFuture);
            final Future<?> executionFuture =  executorService.submit(evalFuture);

            // Schedule a timeout in the thread pool for future execution
            final ScheduledFuture<?> sf = scheduledExecutorService.schedule(() -> {
                if (executionFuture.cancel(true)) {
                    final CompletableFuture<Object> ef = evaluationFutureRef.get();
                    if (ef != null) {
                        ef.completeExceptionally(new TimeoutException(
                                String.format("Evaluation exceeded the configured 'evaluationTimeout' threshold of %s ms or evaluation was otherwise cancelled directly for request [%s]", scriptEvalTimeOut, "time")));
                    }
                }
            }, scriptEvalTimeOut, TimeUnit.MILLISECONDS);

            // Cancel the scheduled timeout if the eval future is complete or the script evaluation failed with exception
            evaluationFuture.handleAsync((v, t) -> {
                if (!sf.isDone()) {
                    System.out.println("Killing scheduled timeout on script evaluation - {} - as the eval completed (possibly with exception)."+"script");
                    sf.cancel(true);
                }

                // no return is necessary - nothing downstream is concerned with what happens in here
                return null;
            }, scheduledExecutorService);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
