package com.ukayunnuo.s3.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.ukayunnuo.s3.exception.S3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Amazon s3 工具类
 *
 * @author ukayunnuo
 * @since 1.0.0
 */
@Slf4j
public class AmazonS3Utils {

    /**
     * 上传文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param path     路径
     * @param key      key
     * @return {@link PutObjectResult}
     */
    public static PutObjectResult uploadFile(AmazonS3 amazonS3, String bucket, String path, String key) {

        if (Objects.isNull(amazonS3) || StrUtil.isBlank(path) || StrUtil.isBlank(key)) {
            throw new S3Exception("param is empty, please verification!");
        }
        return uploadFile(amazonS3, bucket, new File(path), key);
    }

    /**
     * 上传文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param file     文件
     * @param key      key
     * @return {@link PutObjectResult}
     */
    public static PutObjectResult uploadFile(AmazonS3 amazonS3, String bucket, File file, String key) {

        if (Objects.isNull(amazonS3) || Objects.isNull(file) || StrUtil.isBlank(key)) {
            throw new S3Exception("param is empty, please verification!");
        }
        return amazonS3.putObject(new PutObjectRequest(bucket, key, file));
    }

    /**
     * 上传文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param file     文件
     * @param folder   文件夹
     * @return {@link PutObjectResult}
     * @throws IOException ioexception
     */
    public static PutObjectResult uploadFile(AmazonS3 amazonS3, String bucket, MultipartFile file, String folder) throws IOException {

        if (Objects.isNull(amazonS3) || Objects.isNull(file) || StrUtil.isBlank(folder)) {
            throw new S3Exception("param is empty, please verification!");
        }
        String key = StrUtil.format("%s%s%s", folder, File.separator, file.getOriginalFilename());
        return uploadFile(amazonS3, bucket, file.getInputStream(), file.getSize(), file.getContentType(), key);
    }


