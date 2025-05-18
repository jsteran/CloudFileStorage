package dev.anton_kulakov.config;

import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User userEntity = new User();
        userEntity.setId(customUser.id());
        userEntity.setUsername(customUser.username());
        userEntity.setPassword(customUser.password());

        SecurityUser securityUser = new SecurityUser(userEntity);

        List<GrantedAuthority> authorities = new ArrayList<>(
                Arrays.stream(customUser.roles())
                        .map(role -> "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );

        authorities.addAll(
                Arrays.stream(customUser.authorities())
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityUser,
                securityUser.getPassword(),
                authorities);

        context.setAuthentication(authentication);

        return context;
    }
}
