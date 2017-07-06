package com.rgabay.embedded_sdn_sproc.procedure;

import com.rgabay.embedded_sdn_sproc.domain.Entity;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by rossgabay on 5/25/17.
 */
@Transactional
public class PersonProc {
    private static final int DEFAULT_RECO_LIMIT = 10;

    @Context
    public GraphDatabaseService db;

    @Procedure(name = "com.rgabay.sprocnode", mode = Mode.READ)
    public Stream<NodeResult> getRecommendations(@Name("label") String label) {

        ResourceIterator<Node> input = db.findNodes(Label.label(label));
        if (null == input) {
            return Stream.empty();
        }

        return input.stream().map(NodeResult::new);
    }

    @Procedure(name = "com.rgabay.popdev2", mode = Mode.READ)
    public Stream<PathResult> populateDevices(@Name("startNode") Object start) {

        Node startNode = (Node) start;
        startNode = db.getNodeById(startNode.getId());
        RelationshipType hasRel = RelationshipType.withName("HAS");
        Stream<PathResult> paths = traverseByType(startNode, hasRel);
        return paths;


    }

    @Procedure(name = "com.rgabay.popdev", mode = Mode.READ)
    public Stream<PathResult> populateDevices(@Name("startNode") Long nodeId) {
        Node startNode = db.getNodeById(nodeId);
        RelationshipType hasRel = RelationshipType.withName("HAS");
        Stream<PathResult> paths = traverseByType(startNode, hasRel);
        return paths;


    }

    private Stream<PathResult> traverseByType(Node startNode, RelationshipType relType) {
        TraversalDescription td = db.traversalDescription();
        td.breadthFirst();
        PathExpander<Entity> expander = PathExpanders.forTypeAndDirection(relType, Direction.OUTGOING);
        td.evaluator(Evaluators.toDepth(4));


//        List<Relationship> rels = Iterables.asList(td.traverse(startNode).relationships());
        List<Node> nodes = td.traverse(startNode).stream().map(Path::endNode).collect(Collectors.toList());
        List<Relationship> rels = coverNodes(nodes).collect(Collectors.toList());

        Stream<PathResult> resultStream = Stream.of(new PathResult(nodes, rels));

//        Stream<PathResult> resultStream =  td.traverse(startNode).stream().map(PathResult::new);

        return resultStream;
//        List<Node> nodes = new ArrayList<>();
//        for( Path path : td.traverse(startNode) ) {
//            // TODO how to add to result?
//        };
    }

    // non-parallelized utility method for use by other procedures
    public static Stream<Relationship> coverNodes(Collection<Node> nodes) {
        return nodes.stream()
                .flatMap(n ->
                        StreamSupport.stream(n.getRelationships(Direction.OUTGOING)
                                .spliterator(),false)
                                .filter(r -> nodes.contains(r.getEndNode())));
    }

    public class NodeResult {
        public Node node;

        NodeResult(Node node) {
            this.node = node;
        }
    }

    public class PathResult {
        public final List<Node> nodes;
        public final List<Relationship> relationships;

        public PathResult(List<Node> nodes, List<Relationship> relationships) {
            this.nodes = nodes;
            this.relationships = relationships;
        }

    }

    public class GraphResult {
        public final List<Node> nodes;
        public final List<Relationship> relationships;

        public GraphResult(List<Node> nodes, List<Relationship> relationships) {
            this.nodes = nodes;
            this.relationships = relationships;
        }
    }

}
