package com.wonking.actor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

/**
 * Created by wangke on 2019-08-23 15:02
 * 一个简易版的actor模型，总结一下实现一个actor模型需要解决哪些问题
 * 1.消息存储问题
 * 2.消息与响应代码映射问题(事实上还包括执行代码的表示和传递问题)
 * 3.参数传递问题。与2合并起来，可以借鉴golang的MPG模型，相当于是要做一个Processor，它解决的就是，保存、切换执行上下文的问题
 * 4.线程调度的问题。这里说的线程，不是简单的用户线程，而是绿色线程。
 *   这里还牵涉到线程模型的实现方式。线程模型是指，用户线程如何映射到内核线程，一对一，一对多，多对多
 *   为什么需要考虑线程问题，我们这个例子可能还看不出来。如果一个消息的某段执行代码存在阻塞，线程调度的必要性就体现出来了
 */
public class Numeric extends Thread{
    private int number=0;

    private BlockingQueue<Message> messages=new LinkedBlockingDeque<>();

    private Function<Numeric,Object> add=numeric -> numeric.number++;
    private Function<Numeric,Object> subtract=numeric -> numeric.number--;
    private Function<Integer,Object> addA=integer -> number+=integer;
    private Function<Integer,Object> subtractA=integer -> number-=integer;

    //function里面只允许传递一个参数确实不方便，这就是为什么scala里面定义了两个，三个，四个参数的函数接口
    //但是也只能到此为止了，参数再多下去，就要考虑把它们封装到一个对象里面了，因为任谁看了一个这么多参数的函数都会产生厌恶
    //想想如果所有函数都只能接受一个参数的话，那么当参数的数量不是那么多(事实上，统计规律显示，全世界几乎80%的方法参数个数都集中在2-5个)，
    // 我还是不得不把它们封装到一个对象里去，这同样会让人头疼不已

    public Numeric(){
        this.start();
    }

    public int getNumber() {
        return number;
    }

    public void add(){
        messages.add(new Message(this, add));
    }

    public void subtract(){
        messages.add(new Message(this, subtract));
    }

    public void add(int i){
        messages.add(new Message(i, addA));
    }

    public void subtract(int i){
        messages.add(new Message(i, subtractA));
    }

    @Override
    public void run() {
        while (true){
            Message message=messages.poll();
            if(message!=null){
                message.execute();
            }else {
                //这里不能用Thread.interrupted()，因为调这个方法会清空中断信号，导致
                if(this.isInterrupted()){
                    System.out.println("message is null and interrupted");
                    break;
                }
            }
        }
    }


    private static class Message{
        private Object param;
        private Function function;

        public Message(Object params, Function function) {
            this.param = params;
            this.function = function;
        }

        public Object getParam() {
            return param;
        }

        public Function getFunction() {
            return function;
        }

        public Object execute(){
            return function.apply(param);
        }
    }
}
