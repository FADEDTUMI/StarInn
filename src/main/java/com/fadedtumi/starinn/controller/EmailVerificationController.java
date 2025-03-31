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
package com.fadedtumi.starinn.controller;

import com.fadedtumi.starinn.dto.EmailVerificationRequest;
import com.fadedtumi.starinn.entity.EmailVerification;
import com.fadedtumi.starinn.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/email/verify")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 发起邮箱验证请求，返回各邮箱服务提供商的OAuth授权链接
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestVerification(@Valid @RequestBody EmailVerificationRequest request) {
        // 创建或更新验证记录
        EmailVerification verification = emailVerificationService.createVerification(request.getEmail());

        Map<String, String> authUrls = new HashMap<>();
        String state = verification.getOauthState();

        // 生成QQ邮箱授权链接
        try {
            ClientRegistration qqReg = clientRegistrationRepository.findByRegistrationId("qq");
            if (qqReg != null) {
                String qqUrl = qqReg.getProviderDetails().getAuthorizationUri() +
                        "?client_id=" + qqReg.getClientId() +
                        "&redirect_uri=" + baseUrl + "/api/email/verify/oauth2/code/qq" +
                        "&response_type=code&scope=email,openid&state=" + state;
                authUrls.put("qq", qqUrl);
            }
        } catch (Exception e) {
            // 记录错误但不中断流程
            System.out.println("无法生成QQ邮箱授权URL: " + e.getMessage());
        }

        // 生成网易邮箱授权链接
        try {
            ClientRegistration neteaseReg = clientRegistrationRepository.findByRegistrationId("netease");
            if (neteaseReg != null) {
                String neteaseUrl = neteaseReg.getProviderDetails().getAuthorizationUri() +
                        "?client_id=" + neteaseReg.getClientId() +
                        "&redirect_uri=" + baseUrl + "/api/email/verify/oauth2/code/netease" +
                        "&response_type=code&scope=email&state=" + state;
                authUrls.put("netease", neteaseUrl);
            }
        } catch (Exception e) {
            System.out.println("无法生成网易邮箱授权URL: " + e.getMessage());
        }

        // 生成阿里邮箱授权链接
        try {
            ClientRegistration aliyunReg = clientRegistrationRepository.findByRegistrationId("aliyun");
            if (aliyunReg != null) {
                String aliyunUrl = aliyunReg.getProviderDetails().getAuthorizationUri() +
                        "?client_id=" + aliyunReg.getClientId() +
                        "&redirect_uri=" + baseUrl + "/api/email/verify/oauth2/code/aliyun" +
                        "&response_type=code&scope=email&state=" + state;
                authUrls.put("aliyun", aliyunUrl);
            }
        } catch (Exception e) {
            System.out.println("无法生成阿里邮箱授权URL: " + e.getMessage());
        }

        // 添加直接验证链接（用于开发测试或特殊场景）
        authUrls.put("direct", baseUrl + "/api/email/verify/token/" + verification.getVerificationToken());

        Map<String, Object> response = new HashMap<>();
        response.put("email", verification.getEmail());
        response.put("authUrls", authUrls);
        response.put("expiresIn", "30分钟");

        return ResponseEntity.ok(response);
    }

    /**
     * 处理OAuth回调，各邮箱服务提供商验证成功后的跳转地址
     */
    @GetMapping("/oauth2/code/{provider}")
    public RedirectView handleOAuth2Callback(
            @PathVariable String provider,
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        // 根据state查找验证记录
        Optional<EmailVerification> verification = emailVerificationService.findByOAuthState(state);

        RedirectView redirectView = new RedirectView();
        String redirectUrl = baseUrl + "/verification-result.html";

        if (verification.isPresent()) {
            EmailVerification emailVerification = verification.get();

            // 检查是否已过期
            if (emailVerificationService.isVerificationExpired(emailVerification)) {
                redirectView.setUrl(redirectUrl + "?status=expired");
                return redirectView;
            }

            // 标记为���验证
            emailVerificationService.markAsVerified(emailVerification);
            redirectView.setUrl(redirectUrl + "?status=success&email=" + emailVerification.getEmail());
        } else {
            redirectView.setUrl(redirectUrl + "?status=invalid");
        }

        return redirectView;
    }

    /**
     * 使用令牌直接验证（可用于测试或特殊情况）
     */
    @GetMapping("/token/{token}")
    public RedirectView verifyByToken(@PathVariable String token) {
        Optional<EmailVerification> verification = emailVerificationService.findByToken(token);

        RedirectView redirectView = new RedirectView();
        String redirectUrl = baseUrl + "/verification-result.html";

        if (verification.isPresent()) {
            EmailVerification emailVerification = verification.get();

            // 检查是否已过期
            if (emailVerificationService.isVerificationExpired(emailVerification)) {
                redirectView.setUrl(redirectUrl + "?status=expired");
                return redirectView;
            }

            // 标记为已验证
            emailVerificationService.markAsVerified(emailVerification);
            redirectView.setUrl(redirectUrl + "?status=success&email=" + emailVerification.getEmail());
        } else {
            redirectView.setUrl(redirectUrl + "?status=invalid");
        }

        return redirectView;
    }

    /**
     * 检查邮箱是否已验证
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkVerificationStatus(@RequestParam String email) {
        boolean isVerified = emailVerificationService.isEmailVerified(email);

        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("verified", isVerified);

        return ResponseEntity.ok(response);
    }
}