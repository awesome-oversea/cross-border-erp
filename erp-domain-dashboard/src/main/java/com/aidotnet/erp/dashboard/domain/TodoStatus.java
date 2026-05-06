package com.aidotnet.erp.dashboard.domain;

/**
 * 待办事项状态枚举
 * <p>
 * 描述: 定义待办事项的状态流转，控制待办的生命周期
 * </p>
 * <p>
 * 状态流转: PENDING → IN_PROGRESS → COMPLETED
 *                    └→ CANCELLED
 * </p>
 */
public enum TodoStatus {
    /** 待处理 - 新创建的待办，等待负责人处理 */
    PENDING,
    /** 进行中 - 负责人已开始处理 */
    IN_PROGRESS,
    /** 已完成 - 处理完毕 */
    COMPLETED,
    /** 已取消 - 不再需要处理 */
    CANCELLED
}
