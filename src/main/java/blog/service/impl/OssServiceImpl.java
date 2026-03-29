package blog.service.impl;

import blog.config.AliOssConfig;
import blog.service.OssService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * OSS 文件服务实现（头像上传/删除）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    /**
     * 头像最大大小：5MB。
     */
    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024L * 1024L;

    /**
     * 对象目录中的月份格式：yyyyMM。
     */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 允许的扩展名。
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    /**
     * 允许的 MIME 类型。
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private final AliOssConfig aliOssConfig;

    /**
     * 上传头像并返回对象Key与完整URL。
     */
    @Override
    public UploadResult uploadAvatar(MultipartFile file, Long userId) {
        validateFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String contentType = normalizeContentType(file.getContentType());
        validateMimeMatchesExtension(extension, contentType);

        String objectKey = buildAvatarObjectKey(userId, extension);
        putObject(objectKey, file, contentType);
        return new UploadResult(objectKey, buildFileUrl(objectKey));
    }

    /**
     * 根据对象Key删除文件。
     */
    @Override
    public void deleteObject(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }

        OSS ossClient = createClient();
        try {
            ossClient.deleteObject(aliOssConfig.getBucketName(), objectKey);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 根据URL删除文件（仅处理本项目管理的域名）。
     */
    @Override
    public void deleteByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        String objectKey = extractManagedObjectKey(fileUrl);
        if (!StringUtils.hasText(objectKey)) {
            return;
        }

        deleteObject(objectKey);
    }

    /**
     * 校验头像文件基础规则：非空、大小、扩展名、MIME。
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择头像文件");
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new RuntimeException("头像大小不能超过 5MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("头像仅支持 jpg/jpeg/png 格式");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("头像文件类型不支持");
        }
    }

    /**
     * 解析文件扩展名并统一转小写。
     */
    private String getFileExtension(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (!StringUtils.hasText(extension)) {
            throw new RuntimeException("头像文件缺少扩展名");
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    /**
     * 标准化 MIME 类型（去掉参数并转小写）。
     */
    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw new RuntimeException("头像文件缺少 MIME 类型");
        }

        String normalized = contentType.toLowerCase(Locale.ROOT).trim();
        int separatorIndex = normalized.indexOf(";");
        if (separatorIndex > -1) {
            normalized = normalized.substring(0, separatorIndex).trim();
        }
        return normalized;
    }

    /**
     * 校验扩展名与 MIME 是否匹配，避免伪造文件类型。
     */
    private void validateMimeMatchesExtension(String extension, String contentType) {
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
                return;
            }
            throw new RuntimeException("头像扩展名与文件类型不匹配");
        }

        if ("png".equals(extension) && "image/png".equals(contentType)) {
            return;
        }

        throw new RuntimeException("头像扩展名与文件类型不匹配");
    }

    /**
     * 生成头像对象Key：avatar/{userId}/{yyyyMM}/{uuid}.{ext}
     */
    private String buildAvatarObjectKey(Long userId, String extension) {
        String month = LocalDate.now().format(MONTH_FORMATTER);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        return "avatar/" + userId + "/" + month + "/" + fileName;
    }

    /**
     * 执行 OSS 上传。
     */
    private void putObject(String objectKey, MultipartFile file, String contentType) {
        OSS ossClient = createClient();
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            ossClient.putObject(aliOssConfig.getBucketName(), objectKey, inputStream, metadata);
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 创建 OSS 客户端并校验必要配置。
     */
    private OSS createClient() {
        String endpoint = aliOssConfig.getEndpoint();
        String accessKeyId = aliOssConfig.getAccessKeyId();
        String accessKeySecret = aliOssConfig.getAccessKeySecret();
        String bucketName = aliOssConfig.getBucketName();
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(accessKeyId)
                || !StringUtils.hasText(accessKeySecret) || !StringUtils.hasText(bucketName)) {
            throw new RuntimeException("阿里云 OSS 配置不完整");
        }
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 生成可访问 URL；domain 不带协议时自动补 https://。
     */
    private String buildFileUrl(String objectKey) {
        String domain = aliOssConfig.getDomain();
        if (!StringUtils.hasText(domain)) {
            String endpointHost = extractHost(aliOssConfig.getEndpoint());
            if (!StringUtils.hasText(endpointHost)) {
                throw new RuntimeException("阿里云 OSS 域名配置缺失");
            }
            domain = aliOssConfig.getBucketName() + "." + endpointHost;
        }

        String normalizedDomain = ensureScheme(domain.trim());
        while (normalizedDomain.endsWith("/")) {
            normalizedDomain = normalizedDomain.substring(0, normalizedDomain.length() - 1);
        }
        return normalizedDomain + "/" + objectKey;
    }

    /**
     * 从 URL 中提取对象Key，仅在域名属于本项目管理范围时才返回。
     */
    private String extractManagedObjectKey(String fileUrl) {
        URI uri;
        try {
            uri = URI.create(ensureScheme(fileUrl.trim()));
        } catch (Exception e) {
            return null;
        }

        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            return null;
        }

        Set<String> managedHosts = buildManagedHosts();
        if (!managedHosts.contains(host.toLowerCase(Locale.ROOT))) {
            return null;
        }

        String path = uri.getPath();
        if (!StringUtils.hasText(path) || "/".equals(path)) {
            return null;
        }

        String objectKey = path.startsWith("/") ? path.substring(1) : path;
        return URLDecoder.decode(objectKey, StandardCharsets.UTF_8);
    }

    /**
     * 构建“允许删除”的域名集合（自定义域名、endpoint域名、bucket域名）。
     */
    private Set<String> buildManagedHosts() {
        Set<String> hosts = new HashSet<>();
        String domainHost = extractHost(aliOssConfig.getDomain());
        if (StringUtils.hasText(domainHost)) {
            hosts.add(domainHost);
        }

        String endpointHost = extractHost(aliOssConfig.getEndpoint());
        if (StringUtils.hasText(endpointHost)) {
            hosts.add(endpointHost);
            if (StringUtils.hasText(aliOssConfig.getBucketName())) {
                hosts.add((aliOssConfig.getBucketName() + "." + endpointHost).toLowerCase(Locale.ROOT));
            }
        }
        return hosts;
    }

    /**
     * 提取配置值中的主机名。
     */
    private String extractHost(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            URI uri = URI.create(ensureScheme(value.trim()));
            if (StringUtils.hasText(uri.getHost())) {
                return uri.getHost().toLowerCase(Locale.ROOT);
            }
        } catch (Exception ignored) {
            // URI 解析失败时继续走字符串兜底逻辑。
        }

        String host = value.trim();
        int slashIndex = host.indexOf("/");
        if (slashIndex > -1) {
            host = host.substring(0, slashIndex);
        }
        int colonIndex = host.indexOf(":");
        if (colonIndex > -1) {
            host = host.substring(0, colonIndex);
        }

        return StringUtils.hasText(host) ? host.toLowerCase(Locale.ROOT) : null;
    }

    /**
     * 若未配置协议，默认补 https://。
     */
    private String ensureScheme(String value) {
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return "https://" + value;
    }
}
