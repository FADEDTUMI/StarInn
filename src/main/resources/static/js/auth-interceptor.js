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
// 认证拦截器，自动为请求添加JWT令牌
(function() {
    // 在页面加载时检查认证状态
    document.addEventListener('DOMContentLoaded', function() {
        // 获取存储的令牌
        const token = localStorage.getItem('accessToken');
        const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');

        // 拦截所有AJAX请求，添加认证头
        const originalFetch = window.fetch;

        window.fetch = function(url, options = {}) {
            // 默认headers
            options.headers = options.headers || {};

            // 如果localStorage中有token，添加到请求头中
            const token = localStorage.getItem('accessToken');
            if (token) {
                options.headers['Authorization'] = 'Bearer ' + token;
            }

            return originalFetch(url, options);
        };

        // 对于XMLHttpRequest也添加拦截
        const originalOpen = XMLHttpRequest.prototype.open;
        XMLHttpRequest.prototype.open = function(method, url) {
            this._url = url;
            this._method = method;
            originalOpen.apply(this, arguments);
        };

        const originalSend = XMLHttpRequest.prototype.send;
        XMLHttpRequest.prototype.send = function(body) {
            if (token && this._url) {
                this.setRequestHeader('Authorization', `Bearer ${token}`);
            }
            originalSend.apply(this, arguments);
        };
    });
})();