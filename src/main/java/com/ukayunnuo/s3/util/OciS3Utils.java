package com.ukayunnuo.s3.util;


import cn.hutool.core.io.FileUtil;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.model.PreauthenticatedRequest;
import com.oracle.bmc.objectstorage.model.RenameObjectDetails;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.*;
import com.ukayunnuo.s3.oci.OciS3Prop;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * oci Object Storage 版本工具类
 *
 * @author yunnuo
 * @since 1.0.0
 */
public class OciS3Utils {

    public static final String PRE_URL = "https://objectstorage.<region_ID>.oraclecloud.com<access-uri><objectName>";


    /**
     * 获取默认client
     *
     * @return {@link ObjectStorageClient}
     * @throws IOException ioexception
     */
    public static ObjectStorageClient getDefaultClient() throws IOException {
        final ConfigFileAuthenticationDetailsProvider provider = getProvider(ConfigFileReader.parseDefault());
        return getClient(provider);
    }

    /**
     * 获取client
     *
     * @param configFilePath 配置文件路径
     * @return {@link ObjectStorageClient}
     * @throws IOException ioexception
     */
    public static ObjectStorageClient getClient(String configFilePath) throws IOException {
        final ConfigFileAuthenticationDetailsProvider provider = getProvider(configFilePath);
        return getClient(provider);
    }

    /**
     * 获取client
     *
     * @param configFile 配置文件
     * @return {@link ObjectStorageClient}
     */
    public static ObjectStorageClient getClient(ConfigFileReader.ConfigFile configFile) {
        final ConfigFileAuthenticationDetailsProvider provider = getProvider(configFile);
        return getClient(provider);
    }

    /**
     * 获取client
     *
     * @param provider 提供者
     * @return {@link ObjectStorageClient}
     */
    public static ObjectStorageClient getClient(ConfigFileAuthenticationDetailsProvider provider) {
        return new ObjectStorageClient(provider);
    }


    /**
     * 获取提供者
     *
     * @param configFilePath 配置文件路径
     * @return {@link ConfigFileAuthenticationDetailsProvider}
     * @throws IOException ioexception
     */
    public static ConfigFileAuthenticationDetailsProvider getProvider(String configFilePath) throws IOException {
        final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(configFilePath);
        return getProvider(configFile);

    }

    /**
     * 获取提供者
     *
     * @param configFile 配置文件
     * @return {@link ConfigFileAuthenticationDetailsProvider}
     */
    public static ConfigFileAuthenticationDetailsProvider getProvider(ConfigFileReader.ConfigFile configFile) {
        return new ConfigFileAuthenticationDetailsProvider(configFile);

    }

    /**
     * 复制对象
     *
     * @param client            客户端
     * @param copyObjectRequest 复制对象请求
     * @return {@link CopyObjectResponse}
     */
    public static CopyObjectResponse copyObject(ObjectStorageClient client, CopyObjectRequest copyObjectRequest) {
        return client.copyObject(copyObjectRequest);
    }


    /**
     * 重命名对象
     *
     * @param client     客户端
     * @param namespace  名称空间
     * @param bucketName bucket名称
     * @param sourceName 原名称
     * @param newName    新名字
     * @return {@link RenameObjectResponse}
     */
    public static RenameObjectResponse renameObject(ObjectStorageClient client, String namespace, String bucketName, String sourceName, String newName) {
        RenameObjectDetails renameObjectDetails = RenameObjectDetails.builder()
                .sourceName(sourceName)
                .newName(newName).build();
        RenameObjectRequest renameObjectRequest = RenameObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .renameObjectDetails(renameObjectDetails).build();
        return client.renameObject(renameObjectRequest);
    }

    /**
     * 重命名对象
     *
     * @param client              客户端
     * @param renameObjectRequest 重命名对象请求
     * @return {@link RenameObjectResponse}
     */
    public static RenameObjectResponse renameObject(ObjectStorageClient client, RenameObjectRequest renameObjectRequest) {
        return client.renameObject(renameObjectRequest);
    }

