package Cache;

import SessionPkg.TranscodingRequest;

import java.util.HashMap;

public class CachingWithPathTable extends Caching {

    private HashMap<String,String> Table =new HashMap<String,String>();;

    public CachingWithPathTable(){
        super();
    }
    //don't forget to implement this with something to notify settings, currently it count all settings the same
    public String convertStreamGopToString(TranscodingRequest x){
        return x.DataSource; //simple version
        //return x.videoname+"/"+String.format("%04d",x.segment)+" "+x.cmdSet.toString(); //store cmd and param too
    }
    public boolean checkExistence(String x){
        return Table.containsKey(x);
    }
    public boolean checkExistence(TranscodingRequest x)
    {
        return checkExistence(convertStreamGopToString(x));
    }
    //modify path if it is not the same
    public String getCachedPath(TranscodingRequest x)
    {
        //return "streams/"+x.DataSource+"/"+String.format("%04d",x.segment)+".ts";
        return "streams/"+x.DataSource+".ts";
    }

    public void addCached(TranscodingRequest x){
        Table.put(convertStreamGopToString(x),getCachedPath(x));
    }
}
