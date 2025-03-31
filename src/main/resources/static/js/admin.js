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
// 全局变量定义
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let currentUserData = [];
let currentConfirmCallback = null;
let currentSystemSettings = {};
// 日志管理相关变量
let logCurrentPage = 1;
let logPageSize = 10;
let logTotalPages = 1;
let currentLogData = [];
// 全局变量定义 - 图表对象
let userGrowthChart = null;
let userActivityChart = null;

// DOM加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化主题
    initTheme();

    // 检查登录状态
    checkAdminAuth();

    // 初始化侧边栏
    initSidebar();

    // 绑定主题切换按钮事件
    document.getElementById('theme-switch').addEventListener('click', toggleTheme);

    // 绑定用户下拉菜单
    initUserDropdown();

    // 绑定退出登录按钮
    document.getElementById('logout-btn').addEventListener('click', handleLogout);

    // 初始化各种模态框
    initModals();

    // 绑定添加管理员相关事件
    document.getElementById('add-admin-btn').addEventListener('click', showAddAdminModal);
    document.getElementById('confirm-admin-btn').addEventListener('click', handleAddAdmin);

    // 绑定用户管理相关事件
    document.getElementById('search-btn').addEventListener('click', searchUsers);
    document.getElementById('user-search').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') searchUsers();
    });
    document.getElementById('status-filter').addEventListener('change', searchUsers);
    document.getElementById('role-filter').addEventListener('change', searchUsers);
    document.getElementById('select-all-users').addEventListener('change', toggleSelectAllUsers);

    // 绑定批量操作按钮
    document.getElementById('batch-enable').addEventListener('click', handleBatchEnable);
    document.getElementById('batch-disable').addEventListener('click', handleBatchDisable);
    document.getElementById('batch-delete').addEventListener('click', handleBatchDelete);

    // 绑定系统设置保存按钮
    document.getElementById('save-settings').addEventListener('click', saveSystemSettings);
    document.getElementById('reset-settings').addEventListener('click', resetSystemSettings);

    // 绑定日志操作按钮
    document.getElementById('log-search-btn').addEventListener('click', searchLogs);
    document.getElementById('export-logs').addEventListener('click', exportLogs);
    document.getElementById('clear-logs').addEventListener('click', showClearLogsConfirm);

    // 加载初始数据
    loadDashboardData();
    loadUsers();
    loadSystemSettings();
    loadLogs();
});

// 在admin.js文件的开头添加这段代码
(function() {
    // 提前声明全局函数
    window.showToast = function(message, type = 'info', duration = 3000) {
        // 函数内容保持不变
        // 获取toast容器
        const toastContainer = document.getElementById('toast-container');

        // 创建toast元素
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        // 设置内容
        const icon = getToastIcon(type);
        toast.innerHTML = `
            <div class="toast-content">
                <i class="${icon}"></i>
                <span class="toast-message">${message}</span>
            </div>
            <button class="toast-close"><i class="fas fa-times"></i></button>
        `;

        // 添加到容器
        toastContainer.appendChild(toast);

        // 添加显示类
        setTimeout(() => {
            toast.classList.add('show');
        }, 10);

        // 关闭按钮事件
        const closeBtn = toast.querySelector('.toast-close');
        closeBtn.addEventListener('click', () => {
            closeToast(toast);
        });

        // 自动关闭
        if (duration > 0) {
            setTimeout(() => {
                closeToast(toast);
            }, duration);
        }
    };

    window.closeToast = function(toast) {
        toast.classList.remove('show');
        toast.classList.add('hiding');

        // 动画结束后移除元素
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    };

    window.getToastIcon = function(type) {
        switch(type) {
            case 'success':
                return 'fas fa-check-circle';
            case 'error':
                return 'fas fa-exclamation-circle';
            case 'warning':
                return 'fas fa-exclamation-triangle';
            case 'info':
            default:
                return 'fas fa-info-circle';
        }
    };
})();

// 移除admin.js底部的原函数定义，避免重复
// 原来底部的定义可以删除


// 初始化主题
function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);
}

// 更新主题图标
function updateThemeIcon(theme) {
    const themeIcon = document.getElementById('theme-switch').querySelector('i');
    if (theme === 'dark') {
        themeIcon.className = 'fas fa-sun';
    } else {
        themeIcon.className = 'fas fa-moon';
    }
}

// 切换主题
function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
}

// 检查管理员权限
function checkAdminAuth() {
    const token = localStorage.getItem('accessToken');
    const userRoles = JSON.parse(localStorage.getItem('userRoles') || '[]');

    if (!token || !userRoles.includes('ROLE_ADMIN')) {
        showToast('您没有管理员权限，即将跳转到登录页面', 'error');
        setTimeout(() => {
            window.location.href = '/login.html?expired=true';
        }, 2000);
        return;
    }

    // 显示管理员用户名
    const username = localStorage.getItem('username');
    if (username) {
        document.getElementById('admin-username').textContent = username;
    }
}

