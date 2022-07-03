package com.alice.handler;

import com.alice.config.CommonBean;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

@Component
public class CreateHtml extends CommonBean {

    private static final String PATH_SEPARATOR = File.separator;
    private static final String RUNTIME_DIR = System.getProperty("user.dir");
    private static final String date = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
//    private static final String date = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()+1000*60*60*24);

    /**
     * 生成首页html
     */
    public void createHtml(){

        String userDir = static_file_path;
        if(userDir==null||"".equals(userDir.trim()))
            userDir=RUNTIME_DIR;

        String aday  = deleteNdays;
        if(aday==null||"".equals(aday.trim()))
            aday="7";


        String dir = userDir+PATH_SEPARATOR+"Music"+PATH_SEPARATOR;
        log.info("生成Html页面到：{}",dir);
        //复制所需的js和css文件
        if(!new File(dir+"video.min.js").exists()){
            try {
                InputStream i= this.getClass().getResource("video.min.js").openStream();
                FileOutputStream o = new FileOutputStream(dir+"video.min.js");
                FileCopyUtils.copy(i, o);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!new File(dir+"video-js.min.css").exists()){
            try {
                InputStream i= this.getClass().getResource("video-js.min.css").openStream();
                FileOutputStream o = new FileOutputStream(dir+"video-js.min.css");
                FileCopyUtils.copy(i, o);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(!new File(dir+"favicon.ico").exists()){
            try {
                InputStream i= this.getClass().getResource("favicon.ico").openStream();
                FileOutputStream o = new FileOutputStream(dir+"favicon.ico");
                FileCopyUtils.copy(i, o);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(dir+date+"/list"+date+".html"),"utf-8"); OutputStreamWriter wf = new OutputStreamWriter(new FileOutputStream(dir+"index.html"),"utf-8")) {

            //生成列表
            StringBuilder sb = new StringBuilder();
            boolean hasItem=false;
            sb.append("<div class='sdate'>");
            //sb.append("<div style=\"text-align:center;background-color:yellow;margin:10px;padding:5px;\">");
            sb.append("<span>"+date+"</span>");
            sb.append("</div>");
            sb.append("<ol>");
            File f = new File(dir+date);
            String[] list = f.list();
            Arrays.sort(list, Collections.reverseOrder());
            for (int i = 0; i < list.length; i++)
                if (list[i].endsWith(".aac")){
                    hasItem=true;
                    sb.append("<li class=\"item\"> ");
                    sb.append("<a id=\""+list[i].split("_")[0]+"\" onclick=\"play()\" href=\"#\">"+list[i]+ "</a>");
                    sb.append("</li>\n");
                }
            sb.append("</ol>");
            if(hasItem){
                fw.write(sb.toString());
            }
            fw.flush();
            fw.close();

            //生成首页+列表
            File fm = new File(dir);
            File[] listm = fm.listFiles();
            StringBuilder frame = new StringBuilder();
            frame.append("<html>");
            frame.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
            frame.append("<style type=\"text/css\">li{text-align-last:justify;border:solid 1px #d6fdff;margin:2px;width:580px}</style>");
            frame.append("<style type=\"text/css\">.item:nth-child(odd){background-color:#ffc0cb3b;}</style>");
            frame.append("<style type=\"text/css\">.item:nth-child(even){background-color:#aacaff3b;}</style>");
            frame.append("<style type=\"text/css\">.current{border: solid 1px;width:725px;font-size:initial;text-align:center;background-color:greenyellow;");
            frame.append("animation-name:current_ant;animation-duration: 2s;animation-iteration-count: infinite;animation-direction: alternate;}</style>");
            frame.append("<style type=\"text/css\">@keyframes current_ant{from{transform:scale(1,1)} to{transform:scale(0.8,0.8)}}</style>");
            frame.append("<style type=\"text/css\">h1{cursor: pointer;} a{text-decoration:none;font-size:larger;} a:visited,a:link{color:blue;}</style>");
            frame.append("<style type=\"text/css\">ol{display: flex;flex-direction: column; align-items: center;margin-bottom: 40px;padding: 10px;width: 80%;box-shadow: 0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}</style>");
            frame.append("<style type=\"text/css\">.sdate{font-size:x-large;text-align:center;width:90%;height:30px;border-radius:0.3em;");
            frame.append("box-shadow:0.3rem 0.3rem 0.6rem #c8d0e7, -0.2rem -0.2rem 0.5rem #ffffff;}</style>");

            frame.append("<link rel=\"stylesheet\" href=\"video-js.min.css\">");
            frame.append("<script src=\"video.min.js\"></script>");
            frame.append("<title>在线试听</title>");
            frame.append("</head><body style='font-size:smaller;background-color:#e4ebf8;display: flex; flex-direction: column;align-items: center;'>");
            frame.append("<h1 onclick='dspl()' id=\"showName\" style = \"color:red;text-align: center;\">点击列表↓↓↓↓播放音乐</h1>");
            //头部隐藏块
            frame.append("<div id='showView' style='display: none; flex-direction: column; align-items: center; justify-content: flex-start;'>");            
            frame.append("<video id='my-player'  class='video-js vjs-big-play-centered'></video>");
            frame.append("<div><input id='range' type='range' value='1' min='0.1' max='2.5' step='0.1' onchange='changeV()'>");
            frame.append("<br>倍速:<label id='label' for='range'>1</label></div>");
            frame.append("<div><input type='number' id='searchInput' placeholder='请输入音乐编号：'></input><button onclick='search()'>搜索</button></div>");
            frame.append("<span id='searchA'></span>");
            frame.append("</div>");
            //控制
            frame.append("<h1 id='control'  style='display:none;color:red;text-align: center;'>");
            frame.append("<a title='ctrl+←' href='#' onclick='prevNext(-1)'>←上一首</a>&nbsp;&nbsp;|&nbsp;&nbsp;");
            frame.append("<a title='space' href='#' onclick='continuePlay()'></a>&nbsp;&nbsp;|&nbsp;&nbsp;");
            frame.append("<a title='ctrl+→' href='#' onclick='prevNext(1)'>下一首→</a></h1>");
            frame.append("<h1 id=\"showDowload\" style = \"color:red;text-align: center;\"></h1>");

            //加载列表
            Arrays.asList(listm).stream().filter( o-> o.isDirectory()&&o.getName().startsWith("20")).map(o->o.getName()).sorted(Comparator.reverseOrder()).forEach(dname -> {

                        try{
                            BufferedReader br = new BufferedReader(new FileReader(dir+dname+"/list"+dname+".html"));
                            String len = null;
                            while ((len = br.readLine()) != null) {
                                frame.append(len);
                            }
                            br.close();
                        }catch (Exception e){
                            log.error(e.getMessage());
                        }


                    }
            );

            frame.append("<div>每日13点10分更新，仅保留近"+aday+"天的内容<div>");
            frame.append(" <script type=\"text/javascript\">");
            //初始参数
            frame.append("let swich=true; ");
            frame.append("let rate = 1;");
            frame.append("let btn = document.getElementById('control').children[1];");
            //页面加载完成
            frame.append("window.onload=function(){ ");
            frame.append("player = videojs('my-player', {autoplay:true,controls:true,preload:\"auto\"},function(){");
            frame.append("this.on('ended',()=>{");
            frame.append("prevNext(1);");
            frame.append("});");
            frame.append("this.on('waiting',()=>{");
            frame.append("if (document.querySelector('.current') != null)");
            frame.append("document.querySelector('.current').style.animationPlayState = 'paused';");
            frame.append("btn.innerText ='加载中...';");
            frame.append("});");
            frame.append("this.on('play',()=>{");
            frame.append("player.playbackRate(rate);");
            frame.append("});");
            frame.append("this.on('playing',()=>{");
            frame.append("if (document.querySelector('.current') != null)");
            frame.append("document.querySelector('.current').style.animationPlayState = 'running';");
            frame.append("btn.innerText ='播放中';");
            frame.append("});");
            frame.append("this.on('pause',()=>{");
            frame.append("document.querySelector('.current').style.animationPlayState = 'paused';");
            frame.append("btn.innerText ='已暂停';");
            frame.append("});");
            frame.append("});");
            frame.append("};");
            //倍速
            frame.append("function changeV(){");
            frame.append("rate = document.getElementById('range').value;");
            frame.append("document.getElementById('label').innerText=rate;");
            frame.append("player.playbackRate(rate);");
            frame.append("};");
            //播放
            frame.append("function play() {");
            frame.append("var aa=event.target;");
            frame.append("var id = aa.id;");
            frame.append("var date = aa.parentElement.parentElement.previousElementSibling.innerText;");
            frame.append("var name = aa.textContent;");
            frame.append("if(document.querySelector('.current')!=null)document.querySelector('.current').setAttribute('class','item');");
            frame.append("document.getElementById(id).parentElement.setAttribute('class','current');");
            frame.append("document.getElementById('showName').innerHTML=name;");
            frame.append("document.getElementById('showDowload').innerHTML=\"<a target='_blank' href='\"+date+\"/\"+name+\"'>↓下载↓</a>\";");
            frame.append("player.src([{src: date+\"/\"+id+\"/\"+id+\".m3u8\",type: \"application/x-mpegURL\"}]);");
            frame.append("document.getElementById('showName').scrollIntoView({ behavior: 'smooth'});");
            frame.append("document.getElementById('control').style.setProperty('display','block');");
            frame.append("};");
            //显示播放窗口
            frame.append("function dspl() {");
            frame.append("if (swich=!swich)");
            frame.append("document.getElementById('showView').style.setProperty('display','none');");
            frame.append("else ");
            frame.append("document.getElementById('showView').style.setProperty('display','flex');");
            frame.append("};");
            //搜索
            frame.append("function search() {");
            frame.append("var inputId = document.getElementById('searchInput').value.trim();");
            frame.append("if(!inputId)return;");
            frame.append("document.getElementById('searchA').innerText='处理中...';");
            frame.append("var xhr = new XMLHttpRequest();");
            frame.append("xhr.open('get','/qfdj/'+inputId,true);");
            frame.append("xhr.onload = function () {");
            frame.append("var data = xhr.responseText;");
            frame.append("if(data.length<200){");
            frame.append("if(data){");
            frame.append("var prefix = data.substring(1,data.lastIndexOf('/'));");
            frame.append("var suffix = data.substring(data.lastIndexOf('/')+1);");
            frame.append("var bb = document.createElement('a');");
            frame.append("player.src([{src: prefix+'/'+inputId+'/'+inputId+'.m3u8',type: 'application/x-mpegURL'}]);");
            frame.append("bb.href=data;");
            frame.append("bb.target='_blank';");
            frame.append("bb.innerText=suffix;");
            frame.append("document.getElementById('showName').innerHTML=suffix;");
            frame.append("document.getElementById('searchA').innerText='';");
            frame.append("document.getElementById('searchA').appendChild(bb);");
            frame.append("}else{");
            frame.append("document.getElementById('searchA').innerText='非法编号:'+inputId;");
            frame.append("}}else{");
            frame.append("document.getElementById('searchA').innerText='未知错误';");
            frame.append("};");
            frame.append("document.getElementById('searchInput').value='';");
            frame.append("};");
            frame.append("xhr.send(null);");
            frame.append("};");
            //下一首，上一首
            frame.append("function prevNext(num) {");
            frame.append("var list = document.querySelectorAll('a[id]');");
            frame.append("var nextIndex=0;");
            frame.append("let str = player.currentSrc();");
            frame.append("let id = str.substr(str.lastIndexOf('/')+1,str.length-str.lastIndexOf('.')+1);");
            frame.append("list.forEach((item,index,arr)=>{if(item.id == id)nextIndex=index+num;});");
            frame.append("nextIndex=nextIndex<0?list.length-1:nextIndex;");
            frame.append("nextIndex=nextIndex>list.length-1?0:nextIndex;");
            frame.append("list[nextIndex].click();");
            frame.append("};");
            //暂停，播放
            frame.append("function continuePlay() {");
            frame.append("if(player.paused())player.play();else player.pause();");
            frame.append("};");
            //键盘事件
            frame.append("document.addEventListener('keydown',e=>{");
            frame.append("if(e.ctrlKey&&e.keyCode===37)prevNext(-1);");
            frame.append("else if(e.ctrlKey&&e.keyCode===39)prevNext(1);");
            frame.append("else if(e.ctrlKey&&e.keyCode===38){var tvol=player.volume()+0.1;player.volume(tvol>1?1:tvol)}");
            frame.append("else if(e.ctrlKey&&e.keyCode===40){var tvol=player.volume()-0.1;player.volume(tvol<0?0:tvol)}");
            frame.append("else if(e.keyCode===32)continuePlay();");
            frame.append("});");
            frame.append("</script>");
            frame.append("</body>");
            frame.append("</html>");
            wf.write(frame.toString());
            wf.flush();
            log.info("生成页面结束!");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("生成Html页面异常:",e.getMessage());
        }

    }

}
