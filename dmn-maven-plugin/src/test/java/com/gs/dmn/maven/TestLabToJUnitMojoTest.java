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
package com.gs.dmn.maven;

import com.gs.dmn.maven.configuration.components.DMNTransformerComponent;
import com.gs.dmn.signavio.dialect.SignavioDMNDialectDefinition;
import com.gs.dmn.signavio.transformation.template.SignavioTreeTemplateProvider;
import com.gs.dmn.transformation.NopDMNTransformer;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class TestLabToJUnitMojoTest extends AbstractMojoTest {
    private final TestLabToJUnitMojo mojo = new TestLabToJUnitMojo();
    private final MavenProject project = new MavenProject();

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteWhenMissingInput() throws Exception {
        mojo.inputParameters = makeParams();
        mojo.execute();
        assertTrue(true);
    }

    @Test
    public void testExecute() throws Exception {
        String inputModel = this.getClass().getClassLoader().getResource("input/NPEValidation2.dmn").getFile();
        String inputTest = this.getClass().getClassLoader().getResource("input/NPEValidation2.json").getFile();
        mojo.project = project;
        mojo.dmnDialect = SignavioDMNDialectDefinition.class.getName();
        mojo.dmnTransformers = new DMNTransformerComponent[] { new DMNTransformerComponent(NopDMNTransformer.class.getName()) };
        mojo.templateProvider = SignavioTreeTemplateProvider.class.getName();
        mojo.inputModelFileDirectory = new File(inputModel);
        mojo.inputTestFileDirectory = new File(inputTest);
        mojo.outputFileDirectory = new File("target/output");
        mojo.inputParameters = makeParams();
        mojo.execute();
        assertTrue(true);
    }
}