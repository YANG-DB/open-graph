package org.opensearch.graph.model.results;



import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.descriptors.GraphDescriptor;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AssignmentQueryResultsDescriptor implements Descriptor<AssignmentsQueryResult<Entity,Relationship>>, GraphDescriptor<AssignmentsQueryResult<Entity,Relationship>>
{
    private static AssignmentQueryResultsDescriptor INSTANCE = new AssignmentQueryResultsDescriptor();

    @Override
    public String describe(AssignmentsQueryResult<Entity,Relationship> queryResult) {
        StringJoiner joiner = new StringJoiner("\n", "", "");

        List<StringJoiner> collect = queryResult.getAssignments().stream()
                .map(AssignmentDescriptor::print)
                .map(joiner::add)
                .collect(Collectors.toList());

        return collect.stream().map(StringJoiner::toString).collect(Collectors.joining());
    }

    public static String print(AssignmentsQueryResult<Entity,Relationship> queryResult) {
        return INSTANCE.describe(queryResult);
    }


    @Override
    public String visualize(AssignmentsQueryResult<Entity, Relationship> item) {
        //todo
        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { \n");
        //left to right direction
        sb.append("\t rankdir=LR; \n");
        //general node shape
        sb.append("\t node [shape=Mrecord]; \n");
        //todo - remove once finished
        sb.append("TODO");
        //append start node shape (first node in query elements list)
//        sb.append("\t start [shape=Mdiamond, color=blue, style=\"rounded\"]; \n");

        //print entities
//        entities(sb, item.getEntities());
        //print relations
        // relations(sb, item.getRelationships());
        //iterate over the query
        sb.append("\n\t } \n");
        return sb.toString();
    }
}

