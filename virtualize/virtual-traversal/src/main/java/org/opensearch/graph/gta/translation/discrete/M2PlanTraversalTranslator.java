package org.opensearch.graph.gta.translation.discrete;



import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.gta.strategy.discrete.M2PlanOpTranslationStrategy;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class M2PlanTraversalTranslator extends ChainedPlanOpTraversalTranslator {
    //region Constructors
    public M2PlanTraversalTranslator() {
        super(new M2PlanOpTranslationStrategy());
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        return super.translate(planWithCost, context);
    }
    //endregion
}
