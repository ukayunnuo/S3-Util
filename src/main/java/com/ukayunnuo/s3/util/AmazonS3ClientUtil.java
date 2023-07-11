package com.ukayunnuo.s3.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ukayunnuo.s3.aws.AwsS3Prop;
import com.ukayunnuo.s3.exception.S3Exception;
import com.ukayunnuo.s3.oci.OciS3Prop;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Amazon S3 连接 工具
 *
 * @author ukayunnuo
 * @since 1.0.0
 */
@Slf4j
public class AmazonS3ClientUtil {

    private static final String ENDPOINT_CONSTANT_STR = "%s.compat.objectstorage.%s.oraclecloud.com";

    public static AmazonS3 awsAmazonS3Client(AwsS3Prop config) {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        Regions regions = Regions.fromName(config.getRegions());
        builder.withRegion(regions);
        if (StrUtil.isNotBlank(config.getAccessKeyId()) && StrUtil.isNotBlank(config.getSecretAccessKey())) {
            // AWS静态凭证
            builder.withCredentials(
                    new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(config.getAccessKeyId(), config.getSecretAccessKey())));
        } else {
            // ec2容器凭据
            builder.withCredentials(
                    new EC2ContainerCredentialsProviderWrapper());
        }
        if (Objects.isNull(builder.build())) {
            log.warn("connect S3 Server is failure , please check your config param! config:{}", JSONObject.toJSONString(config));
            throw new S3Exception("AWS S3 Client Failed!");
        }
        return builder.build();
    }

    public static AmazonS3 ociAmazonS3Client(OciS3Prop config) {
        if (!StrUtil.isAllNotBlank(config.getAccessKeyId(), config.getSecretAccessKey())) {
            log.warn("ociS3Client ak, sk 不允许配置为空! config:{}", config);
            throw new S3Exception("ak, sk 不允许配置为空!");
        }
        if (!StrUtil.isAllNotBlank(config.getNamespace(), config.getRegions())) {
            log.warn("ociS3Client namespace, regions 不允许配置为空! config:{}", config);
            throw new S3Exception("namespace, regions 不允许配置为空!");
        }
        AWSCredentialsProvider credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                config.getAccessKeyId(),
                config.getSecretAccessKey()));
        String endpoint = String.format(ENDPOINT_CONSTANT_STR, config.getNamespace(), config.getRegions());
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, config.getRegions());
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(credentials)
                .withEndpointConfiguration(endpointConfiguration)
                .disableChunkedEncoding()
                .enablePathStyleAccess()
                .build();
    }

}
