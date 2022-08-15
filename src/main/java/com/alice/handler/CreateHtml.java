package com.alice.handler;

import com.alice.config.CommonBean;
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

    /**
     * 生成首页html
     */
    public void createHtml() {

        String userDir = staticFilePath;
        if (userDir == null || "".equals(userDir.trim()))
            userDir = RUNTIME_DIR;

        String aday = deleteNDays;
        if (aday == null || "".equals(aday.trim()))
            aday = "7";

        String dir = userDir + PATH_SEPARATOR + "Music" + PATH_SEPARATOR;
        log.info("生成Html页面到：{}", dir);

        //复制所需的js和css文件
        String[] js = {"video.min.js","play.gif","favicon.ico"};
        for(String item :js){
            if (!new File(dir + item).exists()) {
                try {
                    InputStream i = this.getClass().getResource(item).openStream();
                    FileOutputStream o = new FileOutputStream(dir + item);
                    FileCopyUtils.copy(i, o);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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



                    //先从列表文件找，列表没有再从本地找
                    File flist = new File(dir + datedirName+PATH_SEPARATOR+datedirName+".list");
                    if (flist.exists()&&flist.length()>0){
                        BufferedReader br = new BufferedReader(new FileReader(flist));
                        String len = null;
                        while ((len = br.readLine()) != null) {
                            if (len.endsWith("##")){
                                String url = len.split("##")[0];
                                String name = len.split("##")[1];
                                String id = url.substring(url.lastIndexOf("/")+1, url.lastIndexOf(".m3u8"));
                                //判断列表文件和ts目录是否存在
                                File f = new File(dir + datedirName);
                                File[] dirnames = f.listFiles();
                                for (int j = 0; j < dirnames.length; j++) {
                                    if(dirnames[j].isDirectory()&&dirnames[j].getName().equals(id)){
                                        hasItem = true;
                                        sb.append("<li> ");
                                        sb.append("<a id=\"" + id + "\">" + id+"_"+name+ ".aac</a>");
                                        sb.append("</li>\n");
                                    }
                                }

                            }
                        }
                        br.close();
                    }

                    //本地列表没有，找本地目录
                    if (!hasItem) {
                        File f = new File(dir + datedirName);
                        log.info("从文件夹获取列表:{}",f.getAbsolutePath());
                        String[] list = f.list();
                        Arrays.sort(list, Collections.reverseOrder());
                        for (int j = 0; j < list.length; j++)
                            if (list[j].endsWith(".aac")) {
                                String id = list[j].split("_")[0];

                                //判断列表文件和ts目录是否存在
                                File[] dirnames = f.listFiles();
                                for (int k = 0; k < dirnames.length; k++) {
                                    if(dirnames[k].isDirectory()&&dirnames[k].getName().equals(id)){
                                        hasItem = true;
                                        sb.append("<li> ");
                                        sb.append("<a id=\"" + id + "\">" + list[k] + "</a>");
                                        sb.append("</li>\n");

                                        //获取本地文件后，生成list列表
                                        if(flist.exists()){
                                            //读取list文件
                                            BufferedReader br = new BufferedReader(new FileReader(flist));
                                            StringBuffer save =new StringBuffer();
                                            String len = null;
                                            while ((len = br.readLine()) != null) {
                                                if (len.contains(id)){
                                                    save.append(len +"##");
                                                }else {
                                                    save.append(len);
                                                }
                                                save.append(System.lineSeparator());
                                            }
                                            br.close();
                                            //保存list文件
                                            BufferedWriter bw =new BufferedWriter(new FileWriter(flist));
                                            bw.write(save.toString());
                                            bw.flush();
                                            bw.close();
                                        }

                                    }
                                }

                            }
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
            frame.append("<style type=\"text/css\">* {user-select: none;} h1>a:active{background:#b4d7d9;box-shadow:none;}</style>");
            frame.append("<style type=\"text/css\">h1>a{padding:10px;border-radius:5px;margin-left:20px;margin-right:20px;box-shadow:0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #c8d0e7;}</style>");
            frame.append("<style type=\"text/css\">li{text-align-last:justify;border:solid 1px #d6fdff;margin:2px;width:580px;white-space:nowrap;}</style>");
            frame.append("<style type=\"text/css\">li:nth-child(odd){background-color:#ffc0cb3b;}</style>");
            frame.append("<style type=\"text/css\">li:nth-child(even){background-color:#aacaff3b;}</style>");
            frame.append("<style type=\"text/css\">li.current{border: solid 1px;font-size: initial;width:725px;text-align:center;background-color:greenyellow;");
            frame.append("animation-name:current_ant;animation-duration: 2s;animation-iteration-count: infinite;animation-direction: alternate;}</style>");
            frame.append("<style type=\"text/css\">@keyframes current_ant{from{transform:scale(1,1)} to{transform:scale(0.8,0.8)}}</style>");
            frame.append("<style type=\"text/css\">h1{cursor: pointer;} a{text-decoration:none;font-size:larger;color: blue;cursor: pointer;}</style>");
            frame.append("<style type=\"text/css\">ol{display: flex;flex-direction: column; align-items: center;padding: 10px;width: 80%;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}</style>");
            frame.append("<style type=\"text/css\">.sdate{margin-top: 40px;font-size:x-large;text-align:center;width:90%;height:30px;border-radius:0.3em;");
            frame.append("box-shadow:0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}</style>");
            frame.append("<style type=\"text/css\">.mask{display:none;position:fixed;left:0px;top:0px;background:#00000088;width:100%;height:100%;z-index:98;}</style>");
            frame.append("<style type=\"text/css\">.popWindow{position:relative;display:flex;flex-direction:column;align-items:center;justify-content:center;background:#e4ebf8;font-size:x-large;text-align:center;width:50%;height:300px;border: solid 5px #d6fdff;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;border-radius:10px;margin:5% auto;z-index:99;}</style>");
            frame.append("<style type=\"text/css\">#header-right{position:absolute;width:30px;height:30px;border-radius:5px;background:red;color:#fff;right:5px;top:5px;cursor:pointer;text-align:center;}</style>");
            frame.append("<style type=\"text/css\">.locale {background:url(play.gif) no-repeat center;height:40px;width:40px;border:solid 5px #10ff00; border-radius:15px;display:flex;flex-direction:column;justify-content:center;align-items:center;}</style>");
*/
            //压缩后的css,  http://www.esjson.com/cssformat.html
            frame.append("<style type=\"text/css\">");
            frame.append("* {user-select: none;}h1>a:active {background: #b4d7d9;box-shadow: none;}h1>a {padding: 10px;border-radius: 5px;margin-left: 20px;margin-right: 20px;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #c8d0e7;}li {text-align-last: justify;border: solid 1px #d6fdff;margin: 2px;width: 580px;white-space: nowrap;}li:nth-child(odd) {background-color: #ffc0cb3b;}li:nth-child(even) {background-color: #aacaff3b;}li.current {border: solid 1px;font-size: initial;width: 725px;text-align: center;background-color: greenyellow;animation-name: current_ant;animation-duration: 2s;animation-iteration-count: infinite;animation-direction: alternate;}@keyframes current_ant {from {transform: scale(1,1) }to {transform: scale(0.8,0.8) }}h1 {cursor: pointer;}a {text-decoration: none;font-size: larger;color: blue;cursor: pointer;}ol {display: flex;flex-direction: column;align-items: center;padding: 10px;width: 80%;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}.sdate {margin-top: 40px;font-size: x-large;text-align: center;width: 90%;height: 30px;border-radius: 0.3em;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}.mask {display: none;position: fixed;left: 0px;top: 0px;background: #00000088;width: 100%;height: 100%;z-index: 98;}.popWindow {position: relative;display: flex;flex-direction: column;align-items: center;justify-content: center;background: #e4ebf8;font-size: x-large;text-align: center;width: 50%;height: 300px;border: solid 5px #d6fdff;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;border-radius: 10px;margin: 5% auto;z-index: 99;}#header-right {position: absolute;width: 30px;height: 30px;border-radius: 5px;background: red;color: #fff;right: 5px;top: 5px;cursor: pointer;text-align: center;}.locale {background: url(play.gif) no-repeat center;height: 40px;width: 40px;border: solid 5px #10ff00;border-radius: 15px;display: flex;flex-direction: column;justify-content: center;align-items: center;}");
            frame.append("</style>");

//            frame.append("<link rel=\"stylesheet\" href=\"video-js.min.css\">");
            frame.append("<script src=\"video.min.js\"></script>");
            frame.append("<title>在线试听</title>");
            frame.append("</head><body style='font-size:smaller;background-color:#e4ebf8;display: flex; flex-direction: column;align-items: center;'>");
            frame.append("<h1 onclick='dspl()' id=\"showName\" style = \"color:red;text-align: center;\">点击列表↓↓↓↓播放音乐</h1>");
            frame.append("<div id='collectDiv' style='display: none;'></div>");
            //头部隐藏块
            frame.append("<div id='showView' style='display: none; flex-direction: column; align-items: center; justify-content: flex-start;'>");
//            frame.append("<video id='my-player'  class='video-js vjs-big-play-centered'></video>");
            frame.append("<div><input id='range' type='range' value='1' min='0.1' max='2.5' step='0.1' onchange='changeV()'>");
            frame.append("<br>倍速:<label id='label' for='range'>1</label></div>");
            frame.append("<div><input type='number' id='searchInput' placeholder='请输入歌曲编号:'></input><button onclick='searchById()'>搜索</button></div>");
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
            //弹窗
            frame.append("<div class='mask' id='mask'>");
            frame.append("<div class='popWindow'>");
            frame.append("<span id='searchA'></span>");
            frame.append("<div id='header-right' onclick=\"document.getElementById('mask').style.display='none';document.body.style.position = 'relative';document.body.style.overflow = 'scroll';\">x</div>");
            frame.append("</div>");
            frame.append("</div>");
            frame.append("<div id='showCollection' style='display:none;border-radius: 0.3em; box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;padding:10px;margin-top: 20px;'></div>");


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

            frame.append("<div id='locale' style='display:none;bottom:10%;right:10%;overflow:hidden;position:fixed;z-index:97;'>");
            frame.append("<a title='正在播放' class='locale'></a>");
            frame.append("</div>");

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
            frame.append("let MASK = document.getElementById('mask');");
            frame.append("let LOCALE = document.getElementById('locale');");
            frame.append("let collectionList=[];");
            frame.append("let SHOW_COLLECTION = document.getElementById('showCollection');");
            frame.append("let COLLECT_DIV = document.getElementById('collectDiv');");

            //页面滚动定位事件
            frame.append("window.onscroll=function(){");
            frame.append("let cli=document.querySelector('.current');");
            frame.append("if(cli){");
            frame.append("let ct = cli.offsetTop;");
            frame.append("let st = document.body.scrollTop;");
            frame.append("let ch = cli.offsetHeight;");
            frame.append("let wh = document.body.clientHeight;");
            frame.append("if(Math.abs(st-ct)>wh||st>ct+ch){");
            frame.append("LOCALE.style.setProperty('display', 'block');");
            frame.append("}else{");
            frame.append("LOCALE.style.setProperty('display', 'none');");
            frame.append("}}};");

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
            frame.append("showCollection();");

            //页面加载完成，video js 事件
            frame.append("player = videojs('my-player', {");
            //frame.append("poster: 'favicon.ico',");
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


            // 清空收藏
            frame.append("function clean(id) {");
            frame.append("if(id){");
            frame.append("collectionList = collectionList.filter(item=>{return item.id!=id});");
            frame.append("if(collectionList.length>0){");
            frame.append("isCollection(document.querySelector('.current').children[0].id);");
            frame.append("localStorage.setItem('collectionList',JSON.stringify(collectionList));");
            frame.append("showCollection();");
            frame.append("return;}}");
            frame.append("localStorage.removeItem('collectionList');");
            frame.append("collectionList=[];");
            frame.append("SHOW_COLLECTION.innerHTML='';");
            frame.append("SHOW_COLLECTION.style.setProperty('display','none');");
            frame.append("};");

            //判断是否已收藏
            frame.append("function isCollection(id){");
            frame.append("let text = '<a onclick=\\'collection()\\'>未收藏</a>';");
            frame.append("if(collectionList.length>0){");
            frame.append("if(collectionList.filter(item=>{return item.id==id}).length>0){");
            frame.append("text = '<a style=\\'color: black;cursor: not-allowed;\\'>已收藏</a>';\t");
            frame.append("}};");
            frame.append("COLLECT_DIV.innerHTML=text;");
            frame.append("COLLECT_DIV.style.setProperty('display','block');");
            frame.append("}");

            // 收藏
            frame.append("function collection(){");
            frame.append("let item = document.querySelector('.current');");
            frame.append("if(item){");
            frame.append("if(collectionList){");
            frame.append("let time = new Date().toLocaleString('chinese',{hour12:false});");
            frame.append("let id = item.children[0].id;");
            frame.append("let name = item.children[0].textContent;");
            frame.append("collectionList = collectionList.filter(item=>{return item.id!=id});");
            frame.append("collectionList.unshift({id,name,time});");
            frame.append("localStorage.setItem('collectionList',JSON.stringify(collectionList));");
            frame.append("isCollection(id);}");
            frame.append("showCollection();");
            frame.append("}}");

            //显示收藏
            frame.append("function showCollection() {");
            frame.append("let collectionListItem = localStorage.getItem('collectionList');");
            frame.append("if(collectionListItem){");
            frame.append("collectionListItem = JSON.parse(collectionListItem);");
            frame.append("collectionList=collectionListItem;");
            frame.append("SHOW_COLLECTION.style.setProperty('display','block');");
            frame.append("let collect_body='我的收藏：<span style=\\'float: right;color:blue;cursor:pointer;\\' onclick=\\'clean()\\'>清空</span><br>';");
            frame.append("collect_body+='<ul style=\\'width:auto;display:block;padding-inline-start: 0px;\\'>';");
            frame.append("let tmp=[];");
            frame.append("let count = collectionListItem.length>10?10:collectionListItem.length;");
            frame.append("for(let i=0;i<count;i++){");
            frame.append("let item = collectionListItem[i];");
            frame.append("tmp.push(item);");
            frame.append("collect_body+='<li style=\\'list-style: none;width: auto;text-align-last: auto;background-color: #e4ebf8; border: none;margin: auto;\\' >';");
            frame.append("collect_body+='<span style=\\'color:#795548;\\'>'+item.time+'&emsp;</span>';");
            frame.append("if(document.getElementById(item.id)){");
            frame.append("collect_body+='<a style=\\'font-size:inherit;\\' href=\\'#'+item.id+'\\' onclick=\\'document.getElementById('+item.id+').click()\\'>'+item.name+'&emsp;</a>';");
            frame.append("}else{");
            frame.append("collect_body+='<a style=\\'font-size:inherit;color:#9E9E9E;cursor: not-allowed;\\'>'+item.name+'&emsp;</a>';");
            frame.append("}");
            frame.append("collect_body+='<span style=\\'color: blue;cursor: pointer;float: right;\\' onclick=\\'clean('+item.id+')\\'>删除</span>';");
            frame.append("collect_body+='</li>';");
            frame.append("}");
            frame.append("collect_body+='</ul>';");
            frame.append("SHOW_COLLECTION.innerHTML=collect_body;");
            frame.append("collectionList=tmp;");
            frame.append("}else{");
            frame.append("SHOW_COLLECTION.style.setProperty('display','none');");
            frame.append("}};");


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
            frame.append("let endTime = getTime(player.currentTime());");
            frame.append("player.dispose();");
            frame.append("document.body.innerHTML='定时结束,已销毁,请刷新！<br>最后播放位置: '+endTime+'<br>'+SHOW_NAME.innerText;");
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
            frame.append("LOCALE.children[0].href='#'+id;");
            frame.append("if(document.getElementById(id).offsetParent.offsetTop>document.body.clientHeight){");
            frame.append("LOCALE.style.setProperty('display', 'block');");
            frame.append("}else{");
            frame.append("LOCALE.style.setProperty('display', 'none');}");
            frame.append("SHOW_NAME.innerHTML = name;");
            frame.append("SHOW_DOWNLOAD.innerHTML = '<a onclick=\\'searchById('+id+')\\'>下&emsp;载</a>';");
            frame.append("player.src([{src: date + '/' + id + '/' + id + '.m3u8',type: 'application/x-mpegURL'}]);");
            frame.append("SHOW_NAME.scrollIntoView({behavior: 'smooth'});");
            frame.append("CONTROL.style.setProperty('display', 'block');");
            frame.append("MY_PROGRESS.style.setProperty('display', 'block');");
            frame.append("SEARCH_A.innerText = '';");
            frame.append("isCollection(id);");
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
            frame.append("function searchById(id) {");
            frame.append("let inputId = SEARCH_INPUT.value.trim();");
            frame.append("if(id) inputId=id;");
            frame.append("if (!inputId)return;");
            frame.append("MASK.style.setProperty('display', 'block');");
            //frame.append("document.body.style.position = 'fixed';");
            frame.append("document.body.style.overflow = 'hidden';");
            frame.append("SEARCH_A.innerHTML = '<span style=\\'color:red\\'>处理中...</span>';");
            frame.append("let xhr = new XMLHttpRequest();");
            frame.append("xhr.open('post', '/qfdj/' + inputId, true);");
            frame.append("xhr.onload = function() {");
            frame.append("let data = xhr.responseText;");
            frame.append("if (xhr.status == 200) {");
            frame.append("if (data) {");
            frame.append("let prefix = data.substring(1, data.lastIndexOf('/'));");
            frame.append("let suffix = data.substring(data.lastIndexOf('/') + 1);");
            frame.append("SEARCH_A.innerHTML = '<span style=\\'color:red\\'>↓点击下载↓</span><br><br><a href=\\''+data+'\\' target=\\'_blank\\' >'+suffix+'</a>';");
            frame.append("SHOW_DOWNLOAD.innerHTML = '<a onclick=\\'searchById('+inputId+')\\'>下&emsp;载</a>';");
            frame.append("if(!id){");
            frame.append("player.src([{src: prefix + '/' + inputId + '/' + inputId + '.m3u8',type: 'application/x-mpegURL'}]);");
            frame.append("SHOW_NAME.innerHTML = suffix;");
            frame.append("let currentLi=document.querySelector('.current');");
            frame.append("if (currentLi){currentLi.removeAttribute('class');currentLi.removeAttribute('style');}");
            frame.append("}");
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
            frame.append("let isDspl=!0,rate=1,MY_INTERVAL=null,MY_PROGRESS=document.getElementById(\"myProgress\"),MY_BAR=document.getElementById(\"myBar\"),SHOW_VIEW=document.getElementById(\"showView\"),SHOW_NAME=document.getElementById(\"showName\"),SHOW_DOWNLOAD=document.getElementById(\"showDownload\"),SEARCH_A=document.getElementById(\"searchA\"),SEARCH_INPUT=document.getElementById(\"searchInput\"),BID=document.getElementById(\"bid\"),CONTROL=document.getElementById(\"control\"),BTN=CONTROL.children[1],MY_TIMEOUT=null,TIME_INPUT=document.getElementById(\"timeInput\"),MASK=document.getElementById(\"mask\"),LOCALE=document.getElementById(\"locale\"),collectionList=[],SHOW_COLLECTION=document.getElementById(\"showCollection\"),COLLECT_DIV=document.getElementById(\"collectDiv\");function clean(e){if(e&&(collectionList=collectionList.filter(t=>t.id!=e)).length>0)return isCollection(document.querySelector(\".current\").children[0].id),localStorage.setItem(\"collectionList\",JSON.stringify(collectionList)),void showCollection();localStorage.removeItem(\"collectionList\"),collectionList=[],SHOW_COLLECTION.innerHTML=\"\",SHOW_COLLECTION.style.setProperty(\"display\",\"none\")}function isCollection(e){let t=\"<a onclick='collection()'>未收藏</a>\";collectionList.length>0&&collectionList.filter(t=>t.id==e).length>0&&(t=\"<a style='color: black;cursor: not-allowed;'>已收藏</a>\"),COLLECT_DIV.innerHTML=t,COLLECT_DIV.style.setProperty(\"display\",\"block\")}function collection(){let e=document.querySelector(\".current\");if(e){if(collectionList){let t=(new Date).toLocaleString(\"chinese\",{hour12:!1}),n=e.children[0].id,l=e.children[0].textContent;(collectionList=collectionList.filter(e=>e.id!=n)).unshift({id:n,name:l,time:t}),localStorage.setItem(\"collectionList\",JSON.stringify(collectionList)),isCollection(n)}showCollection()}}function showCollection(){let e=localStorage.getItem(\"collectionList\");if(e){e=JSON.parse(e),collectionList=e,SHOW_COLLECTION.style.setProperty(\"display\",\"block\");let t=\"我的收藏：<span style='float: right;color:blue;cursor:pointer;' onclick='clean()'>清空</span><br>\";t+=\"<ul style='width:auto;display:block;padding-inline-start: 0px;'>\";let n=[],l=e.length>10?10:e.length;for(let o=0;o<l;o++){let l=e[o];n.push(l),t+=\"<li style='list-style: none;width: auto;text-align-last: auto;background-color: #e4ebf8; border: none;margin: auto;' >\",t+=\"<span style='color:#795548;'>\"+l.time+\"&emsp;</span>\",document.getElementById(l.id)?t+=\"<a style='font-size:inherit;' href='#\"+l.id+\"' onclick='document.getElementById(\"+l.id+\").click()'>\"+l.name+\"&emsp;</a>\":t+=\"<a style='font-size:inherit;color:#9E9E9E;cursor: not-allowed;'>\"+l.name+\"&emsp;</a>\",t+=\"<span style='color: blue;cursor: pointer;float: right;' onclick='clean(\"+l.id+\")'>删除</span>\",t+=\"</li>\"}t+=\"</ul>\",SHOW_COLLECTION.innerHTML=t,collectionList=n}else SHOW_COLLECTION.style.setProperty(\"display\",\"none\")}function changeV(){rate=document.getElementById(\"range\").value,document.getElementById(\"label\").innerText=rate,player.playbackRate(rate)}function timeOut(){let e=TIME_INPUT.value;e&&e>0&&e<1200&&(MY_TIMEOUT&&(window.clearTimeout(MY_TIMEOUT),MY_TIMEOUT=null),MY_TIMEOUT=window.setTimeout(()=>{let e=getTime(player.currentTime());player.dispose(),document.body.innerHTML=\"定时结束,已销毁,请刷新！<br>最后播放位置: \"+e+\"<br>\"+SHOW_NAME.innerText},60*e*1e3),TIME_INPUT.parentElement.innerHTML=\"<span style='color:red'>定时关闭已开启[\"+(new Date).toLocaleString(\"zh\",{huor12:!1})+\"]<br>将在\"+e+\"分钟后自动关闭(取消请刷新页面)</span>\")}function play(){let e=event.target,t=e.id,n=e.parentElement.parentElement.previousElementSibling.innerText,l=e.textContent,o=document.querySelector(\".current\");o&&(o.removeAttribute(\"class\"),o.removeAttribute(\"style\")),document.getElementById(t).parentElement.setAttribute(\"class\",\"current\"),LOCALE.children[0].href=\"#\"+t,document.getElementById(t).offsetParent.offsetTop>document.body.clientHeight?LOCALE.style.setProperty(\"display\",\"block\"):LOCALE.style.setProperty(\"display\",\"none\"),SHOW_NAME.innerHTML=l,SHOW_DOWNLOAD.innerHTML=\"<a onclick='searchById(\"+t+\")'>下&emsp;载</a>\",player.src([{src:n+\"/\"+t+\"/\"+t+\".m3u8\",type:\"application/x-mpegURL\"}]),SHOW_NAME.scrollIntoView({behavior:\"smooth\"}),CONTROL.style.setProperty(\"display\",\"block\"),MY_PROGRESS.style.setProperty(\"display\",\"block\"),SEARCH_A.innerText=\"\",isCollection(t)}function dspl(){(isDspl=!isDspl)?SHOW_VIEW.style.setProperty(\"display\",\"none\"):SHOW_VIEW.style.setProperty(\"display\",\"flex\")}function progress(){let e=MY_PROGRESS,t=e.offsetWidth,n=getOffsetLeft(e),l=((event.clientX+document.body.scrollLeft-n)/t*100).toFixed(2);player.currentTime(player.duration()*l/100)}function getOffsetLeft(e){let t=e.offsetLeft,n=e.offsetParent;for(;null!=n;)t+=n.offsetLeft,n=n.offsetParent;return t}function getTime(e){let t=Math.floor(e/3600),n=Math.floor(e%3600/60),l=Math.floor(e%60);return(t>9?t:\"0\"+t)+\":\"+(n>9?n:\"0\"+n)+\":\"+(l>9?l:\"0\"+l)}function searchById(e){let t=SEARCH_INPUT.value.trim();if(e&&(t=e),!t)return;MASK.style.setProperty(\"display\",\"block\"),document.body.style.overflow=\"hidden\",SEARCH_A.innerHTML=\"<span style='color:red'>处理中...</span>\";let n=new XMLHttpRequest;n.open(\"post\",\"/qfdj/\"+t,!0),n.onload=function(){let l=n.responseText;if(200==n.status)if(l){let n=l.substring(1,l.lastIndexOf(\"/\")),o=l.substring(l.lastIndexOf(\"/\")+1);if(SEARCH_A.innerHTML=\"<span style='color:red'>↓点击下载↓</span><br><br><a href='\"+l+\"' target='_blank' >\"+o+\"</a>\",SHOW_DOWNLOAD.innerHTML=\"<a onclick='searchById(\"+t+\")'>下&emsp;载</a>\",!e){player.src([{src:n+\"/\"+t+\"/\"+t+\".m3u8\",type:\"application/x-mpegURL\"}]),SHOW_NAME.innerHTML=o;let e=document.querySelector(\".current\");e&&(e.removeAttribute(\"class\"),e.removeAttribute(\"style\"))}}else SEARCH_A.innerHTML=\"<span style='color:red'>非法编号: \"+t+\"</span>\";else console.log(n.status,n.responseText,n.getAllResponseHeaders()),SEARCH_A.innerHTML=\"<span style='color:red'>未知错误!\"+n.status+\"</span>\";SEARCH_INPUT.value=\"\"},n.timeout=18e4,n.ontimeout=function(){SEARCH_A.innerHTML=\"<span style='color:red'>请求超时,请稍后再试!</span>\"},n.send(null)}function prevNext(e){let t=document.querySelectorAll(\"a[id]\"),n=0,l=player.currentSrc();if(l){let o=l.substr(l.lastIndexOf(\"/\")+1,l.length-l.lastIndexOf(\".\")+1);t.forEach((t,l,r)=>{t.id==o&&(n=l+e)})}t[n=(n=n<0?t.length-1:n)>t.length-1?0:n].click()}function continuePlay(){player.paused()?(player.src([{src:player.currentSrc(),type:\"application/x-mpegURL\"}]),player.currentTime(player.currentTime())):(player.pause(),MY_INTERVAL&&(clearInterval(MY_INTERVAL),MY_INTERVAL=null))}window.onscroll=function(){let e=document.querySelector(\".current\");if(e){let t=e.offsetTop,n=document.body.scrollTop,l=e.offsetHeight,o=document.body.clientHeight;Math.abs(n-t)>o||n>t+l?LOCALE.style.setProperty(\"display\",\"block\"):LOCALE.style.setProperty(\"display\",\"none\")}},window.onload=function(){document.querySelectorAll(\"a[id]\").forEach(e=>{e.addEventListener(\"click\",play)});let e=document.createElement(\"iframe\");e.setAttribute(\"style\",\"display:none\");let t=document.createElement(\"video\");t.setAttribute(\"id\",\"my-player\"),e.appendChild(t),document.body.appendChild(e),showCollection(),player=videojs(\"my-player\",{autoplay:!0,controls:!0,preload:\"auto\"},function(){this.on(\"ended\",()=>{prevNext(1)}),this.on(\"waiting\",()=>{document.querySelector(\".current\")&&(document.querySelector(\".current\").style.animationPlayState=\"paused\"),BTN.innerText=\"加载中\"}),this.on(\"error\",()=>{MY_INTERVAL&&(this.clearInterval(MY_INTERVAL),MY_INTERVAL=null),MY_INTERVAL=this.setInterval(()=>{this.src([{src:this.currentSrc(),type:\"application/x-mpegURL\"}]),this.currentTime(this.currentTime())},1e4)}),this.on(\"loadedmetadata\",()=>{player.playbackRate(rate),MY_INTERVAL&&(this.clearInterval(MY_INTERVAL),MY_INTERVAL=null)}),this.on(\"timeupdate\",()=>{let e=this.currentTime(),t=(e/this.duration()*100).toFixed(2),n=getTime(e);MY_BAR.style.width=t+\"%\",MY_BAR.innerText=n,BID.innerText=\"-\"+getTime(this.remainingTime())}),this.on(\"playing\",()=>{document.querySelector(\".current\")&&(document.querySelector(\".current\").style.animationPlayState=\"running\"),BTN.innerText=\"播放中\"}),this.on(\"pause\",()=>{document.querySelector(\".current\")&&(document.querySelector(\".current\").style.animationPlayState=\"paused\"),BTN.innerText=\"已暂停\"})})},document.addEventListener(\"keydown\",e=>{if(e.ctrlKey&&37===e.keyCode)prevNext(-1);else if(e.ctrlKey&&39===e.keyCode)prevNext(1);else if(e.ctrlKey&&38===e.keyCode){let e=player.volume()+.1;player.volume(e>1?1:e)}else if(e.ctrlKey&&40===e.keyCode){let e=player.volume()-.1;player.volume(e<0?0:e)}else 32===e.keyCode&&(player.currentSrc()?continuePlay():prevNext())});");
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
