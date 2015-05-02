package net.hazychill.ikafs.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.models.ConfigEntry;

import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class ConfigManager {
	Map<String, String> configMap;

	public ConfigManager() {
		configMap = new HashMap<String, String>();
	}

	public void updateAll(List<ConfigEntry> configEntries) {
		Datastore.put(configEntries);
		MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
		for (ConfigEntry entry : configEntries) {
			String configKey = entry.getKey().getName();
			String configValue = entry.getValue();
			ms.put(configKey, configValue);
			configMap.put(configKey, configValue);
		}
	}

	public List<ConfigEntry> listConfigs() {
		List<ConfigEntry> configEntries = new ArrayList<ConfigEntry>();
		for (String configKey : IkafsConstants.CONFIG_KEYS) {
			ConfigEntry entry = new ConfigEntry();
			Key key = Datastore.createKey(ConfigEntry.class, configKey);
			entry.setKey(key);
			String configValue;
			if (configMap.containsKey(configKey)) {
				configValue = configMap.get(configKey);
			}
			else {
				MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
				configValue = (String) ms.get(configKey);
				if (configValue == null) {
					try {
						ConfigEntry datastoreStoredEntry = Datastore.get(ConfigEntry.class, key);
						configValue = datastoreStoredEntry.getValue();
					}
					catch (EntityNotFoundRuntimeException e) {
						configValue = "!NULL";
					}
				}
				configMap.put(configKey, configValue);
			}
			entry.setValue(configValue);
			configEntries.add(entry);
		}

		return configEntries;
	}

	public String get(String configKey) {
		String configValue;

		if (configMap.containsKey(configKey)) {
			configValue = configMap.get(configKey);
		}
		else {
			MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
			configValue = (String) ms.get(configKey);
			if (configValue == null || configValue.length() == 0) {
				Key key = Datastore.createKey(ConfigEntry.class, configKey);
				ConfigEntry entry = Datastore.get(ConfigEntry.class, key);
				configValue = entry.getValue();
			}
			configMap.put(configKey, configValue);
		}

		return configValue;
	}

	public void set(String configKey, String configValue) {
		ConfigEntry entry = new ConfigEntry();
		Key key = Datastore.createKey(ConfigEntry.class, configKey);
		entry.setKey(key);
		entry.setValue(configValue);
		Datastore.put(entry);

		MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
		ms.put(configKey, configValue);
		configMap.put(configKey, configValue);
	}

	public int getInt(String configKey) {
		String configValueStr = get(configKey);
		int configValue = Integer.valueOf(configValueStr);
		return configValue;
	}

	public void setInt(String configKey, int configValue) {
		String configValueStr = Integer.toString(configValue);
		set(configKey, configValueStr);
	}

	public long getLong(String configKey) {
		String configValueStr = get(configKey);
		long configValue = Long.valueOf(configValueStr);
		return configValue;
	}

	public void setLong(String configKey, long configValue) {
		String configValueStr = Long.toString(configValue);
		set(configKey, configValueStr);
	}
}
