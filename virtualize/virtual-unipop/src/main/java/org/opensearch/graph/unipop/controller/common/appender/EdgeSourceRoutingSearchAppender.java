package org.opensearch.graph.unipop.controller.common.appender;

/*-
 * #%L
 * virtual-unipop
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





import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schema.providers.GraphEdgeSchema;
import org.opensearch.graph.unipop.schema.providers.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schema.providers.GraphRedundantPropertySchema;
import org.opensearch.graph.unipop.schema.providers.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

public class EdgeSourceRoutingSearchAppender  implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Set<String> labels = context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        String edgeLabel = Stream.ofAll(labels).get(0);

        //currently assuming a single vertex label in bulk
        String contextVertexLabel = getLabel(context,"?");


        Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(contextVertexLabel, context.getDirection(), edgeLabel);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        GraphEdgeSchema.End otherEndSchema = edgeSchema.getEndB().get();

        Iterable<GraphVertexSchema> otherVertexSchemas = context.getSchemaProvider().getVertexSchemas(otherEndSchema.getLabel().get());

        if (!Stream.ofAll(otherVertexSchemas).isEmpty()) {
            return false;
        }

        GraphVertexSchema otherVertexSchema = Stream.ofAll(otherVertexSchemas).get(0);
        if (!otherVertexSchema.getRouting().isPresent()) {
            return false;
        }


        GraphElementPropertySchema routingProperty = otherVertexSchema.getRouting().get().getRoutingProperty();
        Optional<GraphRedundantPropertySchema> redundnatRoutingProperty = otherEndSchema.getRedundantProperty(routingProperty);
        if (!redundnatRoutingProperty.isPresent()) {
            return false;
        }

        searchBuilder.getIncludeSourceFields().add(redundnatRoutingProperty.get().getPropertyRedundantName());
        return true;
    }
    //endregion
}
