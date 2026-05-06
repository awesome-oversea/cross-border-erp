package com.aidotnet.erp.iam.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * IAM域Web MVC配置
 * <p>
 * 描述: 注册IAM认证拦截器，拦截/api/iam/**路径下的所有请求。
 *       拦截器负责JWT令牌校验和权限检查。
 * </p>
 *
 * @author ERP系统
 */
@Configuration
public class IamWebMvcConfig implements WebMvcConfigurer {

    private final IamAuthInterceptor iamAuthInterceptor;

    public IamWebMvcConfig(IamAuthInterceptor iamAuthInterceptor) {
        this.iamAuthInterceptor = iamAuthInterceptor;
    }

    /** 注册IAM认证拦截器，拦截路径: /api/iam/** */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(iamAuthInterceptor).addPathPatterns("/api/iam/**");
    }
}
