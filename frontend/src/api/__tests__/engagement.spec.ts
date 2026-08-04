import { describe, it, expect, vi, beforeEach } from 'vitest'
import type {
  RiskLevel,
  Industry,
  EnterpriseScale,
  CustomerTier,
  JourneyPhase,
  SignalType,
  SignalStatus,
  ReportType,
  InteractionType,
  InteractionOutcome,
  Customer,
  CustomerJourney,
  OpportunitySignal,
  Interaction,
  Claim,
  TransactionRecord,
} from '../engagement'

// Mock axios with inline mock functions (hoisted to top by vitest)
vi.mock('axios', () => {
  const get = vi.fn()
  const post = vi.fn()
  return {
    default: {
      create: vi.fn(() => ({
        get,
        post,
        interceptors: {
          request: { use: vi.fn() },
          response: { use: vi.fn() },
        },
      })),
    },
    _mockGet: get,
    _mockPost: post,
  }
})

// Import after mock setup
import {
  INDUSTRY_LABELS,
  ENTERPRISE_SCALE_LABELS,
  CUSTOMER_TIER_LABELS,
  RISK_LEVEL_LABELS,
  JOURNEY_PHASE_LABELS,
  SIGNAL_TYPE_LABELS,
  SIGNAL_STATUS_LABELS,
  REPORT_TYPE_LABELS,
  INTERACTION_TYPE_LABELS,
  OUTCOME_LABELS,
  fetchCustomers,
  fetchCustomer,
  fetchCustomerContext,
  fetchCustomerJourneys,
  fetchJourney,
  fetchJourneyInteractions,
  fetchJourneyClaims,
  fetchJourneySignals,
  fetchReport,
  fetchOperatingCases,
  executePrevisit,
  executePostvisit,
  generateOutreachScript,
  generateMeetingScript,
  fetchKycGapProfile,
  fetchOpportunitySignals,
  fetchTransactions,
} from '../engagement'

// Get references to the mock functions from the mocked module
const mockGet = (vi.mocked(await import('axios')) as any)._mockGet
const mockPost = (vi.mocked(await import('axios')) as any)._mockPost

describe('engagement API - Enum mappings', () => {
  it('INDUSTRY_LABELS covers all Industry values', () => {
    const industries: Industry[] = [
      'MANUFACTURING', 'FINANCE', 'TECHNOLOGY', 'REAL_ESTATE',
      'ENERGY', 'HEALTHCARE', 'AGRICULTURE', 'LOGISTICS', 'RETAIL', 'OTHER',
    ]
    for (const ind of industries) {
      expect(INDUSTRY_LABELS[ind]).toBeDefined()
      expect(typeof INDUSTRY_LABELS[ind]).toBe('string')
    }
  })

  it('RISK_LEVEL_LABELS covers all RiskLevel values', () => {
    const levels: RiskLevel[] = ['HIGH', 'MEDIUM', 'LOW']
    for (const level of levels) {
      expect(RISK_LEVEL_LABELS[level]).toBeDefined()
    }
    expect(RISK_LEVEL_LABELS.HIGH).toBe('高风险')
    expect(RISK_LEVEL_LABELS.MEDIUM).toBe('中风险')
    expect(RISK_LEVEL_LABELS.LOW).toBe('低风险')
  })

  it('ENTERPRISE_SCALE_LABELS covers all EnterpriseScale values', () => {
    const scales: EnterpriseScale[] = ['LARGE', 'MEDIUM', 'SMALL', 'MICRO']
    for (const scale of scales) {
      expect(ENTERPRISE_SCALE_LABELS[scale]).toBeDefined()
    }
  })

  it('CUSTOMER_TIER_LABELS covers all CustomerTier values', () => {
    const tiers: CustomerTier[] = ['STRATEGIC', 'KEY', 'GROWTH', 'GENERAL']
    for (const tier of tiers) {
      expect(CUSTOMER_TIER_LABELS[tier]).toBeDefined()
    }
  })

  it('JOURNEY_PHASE_LABELS covers all JourneyPhase values', () => {
    const phases: JourneyPhase[] = [
      'KYC_COLLECT', 'INSIGHT_ANALYSIS', 'PRODUCT_MATCHING',
      'PREVISIT_PREP', 'POSTVISIT_REVIEW', 'COMPLETED',
    ]
    for (const phase of phases) {
      expect(JOURNEY_PHASE_LABELS[phase]).toBeDefined()
    }
  })

  it('SIGNAL_TYPE_LABELS covers all SignalType values', () => {
    const types: SignalType[] = ['FINANCING_NEED', 'PRODUCT_OPPORTUNITY', 'RELATIONSHIP_CHANGE']
    for (const type of types) {
      expect(SIGNAL_TYPE_LABELS[type]).toBeDefined()
    }
  })

  it('SIGNAL_STATUS_LABELS covers all SignalStatus values', () => {
    const statuses: SignalStatus[] = ['DETECTED', 'CONFIRMED', 'DISMISSED', 'CONVERTED']
    for (const status of statuses) {
      expect(SIGNAL_STATUS_LABELS[status]).toBeDefined()
    }
  })

  it('REPORT_TYPE_LABELS covers all ReportType values', () => {
    const types: ReportType[] = [
      'INTERNAL_RELATIONSHIP', 'CRM_CALL', 'UPDATED_RELATIONSHIP', 'NEXT_PREVISIT',
    ]
    for (const type of types) {
      expect(REPORT_TYPE_LABELS[type]).toBeDefined()
    }
  })

  it('INTERACTION_TYPE_LABELS covers all InteractionType values', () => {
    const types: InteractionType[] = [
      'SIGNAL_TRIGGER', 'AI_INSIGHT_PUSH', 'PHONE_CALL', 'FACE_TO_FACE_VISIT',
      'VIDEO_CONFERENCE', 'INSTANT_MESSAGE', 'EMAIL', 'PRODUCT_PRESENTATION',
      'CUSTOMER_COMPLAINT', 'FOLLOW_UP',
    ]
    for (const type of types) {
      expect(INTERACTION_TYPE_LABELS[type]).toBeDefined()
    }
  })

  it('OUTCOME_LABELS covers all InteractionOutcome values', () => {
    const outcomes: InteractionOutcome[] = [
      'COMPLETED', 'CUSTOMER_AGREED', 'CUSTOMER_DECLINED', 'CUSTOMER_DEFERRED',
      'FOLLOW_UP_REQUIRED', 'INTERRUPTED', 'INFORMATION_GATHERED',
    ]
    for (const outcome of outcomes) {
      expect(OUTCOME_LABELS[outcome]).toBeDefined()
    }
  })
})

