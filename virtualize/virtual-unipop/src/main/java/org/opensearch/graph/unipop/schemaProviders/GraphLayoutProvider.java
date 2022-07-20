package org.opensearch.graph.unipop.schemaProviders;

/*-
 * #%L
 * fuse-dv-unipop
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

import java.util.Optional;

/**
 * Created by moti on 5/17/2017.
 */
public interface GraphLayoutProvider {
    class NoneRedundant implements GraphLayoutProvider {
        public static GraphLayoutProvider getInstance() {
            return instance;
        }
        private static GraphLayoutProvider instance = new NoneRedundant();

        //region GraphLayoutProvider Implementation
        @Override
        public Optional<GraphRedundantPropertySchema> getRedundantProperty(String edgeType, GraphElementPropertySchema property) {
            return Optional.empty();
        }
        //endregion
    }

    Optional<GraphRedundantPropertySchema> getRedundantProperty(String edgeType, GraphElementPropertySchema property);
}