    /**
     * 获取对象
     *
     * @param client     客户端
     * @param namespace  名称空间
     * @param bucketName bucket名称
     * @param key        key
     * @return {@link GetObjectResponse}
     */
    public static GetObjectResponse getObject(ObjectStorageClient client, String namespace, String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(key).build();
        return client.getObject(getObjectRequest);
    }

    /**
     * 获取对象
     *
     * @param client        客户端
     * @param objectRequest 对象请求
     * @return {@link GetObjectResponse}
     */
    public static GetObjectResponse getObject(ObjectStorageClient client, GetObjectRequest objectRequest) {
        return client.getObject(objectRequest);
    }

    /**
     * 删除对象
     *
     * @param client     客户端
     * @param namespace  名称空间
     * @param bucketName bucket名称
     * @param key        key
     * @return {@link DeleteObjectResponse}
     */
    public static DeleteObjectResponse deleteObject(ObjectStorageClient client, String namespace, String bucketName, String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(key).build();
        return client.deleteObject(deleteObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param client              客户端
     * @param deleteObjectRequest 删除对象请求
     * @return {@link DeleteObjectResponse}
     */
    public static DeleteObjectResponse deleteObject(ObjectStorageClient client, DeleteObjectRequest deleteObjectRequest) {
        return client.deleteObject(deleteObjectRequest);
    }

    /**
     * 预签名上传 单个文件 预授权url
     * <p color='red'> 注意: 如果需要上传多个文件到同一个目录下, 请使用{@code getPreAuthAnyObjectPathReadWriteURL} </p>
     *
     * @param client      客户端
     * @param config      配置
     * @param name        名字(主要是在Console里面起显示作用, 不允许重复)
     * @param timeExpires 过期时间
     * @param key         key
     * @return {@link String}
     */
    public static String getPreAuthAnyObjectReadWriteURL(ObjectStorageClient client, OciS3Prop config, String name, Date timeExpires, String key) {
        CreatePreauthenticatedRequestResponse response = getPreAuthAnyObjectReadWriteResponse(client, config.getBucketName(), config.getNamespace(), name, key, timeExpires);
        return getPreAuthUrl(config.getRegions(), response.getPreauthenticatedRequest());
    }


    /**
     * 预签名上传 获取预身份验证对象路径[权限: 读写] URL
     * <p color='red'>解决性能问题: prefixKey可以传一个前缀路径, 然后通过生成的url地址拼接对象名称进行上传,防止上传多个文件到同一个文件夹下申请多次url</p>
     *
     * @param client      客户端
     * @param config      配置
     * @param name        名字(主要是在Console里面起显示作用, 不允许重复)
     * @param prefixKey   前缀路径
     * @param timeExpires 过期时间
     * @return {@link String}
     */
    public static String getPreAuthAnyObjectPathReadWriteURL(ObjectStorageClient client, OciS3Prop config, String name, String prefixKey, Date timeExpires) {
        CreatePreauthenticatedRequestResponse response = getPreAuthAnyObjectReadWriteResponse(client, config.getBucketName(), config.getNamespace(), name, prefixKey, timeExpires);
        return getPreAuthUrl(config.getRegions(), response.getPreauthenticatedRequest());
    }


    /**
     * 获取预身份验证对象[权限: 读写]响应
     *
     * @param client        客户端
     * @param bucketName    bucket名称
     * @param namespaceName 名称空间名字
     * @param name          名字(主要是在Console里面起显示作用, 不允许重复)
     * @param key           key
     * @param timeExpires   过期时间
     * @return {@link CreatePreauthenticatedRequestResponse}
     */
    public static CreatePreauthenticatedRequestResponse getPreAuthAnyObjectReadWriteResponse(ObjectStorageClient client, String bucketName, String namespaceName, String name, String key, Date timeExpires) {
        CreatePreauthenticatedRequestDetails createPreauthenticatedRequestDetails = CreatePreauthenticatedRequestDetails.builder()
                .name(name)
                .bucketListingAction(PreauthenticatedRequest.BucketListingAction.ListObjects)
                .objectName(key)
                .accessType(CreatePreauthenticatedRequestDetails.AccessType.AnyObjectReadWrite)
                .timeExpires(timeExpires).build();

        CreatePreauthenticatedRequestRequest createPreauthenticatedRequestRequest = CreatePreauthenticatedRequestRequest.builder()
                .namespaceName(namespaceName)
                .bucketName(bucketName)
                .createPreauthenticatedRequestDetails(createPreauthenticatedRequestDetails).build();

        return client.createPreauthenticatedRequest(createPreauthenticatedRequestRequest);
    }


    /**
     * 上传对象
     *
     * @param config 配置
     * @param client 客户端
     * @param file   文件
     * @return {@link PutObjectResponse}
     */
    public static PutObjectResponse putObject(OciS3Prop config, ObjectStorageClient client, File file) {
        BufferedInputStream inputStream = FileUtil.getInputStream(file);
        long size = FileUtil.size(file);
        String mimeType = FileUtil.getMimeType(file.getPath());
        String name = file.getName();
        return putObject(config, client, inputStream, size, mimeType, name);
    }


    /**
     * 上传对象
     *
     * @param config 配置
     * @param client 客户端
     * @param file   文件
     * @param key    key
     * @return {@link PutObjectResponse}
     * @throws IOException ioexception
     */
    public static PutObjectResponse putObject(OciS3Prop config, ObjectStorageClient client, MultipartFile file, String key) throws IOException {
        return putObject(config, client, file.getInputStream(), file.getSize(), file.getContentType(), key);
    }

    /**
     * 上传对象
     *
     * @param config        配置
     * @param client        客户端
     * @param contentLength 内容长度
     * @param contentType   内容类型
     * @param key           key
     * @param objectContent 对象内容
     * @return {@link PutObjectResponse}
     */
    public static PutObjectResponse putObject(OciS3Prop config, ObjectStorageClient client, InputStream objectContent, Long contentLength, String contentType, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .namespaceName(config.getNamespace())
                .bucketName(config.getBucketName())
                .objectName(key)
                .putObjectBody(objectContent)
                .contentLength(contentLength)
                .contentType(contentType).build();
        return getPutObjectRequest(client, putObjectRequest);
    }

    /**
     * 获取 上传对象请求
     *
     * @param client           客户端
     * @param putObjectRequest 把对象请求
     * @return {@link PutObjectResponse}
     */
    public static PutObjectResponse getPutObjectRequest(ObjectStorageClient client, PutObjectRequest putObjectRequest) {
        return client.putObject(putObjectRequest);
    }


    /**
     * 获取预身份验证url
     *
     * @param region                  地区
     * @param preauthenticatedRequest preauthenticated请求
     * @return {@link String}
     */
    public static String getPreAuthUrl(String region, PreauthenticatedRequest preauthenticatedRequest) {
        return getPreAuthUrl(region, preauthenticatedRequest.getAccessUri(), preauthenticatedRequest.getObjectName());
    }

    /**
     * 获取预身份验证url
     *
     * @param accessUri  uri访问
     * @param objectName 对象名称
     * @param region     地区
     * @return {@link String}
     */
    public static String getPreAuthUrl(String region, String accessUri, String objectName) {
        return PRE_URL.replace("<region_ID>", region)
                .replace("<access-uri>", accessUri)
                .replace("<objectName>", objectName);
    }

    /**
     * 展示对象列表信息
     *
     * @param config 配置
     * @param client 客户端
     * @param prefix 前缀
     * @param limit  限制
     * @return {@link ListObjectsResponse}
     */
    public static ListObjectsResponse listObjects(OciS3Prop config, ObjectStorageClient client, String prefix, int limit){

        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .namespaceName(config.getNamespace())
                .bucketName(config.getBucketName())
                .prefix(prefix)
                .limit(limit).build();

        return client.listObjects(listObjectsRequest);
    }


}
