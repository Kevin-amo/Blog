package blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author admin
 *
 * OSS 配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOssConfig {

    /**
     * OSS 节点地址
     */
    private String endpoint;

    /**
     * bucket 名称
     */
    private String bucketName;

    /**
     * 文件访问域名
     */
    private String domain;

    /**
     * AccessKeyId
     */
    private String accessKeyId;

    /**
     * AccessKeySecret
     */
    private String accessKeySecret;

}
