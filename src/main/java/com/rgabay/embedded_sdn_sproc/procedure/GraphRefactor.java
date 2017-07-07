package com.rgabay.embedded_sdn_sproc.procedure;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author jarhanco
 */
@Transactional
public class GraphRefactor {

    @Context
    public GraphDatabaseService db;

    /**
     * Redirects a relationships to a new target node.
     * Impl is from APOC.
     */
    @Procedure(name = "jared.refactor.to", mode = Mode.WRITE)
    @Description("jared.refactor.to(rel, endNode) redirect relationship to use new end-node")
    public Stream<RelationshipRefactorResult> to(@Name("relationship") Relationship rel, @Name("newNode") Node newNode) {
        RelationshipRefactorResult result = new RelationshipRefactorResult(rel.getId());
        try {
            Relationship newRel = rel.getStartNode().createRelationshipTo(newNode, rel.getType());
            copyProperties(rel,newRel);
            rel.delete();
            return Stream.of(result.withOther(newRel));
        } catch (Exception e) {
            return Stream.of(result.withError(e));
        }
    }

    private <T extends PropertyContainer> T copyProperties(PropertyContainer source, T target) {
        for (Map.Entry<String, Object> prop : source.getAllProperties().entrySet())
            target.setProperty(prop.getKey(), prop.getValue());
        return target;
    }

    public class RelationshipRefactorResult {
        public long input;
        public Relationship output;
        public String error;

        public RelationshipRefactorResult(Long id) {
            this.input = id;
        }

        public RelationshipRefactorResult withError(Exception e) {
            this.error = e.getMessage();
            return this;
        }

        public RelationshipRefactorResult withError(String message) {
            this.error = message;
            return this;
        }

        public RelationshipRefactorResult withOther(Relationship rel) {
            this.output = rel;
            return this;
        }
    }

}
