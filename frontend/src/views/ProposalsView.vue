<script setup lang="ts">
import ProposalDegradeShell from '../components/shell/ProposalDegradeShell.vue'
import { PROPOSAL_SHELL_PAGES } from '../composables/proposalDegrade'

const page = PROPOSAL_SHELL_PAGES.P23
</script>

<template>
  <ProposalDegradeShell v-bind="page">
    <p class="empty" data-testid="p23-empty">暂无建议书对象。本页不编造建议书内容。</p>
    <router-link class="link" to="/proposals/new" data-testid="p23-open-wizard">
      进入新建向导（降级壳）
    </router-link>
  </ProposalDegradeShell>
</template>

<style scoped>
.empty,
.link {
  display: block;
  color: var(--text-tertiary);
  font-size: 13px;
  margin: 0 0 12px;
}
.link {
  color: var(--brand-primary);
  width: fit-content;
}
</style>
