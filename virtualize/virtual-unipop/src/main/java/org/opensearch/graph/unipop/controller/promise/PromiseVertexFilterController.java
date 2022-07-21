package org.opensearch.graph.unipop.controller.promise;


import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.dispatcher.provision.ScrollProvisioning;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.VertexControllerBase;
import org.opensearch.graph.unipop.controller.common.appender.CompositeSearchAppender;
import org.opensearch.graph.unipop.controller.common.appender.FilterSourceSearchAppender;
import org.opensearch.graph.unipop.controller.common.appender.MustFetchSourceSearchAppender;
import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;
import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.controller.promise.appender.FilterIndexSearchAppender;
import org.opensearch.graph.unipop.controller.promise.appender.FilterVerticesSearchAppender;
import org.opensearch.graph.unipop.controller.promise.appender.PromiseConstraintSearchAppender;
import org.opensearch.graph.unipop.controller.promise.appender.SizeSearchAppender;
import org.opensearch.graph.unipop.controller.promise.context.PromiseVertexFilterControllerContext;
import org.opensearch.graph.unipop.controller.promise.converter.SearchHitPromiseFilterEdgeConverter;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.search.SearchOrderProviderFactory;
import org.opensearch.graph.unipop.converter.SearchHitScrollIterable;
import org.opensearch.graph.unipop.predicates.SelectP;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.search.SearchHit;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.opensearch.graph.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by Elad on 4/27/2017.
 * This controller handles constraints on the endB vertices of promise edges.
 * These constraints are modeled as constraints on special virtual 'promise-filter' edges.
 * The controller starts with promise-vertices, filter these vertices
 * and build promise-edges containing the result vertices as end vertices.
 */
public class PromiseVertexFilterController extends VertexControllerBase {

    //region Constructors
    public PromiseVertexFilterController(Client client, OpensearchGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, SearchOrderProviderFactory orderProviderFactory, MetricRegistry metricRegistry) {
        super(labels -> Stream.ofAll(labels).size() == 1 &&
                Stream.ofAll(labels).get(0).equals(GlobalConstants.Labels.PROMISE_FILTER));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.orderProviderFactory = orderProviderFactory;
        this.metricRegistry = metricRegistry;
    }

    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if(constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        return filterPromiseVertices(searchVertexQuery, constraint, selectPHasContainers);
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> filterPromiseVertices(
            SearchVertexQuery searchVertexQuery,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers) {
        SearchBuilder searchBuilder = new SearchBuilder();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new PromiseVertexFilterControllerContext(
                        this.graph,
                        searchVertexQuery.getStepDescriptor(),
                        searchVertexQuery.getVertices(),
                        constraint,
                        selectPHasContainers,
                        schemaProvider,
                        searchVertexQuery.getLimit()));

        CompositeSearchAppender<CompositeControllerContext> appender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                    wrap(new FilterVerticesSearchAppender()),
                    wrap(new SizeSearchAppender(configuration)),
                    wrap(new PromiseConstraintSearchAppender()),
                    wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                    wrap(new FilterSourceSearchAppender()),
                    wrap(new FilterIndexSearchAppender()));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, GlobalConstants.INCLUDE_AGGREGATION).setSize(0);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                new ScrollProvisioning.MetricRegistryScrollProvisioning(metricRegistry,searchVertexQuery.getContext()),
                searchRequest,
                orderProviderFactory.build(context),
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime()
        );

        ElementConverter<SearchHit, Edge> converter = new SearchHitPromiseFilterEdgeConverter(graph);
        javaslang.collection.Iterator<Edge> results = Stream.ofAll(searchHits)
                .flatMap(converter::convert)
                .filter(Objects::nonNull).iterator();
        return results;
    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private SearchOrderProviderFactory orderProviderFactory;
    private MetricRegistry metricRegistry;
    private Client client;
    private OpensearchGraphConfiguration configuration;
    //endregion
}
