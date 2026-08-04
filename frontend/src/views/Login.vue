<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">GITS 客户经营闭环</h1>
      <p class="login-subtitle">请输入 API Key 以访问系统</p>
      <form @submit.prevent="handleLogin" class="login-form">
        <input
          v-model="apiKey"
          type="password"
          placeholder="API Key"
          class="login-input"
          autofocus
        />
        <p v-if="error" class="login-error">{{ error }}</p>
        <button type="submit" class="login-btn" :disabled="!apiKey.trim()">
          登录
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { setApiKey, isAuthenticated } from '../api/auth'

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
  router.push('/')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.login-card {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 360px;
}

.login-title {
  text-align: center;
  font-size: 20px;
  margin-bottom: 8px;
  color: #1a1a1a;
}

.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.login-input {
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.login-input:focus {
  border-color: #1890ff;
}

.login-error {
  color: #ff4d4f;
  font-size: 12px;
  margin: 0;
}

.login-btn {
  padding: 10px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.login-btn:hover:not(:disabled) {
  background: #40a9ff;
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
