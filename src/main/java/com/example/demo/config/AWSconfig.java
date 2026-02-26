package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AWSconfig {
    @Value("${spring.cloud.aws.region.static}")
    private String region;
    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKeyId;
    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretAccessKey;
    @Bean
    public S3Client s3Client() {
//        Create credentials
        AwsCredentials awsCredentials= AwsBasicCredentials.create(accessKeyId,secretAccessKey);
//        Config region and credentials
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
    @Bean
    public S3Presigner s3Presigner(){
        AwsCredentials awsCredentials= AwsBasicCredentials.create(accessKeyId,secretAccessKey);
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
