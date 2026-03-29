package blog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author admin
 */
public interface OssService {

    /**
     * 上传用户头像到 OSS。
     *
     * @param file   头像文件
     * @param userId 当前用户ID
     * @return 上传结果（对象Key + 可访问URL）
     */
    UploadResult uploadAvatar(MultipartFile file, Long userId);

    /**
     * 根据对象Key删除 OSS 文件。
     *
     * @param objectKey OSS对象Key
     */
    void deleteObject(String objectKey);

    /**
     * 根据文件URL删除 OSS 文件（仅处理本项目可识别域名）。
     *
     * @param fileUrl 文件URL
     */
    void deleteByUrl(String fileUrl);

    /**
     * OSS 上传结果。
     *
     * @param objectKey OSS对象Key
     * @param fileUrl   可访问URL
     */
    record UploadResult(String objectKey, String fileUrl) {
    }
}
