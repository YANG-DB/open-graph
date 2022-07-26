package org.openserach.graph.asg.strategy.propertyGrouping;







import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class EPropGroupingAsgStrategy implements AsgStrategy {
    /*
    region AsgStrategy Implementation
    The simple case - no Quantifier involved - e.g., Q142 on V1
    The Entity will have only one EProp child
    */

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, EEntityBase.class))
                .filter(asgEBase -> !AsgQueryUtil.nextAdjacentDescendant(asgEBase, Quant1.class).isPresent())
                .filter(asgEBase -> !AsgQueryUtil.nextAdjacentDescendant(asgEBase, EPropGroup.class).isPresent())
                .forEach(entityBase -> {
                    Optional<AsgEBase<EProp>> asgEProp = AsgQueryUtil.nextAdjacentDescendant(entityBase, EProp.class);
                    if (asgEProp.isPresent()) {
                        EPropGroup ePropGroup = new EPropGroup(Arrays.asList(asgEProp.get().geteBase()));
                        ePropGroup.seteNum(asgEProp.get().geteNum());
                        entityBase.removeNextChild(asgEProp.get());
                        entityBase.addNextChild(new AsgEBase<>(ePropGroup));
                    } else {
                        EPropGroup ePropGroup = new EPropGroup();
                        int maxEnum = Stream.ofAll(AsgQueryUtil.eNums(query)).max().get();

                        if (entityBase.getNext().isEmpty()) {
                            ePropGroup.seteNum(entityBase.geteNum() * 100 + 1);
                            entityBase.addNextChild(new AsgEBase<>(ePropGroup));
                        } else {
                            Quant1 quant1 = new Quant1();
                            quant1.seteNum(maxEnum + 1);
                            quant1.setqType(QuantType.all);
                            AsgEBase<Quant1> asgQuant1 = new AsgEBase<>(quant1);

                            ePropGroup.seteNum(entityBase.geteNum()*100 + 1);

                            asgQuant1.addNextChild(new AsgEBase<>(ePropGroup));
                            new ArrayList<>(entityBase.getNext()).forEach(nextAsgEbase -> {
                                entityBase.removeNextChild(nextAsgEbase);
                                asgQuant1.addNextChild(nextAsgEbase);
                            });
                            entityBase.addNextChild(asgQuant1);
                        }
                    }
                });
    }
    //endregion
}
