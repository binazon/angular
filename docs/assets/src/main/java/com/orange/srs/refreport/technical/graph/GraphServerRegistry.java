package com.orange.srs.refreport.technical.graph;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.lifecycle.LifecycleException;
import org.neo4j.tooling.GlobalGraphOperations;

import com.orange.srs.refreport.model.TO.inventory.GraphDatabaseServiceTO;
import com.orange.srs.refreport.model.TO.inventory.GraphDatabaseServiceTOList;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy.GraphServiceStateEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Singleton
@Startup
@DependsOn("Log4jStartupBean")
public class GraphServerRegistry {

	private static final Logger LOGGER = Logger.getLogger(GraphServerRegistry.class);

	private GraphDatabaseServiceWithStateProxy currentGraphService;

	private Set<GraphDatabaseServiceWithStateProxy> graphServiceRegistry = Collections
			.synchronizedSet(new HashSet<GraphDatabaseServiceWithStateProxy>());

	@Lock(LockType.READ)
	public GraphDatabaseServiceWithStateProxy getCurrentService() {
		return getOrSetCurrentService(null);
	}

	@Lock(LockType.WRITE)
	private GraphDatabaseServiceWithStateProxy getOrSetCurrentService(GraphDatabaseServiceWithStateProxy service) {
		if (service != null) {
			currentGraphService = service;
		} else if (currentGraphService != null) {
			currentGraphService.addClient();
		}

		return currentGraphService;
	}

	@Lock(LockType.READ)
	protected void registerGraphService(GraphDatabaseServiceWithStateProxy service) throws BusinessException {
		synchronized (graphServiceRegistry) {
			boolean insertionInRegistrySuccessful = graphServiceRegistry.add(service);
			if (!insertionInRegistrySuccessful) {
				throw new BusinessException(BusinessException.GRAPH_ALREADY_EXISTS_IN_REGISTRY_EXCEPTION,
						BusinessException.GRAPH_ALREADY_EXISTS_IN_REGISTRY_CODE);
			}
		}
	}

	public GraphDatabaseServiceWithStateProxy createGraphDatabaseService(String location, String[] nodeIndexes,
			SOAContext context) throws IOException, BusinessException {
		GraphDatabaseServiceWithStateProxy proxy = null;
		GraphDatabaseService newService = null;

		try {
			File graphDatabaseFolder = new File(location);
			if (!graphDatabaseFolder.exists()) {
				graphDatabaseFolder.mkdir();
				LOGGER.info(SOATools.buildSOALogMessage(context,
						"Create graph directory " + graphDatabaseFolder.getAbsolutePath()));
			} else {
				throw new BusinessException(
						BusinessException.GRAPH_FOLDER_CREATION_EXCEPTION + " - location=" + location,
						BusinessException.GRAPH_FOLDER_CREATION_EXCEPTION_CODE);
			}

			/*
			 * newService = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
			 * graphDatabaseFolder.getAbsolutePath()) .setConfig( GraphDatabaseSettings.node_auto_indexing, "true" )
			 * .setConfig( GraphDatabaseSettings.node_keys_indexable, "type,entityid" )
			 * .setConfig(GraphDatabaseSettings.cache_type,GraphDatabaseSettings. CacheTypeSetting.strong)
			 * .setConfig(GraphDatabaseSettings.dump_configuration, "true") .newGraphDatabase();
			 */

			newService = GraphDatabaseServiceHelper.createGraphDatabaseService(graphDatabaseFolder, nodeIndexes,
					Configuration.configGraphCachePolicy);

			proxy = new GraphDatabaseServiceWithStateProxy(newService, this, graphDatabaseFolder);

			for (String index : proxy.getIndexes()) {
				LOGGER.info(SOATools.buildSOALogMessage(context, "AutoIndexing node property " + index));
			}

			registerGraphService(proxy);
		} catch (LifecycleException lcex) {
			if (newService != null) {
				try {
					newService.shutdown();
				} catch (Exception e) {
					LOGGER.error(SOATools.buildSOALogMessage(context,
							"Try to shutdown graph service unstance after failure to create " + location));
				}
			}

			if (proxy != null && proxy.getService() != null) {
				GraphDatabaseServiceHelper.stopGraphDatabaseService(proxy.getService());
			}

			throw lcex;
		}

		return proxy;
	}

