package com.energymeter.apigateway.filter;

import com.energymeter.apigateway.exception.ApplicationRuntimeException;
import com.energymeter.apigateway.security.JwtUtil;
import com.energymeter.apigateway.security.Role;
import com.energymeter.apigateway.security.RouteHandler;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalSecurityFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        var request = exchange.getRequest();
        if (RouteHandler.isUriSecured.test(request.getURI().getPath())) {
            var authorizationToken = getAuthorizationToken(request.getHeaders());
            checkTokenValidation(authorizationToken);
            var role = checkTokenAccess(authorizationToken, request.getURI().getPath());

            request = exchange.getRequest().mutate()
                    .header("role", role)
                    .header("userId", JwtUtil.extractUserId(authorizationToken))
                    .header("username", JwtUtil.extractUsername(authorizationToken))
                    .build();
        }
        return chain.filter(exchange.mutate().request(request).build());
    }

    private String getAuthorizationToken(HttpHeaders headers) {
        var authorizationHeaders = headers.get(HttpHeaders.AUTHORIZATION);
        if (ObjectUtils.isEmpty(authorizationHeaders)
                || ObjectUtils.isEmpty(authorizationHeaders.get(0))
                || !authorizationHeaders.get(0).startsWith("Bearer "))
            throw new ApplicationRuntimeException("User Is Not Authenticated!");
        return authorizationHeaders.get(0).substring(7);
    }

    private void checkTokenValidation(String jwtToken) {
        if (!JwtUtil.validateToken(jwtToken))
            throw new ApplicationRuntimeException("User Authentication Token Is Not Valid!");
    }

    private String checkTokenAccess(String authorizationToken, String requestedPath) {
        var role = JwtUtil.extractRole(authorizationToken);
        if (ObjectUtils.isEmpty(role) || isAccessRestricted(role, requestedPath))
            throw new ApplicationRuntimeException("Access Denied!");
        return role;
    }

    private boolean isAccessRestricted(String role, String path) {
        return switch (Role.valueOf(role)) {
            case USER -> RouteHandler.isSecuredForUser.test(path);
            case ADMIN -> RouteHandler.isSecuredForAdmin.test(path);
        };
    }

}
