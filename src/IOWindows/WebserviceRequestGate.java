package IOWindows;

import Scheduler.GOPTaskScheduler;
import SessionPkg.SessionManager;
import mainPackage.CVSE;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//// Trimmed code from:
//https://www.javaworld.com/article/3222065/java-language/web-services-in-java-se-part-3-creating-restful-web-services.html


@WebServiceProvider
@ServiceMode(value = javax.xml.ws.Service.Mode.MESSAGE)
@BindingType(value = HTTPBinding.HTTP_BINDING)
class handler implements Provider<Source> {
    @Resource
    private WebServiceContext wsContext;
    SessionManager SM;
    GOPTaskScheduler GTS;
    public handler(GOPTaskScheduler gts, SessionManager sm){
        GTS=gts; SM=sm;
    }

    @Override
    public Source invoke(Source request) {
        if (wsContext == null)
            throw new RuntimeException("dependency injection failed on wsContext");
        MessageContext msgContext = wsContext.getMessageContext();
        switch ((String) msgContext.get(MessageContext.HTTP_REQUEST_METHOD)) {
            case "DELETE":
                return doDelete(msgContext);
            case "GET":
                return doGet(msgContext);
            case "POST":
                return doPost(msgContext, request);
            case "PUT":
                return doPut(msgContext, request);
            default:
                throw new HTTPException(405);
        }
    }

    private Source doDelete(MessageContext msgContext) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
        xml.append("<response> delete request unimplemeted</response>");
        return new StreamSource(new StringReader(xml.toString()));

    }

    private Source doGet(MessageContext msgContext) {
        String qs = (String) msgContext.get(MessageContext.QUERY_STRING);
        if (qs == null) {
            StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
            //StringBuilder xml = new StringBuilder("<html><header>Access-Control-Allow-Origin: *</header></html>"); //experiment

            xml.append("<response> invalid request</response>");
            return new StreamSource(new StringReader(xml.toString()));
        }
        else
        {
            //internal usage, thus strict format
            //String[] arg = qs.split("[=,]+");

            String[] arg = qs.split("param=|vidnum=");
            arg[1]=arg[1].replaceFirst(",",""); //remove , from first param
                //create Folder
                File file=new File("/runningwork/sampleOutput/"+arg[1]);
                file.mkdirs();
            //Copy manifest
                Path original= Paths.get("/runningwork/sampleRepo/"+arg[1]+"/video.m3u8");
                Path dest=Paths.get("/runningwork/sampleOutput/"+arg[1]+"/video.m3u8");
                try {
                    Files.copy(original, dest);
                }catch (Exception e){
                     System.out.println("copy manifest bug");
                }
            for (int i=0;i<arg.length;i++){
                System.out.println("Printing argument["+i+"]: " + arg[i]);
                //data accepted! arg[1]=vidnum arg[2]=partial params
            }
            long currentTime= System.currentTimeMillis();
            //CVSE.RG.OneSpecificRequest(arqe.videoChoice,arqe.startgopnum,arqe.endgopnum, arqe.command, arqe.setting, arqe.deadline, arqe.appearTime);
            //give 4000ms before video play
            CVSE.RG.OneSpecificRequest(Integer.parseInt(arg[1])-1,-1,-1, "custom", arg[2], currentTime+4000, currentTime);
//CVSE video lib index shifted by 1 (start at 0), so -1 or arg[1]

//            if (!arg[0].equalsIgnoreCase("videoid")&&!arg[2].equalsIgnoreCase("cmd")&&!arg[4].equalsIgnoreCase("setting"))
//                throw new HTTPException(400);
//            int video = Integer.parseInt(arg[1]);
//            int arrival=2000;
//            String cmd=arg[3];
//            String setting=arg[5];

//            Settings newSettings = new Settings();
//            newSettings.type = cmd;
//            newSettings.settingNum = setting;
//            newSettings.videoname = CVSE.VR.videos.get(video).name;
//
//            //newSettings.resolution = true;
//            if(cmd.equals("resolution")){
//                if(setting.equals("1080")){
//                    newSettings.resHeight = "720";
//                    newSettings.resWidth  = "1080";
//                }
//                else if(setting.equals("720")){
//                    newSettings.resHeight = "640";
//                    newSettings.resWidth  = "720";
//                }
//                else if(setting.equals("640")){
//                    newSettings.resHeight = "480";
//                    newSettings.resWidth  = "640";
//                }
//                else if(setting.equals("480")){
//                    newSettings.resHeight = "360";
//                    newSettings.resWidth  = "480";
//                }
//
//            }else if(cmd.equals("bitrate")){
//
//            }
//            else if(cmd.equals("framerate")){
//
//            }
//            else if(cmd.equals("blackwhite")){
//                newSettings.settingNum = "";
//            }
//
//
//            System.out.println("SM: " + SM);
//            System.out.println("GTS: " + GTS);
//            System.out.println();
//
//            SM.InitializeStream(video, newSettings, CVSE.GTS);
//
//            String response = newSettings.videoDir();
//

            StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
            //xml.append("<response> video request "+ arg[1]+" "+arg[3]+" "+arg[5] +" accepted</response>");
            xml.append("<response> video request "+ arg[1]+" accepted</response>");
            //xml.append(newSettings.videoDir());
            return new StreamSource(new StringReader(xml.toString()));
        }
    }

    private Source doPost(MessageContext msgContext, Source source) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
        System.out.println(msgContext);
        xml.append("<response> post request unimplemeted</response>");
        return new StreamSource(new StringReader(xml.toString()));
    }

    private Source doPut(MessageContext msgContext, Source source) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
        xml.append("<response> put request unimplemeted</response>");
        return new StreamSource(new StringReader(xml.toString()));
    }

}

public class WebserviceRequestGate {
    public String addr="http://localhost:9901/transcoderequest";
    //public String addr="http://127.0.0.1/transcoderequest";

    public GOPTaskScheduler GTS;
    public SessionManager SM;
    Endpoint ep;
    public void startListener() throws IOException
    {
        ep=Endpoint.publish(addr, new handler(GTS, SM));
    }
    public void stopListener() throws IOException
    {
        ep.stop();
    }
}