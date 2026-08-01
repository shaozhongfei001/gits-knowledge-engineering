<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Alert as TAlert, Empty as TEmpty, Loading as TLoading, Tag as TTag } from 'tdesign-vue-next'
import 'tdesign-vue-next/es/alert/style/index.css'
import 'tdesign-vue-next/es/empty/style/index.css'
import 'tdesign-vue-next/es/loading/style/index.css'
import 'tdesign-vue-next/es/tag/style/index.css'
import { loadArchitectureStatus, type LoadState } from './architectureStatus'

const state = ref<LoadState>({ kind: 'loading' })

onMounted(async () => {
  state.value = await loadArchitectureStatus()
})
</script>

<template>
  <main class="shell">
    <header class="hero">
      <div>
        <p class="eyebrow">GITS · KNOWLEDGE ENGINEERING</p>
        <h1>知识工程与岗位智能体基础能力</h1>
        <p class="subtitle">可编译语义合同 · 运行本体控制平面 · 可重建投影</p>
      </div>
      <t-tag theme="warning" variant="light">工程候选</t-tag>
    </header>

    <section class="status-panel" aria-live="polite">
      <t-loading v-if="state.kind === 'loading'" text="正在核对工程状态…" />
      <t-empty v-else-if="state.kind === 'empty'" description="状态服务未返回内容" />
      <t-alert v-else-if="state.kind === 'timeout'" theme="warning" :message="state.message" />
      <t-alert v-else-if="state.kind === 'error'" theme="error" :message="state.message" />
      <template v-else>
        <div class="status-grid">
          <div><span>开发包</span><strong>{{ state.value.packageId }}</strong></div>
          <div><span>当前状态</span><strong>{{ state.value.state }}</strong></div>
          <div><span>生产就绪</span><strong>否</strong></div>
          <div><span>已冻结</span><strong>否</strong></div>
        </div>
      </template>
    </section>

    <section class="capabilities">
      <article>
        <span>01</span><h2>规范语义</h2><p>LinkML、OWL、SKOS与SHACL形成可审查、可编译、可版本化的语义合同。</p>
      </article>
      <article>
        <span>02</span><h2>责任链</h2><p>Case、Interaction、Claim、Evidence、人工确认、Action与回执保持可追溯。</p>
      </article>
      <article>
        <span>03</span><h2>真实门禁</h2><p>合同、构建、证据、安全和E2E必须以可执行命令与物证通过，拒绝假绿。</p>
      </article>
    </section>

    <footer>总体22模块保持全景覆盖；首期仅验证客户持续经营六阶段主链。</footer>
  </main>
</template>
