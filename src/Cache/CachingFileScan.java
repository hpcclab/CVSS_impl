package Cache;

import SessionPkg.TranscodingRequest;

import java.io.File;

public class CachingFileScan extends Caching {
    public CachingFileScan(){
        super();
    }
    public boolean checkExistence(TranscodingRequest x)
    {
        //currently only check if file is exist, not checking for set-up
        File targetfile = new File(getCachedPath(x));
        return targetfile.exists();
    }
    // if cached, return Path String for cached video
    public String getCachedPath(TranscodingRequest x)
    {
        //return "streams/"+x.DataSource+"/"+String.format("%04d",x.)+".ts";
        return "streams/"+x.DataSource+".ts";
    }
}
