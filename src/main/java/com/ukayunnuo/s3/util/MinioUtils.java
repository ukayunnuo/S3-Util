package com.ukayunnuo.s3.util;

import com.alibaba.fastjson2.JSONObject;
import com.ukayunnuo.s3.exception.S3MinioException;
import com.ukayunnuo.s3.minio.MinioProp;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * minio 工具类
 *
 * @author yunnuo
 * @since 1.0.0
 */
@Component
@Slf4j
public class MinioUtils {

    @Resource
    private MinioProp minioProp;

    @Resource
    private MinioClient minioClient;


    /* ------------------------------------> bucket operate start  <------------------------------------------------ */

    /**
     * 初始化Bucket
     *
     * @throws S3MinioException s3minio异常
     */
    private void initBucket() throws S3MinioException {
        createBucket(minioProp.getBucketName());
    }

    /**
     * 创建桶
     *
     * @param bucketName bucket名称
     * @throws S3MinioException s3minio异常
     */
    public void createBucket(String bucketName) throws S3MinioException {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new S3MinioException("createBucket error!", e);
        }
    }


    /**
     * 判断桶是否存在
     *
     * @param bucketName 存储桶
     * @return true：存在
     */
    public boolean doesBucketExist(String bucketName) throws S3MinioException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, e.getMessage(), e);
        }
    }

    /**
     * 获取桶策略
     *
     * @param bucketName bucket名称
     * @return {@link String}
     * @throws S3MinioException s3minio异常
     */
    public String getBucketPolicy(String bucketName) throws S3MinioException {
        try {
            return minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, e.getMessage(), e);
        }
    }

    /**
     * 获取所有桶
     *
     * @return {@link List}<{@link Bucket}>
     * @throws S3MinioException s3minio异常
     */
    public List<Bucket> getAllBuckets() throws S3MinioException {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            throw new S3MinioException(e.getMessage(), e);
        }
    }


    /**
     * 获取桶信息
     *
     * @param bucketName bucket名称
     * @return {@link Optional}<{@link Bucket}>
     * @throws S3MinioException s3minio异常
     */
    public Optional<Bucket> getBucketInfo(String bucketName) throws S3MinioException {
        try {
            return getAllBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
        } catch (Exception e) {
            throw new S3MinioException(e.getMessage(), e);
        }
    }

    /**
     * 删除桶
     *
     * @param bucketName bucket名称
     * @throws S3MinioException s3minio异常
     */
    public void removeBucket(String bucketName) throws S3MinioException {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new S3MinioException(e.getMessage(), e);
        }
    }

    /* ------------------------------------> bucket operate end  <------------------------------------------------ */



    /* ------------------------------------> object operate start  <------------------------------------------------ */


    /**
     * 判断文件夹是否存在
     *
     * @param bucketName 存储桶
     * @param folderName 文件夹名称（去掉/）
     * @return true：存在
     */
    public boolean folderExist(String bucketName, String folderName) throws S3MinioException {
        boolean exist = false;
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(folderName).recursive(false).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() && folderName.equals(item.objectName())) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            throw new S3MinioException(bucketName, folderName, e.getMessage(), e);
        }
        return exist;
    }

    /**
     * 判断对象是否存在
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return boolean
     */
    public boolean objectExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }


    /**
     * 获取文件外链
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @param expires    过期时间 <=7 秒
     * @return {@link String} url
     * @throws Exception 异常
     */
    public String getObjectUrl(String bucketName, String objectName, Integer expires) throws S3MinioException {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs
                    .builder().bucket(bucketName).object(objectName).expiry(expires).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }


    /**
     * 获取文件外链
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @param expires    过期时间 <=7 秒
     * @param method     方法
     * @return {@link String}
     * @throws S3MinioException s3minio异常
     */
    public String getObjectUrl(String bucketName, String objectName, Integer expires, Method method) throws S3MinioException {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs
                    .builder().bucket(bucketName).object(objectName).expiry(expires).method(Method.GET).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }


    /**
     * 获取对象
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return {@link InputStream} 文件流
     * @throws S3MinioException s3minio异常
     */
    public InputStream getObject(String bucketName, String objectName) throws S3MinioException {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }

    /**
     * 获取对象信息
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @return {@link String}
     * @throws S3MinioException s3minio异常
     */
    public String getObjectInfo(String bucketName, String objectName) throws S3MinioException {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(objectName).build()).toString();
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }


    /**
     * 获取对象 （断点下载）
     *
     * @param bucketName bucket名称
     * @param objectName 对象名称
     * @param offset     起始字节位置
     * @param length     读取长度
     * @return {@link InputStream}
     * @throws Exception 异常
     */
    public InputStream getObject(String bucketName, String objectName, long offset, long length) throws S3MinioException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(objectName).offset(offset).length(length).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }

    /**
     * 通过MultipartFile上传文件
     *
     * @param bucketName 存储桶
     * @param file       文件
     * @param objectName 对象名
     */
    public ObjectWriteResponse putObject(String bucketName, MultipartFile file, String objectName, String contentType) throws S3MinioException {
        try (InputStream inputStream = file.getInputStream()) {
            return minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).contentType(contentType)
                            .stream(inputStream, inputStream.available(), -1).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }

    /**
     * 上传本地文件
     *
     * @param bucketName 存储桶
     * @param objectName 对象名称
     * @param fileName   本地文件路径
     */
    public ObjectWriteResponse putObject(String bucketName, String objectName, String fileName) throws S3MinioException {
        try {
            return minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName).object(objectName).filename(fileName).build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }

    /**
     * 通过流上传文件
     *
     * @param bucketName  存储桶
     * @param objectName  文件对象
     * @param inputStream 文件流
     */
    public ObjectWriteResponse putObjectByStream(String bucketName, String objectName, InputStream inputStream) throws S3MinioException {
        try {
            return minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(inputStream, inputStream.available(), -1)
                            .build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }

    /**
     * 创建文件夹或目录
     *
     * @param bucketName 存储桶
     * @param objectName 目录路径
     */
    public ObjectWriteResponse createFolder(String bucketName, String objectName) throws S3MinioException {
        try {
            return minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }


    /**
     * 拷贝文件
     *
     * @param bucketName    存储桶
     * @param objectName    文件名
     * @param srcBucketName 目标存储桶
     * @param srcObjectName 目标文件名
     */
    public ObjectWriteResponse copyObject(String bucketName, String objectName, String srcBucketName, String srcObjectName) throws S3MinioException {
        try {
            return minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder().bucket(bucketName).object(objectName).build())
                            .bucket(srcBucketName)
                            .object(srcObjectName)
                            .build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶
     * @param objectName 文件名称
     */
    public void removeObject(String bucketName, String objectName) throws S3MinioException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new S3MinioException(bucketName, objectName, e.getMessage(), e);
        }
    }


    /**
     * 批量删除对象
     *
     * @param bucketName  bucket名称
     * @param objectsName 对象名字
     * @return {@link Iterable}<{@link Result}<{@link DeleteError}>>
     * @throws S3MinioException s3minio异常
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objectsName) throws S3MinioException {
        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName)
                    .objects(objectsName.stream().map(DeleteObject::new).collect(Collectors.toList())).build());
            return results;
        } catch (Exception e) {
            throw new S3MinioException(bucketName, JSONObject.toJSONString(objectsName), e.getMessage(), e);
        }
    }

    /**
     * 删除对象
     *
     * @param bucketName  bucket名称
     * @param objectName 对象名字
     * @return {@link Iterable}<{@link Result}<{@link DeleteError}>>
     * @throws S3MinioException s3minio异常
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, String... objectName) throws S3MinioException {
        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName)
                    .objects(Arrays.stream(objectName).map(DeleteObject::new).collect(Collectors.toList())).build());
            return results;
        } catch (Exception e) {
            throw new S3MinioException(bucketName, JSONObject.toJSONString(objectName), e.getMessage(), e);
        }
    }

}