    /**
     * 上传文件
     *
     * @param amazonS3        amazon s3
     * @param bucket          桶
     * @param fileInput       文件输入
     * @param contentLength   内容长度
     * @param fileContentType 文件内容类型
     * @param key             key
     * @return {@link PutObjectResult}
     */
    public static PutObjectResult uploadFile(AmazonS3 amazonS3, String bucket, InputStream fileInput, Long contentLength, String fileContentType, String key) {
        if (Objects.isNull(amazonS3) || Objects.isNull(fileInput)) {
            throw new S3Exception("param is empty, please verification!");
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        if (StrUtil.isNotBlank(fileContentType)) {
            metadata.setContentType(fileContentType);
        }
        PutObjectRequest request = new PutObjectRequest(bucket, key, fileInput, metadata);
        return amazonS3.putObject(request);
    }


    /**
     * 分段上传文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param file     文件
     * @param folder   文件夹
     * @return {@link UploadResult}
     * @throws InterruptedException 打断了异常
     * @throws IOException          ioexception
     */
    public static UploadResult subsectionUploadFile(AmazonS3 amazonS3, String bucket, MultipartFile file, String folder) throws InterruptedException, IOException {
        if (Objects.isNull(amazonS3) || Objects.isNull(file) || StrUtil.isBlank(folder)) {
            throw new S3Exception("param is empty, please verification!");
        }
        String key = StrUtil.format("%s%s%s", folder, File.separator, file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        return subsectionUploadFile(amazonS3, new PutObjectRequest(bucket, key, file.getInputStream(), metadata));
    }

    /**
     * 分段上传文件
     *
     * @param amazonS3       amazon s3
     * @param bucket         桶
     * @param input          input输入流
     * @param objectMetadata 对象元数据
     * @param key            key
     * @return {@link UploadResult}
     * @throws InterruptedException 打断了异常
     */
    public static UploadResult subsectionUploadFile(AmazonS3 amazonS3, String bucket, InputStream input, ObjectMetadata objectMetadata, String key) throws InterruptedException {
        return subsectionUploadFile(amazonS3, new PutObjectRequest(bucket, key, input, objectMetadata));
    }

    /**
     * 分段上传文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param file     文件
     * @param key      key
     * @return {@link UploadResult}
     * @throws InterruptedException 打断了异常
     */
    public static UploadResult subsectionUploadFile(AmazonS3 amazonS3, String bucket, File file, String key) throws InterruptedException {
        return subsectionUploadFile(amazonS3, new PutObjectRequest(bucket, key, file));
    }

    /**
     * 分段上传文件
     *
     * @param amazonS3         amazon s3
     * @param putObjectRequest 对象
     * @return {@link UploadResult}
     * @throws InterruptedException 打断异常
     */
    public static UploadResult subsectionUploadFile(AmazonS3 amazonS3, PutObjectRequest putObjectRequest) throws InterruptedException {
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(amazonS3)
                .build();
        Upload upload = tm.upload(putObjectRequest);
        return upload.waitForUploadResult();
    }

    /**
     * 预签名上传文件( 默认：GET 请求)
     *
     * @param amazonS3   amazon s3
     * @param bucket     桶
     * @param expiration 过期
     * @param key        key
     * @return {@link URL}
     */
    public static URL preUploadFile(AmazonS3 amazonS3, String bucket, Date expiration, String key) {
        return amazonS3.generatePresignedUrl(new GeneratePresignedUrlRequest(bucket, key)
                .withExpiration(expiration));
    }

    /**
     * 预签名上传文件
     *
     * @param amazonS3   amazon s3
     * @param bucket     桶
     * @param expiration 过期时间
     * @param method     方法
     * @param key        key
     * @return {@link URL}
     */
    public static URL preUploadFile(AmazonS3 amazonS3, String bucket, Date expiration, HttpMethod method, String key) {
        return amazonS3.generatePresignedUrl(new GeneratePresignedUrlRequest(bucket, key)
                .withExpiration(expiration)
                .withMethod(method));
    }


    /**
     * 获取对象 key
     *
     * @param amazonS3   amazon s3
     * @param bucketName bucket名称
     * @param maxKeys    马克斯钥匙
     * @return {@link List}<{@link String}>
     */
    public static List<String> getObjectKeys(AmazonS3 amazonS3, String bucketName, int maxKeys) {
        return getObjectKeys(amazonS3, bucketName, StrUtil.EMPTY, maxKeys);
    }

    /**
     * 获取对象 key
     *
     * @param amazonS3   amazon s3
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @param maxKeys    马克斯钥匙
     * @return {@link List}<{@link String}>
     */
    public static List<String> getObjectKeys(AmazonS3 amazonS3, String bucketName, String prefix, int maxKeys) {
        ObjectListing objectList = getObjectList(amazonS3, bucketName, prefix, maxKeys);
        if (Objects.isNull(objectList) || CollUtil.isEmpty(objectList.getObjectSummaries())) {
            return ListUtil.empty();
        }
        return objectList.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    /**
     * 获取对象列表
     *
     * @param amazonS3   amazon s3
     * @param bucketName bucket名称
     * @param maxKeys    马克斯钥匙
     * @return {@link ObjectListing}
     */
    public static ObjectListing getObjectList(AmazonS3 amazonS3, String bucketName, int maxKeys) {
        return getObjectList(amazonS3, bucketName, StrUtil.EMPTY, maxKeys);
    }

    /**
     * 获取对象列表
     *
     * @param amazonS3   amazon s3
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @param maxKeys    马克斯钥匙
     * @return {@link ObjectListing}
     */
    public static ObjectListing getObjectList(AmazonS3 amazonS3, String bucketName, String prefix, int maxKeys) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        if (StrUtil.isNotBlank(prefix)) {
            listObjectsRequest.setPrefix(prefix);
        }
        if (maxKeys >= 0) {
            listObjectsRequest.setMaxKeys(maxKeys);
        }
        return amazonS3.listObjects(listObjectsRequest);
    }

    /**
     * 获取s3对象信息
     *
     * @param amazonS3   amazon s3
     * @param bucketName bucket名称
     * @param key        文件key
     * @return {@link S3Object}
     */
    public static S3Object getObjectInfo(AmazonS3 amazonS3, String bucketName, String key) {
        return amazonS3.getObject(new GetObjectRequest(bucketName, key));
    }

    /**
     * 共享文件
     * <p> 注意: 使用 Amazon 软件开发工具包，预签名 URL 的最长过期时间为自创建时起 7 天 </p>
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param expDay   过期单位：天
     * @param method   方法
     * @param key      key
     * @return {@link URL}
     */
    public static URL shareFile(AmazonS3 amazonS3, String bucket, Integer expDay, HttpMethod method, String key) {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucket, key);
        urlRequest.setExpiration(DateUtil.offsetDay(DateUtil.date(), expDay));
        urlRequest.setMethod(method);
        return amazonS3.generatePresignedUrl(urlRequest);
    }

    /**
     * 共享文件
     * <p> 注意: 使用 Amazon 软件开发工具包，预签名 URL 的最长过期时间为自创建时起 7 天 </p>
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param expDate  实验日期
     * @param method   方法
     * @param key      key
     * @return {@link URL}
     */
    public static URL shareFile(AmazonS3 amazonS3, String bucket, Date expDate, HttpMethod method, String key) {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucket, key);
        urlRequest.setExpiration(expDate);
        urlRequest.setMethod(method);
        return amazonS3.generatePresignedUrl(urlRequest);
    }

