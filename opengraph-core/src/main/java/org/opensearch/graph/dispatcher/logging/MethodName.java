package org.opensearch.graph.dispatcher.logging;

/*-
 * #%L
 * opengraph-core
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */







import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MethodName{
    //region Static
    public static MDCWriter of(String methodName) {
        return new MDCWriter(methodName);
    }
    //endregion

    public static class MDCWriter extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public MDCWriter(String methodName) {
            super(Converter.key, methodName);
        }
        //endregion

        //region Properties
        public String getMethodName() {
            return this.value;
        }
        //endregion

        //region Override Methods
        @Override
        public String toString() {
            return this.value;
        }
        //endregion
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "methodName";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().get(key);
        }
        //endregion
    }
}
