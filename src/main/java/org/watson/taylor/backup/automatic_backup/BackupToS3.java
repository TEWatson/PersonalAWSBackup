package org.watson.taylor.backup.automatic_backup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

public class BackupToS3 {
	private static String bucketName     = "org-watson-taylor-backup";
	private static String keyName        = "backup.zip";
	private static String uploadFileName;
	private static AmazonS3 s3Client;
	private static TransferManager uploadManager;
	
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			uploadFileName = args[0];
			System.out.println("Uploading " + args[0] + " to S3...");
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Date dateForFormatter = new Date();
		String stringDate = dateFormat.format(dateForFormatter);
		keyName = stringDate + keyName;
		
        s3Client = AmazonS3ClientBuilder.standard()
        		.withCredentials(new ProfileCredentialsProvider("backup"))
                .withRegion(Regions.US_WEST_2)
                .build();
        // Use TransferManager because backup files may be more than 5GB
        uploadManager = TransferManagerBuilder.standard()
        		.withS3Client(s3Client)
        		.build();
        
        boolean uploadSuccess = true;
        try {
            File file = new File(uploadFileName);
            Upload upload = uploadManager.upload(new PutObjectRequest(
            										bucketName, keyName, file));
            upload.waitForCompletion();
            

         } catch (AmazonServiceException ase) {
        	uploadSuccess = false;
            System.out.println("Caught an AmazonServiceException, which " +
            		"means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
        	uploadSuccess = false;
            System.out.println("Caught an AmazonClientException, which " +
            		"means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (InterruptedException inte) {
        	System.out.println("Upload was interrupted with the following message:");
			System.out.println(inte.getMessage());
		}
        if (uploadSuccess)
        	System.out.println("Connection concluded.");
        else
        	System.out.println("Something went wrong with the file upload! Object not sent.");
    }
}
