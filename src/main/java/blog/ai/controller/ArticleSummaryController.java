package blog.ai.controller;

import blog.ai.summary.ArticleSummaryService;
import blog.ai.summary.dto.ArticleSummaryDTO;
import blog.model.LoginUser;
import blog.util.PermissionUtil;
import blog.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 文章 AI 摘要接口。
 */
@RestController
@RequestMapping("/ai/article-summary")
@RequiredArgsConstructor
@Tag(name = "AI 文章摘要接口")
public class ArticleSummaryController {

    private final ArticleSummaryService articleSummaryService;

    private final AsyncTaskExecutor applicationTaskExecutor;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式生成文章总结")
    public SseEmitter generateStream(@RequestBody @Valid ArticleSummaryDTO generateDTO, HttpServletRequest request) {
        PermissionUtil.requireUser();
        LoginUser loginUser = UserContext.getUser();
        String clientIp = resolveClientIp(request);
        SseEmitter emitter = new SseEmitter(60000L);

        applicationTaskExecutor.execute(() -> {
            UserContext.setUser(loginUser);
            try {
                articleSummaryService.summaryStream(generateDTO, clientIp,
                        chunk -> sendEvent(emitter, "chunk", chunk));
                sendEvent(emitter, "done", "[DONE]");
                emitter.complete();
            } catch (Exception e) {
                sendEvent(emitter, "error", e.getMessage());
                emitter.complete();
            } finally {
                UserContext.clear();
            }
        });

        return emitter;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void sendEvent(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
