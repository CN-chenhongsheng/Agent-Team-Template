/**
 * 认证工具函数
 * @module utils/auth
 */

const TokenKey = 'ACCESS_TOKEN';
const RefreshTokenKey = 'REFRESH_TOKEN';
const TokenPrefix = 'Bearer ';

/**
 * 检查是否已登录
 */
function isLogin(): boolean {
  return !!uni.getStorageSync(TokenKey);
}

/**
 * 获取访问令牌
 */
function getToken(): string {
  return uni.getStorageSync(TokenKey) || '';
}

/**
 * 设置访问令牌
 */
function setToken(token: string): void {
  uni.setStorageSync(TokenKey, token);
}

/**
 * 清除访问令牌
 */
function clearToken(): void {
  uni.removeStorageSync(TokenKey);
}

/**
 * 获取刷新令牌
 */
function getRefreshToken(): string {
  return uni.getStorageSync(RefreshTokenKey) || '';
}

/**
 * 设置刷新令牌
 */
function setRefreshToken(token: string): void {
  uni.setStorageSync(RefreshTokenKey, token);
}

/**
 * 清除刷新令牌
 */
function clearRefreshToken(): void {
  uni.removeStorageSync(RefreshTokenKey);
}

/**
 * 清除所有令牌
 */
function clearAllTokens(): void {
  clearToken();
  clearRefreshToken();
}

export {
  TokenPrefix,
  isLogin,
  getToken,
  setToken,
  clearToken,
  getRefreshToken,
  setRefreshToken,
  clearRefreshToken,
  clearAllTokens,
};
