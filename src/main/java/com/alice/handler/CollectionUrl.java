package com.alice.handler;

import com.alice.config.CommonBean;
import com.eclipsesource.v8.V8;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CollectionUrl extends CommonBean {


    private String date = new SimpleDateFormat("yyyy/MM/dd").format(System.currentTimeMillis());


    /**
     * 搜集连接 [url##name,url##name]
     *
     * @return
     */
    public List<String> collectonUrl() {
        ArrayList<String> musicUrlAndName = new ArrayList<>();
        try {
            //
            List<String> musicIds = getMusicId();
            int listSize = musicIds.size();
            for (int i = 0; i < listSize; i++) {
                //延时1秒
                Thread.sleep(1000);
                String musicId = musicIds.get(i);
                String url = "https://www.vvvdj.com/play/" + musicId + ".html";
                String html = Jsoup.connect(url).get().html();


                /*new FileWriter("item_body.txt").write(html);
                BufferedReader bbr = new BufferedReader(new FileReader("item_body.txt"));
                StringBuilder ssb = new StringBuilder();
                String len = null;
                while ((len = bbr.readLine()) != null) {
                    ssb.append(len);
                    ssb.append("\r\n");
                }
                bbr.close();
                String itemBody = ssb.toString();
                System.out.println(itemBody);*/


                //获取加密字符串
                String a = "";
                String b = "";
                String regCode = "playurl=x\\.O000O0OO0O0OO\\('(.*)','(.*)'\\);";//定义正则表达式
                Pattern patten = Pattern.compile(regCode);//编译正则表达式
                Matcher matcher = patten.matcher(html);// 指定要匹配的字符串
                if (matcher.find()) {
                    a = matcher.group(1);
                    b = matcher.group(2);
                } else {
                    log.error("ID:{},获取加密连接失败", musicId);
                    return null;
                }

                //获取歌曲名称
                String musicName = "";
                regCode = "<h1>(.*)<\\/h1>";//定义正则表达式
                patten = Pattern.compile(regCode);//编译正则表达式
                matcher = patten.matcher(html);// 指定要匹配的字符串
                if (matcher.find()) {
                    musicName = matcher.group(1);
                } else {
                    log.error("ID:{},获取歌曲名称失败", musicId);
                    return null;
                }

                //j2v8调用js解码
                V8 runtime = V8.createV8Runtime();
//                String decodeJsPath=CollectionUrl.class.getResource("qfUrlDecode.js").getPath();
                InputStream inputStream= this.getClass().getResource("qfUrlDecode.js").openStream();
                BufferedInputStream br = new BufferedInputStream(inputStream);

                byte[] by = new byte[1024];
                int len=0;
                StringBuilder sb = new StringBuilder();
                while ((len=br.read(by))!=-1) {
                    sb.append(new String(by,0,len,"utf-8"));
                }
                String everything = sb.toString();

                runtime.executeScript(everything);
                String playurl = (String) runtime.executeJSFunction("getDownloadUrl", a, b);
                runtime.release();
                musicUrlAndName.add("https:" + playurl + "##" + musicName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取连接异常：{}", e.getMessage());
        }

        log.info("音乐链接：{}",musicUrlAndName);
        return musicUrlAndName;
    }

    /**
     * 搜集ID  [id,id,id]
     *
     * @return
     * @throws IOException
     */
    private List<String> getMusicId() throws IOException {
        String url = "https://www.vvvdj.com/sort/c1/";
        log.info("每日最新来源地址：{}",url);
        ArrayList<String> listIds = new ArrayList<>();
        Connection connect = Jsoup.connect(url);
        Document document = connect.get();


        /*new FileWriter("document_body.txt").write(document.html());
        BufferedReader bbr = new BufferedReader(new FileReader("document_body.txt"));
        String len = null;
        StringBuilder ssb = new StringBuilder();
        while ((len = bbr.readLine()) != null) {
            ssb.append(len);
        }
        bbr.close();
        Document document = Jsoup.parse(ssb.toString());*/


        Element table = document.getElementsByClass("list_musiclist").get(0);
        Elements trs = table.getElementsByTag("tr");
        for (int i = 1; i < trs.size(); i++) {
            Elements tds = trs.get(i).getElementsByTag("td");
            String id = tds.get(1).text();
            String datetime = tds.get(4).text();
            if (date.equals(datetime)) {
                listIds.add(id);
            }
        }
        log.info("音乐id:{}",listIds);
        return listIds;
    }

/*
    public static void main(String[] args) {
        List<String> list = new CollectionUrl().collectonUrl();
        System.out.println("list = " + list);

    }*/


}
