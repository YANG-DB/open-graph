package org.opensearch.graph.unipop.controller.common.appender;


import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;
import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalHasStepFinder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalQueryTranslator;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphElementConstraint;
import org.opensearch.graph.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.AndStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

/**
 * Created by Elad on 4/26/2017.
 */
public class ConstraintSearchAppender implements SearchAppender<CompositeControllerContext> {
    //region ElementControllerContext Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, CompositeControllerContext context) {
        Set<String> labels = getContextRelevantLabels(context);

        Traversal newConstraint = context.getConstraint().isPresent() ?
                context.getConstraint().get().getTraversal().asAdmin().clone() :
                __.start().has(T.label, P.within(labels));

        List<GraphElementConstraint> elementConstraints =
                    context.getElementType().equals(ElementType.vertex) ?
                            Stream.ofAll(labels)
                                    .flatMap(label -> context.getSchemaProvider().getVertexSchemas(label))
                                    .map(p->p.getConstraint())
                                    .toJavaList() :
                            Stream.ofAll(context.getSchemaProvider().getEdgeSchemas(
                                    getLabel(context,"?"),
                                    context.getDirection(),
                                    Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor())).get(0)))
                                    .map(p->p.getConstraint())
                                    .toJavaList();

        if (!elementConstraints.isEmpty()) {
            List<HasStep> labelHasSteps = Stream.ofAll(new TraversalHasStepFinder(step -> step.getHasContainers().get(0).getKey().equals(T.label.getAccessor()))
                    .getValue(newConstraint)).toJavaList();

            if (!labelHasSteps.isEmpty()) {
                labelHasSteps.get(0).getTraversal().removeStep(labelHasSteps.get(0));
            }

            Traversal elementConstraintsTraversal = elementConstraints.size() > 1 ?
                    __.start().or(Stream.ofAll(elementConstraints).map(GraphElementConstraint::getTraversalConstraint).toJavaArray(Traversal.class)) :
                    elementConstraints.get(0).getTraversalConstraint();

            newConstraint = Stream.ofAll(new TraversalHasStepFinder(step -> true).getValue(newConstraint)).isEmpty() ?
                    elementConstraintsTraversal :
                    __.start().and(elementConstraintsTraversal, newConstraint);

        }

        if (!(newConstraint.asAdmin().getSteps().get(0) instanceof AndStep)) {
            newConstraint = __.start().and(newConstraint);
        }

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query();//.filtered().filter();
        AggregationBuilder aggregationBuilder = searchBuilder.getAggregationBuilder().seekRoot();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, aggregationBuilder, false);
        traversalQueryTranslator.visit(newConstraint);
        return true;
    }

    //endregion

    //region Private Methods
    private Set<String> getContextRelevantLabels(CompositeControllerContext context) {
        if (context.getVertexControllerContext().isPresent()) {
            return getVertexContextRelevantLabels(context);
        }

        return getElementContextRelevantLabels(context);
    }

    private Set<String> getElementContextRelevantLabels(ElementControllerContext context) {
        Set<String> labels = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (labels.isEmpty()) {
            labels = Stream.ofAll(context.getElementType().equals(ElementType.vertex) ?
                    context.getSchemaProvider().getVertexLabels() :
                    context.getSchemaProvider().getEdgeLabels()).toJavaSet();
        }

        return labels;
    }

    private Set<String> getVertexContextRelevantLabels(VertexControllerContext context) {
        // currently assuming homogeneous bulk
        return Stream.ofAll(context.getBulkVertices())
                .take(1)
                .map(Element::label)
                .toJavaSet();
    }
    //endregion
}
