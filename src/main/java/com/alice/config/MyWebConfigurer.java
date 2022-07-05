package com.alice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;


@Configuration
public class MyWebConfigurer extends CommonBean implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (static_file_path == null || "".equals(static_file_path.trim()))
            static_file_path = System.getProperty("user.dir");

//        System.out.println(static_file_path);
        //指定静态资源位置
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + static_file_path + File.separator + "Music" + File.separator);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //重定向到index.html页面
        registry.addViewController("/").setViewName("redirect:index.html");
    }
}