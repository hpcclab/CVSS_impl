package Operations;

public class Bitrate  extends simpleoperation{
    public Bitrate(){
        operationname="Bitrate";
    }
    public String toCMD(String input, String output, String parameter,int mode){
        if(mode==0){ //for sim mode, do nothing to convert the parameter to command
            return parameter;
        }
        //other case, try to make ffmpeg call
        return "";
    }
}
