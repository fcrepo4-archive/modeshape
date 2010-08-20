package org.modeshape.rhq.plugin.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.modeshape.jboss.managed.ManagedRepository;
import org.modeshape.jboss.managed.ManagedSequencerConfig;
import org.modeshape.rhq.plugin.objects.ExecutedResult;
import org.modeshape.rhq.plugin.util.PluginConstants.ComponentType.Connector;
import org.modeshape.rhq.plugin.util.PluginConstants.ComponentType.Engine;
import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyMapToPropertiesValueAdapter;
import org.rhq.plugins.jbossas5.connection.ProfileServiceConnection;
import org.rhq.plugins.jbossas5.util.ConversionUtils;

import com.sun.istack.Nullable;

public class ModeShapeManagementView implements PluginConstants {

	private static final Log LOG = LogFactory
			.getLog(PluginConstants.DEFAULT_LOGGER_CATEGORY);

	private static final MetaValueFactory metaValueFactory = MetaValueFactory
			.getInstance();

	public ModeShapeManagementView() {

	}

	/*
	 * Metric methods
	 */
	public Object getMetric(ProfileServiceConnection connection,
			String componentType, String identifier, String metric,
			Map<String, Object> valueMap) throws Exception {
		Object resultObject = new Object();

		if (componentType.equals(ComponentType.SequencingService.NAME)) {
			resultObject = getSequencerServiceMetric(connection, componentType,
					metric, valueMap);
		} else if (componentType.equals(ComponentType.Repository.NAME)) {
			resultObject = getSequencerServiceMetric(connection, componentType, metric,
					valueMap);
		}

		return resultObject;
	}

	/*
	 * Metric methods
	 */
	private Object getSequencerServiceMetric(
			ProfileServiceConnection connection, String componentType,
			String metric, Map<String, Object> valueMap) throws Exception {

		Object resultObject = new Object();
		MetaValue value = null;

		if (metric.equals(ComponentType.SequencingService.Metrics.NUM_NODES_SEQUENCED) ||
			metric.equals(ComponentType.SequencingService.Metrics.NUM_NODES_SKIPPED)) {
			value = executeSequencingServiceOperation(connection, metric, valueMap);
			resultObject = ProfileServiceUtil.stringValue(value);
		} 
		return resultObject;
	}

	/*
	 * Operation methods
	 */

	public void executeOperation(ProfileServiceConnection connection,
			ExecutedResult operationResult, final Map<String, Object> valueMap) {

		if (operationResult.getComponentType().equals(
				ComponentType.Engine.MODESHAPE_ENGINE)) {
			executeEngineOperation(connection, operationResult, operationResult
					.getOperationName(), valueMap);
		} else if (operationResult.getComponentType().equals(
				ComponentType.Repository.NAME)) {
			// TODO Implement repo ops
		} else if (operationResult.getComponentType().equals(
				ComponentType.Connector.NAME)) {
			executeConnectorOperation(connection, operationResult,
					operationResult.getOperationName(), valueMap);
		}

	}

	private void executeEngineOperation(ProfileServiceConnection connection,
			ExecutedResult operationResult, final String operationName,
			final Map<String, Object> valueMap) {

		if (operationName.equals(Engine.Operations.RESTART)) {
			try {
				executeManagedOperation(ProfileServiceUtil
						.getManagedEngine(connection), operationName,
						new MetaValue[] { null });
			} catch (Exception e) {
				final String msg = "Exception executing operation: " + Engine.Operations.RESTART; //$NON-NLS-1$
				LOG.error(msg, e);
			}
		} else if (operationName.equals(Engine.Operations.SHUTDOWN)) {
			try {
				executeManagedOperation(ProfileServiceUtil
						.getManagedEngine(connection), operationName,
						new MetaValue[] { null });
			} catch (Exception e) {
				final String msg = "Exception executing operation: " + Engine.Operations.SHUTDOWN; //$NON-NLS-1$
				LOG.error(msg, e);
			}
		}
	}

	private void executeConnectorOperation(ProfileServiceConnection connection,
			ExecutedResult operationResult, final String operationName,
			final Map<String, Object> valueMap) {

		if (operationName.equals(Connector.Operations.PING)) {
			try {
				String connectorName = (String) valueMap
						.get(Connector.Operations.Parameters.CONNECTOR_NAME);
				MetaValue[] args = new MetaValue[] { metaValueFactory
						.create(connectorName) };
				MetaValue value = executeManagedOperation(ProfileServiceUtil
						.getManagedEngine(connection), operationName,
						operationResult, args);
				operationResult.setContent(value);
			} catch (Exception e) {
				final String msg = "Exception executing operation: " + Connector.Operations.PING; //$NON-NLS-1$
				LOG.error(msg, e);
			}
		}
	}

