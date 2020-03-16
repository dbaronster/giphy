package com.example.config;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class GiphyServiceConfig {
	
	@Value("${application.giphyConfig.host}")
	private String host;
	
	public String getHost() {
		return host;
	}
}