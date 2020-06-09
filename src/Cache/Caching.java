package Cache;

import SessionPkg.TranscodingRequest;

public class Caching {
    public Caching(){
    }
    //always return false in this simplest version, all items not exist
    public boolean checkExistence(TranscodingRequest x)
    {
        return false;
    }
    // if cached, return Path String for cached video
    public String getCachedPath(TranscodingRequest x)
    {
        return "";
    }
    // if caching policy can remember the video, implement this function
    public void addCached(TranscodingRequest x){

    }
}
