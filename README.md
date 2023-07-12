# S3-Util

分布式存储工具类：Amazon S3、Oracle Object Storage、Minio

## Amazon S3

### AmazonS3ClientUtil Amazon S3 连接 工具:

> 平台：支持aws平台,Oracle平台
>
> 连接方式： AWS静态凭证、ec2容器凭据、Oracle连接

### AmazonS3Utils Amazon s3 工具类

支持功能：预签名上传文件, 上传文件, 分段上传文件, 删除文件, 克隆文件等功能
> 注意: Amazon s3 工具类 在生成 预签名上传URL
> oracle平台是不支持跨域问题的,如果需要在Oracle平台支持跨域问题需要用Oracle平台原生的功能进行上传, `OciS3Utils`已支持跨域问题

## Oracle Object Storage S3
### OciS3Utils oci Object Storage 版本工具类

支持功能：预签名上传文件, 上传文件, 删除文件, 克隆文件等功能

>注意：使用该工具类进行操作Oracle平台的对象,进行连接是需要配置文件的
> 
> 参考文档：https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdkgettingstarted.htm

## Minio

### MinioUtils minio工具类
支持功能： Minio连接, Bucket创建、删除、查看信息; 文件上传、复制、删除 展示文件列表等功能 

## 配置说明

找到`src/main/resources/application.yml`,更改以下配置后, 启动`src/main/java/com/ukayunnuo/S3App.java` 启动类文件
```yaml

# 项目启动端口
server:
  port: 8080
  
# minio配置
minio:
  endpoint: http://localhost:9000 #默认端口9000
  access-key: minioadmin #默认用户名
  secret-key: minioadmin #默认密码
  bucket-name: backFileName #桶名
  cdn-prefix: cdn前缀

# aws S3配置
aws:
  secret-access-key: s3秘密访问密钥
  access-key-id: s3访问密钥id
  bucket-name: 桶名
  regions: 地区
  cdn-prefix: cdn前缀

# oracle oci S3配置
oci:
  secret-access-key: s3秘密访问密钥
  access-key-id: s3访问密钥id
  bucket-name: 桶名
  regions: 地区
  cdn-prefix: cdn前缀
  namespace: namespace
  configFilePath: 配置文件位置

```