// 初始化侧边栏
function initSidebar() {
    const sidebarItems = document.querySelectorAll('.admin-sidebar ul li');
    sidebarItems.forEach(item => {
        item.addEventListener('click', function() {
            // 移除所有活动状态
            sidebarItems.forEach(i => i.classList.remove('active'));
            // 添加当前项目的活动状态
            this.classList.add('active');

            // 隐藏所有面板
            document.querySelectorAll('.panel').forEach(panel => {
                panel.classList.remove('active');
            });

            // 显示选中的面板
            const panelId = this.getAttribute('data-panel');
            document.getElementById(panelId).classList.add('active');
        });
    });
}

// 初始化用户下拉菜单
function initUserDropdown() {
    const dropdown = document.getElementById('userDropdown');
    const menu = document.querySelector('.dropdown-menu');

    dropdown.addEventListener('click', function(e) {
        e.stopPropagation();
        menu.classList.toggle('show');
    });

    // 点击其他地方关闭下拉菜单
    document.addEventListener('click', function() {
        if (menu.classList.contains('show')) {
            menu.classList.remove('show');
        }
    });
}

// 处理退出登录
function handleLogout() {
    showConfirmModal('确定要退出登录吗？', function() {
        // 清除本地存储的token和用户信息
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        localStorage.removeItem('email');
        localStorage.removeItem('userRoles');

        // 跳转到登录页面
        window.location.href = '/login.html';
    });
}

// 初始化模态框
function initModals() {
    // 关闭添加管理员模态框
    document.getElementById('close-admin-modal').addEventListener('click', function() {
        document.getElementById('add-admin-modal').classList.remove('show');
    });
    document.getElementById('cancel-admin-btn').addEventListener('click', function() {
        document.getElementById('add-admin-modal').classList.remove('show');
    });

    // 关闭用户编辑模态框
    document.getElementById('close-user-modal').addEventListener('click', function() {
        document.getElementById('edit-user-modal').classList.remove('show');
    });
    document.getElementById('cancel-edit-btn').addEventListener('click', function() {
        document.getElementById('edit-user-modal').classList.remove('show');
    });

    // 关闭确认模态框
    document.getElementById('close-confirm-modal').addEventListener('click', function() {
        document.getElementById('confirm-modal').classList.remove('show');
    });
    document.getElementById('cancel-confirm-btn').addEventListener('click', function() {
        document.getElementById('confirm-modal').classList.remove('show');
    });

    // 确认操作回调
    document.getElementById('confirm-action-btn').addEventListener('click', function() {
        document.getElementById('confirm-modal').classList.remove('show');
        if (typeof currentConfirmCallback === 'function') {
            currentConfirmCallback();
            currentConfirmCallback = null;
        }
    });
}

// 显示确认模态框
function showConfirmModal(message, callback) {
    document.getElementById('confirm-message').textContent = message;
    document.getElementById('confirm-modal').classList.add('show');
    currentConfirmCallback = callback;
}

// 显示添加管理员模态框
function showAddAdminModal() {
    // 重置表单
    document.getElementById('add-admin-form').reset();
    // 显示模态框
    document.getElementById('add-admin-modal').classList.add('show');
}


// 处理添加管理员
function handleAddAdmin() {
    const usernameInput = document.getElementById('admin-username-input');
    const emailInput = document.getElementById('admin-email-input');
    const passwordInput = document.getElementById('admin-password-input');
    const fullnameInput = document.getElementById('admin-fullname-input');
    const phoneInput = document.getElementById('admin-phone-input');
    const keyInput = document.getElementById('admin-key-input');

    // 简单验证
    if (!usernameInput.value || !emailInput.value || !passwordInput.value || !keyInput.value) {
        showToast('请填写必填字段', 'error');
        return;
    }

    // 准备提交数据
    const adminData = {
        username: usernameInput.value,
        email: emailInput.value,
        password: passwordInput.value,
        fullName: fullnameInput.value,
        phone: phoneInput.value,
        registrationKey: keyInput.value
    };

    // 禁用提交按钮
    const submitBtn = document.getElementById('confirm-admin-btn');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 处理中...';

    // 发送请求
    fetch('/api/admin/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        },
        body: JSON.stringify(adminData)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '添加管理员失败');
                });
            }
            return response.json();
        })
        .then(() => { // 移除未使用的data参数
            // 关闭模态框
            document.getElementById('add-admin-modal').classList.remove('show');
            // 显示成功消息
            showToast('管理员添加成功', 'success');
            // 刷新用户列表
            loadUsers();
            // 刷新仪表盘数据
            loadDashboardData();
        })
        .catch(error => {
            showToast(error.message, 'error');
        })
        .finally(() => {
            // 恢复按钮状态
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        });
}

