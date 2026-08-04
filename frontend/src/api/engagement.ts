import axios from 'axios'
import { getApiKey, clearApiKey } from './auth'

const api = axios.create({
  baseURL: '/api/v1/engagement',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器: 注入API Key
api.interceptors.request.use((config) => {
  const apiKey = getApiKey()
  if (apiKey) {
    config.headers['X-API-KEY'] = apiKey
  }
  return config
})

// 响应拦截器: 401时清除认证并提示
api.interceptors.response.use(
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

export type RiskLevel = 'HIGH' | 'MEDIUM' | 'LOW'
export type Industry = 'MANUFACTURING' | 'FINANCE' | 'TECHNOLOGY' | 'REAL_ESTATE' | 'ENERGY' | 'HEALTHCARE' | 'AGRICULTURE' | 'LOGISTICS' | 'RETAIL' | 'OTHER'
export type EnterpriseScale = 'LARGE' | 'MEDIUM' | 'SMALL' | 'MICRO'
export type CustomerTier = 'STRATEGIC' | 'KEY' | 'GROWTH' | 'GENERAL'
export type ListedStatus = 'LISTED' | 'UNLISTED' | 'DELISTED'

export type JourneyPhase = 'KYC_COLLECT' | 'INSIGHT_ANALYSIS' | 'PRODUCT_MATCHING' | 'PREVISIT_PREP' | 'POSTVISIT_REVIEW' | 'COMPLETED'

export type SignalType = 'FINANCING_NEED' | 'PRODUCT_OPPORTUNITY' | 'RELATIONSHIP_CHANGE'
export type SignalSourceType = 'INTERACTION' | 'EXTERNAL_EVENT' | 'ANALYSIS'
export type SignalStatus = 'DETECTED' | 'CONFIRMED' | 'DISMISSED' | 'CONVERTED'

export type ReportType = 'INTERNAL_RELATIONSHIP' | 'CRM_CALL' | 'UPDATED_RELATIONSHIP' | 'NEXT_PREVISIT'

export type InteractionType = 'SIGNAL_TRIGGER' | 'AI_INSIGHT_PUSH' | 'PHONE_CALL' | 'FACE_TO_FACE_VISIT' | 'VIDEO_CONFERENCE' | 'INSTANT_MESSAGE' | 'EMAIL' | 'PRODUCT_PRESENTATION' | 'CUSTOMER_COMPLAINT' | 'FOLLOW_UP'
export type Direction = 'OUTBOUND' | 'INBOUND'
export type InteractionOutcome = 'COMPLETED' | 'CUSTOMER_AGREED' | 'CUSTOMER_DECLINED' | 'CUSTOMER_DEFERRED' | 'FOLLOW_UP_REQUIRED' | 'INTERRUPTED' | 'INFORMATION_GATHERED'

export interface Customer {
  customerId: string
  customerName: string
  customerShortName?: string
  unifiedSocialCreditCode?: string
  establishedDate?: string
  registeredCapitalCny?: number
  industry?: Industry
  region?: string
  enterpriseScale?: EnterpriseScale
  customerTier?: CustomerTier
  relationshipSince?: string
  rmId?: string
  rmName?: string
  managingBranch?: string
  groupFlag?: boolean
  listedStatus?: ListedStatus
  riskLevel?: RiskLevel
  mainProducts?: string[]
  coreTags?: string[]
  relationshipSummary?: string
  createdAt?: string
  updatedAt?: string
}

export interface CustomerJourney {
  journeyId: string
  operatingCaseId: string
  customerId: string
  customerName: string
  phase: JourneyPhase
  startedAt: string
  updatedAt?: string
}

export interface OperatingCase {
  caseId: string
  customerId: string
  caseType: string
  status: string
  createdAt?: string
  updatedAt?: string
}

export interface KycGapProfile {
  profileId: string
  customerId: string
  asOf: string
  knownItems: string[]
  partialKnownItems: string[]
  staleItems: string[]
  conflictingOrAmbiguousItems: string[]
  unknownItems: string[]
  priorityQuestions: string[]
}

export interface OpportunitySignal {
  signalId: string
  operatingCaseId?: string
  journeyId?: string
  signalType: SignalType
  content: string
  sourceType: SignalSourceType
  sourceRef?: string
  confidence?: number
  status: SignalStatus
  evidenceRef?: string
  detectedAt: string
  confirmedAt?: string
}

export interface RelationshipReport {
  reportId: string
  operatingCaseId?: string
  journeyId?: string
  reportType: ReportType
  content: string
  basedOnEvidence?: string[]
  basedOnReconciliations?: string[]
  generatedAt: string
  supersedesReportId?: string
}

export interface Interaction {
  interactionId: string
  caseId: string
  journeyId?: string
  type: InteractionType
  direction: Direction
  channel: string
  initiator: Participant
  participants?: Participant[]
  contentSummary?: string
  producedClaimIds?: string[]
  outcome: InteractionOutcome
  occurredAt: string
  endedAt?: string
}

export interface Participant {
  participantId: string
  role: string
  displayName: string
}

export interface Claim {
  claimId: string
  operatingCaseId?: string
  journeyId?: string
  claimType: string
  content: string
  status: string
  evidenceRefs?: string[]
  createdAt?: string
}

export interface TransactionRecord {
  transactionId: string
  customerId: string
  amount: number
  currency: string
  transactionType: string
  occurredAt: string
  description?: string
}

export interface CustomerContext {
  customer: Customer
  kycGapProfile?: KycGapProfile
  opportunitySignals: OpportunitySignal[]
  recentInteractions: Interaction[]
  activeJourneys: CustomerJourney[]
  recentTransactions: TransactionRecord[]
}

export interface PrevisitReport {
  reportId: string
  customerOverview: Customer
  kycGaps: KycGapProfile
  productRecommendations: string[]
  opportunitySignals: OpportunitySignal[]
  previousVisitSummary?: string
}

export interface PostvisitReport {
  reportId: string
  keyFindings: string[]
  opportunitySignals: OpportunitySignal[]
  commitments: string[]
  factReconciliation: { item: string; status: string }[]
  nextSteps: string[]
}

export interface EngagementScript {
  scriptId: string
  scriptType: 'OUTREACH' | 'MEETING'
  customerId: string
  content: string
  generatedAt: string
}

// ===================== API 调用 =====================

/** 获取客户列表 */
export async function fetchCustomers(): Promise<Customer[]> {
  const { data } = await api.get('/customers')
  return data
}

/** 获取客户详情 */
export async function fetchCustomer(customerId: string): Promise<Customer> {
  const { data } = await api.get(`/customers/${customerId}`)
  return data
}

/** 获取客户上下文（含KYC、信号、交互、旅程、交易） */
export async function fetchCustomerContext(customerId: string): Promise<CustomerContext> {
  const { data } = await api.get(`/customers/${customerId}/context`)
  return data
}

/** 获取客户旅程列表 */
export async function fetchCustomerJourneys(customerId: string): Promise<CustomerJourney[]> {
  const { data } = await api.get(`/customers/${customerId}/journeys`)
  return data
}

/** 获取旅程详情 */
export async function fetchJourney(journeyId: string): Promise<CustomerJourney> {
  const { data } = await api.get(`/journeys/${journeyId}`)
  return data
}

/** 获取旅程交互记录 */
export async function fetchJourneyInteractions(journeyId: string): Promise<Interaction[]> {
  const { data } = await api.get(`/journeys/${journeyId}/interactions`)
  return data
}

/** 获取旅程主张列表 */
export async function fetchJourneyClaims(journeyId: string): Promise<Claim[]> {
  const { data } = await api.get(`/journeys/${journeyId}/claims`)
  return data
}

/** 获取旅程机会信号 */
export async function fetchJourneySignals(journeyId: string): Promise<OpportunitySignal[]> {
  const { data } = await api.get(`/journeys/${journeyId}/signals`)
  return data
}

/** 获取报告详情 */
export async function fetchReport(reportId: string): Promise<RelationshipReport> {
  const { data } = await api.get(`/reports/${reportId}`)
  return data
}

/** 获取经营案例列表 */
export async function fetchOperatingCases(customerId: string): Promise<OperatingCase[]> {
  const { data } = await api.get(`/customers/${customerId}/cases`)
  return data
}

/** 执行访前报告生成 */
export async function executePrevisit(caseId: string): Promise<PrevisitReport> {
  const { data } = await api.post(`/cases/${caseId}/previsit`)
  return data
}

/** 执行访后分析 */
export async function executePostvisit(caseId: string, interactionId: string): Promise<PostvisitReport> {
  const { data } = await api.post(`/cases/${caseId}/postvisit`, { interactionId })
  return data
}

/** 生成外联脚本 */
export async function generateOutreachScript(customerId: string): Promise<EngagementScript> {
  const { data } = await api.post(`/customers/${customerId}/scripts/outreach`)
  return data
}

/** 生成会面脚本 */
export async function generateMeetingScript(customerId: string): Promise<EngagementScript> {
  const { data } = await api.post(`/customers/${customerId}/scripts/meeting`)
  return data
}

/** 获取KYC缺口画像 */
export async function fetchKycGapProfile(customerId: string): Promise<KycGapProfile> {
  const { data } = await api.get(`/customers/${customerId}/kyc-gap`)
  return data
}

/** 获取机会信号列表 */
export async function fetchOpportunitySignals(customerId: string): Promise<OpportunitySignal[]> {
  const { data } = await api.get(`/customers/${customerId}/signals`)
  return data
}

/** 获取交易流水 */
export async function fetchTransactions(customerId: string): Promise<TransactionRecord[]> {
  const { data } = await api.get(`/customers/${customerId}/transactions`)
  return data
}

// ===================== 枚举映射 =====================

export const INDUSTRY_LABELS: Record<Industry, string> = {
  MANUFACTURING: '制造业',
  FINANCE: '金融业',
  TECHNOLOGY: '科技业',
  REAL_ESTATE: '房地产',
  ENERGY: '能源业',
  HEALTHCARE: '医疗健康',
  AGRICULTURE: '农业',
  LOGISTICS: '物流业',
  RETAIL: '零售业',
  OTHER: '其他'
}

export const ENTERPRISE_SCALE_LABELS: Record<EnterpriseScale, string> = {
  LARGE: '大型',
  MEDIUM: '中型',
  SMALL: '小型',
  MICRO: '微型'
}

export const CUSTOMER_TIER_LABELS: Record<CustomerTier, string> = {
  STRATEGIC: '战略客户',
  KEY: '重点客户',
  GROWTH: '成长客户',
  GENERAL: '一般客户'
}

export const RISK_LEVEL_LABELS: Record<RiskLevel, string> = {
  HIGH: '高风险',
  MEDIUM: '中风险',
  LOW: '低风险'
}

export const JOURNEY_PHASE_LABELS: Record<JourneyPhase, string> = {
  KYC_COLLECT: 'KYC信息采集',
  INSIGHT_ANALYSIS: '洞察分析',
  PRODUCT_MATCHING: '产品匹配',
  PREVISIT_PREP: '访前准备',
  POSTVISIT_REVIEW: '访后复盘',
  COMPLETED: '已完成'
}

export const SIGNAL_TYPE_LABELS: Record<SignalType, string> = {
  FINANCING_NEED: '融资需求',
  PRODUCT_OPPORTUNITY: '产品机会',
  RELATIONSHIP_CHANGE: '关系变化'
}

export const SIGNAL_STATUS_LABELS: Record<SignalStatus, string> = {
  DETECTED: '已检测',
  CONFIRMED: '已确认',
  DISMISSED: '已排除',
  CONVERTED: '已转化'
}

export const REPORT_TYPE_LABELS: Record<ReportType, string> = {
  INTERNAL_RELATIONSHIP: 'R5A 内部关系报告',
  CRM_CALL: 'R5B CRM通话报告',
  UPDATED_RELATIONSHIP: 'R7 更新关系报告',
  NEXT_PREVISIT: 'R8 下一轮访前报告'
}

export const INTERACTION_TYPE_LABELS: Record<InteractionType, string> = {
  SIGNAL_TRIGGER: '信号触发',
  AI_INSIGHT_PUSH: 'AI洞察推送',
  PHONE_CALL: '电话沟通',
  FACE_TO_FACE_VISIT: '面对面拜访',
  VIDEO_CONFERENCE: '视频会议',
  INSTANT_MESSAGE: '即时消息',
  EMAIL: '邮件往来',
  PRODUCT_PRESENTATION: '产品推介',
  CUSTOMER_COMPLAINT: '客户投诉',
  FOLLOW_UP: '回访跟进'
}

export const OUTCOME_LABELS: Record<InteractionOutcome, string> = {
  COMPLETED: '已完成',
  CUSTOMER_AGREED: '客户同意',
  CUSTOMER_DECLINED: '客户拒绝',
  CUSTOMER_DEFERRED: '客户延后',
  FOLLOW_UP_REQUIRED: '需跟进',
  INTERRUPTED: '中断',
  INFORMATION_GATHERED: '信息收集'
}
