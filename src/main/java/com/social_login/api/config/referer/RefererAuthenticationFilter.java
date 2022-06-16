package com.social_login.api.config.referer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.social_login.api.config.exception.RefererException;

import org.springframework.web.filter.OncePerRequestFilter;

public class RefererAuthenticationFilter extends OncePerRequestFilter {
    // TODO :: 배포 후 도메인 추가
    final static List<String> refererWhiteList = Arrays.asList(
        "http://localhost:3000"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                // GET 메소드 Referer 체크 통과
                if (request.getMethod().equals("GET")) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String referer = null;
                Matcher match = null;
                try {
                    referer = request.getHeader("Referer") != null ? request.getHeader("Referer") : null;
                    String regex = "(http|https)?:\\/\\/(localhost|(w{1,3})?.([^\\s.]*(?:\\.[a-z]+)))*(?::\\d+)?(?![^<]*(?:<\\/\\w+>|\\/?>))";
                    match = Pattern.compile(regex).matcher(referer);
                } catch (NullPointerException e) {
                    throw new RefererException("Referer not found.");
                } catch (IllegalStateException e) {
                    throw new RefererException("Referer not allowed.");
                }

                if (!match.find()) {
                    // 올바른 url 패턴이 아닌경우
                    throw new RefererException("Referer URL pattern not matched.");
                } else {
                    // referer white list에 없는 도메인인 경우
                    if (!refererWhiteList.contains(match.group())) {
                        throw new RefererException("Referer access denied.");
                    }
                }

                filterChain.doFilter(request, response);
            }
}
