package net.detalk.api.support.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.AppProperties;
import net.detalk.api.support.util.CookieUtil;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static net.detalk.api.support.Constant.COOKIE_ACCESS_TOKEN;
import static net.detalk.api.support.Constant.COOKIE_REFRESH_TOKEN;
import static net.detalk.api.support.security.OAuth2AuthorizationRequestRepository.REDIRECT_URI_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtOAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final AppProperties appProperties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final Environment env;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {

        OAuthUser oAuth2User = (OAuthUser) authentication.getPrincipal();

        boolean secure = !Arrays.asList(env.getActiveProfiles()).contains("dev");

        ResponseCookie accessTokenCookie = ResponseCookie
            .from(COOKIE_ACCESS_TOKEN, oAuth2User.getAccessToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite("Lax")
            .path("/")
            .build();

        ResponseCookie refreshTokenCookie = ResponseCookie
            .from(COOKIE_REFRESH_TOKEN, oAuth2User.getRefreshToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite("Lax")
            .path("/")
            .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        String redirectUrl = CookieUtil.getCookie(REDIRECT_URI_COOKIE_NAME, request)
            .map(cookie -> URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8))
            .orElse(appProperties.getBaseUrl());

        String finalRedirectUrl = redirectUrl + "?ok=true" + "&access-token=" + oAuth2User.getAccessToken();

        redirectStrategy.sendRedirect(request, response, finalRedirectUrl);
    }
}
