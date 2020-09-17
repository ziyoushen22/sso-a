package com.example.ssoa.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.example.ssoa.constants.DemoAConstants;
import com.example.ssoa.model.TokenInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Slf4j
@Data
@Component
public class UserSecurityInterceptor implements HandlerInterceptor {

    @Value("${sso.path}")
    private String ssoPath;
    @Value("${sso.isLogin}")
    private String ssoIsLogin;
    @Value("${sso.login}")
    private String ssoLogin;
    @Value("${sso.tokenCheck}")
    private String tokenCheck;
    @Value("${sso.sysName}")
    private String sysName;
    @Value("${hostAddress.ip}")
    private String hostAddress;
    @Value("${hostAddress.exitUrl}")
    private String exitUrl;

    @Autowired
    private DemoAConstants demoAConstants;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        if (!(handler instanceof HandlerMethod)){
//            return true;
//        }
        Map<String, TokenInfo> sessionIdTokenMap = demoAConstants.getSessionIdTokenMap();
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        log.info("ssoa sessionId: " + sessionId);

        //该子系统刚登录过，判断带过来的token
        String token = request.getParameter("tokenFromSSO");
        if (StringUtils.isNotBlank(token)) {
            //子系统首次登录，设置局部会话
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            sessionIdTokenMap.put(sessionId, new TokenInfo(token, username, password));
            //令牌校验
            tokenCheck(request, response, token, sessionId, username, password);
        }
        //别的子系统登录过
        String isLogin = request.getParameter("isLogin");
        if (StringUtils.isNotBlank(isLogin)) {
            String isLoginToken = request.getParameter("isLoginToken");
            String isLoginUsername = request.getParameter("isLoginUsername");
            String isLoginPassword = request.getParameter("isLoginPassword");
            sessionIdTokenMap.put(sessionId, new TokenInfo(isLoginToken, isLoginUsername, isLoginPassword));
            return true;
        }
        //检查过token
        String isChecked = request.getParameter("isChecked");
        if (StringUtils.isNotBlank(isChecked)) {
            return true;
        }
        //先判断本地缓存，如果没有则去sso判断是否有别的子系统登录过，有则加入本子系统缓存
        if (sessionIdTokenMap.containsKey(sessionId)) {
            return true;
        } else {
            String redirectUrl = ssoPath + ssoIsLogin;
            redirectUrl += "?service=" + request.getRequestURL().toString();
            redirectUrl += "&sysName=" + sysName;
            redirectUrl += "&hostAddress" + hostAddress;
            redirectUrl += "&exitUrl=" + exitUrl;
            redirectUrl += "&sessionId=" + sessionId;
            log.info("isLogin: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        }
        return true;
    }

    private void tokenCheck(HttpServletRequest request, HttpServletResponse response, String token, String sessionId, String username, String password) throws ServletException, IOException {
        JSONObject json = new JSONObject();
        json.put("service", request.getRequestURL().toString());
        json.put("tokenCheck", token);
        json.put("hostAddress", hostAddress);
        json.put("exitUrl", exitUrl);
        json.put("username", username);
        json.put("password", password);
        json.put("sessionId", sessionId);
        String requestParam = json.toJSONString();
        URI uri = UriComponentsBuilder.fromHttpUrl(ssoPath).path(tokenCheck).build(true).toUri();
        RequestEntity<String> requestEntity = RequestEntity.post(uri).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).body(requestParam);
        restTemplate.exchange(requestEntity, JSONObject.class);
    }

}