	@Lock(LockType.WRITE)
	protected void activateGraphService(GraphDatabaseServiceWithStateProxy service, SOAContext context) {
		GraphDatabaseServiceWithStateProxy oldService = currentGraphService;
		getOrSetCurrentService(service);
		try {
			if (oldService != null) {
				oldService.inactivate(context);
			}
		} catch (BusinessException bex) {
			LOGGER.error(SOATools.buildSOALogMessage(context, "Cannot garbage GraphDatabase " + oldService, bex));
		}
	}

	@Lock(LockType.READ)
	protected void garbageGraphService(GraphDatabaseServiceWithStateProxy service, SOAContext context)
			throws BusinessException {
		synchronized (graphServiceRegistry) {
			graphServiceRegistry.remove(service);
		}

		if (service != null) {
			if (service.getState() == GraphServiceStateEnum.TO_GARBAGE) {
				destroyGraphDatabaseService(service, true, context);
				LOGGER.info("Garbage " + service);
			} else {
				synchronized (graphServiceRegistry) {
					graphServiceRegistry.add(service);
				}
				throw new BusinessException(BusinessException.INCONSISTENT_GRAPH_STATE_FOR_OPERATION_EXCEPTION,
						BusinessException.INCONSISTENT_GRAPH_STATE_FOR_OPERATION_CODE);
			}
		} else {
			throw new BusinessException(BusinessException.UNKNOWN_GRAPH__TO_REGISTRY_EXCEPTION,
					BusinessException.UNKNOWN_GRAPH_TO_REGISTRY_CODE);
		}
	}

	private void destroyGraphDatabaseService(GraphDatabaseServiceWithStateProxy service, boolean delete,
			SOAContext context) {
		/*
		 * try { GraphDatabaseServiceHelper.clearGraphDatabaseService(service.getService()); }catch(Exception e) {
		 * LOGGER.error(SOATools.buildSOALogMessage(context, "Cannot clear graph " + service), e); }
		 */
		try {
			GraphDatabaseServiceHelper.stopGraphDatabaseService(service.getService());
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(context, "Cannot stop graph " + service), e);
		}

