package com.alice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@SpringBootApplication
public class App {

    public static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @PostConstruct
    public void init() {
        File f = new File("application.yml");
        if (!f.exists()) {
            try {
                // 获取资源方式
                // 方法一
                // InputStream i = new ClassPathResource("application.yml").getInputStream();
                // 方法二
                // InputStream i = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.yml");
                // 方法三
                // InputStream i = this.getClass().getResourceAsStream("/application.yml");
                // 方法四
                // InputStream i = new FileInputStream(ResourceUtils.getFile("classpath:application.yml"));
                // 获取文件在当前class位置的路径
                // String path = this.getClass().getResource("application.yml").getPath();
                //方法五 获取当前jar同级目录文件
                //String userDir = System.getProperty("user.dir");
                //log.error("userDir = " + userDir);

                log.info("cp file application.yml to current running directory");
                // 复制配置文件到当前运行目录下
                InputStream i = this.getClass().getClassLoader().getResourceAsStream("application.yml");
                FileOutputStream o = new FileOutputStream(f);
                FileCopyUtils.copy(i, o);
                log.info("file applicaton.yml copy ok !");

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else {
            log.info("exists file application.yml");
        }

    }

}

