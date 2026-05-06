package com.aidotnet.erp.common.pdm;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PdmExtensionService {

    private static final Logger log = LoggerFactory.getLogger(PdmExtensionService.class);
    private final Map<String, SelectionProposal> selectionProposals = new ConcurrentHashMap<>();
    private final Map<String, DevelopmentProcess> developmentProcesses = new ConcurrentHashMap<>();
    private final Map<String, IntellectualProperty> intellectualProperties = new ConcurrentHashMap<>();
    private final Map<String, QualityStandard> qualityStandards = new ConcurrentHashMap<>();

    public SelectionProposal createSelectionProposal(String tenantId, String productName, String category,
                                                      String targetMarket, String sourceType, String sourceLink,
                                                      String submitter, String analysisData) {
        String proposalId = "SEL-" + System.currentTimeMillis();
        SelectionProposal proposal = new SelectionProposal(proposalId, tenantId, productName, category,
                targetMarket, sourceType, sourceLink, submitter, analysisData,
                "DRAFT", Instant.now(), Instant.now());
        selectionProposals.put(proposalId, proposal);
        log.info("Created selection proposal: id={}, product={}, category={}", proposalId, productName, category);
        return proposal;
    }

    public SelectionProposal submitProposal(String proposalId) {
        SelectionProposal existing = selectionProposals.get(proposalId);
        if (existing == null) throw new IllegalArgumentException("Selection proposal not found: " + proposalId);
        SelectionProposal submitted = new SelectionProposal(proposalId, existing.tenantId(),
                existing.productName(), existing.category(), existing.targetMarket(),
                existing.sourceType(), existing.sourceLink(), existing.submitter(),
                existing.analysisData(), "PENDING_REVIEW", existing.createdAt(), Instant.now());
        selectionProposals.put(proposalId, submitted);
        log.info("Submitted selection proposal: id={}", proposalId);
        return submitted;
    }

    public SelectionProposal reviewProposal(String proposalId, String reviewer, String decision, String comments) {
        SelectionProposal existing = selectionProposals.get(proposalId);
        if (existing == null) throw new IllegalArgumentException("Selection proposal not found: " + proposalId);
        String status = "APPROVED".equals(decision) ? "APPROVED" : "REJECTED";
        SelectionProposal reviewed = new SelectionProposal(proposalId, existing.tenantId(),
                existing.productName(), existing.category(), existing.targetMarket(),
                existing.sourceType(), existing.sourceLink(), existing.submitter(),
                existing.analysisData(), status, existing.createdAt(), Instant.now());
        selectionProposals.put(proposalId, reviewed);
        log.info("Reviewed selection proposal: id={}, decision={}", proposalId, decision);
        return reviewed;
    }

    public List<SelectionProposal> listSelectionProposals(String tenantId, String status) {
        return selectionProposals.values().stream()
                .filter(p -> tenantId == null || tenantId.equals(p.tenantId()))
                .filter(p -> status == null || status.equals(p.status()))
                .toList();
    }

    public DevelopmentProcess createDevelopmentProcess(String tenantId, String productName, String proposalId,
                                                        String developer, String milestones) {
        String processId = "DEV-" + System.currentTimeMillis();
        DevelopmentProcess process = new DevelopmentProcess(processId, tenantId, productName,
                proposalId, developer, milestones, "INITIATED", Instant.now(), Instant.now());
        developmentProcesses.put(processId, process);
        log.info("Created development process: id={}, product={}", processId, productName);
        return process;
    }

    public DevelopmentProcess updateProcessStatus(String processId, String status) {
        DevelopmentProcess existing = developmentProcesses.get(processId);
        if (existing == null) throw new IllegalArgumentException("Development process not found: " + processId);
        DevelopmentProcess updated = new DevelopmentProcess(processId, existing.tenantId(),
                existing.productName(), existing.proposalId(), existing.developer(),
                existing.milestones(), status, existing.createdAt(), Instant.now());
        developmentProcesses.put(processId, updated);
        log.info("Updated development process status: id={}, status={}", processId, status);
        return updated;
    }

    public List<DevelopmentProcess> listDevelopmentProcesses(String tenantId, String status) {
        return developmentProcesses.values().stream()
                .filter(p -> tenantId == null || tenantId.equals(p.tenantId()))
                .filter(p -> status == null || status.equals(p.status()))
                .toList();
    }

    public IntellectualProperty createIntellectualProperty(String tenantId, String productName, String ipType,
                                                            String ipNumber, String status, String owner,
                                                            Instant filingDate, Instant expiryDate, String description) {
        String ipId = "IP-" + System.currentTimeMillis();
        IntellectualProperty ip = new IntellectualProperty(ipId, tenantId, productName, ipType,
                ipNumber, status, owner, filingDate, expiryDate, description, Instant.now());
        intellectualProperties.put(ipId, ip);
        log.info("Created intellectual property: id={}, type={}, product={}", ipId, ipType, productName);
        return ip;
    }

    public IntellectualProperty updateIpStatus(String ipId, String status) {
        IntellectualProperty existing = intellectualProperties.get(ipId);
        if (existing == null) throw new IllegalArgumentException("IP not found: " + ipId);
        IntellectualProperty updated = new IntellectualProperty(ipId, existing.tenantId(),
                existing.productName(), existing.ipType(), existing.ipNumber(),
                status, existing.owner(), existing.filingDate(), existing.expiryDate(),
                existing.description(), existing.createdAt());
        intellectualProperties.put(ipId, updated);
        log.info("Updated IP status: id={}, status={}", ipId, status);
        return updated;
    }

    public List<IntellectualProperty> listIntellectualProperties(String tenantId, String ipType) {
        return intellectualProperties.values().stream()
                .filter(ip -> tenantId == null || tenantId.equals(ip.tenantId()))
                .filter(ip -> ipType == null || ipType.equals(ip.ipType()))
                .toList();
    }

    public QualityStandard createQualityStandard(String tenantId, String standardName, String category,
                                                  String applicableProducts, String inspectionItems,
                                                  String acceptanceCriteria, String version) {
        String standardId = "QS-" + System.currentTimeMillis();
        QualityStandard standard = new QualityStandard(standardId, tenantId, standardName, category,
                applicableProducts, inspectionItems, acceptanceCriteria, version, true, Instant.now());
        qualityStandards.put(standardId, standard);
        log.info("Created quality standard: id={}, name={}, category={}", standardId, standardName, category);
        return standard;
    }

    public QualityStandard updateQualityStandard(String standardId, boolean active) {
        QualityStandard existing = qualityStandards.get(standardId);
        if (existing == null) throw new IllegalArgumentException("Quality standard not found: " + standardId);
        QualityStandard updated = new QualityStandard(standardId, existing.tenantId(),
                existing.standardName(), existing.category(), existing.applicableProducts(),
                existing.inspectionItems(), existing.acceptanceCriteria(),
                existing.version(), active, existing.createdAt());
        qualityStandards.put(standardId, updated);
        log.info("Updated quality standard: id={}, active={}", standardId, active);
        return updated;
    }

    public List<QualityStandard> listQualityStandards(String tenantId, String category) {
        return qualityStandards.values().stream()
                .filter(qs -> tenantId == null || tenantId.equals(qs.tenantId()))
                .filter(qs -> category == null || category.equals(qs.category()))
                .toList();
    }

    public record SelectionProposal(String proposalId, String tenantId, String productName, String category,
                                     String targetMarket, String sourceType, String sourceLink,
                                     String submitter, String analysisData, String status,
                                     Instant createdAt, Instant updatedAt) {}
    public record DevelopmentProcess(String processId, String tenantId, String productName, String proposalId,
                                      String developer, String milestones, String status,
                                      Instant createdAt, Instant updatedAt) {}
    public record IntellectualProperty(String ipId, String tenantId, String productName, String ipType,
                                        String ipNumber, String status, String owner,
                                        Instant filingDate, Instant expiryDate, String description,
                                        Instant createdAt) {}
    public record QualityStandard(String standardId, String tenantId, String standardName, String category,
                                   String applicableProducts, String inspectionItems,
                                   String acceptanceCriteria, String version, boolean active,
                                   Instant createdAt) {}
}
