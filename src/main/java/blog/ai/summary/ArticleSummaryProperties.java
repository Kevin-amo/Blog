package blog.ai.summary;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * AI 摘要限流与缓存配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.ai.summary")
public class ArticleSummaryProperties {

    /**
     * 用户维度限流窗口。
     */
    private Duration userWindow = Duration.ofMinutes(1);

    /**
     * 用户维度窗口内最大请求数。
     */
    private int userWindowMaxRequests = 3;

    /**
     * IP 维度限流窗口。
     */
    private Duration ipWindow = Duration.ofMinutes(1);

    /**
     * IP 维度窗口内最大请求数。
     */
    private int ipWindowMaxRequests = 10;

    /**
     * 摘要缓存有效期。
     */
    private Duration cacheTtl = Duration.ofHours(12);

    /**
     * 生成锁有效期，避免异常情况下锁长期遗留。
     */
    private Duration lockTtl = Duration.ofMinutes(2);

    /**
     * 非首个请求等待已在生成中的摘要结果的最长时间。
     */
    private Duration waitTimeout = Duration.ofSeconds(75);

    /**
     * 轮询缓存结果的时间间隔。
     */
    private Duration waitPollInterval = Duration.ofMillis(300);
}