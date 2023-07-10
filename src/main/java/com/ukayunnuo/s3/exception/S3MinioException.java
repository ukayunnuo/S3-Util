package com.ukayunnuo.s3.exception;

import cn.hutool.core.util.StrUtil;
import io.minio.errors.MinioException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;

/**
 * s3 minio例外
 * minio 异常
 *
 * @author ukayunnuo
 * @date 2023-07-10
 * @since 1.0.0
 */
@Getter
@Setter
public class S3MinioException extends MinioException implements Serializable {

    private String bucketName;

    private String objectName;

    public S3MinioException(String message, String httpTrace) {
        super(message, httpTrace);
    }

    public S3MinioException(String bucketName, String message, Exception e) {
        super(StrUtil.format("bucketName:{}, msg:{}", bucketName, message), Arrays.toString(e.getStackTrace()));
        this.bucketName = bucketName;
        super.setStackTrace(e.getStackTrace());
    }

    public S3MinioException(String bucketName, String objectName, String message, Exception e) {
        super(StrUtil.format("bucketName:{}, objectName:{}, msg:{}", bucketName, objectName, message), Arrays.toString(e.getStackTrace()));
        this.bucketName = bucketName;
        this.objectName = objectName;
        super.setStackTrace(e.getStackTrace());
    }

    public S3MinioException(String bucketName, String objectName, String message) {
        super(StrUtil.format("bucketName:{}, objectName:{}, msg:{}", bucketName, objectName, message));
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    public S3MinioException(Exception e) {
        super(e.getMessage(), Arrays.toString(e.getStackTrace()));
    }

    public S3MinioException(String message, Exception e) {
        super(message, Arrays.toString(e.getStackTrace()));
    }

    public S3MinioException(String message) {
        super(message);
    }

}
