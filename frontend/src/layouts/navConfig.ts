export type ShellNavItem = {
  key: string
  label: string
  to?: string
  routeName?: string
  disabled?: boolean
  disableReason?: string
  unlockPath?: string
}

export type ShellNavGroup = {
  key: string
  label: string
  children: ShellNavItem[]
}

/** V3.2 左侧一级域。P04 经客户列表进入；P05–P07 从客户对象头页签进入。 */
export const SHELL_NAV_GROUPS: ShellNavGroup[] = [
  {
    key: 'daily',
    label: '日常作业',
    children: [
      { key: 'Workbench', label: '客户经营作战台', to: '/workbench', routeName: 'Workbench' },
      { key: 'CommitmentDashboard', label: '任务与承诺', to: '/commitments', routeName: 'CommitmentDashboard' },
    ],
  },
  {
    key: 'accounts',
    label: '客户经营',
    children: [
      { key: 'AccountsHome', label: '客户组合', to: '/accounts', routeName: 'AccountsHome' },
      { key: 'PortfolioBoard', label: '客户分层看板', to: '/accounts/portfolio', routeName: 'PortfolioBoard' },
      { key: 'SignalsHome', label: '经营信号', to: '/signals', routeName: 'SignalsHome' },
      { key: 'EngagementsHome', label: '互动对象', to: '/engagements', routeName: 'EngagementsHome' },
      { key: 'EngagementWorkspace', label: '访前路径', to: '/engagement', routeName: 'EngagementWorkspace' },
      { key: 'PrevisitGaps', label: '访前目标与缺口', to: '/engagement/previsit/gaps', routeName: 'PrevisitGaps' },
      { key: 'PrevisitEvidence', label: '访前证据装配', to: '/engagement/previsit/evidence', routeName: 'PrevisitEvidence' },
      { key: 'PrevisitPack', label: '访前包预览', to: '/engagement/previsit/pack', routeName: 'PrevisitPack' },
      { key: 'PostvisitReconcile', label: '访后事实对账', to: '/engagement/postvisit', routeName: 'PostvisitReconcile' },
      { key: 'CrmWriteback', label: 'CRM 受控回写', to: '/engagement/crm-writeback', routeName: 'CrmWriteback' },
      { key: 'ExternalEventMonitor', label: '外部事件监控', to: '/external-events', routeName: 'ExternalEventMonitor' },
    ],
  },
  {
    key: 'delivery',
    label: '方案与交付',
    children: [
      { key: 'InMeetingAssistant', label: '会中助手', to: '/in-meeting', routeName: 'InMeetingAssistant' },
      { key: 'NeedsHome', label: '需求/机会（降级）', to: '/needs', routeName: 'NeedsHome' },
      { key: 'ProposalsHome', label: '服务建议书', to: '/proposals', routeName: 'ProposalsHome' },
      { key: 'ApprovalsHome', label: '审批工作中心', to: '/approvals', routeName: 'ApprovalsHome' },
      { key: 'CollabHome', label: '专家协同', to: '/collab', routeName: 'CollabHome' },
      { key: 'DeliveryHome', label: '对客交付中心', to: '/delivery', routeName: 'DeliveryHome' },
    ],
  },
  {
    key: 'governance',
    label: '知识与治理',
    children: [
      { key: 'KnowledgeMapView', label: '知识卡与产品适用边界', to: '/knowledge-map', routeName: 'KnowledgeMapView' },
      { key: 'AuditTrace', label: '审计与权限', to: '/audit-trace', routeName: 'AuditTrace' },
      { key: 'ClaimsHome', label: 'Claim / Evidence 中心', to: '/claims', routeName: 'ClaimsHome' },
      { key: 'AccountPlansHome', label: '30/90/180 账户计划', to: '/account-plans', routeName: 'AccountPlansHome' },
      { key: 'ValueHome', label: '客户价值实现', to: '/value', routeName: 'ValueHome' },
      { key: 'DegradeHome', label: '服务降级与异常恢复', to: '/degrade', routeName: 'DegradeHome' },
    ],
  },
  {
    key: 'mobile',
    label: '移动端（降级）',
    children: [
      { key: 'MobileToday', label: '今日客户行动', to: '/m/today', routeName: 'MobileToday' },
      { key: 'MobilePrevisit', label: '访前包', to: '/m/previsit', routeName: 'MobilePrevisit' },
      { key: 'MobileNotes', label: '会中速记', to: '/m/notes', routeName: 'MobileNotes' },
      { key: 'MobileCheckout', label: '离场确认', to: '/m/checkout', routeName: 'MobileCheckout' },
    ],
  },
]
