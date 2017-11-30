package TranscodingVM;
/*
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.*;
*/
public class cloudMain {
    public static void main(String[] args){
        /*
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAJTLLH5SVF74IJ6NQ",
                "ZJCx4bcqYb78EZc/1d1foHSSgJSIDMhV+uiQkFKG");

        AmazonS3 s3client = new AmazonS3Client(credentials);
        */
        //if(args.length==3) {
            System.out.println("running ec2 test x");
            TranscodingVMcloud me = new TranscodingVMcloud(args[0],args[1], Integer.parseInt(args[2]));
            me.run();
        /*}else{

            System.out.println("running ec2 test 2 default");
            TranscodingVMcloud me = new TranscodingVMcloud("ec2","localhost", 5060);
            me.run();
        //}
        */
    }
}
