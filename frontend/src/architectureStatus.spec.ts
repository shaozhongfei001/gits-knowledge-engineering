import { describe, expect, it } from 'vitest'
import { isSafeCandidate, type ArchitectureStatus } from './architectureStatus'

describe('architecture status', () => {
  it('does not turn an engineering candidate into a production claim', () => {
    const status: ArchitectureStatus = {
      packageId: 'HZB-KNO-DEV-PACKAGE-V0.1',
      state: 'DEV_PACKAGE_CANDIDATE',
      productionReady: false,
      frozen: false,
    }
    expect(isSafeCandidate(status)).toBe(true)
  })
})
