<script setup lang="ts">
import ProposalDegradeShell from '../components/shell/ProposalDegradeShell.vue'
import { PROPOSAL_SHELL_PAGES } from '../composables/proposalDegrade'

const page = PROPOSAL_SHELL_PAGES.P24
</script>

<template>
  <ProposalDegradeShell v-bind="page" v-slot="{ wizard }">
    <section v-if="wizard" class="wizard" data-testid="p24-empty-draft">
      <p>空草稿：未创建正式建议书对象。{{ wizard.degradeLabel }}。</p>
      <ol class="steps">
        <li>选择客户（不可保存）</li>
        <li>选择需求/机会近似对象（不可保存）</li>
        <li>选择模板（不可保存）</li>
      </ol>
    </section>
  </ProposalDegradeShell>
</template>

<style scoped>
.wizard p {
  margin: 0 0 12px;
  color: var(--text-secondary);
}
.steps {
  margin: 0;
  padding-left: 20px;
  color: var(--text-tertiary);
  font-size: 13px;
}
</style>
