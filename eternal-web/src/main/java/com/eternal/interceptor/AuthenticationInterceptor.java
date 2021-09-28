package com.eternal.interceptor;


import com.eternal.common.annotation.PassToken;
import com.eternal.common.web.domain.AjaxResult;
import com.eternal.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;


/**
 * token验证拦截
 */
@Slf4j

public class AuthenticationInterceptor implements HandlerInterceptor {


    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }

        String tokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (tokenHeader == null || "".equals(tokenHeader)) {
            log.info("token not found");
            //throw new Exception("Error : token not found.");
            result(response,
                   " {\"code\":\"400\",\"msg\":\"Error : token not found.\"}"
                    );
        }
        assert tokenHeader != null;
        if (!tokenHeader.startsWith("Bearer")) {
            log.info("invalid token");
            //throw new Exception("Error : invalid token.");
            result(response,
                    " {\"code\":\"400\",\"msg\":\"Error : invalid token.\"}"
            );
        }
        String token = tokenHeader.replace("Bearer", "").trim();
        Boolean checkToken = userService.checkToken(token);
        if(!checkToken){
            //throw new Exception("Error : The token has expired. ");
            result(response,
                    " {\"code\":\"400\",\"msg\":\"Error : The token has expired. \"}"
            );
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // TODO Auto-generated method stub
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // TODO Auto-generated method stub
    }

    private void result(HttpServletResponse response, String json)  {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
            log.error("response error",e);
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