	private MetaValue executeSequencingServiceOperation(
			ProfileServiceConnection connection, final String operationName,
			final Map<String, Object> valueMap) {
		MetaValue value = null;
		try {
			MetaValue[] args = new MetaValue[] {};
			value = executeManagedOperation(ProfileServiceUtil
					.getManagedSequencingService(connection), operationName, args);
		} catch (Exception e) {
			final String msg = "Exception executing operation: " + operationName; //$NON-NLS-1$
			LOG.error(msg, e);
		}
		
		return value;

	}

	/**
	 * @param mc
	 * @param operation
	 * @param args
	 * @return {@link MetaValue}
	 * @throws Exception
	 */
	public static MetaValue executeManagedOperation(ManagedComponent mc,
			String operation, @Nullable MetaValue... args) throws Exception {

		for (ManagedOperation mo : mc.getOperations()) {
			String opName = mo.getName();
			if (opName.equals(operation)) {
				try {
					if (args == null || (args.length == 1 && args[0] == null)) {
						return mo.invoke();
					}
					return mo.invoke(args);
				} catch (Exception e) {
					final String msg = "Exception invoking " + operation; //$NON-NLS-1$
					LOG.error(msg, e);
					throw e;
				}
			}
		}
		throw new Exception("No operation found with given name =" + operation); //$NON-NLS-1$

	}

	/**
	 * @param mc
	 * @param operation
	 * @param args
	 * @param operationResult
	 * @return {@link MetaValue}
	 * @throws Exception
	 */
	public static MetaValue executeManagedOperation(ManagedComponent mc,
			String operation, ExecutedResult operationResult,
			@Nullable MetaValue... args) throws Exception {

		for (ManagedOperation mo : mc.getOperations()) {
			String opName = mo.getName();
			if (opName.equals(operation)) {
				operationResult.setManagedOperation(mo);
				try {
					if (args == null || (args.length == 1 && args[0] == null)) {
						return mo.invoke();
					}
					return mo.invoke(args);
				} catch (Exception e) {
					final String msg = "Exception invoking " + operation; //$NON-NLS-1$
					LOG.error(msg, e);
					throw e;
				}
			}
		}
		throw new Exception("No operation found with given name =" + operation); //$NON-NLS-1$

	}

	public static Collection<ManagedRepository> getRepositoryCollectionValue(
			MetaValue pValue) {
		Collection<ManagedRepository> list = new ArrayList<ManagedRepository>();
		MetaType metaType = pValue.getMetaType();
		if (metaType.isCollection()) {
			for (MetaValue value : ((CollectionValueSupport) pValue)
					.getElements()) {
				if (value.getMetaType().isComposite()) {
					ManagedRepository repository = (ManagedRepository) MetaValueFactory
							.getInstance().unwrap(value);
					list.add(repository);
				} else {
					throw new IllegalStateException(pValue
							+ " is not a Composite type"); //$NON-NLS-1$
				}
			}
		}
		return list;
	}

	public static String getConnectorPingString(MetaValue pValue)
			throws Exception {
		MetaType metaType = pValue.getMetaType();
		StringBuffer sb = new StringBuffer();
		if (metaType.isCollection()) {
			for (MetaValue value : ((CollectionValueSupport) pValue)
					.getElements()) {
				String resultValue = ProfileServiceUtil.stringValue(value);
				sb.append(resultValue + " ");
			}
		}
		return sb.toString();
	}

	public static Collection<ManagedSequencerConfig> getSequencerCollectionValue(
			MetaValue pValue) {
		Collection<ManagedSequencerConfig> list = new ArrayList<ManagedSequencerConfig>();
		MetaType metaType = pValue.getMetaType();
		if (metaType.isCollection()) {
			for (MetaValue value : ((CollectionValueSupport) pValue)
					.getElements()) {
				if (value.getMetaType().isComposite()) {
					ManagedSequencerConfig sequencer = (ManagedSequencerConfig) MetaValueFactory
							.getInstance().unwrap(value);
					list.add(sequencer);
				} else {
					throw new IllegalStateException(pValue
							+ " is not a Composite type"); //$NON-NLS-1$
				}
			}
		}
		return list;
	}

	private Collection createReportResultList(List fieldNameList,
			Iterator objectIter) {
		Collection reportResultList = new ArrayList();

		while (objectIter.hasNext()) {
			Object object = objectIter.next();

			Class cls = null;
			try {
				cls = object.getClass();
				Iterator methodIter = fieldNameList.iterator();
				Map reportValueMap = new HashMap<String, String>();
				while (methodIter.hasNext()) {
					String fieldName = (String) methodIter.next();
					String methodName = fieldName;
					Method meth = cls.getMethod(methodName, (Class[]) null);
					Object retObj = meth.invoke(object, (Object[]) null);
					reportValueMap.put(fieldName, retObj);
				}
				reportResultList.add(reportValueMap);
			} catch (Throwable e) {
				System.err.println(e);
			}
		}
		return reportResultList;
	}

}