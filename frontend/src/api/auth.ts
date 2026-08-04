const API_KEY_STORAGE_KEY = 'gits_api_key'

/** 获取存储的API Key */
export function getApiKey(): string | null {
  return localStorage.getItem(API_KEY_STORAGE_KEY)
}

/** 设置API Key (登录) */
export function setApiKey(key: string): void {
  localStorage.setItem(API_KEY_STORAGE_KEY, key)
}

/** 清除API Key (登出) */
export function clearApiKey(): void {
  localStorage.removeItem(API_KEY_STORAGE_KEY)
}

/** 检查是否已认证 */
export function isAuthenticated(): boolean {
  const key = getApiKey()
  return key !== null && key.trim() !== ''
}

/** 登出: 清除认证并跳转到登录页 */
export function logout(): void {
  clearApiKey()
  window.location.href = '/login'
}
