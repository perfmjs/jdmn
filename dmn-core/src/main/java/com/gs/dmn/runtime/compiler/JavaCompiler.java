/**
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
package com.gs.dmn.runtime.compiler;

import com.gs.dmn.feel.analysis.syntax.ast.FEELContext;
import com.gs.dmn.feel.analysis.syntax.ast.expression.function.FunctionDefinition;
import com.gs.dmn.feel.synthesis.FEELTranslator;
import com.gs.dmn.transformation.basic.BasicDMN2JavaTransformer;

/**
 * Created by Octavian Patrascoiu on 29/06/2017.
 */
public interface JavaCompiler {
    ClassData makeClassData(FunctionDefinition element, FEELContext context, BasicDMN2JavaTransformer dmnTransformer, FEELTranslator feelTranslator, String libClassName);

    Class<?> compile(ClassData args) throws Exception;
}