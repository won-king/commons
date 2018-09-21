package com.wonking.utils.thread.tokenlimiter;

/**
 * Created by wangke18 on 2018/9/20.
 */
public class TokenLimterApp {

    public static void main(String[] args) {
        TokenLimter limter=new TokenLimter();
        new Thread(limter).start();
        for(int i=0;i<10;++i){
            new Thread(new GetTimeTask(limter)).start();
        }

    }
}
