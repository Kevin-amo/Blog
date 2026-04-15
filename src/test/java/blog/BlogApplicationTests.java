package blog;

import blog.service.SensitiveWordService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class BlogApplicationTests {

    @Resource
    private SensitiveWordService sensitiveWordService;

    private static final Logger log = LoggerFactory.getLogger(BlogApplicationTests.class);

    @Test
    void Demo() {
        final String message = "我操你妈，傻逼走一梦，傻逼王家豪";
        String replaced = sensitiveWordService.replace(message);
        System.out.println(replaced);
    }

}
