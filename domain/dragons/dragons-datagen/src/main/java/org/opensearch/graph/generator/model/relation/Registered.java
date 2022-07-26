package org.opensearch.graph.generator.model.relation;





import org.opensearch.graph.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 05-Jun-17.
 */
public class Registered extends RelationBase {

    //region Ctrs
    public Registered(String id, String source, String target, Date since) {
        super(id, source, target, RelationType.REGISTERED);
        this.since = since;
    }
    //endregion

    //region Getters & Setters
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }
    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{
                this.getId(),
                this.getSource(),
                "Guild",// source entity type
                this.getTarget(),
                "Kingdom",// target entity type
                Long.toString(this.getSince().getTime())
        };
    }
    //endregion

    //region Fields
    private Date since;
    //endregion
}

