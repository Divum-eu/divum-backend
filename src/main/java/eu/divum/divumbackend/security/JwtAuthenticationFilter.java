package eu.divum.divumbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserJwtService userJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        String accessToken = null;
        String authHeader = request.getHeader("Authorization");
        String wsProtocolHeader = request.getHeader("Sec-WebSocket-Protocol");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.replace("Bearer ", "");
        } else if (wsProtocolHeader != null && !wsProtocolHeader.isEmpty()) {
            accessToken = wsProtocolHeader;
            response.setHeader("Sec-WebSocket-Protocol", accessToken);
        }

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserJwt jwt = userJwtService.parseToken(accessToken);

        if (jwt == null || jwt.isExpired()) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                jwt.getUserId(),
                null,
                Collections.emptyList()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
