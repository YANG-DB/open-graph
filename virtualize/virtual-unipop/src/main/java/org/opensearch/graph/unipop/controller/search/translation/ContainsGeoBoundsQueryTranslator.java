package org.opensearch.graph.unipop.controller.search.translation;

/*-
 * #%L
 * virtual-unipop
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





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
        final List box = (List) predicate.getValue();//checked in the PredicateQueryTranslator.test() to verify this is a list
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
