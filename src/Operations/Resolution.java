package Operations;


public class Resolution extends simpleoperation{
    public Resolution(){
    operationname="Resolution";
    }
    public String toCMD(String input, String output, String parameter,int mode){
        if(mode==0){ //for sim mode, do nothing to convert the parameter to command
            return parameter;
        }
        //other case, try to make ffmpeg call
        return "ffmpeg -i "+ input+" -r "+parameter+" -o "+output;
    }
}
