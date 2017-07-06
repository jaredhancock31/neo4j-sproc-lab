package com.rgabay.embedded_sdn_sproc.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author jarhanco
 */
@NodeEntity
public abstract class Entity {

    @GraphId
    Long id;

    public Long getId() {
        return this.id;
    }
}
