package com.ukayunnuo.s3.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * minio 配置
 *
 * @author yunnuo
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProp {

    /**
     连接地址
     */
    private String endpoint;
    /**
     * 用户名
     */
    private String accesskey;
    /**
     * 密码
     */
    private String secretkey;
    /**
     * 桶名
     */
    private String bucketName;

    /**
     * cdn前缀
     */
    private String cdnPrefix;

}
