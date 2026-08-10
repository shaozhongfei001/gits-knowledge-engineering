import axios from 'axios'
import { getApiKey, clearApiKey } from './auth'

// V1.1 新增实体的API客户端
const v11Api = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

v11Api.interceptors.request.use((config) => {
  const apiKey = getApiKey()
  if (apiKey) {
    config.headers['X-API-KEY'] = apiKey
  }
  return config
})

v11Api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearApiKey()
      window.dispatchEvent(new CustomEvent('auth:unauthorized'))
    }
    return Promise.reject(error)
  }
)

// ===================== 类型定义 =====================

export type CommitmentType = 'RM_COMMITMENT' | 'CUSTOMER_COMMITMENT' | 'REGULATORY_COMMITMENT'
export type CommitmentStatus = 'OPEN' | 'FULFILLED' | 'BREACHED' | 'WAIVED'

export type TaskType = 'FOLLOW_UP' | 'DOCUMENT_COLLECTION' | 'APPROVAL' | 'REVIEW' | 'COMPLIANCE_CHECK'
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type OpportunityType = 'FINANCING' | 'DEPOSIT' | 'SETTLEMENT' | 'INVESTMENT' | 'INSURANCE' | 'ADVISORY'
export type OpportunityStage = 'IDENTIFIED' | 'QUALIFIED' | 'PROPOSAL' | 'NEGOTIATION' | 'CLOSED_WON' | 'CLOSED_LOST'

export type ExternalEventType = 'OFFICIAL_ANNOUNCEMENT' | 'INDUSTRY_REPORT' | 'NEWS' | 'REGULATORY_UPDATE' | 'MARKET_EVENT'
export type ExternalEventSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export type RecordingConsentStatus = 'GRANTED' | 'DENIED' | 'PENDING' | 'REVOKED'

export interface Commitment {
  commitmentId: string
  operatingCaseId?: string
  journeyId?: string
  commitmentType: CommitmentType
  content: string
  owner?: string
  dueDate?: string
  status: CommitmentStatus
  evidenceRef?: string
  createdAt?: string
  fulfilledAt?: string
}

export interface Task {
  taskId: string
  interactionId?: string
  operatingCaseId?: string
  customerId?: string
  taskType: TaskType
  title: string
  description?: string
  assignedTo?: string
  dueDate?: string
  status: TaskStatus
  parentTaskId?: string
  createdAt?: string
  completedAt?: string
}

export interface Opportunity {
  opportunityId: string
  customerId: string
  opportunityType: OpportunityType
  stage: OpportunityStage
  estimatedAmount?: number
  currency?: string
  description: string
  assignedTo?: string
  sourceSignalId?: string
  probability?: number
  expectedCloseDate?: string
  createdAt?: string
  updatedAt?: string
}

export interface ExternalEvent {
  eventId: string
  eventDate: string
  sourceType: ExternalEventType
  sourceName?: string
  entity?: string
  title: string
  content: string
  confidence?: string
  reliability?: string
  bankUseAllowed?: boolean
  linkedThemes?: string[]
  possibleBusinessSignal?: string
  noGoStatement?: string
  evidenceRef?: string
}

export interface ProductKnowledgeVersion {
  versionId: string
  productId: string
  version: string
  category?: string
  content: string
  changeSummary?: string
  effectiveFrom: string
  effectiveTo?: string
  createdAt?: string
}

export interface RecordingConsent {
  consentId: string
  interactionId: string
  customerId?: string
  consentStatus: RecordingConsentStatus
  consentMethod?: string
  recordingPurpose?: string
  grantedAt?: string
  revokedAt?: string
}

// ===================== Commitment API =====================

export async function fetchCommitment(commitmentId: string): Promise<Commitment> {
  const { data } = await v11Api.get(`/commitments/${commitmentId}`)
  return data
}

export async function fetchCommitments(params: {
  interactionId?: string
  customerId?: string
  status?: CommitmentStatus
}): Promise<Commitment[]> {
  const { data } = await v11Api.get('/commitments', { params })
  return data
}

