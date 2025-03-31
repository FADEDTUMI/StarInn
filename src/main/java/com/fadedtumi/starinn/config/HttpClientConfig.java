/**
 * @作者: FADEDTUMI
 * @版本: 3.3.0
 * @创建时间: 2025-03-31
 * @修改记录:FADEDTUMI
 * -----------------------------------------------------------------------------
 * 版权声明: 本代码归FADEDTUMI所有，任何形式的商业使用需要获得授权
 * 项目主页: https://github.com/FADEDTUMI/SillytavernAccount
 * 赞助链接: https://afdian.com/a/FadedTUMI
 */
package com.fadedtumi.starinn.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 使用默认的SSL配置，而不是接受所有证书
        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .build()
                )
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        factory.setConnectTimeout(5000); // 5秒连接超时
        factory.setReadTimeout(30000);   // 30秒读取超时

        return new RestTemplate(factory);
    }
}