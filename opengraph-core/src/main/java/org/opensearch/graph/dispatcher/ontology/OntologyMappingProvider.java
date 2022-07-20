package org.opensearch.graph.dispatcher.ontology;

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


import org.opensearch.graph.model.ontology.mapping.MappingOntologies;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * mapping ontologies provider
 */
public interface OntologyMappingProvider {
    Optional<MappingOntologies> get(String id);

    Collection<MappingOntologies> getAll();

    MappingOntologies add(MappingOntologies ontology);

    class VoidOntologyMappingProvider implements OntologyMappingProvider {

        @Override
        public Optional<MappingOntologies> get(String id) {
            return Optional.empty();
        }

        @Override
        public Collection<MappingOntologies> getAll() {
            return Collections.emptyList();
        }

        @Override
        public MappingOntologies add(MappingOntologies ontology) {
            return ontology;
        }
    }
}