/**
 * 
 */
package vnet.sms.common.shell.clamshellspring.internal;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collections;
import java.util.Map;

import org.clamshellcli.api.Configurator;

/**
 * @author obergner
 * 
 */
final class StaticConfigurator implements Configurator {

	private final Map<String, Map<String, ? extends Object>>	configMap;

	private final Map<String, Map<String, ? extends Object>>	controllersMap;

	private final Map<String, String>	                     propertiesMap;

	/**
	 * @param configMap
	 * @param controllersMap
	 * @param propertiesMap
	 */
	StaticConfigurator(
	        final Map<String, Map<String, ? extends Object>> configMap,
	        final Map<String, Map<String, ? extends Object>> controllersMap,
	        final Map<String, String> propertiesMap) {
		notNull(configMap, "Argument 'configMap' must not be null");
		notNull(controllersMap, "Argument 'controllersMap' must not be null");
		notNull(propertiesMap, "Argument 'propertiesMap' must not be null");
		this.configMap = Collections.unmodifiableMap(configMap);
		this.controllersMap = Collections.unmodifiableMap(controllersMap);
		this.propertiesMap = Collections.unmodifiableMap(propertiesMap);
	}

	/**
	 * @see org.clamshellcli.api.Configurator#getConfigMap()
	 */
	@Override
	public Map<String, Map<String, ? extends Object>> getConfigMap() {
		return this.configMap;
	}

	/**
	 * @see org.clamshellcli.api.Configurator#getControllersMap()
	 */
	@Override
	public Map<String, Map<String, ? extends Object>> getControllersMap() {
		return this.controllersMap;
	}

	/**
	 * @see org.clamshellcli.api.Configurator#getPropertiesMap()
	 */
	@Override
	public Map<String, String> getPropertiesMap() {
		return this.propertiesMap;
	}
}
