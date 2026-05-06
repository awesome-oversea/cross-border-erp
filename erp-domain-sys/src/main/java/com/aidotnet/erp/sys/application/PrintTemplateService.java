package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.PrintTemplate;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 打印模板管理应用服务
 * <p>
 * 描述: 系统设置域打印模板服务，负责打印模板的创建/更新/删除/查询等
 *       业务逻辑。支持面单/发票/装箱单等多种打印模板类型。
 * </p>
 *
 * @author ERP系统
 */
@Service
public class PrintTemplateService {
    private final SysExtStore extStore;

    public PrintTemplateService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public PrintTemplate createTemplate(String tenantId, CreatePrintTemplateCommand command) {
        extStore.findPrintTemplateByCode(tenantId, command.templateCode())
                .ifPresent(existing -> { throw new BizException("TEMPLATE_CODE_DUPLICATED", "模板编码已存在"); });
        Instant now = Instant.now();
        PrintTemplate template = new PrintTemplate(
                UUID.randomUUID().toString(), tenantId, command.templateCode(), command.templateName(),
                command.templateType(), command.content(), command.paperSize(), command.orientation(),
                command.variables(), true, command.description(), now, now);
        return extStore.savePrintTemplate(template);
    }

    @Transactional
    public PrintTemplate updateTemplate(String tenantId, String templateId, UpdatePrintTemplateCommand command) {
        PrintTemplate existing = getTemplate(tenantId, templateId);
        Instant now = Instant.now();
        PrintTemplate updated = new PrintTemplate(
                existing.templateId(), existing.tenantId(), existing.templateCode(),
                command.templateName() != null ? command.templateName() : existing.templateName(),
                command.templateType() != null ? command.templateType() : existing.templateType(),
                command.content() != null ? command.content() : existing.content(),
                command.paperSize() != null ? command.paperSize() : existing.paperSize(),
                command.orientation() != null ? command.orientation() : existing.orientation(),
                command.variables() != null ? command.variables() : existing.variables(),
                existing.enabled(),
                command.description() != null ? command.description() : existing.description(),
                existing.createdAt(), now);
        return extStore.savePrintTemplate(updated);
    }

    @Transactional
    public PrintTemplate toggleTemplate(String tenantId, String templateId, boolean enabled) {
        PrintTemplate existing = getTemplate(tenantId, templateId);
        Instant now = Instant.now();
        PrintTemplate updated = new PrintTemplate(
                existing.templateId(), existing.tenantId(), existing.templateCode(), existing.templateName(),
                existing.templateType(), existing.content(), existing.paperSize(), existing.orientation(),
                existing.variables(), enabled, existing.description(), existing.createdAt(), now);
        return extStore.savePrintTemplate(updated);
    }

    public PrintTemplate getTemplate(String tenantId, String templateId) {
        return extStore.findPrintTemplate(tenantId, templateId)
                .orElseThrow(() -> new BizException("TEMPLATE_NOT_FOUND", "打印模板不存在"));
    }

    public PrintTemplate getTemplateByCode(String tenantId, String templateCode) {
        return extStore.findPrintTemplateByCode(tenantId, templateCode)
                .orElseThrow(() -> new BizException("TEMPLATE_NOT_FOUND", "打印模板不存在"));
    }

    public List<PrintTemplate> listTemplates(String tenantId, String templateType) {
        return extStore.listPrintTemplates(tenantId, templateType);
    }

    public String renderTemplate(String tenantId, String templateCode, Map<String, String> params) {
        PrintTemplate template = getTemplateByCode(tenantId, templateCode);
        if (!template.enabled()) {
            throw new BizException("TEMPLATE_DISABLED", "模板已停用");
        }
        String rendered = template.content();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return rendered;
    }

    public record CreatePrintTemplateCommand(String templateCode, String templateName, String templateType,
                                             String content, String paperSize, String orientation,
                                             Map<String, Object> variables, String description) {}
    public record UpdatePrintTemplateCommand(String templateName, String templateType, String content,
                                             String paperSize, String orientation,
                                             Map<String, Object> variables, String description) {}
}
