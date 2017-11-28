package TranscodingVM;

public class cloudMain {
    public static void main(String[] args){
        if(args.length==3) {
            System.out.println("running ec2 test x");
            TranscodingVMcloud me = new TranscodingVMcloud(args[0],args[1], Integer.parseInt(args[2]));
            me.run();
        }else{
            System.out.println("running ec2 test 2 default");
            TranscodingVMcloud me = new TranscodingVMcloud("ec2","localhost", 333);
            me.run();
        }
    }
}