    /**
     * 共享文件
     * <p> 注意: 使用 Amazon 软件开发工具包，预签名 URL 的最长过期时间为自创建时起 7 天 </p>
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param expDate  实验日期
     * @param key      key
     * @return {@link URL}
     */
    public static URL shareFile(AmazonS3 amazonS3, String bucket, Date expDate, String key) {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucket, key);
        urlRequest.setExpiration(expDate);
        urlRequest.setMethod(HttpMethod.GET);
        return amazonS3.generatePresignedUrl(urlRequest);
    }

    /**
     * 下载文件
     *
     * @param amazonS3   amazon s3
     * @param bucketName bucket名称
     * @param key        key
     * @return {@link InputStream}
     */
    public static InputStream downloadFile(AmazonS3 amazonS3, String bucketName, String key) {
        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        S3Object response = amazonS3.getObject(request);
        return response.getObjectContent();
    }

    /**
     * 下载文件
     *
     * @param amazonS3       amazon s3
     * @param bucketName     bucket名称
     * @param key            key
     * @param targetFilePath 目标路径
     * @return {@link File}
     */
    public static File downloadFile(AmazonS3 amazonS3, String bucketName, String key, String targetFilePath) {
        S3ObjectInputStream inputStream = null;
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            inputStream = amazonS3.getObject(request).getObjectContent();
            return FileUtil.writeFromStream(inputStream, new File(targetFilePath));
        } finally {
            if (inputStream != null) {
                IoUtil.close(inputStream);
            }
        }
    }

    /**
     * 复制文件
     *
     * @param amazonS3          amazon s3
     * @param sourceBucket      源桶
     * @param destinationBucket 目地桶
     * @param sourceKey         源key
     * @param destinationKey    目地key
     * @return {@link CopyObjectResult}
     */
    public static CopyObjectResult copyFile(AmazonS3 amazonS3, String sourceBucket, String destinationBucket, String sourceKey, String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucket, sourceKey, destinationBucket, destinationKey);
        return amazonS3.copyObject(copyObjectRequest);
    }

    /**
     * 复制文件
     *
     * @param amazonS3       amazon s3
     * @param bucket         桶
     * @param sourceKey      源key
     * @param destinationKey 目地key
     * @return {@link CopyObjectResult}
     */
    public static CopyObjectResult copyFile(AmazonS3 amazonS3, String bucket, String sourceKey, String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucket, sourceKey, bucket, destinationKey);
        return amazonS3.copyObject(copyObjectRequest);
    }


    /**
     * 删除文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param key      key
     */
    public static void deleteFile(AmazonS3 amazonS3, String bucket, String key) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, key));
    }

    /**
     * 批量删除文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param keys     键
     * @return {@link DeleteObjectsResult}
     */
    public static DeleteObjectsResult deleteFiles(AmazonS3 amazonS3, String bucket, List<String> keys) {
        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys.stream().filter(Objects::nonNull).map(DeleteObjectsRequest.KeyVersion::new).collect(Collectors.toList()))
                .withQuiet(false);

        return amazonS3.deleteObjects(multiObjectDeleteRequest);
    }

    /**
     * 批量删除文件
     *
     * @param amazonS3 amazon s3
     * @param bucket   桶
     * @param keys     键
     * @return {@link DeleteObjectsResult}
     */
    public static DeleteObjectsResult deleteFiles(AmazonS3 amazonS3, String bucket, String... keys) {
        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucket)
                .withKeys(Arrays.stream(keys).filter(Objects::nonNull).map(DeleteObjectsRequest.KeyVersion::new).collect(Collectors.toList()))
                .withQuiet(false);

        return amazonS3.deleteObjects(multiObjectDeleteRequest);
    }


}