// 加载仪表盘数据
function loadDashboardData() {
    // 显示加载状态
    document.getElementById('total-users').textContent = '加载中...';
    document.getElementById('active-today').textContent = '加载中...';
    document.getElementById('system-status').textContent = '加载中...';
    document.getElementById('new-users').textContent = '加载中...';

    // 显示图表加载状态
    document.getElementById('user-growth-chart-loading').style.display = 'flex';
    document.getElementById('user-activity-chart-loading').style.display = 'flex';
    document.getElementById('user-growth-chart-container').style.display = 'none';
    document.getElementById('user-activity-chart-container').style.display = 'none';

    fetch('/api/admin/dashboard', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('网络响应不正常');
            }
            return response.json();
        })
        .then(data => {
            // 更新统计卡片数据
            document.getElementById('total-users').textContent = data.totalUsers || 0;
            document.getElementById('active-today').textContent = data.activeToday || 0;
            document.getElementById('system-status').textContent = data.systemStatus || '正常';
            document.getElementById('new-users').textContent = data.newUsers || 0;

            // 处理图表数据
            if (data.userGrowthData) {
                updateUserGrowthChart(data.userGrowthData);
                document.getElementById('user-growth-chart-loading').style.display = 'none';
                document.getElementById('user-growth-chart-container').style.display = 'block';
            } else {
                document.getElementById('user-growth-chart-loading').textContent = '无数据';
            }

            if (data.activityData) {
                updateActivityChart(data.activityData);
                document.getElementById('user-activity-chart-loading').style.display = 'none';
                document.getElementById('user-activity-chart-container').style.display = 'block';
            } else {
                document.getElementById('user-activity-chart-loading').textContent = '无数据';
            }
        })
        .catch(error => {
            console.error('加载仪表盘数据失败:', error);
            document.getElementById('total-users').textContent = '加载失败';
            document.getElementById('active-today').textContent = '加载失败';
            document.getElementById('system-status').textContent = '加载失败';
            document.getElementById('new-users').textContent = '加载失败';

            document.getElementById('user-growth-chart-loading').textContent = '加载失败';
            document.getElementById('user-activity-chart-loading').textContent = '加载失败';
        });
}

// 加载用户列表
// 修复用户列表加载函数，确保处理createdAt字段
function loadUsers(page = 1, size = 10) {
    // 更新当前页
    currentPage = page;
    pageSize = size;

    // 获取搜索参数
    const searchTerm = document.getElementById('user-search').value;
    const statusFilter = document.getElementById('status-filter').value;
    const roleFilter = document.getElementById('role-filter').value;

    // 显示加载状态
    const tableBody = document.getElementById('users-list');
    tableBody.innerHTML = `
        <tr class="loading-row">
            <td colspan="9" class="text-center">
                <div class="spinner">
                    <div class="rect1"></div>
                    <div class="rect2"></div>
                    <div class="rect3"></div>
                    <div class="rect4"></div>
                    <div class="rect5"></div>
                </div>
                <p>正在加载数据...</p>
            </td>
        </tr>
    `;

    // 构建查询参数
    const params = new URLSearchParams({
        page: page - 1,  // 后端可能是从0开始计数
        size: size
    });

    if (searchTerm) params.append('search', searchTerm);
    if (statusFilter !== 'all') params.append('status', statusFilter);
    if (roleFilter !== 'all') params.append('role', roleFilter);

    fetch(`/api/admin/users?${params.toString()}`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        }
    })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    // 未授权，重定向到登录页面
                    localStorage.removeItem('accessToken');
                    window.location.href = '/login.html?expired=true';
                    throw new Error('未授权访问或请求失败');
                }
                return response.json().then(data => {
                    throw new Error(data.message || '加载用户数据失败');
                });
            }
            return response.json();
        })
        .then(data => {
            // 保存当前数据
            currentUserData = data.content || [];
            // 更新总页数
            totalPages = data.totalPages || 1;

            // 清空表格
            tableBody.innerHTML = '';

            // 如果没有数据
            if (!currentUserData.length) {
                tableBody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center">
                        <p>暂无用户数据</p>
                    </td>
                </tr>
            `;
                return;
            }

            // 填充用户数据
            currentUserData.forEach(user => {
                // 确保有创建日期，如果没有则使用默认值
                const createdAtFormatted = user.createdAt ? formatDateTime(user.createdAt) : '未知';
                const lastLoginFormatted = user.lastLogin ? formatDateTime(user.lastLogin) : '从未登录';

                const statusClass = user.enabled ? 'status-active' : 'status-inactive';
                const statusText = user.enabled ? '启用' : '禁用';

                tableBody.innerHTML += `
                <tr>
                    <td><input type="checkbox" class="user-select" data-id="${user.id}"></td>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.email}</td>
                    <td>${user.fullName || '-'}</td>
                    <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                    <td>${createdAtFormatted}</td>
                    <td>${lastLoginFormatted}</td>
                    <td>
                        <button class="action-btn edit-btn" data-id="${user.id}" title="编辑用户">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-btn delete-btn" data-id="${user.id}" title="删除用户">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </td>
                </tr>
            `;
            });

            // 添加编辑和删除按钮事件
            document.querySelectorAll('.edit-btn').forEach(btn => {
                btn.addEventListener('click', function() {
                    const userId = this.getAttribute('data-id');
                    showEditUserModal(userId);
                });
            });

            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', function() {
                    const userId = this.getAttribute('data-id');
                    showDeleteUserConfirm(userId);
                });
            });

            // 更新用户选择框事件
            document.querySelectorAll('.user-select').forEach(checkbox => {
                checkbox.addEventListener('change', updateBatchButtonStatus);
            });

            // 更新分页信息
            updatePagination(data);
        })
        .catch(error => {
            console.error('获取用户数据失败:', error);
            showToast(error.message, 'error');
            tableBody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center">
                    <p class="error-message">加载数据失败: ${error.message}</p>
                </td>
            </tr>
        `;
        });
}