		if (delete) {
			try {
				FileUtils.forceDelete(service.getDirectory());
			} catch (Exception ex) {
				LOGGER.error(SOATools.buildSOALogMessage(context, "Cannot delete database folder " + service), ex);
			}
		}
	}

	@Lock(LockType.READ)
	public GraphDatabaseServiceTOList getGraphServicesList() {
		GraphDatabaseServiceTOList results = new GraphDatabaseServiceTOList();
		synchronized (graphServiceRegistry) {
			for (GraphDatabaseServiceWithStateProxy service : graphServiceRegistry) {
				results.graphDatabaseServices
						.add(GraphDatabaseServiceTO.makeFromGraphDatabaseServiceWithStateProxy(service));
			}
		}

		return results;
	}

	@Lock(LockType.READ)
	public void forceGraphServicePurge(String databaseFolder, SOAContext context) throws BusinessException {
		File databaseFolderToRemove = new File(databaseFolder);

		synchronized (graphServiceRegistry) {
			GraphDatabaseServiceWithStateProxy serviceToPurge = null;
			boolean found = false;
			Iterator<GraphDatabaseServiceWithStateProxy> servicesIterator = graphServiceRegistry.iterator();
			while (servicesIterator.hasNext() && !found) {
				GraphDatabaseServiceWithStateProxy currentServiceProxy = servicesIterator.next();

				if (currentServiceProxy.getDirectory().equals(databaseFolderToRemove)) {
					destroyGraphDatabaseService(currentServiceProxy, true, context);
					serviceToPurge = currentServiceProxy;
					found = true;
					LOGGER.info(SOATools.buildSOALogMessage(context, "Force purge of " + currentServiceProxy));
				}
			}

			if (found) {
				graphServiceRegistry.remove(serviceToPurge);
				if (serviceToPurge == currentGraphService) {
					currentGraphService = null;
				}
			} else {
				throw new BusinessException(
						BusinessException.UNKNOWN_GRAPH__TO_REGISTRY_EXCEPTION + " : " + databaseFolder,
						BusinessException.UNKNOWN_GRAPH_TO_REGISTRY_CODE);
			}
		}
	}

	@PreDestroy
	@Lock(LockType.READ)
	public void purgeRegistry() {
		SOAContext context = SOATools.buildSOAContext(null);

		LOGGER.info(SOATools.buildSOALogMessage(context, "Purging registry"));
		synchronized (graphServiceRegistry) {
			for (GraphDatabaseServiceWithStateProxy serviceProxyToRemove : graphServiceRegistry) {
				try {
					boolean deleteService = serviceProxyToRemove != currentGraphService;

					LOGGER.info(SOATools.buildSOALogMessage(context, "Purging graph " + serviceProxyToRemove));
					destroyGraphDatabaseService(serviceProxyToRemove, deleteService, context);

				} catch (Exception ex) {
					LOGGER.error(SOATools.buildSOALogMessage(context, "Fail to purge graph " + serviceProxyToRemove));
				}
			}

			graphServiceRegistry.clear();
			currentGraphService = null;
		}

		LOGGER.info(SOATools.buildSOALogMessage(context, "Purge graph done. Current graph is " + currentGraphService
				+ " - " + graphServiceRegistry.size() + " remaining in registry"));
	}

	public GraphDatabaseServiceWithStateProxy createCurrentDatabaseGraphFromExistingFolder(String location,
			String[] nodeIndexes, SOAContext context) throws IOException, BusinessException {
		GraphDatabaseServiceWithStateProxy proxy = null;
		GraphDatabaseService newService = null;

		try {
			File graphDatabaseFolder = new File(location);
			if (!graphDatabaseFolder.exists()) {
				throw new BusinessException(
						BusinessException.WRONG_PARAMETER_EXCEPTION + " - No folder with location=" + location,
						BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
			} else {
				LOGGER.info(SOATools.buildSOALogMessage(context, "Graph database folder " + location + " found"));
			}

			newService = GraphDatabaseServiceHelper.createGraphDatabaseService(graphDatabaseFolder, nodeIndexes,
					Configuration.configGraphCachePolicy);
			proxy = new GraphDatabaseServiceWithStateProxy(newService, this, graphDatabaseFolder);

			for (String index : proxy.getIndexes()) {
				LOGGER.info(SOATools.buildSOALogMessage(context, "AutoIndexing node property " + index));
			}

			registerGraphService(proxy);
			LOGGER.info(SOATools.buildSOALogMessage(context, "Graph database registered"));

			proxy.activate(context);
			LOGGER.info(SOATools.buildSOALogMessage(context, "Graph database activated"));
		} catch (LifecycleException lcex) {
			if (newService != null) {
				try {
					newService.shutdown();
				} catch (Exception e) {
					LOGGER.error(SOATools.buildSOALogMessage(context,
							"Try to shutdown graph service unstance after failure to create " + location));
				}
			}

			if (proxy != null && proxy.getService() != null) {
				GraphDatabaseServiceHelper.stopGraphDatabaseService(proxy.getService());
			}

			throw lcex;
		}

		return proxy;
	}

	public void loadCurrentGraphInMemory(SOAContext soaContext) {
		if (currentGraphService != null) {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Start loading graph in memory"));
			for (Node node : GlobalGraphOperations.at(currentGraphService.getService()).getAllNodes()) {
				IteratorUtil.count(node.getRelationships());
			}
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Finished loading graph in memory"));
		} else {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "No graphDatabase active"));
		}
	}

	/**
	 * Flush all the Neo4J graph databases with state NEW/INACTIVE/TO_GARBAGE from memory.<br/>
	 * Keep the ACTIVE ones.<br/>
	 * Do not delete the Neo4J graph database files on disk.<br/>
	 * 
	 * @param soaContext
	 * @return the number of Neo4J graph databases that have been flushed from memory
	 * 
	 * @author Pascal Morvan (Atos)
	 * @see [GSD02314-2018-114206-Openstat-BaseGraphNeo4J--ProgrammerLeMenage, PR_02314_P228_001]
	 */
	@Lock(LockType.READ)
	public Integer flushAllExceptActiveGraphDatabases(SOAContext soaContext) {
		synchronized (graphServiceRegistry) {
			Set<GraphDatabaseServiceWithStateProxy> servicesToRemove = new HashSet<GraphDatabaseServiceWithStateProxy>();
			for (GraphDatabaseServiceWithStateProxy service : graphServiceRegistry) {
				GraphServiceStateEnum state = service.getState();
				if (state == GraphServiceStateEnum.INACTIVE || state == GraphServiceStateEnum.TO_GARBAGE
						|| state == GraphServiceStateEnum.NEW) {
					destroyGraphDatabaseService(service, false, soaContext);
					LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Flush service" + service));

					if (currentGraphService == service) {
						currentGraphService = null;
					}
					servicesToRemove.add(service);
				}
			}
			graphServiceRegistry.removeAll(servicesToRemove);
			return servicesToRemove.size();
		}
	}

}
