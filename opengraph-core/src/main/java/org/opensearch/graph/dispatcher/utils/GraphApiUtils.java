package org.opensearch.graph.dispatcher.utils;


import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.query.*;
import org.opensearch.graph.model.query.entity.*;
import org.opensearch.graph.model.query.optional.OptionalComp;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static org.opensearch.graph.model.GlobalConstants._ALL;
import static org.opensearch.graph.model.query.Rel.Direction.R;


public abstract class GraphApiUtils {
    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param sourceId
     * @param targetId
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPath(String ontology, String sourceEntity, String sourceId, String targetEntity, String targetId, String relationType, int maxHops) {
        if (StringUtils.isEmpty(sourceId) && StringUtils.isEmpty(targetId) && StringUtils.isEmpty(targetEntity)) {
            return findPathQuery(ontology, sourceEntity, relationType, maxHops);
        }

        if (StringUtils.isEmpty(sourceId) && StringUtils.isEmpty(targetId)) {
            return findPathQuery(ontology, sourceEntity, targetEntity, relationType, maxHops);
        }

         if (StringUtils.isEmpty(targetId)) {
            return findPathQuery(ontology, sourceEntity, sourceId, targetEntity, relationType, maxHops);
        }

        return findPathQuery(ontology,sourceEntity,sourceId,targetEntity,targetId,relationType,maxHops);
    }

    /**
     *
     * @param ontology
     * @param sourceEntity
     * @param sourceId
     * @param targetEntity
     * @param targetId
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String sourceId, String targetEntity, String targetId, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, sourceEntity + "_" + sourceId, sourceEntity, sourceId, sourceEntity + "_" + sourceId, 2),
                        new RelPattern(2, relationType, new org.opensearch.graph.model.Range(1, maxHops), R, null, 3, 0),
                        new TypedEndPattern<>(new ETyped(3, Tagged.tagSeq(targetEntity), targetEntity, 4)),
                        new Rel(4, relationType, R, null, 5),
                        new EConcrete(5, targetEntity + "_" + targetId, targetEntity, targetId, targetEntity + "_" + targetId, 0)
                )).build();
/*
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, sourceEntity + "_" + sourceId, sourceEntity, sourceId, sourceEntity + "_" + sourceId, 2),
                        new RelPattern(2, relationType, new org.opensearch.graph.model.Range(1, maxHops), R, null, 3, 0),
                        new TypedEndPattern<>(new EConcrete(3, Tagged.tagSeq(targetEntity), targetEntity, targetId, targetEntity + "_" + targetId, 0))
                )).build();
*/
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param sourceId
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String sourceId, String targetEntity, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, sourceEntity + "_" + sourceId, sourceEntity, sourceId, sourceEntity + "_" + sourceId, 2),
                        new RelPattern(2, relationType, new org.opensearch.graph.model.Range(1, maxHops), R, null, 3, 0),
                        new TypedEndPattern<>(new ETyped(3, Tagged.tagSeq(targetEntity), targetEntity, 0))
                )).build();
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String targetEntity, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, sourceEntity, sourceEntity, 2),
                        new RelPattern(2, relationType, new org.opensearch.graph.model.Range(1, maxHops), R, null, 3, 0),
                        new TypedEndPattern<>(new ETyped(3, Tagged.tagSeq(targetEntity), targetEntity, 0))
                )).build();
    }

    /**
     * expend a concrete vertex in any direction with maxHops
     *
     * @param ontology
     * @param maxHops
     * @return
     */
    public static Query expandVertex(String ontology, String sourceEntity, String sourceId, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, sourceEntity + "_" + sourceId, sourceEntity, sourceId, sourceEntity + "_" + sourceId, 2),
                        new RelUntyped(2, Collections.emptySet(), R,null, 3 ),
                        new UnTypedEndPattern<>(new EUntyped(3, Tagged.tagSeq("AnyOf"), -1, 0))
                )).build();
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, sourceEntity, sourceEntity, 2),
                        new RelPattern(2, relationType, new org.opensearch.graph.model.Range(1, maxHops), R, null, 3, 0),
                        new UnTypedEndPattern<>(new EUntyped(3, Tagged.tagSeq("AnyOf"), -1, 0))
                )).build();
    }

    /**
     * generate findPath graph query between two concrete vertices id's
     *
     * @param ontology
     * @param relationType
     * @param maxHops
     * @return
     */
    public static Query findPathQuery(String ontology, String sourceEntity, String[] targetEntities, String relationType, int maxHops) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, sourceEntity, sourceEntity, 2),
                        new RelPattern(2, relationType, new org.opensearch.graph.model.Range(1, maxHops), R, null, 3, 0),
                        new UnTypedEndPattern<>(new EUntyped(3, Tagged.tagSeq("AnyOf"), new HashSet<>(Arrays.asList(targetEntities)), Collections.emptySet(), -1, 0))
                )).build();
    }

    /**
     * get vertex by id & type
     *
     * @param ontology
     * @param type
     * @param id
     * @return
     */
    public static Query getVertex(String ontology, String type, String id) {
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, id, type, id, id, 0)
                )).build();
    }

    /**
     * get vertex and its neighbors by id and type
     *
     * @param ontology
     * @param type
     * @param id
     * @return
     */
    public static Query getNeighbors(String ontology, String type, String id) {
        Set<String> all = Collections.singleton(_ALL);
        return Query.Builder.instance().withName(UUID.randomUUID().toString())
                .withOnt(ontology)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new EConcrete(1, id, type, id, id, 2),
                        new OptionalComp(2, 3),
                        new RelUntyped(3, all, R, "relation", 4),
                        new EUntyped(4, "neighbor", all, 0, 0)
                )).build();
    }
}
