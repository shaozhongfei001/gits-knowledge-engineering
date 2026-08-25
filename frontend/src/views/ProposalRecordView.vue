<script setup lang="ts">
import ProposalDegradeShell from '../components/shell/ProposalDegradeShell.vue'
import { PROPOSAL_SHELL_PAGES } from '../composables/proposalDegrade'

const page = PROPOSAL_SHELL_PAGES.P25
</script>

<template>
  <ProposalDegradeShell v-bind="page" v-slot="{ placeholder }">
    <section v-if="placeholder" class="record">
      <dl class="facts">
        <div>
          <dt>占位对象 ID（路由 param，非正式）</dt>
          <dd>{{ placeholder.placeholderId }}</dd>
        </div>
        <div>
          <dt>对象标记</dt>
          <dd>{{ placeholder.degradeLabel }}</dd>
        </div>
        <div>
          <dt>合同说明</dt>
          <dd>{{ placeholder.contractNote }}</dd>
        </div>
      </dl>
      <section class="stage-notice" data-testid="p25-stage-notice">
        <p>阶段机 C3 未授权。本页仅静态说明，不提供可点晋级或回写。</p>
        <p>G0–G5 不是本分支合同对象，禁止把占位 ID 当作正式建议书阶段。</p>
      </section>
    </section>
  </ProposalDegradeShell>
</template>

<style scoped>
.facts {
  display: grid;
  gap: 12px;
  margin: 0 0 16px;
}
.facts div,
.stage-notice {
  padding: 12px 14px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: 8px;
}
dt {
  font-size: 12px;
  color: var(--text-tertiary);
}
dd {
  margin: 4px 0 0;
  font-size: 14px;
}
.stage-notice p {
  margin: 0 0 8px;
  color: var(--text-secondary);
  font-size: 13px;
}
.stage-notice p:last-child {
  margin-bottom: 0;
}
</style>
