package com.bme.opentsdb.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenTSDBClientMarkerConfiguration {

	@Bean
	public Marker openTSDBClientMarkerBean() {
		return new Marker();
	}

	class Marker {

	}

}