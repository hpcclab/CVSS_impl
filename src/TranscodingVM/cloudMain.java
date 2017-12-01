package TranscodingVM;
import Scheduler.ServerConfig;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import java.util.List;
public class cloudMain {
    public static void main(String[] args){
        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        List<Bucket> buckets=s3.listBuckets();
        Bucket b;
        System.out.println(buckets);


        if (s3.doesBucketExist("cvss-video-bucket")) {
            System.out.format("Bucket %s already exists.\n", "cvss-video-bucket");
            b =s3.listBuckets().get(0);

        }else{
            System.out.println("Bucket Not Exist");
            b = s3.createBucket("cvss-video-bucket");
        }

        if(args.length==4) {
            if(args[0].equalsIgnoreCase("EC2")) {
                ServerConfig.path=args[2];
                ServerConfig.addFakeDelay=Boolean.parseBoolean(args[3]);
                //ServerConfig.
                ServerConfig.defaultInputPath="/sdc/";
                ServerConfig.defaultOutputPath="";

                System.out.println("running ec2 test x");
                TranscodingVMcloud me = new TranscodingVMcloud(args[0], "test", Integer.parseInt(args[1]));
                me.run();
            }
        }else {
            System.out.println("incorrect args");
        }

    }
}
