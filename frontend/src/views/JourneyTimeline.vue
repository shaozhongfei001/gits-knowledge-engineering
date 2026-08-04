<template>
  <div class="journey-timeline">
    <div v-if="loading" class="loading-state">
      <n-spin size="large" />
    </div>

    <template v-else>
      <!-- 旅程头部 -->
      <div class="journey-header-section">
        <div class="journey-title-row">
          <n-button text @click="goBack">
            &larr; 返回
          </n-button>
          <h2>经营旅程</h2>
          <n-tag :type="phaseColor" size="medium">
            {{ phaseLabel }}
          </n-tag>
        </div>
        <div class="journey-meta-row">
          <span>客户: {{ journey?.customerName }}</span>
          <span>开始时间: {{ formatDate(journey?.startedAt) }}</span>
        </div>
        <PhaseIndicator v-if="journey" :current="journey.phase" />
      </div>

      <!-- 时间线内容 -->
      <div class="timeline-section">
        <h3 class="section-title">交互时间线</h3>
        <div v-if="interactions.length" class="timeline">
          <TimelineItem
            v-for="interaction in interactions"
            :key="interaction.interactionId"
            :title="interactionTypeLabel(interaction.type)"
            :subtitle="interaction.contentSummary"
            :time="interaction.occurredAt"
            :color="interactionColor(interaction)"
          >
            <div class="interaction-detail">
              <div class="detail-row">
                <span class="detail-label">方向:</span>
                <span>{{ interaction.direction === 'OUTBOUND' ? '出站' : '入站' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">渠道:</span>
                <span>{{ interaction.channel }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">发起人:</span>
                <span>{{ interaction.initiator.displayName }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">结果:</span>
                <span>{{ outcomeLabel(interaction.outcome) }}</span>
              </div>
              <div v-if="interaction.producedClaimIds?.length" class="detail-row">
                <span class="detail-label">产出主张:</span>
                <div class="claim-refs">
                  <n-tag v-for="cid in interaction.producedClaimIds" :key="cid" size="small" type="info">
                    {{ cid.slice(0, 8) }}
                  </n-tag>
                </div>
              </div>
            </div>
          </TimelineItem>
        </div>
        <n-empty v-else description="暂无交互记录" />
      </div>

      <!-- 主张列表 -->
      <div class="claims-section">
        <h3 class="section-title">主张与洞察</h3>
        <div v-if="claims.length" class="claims-list">
          <n-card v-for="claim in claims" :key="claim.claimId" size="small" class="claim-card">
            <div class="claim-header">
              <n-tag size="small" :type="claimStatusColor(claim.status)">{{ claim.status }}</n-tag>
              <n-tag size="small" type="info">{{ claim.claimType }}</n-tag>
            </div>
            <div class="claim-content">{{ claim.content }}</div>
          </n-card>
        </div>
        <n-empty v-else description="暂无主张记录" />
      </div>

      <!-- 机会信号 -->
      <div class="signals-section">
        <h3 class="section-title">机会信号</h3>
        <div v-if="signals.length" class="signal-list">
          <SignalCard v-for="signal in signals" :key="signal.signalId" :signal="signal" />
        </div>
        <n-empty v-else description="暂无机会信号" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NSpin, NButton, NTag, NCard, NEmpty } from 'naive-ui'
import PhaseIndicator from '../components/PhaseIndicator.vue'
import TimelineItem from '../components/TimelineItem.vue'
import SignalCard from '../components/SignalCard.vue'
import { fetchJourney, fetchJourneyInteractions, fetchJourneyClaims, fetchJourneySignals } from '../api/engagement'
import type { CustomerJourney, Interaction, Claim, OpportunitySignal, JourneyPhase, InteractionType, InteractionOutcome } from '../api/engagement'
import { JOURNEY_PHASE_LABELS, INTERACTION_TYPE_LABELS, OUTCOME_LABELS } from '../api/engagement'

const route = useRoute()
const router = useRouter()

const journey = ref<CustomerJourney | null>(null)
const interactions = ref<Interaction[]>([])
const claims = ref<Claim[]>([])
const signals = ref<OpportunitySignal[]>([])
const loading = ref(true)

const phaseLabel = computed(() =>
  journey.value ? JOURNEY_PHASE_LABELS[journey.value.phase as JourneyPhase] : ''
)

const phaseColor = computed((): 'success' | 'warning' | 'info' | 'default' => {
  switch (journey.value?.phase) {
    case 'COMPLETED': return 'success'
    case 'POSTVISIT_REVIEW': return 'warning'
    case 'KYC_COLLECT': return 'info'
    default: return 'default'
  }
})

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  try {
    return new Date(dateStr).toLocaleString('zh-CN')
  } catch {
    return dateStr
  }
}

function interactionTypeLabel(type: InteractionType): string {
  return INTERACTION_TYPE_LABELS[type] || type
}

function outcomeLabel(outcome: InteractionOutcome): string {
  return OUTCOME_LABELS[outcome] || outcome
}

function interactionColor(interaction: Interaction): string {
  if (interaction.type === 'FACE_TO_FACE_VISIT') return '#b8860b'
  if (interaction.type === 'AI_INSIGHT_PUSH') return '#003366'
  if (interaction.direction === 'INBOUND') return '#389e0d'
  return '#666'
}

function claimStatusColor(status: string): 'success' | 'warning' | 'error' | 'info' | 'default' {
  switch (status) {
    case 'VERIFIED_FACT': return 'success'
    case 'CANDIDATE': return 'warning'
    case 'DISPUTED': return 'error'
    case 'REJECTED': return 'default'
    default: return 'info'
  }
}

function goBack() {
  router.back()
}

onMounted(async () => {
  const journeyId = route.params.id as string
  try {
    const [journeyData, interactionsData, claimsData, signalsData] = await Promise.all([
      fetchJourney(journeyId),
      fetchJourneyInteractions(journeyId),
      fetchJourneyClaims(journeyId),
      fetchJourneySignals(journeyId)
    ])
    journey.value = journeyData
    interactions.value = interactionsData
    claims.value = claimsData
    signals.value = signalsData
  } catch (e) {
    console.error('Failed to load journey:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.journey-timeline {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}
.loading-state {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.journey-header-section {
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.journey-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.journey-title-row h2 {
  font-size: 20px;
  color: #003366;
  margin: 0;
}
.journey-meta-row {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}
.section-title {
  font-size: 16px;
  color: #003366;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #b8860b;
  display: inline-block;
}
.timeline-section,
.claims-section,
.signals-section {
  margin-bottom: 24px;
}
.timeline {
  padding-left: 8px;
}
.interaction-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.detail-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
}
.detail-label {
  color: #8c8c8c;
  min-width: 60px;
}
.claim-refs {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.claims-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.claim-card {
  border-left: 3px solid #003366;
}
.claim-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.claim-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}
.signal-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
