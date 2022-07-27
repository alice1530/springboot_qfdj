package com.alice.handler;

import com.alice.config.CommonBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

@Component
public class CreateHtml extends CommonBean {

    private static final String PATH_SEPARATOR = File.separator;
    private static final String RUNTIME_DIR = System.getProperty("user.dir");

    @Value("${qfdj.task_cron}")
    protected String taskCron;

    /**
     * 生成首页html
     */
    public void createHtml() {

        String userDir = static_file_path;
        if (userDir == null || "".equals(userDir.trim()))
            userDir = RUNTIME_DIR;

        String aday = deleteNdays;
        if (aday == null || "".equals(aday.trim()))
            aday = "7";

        String dir = userDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR;
        log.info("生成Html页面到：{}", dir);
        //复制所需的js和css文件
        if (!new File(dir + "video.min.js").exists()) {
            try {
                InputStream i = this.getClass().getResource("video.min.js").openStream();
                FileOutputStream o = new FileOutputStream(dir + "video.min.js");
                FileCopyUtils.copy(i, o);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(dir + "video-js.min.css").exists()) {
            try {
                InputStream i = this.getClass().getResource("video-js.min.css").openStream();
                FileOutputStream o = new FileOutputStream(dir + "video-js.min.css");
                FileCopyUtils.copy(i, o);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (!new File(dir + "favicon.ico").exists()) {
            try {
                InputStream i = this.getClass().getResource("favicon.ico").openStream();
                FileOutputStream o = new FileOutputStream(dir + "favicon.ico");
                FileCopyUtils.copy(i, o);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        OutputStreamWriter fw = null;
        OutputStreamWriter wf = null;
        try {


            //从本地文件夹，实时更新全部列表
            File fdir = new File(dir);
            File[] files = fdir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File datedir = files[i];
                if (datedir.getName().startsWith("20") && datedir.isDirectory()) {
                    String datedirName = datedir.getName();
                    fw = new OutputStreamWriter(new FileOutputStream(dir + datedirName + "/list" + datedirName + ".html"), "utf-8");
                    //生成列表
                    StringBuilder sb = new StringBuilder();
                    boolean hasItem = false;
                    sb.append("<div class='sdate'>");
                    sb.append("<span>" + datedirName + "</span>");
                    sb.append("</div>");
                    sb.append("<ol>");
                    File f = new File(dir + datedirName);
                    String[] list = f.list();
                    Arrays.sort(list, Collections.reverseOrder());
                    for (int j = 0; j < list.length; j++)
                        if (list[j].endsWith(".aac")) {
                            hasItem = true;
                            sb.append("<li> ");
                            sb.append("<a id=\"" + list[j].split("_")[0] + "\">" + list[j] + "</a>");
                            sb.append("</li>\n");
                        }
                    sb.append("</ol>");
                    if (hasItem) {
                        fw.write(sb.toString());
                    }
                    fw.flush();
                    fw.close();
                }
            }

            //生成首页+列表
            File fm = new File(dir);
            File[] listm = fm.listFiles();
            StringBuilder frame = new StringBuilder();
            frame.append("<html>");
            frame.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
/*
            //原始css
            frame.append("<style type=\"text/css\">h1>a:active{background:#b4d7d9;box-shadow:none;}</style>");
            frame.append("<style type=\"text/css\">h1>a{padding:10px;border-radius:5px;margin-left:20px;margin-right:20px;box-shadow:0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #c8d0e7;}</style>");
            frame.append("<style type=\"text/css\">li{text-align-last:justify;border:solid 1px #d6fdff;margin:2px;width:580px}</style>");
            frame.append("<style type=\"text/css\">li:nth-child(odd){background-color:#ffc0cb3b;}</style>");
            frame.append("<style type=\"text/css\">li:nth-child(even){background-color:#aacaff3b;}</style>");
            frame.append("<style type=\"text/css\">li.current{border: solid 1px;font-size: initial;width:725px;text-align:center;background-color:greenyellow;");
            frame.append("animation-name:current_ant;animation-duration: 2s;animation-iteration-count: infinite;animation-direction: alternate;}</style>");
            frame.append("<style type=\"text/css\">@keyframes current_ant{from{transform:scale(1,1)} to{transform:scale(0.8,0.8)}}</style>");
            frame.append("<style type=\"text/css\">h1{cursor: pointer;} a{text-decoration:none;font-size:larger;color: blue;cursor: pointer;}</style>");
            frame.append("<style type=\"text/css\">ol{display: flex;flex-direction: column; align-items: center;padding: 10px;width: 80%;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}</style>");
            frame.append("<style type=\"text/css\">.sdate{margin-top: 40px;font-size:x-large;text-align:center;width:90%;height:30px;border-radius:0.3em;");
            frame.append("box-shadow:0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}</style>");
*/

            //压缩后的css,  http://www.esjson.com/cssformat.html
            frame.append("<style type=\"text/css\">");
            frame.append("h1>a:active {background: #b4d7d9;box-shadow: none;}h1>a {padding: 10px;border-radius: 5px;margin-left: 20px;margin-right: 20px;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #c8d0e7;}li {text-align-last: justify;border: solid 1px #d6fdff;margin: 2px;width: 580px }li:nth-child(odd) {background-color: #ffc0cb3b;}li:nth-child(even) {background-color: #aacaff3b;}li.current {border: solid 1px;font-size: initial;width: 725px;text-align: center;background-color: greenyellow;animation-name: current_ant;animation-duration: 2s;animation-iteration-count: infinite;animation-direction: alternate;}@keyframes current_ant {from {transform: scale(1,1) }to {transform: scale(0.8,0.8) }}h1 {cursor: pointer;}a {text-decoration: none;font-size: larger;color: blue;cursor: pointer;}ol {display: flex;flex-direction: column;align-items: center;padding: 10px;width: 80%;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}.sdate {margin-top: 40px;font-size: x-large;text-align: center;width: 90%;height: 30px;border-radius: 0.3em;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}");
            frame.append("</style>");

//            frame.append("<link rel=\"stylesheet\" href=\"video-js.min.css\">");
            frame.append("<script src=\"video.min.js\"></script>");
            frame.append("<title>在线试听</title>");
            frame.append("</head><body style='font-size:smaller;background-color:#e4ebf8;display: flex; flex-direction: column;align-items: center;'>");
            frame.append("<h1 onclick='dspl()' id=\"showName\" style = \"color:red;text-align: center;\">点击列表↓↓↓↓播放音乐</h1>");
            //头部隐藏块
            frame.append("<div id='showView' style='display: none; flex-direction: column; align-items: center; justify-content: flex-start;'>");
//            frame.append("<video id='my-player'  class='video-js vjs-big-play-centered'></video>");
            frame.append("<div><input id='range' type='range' value='1' min='0.1' max='2.5' step='0.1' onchange='changeV()'>");
            frame.append("<br>倍速:<label id='label' for='range'>1</label></div>");
            frame.append("<div><input type='number' id='searchInput' placeholder='请输入歌曲编号:'></input><button onclick='search()'>搜索</button></div>");
            frame.append("<span id='searchA'></span>");
            frame.append("<div><input id='timeInput' type='number' value='30' min='1' max='1200'>分钟");
            frame.append("<button onclick='timeOut()'>定时关闭</button></div>");
            frame.append("</div>");
            //进度条
            frame.append("<div id=\"myProgress\" onmousedown=\"progress()\" style=\"display:none;cursor:pointer;overflow:hidden;padding-right:5px;border-radius:8px;position:relative;margin:20px;text-align:right; height:20px;width: 60%;color: white;background-color: #4CAF50;\">");
            frame.append("<span id=\"bid\"></span>");
            frame.append("<div id=\"myBar\" style=\"cursor:pointer;position:absolute;left:0px;top:0px;float:left;width:1px;background-color:#ddd;height:100%;text-align:right;color: black;\"></div>");
            frame.append("</div>");
            //控制
            frame.append("<h1 id='control'  style='display:none;color:red;text-align: center;'>");
            frame.append("<a title='ctrl+←'  onclick='prevNext(-1)'>上一首</a>");
            frame.append("<a title='space'  onclick='continuePlay()'>在线试听</a>");
            frame.append("<a title='ctrl+→' onclick='prevNext(1)'>下一首</a></h1>");
            frame.append("<h1 id=\"showDownload\" style = \"color:red;text-align: center;\"></h1>");

            //加载列表
            Arrays.asList(listm).stream().filter(o -> o.isDirectory() && o.getName().startsWith("20")).map(o -> o.getName()).sorted(Comparator.reverseOrder()).forEach(dname -> {

                        try {
                            BufferedReader br = new BufferedReader(new FileReader(dir + dname + "/list" + dname + ".html"));
                            String len = null;
                            while ((len = br.readLine()) != null) {
                                frame.append(len);
                            }
                            br.close();
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }


                    }
            );

            //显示底部文字
            String bottomText = "<div>每日13点10分更新，仅保留近" + aday + "天的内容</div>";
            if (!StringUtils.isEmpty(taskCron)) {
                String[] split = taskCron.split(" +");
                bottomText = "<div>每日" + split[2] + "点" + split[1] + "分" + split[0] + "秒开始更新，仅保留近" + aday + "天的内容</div>";
            }
            //判断自由文本
            if (!StringUtils.isEmpty(freedomText))
                frame.append("<div>" + freedomText + "</div>");
            else
                frame.append(bottomText);


/*

            //原始js
            frame.append(" <script type=\"text/javascript\">");
            //初始参数
            frame.append("let isDspl = true; let rate = 1; let MY_INTERVAL = null;");
            frame.append("let MY_PROGRESS = document.getElementById('myProgress');");
            frame.append("let MY_BAR = document.getElementById('myBar');");
            frame.append("let SHOW_VIEW = document.getElementById('showView');");
            frame.append("let SHOW_NAME = document.getElementById('showName');");
            frame.append("let SHOW_DOWNLOAD = document.getElementById('showDownload');");
            frame.append("let SEARCH_A = document.getElementById('searchA');");
            frame.append("let SEARCH_INPUT = document.getElementById('searchInput');");
            frame.append("let BID = document.getElementById('bid');");
            frame.append("let CONTROL = document.getElementById('control');");
            frame.append("let BTN = CONTROL.children[1];");
            frame.append("let MY_TIMEOUT=null;");
            frame.append("let TIME_INPUT = document.getElementById('timeInput');");

            //加载完成绑定事件
            frame.append("window.onload = function() {");
            frame.append("let list = document.querySelectorAll('a[id]');");
            frame.append("list.forEach(item=>{item.addEventListener('click', play);});");
            frame.append("let iframe = document.createElement('iframe');");
            frame.append("iframe.setAttribute('style','display:none');");
            frame.append("let v = document.createElement('video');");
            frame.append("v.setAttribute('id','my-player');");
            frame.append("iframe.appendChild(v);");
            frame.append("document.body.appendChild(iframe);");

            //页面加载完成，video js 事件
            frame.append("player = videojs('my-player', {");
            frame.append("poster: 'favicon.ico',");
            frame.append("autoplay: true,");
            frame.append("controls: true,");
            frame.append("preload: 'auto'");
            frame.append("}, function() {");
            frame.append("this.on('ended', ()=>{prevNext(1);});");

                    frame.append("this.on('waiting', ()=>{");
                    frame.append("if (document.querySelector('.current'))");
                    frame.append("document.querySelector('.current').style.animationPlayState = 'paused';");
                    frame.append("BTN.innerText = '加载中';");
                    frame.append("});");

                    frame.append("this.on('error', ()=>{");
                    frame.append("if (MY_INTERVAL) {this.clearInterval(MY_INTERVAL);MY_INTERVAL = null;}");
                    frame.append("MY_INTERVAL = this.setInterval(()=>{");
                    frame.append("this.src([{src: this.currentSrc(),type: 'application/x-mpegURL'}]);");
                    frame.append("this.currentTime(this.currentTime());");
                    frame.append("}, 10000);");
                    frame.append("});");

                    frame.append("this.on('loadedmetadata', ()=>{");
                    frame.append("player.playbackRate(rate);");
                    frame.append("if (MY_INTERVAL) {this.clearInterval(MY_INTERVAL);MY_INTERVAL = null;}");
                    frame.append("});");

                    frame.append("this.on('timeupdate', ()=>{");
                    frame.append("let current = this.currentTime();");
                    frame.append("let total = this.duration();");
                    frame.append("let percent = (current / total * 100).toFixed(2);");
                    frame.append("let textTime = getTime(current);");
                    frame.append("MY_BAR.style.width = percent + '%';");
                    frame.append("MY_BAR.innerText = textTime;");
                    frame.append("BID.innerText = '-' + getTime(this.remainingTime());");
                    frame.append("});");

                    frame.append("this.on('playing', ()=>{");
                    frame.append("if (document.querySelector('.current'))");
                    frame.append("document.querySelector('.current').style.animationPlayState = 'running';");
                    frame.append("BTN.innerText = '播放中';");
                    frame.append("});");

                    frame.append("this.on('pause', ()=>{");
                    frame.append("if (document.querySelector('.current'))");
                    frame.append("document.querySelector('.current').style.animationPlayState = 'paused';");
                    frame.append("BTN.innerText = '已暂停';");
                    frame.append("});");
                    frame.append("});");
                    frame.append("};");

            //倍速
            frame.append("function changeV() {");
            frame.append("rate = document.getElementById('range').value;");
            frame.append("document.getElementById('label').innerText = rate;");
            frame.append("player.playbackRate(rate);");
            frame.append("};");


            //定时关闭
            frame.append("function timeOut() {");
            frame.append("let val= TIME_INPUT.value;");
            frame.append("if(val&&val>0&&val<1200){");
            frame.append("MY_TIMEOUT && (window.clearTimeout(MY_TIMEOUT),MY_TIMEOUT = null);");
            frame.append("MY_TIMEOUT = window.setTimeout(()=>{");
            frame.append("player.dispose();");
            frame.append("document.body.innerHTML='定时结束,已销毁,请刷新！';");
            frame.append("},val*60*1000);");
            frame.append("TIME_INPUT.parentElement.innerHTML='<span style=\\'color:red\\'>定时关闭已开启['+new Date().toLocaleString('zh',{huor12:false})+']<br>将在'+val+'分钟后自动关闭(取消请刷新页面)</span>';");
            frame.append("};");
            frame.append("};");

            //播放
            frame.append("function play() {");
            frame.append("let aa = event.target;");
            frame.append("let id = aa.id;");
            frame.append("let date = aa.parentElement.parentElement.previousElementSibling.innerText;");
            frame.append("let name = aa.textContent;");
            frame.append("let currentLi=document.querySelector('.current');");
            frame.append("if (currentLi){currentLi.removeAttribute('class');currentLi.removeAttribute('style');}");
            frame.append("document.getElementById(id).parentElement.setAttribute('class', 'current');");
            frame.append("SHOW_NAME.innerHTML = name;");
            frame.append("SHOW_DOWNLOAD.innerHTML = '<a target=\\'_blank\\' href=\\'' + date + '/' + name + '\\'>↓下载↓</a>';");
            frame.append("player.src([{src: date + '/' + id + '/' + id + '.m3u8',type: 'application/x-mpegURL'}]);");
            frame.append("SHOW_NAME.scrollIntoView({behavior: 'smooth'});");
            frame.append("CONTROL.style.setProperty('display', 'block');");
            frame.append("MY_PROGRESS.style.setProperty('display', 'block');");
            frame.append("SEARCH_A.innerText = '';");
            frame.append("};");

            //显示
            frame.append("function dspl() {");
            frame.append("if (isDspl = !isDspl)");
            frame.append("SHOW_VIEW.style.setProperty('display', 'none');");
            frame.append("else ");
            frame.append("SHOW_VIEW.style.setProperty('display', 'flex');");
            frame.append("};");

            //进度条
            frame.append("function progress() {");
            frame.append("let obj = MY_PROGRESS;");
            frame.append("let width = obj.offsetWidth;");
            frame.append("let objX = getOffsetLeft(obj);");
            frame.append("let mouseX = event.clientX + document.body.scrollLeft;");
            frame.append("let objXtmp = mouseX - objX;");
            frame.append("let percent = (objXtmp / width * 100).toFixed(2);");
            frame.append("player.currentTime(player.duration() * percent / 100);");
            frame.append("};");

            //获取坐标
            frame.append("function getOffsetLeft(obj) {");
            frame.append("let tmp = obj.offsetLeft;");
            frame.append("let val = obj.offsetParent;");
            frame.append("while (val != null) {");
            frame.append("tmp += val.offsetLeft;");
            frame.append("val = val.offsetParent;");
            frame.append("}");
            frame.append("return tmp;");
            frame.append("};");

            //时间
            frame.append("function getTime(val) {");
            frame.append("let h = Math.floor(val / 3600);");
            frame.append("let m = Math.floor((val % 3600) / 60);");
            frame.append("let s = Math.floor((val) % 60);");
            frame.append("let text = (h > 9 ? h : '0' + h) + ':' + (m > 9 ? m : '0' + m) + ':' + (s > 9 ? s : '0' + s);");
            frame.append("return text;");
            frame.append("};");

            //搜索
            frame.append("function search() {");
            frame.append("let inputId = SEARCH_INPUT.value.trim();");
            frame.append("if (!inputId)return;");
            frame.append("SEARCH_A.innerHTML = '<span style=\\'color:red\\'>处理中...</span>';");
            frame.append("let xhr = new XMLHttpRequest();");
            frame.append("xhr.open('post', '/qfdj/' + inputId, true);");
            frame.append("xhr.onload = function() {");
            frame.append("let data = xhr.responseText;");
            frame.append("if (xhr.status == 200) {");
            frame.append("if (data) {");
            frame.append("let prefix = data.substring(1, data.lastIndexOf('/'));");
            frame.append("let suffix = data.substring(data.lastIndexOf('/') + 1);");
            frame.append("let bb = document.createElement('a');");
            frame.append("player.src([{src: prefix + '/' + inputId + '/' + inputId + '.m3u8',type: 'application/x-mpegURL'}]);");
            frame.append("bb.href = data;");
            frame.append("bb.target = '_blank';");
            frame.append("bb.innerText = suffix;");
            frame.append("SHOW_NAME.innerHTML = suffix;");
            frame.append("SEARCH_A.innerText = '';");
            frame.append("SEARCH_A.appendChild(bb);");
            frame.append("let currentLi=document.querySelector('.current');");
            frame.append("if (currentLi){currentLi.removeAttribute('class');currentLi.removeAttribute('style');}");
            //frame.append("if (document.querySelector('.current') != null)document.querySelector('.current').setAttribute('class', 'item');");
            frame.append("} else {");
            frame.append("SEARCH_A.innerHTML = '<span style=\\'color:red\\'>非法编号: ' + inputId + '</span>';");
            frame.append("}");
            frame.append("} else {");
            frame.append("console.log(xhr.status, xhr.responseText, xhr.getAllResponseHeaders());");
            frame.append("SEARCH_A.innerHTML = '<span style=\\'color:red\\'>未知错误!' + xhr.status + '</span>';");
            frame.append("};SEARCH_INPUT.value = '';");
            frame.append("};");
            frame.append("xhr.timeout = 1000 * 60 * 3;");
            frame.append("xhr.ontimeout = function() {");
            frame.append("SEARCH_A.innerHTML = '<span style=\\'color:red\\'>请求超时,请稍后再试!</span>';");
            frame.append("};");
            frame.append("xhr.send(null);");
            frame.append("};");

            //下一首
            frame.append("function prevNext(num) {");
            frame.append("let list = document.querySelectorAll('a[id]');");
            frame.append("let nextIndex = 0;");
            frame.append("let str = player.currentSrc();");
            frame.append("if(str){");
            frame.append("let id = str.substr(str.lastIndexOf('/') + 1, str.length - str.lastIndexOf('.') + 1);");
            frame.append("list.forEach((item,index,arr)=>{");
            frame.append("if (item.id == id)");
            frame.append("nextIndex = index + num;");
            frame.append("});}");
            frame.append("nextIndex = nextIndex < 0 ? list.length - 1 : nextIndex;");
            frame.append("nextIndex = nextIndex > list.length - 1 ? 0 : nextIndex;");
            frame.append("list[nextIndex].click();");
            frame.append("};");

            //继续播放
            frame.append("function continuePlay() {");
            frame.append("if (player.paused()) {");
            frame.append("player.src([{src: player.currentSrc(),type: 'application/x-mpegURL'}]);");
            frame.append("player.currentTime(player.currentTime());");
            frame.append("} else {");
            frame.append("player.pause();");
            frame.append("if (MY_INTERVAL) {clearInterval(MY_INTERVAL);MY_INTERVAL = null;}");
            frame.append("};");
            frame.append("};");

            //键盘事件
            frame.append("document.addEventListener('keydown', e=>{");
            frame.append("if (e.ctrlKey && e.keyCode === 37)");
            frame.append("prevNext(-1);");
            frame.append("else if (e.ctrlKey && e.keyCode === 39)");
            frame.append("prevNext(1);");
            frame.append("else if (e.ctrlKey && e.keyCode === 38) {");
            frame.append("let tvol = player.volume() + 0.1;");
            frame.append("player.volume(tvol > 1 ? 1 : tvol)");
            frame.append("} else if (e.ctrlKey && e.keyCode === 40) {");
            frame.append("let tvol = player.volume() - 0.1;");
            frame.append("player.volume(tvol < 0 ? 0 : tvol)");
            frame.append("} else if (e.keyCode === 32)");
            frame.append("player.currentSrc()?continuePlay():prevNext();");
            frame.append("});");
            frame.append("</script>");

*/
            //压缩后的js,  https://www.qianbo.com.cn/Tool/Beautify/Js-Compress.html
            frame.append("<script type=\"text/javascript\">");
            frame.append("let isDspl=!0,rate=1,MY_INTERVAL=null,MY_PROGRESS=document.getElementById(\"myProgress\"),MY_BAR=document.getElementById(\"myBar\"),SHOW_VIEW=document.getElementById(\"showView\"),SHOW_NAME=document.getElementById(\"showName\"),SHOW_DOWNLOAD=document.getElementById(\"showDownload\"),SEARCH_A=document.getElementById(\"searchA\"),SEARCH_INPUT=document.getElementById(\"searchInput\"),BID=document.getElementById(\"bid\"),CONTROL=document.getElementById(\"control\"),BTN=CONTROL.children[1],MY_TIMEOUT=null,TIME_INPUT=document.getElementById(\"timeInput\");function changeV(){rate=document.getElementById(\"range\").value,document.getElementById(\"label\").innerText=rate,player.playbackRate(rate)}function timeOut(){let e=TIME_INPUT.value;e&&e>0&&e<1200&&(MY_TIMEOUT&&(window.clearTimeout(MY_TIMEOUT),MY_TIMEOUT=null),MY_TIMEOUT=window.setTimeout(()=>{player.dispose(),document.body.innerHTML=\"定时结束,已销毁,请刷新！\"},60*e*1e3),TIME_INPUT.parentElement.innerHTML=\"<span style='color:red'>定时关闭已开启[\"+(new Date).toLocaleString(\"zh\",{huor12:!1})+\"]<br>将在\"+e+\"分钟后自动关闭(取消请刷新页面)</span>\")}function play(){let e=event.target,t=e.id,n=e.parentElement.parentElement.previousElementSibling.innerText,r=e.textContent,l=document.querySelector(\".current\");l&&(l.removeAttribute(\"class\"),l.removeAttribute(\"style\")),document.getElementById(t).parentElement.setAttribute(\"class\",\"current\"),SHOW_NAME.innerHTML=r,SHOW_DOWNLOAD.innerHTML=\"<a target='_blank' href='\"+n+\"/\"+r+\"'>↓下载↓</a>\",player.src([{src:n+\"/\"+t+\"/\"+t+\".m3u8\",type:\"application/x-mpegURL\"}]),SHOW_NAME.scrollIntoView({behavior:\"smooth\"}),CONTROL.style.setProperty(\"display\",\"block\"),MY_PROGRESS.style.setProperty(\"display\",\"block\"),SEARCH_A.innerText=\"\"}function dspl(){(isDspl=!isDspl)?SHOW_VIEW.style.setProperty(\"display\",\"none\"):SHOW_VIEW.style.setProperty(\"display\",\"flex\")}function progress(){let e=MY_PROGRESS,t=e.offsetWidth,n=getOffsetLeft(e),r=((event.clientX+document.body.scrollLeft-n)/t*100).toFixed(2);player.currentTime(player.duration()*r/100)}function getOffsetLeft(e){let t=e.offsetLeft,n=e.offsetParent;for(;null!=n;)t+=n.offsetLeft,n=n.offsetParent;return t}function getTime(e){let t=Math.floor(e/3600),n=Math.floor(e%3600/60),r=Math.floor(e%60);return(t>9?t:\"0\"+t)+\":\"+(n>9?n:\"0\"+n)+\":\"+(r>9?r:\"0\"+r)}function search(){let e=SEARCH_INPUT.value.trim();if(!e)return;SEARCH_A.innerHTML=\"<span style='color:red'>处理中...</span>\";let t=new XMLHttpRequest;t.open(\"post\",\"/qfdj/\"+e,!0),t.onload=function(){let n=t.responseText;if(200==t.status)if(n){let t=n.substring(1,n.lastIndexOf(\"/\")),r=n.substring(n.lastIndexOf(\"/\")+1),l=document.createElement(\"a\");player.src([{src:t+\"/\"+e+\"/\"+e+\".m3u8\",type:\"application/x-mpegURL\"}]),l.href=n,l.target=\"_blank\",l.innerText=r,SHOW_NAME.innerHTML=r,SEARCH_A.innerText=\"\",SEARCH_A.appendChild(l);let o=document.querySelector(\".current\");o&&(o.removeAttribute(\"class\"),o.removeAttribute(\"style\"))}else SEARCH_A.innerHTML=\"<span style='color:red'>非法编号: \"+e+\"</span>\";else console.log(t.status,t.responseText,t.getAllResponseHeaders()),SEARCH_A.innerHTML=\"<span style='color:red'>未知错误!\"+t.status+\"</span>\";SEARCH_INPUT.value=\"\"},t.timeout=18e4,t.ontimeout=function(){SEARCH_A.innerHTML=\"<span style='color:red'>请求超时,请稍后再试!</span>\"},t.send(null)}function prevNext(e){let t=document.querySelectorAll(\"a[id]\"),n=0,r=player.currentSrc();if(r){let l=r.substr(r.lastIndexOf(\"/\")+1,r.length-r.lastIndexOf(\".\")+1);t.forEach((t,r,o)=>{t.id==l&&(n=r+e)})}t[n=(n=n<0?t.length-1:n)>t.length-1?0:n].click()}function continuePlay(){player.paused()?(player.src([{src:player.currentSrc(),type:\"application/x-mpegURL\"}]),player.currentTime(player.currentTime())):(player.pause(),MY_INTERVAL&&(clearInterval(MY_INTERVAL),MY_INTERVAL=null))}window.onload=function(){document.querySelectorAll(\"a[id]\").forEach(e=>{e.addEventListener(\"click\",play)});let e=document.createElement(\"iframe\");e.setAttribute(\"style\",\"display:none\");let t=document.createElement(\"video\");t.setAttribute(\"id\",\"my-player\"),e.appendChild(t),document.body.appendChild(e),player=videojs(\"my-player\",{poster:\"favicon.ico\",autoplay:!0,controls:!0,preload:\"auto\"},function(){this.on(\"ended\",()=>{prevNext(1)}),this.on(\"waiting\",()=>{document.querySelector(\".current\")&&(document.querySelector(\".current\").style.animationPlayState=\"paused\"),BTN.innerText=\"加载中\"}),this.on(\"error\",()=>{MY_INTERVAL&&(this.clearInterval(MY_INTERVAL),MY_INTERVAL=null),MY_INTERVAL=this.setInterval(()=>{this.src([{src:this.currentSrc(),type:\"application/x-mpegURL\"}]),this.currentTime(this.currentTime())},1e4)}),this.on(\"loadedmetadata\",()=>{player.playbackRate(rate),MY_INTERVAL&&(this.clearInterval(MY_INTERVAL),MY_INTERVAL=null)}),this.on(\"timeupdate\",()=>{let e=this.currentTime(),t=(e/this.duration()*100).toFixed(2),n=getTime(e);MY_BAR.style.width=t+\"%\",MY_BAR.innerText=n,BID.innerText=\"-\"+getTime(this.remainingTime())}),this.on(\"playing\",()=>{document.querySelector(\".current\")&&(document.querySelector(\".current\").style.animationPlayState=\"running\"),BTN.innerText=\"播放中\"}),this.on(\"pause\",()=>{document.querySelector(\".current\")&&(document.querySelector(\".current\").style.animationPlayState=\"paused\"),BTN.innerText=\"已暂停\"})})},document.addEventListener(\"keydown\",e=>{if(e.ctrlKey&&37===e.keyCode)prevNext(-1);else if(e.ctrlKey&&39===e.keyCode)prevNext(1);else if(e.ctrlKey&&38===e.keyCode){let e=player.volume()+.1;player.volume(e>1?1:e)}else if(e.ctrlKey&&40===e.keyCode){let e=player.volume()-.1;player.volume(e<0?0:e)}else 32===e.keyCode&&(player.currentSrc()?continuePlay():prevNext())});");
            frame.append("</script>");

            frame.append("</body>");
            frame.append("</html>");
            //写入html
            wf = new OutputStreamWriter(new FileOutputStream(dir + "index.html"), "utf-8");
            wf.write(frame.toString());
            wf.flush();
            log.info("生成页面结束!");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("生成Html页面异常:", e.getMessage());
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (wf != null) wf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
