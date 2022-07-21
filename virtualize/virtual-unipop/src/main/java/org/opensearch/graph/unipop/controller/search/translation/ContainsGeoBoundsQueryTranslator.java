package org.opensearch.graph.unipop.controller.search.translation;


import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.opensearch.common.geo.GeoPoint;

import java.util.Arrays;
import java.util.List;

public class ContainsGeoBoundsQueryTranslator implements PredicateQueryTranslator {
    public static final String GEO_BOUNDS = "geo_bounds";
    private String[] geoFields;
    //region PredicateQueryTranslator Implementation

    public ContainsGeoBoundsQueryTranslator(String... geoFields) {
        this.geoFields = geoFields;
    }

    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        Contains contains = (Contains) predicate.getBiPredicate();
        final List box = (List) predicate.getValue();
        switch (contains) {
            case within:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().bool().mustNot().exists(key).pop();
                } else {
                    queryBuilder.push()
                            //box.get(0) - geo function name (geo_bounds)
                            .geoBoundingBox(GEO_BOUNDS, key, new GeoPoint(box.get(1).toString()), new GeoPoint(box.get(2).toString()))
                            .pop();
                }
                break;
            case without:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().exists(key).pop();
                } else {
                    queryBuilder.push().bool().mustNot()
                            //box.get(0) - geo function name (geo_bounds)
                            .geoBoundingBox(GEO_BOUNDS, key, new GeoPoint(box.get(1).toString()), new GeoPoint(box.get(2).toString()))
                            .pop();
                }
                break;
        }

        return queryBuilder;
    }
    //endregion


    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && ((predicate.getBiPredicate() instanceof Contains) && (predicate.getValue() instanceof List)
                && ((List) predicate.getValue()).get(0).equals(GEO_BOUNDS)
                && Arrays.asList(geoFields).contains(key)

        );
    }
}