export async function fetchOverdueCommitments(): Promise<Commitment[]> {
  const { data } = await v11Api.get('/commitments/overdue')
  return data
}

export async function createCommitment(commitment: Partial<Commitment>): Promise<Commitment> {
  const { data } = await v11Api.post('/commitments', commitment)
  return data
}

export async function updateCommitmentStatus(commitmentId: string, status: CommitmentStatus): Promise<void> {
  await v11Api.put(`/commitments/${commitmentId}/status`, { status })
}

// ===================== Task API =====================

export async function fetchTask(taskId: string): Promise<Task> {
  const { data } = await v11Api.get(`/tasks/${taskId}`)
  return data
}

export async function fetchTasks(params: {
  interactionId?: string
  customerId?: string
  operatingCaseId?: string
  assignedTo?: string
}): Promise<Task[]> {
  const { data } = await v11Api.get('/tasks', { params })
  return data
}

export async function fetchOverdueTasks(): Promise<Task[]> {
  const { data } = await v11Api.get('/tasks/overdue')
  return data
}

export async function createTask(task: Partial<Task>): Promise<Task> {
  const { data } = await v11Api.post('/tasks', task)
  return data
}

export async function updateTaskStatus(taskId: string, status: TaskStatus): Promise<void> {
  await v11Api.put(`/tasks/${taskId}/status`, { status })
}

// ===================== Opportunity API =====================

export async function fetchOpportunity(opportunityId: string): Promise<Opportunity> {
  const { data } = await v11Api.get(`/opportunities/${opportunityId}`)
  return data
}

export async function fetchOpportunities(params: {
  customerId?: string
  status?: OpportunityStage
  opportunityType?: OpportunityType
  assignedTo?: string
}): Promise<Opportunity[]> {
  const { data } = await v11Api.get('/opportunities', { params })
  return data
}

export async function fetchActiveOpportunities(customerId: string): Promise<Opportunity[]> {
  const { data } = await v11Api.get(`/opportunities/${customerId}/active`)
  return data
}

export async function createOpportunity(opportunity: Partial<Opportunity>): Promise<Opportunity> {
  const { data } = await v11Api.post('/opportunities', opportunity)
  return data
}

export async function updateOpportunityStage(opportunityId: string, stage: OpportunityStage): Promise<void> {
  await v11Api.put(`/opportunities/${opportunityId}/status`, { stage })
}

// ===================== ExternalEvent API =====================

export async function fetchExternalEvent(eventId: string): Promise<ExternalEvent> {
  const { data } = await v11Api.get(`/external-events/${eventId}`)
  return data
}

export async function fetchExternalEvents(params: {
  eventType?: ExternalEventType
  customerId?: string
  industry?: string
  severity?: ExternalEventSeverity
}): Promise<ExternalEvent[]> {
  const { data } = await v11Api.get('/external-events', { params })
  return data
}

export async function fetchRecentExternalEvents(limit: number = 20): Promise<ExternalEvent[]> {
  const { data } = await v11Api.get('/external-events/recent', { params: { limit } })
  return data
}

export async function createExternalEvent(event: Partial<ExternalEvent>): Promise<ExternalEvent> {
  const { data } = await v11Api.post('/external-events', event)
  return data
}

// ===================== ProductKnowledgeVersion API =====================

export async function fetchProductKnowledgeVersion(versionId: string): Promise<ProductKnowledgeVersion> {
  const { data } = await v11Api.get(`/product-knowledge/versions/${versionId}`)
  return data
}

export async function fetchProductKnowledgeVersions(params: {
  productId?: string
  category?: string
}): Promise<ProductKnowledgeVersion[]> {
  const { data } = await v11Api.get('/product-knowledge', { params })
  return data
}

export async function fetchLatestProductKnowledge(productId: string): Promise<ProductKnowledgeVersion> {
  const { data } = await v11Api.get(`/product-knowledge/latest/${productId}`)
  return data
}

