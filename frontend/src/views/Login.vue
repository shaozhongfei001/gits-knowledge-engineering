<template>
  <div class="login-container">
    <div class="login-card surface-card-elevated">
      <div class="login-brand">
        <div class="brand-mark">G</div>
        <div class="brand-info">
          <h1 class="login-title">GITS 客户经营闭环</h1>
          <p class="login-subtitle">Knowledge Engineering Platform</p>
        </div>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-field">
          <label class="form-label">API Key</label>
          <input
            v-model="apiKey"
            type="password"
            placeholder="请输入 API Key"
            class="login-input"
            autofocus
          />
        </div>
        <p v-if="error" class="login-error">{{ error }}</p>
        <button type="submit" class="btn btn-primary login-btn" :disabled="!apiKey.trim()">
          登录
        </button>
        <div class="login-divider">
          <span>或</span>
        </div>
        <button type="button" class="btn btn-secondary dev-btn" @click="handleDevLogin">
          开发模式直接进入
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { setApiKey } from '../api/auth'

const router = useRouter()
const apiKey = ref('')
const error = ref('')

function handleLogin() {
  if (!apiKey.value.trim()) {
    error.value = 'API Key 不能为空'
    return
  }
  setApiKey(apiKey.value.trim())
  error.value = ''
  const redirect = (router.currentRoute.value.query.redirect as string) || '/'
  router.push(redirect)
}

function handleDevLogin() {
  setApiKey('dev-mode')
  const redirect = (router.currentRoute.value.query.redirect as string) || '/'
  router.push(redirect)
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-page);
}

.login-card {
  padding: var(--space-10);
  width: 400px;
  animation: fadeIn 0.3s ease-out;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: var(--brand-primary);
  color: var(--text-inverse);
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-info {
  display: flex;
  flex-direction: column;
}

.login-title {
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.login-subtitle {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin: 0;
  letter-spacing: 0.5px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.form-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-secondary);
}

.login-input {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border-normal);
  border-radius: var(--radius-sm);
  font-size: var(--text-base);
  color: var(--text-primary);
  background: var(--bg-surface);
  outline: none;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.login-input:focus {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px var(--brand-primary-lighter);
}

.login-input::placeholder {
  color: var(--text-tertiary);
}

.login-error {
  color: var(--color-danger);
  font-size: var(--text-xs);
  margin: 0;
  padding: var(--space-1) 0;
}

.login-btn {
  width: 100%;
  padding: var(--space-3);
  font-size: var(--text-base);
  margin-top: var(--space-1);
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.login-divider {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--text-tertiary);
  font-size: var(--text-xs);
}

.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-light);
}

.dev-btn {
  width: 100%;
  padding: var(--space-3);
  font-size: var(--text-sm);
}
</style>
