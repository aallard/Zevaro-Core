package ai.zevaro.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component("zevaroRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestContextFilter extends OncePerRequestFilter {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            CONTEXT.set(new RequestContext(
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    request.getHeader("X-Request-ID")
            ));
            chain.doFilter(request, response);
        } finally {
            CONTEXT.remove();
        }
    }

    public static RequestContext getContext() {
        return CONTEXT.get();
    }

    public record RequestContext(String ipAddress, String userAgent, String requestId) {}
}
