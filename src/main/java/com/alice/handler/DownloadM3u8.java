package com.alice.handler;

import com.alice.config.CommonBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DownloadM3u8 extends CommonBean {


    private static final String PATH_SEPARATOR = File.separator;
    private static final String RUNTIME_DIR = System.getProperty("user.dir");
    private Map<String, Integer> errorDownloadUrl = new ConcurrentHashMap<>();
    private static boolean dLocalListFirst;
    private static Integer dConnectTimedOut = 10;
    private static Integer dRetryTimes=10;

    public void handle() {
        try {
            //获取当前时间
            String DATE = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
            String userDir = staticFilePath;
            if (userDir == null || "".equals(userDir.trim()))
                userDir = RUNTIME_DIR;

            if(!StringUtils.isEmpty(connectTimedOut)){
                dConnectTimedOut = Integer.parseInt(connectTimedOut);
            }

            if(!StringUtils.isEmpty(retryTimes))
                dRetryTimes =Integer.valueOf(retryTimes);

            log.info("当前文件存放路径：{}", userDir);
            List<String> musicList = new ArrayList<>();
            String dayPath = userDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + DATE;
            String dayPathList = userDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + DATE + PATH_SEPARATOR + DATE + ".list";
            File daylist = new File(dayPathList);

            dLocalListFirst = localListFirst;
            log.info("是否优先从本地列表文件获取下载链接：{}", dLocalListFirst);
            if (dLocalListFirst&&daylist.exists() && daylist.length() > 0) {
                log.info("已存在当日列表文件：{}", dayPathList);
                //从本地获取列表文件
                try {
                    BufferedReader br = new BufferedReader(new FileReader(dayPathList));
                    String len = null;
                    while ((len = br.readLine()) != null) {
                        musicList.add(len);
                    }
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                //从网络上获取音乐链接和名称
                musicList = collectionUrl.collectonUrl();
                //写入本地列表
                StringBuilder sbb = new StringBuilder();
                for (int i = 0; i < musicList.size(); i++) {
                    sbb.append(musicList.get(i));
                    sbb.append(System.lineSeparator());
                }
                //生成当日文件夹
                new File(dayPath).mkdirs();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dayPathList), "utf-8");
                writer.write(sbb.toString());
                writer.flush();
                writer.close();
            }


//            if(true)return;

            Integer threadNumber = 5;
            if (!StringUtils.isEmpty(downloadThreadsNumber)) {
                int num = Integer.parseInt(downloadThreadsNumber);
                threadNumber = num>0&&num<20? num:20;
            }

            //使用线程池，10个线程一组
            ExecutorService pool = Executors.newFixedThreadPool(threadNumber);
            log.info("开启{}个线程进行处理....",threadNumber);
            int musicSize = musicList.size();
            AtomicInteger count = new AtomicInteger(musicSize);
            AtomicInteger success = new AtomicInteger(0);
            if (musicList != null && musicList.size() > 0) {
                log.info("等待线程下载合并完成后生成html页面.....");
                for (int i = 0; i < musicSize; i++) {
                    String finalStr = musicList.get(i);
                    //启动线程处理下载文件
                    //添加到线程池
                    pool.submit(() -> {
                        int current = count.getAndDecrement();
                        log.info("共{}条链接，剩余{}条待处理", musicSize, current);
                        String downloadM3u8 = new DownloadM3u8().downloadM3u8(finalStr);
                        if (downloadM3u8!=null) success.incrementAndGet();
                    });
                }
            } else {
                log.error("当前无音乐列表,请往查看当日是否已更新  https://www.vvvdj.com/sort/c1/ ");
            }
            pool.shutdown();
            while (!pool.isTerminated()) ;
            log.info("处理结束！处理链接[{}]条,处理成功[{}]条!",musicList.size(),success.get());


        } catch (Exception e) {
            e.printStackTrace();
            log.error("异常:{}",e.getMessage());
        }
    }

    public String downloadM3u8(String finalUrl) {
        return downloadM3u8(null, finalUrl);

    }

    public String downloadM3u8(String currentDir, String finalUrl) {
        try {
            //url = "https://tspc.vvvdj.com/c1/2021/12/224514-b9caed/224514.m3u8?upt=a88e0ee91643471999&news";
            if (finalUrl == null) return null;
            String userDir = staticFilePath;
            if (userDir == null || "".equals(userDir.trim()))
                userDir = RUNTIME_DIR;
            String baseDir = userDir;
            if (currentDir == null) {
                currentDir = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
            }
            String url = finalUrl.split("##")[0];
            String musicName = finalUrl.split("##")[1].replaceAll("\\s+","");
            log.debug("当前进程的工作空间:" + baseDir);
            String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
            String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
            String id = fileName.substring(0, fileName.lastIndexOf("."));
            String sourceDir = baseDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + currentDir;
            String downloadDir = sourceDir + PATH_SEPARATOR + id;
            String downloadFile = downloadDir + PATH_SEPARATOR + fileName;
            String outfileDir = baseDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + currentDir;
            String outfile = outfileDir + PATH_SEPARATOR + id + "_" + musicName + ".aac";
            String downloadUrl = PATH_SEPARATOR + currentDir + PATH_SEPARATOR + id + "_" + musicName + ".aac";

            //判断文件是否存在
            File outf = new File(outfile);
            if (dLocalListFirst&&outf.exists()&&outf.length()>0) {
                log.debug(outfile + "   exists!");
                return downloadUrl;
            }


            File dir = new File(downloadDir);
            if (!dir.exists()) dir.mkdirs();

            dir = new File(outfileDir);
            if (!dir.exists()) dir.mkdirs();

            //下载m3u8
            File m3u8 = new File(downloadFile);
            if (!dLocalListFirst||!m3u8.exists()||m3u8.length()<=0)
                downloadFile(url, downloadFile);

            //如果有失败，尝试重新下载
            boolean retryDownloadm3u8 = retryDownload();
            log.debug("m3u8 retryDownload = {}",retryDownloadm3u8);
            //下载m3u8失败，退出不合并文件
            if (!retryDownloadm3u8) {
                return null;
            }

            //下载存储ts文件
            List<String> filetxtList = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(m3u8));
            String len = null;
            while ((len = br.readLine()) != null) {
                if (len != null && len.startsWith("#")) continue;
                String tmpurl = baseUrl + len;
                String filename = len.substring(0, len.lastIndexOf("?"));
                String tempfilepath = downloadDir + PATH_SEPARATOR + filename;
                //log.debug("tmpurl = "+filename+":"+ new File(tempfilepath).length());
                File file = new File(tempfilepath);
                if (!file.exists() || file.length() <= 0){
                    //下载
                    downloadFile(tmpurl, tempfilepath);
                }
                filetxtList.add(tempfilepath);
            }
            br.close();

            //如果有失败，尝试重新下载
            boolean retryDownloadts = retryDownload();
            log.debug("ts retryDownload = {}",retryDownloadts);
            //下载ts失败，不合并文件
            if (!retryDownloadts)
                return null;



            //保存ts列表id.txt文件名
            String filetxt = downloadDir + PATH_SEPARATOR + id + ".txt";
            File ftxt = new File(filetxt);
            if (!ftxt.exists() || ftxt.length() <= 0) {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ftxt), "utf-8"));
                int size = filetxtList.size();
                for (int i = 0; i < size; i++) {
                    bw.write("file '" + filetxtList.get(i) + "'");
                    bw.newLine();
                }
                bw.close();
                log.debug(filetxt + " 保存成功！");
            }


            //ts合成

            boolean isWin = System.getProperties().getProperty("os.name").toLowerCase().contains("win");
            if (isWin) {
                String ffmpegPath = RUNTIME_DIR + PATH_SEPARATOR + "ffmpeg.exe";
                if (!new File(ffmpegPath).exists()) {
                    ffmpegPath = baseDir + PATH_SEPARATOR +"Music"+PATH_SEPARATOR+ "ffmpeg.exe";
                    if (!new File(ffmpegPath).exists()) {
                        log.error("Not Fund ffmpeg.exe file ");
                        log.error("找不到ffmpeg.exe文件，使用简单stream流合成，效果不如ffmpeg合成");
                        simpleMrege(outfile, filetxtList);
                        return downloadUrl;
                    }
                }
                String cmd = ffmpegPath + " -y  -loglevel quiet -f concat -safe 0 -i \"" + filetxt + "\" -acodec copy \"" + outfile+"\"";
                log.debug("合成命令: " + cmd);
                ProcessBuilder pb = new ProcessBuilder().command("cmd.exe", "/c", cmd).inheritIO();
                pb.start().waitFor();
            } else {
                String ffmpegPath = RUNTIME_DIR + PATH_SEPARATOR + "ffmpeg";
                if (!new File(ffmpegPath).exists()) {
                    ffmpegPath = baseDir + PATH_SEPARATOR +"Music"+PATH_SEPARATOR+ "ffmpeg";
                    if (!new File(ffmpegPath).exists()) {
                        log.error("Not Fund ffmpeg file ");
                        log.error("找不到ffmpeg文件，使用简单stream流合成，效果不如ffmpeg合成");
                        simpleMrege(outfile, filetxtList);
                        return downloadUrl;
                    }
                }
                String cmd = ffmpegPath + " -y  -loglevel quiet -f concat -safe 0 -i \"" + filetxt + "\" -acodec copy \"" + outfile+"\"";
                log.debug("合成命令: " + cmd);
                ProcessBuilder pb = new ProcessBuilder().command("sh", "-c", cmd).inheritIO();
                //pb.redirectErrorStream(true);//这里是把控制台中的红字变成了黑字，用通常的方法其实获取不到，控制台的结果是pb.start()方法内部输出            的。
                //pb.redirectOutput(tmpFile);//把执行结果输出。
                pb.start().waitFor();//等待语句执行完成，否则可能会读不到结果。
            }
            log.debug("执行合成完成");
            if (new File(outfile).exists()) {
                log.debug(outfile + " 保存成功！");
                return downloadUrl;

			/*
			File oldfile=new File(outfile);
			File newfile = new File(sourceDir+ PATH_SEPARATOR + id + ".aac");
			oldfile.renameTo(newfile);
			*/
                //删除ts文件

           /* File[] tsList = new File(downloadDir).listFiles(o -> o.isFile()&& o.getName().endsWith(".ts"));
            for (File f:tsList){
                f.delete();
            }
			log.debug("删除ts文件完成");
           */
            } else {

                log.error(outfile + " 保存失败，请检查！");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return null;

    }

    private boolean retryDownload() {
        //尝试重新下载失败的请求
        while (!errorDownloadUrl.isEmpty()) {
            Iterator<Map.Entry<String, Integer>> iterator = errorDownloadUrl.entrySet().iterator();
            if (iterator.hasNext()){
                Map.Entry<String, Integer> next = iterator.next();
                String[] tmp = next.getKey().split("##");
                String tmpurl = tmp[0];
                String tempfilepath = tmp[1];
                Integer value = next.getValue();
                if (value <= dRetryTimes) {
                    log.error("第{}次尝试下载:{}", value, tmpurl);
                    if(downloadFile(tmpurl,tempfilepath)){
                        //下载成功，移除列表
                        iterator.remove();
                        return retryDownload();
                    }
                } else {
                    //超过5次直接丢弃
                    log.error("超过{}次尝试，直接丢弃:{}条。{}", value, errorDownloadUrl.size(),tmpurl);
                    iterator.remove();
                    return false;
                }
            }
        }
        return true;
    }

    private void simpleMrege(String outfile, List<String> filetxtList) throws IOException {
        //ts合成，简单的流合并
        FileOutputStream out = new FileOutputStream(outfile);
        int fcount = filetxtList.size();
        for (int i = 0; i < fcount; i++) {
            FileInputStream in = new FileInputStream(filetxtList.get(i));
            int lens = 0;
            byte[] b = new byte[1024];
            while ((lens = in.read(b)) != -1) {
                out.write(b, 0, lens);
            }
            in.close();
        }
        out.close();
    }

    private boolean downloadFile(String url, String downloadFile) {

        OutputStream os = null;
        InputStream is = null;
        try {
//            Thread.sleep(3000);
            URL u = new URL(url);
            os = new FileOutputStream(downloadFile);
//            is = u.openStream();
            URLConnection connection = u.openConnection();
            connection.setConnectTimeout(dConnectTimedOut*1000);
            connection.setReadTimeout(dConnectTimedOut*1000);
            is = connection.getInputStream();

            int len = 0;
            byte[] b = new byte[1024];
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
            log.debug(url + "  download success!");
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            //记录错误url和路径,尝试重新请求
            String key =url+"##"+downloadFile;
            Integer count = errorDownloadUrl.get(key);
            if (count != null) {
                errorDownloadUrl.put(key, count+ 1);
            } else {
                errorDownloadUrl.put(key, 1);
            }
            log.error(url + "  download field! :{}",e.getMessage());
        } finally {
            try {
                if (os!=null)
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is!=null)
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;

    }


    public void deleteNdays() {
        String userDir = staticFilePath;
        if (userDir == null || "".equals(userDir.trim())) {
            userDir = RUNTIME_DIR;
        }

        String dDeleteNDays = deleteNDays;
        if (dDeleteNDays == null || "".equals(dDeleteNDays.trim())) {
            dDeleteNDays = "7";
        }
        log.info("尝试删除 {} 天前的文件 !", dDeleteNDays);

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, -Integer.parseInt(dDeleteNDays));
        Date time = instance.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        String dStaticFilePath = userDir + PATH_SEPARATOR + "Music";
        File f = new File(dStaticFilePath);
        File[] files = f.listFiles();
        for (File o : files) {
            if (o.isDirectory() && o.getName().startsWith("20")) {
                String name = o.getName();
                try {
                    Date fdate = format.parse(name);
                    if (fdate.before(time)) {
                        delFiles(o.getAbsolutePath());
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }
        //删除搜索目录
        String searchdownload = dStaticFilePath + PATH_SEPARATOR + "searchdownload";
        delFiles(searchdownload);
        log.info("删除结束！");
    }

    private void delFiles(String path) {
        File ff = new File(path);
        if (!ff.exists()) return;
        File[] files = ff.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists() && files[i].isFile())
                files[i].delete();
            else delFiles(files[i].getAbsolutePath());
        }
        log.info("删除文件夹：{}", path);
        ff.delete();
    }


    public String findFileById(String id) {
        String userDir = staticFilePath;
        if (userDir == null || "".equals(userDir.trim()))
            userDir = RUNTIME_DIR;
        File ff = new File(userDir + PATH_SEPARATOR + "Music");
        if (!ff.exists()) return null;
        File[] files = ff.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists() && files[i].isDirectory() && files[i].getName().startsWith("20")) {
                File f = files[i].getAbsoluteFile();
                String[] list = f.list();
                for (int j = 0; j < list.length; j++) {
                    if (list[j].startsWith(id) && list[j].endsWith("aac"))
                        return PATH_SEPARATOR + f.getName() + PATH_SEPARATOR + list[j];
                }
            }

        }
        return null;
    }

}
