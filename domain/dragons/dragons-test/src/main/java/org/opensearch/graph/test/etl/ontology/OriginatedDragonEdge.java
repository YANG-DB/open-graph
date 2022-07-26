package org.opensearch.graph.test.etl.ontology;



import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.graph.test.etl.*;
import org.opensearch.graph.test.scenario.ETLUtils;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.opensearch.graph.model.execution.plan.Direction.out;

/**
 * Created by moti on 6/7/2017.
 */
public interface OriginatedDragonEdge {
    static void main(String args[]) throws IOException {
        Map<String, String> outConstFields=  new HashMap<>();
        outConstFields.put(ETLUtils.ENTITY_A_TYPE, ETLUtils.DRAGON);
        outConstFields.put(ETLUtils.ENTITY_B_TYPE, ETLUtils.KINGDOM);
        AddConstantFieldsTransformer outFieldsTransformer = new AddConstantFieldsTransformer(outConstFields, out);
        Map<String, String> inConstFields=  new HashMap<>();
        inConstFields.put(ETLUtils.ENTITY_A_TYPE, ETLUtils.KINGDOM);
        inConstFields.put(ETLUtils.ENTITY_B_TYPE, ETLUtils.DRAGON);
        AddConstantFieldsTransformer inFieldsTransformer = new AddConstantFieldsTransformer(inConstFields, Direction.in);

        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(ETLUtils.getClient(),
                ETLUtils.redundant(ETLUtils.ORIGINATED, out, "A"),
                ETLUtils.ENTITY_A_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.DRAGON,
                ETLUtils.redundant(ETLUtils.ORIGINATED, out,"B"),
                ETLUtils.ENTITY_B_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.KINGDOM,
                out.name());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(ETLUtils.getClient(),
                ETLUtils.redundant(ETLUtils.ORIGINATED,  Direction.in, "A"),
                ETLUtils.ENTITY_A_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.KINGDOM).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.KINGDOM,
                ETLUtils.redundant(ETLUtils.ORIGINATED, Direction.in,"B"),
                ETLUtils.ENTITY_B_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.DRAGON, Direction.in.name());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ETLUtils.ENTITY_A_ID, ETLUtils.ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(ETLUtils.SINCE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ETLUtils.ID, ETLUtils.DIRECTION_FIELD, ETLUtils.ORIGINATED + "D");
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                duplicateEdgeTransformer,
                outFieldsTransformer,
                inFieldsTransformer,
                redundantInTransformer,
                redundantOutTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_DRAGON.csv",
                "C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_DRAGON-out.csv",
                chainedTransformer,
                Arrays.asList(ETLUtils.ID, ETLUtils.ENTITY_A_ID, ETLUtils.ENTITY_B_ID,  ETLUtils.SINCE),
                5000);
        transformer.transform();
    }

}
