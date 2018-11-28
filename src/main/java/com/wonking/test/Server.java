package com.wonking.test;

import com.wonking.utils.thread.ThreadUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by wangke18 on 2018/11/14.
 * 这里有个问题，就是必须得有一个命令监听器时刻监听socket传来的命令
 * 以便根据命令来通知线程池关闭所有任务
 * 线程池关掉所有任务的标识就是，所有运行中的任务全部执行完毕
 * 所以这里就会产生一个死循环，如果监听器任务运行在线程池里
 * 那么监听器要关闭线程池中的所有任务，就必须使所有任务都执行完毕
 * 就会产生这样一个悖论，一个任务必须等所有任务都执行完毕，他才算执行完毕，所有任务也包括他自己
 * 更形象一点就是，一个任务等着自己被杀死之后，才开始杀死自己
 *
 * 经过昨天跟他们的一番讨论，我有了更优雅的解决思路
 * 就是将这个任务独立出来，并且放到守护线程中。这样随着其他线程的结束，
 * 守护线程不就是干这样一件事的吗
 */
public class Server implements Runnable{
    public static final String ARG_STOP="stop";
    public static final String ARG_START="start";

    public static final int SYSTEM_STATUS_STOPPED=0;
    public static final int SYSTEM_STATUS_STARTED=1;
    public static final int SYSTEM_STATUS_STOPPING=2;
    public static final int SYSTEM_STATUS_STARTING=3;

    public static final int PORT=8081;
    private static volatile int status=SYSTEM_STATUS_STOPPED;
    private ServerSocketChannel server;

    private ThreadUtil threadUtil;

    public Server(ThreadUtil threadUtil){
        try {
            server=ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(PORT));
            server.socket().setReuseAddress(true);
            server.configureBlocking(false);
            this.threadUtil=threadUtil;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true){
            String command=null;
            try {
                SocketChannel socket=server.accept();
                if(socket!=null && socket.isConnected()){
                    command=read(socket);
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
            if(ARG_STOP.equals(command)){
                System.out.println("clean all running tasks");
                status=SYSTEM_STATUS_STOPPING;
                threadUtil.shutdownNow();
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private String read(SocketChannel socket){
        ByteBuffer bf=ByteBuffer.allocate(20);
        String command=null;
        try {
            int readSize=socket.read(bf);
            if (readSize>0){
                bf.flip();
                byte[] bytes=new byte[bf.remaining()];
                bf.get(bytes);
                command=new String(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return command;
    }
}
