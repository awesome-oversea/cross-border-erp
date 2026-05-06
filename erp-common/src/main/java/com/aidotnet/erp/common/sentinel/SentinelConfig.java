package com.aidotnet.erp.common.sentinel;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelConfig {

    @Value("${erp.sentinel.flow-rate:100}")
    private int flowRate;

    @Value("${erp.sentinel.degrade-rt:3000}")
    private double degradeRt;

    @Value("${erp.sentinel.degrade-ratio:0.5}")
    private double degradeRatio;

    @Value("${erp.sentinel.degrade-window:10}")
    private int degradeWindow;

    @Value("${erp.sentinel.system-load:-1}")
    private double systemLoad;

    @Value("${erp.sentinel.system-cpu-usage:0.8}")
    private double systemCpuUsage;

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        initSystemRules();
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule apiRule = new FlowRule();
        apiRule.setResource("erp-api");
        apiRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        apiRule.setCount(flowRate);
        apiRule.setLimitApp("default");
        apiRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        apiRule.setWarmUpPeriodSec(10);
        rules.add(apiRule);

        FlowRule outApiRule = new FlowRule();
        outApiRule.setResource("erp-out-api");
        outApiRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        outApiRule.setCount(flowRate / 2);
        outApiRule.setLimitApp("default");
        outApiRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        outApiRule.setMaxQueueingTimeMs(5000);
        rules.add(outApiRule);

        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule rtRule = new DegradeRule("erp-api");
        rtRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rtRule.setCount(degradeRt);
        rtRule.setTimeWindow(degradeWindow);
        rtRule.setMinRequestAmount(5);
        rtRule.setStatIntervalMs(60000);
        rules.add(rtRule);

        DegradeRule ratioRule = new DegradeRule("erp-api");
        ratioRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        ratioRule.setCount(degradeRatio);
        ratioRule.setTimeWindow(degradeWindow);
        ratioRule.setMinRequestAmount(5);
        ratioRule.setStatIntervalMs(60000);
        rules.add(ratioRule);

        DegradeRule outDegradeRule = new DegradeRule("erp-out-api");
        outDegradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        outDegradeRule.setCount(0.3);
        outDegradeRule.setTimeWindow(30);
        outDegradeRule.setMinRequestAmount(3);
        outDegradeRule.setStatIntervalMs(60000);
        rules.add(outDegradeRule);

        DegradeRuleManager.loadRules(rules);
    }

    private void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();

        if (systemLoad > 0) {
            SystemRule loadRule = new SystemRule();
            loadRule.setHighestSystemLoad(systemLoad);
            rules.add(loadRule);
        }

        SystemRule cpuRule = new SystemRule();
        cpuRule.setHighestCpuUsage(systemCpuUsage);
        rules.add(cpuRule);

        SystemRule rtRule = new SystemRule();
        rtRule.setAvgRt(5000);
        rules.add(rtRule);

        SystemRule threadRule = new SystemRule();
        threadRule.setMaxThread(500);
        rules.add(threadRule);

        SystemRuleManager.loadRules(rules);
    }
}