describe('engagement API - TypeScript interface definitions', () => {
  it('Customer interface is usable', () => {
    const customer: Customer = {
      customerId: 'c1',
      customerName: 'Test Corp',
    }
    expect(customer.customerId).toBe('c1')
  })

  it('CustomerJourney interface is usable', () => {
    const journey: CustomerJourney = {
      journeyId: 'j1',
      operatingCaseId: 'oc1',
      customerId: 'c1',
      customerName: 'Test',
      phase: 'KYC_COLLECT',
      startedAt: '2025-01-01',
    }
    expect(journey.phase).toBe('KYC_COLLECT')
  })

  it('OpportunitySignal interface is usable', () => {
    const signal: OpportunitySignal = {
      signalId: 's1',
      signalType: 'FINANCING_NEED',
      content: 'test',
      sourceType: 'INTERACTION',
      status: 'DETECTED',
      detectedAt: '2025-01-01',
    }
    expect(signal.signalType).toBe('FINANCING_NEED')
  })

  it('Interaction interface is usable', () => {
    const interaction: Interaction = {
      interactionId: 'i1',
      caseId: 'oc1',
      type: 'PHONE_CALL',
      direction: 'OUTBOUND',
      channel: 'phone',
      initiator: { participantId: 'p1', role: 'RM', displayName: 'Zhang' },
      outcome: 'COMPLETED',
      occurredAt: '2025-01-01',
    }
    expect(interaction.type).toBe('PHONE_CALL')
  })

  it('Claim interface is usable', () => {
    const claim: Claim = {
      claimId: 'cl1',
      claimType: 'FINANCIAL',
      content: 'test claim',
      status: 'ACTIVE',
    }
    expect(claim.claimType).toBe('FINANCIAL')
  })

  it('TransactionRecord interface is usable', () => {
    const tx: TransactionRecord = {
      transactionId: 't1',
      customerId: 'c1',
      amount: 10000,
      currency: 'CNY',
      transactionType: 'DEPOSIT',
      occurredAt: '2025-01-01',
    }
    expect(tx.amount).toBe(10000)
  })
})

