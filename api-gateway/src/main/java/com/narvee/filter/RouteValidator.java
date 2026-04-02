//
//package com.narvee.filter;
//
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//
//import java.util.function.Predicate;
//
//@Component
//public class RouteValidator {
//
//    public Predicate<ServerHttpRequest> isSecured = request -> {
//        String path = request.getURI().getPath();
//        return !(path.startsWith("/auth/") ||
//                 path.startsWith("/platform/register") ||
//                 path.startsWith("/platform/customer") ||
//                 path.startsWith("/platform/bp") ||
//                 path.startsWith("/platform/cp") ||
//                 path.startsWith("/tenant/register") ||
//                 path.startsWith("/tenant/send-otp") ||
//                 path.startsWith("/tenant/verify-otp") ||
//                 path.startsWith("/partner/register") ||
//                 path.startsWith("/partner/verify-otp") ||
//                 path.contains("/v3/api-docs") ||
//                 path.contains("/swagger-ui") ||
//                 path.contains("/webjars") ||
//                 path.contains("/swagger-resources") ||
//                 path.startsWith("/actuator") ||
//                 path.startsWith("/eureka") ||
//                 path.startsWith("/fallback") ||
//                 path.startsWith("/tenant/internal/"));
//    };
//}
package com.narvee.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class RouteValidator {

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        return !(
            // Product Owner auth
            path.equals("/platform/po/register") ||
            path.equals("/platform/po/send-otp") ||
            path.equals("/platform/po/verify-otp") ||
            // Tenant auth
            path.equals("/platform/tenant/register") ||
            path.equals("/platform/tenant/send-otp") ||
            path.equals("/platform/tenant/verify-otp") ||
            // Partner auth
            path.equals("/platform/partner/register") ||
            path.equals("/platform/partner/verify-otp") ||
            // Internal endpoints
            path.startsWith("/platform/tenant/internal/") ||
            // Swagger / docs
            path.contains("/v3/api-docs") ||
            path.contains("/swagger-ui") ||
            path.contains("/webjars") ||
            path.contains("/swagger-resources") ||
            // Infrastructure
            path.startsWith("/actuator") ||
            path.startsWith("/eureka") ||
            path.startsWith("/fallback")
        );
    };
}