package com.rgabay.embedded_sdn_sproc.domain;

import org.neo4j.ogm.annotation.Property;

/**
 * @author jarhanco
 */
public class Device extends Entity {

    @Property
    private String model;

    public Device() {
    }

    public String getModel() {

        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
