package org.opensearch.graph.executor.ontology.promise;





import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.ElementController;
import org.opensearch.graph.unipop.controller.common.logging.LoggingSearchController;
import org.opensearch.graph.unipop.controller.common.logging.LoggingSearchVertexController;
import org.opensearch.graph.unipop.controller.promise.PromiseElementEdgeController;
import org.opensearch.graph.unipop.controller.promise.PromiseElementVertexController;
import org.opensearch.graph.unipop.controller.promise.PromiseVertexController;
import org.opensearch.graph.unipop.controller.promise.PromiseVertexFilterController;
import org.opensearch.graph.unipop.controller.search.SearchOrderProviderFactory;
import org.opensearch.graph.unipop.process.traversal.strategy.StandardStrategyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.SearchUniGraph;
import org.opensearch.client.Client;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Set;

public class M1ElasticUniGraphProvider implements UniGraphProvider {
    //region Constructors

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public M1ElasticUniGraphProvider(
            Client client,
            OpensearchGraphConfiguration opensearchGraphConfiguration,
            UniGraphConfiguration uniGraphConfiguration,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            SearchOrderProviderFactory orderProviderFactory) {
        this.client = client;
        this.opensearchGraphConfiguration = opensearchGraphConfiguration;
        this.uniGraphConfiguration = uniGraphConfiguration;
        this.schemaProviderFactory = schemaProviderFactory;
        this.orderProviderFactory = orderProviderFactory;
    }
    //endregion

    @Override
    public UniGraph getGraph(Ontology ontology) throws Exception {
        return new SearchUniGraph(
                this.uniGraphConfiguration,
                controllerManagerFactory(schemaProviderFactory.get(ontology)),
                new StandardStrategyProvider());
    }

    //region Private Methods
    /**
     * default controller Manager
     * @return
     */
    private ControllerManagerFactory controllerManagerFactory(GraphElementSchemaProvider schemaProvider) {
        return uniGraph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new ElementController(
                                new LoggingSearchController(
                                        new PromiseElementVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProviderFactory,metricRegistry),
                                        metricRegistry),
                                new LoggingSearchController(
                                        new PromiseElementEdgeController(client, opensearchGraphConfiguration, uniGraph, schemaProvider,metricRegistry),
                                        metricRegistry)),
                        new LoggingSearchVertexController(
                                new PromiseVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider,metricRegistry),
                                metricRegistry),
                        new LoggingSearchVertexController(
                                new PromiseVertexFilterController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProviderFactory,metricRegistry),
                                metricRegistry)
                );
            }

            @Override
            public void close() {
            }
        };
    }
    //endregion

    //region Fields
    private final Client client;
    private final OpensearchGraphConfiguration opensearchGraphConfiguration;
    private final UniGraphConfiguration uniGraphConfiguration;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;
    private final SearchOrderProviderFactory orderProviderFactory;
    //endregion
}
