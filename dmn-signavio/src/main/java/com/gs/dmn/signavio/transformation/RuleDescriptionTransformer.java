/*
 * Copyright 2016 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.gs.dmn.signavio.transformation;

import com.gs.dmn.DMNModelRepository;
import com.gs.dmn.log.BuildLogger;
import com.gs.dmn.log.Slf4jBuildLogger;
import com.gs.dmn.runtime.Pair;
import com.gs.dmn.signavio.testlab.TestLab;
import com.gs.dmn.transformation.SimpleDMNTransformer;
import org.apache.commons.lang3.StringUtils;
import org.omg.spec.dmn._20180521.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuleDescriptionTransformer extends SimpleDMNTransformer<TestLab> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleDescriptionTransformer.class);
    private static final Map<String, String> PATTERNS = new LinkedHashMap<>();
    static {
        PATTERNS.put("[ ,", "[");
        PATTERNS.put(",  ,", ",");
        PATTERNS.put(", ]", "]");

        PATTERNS.put("string(-)", "\"\"");

        PATTERNS.put("\u00A0", " ");
    }

    private final BuildLogger logger;
    private Map<String, Pair<TInputData, List<TInputData>>> inputDataClasses;

    public RuleDescriptionTransformer() {
        this(new Slf4jBuildLogger(LOGGER));
    }

    public RuleDescriptionTransformer(BuildLogger logger) {
        this.logger = logger;
    }

    @Override
    public DMNModelRepository transform(DMNModelRepository repository) {
        this.inputDataClasses = new LinkedHashMap<>();

        return cleanRuleDescription(repository, logger);
    }

    @Override
    public Pair<DMNModelRepository, TestLab> transform(DMNModelRepository repository, TestLab testLab) {
        if (inputDataClasses == null) {
            transform(repository);
        }

        return new Pair<>(repository, testLab);
    }

    private DMNModelRepository cleanRuleDescription(DMNModelRepository repository, BuildLogger logger) {
        for(TDecision decision: repository.decisions()) {
            TExpression expression = repository.expression(decision);
            if (expression instanceof TDecisionTable) {
                LOGGER.debug("Cleaning descriptions for '{}'", decision.getName());
                List<TDecisionRule> rules = ((TDecisionTable) expression).getRule();
                for (TDecisionRule rule: rules) {
                    cleanRuleDescription(rule);
                }
            }
        }

        return repository;
    }

    void cleanRuleDescription(TDecisionRule rule) {
        String description = rule.getDescription();
        if (StringUtils.isNotBlank(description)) {
            for (Map.Entry<String, String> entry : PATTERNS.entrySet()) {
                description = description.replace(entry.getKey(), entry.getValue());
            }
            rule.setDescription(description);
        }
    }
}
