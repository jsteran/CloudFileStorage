package dev.anton_kulakov.config.resolver;

import dev.anton_kulakov.model.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullPathArgumentResolver implements HandlerMethodArgumentResolver {
    private final static String USER_ROOT_FOLDER_TEMPLATE = "user-%s-files/";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(FullPath.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        SecurityUser securityUser = getSecurityUser();
        FullPath annotation = parameter.getParameterAnnotation(FullPath.class);

        if (annotation == null) {
            throw new IllegalStateException("The %s is called for a parameter without %s annotation"
                    .formatted(FullPathArgumentResolver.class.getSimpleName(), FullPath.class.getSimpleName()));
        }

        String paramName = annotation.value();
        String pathFromRequest = webRequest.getParameter(paramName);

        if (pathFromRequest == null) {
            return null;
        }

        String userRootFolder = USER_ROOT_FOLDER_TEMPLATE.formatted(securityUser.getUserId());
        Path fullPath = Paths.get(userRootFolder, pathFromRequest).normalize();

        if (!fullPath.startsWith(userRootFolder)) {
            log.warn("Path Traversal attempt detected from user {} for path {}", securityUser.getUserId(), pathFromRequest);
            throw new AccessDeniedException("Path Traversal attempt detected for path %s".formatted(pathFromRequest));
        }

        String fullPathString = fullPath.toString().replace('\\', '/');

        if (pathFromRequest.isEmpty() || pathFromRequest.endsWith("/")) {
            fullPathString += "/";
        }

        return fullPathString;
    }

    @NotNull
    private static SecurityUser getSecurityUser() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof SecurityUser)) {
            throw new AccessDeniedException("Invalid principal type. Expected %s".formatted(SecurityUser.class.getSimpleName()));
        }

        return (SecurityUser) principal;
    }
}
