package com.alice.controller;

import com.alice.handler.CollectionUrl;
import com.alice.handler.DownloadM3u8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("qfdj")
public class BaseController {

    @Autowired
    public DownloadM3u8 downloadM3u8;
    @Autowired
    public CollectionUrl collectionUrl;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/{id}")
    @ResponseBody
    public String search(@PathVariable("id") String id){


        try {
            Integer.parseInt(id);
            if (id.length()<5||id.length()>6)return null;
        } catch (Exception e) {
            log.error("非法id:{}",e.getMessage());
            return null;
        }
        String downloadUrl = null;
        try {
            log.info("搜索下载id:{}",id);
            log.info("先从本地文件查找...");
            downloadUrl = downloadM3u8.findFileById(id);
            if (downloadUrl==null) {
                log.info("本地无缓存，从网络上下载...");
                String urlAndName = collectionUrl.getUrlAndName(id);
                if (urlAndName != null) {
                    downloadUrl = downloadM3u8.downloadM3u8("searchdownload", urlAndName);
                    if (downloadUrl != null) {
                        downloadUrl = downloadUrl.replace("\\", "/");
                        log.info("处理完成：{}", downloadUrl);
                    }
                    //System.out.println(downloadUrl);
                }else {
                    log.info("网络上无结果");
                }
            }else {
                log.info("本地找到文件:{}",downloadUrl);
                downloadUrl = downloadUrl.replace("\\", "/");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("解析错误：{}",e.getMessage());

        }
        return downloadUrl;
    }
}
