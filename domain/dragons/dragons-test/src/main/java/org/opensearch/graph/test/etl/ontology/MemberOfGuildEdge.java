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
public interface MemberOfGuildEdge {


    static void main(String args[]) throws IOException {

        // FREEZE
        // Add sideB type
        // redundant field
        // dup + add direction
        Map<String, String> outConstFields=  new HashMap<>();
        outConstFields.put(ETLUtils.ENTITY_A_TYPE, ETLUtils.PERSON);
        outConstFields.put(ETLUtils.ENTITY_B_TYPE, ETLUtils.GUILD);
        AddConstantFieldsTransformer outFieldsTransformer = new AddConstantFieldsTransformer(outConstFields, out);
        Map<String, String> inConstFields=  new HashMap<>();
        inConstFields.put(ETLUtils.ENTITY_A_TYPE, ETLUtils.GUILD);
        inConstFields.put(ETLUtils.ENTITY_B_TYPE, ETLUtils.PERSON);
        AddConstantFieldsTransformer inFieldsTransformer = new AddConstantFieldsTransformer(inConstFields, Direction.in);

        RedundantFieldTransformer redundantOutTransformer = new RedundantFieldTransformer(ETLUtils.getClient(),
                ETLUtils.redundant(ETLUtils.MEMBER_OF_GUILD, out, "A"),
                ETLUtils.ENTITY_A_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.PERSON,
                ETLUtils.redundant(ETLUtils.MEMBER_OF_GUILD, out,"B"),
                ETLUtils.ENTITY_B_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.GUILD).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.GUILD,
                out.name());
        RedundantFieldTransformer redundantInTransformer = new RedundantFieldTransformer(ETLUtils.getClient(),
                ETLUtils.redundant(ETLUtils.MEMBER_OF_GUILD,  Direction.in, "A"),
                ETLUtils.ENTITY_A_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.GUILD).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.GUILD,
                ETLUtils.redundant(ETLUtils.MEMBER_OF_GUILD, Direction.in,"B"),
                ETLUtils.ENTITY_B_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.PERSON, Direction.in.name());
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ETLUtils.ENTITY_A_ID, ETLUtils.ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(ETLUtils.START_DATE, ETLUtils.END_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ETLUtils.ID, ETLUtils.DIRECTION_FIELD, ETLUtils.MEMBER_OF_GUILD);
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                duplicateEdgeTransformer,
                outFieldsTransformer,
                inFieldsTransformer,
                redundantOutTransformer,
                redundantInTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\guildsRelations_MEMBER_OF_GUILD.csv",
                "C:\\demo_data_6June2017\\guildsRelations_MEMBER_OF_GUILD-out.csv",
                chainedTransformer,
                Arrays.asList(ETLUtils.ID, ETLUtils.ENTITY_B_ID, ETLUtils.ENTITY_A_ID,  ETLUtils.START_DATE, ETLUtils.END_DATE),
                5000);
        transformer.transform();

    }

}
