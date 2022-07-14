package com.alice.handler;

import com.alice.config.CommonBean;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CollectionUrl extends CommonBean {

    private HashMap<String, Integer> errorIds = new HashMap<>();

    private AtomicInteger count = new AtomicInteger(0);

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
            log.info("获取下载列表信息...");
            int listSize = musicIds.size();
            for (int i = 0; i < listSize; i++) {
                String musicId = musicIds.get(i);
                String result = getUrlAndName(musicId);
                if (result == null) continue;
                log.debug("id:{}===>{}",musicId,result);
                musicUrlAndName.add(result);
            }

            int retryTimes=5;
            if(!StringUtils.isEmpty(retry_times))
                retryTimes =Integer.valueOf(retry_times);
            //尝试处理队列其他失败的链接
            while (!errorIds.isEmpty()) {
                Iterator<Map.Entry<String, Integer>> iterator = errorIds.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, Integer> next = iterator.next();
                    String key = next.getKey();
                    Integer value = next.getValue();
                    if (value <= retryTimes) {
                        log.error("第{}次尝试获取id={}的链接",value, key);
                        String andName = getUrlAndName(key);
                        if (andName == null) continue;
                        //获取到后从队列移除
                        iterator.remove();
                        musicUrlAndName.add(andName);
                        log.debug("id:{}===>{}",key,andName);
                    } else {
                        //超过5次直接丢弃
                        iterator.remove();
                        log.error("超过{}次尝试，直接丢弃id={}的链接请求", value, key);
                    }
                }
            }
            log.info("获取下载列表信息结束！");
            log.info("最终获取id数量:{} ===> 链接数量:{}",musicIds.size(),musicUrlAndName.size());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取链接连接异常：{}", e.getMessage());
        }
        return musicUrlAndName;
    }

    public String getUrlAndName(String musicId) {

        try {
            //延时1秒
            Thread.sleep(1000);
            //测试
            String url = "https://www.vvvdj.com/play/" + musicId + ".html";
            String html = Jsoup.connect(url).get().html();

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
            /*
            V8 runtime = V8.createV8Runtime();
            InputStream inputStream = this.getClass().getResource("qfUrlDecode.js").openStream();
            BufferedInputStream br = new BufferedInputStream(inputStream);

            byte[] by = new byte[1024];
            int len = 0;
            StringBuilder sb = new StringBuilder();
            while ((len = br.read(by)) != -1) {
                sb.append(new String(by, 0, len, "utf-8"));
            }
            String everything = sb.toString();

            runtime.executeScript(everything);
            String playurl = (String) runtime.executeJSFunction("getDownloadUrl", a, b);
            runtime.release();
            */

            //调用解码
            String playurl = decodeUrl(a,b);

            String result = "https:" + playurl + "##" + musicName;
            return result;
        } catch (Exception e) {
            //记录错误id,尝试重新请求
            log.error("获取音乐id={}链接异常:{}", musicId, e.getMessage());
            errorIds.merge(musicId, 1, (a, b) -> a + b);
        }
        return null;

    }

    /**
     * 搜集ID  [id,id,id]
     *
     * @return
     * @throws IOException
     */
    private List<String> getMusicId() {
        ArrayList<String> listIds = new ArrayList<>();;
        try {
            String url = "https://www.vvvdj.com/sort/c1/";
            log.info("获取最新id列表信息...");
            log.info("每日最新来源地址：{}", url);
//            Thread.sleep(5000);
            Connection connect = Jsoup.connect(url);
            Document document = connect.get();

            //获取当天日期
            String DATE = new SimpleDateFormat("yyyy/MM/dd").format(System.currentTimeMillis());
            Element table = document.getElementsByClass("list_musiclist").get(0);
            Elements trs = table.getElementsByTag("tr");
            for (int i = 1; i < trs.size(); i++) {
                Elements tds = trs.get(i).getElementsByTag("td");
                String id = tds.get(1).text();
                String datetime = tds.get(4).text();
                if (DATE.equals(datetime)) {
                    listIds.add(id);
                }
            }
        } catch (Exception e) {
            int retryTimes=5;
            if(!StringUtils.isEmpty(retry_times))
                retryTimes =Integer.valueOf(retry_times);
            if(count.getAndIncrement()<=retryTimes) {
                log.error("尝试第{}次请求",count.get());
                return getMusicId();
            }
        }
        log.info("音乐id:{}", listIds);
        return listIds;
    }


    /**
     * url 解码
     * @param a
     * @param b
     * @return
     */
    public String decodeUrl(String a, String b) {
        int k,l,n,o,p;
        int d = b.length();
        int e = d;

        char[] f = new char[d + 1];
        char[] g = new char[d + 1];

        for (l = 1; d >= l; l++) {
            f[l] = b.charAt(l-1);
            g[e] = f[l];
            e -= 1;
        }

        String m = a.substring(0,2);
        String i = a.substring(2);
        String h = "";
        String j;
        for (l = 0; l < i.length(); l += 4) {
            j = i.substring(l,l+4);
            if (!"".equals(j)) {
                b = j.substring(0,1);
            }
            k = (Integer.parseInt(j.substring(1)) - 100) / 3;
            n = 2 * Integer.valueOf(b.charAt(0));
            o = "e0".equals(m) ? Integer.valueOf(f[k]) : Integer.valueOf(g[k]);
            p = n - o;
            h += String.valueOf((char) p);
        }
        return h;
    }

}
