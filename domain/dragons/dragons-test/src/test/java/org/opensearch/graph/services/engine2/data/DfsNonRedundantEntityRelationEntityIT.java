package org.opensearch.graph.services.engine2.data;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.unipop.promise.Constraint;
import org.opensearch.graph.unipop.promise.TraversalConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman on 22/05/2017.
 */
public class DfsNonRedundantEntityRelationEntityIT extends EntityRelationEntityIT {
    @BeforeClass
    public static void setup() throws Exception {
        setup(SearchEmbeddedNode.getClient());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        cleanup(SearchEmbeddedNode.getClient());
    }

    @Override
    protected TraversalConstraint getExpectedEdgeTraversalConstraint(
            String relationType,
            Direction direction,
            String relProperty,
            P relPropertyPredicate,
            String entityBId,
            Iterable<String> entityBTypes) {

        List<Traversal> traversals = new ArrayList<>();
        traversals.add(__.has(T.label, relationType));
        traversals.add(__.has(GlobalConstants.HasKeys.DIRECTION, direction));

        if (relProperty != null) {
            traversals.add(__.has(relProperty, relPropertyPredicate));
        }

        return Constraint.by(__.and(Stream.ofAll(traversals).toJavaArray(Traversal.class)));
    }
}