// 更新分页控件
function updatePagination(data) {
    const totalElements = data.totalElements || 0;
    const totalPages = data.totalPages || 1;
    const currentPageIndex = data.number || 0;  // 后端可能从0开始计数
    const currentPage = currentPageIndex + 1;   // 前端从1开始显示

    // 更新页码信息
    const start = (currentPageIndex * pageSize) + 1;
    const end = Math.min((currentPageIndex * pageSize) + data.numberOfElements, totalElements);
    document.getElementById('page-info').textContent = `${start}-${end}`;
    document.getElementById('total-count').textContent = totalElements;

    // 生成分页按钮
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    // 上一页按钮
    const prevDisabled = currentPage === 1 ? 'disabled' : '';
    pagination.innerHTML += `
        <button class="pagination-item ${prevDisabled}" data-page="${currentPage - 1}" ${prevDisabled}>
            <i class="fas fa-chevron-left"></i>
        </button>
    `;

    // 页码按钮
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, startPage + 4);

    // 确保始终显示5个按钮（如果有足够的页面）
    if (endPage - startPage < 4 && totalPages > 5) {
        startPage = Math.max(1, endPage - 4);
    }

    for (let i = startPage; i <= endPage; i++) {
        const active = i === currentPage ? 'active' : '';
        pagination.innerHTML += `
            <button class="pagination-item ${active}" data-page="${i}">${i}</button>
        `;
    }

    // 下一页按钮
    const nextDisabled = currentPage === totalPages ? 'disabled' : '';
    pagination.innerHTML += `
        <button class="pagination-item ${nextDisabled}" data-page="${currentPage + 1}" ${nextDisabled}>
            <i class="fas fa-chevron-right"></i>
        </button>
    `;

    // 绑定分页按钮事件
    document.querySelectorAll('.pagination-item:not(.disabled)').forEach(item => {
        item.addEventListener('click', function() {
            const page = parseInt(this.getAttribute('data-page'));
            loadUsers(page, pageSize);
        });
    });
}

// 搜索用户
function searchUsers() {
    loadUsers(1, pageSize); // 重置到第一页
}

// 显示编辑用户模态框
function showEditUserModal(userId) {
    // 查找用户数据 - 使用严格比较
    const user = currentUserData.find(u => u.id === Number(userId) || u.id === userId);

    if (!user) {
        showToast('获取用户数据失败', 'error');
        return;
    }

    // 填充表单数据
    document.getElementById('edit-user-id').value = user.id;
    document.getElementById('edit-username').value = user.username;
    document.getElementById('edit-email').value = user.email;
    document.getElementById('edit-fullname').value = user.fullName || '';
    document.getElementById('edit-phone').value = user.phone || '';
    document.getElementById('edit-status').value = user.enabled.toString();

    // 设置用户角色
    const rolesSelect = document.getElementById('edit-roles');
    Array.from(rolesSelect.options).forEach(option => {
        option.selected = (user.roles || []).includes(option.value);
    });

    // 显示模态框
    document.getElementById('edit-user-modal').classList.add('show');
}

// 显示删除用户确认框
function showDeleteUserConfirm(userId) {
    const user = currentUserData.find(u => u.id === userId);

    if (!user) {
        showToast('获取用户数据失败', 'error');
        return;
    }

    showConfirmModal(`确定要删除用户"${user.username}"吗？此操作不可恢复。`, function() {
        deleteUser(userId);
    });
}

// 删除用户
function deleteUser(userId) {
    fetch(`/api/admin/users/${userId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '删除用户失败');
                });
            }
            return response.text();
        })
        .then(() => {
            showToast('用户删除成功', 'success');
            loadUsers(currentPage, pageSize);
        })
        .catch(error => {
            showToast(error.message, 'error');
        });
}

// 提交用户编辑
document.getElementById('confirm-edit-btn').addEventListener('click', function() {
    const userId = document.getElementById('edit-user-id').value;
    const fullName = document.getElementById('edit-fullname').value;
    const phone = document.getElementById('edit-phone').value;
    const enabled = document.getElementById('edit-status').value === 'true';

    // 获取选中的角色
    const rolesSelect = document.getElementById('edit-roles');
    const selectedRoles = Array.from(rolesSelect.selectedOptions).map(option => option.value);

    // 准备更新数据
    const userData = {
        fullName: fullName,
        phone: phone,
        enabled: enabled,
        roles: selectedRoles
    };

    // 禁用提交按钮
    const submitBtn = document.getElementById('confirm-edit-btn');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 处理中...';

    fetch(`/api/admin/users/${userId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        },
        body: JSON.stringify(userData)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '更新用户失败');
                });
            }
            return response.json();
        })
        .then(() => { // 移除未使用的data参数
            // 关闭模态框
            document.getElementById('edit-user-modal').classList.remove('show');
            // 显示成功消息
            showToast('用户信息更新成功', 'success');
            // 刷新用户列表
            loadUsers(currentPage, pageSize);
        })
        .catch(error => {
            showToast(error.message, 'error');
        })
        .finally(() => {
            // 恢复按钮状态
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        });
});

