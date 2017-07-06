
package com.rgabay.embedded_sdn_sproc.domain;

import org.neo4j.ogm.annotation.*;

import java.util.*;

@NodeEntity
public class Person extends Entity {

    private Person() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public Person(String name) {
        this.name = name;
    }

    @Property
	private String name;

	@Property
	List<Long> arbitraryLongs;

	Person ancestor;

	@Relationship(type = "SISTER")
	Person sister;

	@Relationship(type = "HAS")
	List<Device> devices;

    public List<Long> getArbitraryLongs() {
        return arbitraryLongs;
    }

    public void setArbitraryLongs(List<Long> arbitraryLongs) {
        this.arbitraryLongs = arbitraryLongs;
    }

    public String toString() {

		return this.name + "'s arbitraryLong values:  => "
				+ Optional.ofNullable(this.arbitraryLongs).orElse(new ArrayList<Long>());

	}

    public Person getSister() {
        return sister;
    }

    public void setSister(Person sister) {
        this.sister = sister;
    }

    public Person getAncestor() {
        return ancestor;
    }

    public void setAncestor(Person ancestor) {
        this.ancestor = ancestor;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}
}
