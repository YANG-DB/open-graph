package org.unipop.query.mutation;

/*-
 * #%L
 * unipop-core
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







import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.StepDescriptor;
import org.unipop.query.UniQuery;
import org.unipop.query.controller.UniQueryController;

import java.util.Map;

public class AddVertexQuery extends UniQuery{
    private final Map<String, Object> properties;

    public AddVertexQuery(Map<String, Object> properties, StepDescriptor stepDescriptor) {
        super(stepDescriptor);
        this.properties = properties;
    }

    public Map<String, Object>  getProperties() {
        return properties;
    }

    public interface AddVertexController extends UniQueryController {
        Vertex addVertex(AddVertexQuery uniQuery);
    }

    @Override
    public String toString() {
        return "AddVertexQuery{" +
                "properties=" + properties +
                '}';
    }
}
