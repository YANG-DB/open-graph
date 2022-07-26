package org.opensearch.graph.executor.cursor.promise;




import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.executor.cursor.BaseCursor;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.*;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.results.*;
import org.opensearch.graph.model.results.Property;
import org.opensearch.graph.unipop.promise.IdPromise;
import org.opensearch.graph.unipop.structure.promise.PromiseEdge;
import org.opensearch.graph.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;

public class TraversalCursor extends BaseCursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new TraversalCursor((TraversalCursorContext)context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public TraversalCursor(TraversalCursorContext context) {
        super(context);
        this.ont = new Ontology.Accessor(context.getOntology());
        this.flatPlan = PlanUtil.flat(context.getQueryResource().getExecutionPlan().getPlan());
        this.typeProperty = ont.property$("type");
    }
    //endregion

    //region Cursor Implementation
    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        return toQuery(numResults);
    }
    //endregion

    //region Private Methods
    private AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        builder.withPattern(context.getQueryResource().getQuery());
        //build assignments
        context.next(numResults)
                .forEach(path -> builder.withAssignment(toAssignment(path)));
        return builder
                .withQueryId(context.getQueryResource().getQueryMetadata().getId())
                .withCursorId(context.getQueryResource().getCurrentCursorId())
                .withTimestamp(context.getQueryResource().getQueryMetadata().getCreationTime())
                .build();
    }

    private Assignment toAssignment(Path path) {
        Assignment.Builder builder = Assignment.Builder.instance();
        this.flatPlan.getOps().forEach(planOp -> {
            if (planOp instanceof EntityOp) {
                EEntityBase entity = ((EntityOp)planOp).getAsgEbase().geteBase();

                if(entity instanceof EConcrete) {
                    builder.withEntity(toEntity(path, (EConcrete) entity));
                } else if(entity instanceof ETyped) {
                    builder.withEntity(toEntity(path, (ETyped) entity));
                } else if(entity instanceof EUntyped) {
                    builder.withEntity(toEntity(path, (EUntyped) entity));
                }
            } else if (planOp instanceof RelationOp) {
                RelationOp relationOp = (RelationOp)planOp;
                Optional<EntityOp> prevEntityOp =
                        PlanUtil.prev(this.flatPlan, planOp, EntityOp.class);
                Optional<EntityOp> nextEntityOp =
                        PlanUtil.next(this.flatPlan, planOp, EntityOp.class);

                builder.withRelationship(toRelationship(path,
                        prevEntityOp.get().getAsgEbase().geteBase(),
                        relationOp.getAsgEbase().geteBase(),
                        nextEntityOp.get().getAsgEbase().geteBase()));
            }
        });

        return builder.build();
    }

    private Entity toEntity(Path path, EUntyped element) {
        PromiseVertex vertex = getPromiseVertex(path, element);
        IdPromise idPromise = (IdPromise)vertex.getPromise();

        String eType = idPromise.getLabel().isPresent() ? ont.eType$(idPromise.getLabel().get()) : "";

        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(property -> !property.getpType().equals(this.typeProperty.getpType()))
                .toJavaList();

        return toEntity(vertex.id().toString(),eType,element.geteTag(), properties);
    }

    private Entity toEntity(Path path, EConcrete element) {
        PromiseVertex vertex = getPromiseVertex(path, element);
        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(property -> !property.getpType().equals(this.typeProperty.getpType()))
                .toJavaList();

        return toEntity(vertex.id().toString(),element.geteType(),element.geteTag(), properties);
    }

    private Entity toEntity(Path path, ETyped element) {
        PromiseVertex vertex = getPromiseVertex(path, element);
        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(property -> !property.getpType().equals(this.typeProperty.getpType()))
                .toJavaList();

        return toEntity(vertex.id().toString(),element.geteType(),element.geteTag(), properties);
    }

    private PromiseVertex getPromiseVertex(Path path, EEntityBase element) {
        return path.get(Pop.last, element.geteTag());
    }

    private Entity toEntity(String eId, String eType, String eTag, List<Property> properties) {
        Entity.Builder builder = Entity.Builder.instance();
        builder.withEID(eId);
        builder.withEType(eType);
        builder.withETag(new HashSet<>(Collections.singletonList(eTag)));
        builder.withProperties(properties);
        return builder.build();
    }

    private Relationship toRelationship(Path path, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
        Relationship.Builder builder = Relationship.Builder.instance();
        PromiseEdge edge = path.get(Pop.last, prevEntity.geteTag() + "-->" + nextEntity.geteTag());
        builder.withRID(edge.id().toString());
        builder.withRType(rel.getrType());
        builder.withRTag(rel.geteTag());

        switch (rel.getDir()) {
            case R:
                builder.withEID1(edge.outVertex().id().toString());
                builder.withEID2(edge.inVertex().id().toString());
                builder.withETag1(prevEntity.geteTag());
                builder.withETag2(nextEntity.geteTag());
                break;

            case L:
                builder.withEID1(edge.inVertex().id().toString());
                builder.withEID2(edge.outVertex().id().toString());
                builder.withETag1(nextEntity.geteTag());
                builder.withETag2(prevEntity.geteTag());
        }

        return builder.build();
    }

    private Property toProperty(VertexProperty vertexProperty) {
        return new Property(ont.property$(vertexProperty.key()).getpType(), "raw", vertexProperty.value());
    }
    //endregion

    //region Fields
    private Ontology.Accessor ont;
    private final CompositePlanOp flatPlan;

    private org.opensearch.graph.model.ontology.Property typeProperty;
    //endregion
}
