package org.opensearch.graph.executor.ontology;





import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;

public interface GraphElementSchemaProviderFactory {
    GraphElementSchemaProvider get(Ontology ontology);
}