// 切换选择所有用户
function toggleSelectAllUsers() {
    const selectAll = document.getElementById('select-all-users').checked;
    document.querySelectorAll('.user-select').forEach(checkbox => {
        checkbox.checked = selectAll;
    });
    updateBatchButtonStatus();
}

// 更新批量操作按钮状态
function updateBatchButtonStatus() {
    const selectedUsers = document.querySelectorAll('.user-select:checked');
    const hasSelected = selectedUsers.length > 0;

    document.getElementById('batch-enable').disabled = !hasSelected;
    document.getElementById('batch-disable').disabled = !hasSelected;
    document.getElementById('batch-delete').disabled = !hasSelected;
}

// 获取选中的用户ID
function getSelectedUserIds() {
    const selectedUsers = document.querySelectorAll('.user-select:checked');
    return Array.from(selectedUsers).map(checkbox => checkbox.getAttribute('data-id'));
}

// 批量启用用户
// 参数类型匹配问题的修复 - 确保传递正确的URLSearchParams对象
function handleBatchEnable() {
    const userIds = getSelectedUserIds();
    if (!userIds.length) return;

    showConfirmModal(`确定要启用选中的${userIds.length}个用户吗？`, function() {
        // 创建FormData或URLSearchParams对象
        const formData = new FormData();
        userIds.forEach(id => formData.append('ids', id));

        // 或使用URLSearchParams
        // const params = new URLSearchParams();
        // userIds.forEach(id => params.append('ids', id));

        fetch('/api/admin/users/batch-enable', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
                // 注意：使用FormData时不要设置Content-Type
            },
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.message || '批量启用用户失败');
                    });
                }
                return response.text();
            })
            .then(() => {
                showToast('用户启用成功', 'success');
                loadUsers(currentPage, pageSize);
            })
            .catch(error => {
                console.error('批量启用用户失败:', error);
                showToast(error.message, 'error');
            });
    });
}

// 批量禁用用户
// 类似地，修复其他批量操作函数
function handleBatchDisable() {
    const userIds = getSelectedUserIds();
    if (!userIds.length) return;

    showConfirmModal(`确定要禁用选中的${userIds.length}个用户吗？`, function() {
        const formData = new FormData();
        userIds.forEach(id => formData.append('ids', id));

        fetch('/api/admin/users/batch-disable', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
            },
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.message || '批量禁用用户失败');
                    });
                }
                return response.text();
            })
            .then(() => {
                showToast('用户禁用成功', 'success');
                loadUsers(currentPage, pageSize);
            })
            .catch(error => {
                console.error('批量禁用用户失败:', error);
                showToast(error.message, 'error');
            });
    });
}

// 批量删除用户
function handleBatchDelete() {
    const userIds = getSelectedUserIds();
    if (!userIds.length) return;

    showConfirmModal(`确定要删除选中的${userIds.length}个用户吗？此操作不可恢复。`, function() {
        const formData = new FormData();
        userIds.forEach(id => formData.append('ids', id));

        fetch('/api/admin/users/batch-delete', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
            },
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.message || '批量删除用户失败');
                    });
                }
                return response.text();
            })
            .then(() => {
                showToast('用户删除成功', 'success');
                loadUsers(currentPage, pageSize);
            })
            .catch(error => {
                console.error('批量删除用户失败:', error);
                showToast(error.message, 'error');
            });
    });
}

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间字符串或日期对象
 * @param {boolean} includeTime - 是否包含时间部分，默认为true
 * @returns {string} - 格式化后的日期时间字符串
 */
function formatDateTime(dateTime, includeTime = true) {
    if (!dateTime) return '无';

    try {
        const date = new Date(dateTime);
        if (isNaN(date.getTime())) return '无效日期';

        // 日期部分：年-月-日
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const dateStr = `${year}-${month}-${day}`;

        if (!includeTime) return dateStr;

        // 时间部分：时:分:秒
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        const timeStr = `${hours}:${minutes}:${seconds}`;

        return `${dateStr} ${timeStr}`;
    } catch (e) {
        console.error('日期格式化错误', e);
        return '格式化错误';
    }
}

