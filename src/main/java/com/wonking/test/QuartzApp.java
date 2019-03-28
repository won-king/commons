package com.wonking.test;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * Created by wangke18 on 2018/11/28.
 */
public class QuartzApp {
    public static void main(String[] args) throws SchedulerException, IOException {
        JobDetail job=JobBuilder.newJob(FuzzyJob.class)
                .withIdentity("job1", "group1") //设置name/group
                .withDescription("this is a test job") //设置描述
                .usingJobData("age", 18) //加入属性到ageJobDataMap
                .build();

        job.getJobDataMap().put("name", "quertz"); //加入属性name到JobDataMap

        ScheduleBuilder sb=null;
        sb=simpleSchedule()
                .withIntervalInSeconds(10)
                .repeatForever();
        sb=CronScheduleBuilder.cronSchedule("0 0 0/12 * * ?");
        //定义一个每秒执行一次的SimpleTrigger
        Trigger trigger=TriggerBuilder.newTrigger()
                .startNow()
                .withIdentity("trigger1")
                .withSchedule(sb)
                .build();

        Scheduler sche= StdSchedulerFactory.getDefaultScheduler();
        sche.scheduleJob(job, trigger);

        sche.start();
        System.in.read();
        sche.shutdown();
    }

    public static class FuzzyJob implements Job{

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("hahahahaha");
        }
    }
}
