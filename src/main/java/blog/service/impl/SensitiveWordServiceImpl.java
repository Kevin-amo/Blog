package blog.service.impl;

import blog.service.SensitiveWordService;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Resource
    private SensitiveWordBs sensitiveWordBs;

    @Override
    public boolean contains(String text) {
        // 如果 text 为空
        if (text == null || text.trim().isEmpty()) {
            // 无违禁词，返回false
            return false;
        }
        // 有违禁词
        return sensitiveWordBs.contains(text);
    }

    @Override
    public List<String> findAll(String text) {
        // 判空
        if (text == null || text.trim().isEmpty()) {
            // 返回空列表
            return Collections.emptyList();
        }
        // 返回所有违禁词
        return sensitiveWordBs.findAll(text);
    }

    @Override
    public String replace(String text) {
        // 如果 text 为空
        if (text == null || text.trim().isEmpty()) {
            // 无违禁词，返回text
            return text;
        }
        return sensitiveWordBs.replace(text);
    }
}
