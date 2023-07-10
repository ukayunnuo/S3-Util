package com.ukayunnuo.s3.minio;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * minio 连接配置
 *
 * @author ukayunnuo
 * @since 1.0.0
 */
@Slf4j
public class MinioClientConfig {

    @Resource
    private MinioProp minioProp;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProp.getEndpoint())
                .credentials(minioProp.getAccesskey(), minioProp.getSecretkey())
                .build();
    }

}
