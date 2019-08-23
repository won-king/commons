package com.wonking.actor;

import com.wonking.utils.thread.ThreadUtil;

/**
 * Created by wangke on 2019-08-23 16:22
 */
public class ActorApp {
    public static void main(String[] args) {
        Numeric numeric=new Numeric();
        ThreadUtil util = ThreadUtil.getFixedExecutor(12);
        for(int i=0;i<3;++i){
            util.submitTask(new AddTask(numeric));
        }
        for(int i=0;i<3;++i){
            util.submitTask(new SubtractTask(numeric));
        }
        for(int i=0;i<3;++i){
            util.submitTask(new AddATask(numeric, 5));
        }
        for(int i=0;i<3;++i){
            util.submitTask(new SubtractATask(numeric, 5));
        }
        ThreadUtil.sleep(2);
        System.out.println(numeric.getNumber());
        util.shutdown();
        numeric.interrupt();
    }

    private static class AddTask implements Runnable{
        private Numeric numeric;

        public AddTask(Numeric numeric) {
            this.numeric = numeric;
        }

        @Override
        public void run() {
            int count=10000;
            while (--count > 0){
                numeric.add();
            }
        }
    }

    private static class AddATask implements Runnable{
        private Numeric numeric;
        private int i;

        public AddATask(Numeric numeric, int i) {
            this.numeric = numeric;
            this.i=i;
        }

        @Override
        public void run() {
            int count=10000;
            while (--count > 0){
                numeric.add(i);
            }
        }
    }

    private static class SubtractTask implements Runnable{
        private Numeric numeric;

        public SubtractTask(Numeric numeric) {
            this.numeric = numeric;
        }

        @Override
        public void run() {
            int count=10000;
            while (--count > 0){
                numeric.subtract();
            }
        }
    }

    private static class SubtractATask implements Runnable{
        private Numeric numeric;
        private int i;

        public SubtractATask(Numeric numeric, int i) {
            this.numeric = numeric;
            this.i=i;
        }

        @Override
        public void run() {
            int count=10000;
            while (--count > 0){
                numeric.subtract(i);
            }
        }
    }
}
