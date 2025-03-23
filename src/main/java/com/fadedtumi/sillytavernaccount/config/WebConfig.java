package com.fadedtumi.sillytavernaccount.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    /**
     * 配置消息转换器，确保使用UTF-8编码
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 清除默认的消息转换器
        converters.clear();

        // 添加String消息转换器，强制UTF-8
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);  // 避免在Content-Type中添加accept-charset参数
        converters.add(stringConverter);

        // 添加JSON消息转换器，强制UTF-8
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(jsonConverter);
    }

    /**
     * 配置内容协商，确保默认使用UTF-8
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/",
                        "classpath:/resources/", "classpath:/META-INF/resources/");
    }

    // 移除这个方法，避免与SecurityConfig中的CORS配置冲突
    // @Override
    // public void addCorsMappings(CorsRegistry registry) { ... }

    // 移除这个Bean，避免与SecurityConfig中的CORS配置冲突
    // @Bean
    // public CorsFilter corsFilter() { ... }
}