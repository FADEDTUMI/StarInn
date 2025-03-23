/**
 * 全局主题管理器
 * 在所有页面中使用此脚本确保主题一致性
 */
class ThemeManager {
    constructor() {
        this.themeKey = 'theme';
        this.defaultTheme = 'light';
    }

    // 初始化主题
    init() {
        const savedTheme = localStorage.getItem(this.themeKey) || this.defaultTheme;
        this.applyTheme(savedTheme);
        this.setupThemeToggle();
    }

    // 应用主题到页面
    applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem(this.themeKey, theme);
        this.updateThemeIcons(theme);
    }

    // 切换主题
    toggleTheme() {
        const currentTheme = localStorage.getItem(this.themeKey) || this.defaultTheme;
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        this.applyTheme(newTheme);
    }

    // 更新页面上所有主题切换按钮的图标
    updateThemeIcons(theme) {
        const themeButtons = document.querySelectorAll('.theme-switch');
        themeButtons.forEach(button => {
            const icon = button.querySelector('i');
            if (icon) {
                if (theme === 'dark') {
                    icon.className = 'fas fa-sun';
                } else {
                    icon.className = 'fas fa-moon';
                }
            }
        });
    }

    // 设置主题切换按钮事件
    setupThemeToggle() {
        const themeButtons = document.querySelectorAll('.theme-switch');
        themeButtons.forEach(button => {
            button.addEventListener('click', () => this.toggleTheme());
        });
    }
}

// 创建全局实例并导出
const themeManager = new ThemeManager();

// 页面加载完成后初始化主题
document.addEventListener('DOMContentLoaded', () => {
    themeManager.init();
});