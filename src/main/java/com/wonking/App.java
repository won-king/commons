package com.wonking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * long may spring!
 *
 */
@Component
public class App {

    @Value("${url}")
    private String url;

    public String getUrl(){
        return this.url;
    }

    public static void main( String[] args ) {
        List<Integer> numbers=new ArrayList<>();
        for(int i=0;i<50;++i){
            numbers.add(i);
        }
        int sum= numbers.stream().reduce(0,(t,s)->t+s);
        Optional<Integer> o=numbers.stream().reduce(Integer::sum);
        int sum1=o.isPresent()?o.get():0;
        System.out.println("sum->"+sum);
        System.out.println("sum1->"+sum1);
    }

    private static void testSpring(){
        ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("classpath:spring-context.xml");
        App app= (App) context.getBean("app");
        System.out.println("url is "+app.getUrl());
        File file=new File("abc.txt");
        FileOutputStream fos=null;
        BufferedOutputStream bos=null;
        try {
            if(!file.createNewFile()){
                System.out.println("create file fail");
                return;
            }
            fos=new FileOutputStream(file);
            bos=new BufferedOutputStream(fos);
            for(int i=0;i<100;++i){
                String uuid=UUID.randomUUID().toString().replaceAll("-","")+"\n";
                bos.write(uuid.getBytes());
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(bos!=null){
                    bos.close();
                }
                if(fos!=null){
                    fos.close();
                }
            }catch (IOException e){
                //close silently
            }

        }
        System.out.println("write successfully");
    }
}
