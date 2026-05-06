package com.aidotnet.erp.dashboard.domain;

import java.time.Instant;
import java.util.Map;

/**
 * AI洞察卡片领域模型
 * <p>
 * 描述: AI看板核心展示单元，承载AI分析产生的业务洞察、预警和建议。
 *       每张卡片对应一个AI分析结果，支持多种洞察类型和严重等级。
 *       洞察卡片由各业务域事件触发AI分析生成，为用户提供智能决策建议。
 * </p>
 * <p>
 * 业务规则:
 *   1. 洞察类型(insightType): anomaly(异常)/trend(趋势)/opportunity(机会)/risk(风险)
 *   2. 严重等级(severity): critical/high/medium/low，按等级降序展示
 *   3. data存储洞察数据JSONB，如异常指标值、趋势变化率等
 *   4. sourceDomain标识来源域，如: OMS/WMS/ADS/FMS
 *   5. validUntil过期后不再展示，自动清理
 *   6. isDismissed=true表示用户已忽略此洞察
 * </p>
 *
 * @param cardId       卡片唯一标识，UUID格式
 * @param tenantId     租户ID，实现多租户数据隔离
 * @param userId       目标用户ID，支持按用户推送个性化洞察
 * @param title        洞察标题，简洁描述洞察要点
 * @param summary      洞察摘要，简要说明洞察内容
 * @param insightType  洞察类型: anomaly(异常)/trend(趋势)/opportunity(机会)/risk(风险)
 * @param severity     严重等级: critical/high/medium/low
 * @param data         洞察数据，JSON格式存储结构化洞察数据
 * @param sourceDomain 来源业务域: OMS/WMS/ADS/FMS等
 * @param suggestion   AI建议，为用户提供决策参考
 * @param actionUrl    操作跳转链接，引导用户到对应业务页面处理
 * @param isRead       是否已读
 * @param isDismissed  是否已忽略
 * @param validUntil   过期时间，过期后不再展示
 * @param createdAt    创建时间，UTC时区
 * @param updatedAt    更新时间，UTC时区
 * @author ERP系统
 */
public record AIInsightCard(String cardId, String tenantId, String userId, String title, String summary,
                            String insightType, String severity, Map<String, Object> data,
                            String sourceDomain, String suggestion, String actionUrl,
                            boolean isRead, boolean isDismissed, Instant validUntil,
                            Instant createdAt, Instant updatedAt) {}
