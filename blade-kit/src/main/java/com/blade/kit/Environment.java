/**
 * Copyright (c) 2016, biezhi 王爵 (biezhi.me@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blade.kit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Environment
 * 
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class Environment {

	private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

	private Map<String, String> envMap = new HashMap<String, String>(20);
	
	public Environment() {
		
	}
	
	public Environment(String confPath){
		this.envMap = loadMap(confPath);
	}
	
	private Map<String, String> loadMap(String confPath){
		Map<String, String> envMap = new HashMap<String, String>(20);
		Properties config = new Properties();
		
		InputStreamReader inr = null;
		try {
			InputStream inputStream = Environment.class.getClassLoader().getResourceAsStream(confPath);
			if(null != inputStream){
				inr = new InputStreamReader(inputStream, "UTF-8");
				config.load(inr);
				// parse properties file
				Set<Entry<Object, Object>> set = config.entrySet();
				if(CollectionKit.isNotEmpty(set)){
					Iterator<Map.Entry<Object, Object>> it = set.iterator();
					while (it.hasNext()) {
						Entry<Object, Object> entry = it.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						String fuKey = getWildcard(value);
						if(null != fuKey && null != config.get(fuKey)){
							String fuValue = config.get(fuKey).toString();
							value = value.replaceAll("\\$\\{" + fuKey + "\\}", fuValue);
						}
						envMap.put(key, value);
					}
					LOGGER.info("Load environment config [classpath:" + confPath + "]");
				}
			}
		} catch (IOException e) {
			LOGGER.error("Load environment config error", e);
		} finally {
			IOKit.closeQuietly(inr);
		}
		return envMap;
	}
	
	public static Environment load(String confPath) {
		return new Environment(confPath);
	}
	
	public Environment add(String confPath){
		this.envMap.putAll(loadMap(confPath));
		return this;
	}
	
	private static String getWildcard(String str) {
		if (null != str && str.indexOf("${") != -1) {
			int start = str.indexOf("${");
			int end = str.indexOf("}");
			if (start != -1 && end != -1) {
				return str.substring(start + 2, end);
			}
		}
		return null;
	}
	
	public Map<String, String> getEnvMap(){
		return envMap;
	}
	
	public String getString(String key) {
		String value = envMap.get(key);
		return null != value ? value.trim() : null;
	}
	
	public String getString(String key, String defaultValue) {
		return null != envMap.get(key) ? envMap.get(key) : defaultValue;
	}

	public Integer getInt(String key) {
		String value = getString(key);
		if (StringKit.isNotBlank(value)) {
			return Integer.valueOf(value);
		}
		return null;
	}

	public Integer getInt(String key, Integer defaultValue) {
		return null != getInt(key) ? getInt(key) : defaultValue;
	}

	public Long getLong(String key) {
		String value = getString(key);
		if (StringKit.isNotBlank(value)) {
			return Long.valueOf(value);
		}
		return null;
	}

	public Long getLong(String key, Long defaultValue) {
		return null != getLong(key) ? getLong(key) : defaultValue;
	}

	public Boolean getBoolean(String key) {
		String value = getString(key);
		if (StringKit.isNotBlank(value)) {
			return Boolean.valueOf(value);
		}
		return null;
	}

	public Boolean getBoolean(String key, boolean defaultValue) {
		return null != getBoolean(key) ? getBoolean(key) : defaultValue;
	}

}
