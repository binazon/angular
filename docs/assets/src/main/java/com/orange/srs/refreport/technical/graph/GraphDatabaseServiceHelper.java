package com.orange.srs.refreport.technical.graph;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.factory.GraphDatabaseSettings.CacheTypeSetting;

public class GraphDatabaseServiceHelper {

	protected static void clearGraphDatabaseService(GraphDatabaseService service) throws Exception {
		ExecutionEngine engine = new ExecutionEngine(service);
		engine.execute("START n=node(*) MATCH n-[r?]->() DELETE r WHERE ID(n) <> 0 DELETE n");
	}

	protected static void stopGraphDatabaseService(GraphDatabaseService service) {
		service.shutdown();
	}

	protected static GraphDatabaseService createGraphDatabaseService(File graphDatabaseFolder, String[] nodeIndexes,
			String cachePolicy) {
		String nodeIndexesString = StringUtils.join(nodeIndexes, ',');

		String cacheTypeSetting = CacheTypeSetting.none;
		if ("NORMAL".equals(cachePolicy)) {
			cacheTypeSetting = CacheTypeSetting.gcr;
		} else if ("STRONG".equals(cachePolicy)) {
			cacheTypeSetting = CacheTypeSetting.strong;
		}

		GraphDatabaseService newService = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(graphDatabaseFolder.getAbsolutePath())
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				// .setConfig(GraphDatabaseSettings.node_cache_size, "1G")
				// .setConfig(GraphDatabaseSettings.relationship_cache_size, "1G")
				.setConfig(GraphDatabaseSettings.node_keys_indexable, nodeIndexesString)
				.setConfig(GraphDatabaseSettings.cache_type, cacheTypeSetting)
				.setConfig(GraphDatabaseSettings.dump_configuration, "true").newGraphDatabase();

		return newService;
	}

}