export async function fetchRecentProductKnowledge(limit: number = 20): Promise<ProductKnowledgeVersion[]> {
  const { data } = await v11Api.get('/product-knowledge/recent', { params: { limit } })
  return data
}

export async function createProductKnowledgeVersion(version: Partial<ProductKnowledgeVersion>): Promise<ProductKnowledgeVersion> {
  const { data } = await v11Api.post('/product-knowledge', version)
  return data
}

// ===================== RecordingConsent API =====================

export async function fetchRecordingConsent(consentId: string): Promise<RecordingConsent> {
  const { data } = await v11Api.get(`/recording-consents/${consentId}`)
  return data
}

export async function fetchRecordingConsents(params: {
  interactionId?: string
  customerId?: string
  status?: RecordingConsentStatus
}): Promise<RecordingConsent[]> {
  const { data } = await v11Api.get('/recording-consents', { params })
  return data
}

export async function fetchLatestRecordingConsent(interactionId: string): Promise<RecordingConsent> {
  const { data } = await v11Api.get(`/recording-consents/latest/${interactionId}`)
  return data
}

export async function createRecordingConsent(consent: Partial<RecordingConsent>): Promise<RecordingConsent> {
  const { data } = await v11Api.post('/recording-consents', consent)
  return data
}

export async function updateRecordingConsentStatus(consentId: string, status: RecordingConsentStatus): Promise<void> {
  await v11Api.put(`/recording-consents/${consentId}/status`, { status })
}

// ===================== 枚举映射 =====================

export const COMMITMENT_TYPE_LABELS: Record<CommitmentType, string> = {
  RM_COMMITMENT: '客户经理承诺',
  CUSTOMER_COMMITMENT: '客户承诺',
  REGULATORY_COMMITMENT: '合规承诺'
}

export const COMMITMENT_STATUS_LABELS: Record<CommitmentStatus, string> = {
  OPEN: '待履行',
  FULFILLED: '已履行',
  BREACHED: '已违约',
  WAIVED: '已豁免'
}

export const TASK_TYPE_LABELS: Record<TaskType, string> = {
  FOLLOW_UP: '跟进',
  DOCUMENT_COLLECTION: '文档收集',
  APPROVAL: '审批',
  REVIEW: '审核',
  COMPLIANCE_CHECK: '合规检查'
}

export const TASK_STATUS_LABELS: Record<TaskStatus, string> = {
  PENDING: '待处理',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

export const OPPORTUNITY_TYPE_LABELS: Record<OpportunityType, string> = {
  FINANCING: '融资',
  DEPOSIT: '存款',
  SETTLEMENT: '结算',
  INVESTMENT: '投资',
  INSURANCE: '保险',
  ADVISORY: '咨询'
}

export const OPPORTUNITY_STAGE_LABELS: Record<OpportunityStage, string> = {
  IDENTIFIED: '已识别',
  QUALIFIED: '已确认',
  PROPOSAL: '方案提议',
  NEGOTIATION: '谈判中',
  CLOSED_WON: '已赢单',
  CLOSED_LOST: '已输单'
}

export const EXTERNAL_EVENT_TYPE_LABELS: Record<ExternalEventType, string> = {
  OFFICIAL_ANNOUNCEMENT: '官方公告',
  INDUSTRY_REPORT: '行业报告',
  NEWS: '新闻',
  REGULATORY_UPDATE: '监管更新',
  MARKET_EVENT: '市场事件'
}

export const EXTERNAL_EVENT_SEVERITY_LABELS: Record<ExternalEventSeverity, string> = {
  CRITICAL: '严重',
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低'
}

export const RECORDING_CONSENT_STATUS_LABELS: Record<RecordingConsentStatus, string> = {
  GRANTED: '已授权',
  DENIED: '已拒绝',
  PENDING: '待确认',
  REVOKED: '已撤销'
}