describe('engagement API - API function signatures', () => {
  beforeEach(() => {
    mockGet.mockClear()
    mockPost.mockClear()
  })

  it('fetchCustomers calls GET /customers', async () => {
    mockGet.mockResolvedValue({ data: [{ customerId: 'c1' }] })
    const result = await fetchCustomers()
    expect(mockGet).toHaveBeenCalledWith('/customers')
    expect(result).toEqual([{ customerId: 'c1' }])
  })

  it('fetchCustomer calls GET /customers/:id', async () => {
    mockGet.mockResolvedValue({ data: { customerId: 'c1' } })
    const result = await fetchCustomer('c1')
    expect(mockGet).toHaveBeenCalledWith('/customers/c1')
    expect(result.customerId).toBe('c1')
  })

  it('fetchCustomerContext calls GET /customers/:id/context', async () => {
    mockGet.mockResolvedValue({ data: { customer: {}, opportunitySignals: [] } })
    await fetchCustomerContext('c1')
    expect(mockGet).toHaveBeenCalledWith('/customers/c1/context')
  })

  it('fetchCustomerJourneys calls GET /customers/:id/journeys', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await fetchCustomerJourneys('c1')
    expect(mockGet).toHaveBeenCalledWith('/customers/c1/journeys')
  })

  it('fetchJourney calls GET /journeys/:id', async () => {
    mockGet.mockResolvedValue({ data: { journeyId: 'j1' } })
    await fetchJourney('j1')
    expect(mockGet).toHaveBeenCalledWith('/journeys/j1')
  })

  it('fetchJourneyInteractions calls GET /journeys/:id/interactions', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await fetchJourneyInteractions('j1')
    expect(mockGet).toHaveBeenCalledWith('/journeys/j1/interactions')
  })

  it('fetchJourneyClaims calls GET /journeys/:id/claims', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await fetchJourneyClaims('j1')
    expect(mockGet).toHaveBeenCalledWith('/journeys/j1/claims')
  })

  it('fetchJourneySignals calls GET /journeys/:id/signals', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await fetchJourneySignals('j1')
    expect(mockGet).toHaveBeenCalledWith('/journeys/j1/signals')
  })

  it('fetchReport calls GET /reports/:id', async () => {
    mockGet.mockResolvedValue({ data: { reportId: 'r1' } })
    await fetchReport('r1')
    expect(mockGet).toHaveBeenCalledWith('/reports/r1')
  })

  it('fetchOperatingCases calls GET /customers/:id/cases', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await fetchOperatingCases('c1')
    expect(mockGet).toHaveBeenCalledWith('/customers/c1/cases')
  })

  it('executePrevisit calls POST /cases/:id/previsit', async () => {
    mockPost.mockResolvedValue({ data: { reportId: 'r1' } })
    await executePrevisit('oc1')
    expect(mockPost).toHaveBeenCalledWith('/cases/oc1/previsit')
  })

  it('executePostvisit calls POST /cases/:id/postvisit with interactionId', async () => {
    mockPost.mockResolvedValue({ data: { keyFindings: [] } })
    await executePostvisit('oc1', 'i1')
    expect(mockPost).toHaveBeenCalledWith('/cases/oc1/postvisit', { interactionId: 'i1' })
  })

  it('generateOutreachScript calls POST /customers/:id/scripts/outreach', async () => {
    mockPost.mockResolvedValue({ data: { scriptId: 's1', content: 'test' } })
    await generateOutreachScript('c1')
    expect(mockPost).toHaveBeenCalledWith('/customers/c1/scripts/outreach')
  })

  it('generateMeetingScript calls POST /customers/:id/scripts/meeting', async () => {
    mockPost.mockResolvedValue({ data: { scriptId: 's1', content: 'test' } })
    await generateMeetingScript('c1')
    expect(mockPost).toHaveBeenCalledWith('/customers/c1/scripts/meeting')
  })

  it('fetchKycGapProfile calls GET /customers/:id/kyc-gap', async () => {
    mockGet.mockResolvedValue({ data: { profileId: 'p1' } })
    await fetchKycGapProfile('c1')
    expect(mockGet).toHaveBeenCalledWith('/customers/c1/kyc-gap')
  })

  it('fetchOpportunitySignals calls GET /customers/:id/signals', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await fetchOpportunitySignals('c1')
    expect(mockGet).toHaveBeenCalledWith('/customers/c1/signals')
  })

  it('fetchTransactions calls GET /customers/:id/transactions', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await fetchTransactions('c1')
    expect(mockGet).toHaveBeenCalledWith('/customers/c1/transactions')
  })
})