// 加载系统设置
function loadSystemSettings() {
    // 显示加载状态
    const settingsForm = document.getElementById('system-settings-form');
    const formFields = settingsForm.querySelectorAll('input, select, textarea');
    formFields.forEach(field => {
        field.disabled = true;
    });

    document.getElementById('settings-loading').style.display = 'block';
    document.getElementById('settings-content').style.display = 'none';

    fetch('/api/admin/settings', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '加载系统设置失败');
                });
            }
            return response.json();
        })
        .then(data => {
            // 保存当前设置
            currentSystemSettings = data;

            // 填充表单数据
            document.getElementById('site-title').value = data.siteTitle || '';
            document.getElementById('site-description').value = data.siteDescription || '';
            document.getElementById('allow-registration').checked = data.allowRegistration || false;
            document.getElementById('require-email-verification').checked = data.requireEmailVerification || false;
            document.getElementById('maintenance-mode').checked = data.maintenanceMode || false;
            document.getElementById('max-login-attempts').value = data.maxLoginAttempts || 5;
            document.getElementById('password-expiry-days').value = data.passwordExpiryDays || 90;
            document.getElementById('session-timeout').value = data.sessionTimeout || 30;

            // 如果有其他设置，继续添加

            // 启用表单
            formFields.forEach(field => {
                field.disabled = false;
            });

            // 显示内容
            document.getElementById('settings-loading').style.display = 'none';
            document.getElementById('settings-content').style.display = 'block';
        })
        .catch(error => {
            console.error('加载设置失败:', error);
            showToast(error.message, 'error');

            // 显示错误状态
            document.getElementById('settings-loading').style.display = 'none';
            document.getElementById('settings-error').style.display = 'block';
            document.getElementById('settings-error-message').textContent = error.message;
        });
}

// 保存系统设置
function saveSystemSettings() {
    // 获取表单数据
    const settingsData = {
        siteTitle: document.getElementById('site-title').value,
        siteDescription: document.getElementById('site-description').value,
        allowRegistration: document.getElementById('allow-registration').checked,
        requireEmailVerification: document.getElementById('require-email-verification').checked,
        maintenanceMode: document.getElementById('maintenance-mode').checked,
        maxLoginAttempts: parseInt(document.getElementById('max-login-attempts').value) || 5,
        passwordExpiryDays: parseInt(document.getElementById('password-expiry-days').value) || 90,
        sessionTimeout: parseInt(document.getElementById('session-timeout').value) || 30
        // 如果有其他设置，继续添加
    };

    // 禁用保存按钮
    const saveBtn = document.getElementById('save-settings');
    const resetBtn = document.getElementById('reset-settings');
    saveBtn.disabled = true;
    resetBtn.disabled = true;
    saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 保存中...';

    fetch('/api/admin/settings', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        },
        body: JSON.stringify(settingsData)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '保存系统设置失败');
                });
            }
            return response.json();
        })
        .then(() => {
            // 更新当前设置
            currentSystemSettings = settingsData;
            showToast('系统设置已成功保存', 'success');
        })
        .catch(error => {
            console.error('保存设置失败:', error);
            showToast(error.message, 'error');
        })
        .finally(() => {
            // 恢复按钮状态
            saveBtn.disabled = false;
            resetBtn.disabled = false;
            saveBtn.innerHTML = '保存设置';
        });
}

// 重置系统设置
function resetSystemSettings() {
    showConfirmModal('确定要重置所有系统设置为默认值吗？', function() {
        // 禁用按钮
        const saveBtn = document.getElementById('save-settings');
        const resetBtn = document.getElementById('reset-settings');
        saveBtn.disabled = true;
        resetBtn.disabled = true;
        resetBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 重置中...';

        fetch('/api/admin/settings/reset', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
            }
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.message || '重置系统设置失败');
                    });
                }
                return response.json();
            })
            .then(data => {
                // 更新当前设置
                currentSystemSettings = data;

                // 重新填充表单
                document.getElementById('site-title').value = data.siteTitle || '';
                document.getElementById('site-description').value = data.siteDescription || '';
                document.getElementById('allow-registration').checked = data.allowRegistration || false;
                document.getElementById('require-email-verification').checked = data.requireEmailVerification || false;
                document.getElementById('maintenance-mode').checked = data.maintenanceMode || false;
                document.getElementById('max-login-attempts').value = data.maxLoginAttempts || 5;
                document.getElementById('password-expiry-days').value = data.passwordExpiryDays || 90;
                document.getElementById('session-timeout').value = data.sessionTimeout || 30;

                showToast('系统设置已重置为默认值', 'success');
            })
            .catch(error => {
                console.error('重置设置失败:', error);
                showToast(error.message, 'error');
            })
            .finally(() => {
                // 恢复按钮状态
                saveBtn.disabled = false;
                resetBtn.disabled = false;
                resetBtn.innerHTML = '重置为默认值';
            });
    });
}

