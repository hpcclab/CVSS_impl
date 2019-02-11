package IOWindows;
import Scheduler.GOPTaskScheduler;

import java.io.IOException;
import java.io.StringReader;

import javax.annotation.Resource;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;

import javax.xml.ws.handler.MessageContext;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;

import Simulator.RequestGenerator;

//// Trimmed code from:
//https://www.javaworld.com/article/3222065/java-language/web-services-in-java-se-part-3-creating-restful-web-services.html


@WebServiceProvider
@ServiceMode(value = javax.xml.ws.Service.Mode.MESSAGE)
@BindingType(value = HTTPBinding.HTTP_BINDING)
class handler implements Provider<Source> {
    @Resource
    private WebServiceContext wsContext;
    GOPTaskScheduler GTS;
    public handler(GOPTaskScheduler gts){
        GTS=gts;
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
            xml.append("<response> invalid request</response>");
            return new StreamSource(new StringReader(xml.toString()));
        }
        else
        {
            //internal usage, thus strict format
            String[] arg = qs.split("[=,]+");
            if (!arg[0].equalsIgnoreCase("videoid")&&!arg[2].equalsIgnoreCase("cmd")&&!arg[4].equalsIgnoreCase("setting"))
                throw new HTTPException(400);
            int video = Integer.parseInt(arg[1]);
            int arrival=2000;
            String cmd=arg[3];
            String setting=arg[5];
            RequestGenerator.OneSpecificRequest(GTS, video, cmd,setting, arrival+20000, arrival);

            /*
            if (pair[2].equalsIgnoreCase("dl"))
                String d = pair[1].trim();
            */
            StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
            xml.append("<response> video request "+ arg[1]+" "+arg[3]+" "+arg[5] +" accepted</response>");
            return new StreamSource(new StringReader(xml.toString()));
        }
    }

    private Source doPost(MessageContext msgContext, Source source) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
        xml.append("<response> post request unimplemeted</response>");
        return new StreamSource(new StringReader(xml.toString()));
    }

    private Source doPut(MessageContext msgContext, Source source) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?>");
        xml.append("<response> put request unimplemeted</response>");
        return new StreamSource(new StringReader(xml.toString()));
    }

}

public class Webservicegate {
    public String addr="http://localhost:9902/transcoderequest";
    public GOPTaskScheduler GTS;
    Endpoint ep;
    public void startListener() throws IOException
    {
        ep=Endpoint.publish(addr, new handler(GTS));
    }
    public void stopListener() throws IOException
    {
        ep.stop();
    }
}
