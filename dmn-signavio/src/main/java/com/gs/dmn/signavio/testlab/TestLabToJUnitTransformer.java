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
package com.gs.dmn.signavio.testlab;

import com.gs.dmn.DMNModelRepository;
import com.gs.dmn.dialect.DMNDialectDefinition;
import com.gs.dmn.log.BuildLogger;
import com.gs.dmn.runtime.DMNRuntimeException;
import com.gs.dmn.runtime.Pair;
import com.gs.dmn.serialization.PrefixNamespaceMappings;
import com.gs.dmn.serialization.TypeDeserializationConfigurer;
import com.gs.dmn.signavio.SignavioDMNModelRepository;
import com.gs.dmn.transformation.AbstractDMNTransformer;
import com.gs.dmn.transformation.DMNTransformer;
import com.gs.dmn.transformation.InputParamUtil;
import com.gs.dmn.transformation.basic.BasicDMN2JavaTransformer;
import com.gs.dmn.transformation.lazy.LazyEvaluationDetector;
import com.gs.dmn.transformation.template.TemplateProvider;
import com.gs.dmn.validation.DMNValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.omg.spec.dmn._20180521.model.TDRGElement;
import org.omg.spec.dmn._20180521.model.TDecision;
import org.omg.spec.dmn._20180521.model.TDefinitions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestLabToJUnitTransformer extends AbstractDMNTransformer {
    private final TestLabReader testLabReader = new TestLabReader();
    private final TestLabValidator testLabValidator = new TestLabValidator();

    private String schemaNamespace;
    private final BasicDMN2JavaTransformer basicTransformer;
    private final TestLabUtil testLabUtil;
    private final TestLabEnhancer testLabEnhancer;

    public TestLabToJUnitTransformer(DMNDialectDefinition dialectDefinition, DMNValidator dmnValidator, DMNTransformer dmnTransformer, TemplateProvider templateProvider, LazyEvaluationDetector lazyEvaluationDetector, TypeDeserializationConfigurer typeDeserializationConfigurer, Path inputModelPath, Map<String, String> inputParameters, BuildLogger logger) {
        super(dialectDefinition, dmnValidator, dmnTransformer, templateProvider, lazyEvaluationDetector, typeDeserializationConfigurer, inputParameters, logger);
        this.schemaNamespace = InputParamUtil.getOptionalParam(inputParameters, "signavioSchemaNamespace");
        if (StringUtils.isEmpty(this.schemaNamespace)) {
            this.schemaNamespace = "http://www.signavio.com/schema/dmn/1.1/";
        }
        DMNModelRepository repository = readDMN(inputModelPath.toFile());
        this.basicTransformer = this.dialectDefinition.createBasicTransformer(repository, lazyEvaluationDetector, inputParameters);
        DMNModelRepository dmnModelRepository = this.basicTransformer.getDMNModelRepository();
        this.dmnValidator.validate(dmnModelRepository);

        this.testLabUtil = new TestLabUtil(basicTransformer);
        this.testLabEnhancer = new TestLabEnhancer(testLabUtil);
    }

    @Override
    protected boolean shouldTransform(File inputFile) {
        String name = inputFile.getName();
        return name.endsWith(TestLabReader.TEST_LAB_FILE_EXTENSION) && !name.endsWith(".svn");
    }

    @Override
    protected void transformFile(File child, File root, Path outputPath) {
        try {
            logger.info(String.format("Processing TestLab file '%s'", child.getPath()));
            StopWatch watch = new StopWatch();
            watch.start();

            TestLab testLab = testLabReader.read(child);
            testLabValidator.validate(testLab);
            testLabEnhancer.enhance(testLab);

            testLab = (TestLab) this.dmnTransformer.transform(basicTransformer.getDMNModelRepository(), testLab).getRight();

            String javaClassName = testClassName(testLab, basicTransformer);
            processTemplate(testLab, templateProvider.testBaseTemplatePath(), templateProvider.testTemplateName(), basicTransformer, outputPath, javaClassName);

            watch.stop();
            logger.info("TestLab processing time: " + watch.toString());
        } catch (IOException e) {
            throw new DMNRuntimeException(String.format("Error during transforming %s.", child.getName()), e);
        }
    }

    @Override
    protected DMNModelRepository readDMN(File file) {
        if (isDMNFile(file)) {
            Pair<TDefinitions, PrefixNamespaceMappings> result = dmnReader.read(file);
            SignavioDMNModelRepository repository = new SignavioDMNModelRepository(result, this.schemaNamespace);
            return repository;
        } else {
            throw new DMNRuntimeException(String.format("Invalid DMN file %s", file.getAbsoluteFile()));
        }
    }

    private void processTemplate(TestLab testLab, String baseTemplatePath, String templateName, BasicDMN2JavaTransformer dmnTransformer, Path outputPath, String testClassName) {
        try {
            // Make parameters
            Map<String, Object> params = makeTemplateParams(testLab, dmnTransformer);
            params.put("packageName", javaRootPackage);
            params.put("testClassName", testClassName);
            params.put("decisionBaseClass", decisionBaseClass);

            // Make output file
            String javaPackageName = dmnTransformer.javaRootPackageName();
            String relativeFilePath = javaPackageName.replace('.', '/');
            String fileExtension = ".java";
            File outputFile = makeOutputFile(outputPath, relativeFilePath, testClassName, fileExtension);

            // Process template
            processTemplate(baseTemplatePath, templateName, params, outputFile, true);
        } catch (Exception e) {
            throw new DMNRuntimeException(String.format("Cannot process TestLab template '%s' for '%s'", templateName, testLab.getRootDecisionId()), e);
        }
    }

    private String testClassName(TestLab testLab, BasicDMN2JavaTransformer dmnTransformer) {
        List<OutputParameterDefinition> outputParameterDefinitions = testLab.getOutputParameterDefinitions();
        OutputParameterDefinition outputParameterDefinition = outputParameterDefinitions.get(0);
        return testClassName(dmnTransformer, outputParameterDefinition);
    }

    private String testClassName(BasicDMN2JavaTransformer dmnTransformer, OutputParameterDefinition outputParameterDefinition) {
        TDRGElement decision = testLabUtil.findDRGElement(outputParameterDefinition);
        if (decision instanceof TDecision) {
            String requirementName = decision.getName();
            return dmnTransformer.upperCaseFirst(requirementName + "Test");
        } else {
            throw new IllegalArgumentException(String.format("The DRGElement '%s' should be a decision, is output of TestLab.", outputParameterDefinition.getId()));
        }
    }

    private Map<String, Object> makeTemplateParams(TestLab testLab, BasicDMN2JavaTransformer dmnTransformer) {
        Map<String, Object> params = new HashMap<>();
        params.put("testLab", testLab);
        params.put("testLabUtil", testLabUtil);
        return params;
    }
}
