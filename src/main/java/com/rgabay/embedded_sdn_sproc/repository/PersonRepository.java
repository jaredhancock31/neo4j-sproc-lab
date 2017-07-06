package com.rgabay.embedded_sdn_sproc.repository;


import com.rgabay.embedded_sdn_sproc.domain.Person;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PersonRepository extends GraphRepository<Person> {

    Person findByName(String name);

    @Query ("match (n:Person) return n")
    List<Person> getPlist();

    @Query ("CALL com.rgabay.sprocnode(\"Person\")")
    List<Person> getPlistProc();
}
