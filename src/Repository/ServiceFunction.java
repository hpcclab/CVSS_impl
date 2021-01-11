package Repository;

public class ServiceFunction {
    public String fnName;
    public String containerName;
    String containerType; //either oneUse or persistent or builtin

    public ServiceFunction(String fn_name,String container_name){
        this(fn_name,container_name,"persistent");
    }

    public ServiceFunction(String fn_name,String container_name,String type){

        fnName=fn_name;
        containerName=container_name;
        if(type.equalsIgnoreCase("persistent")){
            containerType="persistent";
        }else if(type.equalsIgnoreCase("oneUse")){
            containerType="oneuse";
        }else if(type.equalsIgnoreCase("builtin")){ //legacy type, try not to use it
            containerType="builtin";
        }else{
            System.out.println("Warning, unknown service function type");
        }
    }
}
