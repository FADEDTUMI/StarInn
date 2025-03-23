// 添加表单动画效果
document.addEventListener('DOMContentLoaded', function() {
    // 给输入框添加焦点效果
    const inputs = document.querySelectorAll('.form-input');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
        });

        input.addEventListener('blur', function() {
            if (this.value.length === 0) {
                this.parentElement.classList.remove('focused');
            }
        });

        // 如果已有值，直接添加focused类
        if (input.value !== '') {
            input.parentElement.classList.add('focused');
        }
    });

    // 按钮点击波纹效果
    const buttons = document.querySelectorAll('.login-btn, .google-btn');
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const ripples = document.createElement('span');
            ripples.style.left = x + 'px';
            ripples.style.top = y + 'px';
            ripples.classList.add('ripple');

            this.appendChild(ripples);

            setTimeout(() => {
                ripples.remove();
            }, 600);
        });
    });

    // Google登录按钮点击处理
    const googleBtn = document.getElementById('google-login-btn');
    if (googleBtn) {
        googleBtn.addEventListener('click', function() {
            window.location.href = '/google-login.html';
        });
    }
});
document.addEventListener('DOMContentLoaded', function() {
    // 表单和元素引用
    const loginForm = document.getElementById('login-form');
    const errorMessage = document.getElementById('error-message');
    const successMessage = document.getElementById('success-message');
    const googleLoginBtn = document.getElementById('google-login-btn');
    const registerLink = document.getElementById('register-link');
    const forgotPasswordLink = document.getElementById('forgot-password');
    const themeSwitch = document.getElementById('theme-switch');
    const loginLoader = document.getElementById('login-loader');

    // 表单输入引用
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const usernameError = document.getElementById('username-error');
    const passwordError = document.getElementById('password-error');

    // 主题切换功能
    function initTheme() {
        const savedTheme = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-theme', savedTheme);
        updateThemeIcon(savedTheme);
    }

    function updateThemeIcon(theme) {
        const icon = themeSwitch.querySelector('i');
        if (theme === 'dark') {
            icon.className = 'fas fa-sun';
        } else {
            icon.className = 'fas fa-moon';
        }
    }

    // 添加缺失的showMessage函数
    function showMessage(message, type) {
        if (type === 'success') {
            successMessage.textContent = message;
            successMessage.style.display = 'block';
        } else {
            errorMessage.textContent = message;
            errorMessage.style.display = 'block';
        }
    }

    // 登录成功后的处理
    function handleLoginSuccess(data) {
        // 保存用户信息和令牌到本地存储
        localStorage.setItem('accessToken', data.token);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('userId', data.id);
        localStorage.setItem('username', data.username);
        localStorage.setItem('email', data.email);

        // 保存用户角色信息
        if (data.roles && Array.isArray(data.roles)) {
            localStorage.setItem('userRoles', JSON.stringify(data.roles));
        }

        // 显示登录成功消息
        showMessage('登录成功，正在跳转...', 'success');

        // 检查用户角色并重定向
        if (data.roles && data.roles.includes('ROLE_ADMIN')) {
            // 跳转到管理员权限检查页面
            setTimeout(() => {
                window.location.href = '/admin-check.html';
            }, 1000);
        } else {
            // 普通用户重定向到首页
            setTimeout(() => {
                window.location.href = '/';
            }, 1000);
        }
    }

    themeSwitch.addEventListener('click', function() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateThemeIcon(newTheme);
    });

    // 初始化主题
    initTheme();

    // 输入验证函数
    function validateForm() {
        let isValid = true;

        // 用户名验证
        if (!usernameInput.value.trim()) {
            usernameError.style.display = 'block';
            usernameInput.classList.add('is-invalid');
            isValid = false;
        } else {
            usernameError.style.display = 'none';
            usernameInput.classList.remove('is-invalid');
        }

        // 密码验证
        if (!passwordInput.value) {
            passwordError.style.display = 'block';
            passwordInput.classList.add('is-invalid');
            isValid = false;
        } else {
            passwordError.style.display = 'none';
            passwordInput.classList.remove('is-invalid');
        }

        return isValid;
    }

    // 输入事件监听，实时验证
    usernameInput.addEventListener('input', function() {
        if (usernameInput.value.trim()) {
            usernameError.style.display = 'none';
            usernameInput.classList.remove('is-invalid');
        }
    });

    passwordInput.addEventListener('input', function() {
        if (passwordInput.value) {
            passwordError.style.display = 'none';
            passwordInput.classList.remove('is-invalid');
        }
    });

    // 表单提交代码部分
    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        // 表单验证
        if (!validateForm()) {
            return;
        }

        // 隐藏之前的错误/成功信息
        errorMessage.style.display = 'none';
        successMessage.style.display = 'none';

        // 显示加载指示器并禁用按钮
        loginLoader.style.display = 'inline-block';
        const submitBtn = loginForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;

        // 准备登录请求数据
        const loginData = {
            username: usernameInput.value.trim(),
            password: passwordInput.value
        };

        try {
            // 发送登录请求
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(loginData)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || '登录失败，请检查用户名和密码');
            }

            // 登录成功，调用处理函数
            handleLoginSuccess(data);

        } catch (error) {
            // 显示错误信息
            errorMessage.textContent = error.message;
            errorMessage.style.display = 'block';
        } finally {
            // 隐藏加载指示器并启用按钮
            loginLoader.style.display = 'none';
            submitBtn.disabled = false;
        }
    });

    // Google登录按钮点击事件
    googleLoginBtn.addEventListener('click', function() {
        window.location.href = '/google-login.html';
    });

    // 注册链接点击事件
    registerLink.addEventListener('click', function(e) {
        e.preventDefault();
        window.location.href = '/register.html';  // 跳转到注册页面
    });

    // 忘记密码链接点击事件
    forgotPasswordLink.addEventListener('click', function(e) {
        e.preventDefault();
        // 这里可以添加跳转到忘记密码页面的逻辑
        // 或者弹出一个密码重置对话框
        alert('密码重置功能即将上线，请联系管理员恢复密码');
    });

    // 检查URL参数，显示相应消息
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('registered')) {
        successMessage.textContent = '注册成功，请登录';
        successMessage.style.display = 'block';
    } else if (urlParams.has('verified')) {
        successMessage.textContent = '邮箱验证成功，请登录';
        successMessage.style.display = 'block';
    } else if (urlParams.has('expired')) {
        errorMessage.textContent = '会话已过期，请重新登录';
        errorMessage.style.display = 'block';
    }
});