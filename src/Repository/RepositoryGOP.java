package Repository;

/**
 * Created by pi on 5/21/17.
 */
public class RepositoryGOP implements java.io.Serializable {

    private String path;
    public String segment;
    public Boolean isTranscoded;
    public String setting;

    public RepositoryGOP(String path){
        setPath(path);
        isTranscoded = false;
        //segment = path.substring(path.length()-7,path.length()-3);//gets last 4 characters of path before extension, a number between 0000 and 9999
        String[] psplit=path.split("/");
        segment=psplit[psplit.length-1].split("\\.")[0];
    }
    public RepositoryGOP(){
    }
    public RepositoryGOP(RepositoryGOP x){
        this.path=x.getPath();
        this.segment=x.segment;
        this.isTranscoded=x.getIsTranscoded();
        this.setting=x.setting;
    }
    public String getPath(){ return path;}

    public void setPath(String path){this.path = path;}

    public String getSegmentNum()
    {
        return segment;
    }

    public void setSegmentNum(String segment)
    {
        this.segment = segment;
    }


    public Boolean getIsTranscoded()
    {
        return isTranscoded;
    }

    public void setIsTranscoded(Boolean isTranscoded)
    {
        this.isTranscoded = isTranscoded;
    }


}