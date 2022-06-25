package com.alice.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
public class DownloadM3u8 {

    @Autowired
    CollectionUrl collectionUrl;

    @Autowired
    CreateHtml createHtml;


    @Value("${qfdj.deleteNdays}")
    private String deleteNdays;
    @Value("${qfdj.static_file_path}")
    private String static_file_path;
    private static final String PATH_SEPARATOR = File.separator;
    private static final String DATE = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
    private static final Logger log = LoggerFactory.getLogger(DownloadM3u8.class);
    private static final String RUNTIME_DIR = System.getProperty("user.dir");

    public void handle() {
        try {
            String userDir = static_file_path;
            if (userDir == null || "".equals(userDir.trim()))
                userDir = RUNTIME_DIR;


            log.info("当前文件存放路径：{}",userDir);
            List<String> musicList = new ArrayList<>();
            String dayPath = userDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + DATE;
            String dayPathList = userDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + DATE + PATH_SEPARATOR + DATE + ".list";
            File daylist = new File(dayPathList);
            if (daylist.exists() && daylist.length() > 0) {
                log.info("已存在当日文件：{}", dayPathList);
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
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dayPathList),"utf-8");
                writer.write(sbb.toString());
                writer.flush();
                writer.close();
            }
//            if(true)return;
            CountDownLatch countDownLatch =new CountDownLatch(musicList.size());
            if (musicList != null && musicList.size() > 0) {
                int musicSize = musicList.size();
                for (int i = 0; i < musicSize; i++) {
                    String finalStr = musicList.get(i);
                    //启动线程处理下载文件
                    String finalUserDir = userDir;
//                    System.out.println("finalUserDir = " + finalUserDir);
                    new Thread(() -> {
                        new DownloadM3u8().dowloadM3u8(finalStr, finalUserDir);
                        countDownLatch.countDown();
                    }).start();
                }
            } else {
                log.error("当前无音乐列表,请往查看当日是否已更新  https://www.vvvdj.com/sort/c1/ ");
            }

            log.info("等待线程下载合并完成后生成html页面.....");
            countDownLatch.await();



        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    private void dowloadM3u8(String finalUrl, String baseDir) {
        try {
            //url = "https://tspc.vvvdj.com/c1/2021/12/224514-b9caed/224514.m3u8?upt=a88e0ee91643471999&news";
            if (finalUrl == null) return;
            String url = finalUrl.split("##")[0];
            String musicName = finalUrl.split("##")[1];
            log.debug("当前进程的工作空间:" + baseDir);
            String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
            String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("?"));
            String id = fileName.substring(0, fileName.lastIndexOf("."));
            String sourceDir = baseDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + DATE;
            String downloadDir = sourceDir + PATH_SEPARATOR + id;
            String downloadFile = downloadDir + PATH_SEPARATOR + fileName;
            String outfileDir = baseDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR + DATE;
            String outfile = outfileDir + PATH_SEPARATOR + id + "_" + musicName + ".aac";
            if (new File(outfile).exists()) {
                log.debug(outfile + "   exists!");
                return;
            }


            File dir = new File(downloadDir);
            if (!dir.exists()) dir.mkdirs();

            dir = new File(outfileDir);
            if (!dir.exists()) dir.mkdirs();


            File m3u8 = new File(downloadFile);
            if (!m3u8.exists())
                downloadFile(url, downloadFile);

            //存储ts文件
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
                if (!file.exists() || file.length() <= 0)
                    downloadFile(tmpurl, tempfilepath);
                filetxtList.add(tempfilepath);
            }
            br.close();


            //保存ts列表id.txt文件名
            String filetxt = downloadDir + PATH_SEPARATOR + id + ".txt";
            File ftxt = new File(filetxt);
            if (!ftxt.exists() || ftxt.length() <= 0) {
                BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(ftxt),"utf-8"));
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
                String ffmpegPath = RUNTIME_DIR + PATH_SEPARATOR + "ffmpeg.exe ";
                if (!new File(ffmpegPath).exists()) {
                    ffmpegPath = baseDir + PATH_SEPARATOR + "ffmpeg.exe";
                    if (!new File(ffmpegPath).exists()) {
                        log.error("Not Fund ffmpeg.exe file ");
                        return;
                    }
                }
                String cmd = ffmpegPath + " -f concat -safe 0 -i " + filetxt + " -acodec copy " + outfile;
                log.debug("cmd = " + cmd);
                ProcessBuilder pb = new ProcessBuilder().command("cmd.exe", "/c", cmd).inheritIO();
                pb.start().waitFor();
            } else {
                String ffmpegPath = RUNTIME_DIR + PATH_SEPARATOR + "ffmpeg ";
                if (!new File(ffmpegPath).exists()) {
                    ffmpegPath = baseDir + PATH_SEPARATOR + "ffmpeg";
                    if (!new File(ffmpegPath).exists()) {
                        log.error("Not Fund ffmpeg file ");
                        return;
                    }
                }
                String cmd = ffmpegPath + " -f concat -safe 0 -i " + filetxt + " -acodec copy " + outfile;
                log.debug("cmd = " + cmd);
                ProcessBuilder pb = new ProcessBuilder().command("sh", "-c", cmd).inheritIO();
                //pb.redirectErrorStream(true);//这里是把控制台中的红字变成了黑字，用通常的方法其实获取不到，控制台的结果是pb.start()方法内部输出            的。
                //pb.redirectOutput(tmpFile);//把执行结果输出。
                pb.start().waitFor();//等待语句执行完成，否则可能会读不到结果。
            }
            log.debug("执行合成完成");
            if (new File(outfile).exists()) {
                log.debug(outfile + " 保存成功！");

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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void downloadFile(String url, String downloadFile) {
        OutputStream os = null;
        InputStream is = null;
        try {
            URL u = new URL(url);
            os = new FileOutputStream(downloadFile);
            is = u.openStream();

            int len = 0;
            byte[] b = new byte[1024];
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
            log.debug(url + "  download success!");
        } catch (IOException e) {
            e.printStackTrace();
            log.debug(url + "  download field!");
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void deleteNdays() {
        String userDir = static_file_path;
        if (userDir == null || "".equals(userDir.trim())) {
            userDir = RUNTIME_DIR;
        }
        if (deleteNdays == null || "".equals(deleteNdays.trim())) {
            deleteNdays = "7";
        }
        log.info("尝试删除 {} 天前的文件 !",deleteNdays);

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,-Integer.parseInt(deleteNdays));
        Date time = instance.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        String staticFilePath = userDir + PATH_SEPARATOR + "Music";
        File f = new File(staticFilePath);
//        System.out.println(staticFilePath);

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

        log.info("删除结束！");
    }

    private void delFiles(String path) {
        File ff = new File(path);
        File[] files = ff.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists()&&files[i].isFile())
                files[i].delete();
            else delFiles(files[i].getAbsolutePath());
        }
        log.info("删除文件夹：{}",path);
        ff.delete();
    }

    public static void main(String[] args) {
        new DownloadM3u8().deleteNdays();
    }
}
