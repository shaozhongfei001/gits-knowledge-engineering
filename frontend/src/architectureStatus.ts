export interface ArchitectureStatus {
  packageId: 'HZB-KNO-DEV-PACKAGE-V0.1'
  state: 'DEV_PACKAGE_CANDIDATE'
  productionReady: false
  frozen: false
}

export type LoadState =
  | { kind: 'loading' }
  | { kind: 'ready'; value: ArchitectureStatus }
  | { kind: 'empty' }
  | { kind: 'error'; message: string }
  | { kind: 'timeout'; message: string }

export async function loadArchitectureStatus(timeoutMs = 5000): Promise<LoadState> {
  const controller = new AbortController()
  const timer = window.setTimeout(() => controller.abort(), timeoutMs)
  try {
    const response = await fetch('/api/v1/architecture/status', { signal: controller.signal })
    if (!response.ok) return { kind: 'error', message: `服务返回 ${response.status}` }
    const value = (await response.json()) as ArchitectureStatus | null
    return value ? { kind: 'ready', value } : { kind: 'empty' }
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      return { kind: 'timeout', message: '架构状态请求超时' }
    }
    return { kind: 'error', message: '暂时无法连接工程服务' }
  } finally {
    window.clearTimeout(timer)
  }
}

export function isSafeCandidate(value: ArchitectureStatus): boolean {
  return value.state === 'DEV_PACKAGE_CANDIDATE' && !value.productionReady && !value.frozen
}
