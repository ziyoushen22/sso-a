package com.example.ssoa.configuration;

import lombok.Data;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Data
public class ErrorPageConfig implements ErrorPageRegistrar {
    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage e404 = new ErrorPage(HttpStatus.NOT_FOUND, "/errorpage/404.jsp");
        ErrorPage e403 = new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/errorpage/403.jsp");
        ErrorPage e500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/errorpage/500.jsp");
        registry.addErrorPages(e404, e403, e500);
    }
}
