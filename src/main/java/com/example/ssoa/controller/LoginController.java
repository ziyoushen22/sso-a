package com.example.ssoa.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.ssoa.constants.DemoAConstants;
import com.example.ssoa.model.TokenInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Data
@Slf4j
public class LoginController {

    @Value("${sso.path}")
    private String ssoPath;
    @Value("${sso.exit}")
    private String ssoExit;

    @Autowired
    private DemoAConstants demoAConstants;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 接收页面发出的注销请求，转发给sso
     *
     * @param request
     */
    @RequestMapping("/exit")
    public void exit(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        Map<String, TokenInfo> sessionIdTokenMap = demoAConstants.getSessionIdTokenMap();
        String token = sessionIdTokenMap.get(sessionId).getToken();
        JSONObject json = new JSONObject();
        json.put("token", token);
        URI uri = UriComponentsBuilder.fromHttpUrl(ssoPath).path(ssoExit).build(true).toUri();
        restTemplate.postForObject(uri, json, String.class);
        log.info("ssoa 发送注销消息成功");
    }

    /**
     * 接收sso发来的注销请求
     *
     * @param jsonObject
     */
    @RequestMapping("/logout")
    public void logout(@RequestBody JSONObject jsonObject) {
        String sessionId = jsonObject.getString("sessionId");
        Map<String, TokenInfo> sessionIdTokenMap = demoAConstants.getSessionIdTokenMap();
        sessionIdTokenMap.remove(sessionId);
        log.info("ssoa 注销成功");
    }

    /**
     * 页面初始化，获取姓名
     *
     * @param request
     * @return
     */
    @RequestMapping("/getUserName")
    public JSONObject getUserName(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        String sessionId = request.getSession().getId();
        Map<String, TokenInfo> sessionIdTokenMap = demoAConstants.getSessionIdTokenMap();
        TokenInfo tokenInfo = sessionIdTokenMap.get(sessionId);
        if (ObjectUtils.isNotEmpty(tokenInfo)) {
            jsonObject.put("username", tokenInfo.getUsername());
        }
        return jsonObject;
    }


}
