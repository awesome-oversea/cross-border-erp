package com.aidotnet.erp.crm.infrastructure.mapper;

import com.aidotnet.erp.crm.infrastructure.data.ComplaintDO;
import com.aidotnet.erp.crm.infrastructure.data.CustomerBehaviorDO;
import com.aidotnet.erp.crm.infrastructure.data.CustomerProfileDO;
import com.aidotnet.erp.crm.infrastructure.data.CustomerTagDO;
import com.aidotnet.erp.crm.infrastructure.data.MessageDO;
import com.aidotnet.erp.crm.infrastructure.data.ReplyTemplateDO;
import com.aidotnet.erp.crm.infrastructure.data.ReviewDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * CRM扩展数据Mapper接口
 * <p>
 * 描述: CRM域扩展功能数据访问层，负责回复模板、客诉、客户标签、
 *       评价、消息、客户画像、客户行为等业务对象的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface CrmExtMapper {

    /** 新增回复模板 */
    void insertReplyTemplate(ReplyTemplateDO template);
    /** 更新回复模板 */
    void updateReplyTemplate(ReplyTemplateDO template);
    /** 按租户和模板ID查询回复模板 */
    ReplyTemplateDO selectReplyTemplate(@Param("tenantId") String tenantId, @Param("templateId") String templateId);
    /** 按租户和分类查询回复模板列表 */
    List<ReplyTemplateDO> selectReplyTemplates(@Param("tenantId") String tenantId, @Param("category") String category);

    /** 新增客诉 */
    void insertComplaint(ComplaintDO complaint);
    /** 更新客诉 */
    void updateComplaint(ComplaintDO complaint);
    /** 按租户和客诉ID查询客诉 */
    ComplaintDO selectComplaint(@Param("tenantId") String tenantId, @Param("complaintId") String complaintId);
    /** 按租户和状态查询客诉列表 */
    List<ComplaintDO> selectComplaints(@Param("tenantId") String tenantId, @Param("status") String status);
    /** 按租户和客户ID查询客诉列表 */
    List<ComplaintDO> selectComplaintsByCustomer(@Param("tenantId") String tenantId, @Param("customerId") String customerId);

    /** 新增客户标签 */
    void insertCustomerTag(CustomerTagDO tag);
    /** 删除客户标签 */
    void deleteCustomerTag(@Param("tenantId") String tenantId, @Param("tagId") String tagId);
    /** 按租户和客户ID查询标签列表 */
    List<CustomerTagDO> selectCustomerTags(@Param("tenantId") String tenantId, @Param("customerId") String customerId);

    /** 新增评价 */
    void insertReview(ReviewDO review);
    /** 更新评价 */
    void updateReview(ReviewDO review);
    /** 按租户和评价ID查询评价 */
    ReviewDO selectReview(@Param("tenantId") String tenantId, @Param("reviewId") String reviewId);
    /** 按租户查询评价列表 */
    List<ReviewDO> selectReviews(@Param("tenantId") String tenantId);
    /** 按租户和客户ID查询评价列表 */
    List<ReviewDO> selectReviewsByCustomer(@Param("tenantId") String tenantId, @Param("customerId") String customerId);
    /** 按租户查询差评列表 */
    List<ReviewDO> selectNegativeReviews(@Param("tenantId") String tenantId);

    /** 新增消息 */
    void insertMessage(MessageDO message);
    /** 更新消息 */
    void updateMessage(MessageDO message);
    /** 按租户和消息ID查询消息 */
    MessageDO selectMessage(@Param("tenantId") String tenantId, @Param("messageId") String messageId);
    /** 按租户和客户ID查询消息列表 */
    List<MessageDO> selectMessages(@Param("tenantId") String tenantId, @Param("customerId") String customerId);
    /** 按租户查询未读消息列表 */
    List<MessageDO> selectUnreadMessages(@Param("tenantId") String tenantId);

    /** 新增客户画像 */
    void insertCustomerProfile(CustomerProfileDO profile);
    /** 更新客户画像 */
    void updateCustomerProfile(CustomerProfileDO profile);
    /** 按租户和画像ID查询客户画像 */
    CustomerProfileDO selectCustomerProfile(@Param("tenantId") String tenantId, @Param("profileId") String profileId);
    /** 按租户和客户ID查询客户画像 */
    CustomerProfileDO selectCustomerProfileByCustomerId(@Param("tenantId") String tenantId, @Param("customerId") String customerId);
    /** 按租户和分群查询客户画像列表 */
    List<CustomerProfileDO> selectCustomerProfiles(@Param("tenantId") String tenantId, @Param("segment") String segment);

    /** 新增客户行为 */
    void insertCustomerBehavior(CustomerBehaviorDO behavior);
    /** 按租户、客户ID和行为类型查询客户行为列表 */
    List<CustomerBehaviorDO> selectCustomerBehaviors(@Param("tenantId") String tenantId, @Param("customerId") String customerId, @Param("behaviorType") String behaviorType);
}
