package com.ukayunnuo.s3.exception;

/**
 * S3 异常类
 *
 * @author ukayunnuo
 * @since 1.0.0
 */
public class S3Exception extends RuntimeException {

    public S3Exception(String message){
        super(message);
    }

    public S3Exception(Exception e){
        super(e.getMessage(), e);
    }

    public S3Exception(String message, Exception e){
        super(message, e);
    }

}
