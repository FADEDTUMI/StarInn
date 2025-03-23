// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 检查URL参数是否有错误信息
    const urlParams = new URLSearchParams(window.location.search);
    const errorMsg = urlParams.get('error');
    if (errorMsg) {
        showMessage(decodeURIComponent(errorMsg), 'error');
    }
});

// 处理Google登录回调
function handleGoogleSignIn(response) {
    if (!response || !response.credential) {
        showMessage('Google登录失败：无法获取认证信息', 'error');
        return;
    }

    const idToken = response.credential;
    showLoader(true);

    // 发送ID令牌到后端进行验证
    fetch('/google-oauth', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ idToken: idToken })
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '验证失败，请重试');
                });
            }
            return response.json();
        })
        .then(data => {
            showLoader(false);

            if (data.tempToken) {
                // 检查是否为新用户
                if (data.newUser) {
                    // 新用户需要完成设置
                    showMessage('首次使用Google账号登录，请设置用户信息', 'success');

                    // 将Google验证信息存储在本地
                    localStorage.setItem('googleAuthInfo', JSON.stringify({
                        email: data.email,
                        name: data.name,
                        pictureUrl: data.pictureUrl,
                        suggestedUsername: data.suggestedUsername
                    }));

                    // 添加一个小延迟以显示成功消息
                    setTimeout(() => {
                        // 重定向到设置页面，携带临时令牌
                        window.location.href = `/google-setup.html?token=${encodeURIComponent(data.tempToken)}`;
                    }, 1000);
                } else {
                    // 已有用户，直接处理登录
                    showMessage('登录成功，正在为您自动登录...', 'success');

                    // 直接进行登录完成操作
                    completeExistingUserLogin(data.tempToken);
                }
            } else {
                showMessage('登录失败：' + (data.message || '未知错误'), 'error');
            }
        })
        .catch(error => {
            showLoader(false);

            // 检查是否是令牌过期错误
            if (error.message && error.message.includes('expired')) {
                showMessage('验证会话已过期，请重新登录', 'error');
            } else {
                showMessage('登录过程中发生错误：' + error.message, 'error');
            }

            // 清除可能存在的本地缓存数据
            localStorage.removeItem('googleAuthInfo');
        });
}

// 处理已存在用户的直接登录
function completeExistingUserLogin(tempToken) {
    showLoader(true);

    // 对已有用户，发送请求直接完成登录过程
    fetch('/google-oauth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ tempToken: tempToken })
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '自动登录失败，请重试');
                });
            }
            return response.json();
        })
        .then(data => {
            showLoader(false);

            if (data.token) {
                // 登录成功，保存令牌
                localStorage.setItem('accessToken', data.token);
                localStorage.setItem('refreshToken', data.refreshToken);
                localStorage.setItem('userId', data.id);
                localStorage.setItem('username', data.username);
                localStorage.setItem('email', data.email);

                showMessage('登录成功，正在跳转...', 'success');

                // 跳转到首页
                setTimeout(() => {
                    window.location.href = '/';
                }, 1000);
            } else {
                showMessage('登录失败：' + (data.message || '未知错误'), 'error');
            }
        })
        .catch(error => {
            showLoader(false);
            showMessage('登录过程中��生错误：' + error.message, 'error');
        });
}

// 显示/隐藏加载状态
function showLoader(show) {
    document.getElementById('loader-container').style.display = show ? 'flex' : 'none';
}

// 显示消息
function showMessage(text, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;
    messageDiv.className = 'message ' + type;
    messageDiv.style.display = 'block';
}