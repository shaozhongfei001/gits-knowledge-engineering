package com.gien.gits.adapter.dmn;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.ontology.port.ClaimReconciliationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 轻量级DMN决策表执行器 — 直接解析DMN XML的decision table规则
 * 不依赖KIE运行时，避免与Spring Boot 3.x的jakarta兼容性问题
 * 从classpath加载claim-reconciliation.dmn，解析规则并执行FIRST命中策略
 */
public class DmnClaimReconciliationAdapter implements ClaimReconciliationPort {

    private static final Logger log = LoggerFactory.getLogger(DmnClaimReconciliationAdapter.class);

    private final List<DmnRule> rules;
    private final FallbackClaimReconciliationAdapter fallback;
    private final BusinessMetrics businessMetrics;

    public DmnClaimReconciliationAdapter(BusinessMetrics businessMetrics) {
        this.fallback = new FallbackClaimReconciliationAdapter(businessMetrics);
        this.businessMetrics = businessMetrics;
        this.rules = loadRulesFromDmn();
        if (rules.isEmpty()) {
            log.warn("DMN规则加载失败，将使用fallback逻辑");
        } else {
            log.info("DMN决策表加载成功，共{}条规则", rules.size());
        }
    }

    @Override
    public ReconciliationResult reconcile(boolean conflictDetected, boolean authoritativeMatch, boolean evidenceComplete) {
        if (rules.isEmpty()) {
            return fallback.reconcile(conflictDetected, authoritativeMatch, evidenceComplete);
        }

        // FIRST命中策略：按规则顺序匹配，返回第一个命中的结果
        for (DmnRule rule : rules) {
            if (rule.matches(conflictDetected, authoritativeMatch, evidenceComplete)) {
                ReconciliationResult result = new ReconciliationResult(
                    ReconciliationStatus.valueOf(rule.outputStatus),
                    "DMN Rule-" + rule.ruleId + ": " + rule.outputStatus);
                businessMetrics.recordDmnDecision(result.status().toString());
                return result;
            }
        }

        // 无规则命中，fallback
        return fallback.reconcile(conflictDetected, authoritativeMatch, evidenceComplete);
    }

    private List<DmnRule> loadRulesFromDmn() {
        List<DmnRule> loaded = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("claim-reconciliation.dmn")) {
            if (is == null) {
                log.warn("classpath下未找到claim-reconciliation.dmn");
                return loaded;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList ruleNodes = doc.getElementsByTagName("rule");
            for (int i = 0; i < ruleNodes.getLength(); i++) {
                Element ruleEl = (Element) ruleNodes.item(i);
                String ruleId = ruleEl.getAttribute("id");

                NodeList inputEntries = ruleEl.getElementsByTagName("inputEntry");
                String conflictVal = getEntryText(inputEntries, 0);
                String matchVal = getEntryText(inputEntries, 1);
                String evidenceVal = getEntryText(inputEntries, 2);

                NodeList outputEntries = ruleEl.getElementsByTagName("outputEntry");
                String outputVal = stripQuotes(getEntryText(outputEntries, 0));

                loaded.add(new DmnRule(ruleId, conflictVal, matchVal, evidenceVal, outputVal));
            }
        } catch (Exception e) {
            log.error("解析DMN文件失败", e);
        }
        return loaded;
    }

    private String getEntryText(NodeList entries, int index) {
        if (index >= entries.getLength()) return "-";
        Element entry = (Element) entries.item(index);
        NodeList texts = entry.getElementsByTagName("text");
        if (texts.getLength() == 0) return "-";
        return texts.item(0).getTextContent().trim();
    }

    private String stripQuotes(String val) {
        if (val != null && val.startsWith("\"") && val.endsWith("\"")) {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }

    /**
     * DMN规则表示 — 每条规则包含3个输入条件(支持"-"通配)和1个输出
     */
    static class DmnRule {
        final String ruleId;
        final TriVal conflict;
        final TriVal match;
        final TriVal evidence;
        final String outputStatus;

        DmnRule(String ruleId, String conflictVal, String matchVal, String evidenceVal, String outputStatus) {
            this.ruleId = ruleId;
            this.conflict = parseTriVal(conflictVal);
            this.match = parseTriVal(matchVal);
            this.evidence = parseTriVal(evidenceVal);
            this.outputStatus = outputStatus;
        }

        boolean matches(boolean conflictDetected, boolean authoritativeMatch, boolean evidenceComplete) {
            return conflict.matches(conflictDetected)
                && match.matches(authoritativeMatch)
                && evidence.matches(evidenceComplete);
        }

        private static TriVal parseTriVal(String val) {
            if (val == null || val.isBlank() || "-".equals(val)) return TriVal.ANY;
            return "true".equalsIgnoreCase(val) ? TriVal.TRUE : TriVal.FALSE;
        }
    }

    enum TriVal {
        TRUE, FALSE, ANY;
        boolean matches(boolean actual) {
            return this == ANY || (this == TRUE && actual) || (this == FALSE && !actual);
        }
    }
}
