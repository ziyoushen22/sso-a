package com.example.ssoa.constants;

import com.example.ssoa.model.TokenInfo;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class DemoAConstants {

    private Map<String, TokenInfo> sessionIdTokenMap=new ConcurrentHashMap<>();

    public final String DEFAULT_CHARSET="UTF-8";

}
