

package testpackage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pi on 11/29/17.
 */
public class S3Control {


    private static void TestBucket(){

        //AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();

        AWSCredentials credentials = new BasicAWSCredentials("AKIAIWLF5HX335BP23RQ", "JP0AWhKmzMvV15Lq69/Az3jJZxUF2FxKvybDyFem");

        Region region = Region.getRegion(Regions.US_EAST_2);

        String bucket_name = "cvss-video-bucket";

        AmazonS3 s3 = new AmazonS3Client(credentials);

        s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build());

        boolean exists = s3.doesBucketExist(bucket_name);

        //AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-2").build();

        //final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        List<Bucket> buckets = s3.listBuckets();
        System.out.println("Your Amazon S3 buckets are:");
        for (Bucket b : buckets) {
            System.out.println("* " + b.getName());
        }

        try {

            System.out.println(" - removing objects from bucket");
            ObjectListing object_listing = s3.listObjects(bucket_name);
            /*
            while (true) {
                for (Iterator<?> iterator =
                     object_listing.getObjectSummaries().iterator();
                     iterator.hasNext();) {
                    S3ObjectSummary summary = (S3ObjectSummary)iterator.next();

                    //s3.deleteObject(bucket_name, summary.getKey());
                }

                // more object_listing to retrieve?
                if (object_listing.isTruncated()) {
                    object_listing = s3.listNextBatchOfObjects(object_listing);
                } else {
                    break;
                }
            };
            */

            //ListAllFiles("output/bbb_trailer", s3);
/*
            File file = new File("/home/pi/repositoryvideos/bbb_trailer/0000.ts");

            PutFile(bucket_name, "output/bbb_trailer/0000.ts", file, s3);
*/
            /*
            try {
                CreateVideoDirectory(bucket_name, "bbb_trailer", s3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            //  CreateFolder(bucket_name, "output/ff_trailer_part1", s3);

        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
    }

    public static void CreateVideoDirectory(String bucketName, String videoName, AmazonS3 client) throws IOException {


        /*
        // create meta-data for your folder and set content-length to 0

        // create empty content
        //InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

        File file = GetFile(bucketName, "repository/" + videoName + "/",client);

        //PutFile(bucketName, "output/" + videoName + "/out.m3u8", file, client);

        InputStream inputStream = new FileInputStream(file);

        byte[] bytes = IOUtils.toByteArray(inputStream);

        Long contentLength = Long.valueOf(bytes.length);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);

        // create a PutObjectRequest passing the folder name suffixed by /
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                "output/" + videoName + "/out.m3u8", inputStream, metadata);

        // send request to S3 to create folder
        client.putObject(putObjectRequest);
        */

        client.copyObject(bucketName, "repository/" + videoName + "/out.m3u8", bucketName, "output/" + videoName + "/out.m3u8");

    }

    public static void PutFile(String bucketName, String fileName, File file, AmazonS3 client){

        client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicReadWrite));
    }

    public static File GetFile(String bucketName, String fileName, AmazonS3 client) throws IOException {

        S3Object s3object = client.getObject(new GetObjectRequest(
                bucketName, fileName));
        System.out.println("Content-Type: "  +
                s3object.getObjectMetadata().getContentType());

        final BufferedInputStream i = new BufferedInputStream(s3object.getObjectContent());

        InputStream reader = new BufferedInputStream(
                s3object.getObjectContent());

        //Files.copy(objectData)

        File file = new File("out.m3u8");
        OutputStream writer = new BufferedOutputStream(new FileOutputStream(file));

        int read = -1;

        while ( ( read = reader.read() ) != -1 ) {
            writer.write(read);
        }

        writer.flush();
        writer.close();
        reader.close();

        return file;

    }

    public static void ListAllFiles(String folderName, AmazonS3 client){
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName("cvss-video-bucket")
                .withPrefix("folderName").withDelimiter("/");
        ObjectListing objects = client.listObjects(listObjectsRequest);
        int test = 0;
        test++;
    }
/*
    public static void CreateFolder(String bucketName, String folderName, AmazonS3 client) {
        // create meta-data for your folder and set content-length to 0
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        // create empty content
        //InputStream emptyContent = new ByteArrayInputStream(new byte[0]);


        final InputStream emptyContent = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };

        // create a PutObjectRequest passing the folder name suffixed by /
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                folderName + "/", emptyContent, metadata);

        // send request to S3 to create folder
        client.putObject(putObjectRequest);
        int test = 0;
        test++;

    }
    */
}
