package org.opensearch.graph.asg.strategy.constraint;

/*-
 * #%L
 * opengraph-asg
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







import org.opensearch.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.List;

import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.ignorableConstraints;

/**
 * this strategy Transforms likeAny constraint into like with a list of operands as the expression value
 */
public class RedundantLikeAnyConstraintAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                    .filter(eProp -> eProp.getCon() != null)
                    .filter(prop -> !ignorableConstraints.contains(prop.getCon().getClass()))
                    .filter(eProp -> eProp.getCon().getOp().equals(ConstraintOp.likeAny))
                    .filter(eProp -> ((List) eProp.getCon().getExpr()).size() == 1)
                    .forEach(eProp -> eProp.setCon(Constraint.of(ConstraintOp.like, ((List) eProp.getCon().getExpr()).get(0))));
        });
    }
    //endregion
}
