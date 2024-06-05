package client;

import grpc.HelloGrpc;
import grpc.HelloMessage;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 采用最简单的逻辑模拟复现 Cancel
 * exec pool
 * schedule pool 定时执行cancel
 */
public class ClientDemo {
    // Specify the number of concurrent threads
    private static final int NUM_THREADS = 10;
    //使用main方法来测试client端
    public static void main(String[] args) throws Exception {

//        ClientDemo clientDemo = new ClientDemo();
//
//        try {
//
//            //基于gRPC远程调用对应的方法
//            clientDemo.remoteCall("【zhongyuan】");
//
//        } finally {
//
//        }

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        Random random = new Random();
            for (int i = 0; i < NUM_THREADS; i++) {
                int finalI = i;
                Future<Void> future = (Future<Void>) executorService.submit(() -> {
                    try {
                        ClientDemo clientDemo = new ClientDemo();
                        clientDemo.remoteCall("【JackyGraph】"+ finalI);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });


                if (random.nextInt(NUM_THREADS) == 0) {
                    try {
                        Thread.sleep(100); // Random delay in milliseconds
                        future.cancel(true);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    /**
     * 基于gRPC框架的使用步骤，进行远程调用
     * @param name
     */
    public void remoteCall(String name) {

        HelloMessage.HelloRequest request = HelloMessage.HelloRequest.newBuilder().setName(name).build();
        HelloMessage.HelloResponse response;

        try {

            // 基于访问地址 创建通道
            Channel channel =  ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

            // 利用通道 创建一个桩（Stub）对象
            HelloGrpc.HelloBlockingStub blockingStub = HelloGrpc.newBlockingStub(channel);


            if (Thread.currentThread().isInterrupted()) {
                throw new StatusRuntimeException(Status.CANCELLED.withDescription("Thread interrupted"));
            }


            //通过桩对象来调用远程方法
            response = blockingStub.sayHello(request);

        } catch (StatusRuntimeException e) {
            return;
        }

        System.out.println("client端远程调用sayHello()的结果为：\n\n" + response.getMessage());
    }

}
