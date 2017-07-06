
package com.rgabay.embedded_sdn_sproc;

import com.rgabay.embedded_sdn_sproc.domain.Device;
import com.rgabay.embedded_sdn_sproc.domain.Entity;
import com.rgabay.embedded_sdn_sproc.domain.Person;
import com.rgabay.embedded_sdn_sproc.procedure.GraphRefactor;
import com.rgabay.embedded_sdn_sproc.procedure.PersonProc;
import com.rgabay.embedded_sdn_sproc.repository.PersonRepository;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EntityScan("com.rgabay.embedded_sdn_sproc.domain")
@EnableNeo4jRepositories
@EnableTransactionManagement
public class Application {

	private final static Logger log = LoggerFactory.getLogger(Application.class);
    @Transactional
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);

	}

	@Bean
    @Transactional(propagation = Propagation.REQUIRED)
	CommandLineRunner demo(/*PersonRepository personRepository,*/ Neo4jTemplate template) {
		return args -> {

//			HttpDriver driver = (HttpDriver) Components.driver();
//			BoltDriver driver = (BoltDriver) Components.driver();
            EmbeddedDriver driver = (EmbeddedDriver) Components.driver();
            GraphDatabaseService databaseService = driver.getGraphDatabaseService();
            ((GraphDatabaseAPI) databaseService).getDependencyResolver().resolveDependency(Procedures.class).registerProcedure(PersonProc.class);
			((GraphDatabaseAPI) databaseService).getDependencyResolver().resolveDependency(Procedures.class).registerProcedure(GraphRefactor.class);


            Person greg = new Person("Greg");
            greg.setArbitraryLongs(Arrays.asList(new Long(1), new Long(2)));
			template.save(greg);
			greg = template.load(Person.class, greg.getId());

            Result r = template.query("CALL com.rgabay.sprocnode(\"Person\")", Collections.EMPTY_MAP);
            r.forEach(System.out::println);

			Device ipad = new Device();
			ipad.setModel("ipad");
			template.save(ipad);

			Device g6 = new Device();
			g6.setModel("lgG6");
			template.save(g6);

			greg.setDevices(Arrays.asList(ipad, g6));
			template.save(greg, -1);

			Person jill = new Person("jill");
			template.save(jill);

			greg.setSister(jill);
            greg = template.save(greg, -1);

			Person kobe = new Person("kobe");
			jill.setAncestor(kobe);
			template.save(jill, 1);

			String query = "MATCH (a:Person {name:'Greg'})-[r]->(b:Person) " +
                           "WITH a,r,b " +
                           "MATCH (b)-[:ANCESTOR]->(d:Person) " +
                           "CALL cisco.refactor.to(r, d) " +
                           "YIELD output " +
                           "RETURN output";

			r = template.query(query, Collections.EMPTY_MAP);


//			Entity res = template.queryForObject(Entity.class, "CALL com.rgabay.popdev({startNode}) YIELD path " +
//                                     "RETURN path", params);
//			Result res = template.query("MATCH (n:Entity) WHERE ID(n)={startNode} CALL com.rgabay.popdev2(n) " +
//                                        "YIELD nodes, relationships " +
//                                        "RETURN nodes as nodes, relationships as rels ", params);

            greg = template.load(Person.class, greg.getId(), -1);

			r.forEach(System.out::println);


		};
	}

//     embedded driver config example, to make the db non-permanent, remove .setURI call
	@Bean
    public Configuration configuration() {
        Configuration config = new Configuration();
        config
                .driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
                //.setURI("file:///var/tmp/graph.db");
        return config;
    }

//	// HTTP driver config example
//	@Bean
//	public Configuration configuration() {
//		Configuration config = new Configuration();
//		config
//				.driverConfiguration()
//				.setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
//				.setURI("http://localhost:7474");
//		return config;
//	}

	@Bean
	public SessionFactory sessionFactory() {
		return new SessionFactory(configuration(), "com.rgabay.embedded_sdn_sproc.domain", "com.rgabay.embedded_sdn_sproc.repository");

	}

    @Bean
    public Neo4jTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new Neo4jTransactionManager(sessionFactory);
    }

    @Bean
    public Neo4jTemplate neo4jTemplate(SessionFactory sessionFactory) {
        return new Neo4jTemplate(sessionFactory);
    }
}
