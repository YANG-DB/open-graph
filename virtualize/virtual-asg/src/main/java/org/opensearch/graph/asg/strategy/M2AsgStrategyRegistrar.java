package org.opensearch.graph.asg.strategy;

/*-
 * #%L
 * virtual-asg
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

import com.google.inject.Inject;
import org.opensearch.graph.asg.strategy.schema.ExactConstraintTransformationAsgStrategy;
import org.opensearch.graph.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import org.opensearch.graph.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.openserach.graph.asg.strategy.AsgNamedParametersStrategy;
import org.openserach.graph.asg.strategy.AsgStrategy;
import org.openserach.graph.asg.strategy.AsgStrategyRegistrar;
import org.openserach.graph.asg.strategy.constraint.*;
import org.openserach.graph.asg.strategy.propertyGrouping.EPropGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.propertyGrouping.HQuantPropertiesGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.propertyGrouping.Quant1PropertiesGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.selection.DefaultETagAsgStrategy;
import org.openserach.graph.asg.strategy.selection.DefaultSelectionAsgStrategy;
import org.openserach.graph.asg.strategy.type.RelationPatternRangeAsgStrategy;
import org.openserach.graph.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
import org.openserach.graph.asg.strategy.type.UntypedRelationInferTypeAsgStrategy;

import java.util.Arrays;

/**
 * Created by roman.margolis on 07/02/2018.
 */
public class M2AsgStrategyRegistrar  implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public M2AsgStrategyRegistrar(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new DefaultETagAsgStrategy(this.ontologyProvider),
                new AsgNamedParametersStrategy(),
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new RelationPatternRangeAsgStrategy(),
                new UntypedRelationInferTypeAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new HQuantPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new AggFilterTransformationAsgStrategy(),
                new LikeToEqTransformationAsgStrategy(),
                new ExactConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeAnyConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantInRangeConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
