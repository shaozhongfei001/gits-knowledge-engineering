package com.gien.gits.ontology;

/**
 * 交互渠道枚举。
 * 包含交互发生的渠道类型和系统来源标识。
 */
public enum Channel {
    // 人工交互渠道
    /** 电话沟通 */
    PHONE,
    /** 面谈 */
    IN_PERSON,
    /** 邮件 */
    EMAIL,
    /** 即时消息 */
    INSTANT_MESSAGE,
    /** 视频会议 */
    VIDEO_CONFERENCE,

    // 系统推送渠道
    /** 系统推送 */
    SYSTEM_PUSH,
    /** CRM推送 */
    CRM_PUSH,

    // 系统来源标识
    /** 风险信号引擎 */
    RISK_SIGNAL_ENGINE,
    /** AI洞察引擎 */
    AI_INSIGHT_ENGINE,
    /** 产品匹配引擎 */
    PRODUCT_MATCH_ENGINE,

    // 特殊渠道
    /** 面对面拜访 */
    FACE_TO_FACE,
    /** 电话回访 */
    PHONE_CALL
}
