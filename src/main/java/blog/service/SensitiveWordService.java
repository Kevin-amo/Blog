package blog.service;

import java.util.List;

/**
 * 敏感词接口
 */
public interface SensitiveWordService {

    /**
     * 是否包含敏感词
     */
    boolean contains(String text);

    /**
     * 找出所有敏感词
     */
    List<String> findAll(String text);

    /**
     * 用 * 替换敏感词
     */
    String replace(String text);

}
