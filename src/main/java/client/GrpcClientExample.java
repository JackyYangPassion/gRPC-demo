package client;

import io.grpc.StatusRuntimeException;

public class GrpcClientExample {


    public static void main(String[] args) throws InterruptedException {
        // 假设我们有一个Greeter服务和相应的proto文件生成的代码


        // 创建一个线程来执行gRPC调用
        Thread thread = new Thread(() -> {
            try {
                // 执行gRPC调用
                ClientDemo clientDemo = new ClientDemo();
                clientDemo.remoteCall("【JackyGraph】");
            } catch (StatusRuntimeException e) {
                // 捕获并打印异常
                System.err.println("RPC failed: " + e.getStatus());
            }
        });

        // 启动线程
        thread.start();

        try {
            Thread.sleep(100); // 等待一段时间，让gRPC调用开始
            thread.interrupt(); // 中断线程，期望触发CANCELLED异常
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }


    }

}
