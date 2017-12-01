package TranscodingVM;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;

public class TranscodingVMcloud extends TranscodingVM{
//just in case anything different to TranscodingVM (thread)
    public TranscodingVMcloud(String type,String addr,int port){
        super(type,addr,port);
        //set flag to use S3

        //connect to S3
        AWSCredentials credentials = new BasicAWSCredentials("AKIAIWLF5HX335BP23RQ", "JP0AWhKmzMvV15Lq69/Az3jJZxUF2FxKvybDyFem");

        Region region = Region.getRegion(Regions.US_EAST_2);
        String bucket_name = "cvss-video-bucket";
        AmazonS3Client s3 = new AmazonS3Client(credentials);
        s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build());

        TT.addS3(s3,bucket_name);
        System.out.println("config cloud finished");
    }


}
