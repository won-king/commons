package com.wonking.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by wangke18 on 2018/11/14.
 */
public class ServiceKiller {
    public static void main(String[] args) {
        Socket socket=null;
        try {
            socket=new Socket("127.0.0.1", Server.PORT);
            OutputStream os=socket.getOutputStream();
            os.write(Server.ARG_STOP.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