// 加载日志数据
function loadLogs(page = 1, size = 10) {
    // 更新当前页
    logCurrentPage = page;
    logPageSize = size;

    // 获取筛选参数
    const logType = document.getElementById('log-type-filter').value;
    const dateFrom = document.getElementById('log-date-from').value;
    const dateTo = document.getElementById('log-date-to').value;

    // 显示加载状态
    const tableBody = document.getElementById('logs-list');
    tableBody.innerHTML = `
        <tr class="loading-row">
            <td colspan="6" class="text-center">
                <div class="spinner-border spinner-border-sm" role="status">
                    <span class="visually-hidden">加载中...</span>
                </div>
                <span class="ms-2">加载日志数据...</span>
            </td>
        </tr>
    `;

    // 构建查询参数
    const params = new URLSearchParams({
        page: page - 1,  // 后端可能是从0开始计数
        size: size
    });

    if (logType !== 'all') params.append('type', logType);
    if (dateFrom) params.append('dateFrom', dateFrom);
    if (dateTo) params.append('dateTo', dateTo);

    fetch(`/api/admin/logs?${params.toString()}`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '加载日志数据失败');
                });
            }
            return response.json();
        })
        .then(data => {
            // 保存当前数据
            currentLogData = data.content || [];

            // 清空表格
            tableBody.innerHTML = '';

            // 如果没有数据
            if (!currentLogData.length) {
                tableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center">暂无日志数据</td>
                </tr>
            `;
                return;
            }

            // 填充日志数据
            currentLogData.forEach(log => {
                const typeClass = getLogTypeClass(log.type);
                tableBody.innerHTML += `
                <tr>
                    <td>${log.id}</td>
                    <td><span class="log-type ${typeClass}">${getLogTypeDisplay(log.type)}</span></td>
                    <td>${log.message}</td>
                    <td>${log.username || '-'}</td>
                    <td>${log.ipAddress || '-'}</td>
                    <td>${formatDateTime(log.timestamp)}</td>
                </tr>
            `;
            });

            // 更新分页信息
            updateLogPagination(data);
        })
        .catch(error => {
            console.error('获取日志数据失败:', error);
            showToast(error.message, 'error');
            tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-danger">
                    <i class="fas fa-exclamation-circle"></i> 加载失败: ${error.message}
                </td>
            </tr>
        `;
        });
}

// 搜索日志
function searchLogs() {
    loadLogs(1, logPageSize); // 重置到第一页
}

// 导出日志
function exportLogs() {
    // 获取筛选参数
    const logType = document.getElementById('log-type-filter').value;
    const dateFrom = document.getElementById('log-date-from').value;
    const dateTo = document.getElementById('log-date-to').value;

    // 构建查询参数
    const params = new URLSearchParams();
    if (logType !== 'all') params.append('type', logType);
    if (dateFrom) params.append('dateFrom', dateFrom);
    if (dateTo) params.append('dateTo', dateTo);

    // 显示加载状态
    const exportBtn = document.getElementById('export-logs');
    const originalText = exportBtn.textContent;
    exportBtn.disabled = true;
    exportBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 导出中...';

    // 使用fetch下载文件
    fetch(`/api/admin/logs/export?${params.toString()}`, {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '导出日志失败');
                });
            }
            return response.blob();
        })
        .then(blob => {
            // 创建下载链接
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;

            // 设置文件名 - 使用当前日期作为文件名的一部分
            const today = new Date();
            const dateStr = formatDateTime(today, false).replace(/-/g, '');
            a.download = `system_logs_${dateStr}.csv`;

            // 触发下载
            document.body.appendChild(a);
            a.click();

            // 清理
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            showToast('日志导出成功', 'success');
        })
        .catch(error => {
            console.error('导出日志失败:', error);
            showToast(error.message, 'error');
        })
        .finally(() => {
            // 恢复按钮状态
            exportBtn.disabled = false;
            exportBtn.innerHTML = originalText;
        });
}

// 显示清空日志确认框
function showClearLogsConfirm() {
    showConfirmModal('确定要清空所有日志记录吗？此操作不可恢复！', function() {
        clearLogs();
    });
}

// 清空日志
function clearLogs() {
    // 显示加载状态
    const clearBtn = document.getElementById('clear-logs');
    const originalText = clearBtn.textContent;
    clearBtn.disabled = true;
    clearBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 处理中...';

    fetch('/api/admin/logs/clear', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('accessToken')
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || '清空日志失败');
                });
            }
            return response.text();
        })
        .then(() => {
            showToast('所有日志已清空', 'success');
            // 重新加载日志
            loadLogs(1, logPageSize);
        })
        .catch(error => {
            console.error('清空日志失败:', error);
            showToast(error.message, 'error');
        })
        .finally(() => {
            // 恢复按钮状态
            clearBtn.disabled = false;
            clearBtn.innerHTML = originalText;
        });
}

// 更新日志分页控件
function updateLogPagination(data) {
    const totalElements = data.totalElements || 0;
    const totalPages = data.totalPages || 1;
    const currentPageIndex = data.number || 0;  // 后端可能从0开始计数
    const currentPage = currentPageIndex + 1;   // 前端从1开始显示

    // 更新页码信息
    const start = (currentPageIndex * logPageSize) + 1;
    const end = Math.min((currentPageIndex * logPageSize) + data.numberOfElements, totalElements);
    document.getElementById('log-page-info').textContent = `${start}-${end}`;
    document.getElementById('log-total-count').textContent = totalElements;

    // 生成分页按钮
    const pagination = document.getElementById('log-pagination');
    pagination.innerHTML = '';

    // 上一页按钮
    const prevDisabled = currentPage === 1 ? 'disabled' : '';
    pagination.innerHTML += `
        <button class="pagination-item ${prevDisabled}" data-page="${currentPage - 1}" ${prevDisabled}>
            <i class="fas fa-chevron-left"></i>
        </button>
    `;

    // 页码按钮
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, startPage + 4);

    // 确保始终显示5个按钮（如果有足够的页面）
    if (endPage - startPage < 4 && totalPages > 5) {
        startPage = Math.max(1, endPage - 4);
    }

    for (let i = startPage; i <= endPage; i++) {
        const active = i === currentPage ? 'active' : '';
        pagination.innerHTML += `
            <button class="pagination-item ${active}" data-page="${i}">${i}</button>
        `;
    }

    // 下一页按钮
    const nextDisabled = currentPage === totalPages ? 'disabled' : '';
    pagination.innerHTML += `
        <button class="pagination-item ${nextDisabled}" data-page="${currentPage + 1}" ${nextDisabled}>
            <i class="fas fa-chevron-right"></i>
        </button>
    `;

    // 绑定分页按钮事件
    document.querySelectorAll('#log-pagination .pagination-item:not(.disabled)').forEach(item => {
        item.addEventListener('click', function() {
            const page = parseInt(this.getAttribute('data-page'));
            loadLogs(page, logPageSize);
        });
    });
}

