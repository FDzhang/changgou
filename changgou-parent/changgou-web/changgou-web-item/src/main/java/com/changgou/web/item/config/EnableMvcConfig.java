package com.changgou.web.item.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ：zxq
 * @date ：Created in 2020/8/1 18:08
 */
@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {
    /***
     * 静态资源放行
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/items/**")
                .addResourceLocations("classpath:/templates/items/");
    }
}
