package com.alice.config;

import com.alice.handler.CreateHtml;
import com.alice.handler.DownloadM3u8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

@Configuration
@EnableScheduling
public class MyScheduleTask extends CommonBean {






    @Scheduled(cron = "${qfdj.task_cron}")
    private void run(){
        try {
	    log.info("定时任务开始......");
            downloadM3u8.handle();
            //删除前几天的文件
            downloadM3u8.deleteNdays();
            //生成Html页面
            createHtml.createHtml();
	    log.info("定时任务结束!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@PostConstruct启动时执行一次
    @PostConstruct
    private void startRunOnce(){
        new Thread(()->{
            run();
        }).start();
    }

}
