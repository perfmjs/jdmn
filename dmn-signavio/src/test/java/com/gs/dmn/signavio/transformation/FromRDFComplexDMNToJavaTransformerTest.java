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

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FromRDFComplexDMNToJavaTransformerTest extends AbstractSignavioDMNToJavaTest {
    @Override
    protected String getInputPath() {
        return "rdf2java/dmn/complex";
    }

    @Override
    protected String getExpectedPath() {
        return "rdf2java/java/complex/dmn";
    }

    @Test
    public void testNPEValidation2() throws Exception {
        doTest("NPEValidation2");
    }
}
