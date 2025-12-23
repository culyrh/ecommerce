package ecommerce.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip logging for certain paths
        String requestURI = httpRequest.getRequestURI();
        if (shouldSkipLogging(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap request and response for caching
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log request/response
            logRequestResponse(requestWrapper, responseWrapper, duration);

            // Copy response back
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestResponse(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long duration
    ) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        int status = response.getStatus();

        String fullUri = queryString != null ? uri + "?" + queryString : uri;

        log.info("[{}] {} - Status: {} - Duration: {}ms",
                method, fullUri, status, duration);

        // Log error responses
        if (status >= 400) {
            log.error("[ERROR] {} {} - Status: {} - Duration: {}ms",
                    method, fullUri, status, duration);
        }
    }

    private boolean shouldSkipLogging(String uri) {
        return uri.startsWith("/swagger-ui") ||
                uri.startsWith("/v3/api-docs") ||
                uri.startsWith("/favicon.ico") ||
                uri.equals("/health");
    }
}