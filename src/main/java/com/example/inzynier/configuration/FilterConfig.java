package com.example.inzynier.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RoleAccessFilter> roleBasedAccessFilter() {
        FilterRegistrationBean<RoleAccessFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RoleAccessFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("RoleBasedAccessFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
