package com.energymeter.apigateway.security;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Predicate;

@Component
public class RouteHandler {

    private static final Set publiclyAvailableEndpoints = Sets.newHashSet("/identity/authentication/register", "/identity/authentication/authenticate", "/eureka");
    private static final Set<String> adminRestrictedRoutes = Sets.newHashSet();
    private static final Set<String> userRestrictedRoutes = Sets.newHashSet("/administration");

    public static Predicate<String> isUriSecured =
            path -> !publiclyAvailableEndpoints.contains(path);

    public static Predicate<String> isSecuredForAdmin =
            path -> !adminRestrictedRoutes.isEmpty() && adminRestrictedRoutes.stream().anyMatch(path::startsWith);

    public static Predicate<String> isSecuredForUser =
            path -> userRestrictedRoutes.stream().anyMatch(path::startsWith);

}