// 获取日志类型显示文本
function getLogTypeDisplay(type) {
    switch(type) {
        case 'LOGIN': return '登录';
        case 'LOGOUT': return '登出';
        case 'REGISTER': return '注册';
        case 'PASSWORD_CHANGE': return '密码修改';
        case 'ADMIN': return '管理员操作';
        case 'SYSTEM': return '系统事件';
        case 'ERROR': return '错误';
        case 'WARNING': return '警告';
        case 'INFO': return '信息';
        default: return type;
    }
}

// 获取日志类型CSS类
function getLogTypeClass(type) {
    switch(type) {
        case 'LOGIN':
        case 'LOGOUT':
        case 'REGISTER':
            return 'log-type-action';
        case 'PASSWORD_CHANGE':
        case 'ADMIN':
            return 'log-type-admin';
        case 'SYSTEM':
            return 'log-type-system';
        case 'ERROR':
            return 'log-type-error';
        case 'WARNING':
            return 'log-type-warning';
        case 'INFO':
            return 'log-type-info';
        default:
            return 'log-type-default';
    }
}

// 更新用户增长图表
function updateUserGrowthChart(data) {
    // 处理图表数据
    const labels = [];
    const values = [];

    if (data && data.length > 0) {
        data.forEach(item => {
            labels.push(formatDate(item.date));
            values.push(item.count);
        });
    } else {
        // 如果没有数据，添加一个默认值
        labels.push('无数据');
        values.push(0);
    }

    // 获取Canvas上下文
    const ctx = document.getElementById('user-growth-chart').getContext('2d');

    // 销毁已存在的图表
    if (userGrowthChart) {
        userGrowthChart.destroy();
    }

    // 创建新图表
    userGrowthChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: '新注册用户',
                data: values,
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                tension: 0.1,
                borderWidth: 2,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    mode: 'index',
                    intersect: false
                }
            },
            scales: {
                x: {
                    display: true,
                    title: {
                        display: true,
                        text: '日期'
                    },
                    ticks: {
                        maxRotation: 45,
                        minRotation: 45
                    }
                },
                y: {
                    display: true,
                    title: {
                        display: true,
                        text: '用户数'
                    },
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });
}

// 更新用户活动图表
function updateActivityChart(data) {
    // 处理图表数据
    const categories = [];
    const values = [];

    if (data && data.length > 0) {
        data.forEach(item => {
            categories.push(getActivityTypeDisplay(item.type));
            values.push(item.count);
        });
    } else {
        // 如果没有数据，添加一个默认值
        categories.push('无数据');
        values.push(1);
    }

    // 获取Canvas上下文
    const ctx = document.getElementById('user-activity-chart').getContext('2d');

    // 销毁已存在的图表
    if (userActivityChart) {
        userActivityChart.destroy();
    }

    // 创建颜色��组
    const backgroundColors = [
        'rgba(255, 99, 132, 0.7)',
        'rgba(54, 162, 235, 0.7)',
        'rgba(255, 206, 86, 0.7)',
        'rgba(75, 192, 192, 0.7)',
        'rgba(153, 102, 255, 0.7)',
        'rgba(255, 159, 64, 0.7)',
        'rgba(199, 199, 199, 0.7)'
    ];

    // 创建新图表
    userActivityChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: categories,
            datasets: [{
                data: values,
                backgroundColor: backgroundColors.slice(0, categories.length),
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 12
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.raw || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = Math.round((value / total) * 100);
                            return `${label}: ${value} (${percentage}%)`;
                        }
                    }
                }
            },
            cutout: '65%'
        }
    });
}

// 格式化日期（年-月-日）
function formatDate(dateString) {
    if (!dateString) return '无效日期';

    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '无效日期';

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    } catch (e) {
        console.error('日期格式化错误', e);
        return '格式化错误';
    }
}

// 获取活动类型显示文本
function getActivityTypeDisplay(type) {
    switch(type) {
        case 'LOGIN': return '登录';
        case 'LOGOUT': return '登出';
        case 'REGISTER': return '注册';
        case 'PASSWORD_RESET': return '重置密码';
        case 'PROFILE_UPDATE': return '更新资料';
        case 'ADMIN_ACTION': return '管理操作';
        case 'API_ACCESS': return 'API访问';
        case 'VIEW': return '浏览';
        case 'DOWNLOAD': return '下载';
        case 'OTHER': return '其他';
        default: return type || '未知';
    }
}


