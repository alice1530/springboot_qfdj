package com.alice.config;

import com.alice.handler.CollectionUrl;
import com.alice.handler.CreateHtml;
import com.alice.handler.DownloadM3u8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CommonBean {
    @Autowired
    protected DownloadM3u8 downloadM3u8;

    @Autowired
    protected CreateHtml createHtml;

    @Autowired
    protected CollectionUrl collectionUrl;

    @Autowired
    protected Environment environment;

    @Value("${qfdj.static_file_path}")
    protected String static_file_path;

    @Value("${qfdj.deleteNdays}")
    protected String deleteNdays;

    protected Logger log = LoggerFactory.getLogger(this.getClass());


}
