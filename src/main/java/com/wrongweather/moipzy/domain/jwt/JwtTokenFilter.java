//package com.wrongweather.moipzy.domain.jwt;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Map;
//
//@RequiredArgsConstructor
//@Slf4j
//public class JwtTokenFilter extends OncePerRequestFilter {
//    private final JwtTokenUtil jwtTokenUtil;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//
//        String requestPath = request.getRequestURI();
//
//        System.out.println("requestPath: " + requestPath);
//
//        //해당 URI가 들어오면 filter를 적용하지 않고 넘어감
//        if(requestPath.startsWith("/moipzy/users/login") || requestPath.equals("/moipzy/users/register")
//                //|| requestPath.startsWith("/login") || requestPath.startsWith("/favicon.ico")
//                //|| requestPath.equals("/login/oauth2/code/google") || requestPath.equals("/oauth2/authorization/google")
//                || requestPath.equals("/moipzy/tomorrow-events")
//                || requestPath.equals("/moipzy/success")
//                || requestPath.startsWith("/swagger") || requestPath.startsWith("/swagger-ui") || requestPath.startsWith("/v3/api-docs")) {
//            //System.out.println("requestPath that does not apply filter: " + requestPath);
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        //다른 URI가 들어올 경우 필터 작동
//        String authorizationHeader = request.getHeader("Authorization");
//
//        //Header 의 Authorization 값이 비어있으면 Jwt Token을 전송하지 않은 것이므로 로그인 하지 않은 것임
//        //Header 의 Authorization 값이 Bearer로 시작하지 않으면 잘못된 토큰임
//        if(authorizationHeader != null || authorizationHeader.startsWith("Bearer ")) {
//            String token = authorizationHeader.substring(7);
//
//            try {
//                Map<String, Object> claims = jwtTokenUtil.extractClaims(token);
//
//                request.setAttribute("userId", claims.get("userId"));
//                request.setAttribute("email", claims.get("email"));
//                request.setAttribute("name", claims.get("name"));
//
//                filterChain.doFilter(request, response);
//                return;
//            } catch (Exception e) {
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
//                return;
//            }
//        }
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT token required");
//    }
//}
