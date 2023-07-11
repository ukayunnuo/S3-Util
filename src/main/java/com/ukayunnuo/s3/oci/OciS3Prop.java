package com.ukayunnuo.s3.oci;

import cn.hutool.json.JSONUtil;
import lombok.Builder;
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
@ConfigurationProperties(prefix = "oci")
public class OciS3Prop {

    /**
     * s3秘密访问密钥
     */
    private String secretAccessKey;

    /**
     * s3访问密钥id
     */
    private String accessKeyId;

    /**
     * s3 bucket
     */
    private String bucketName;

    /**
     * 地区
     */
    private String regions;

    /**
     * 空间
     */
    private String namespace;


    /**
     * oci 配置文件位置
     */
    private String ociConfigFilePath;

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
