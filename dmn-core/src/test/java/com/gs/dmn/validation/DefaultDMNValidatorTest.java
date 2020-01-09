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
package com.gs.dmn.validation;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DefaultDMNValidatorTest extends AbstractValidatorTest {
    private final DMNValidator validator = new DefaultDMNValidator();

    @Test
    public void testValidateWhenCorrect() {
        List<String> expectedErrors = Arrays.asList();
        validate(validator, "tck/cl3/input/0020-vacation-days.dmn", expectedErrors);
    }

    @Test
    public void testValidateDefinitionsWhenNotUniqueNames() {
        List<String> expectedErrors = Arrays.asList(
                "The 'name' of a 'DRGElement' must be unique. Found duplicates for 'CIP Assessments, Input'.",
                "The 'name' of a 'ItemDefinition' must be unique. Found duplicates for 'itemDefinition'.",
                "Missing variable for 'CIP Assessments'",
                "Missing variable for 'CIP Assessments'"
        );
        validate(validator, "dmn/input/test-dmn-with-duplicates.dmn", expectedErrors);
    }

    @Test
    public void testValidateDefinitionsWithError() {
        List<String> expectedErrors = Arrays.asList(
                "Missing variable for 'CIP Assessments'"
        );
        validate(validator, "dmn/input/test-dmn.dmn", expectedErrors);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDefinitionsWhenNull() {
        validator.validate(null);
    }
}