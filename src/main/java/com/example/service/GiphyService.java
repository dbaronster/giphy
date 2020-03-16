package com.example.service;

import java.security.Principal;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.example.config.GiphyServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class GiphyService {

	private static final String PATH = "/v1/gifs/search?=Up7hKBlxSNvsWc88FiNJXGuLB2939c9X&limit=25&offset=0";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	/*
	https://api.giphy.com/v1/gifs/search?limit=25&offset=0
	*/
	@Autowired
	private GiphyServiceConfig giphyConfig;
	
	public JsonNode search(String searchFor) {
		Client client = ClientBuilder.newClient();
		Response response = null;
		WebTarget target = client.target(giphyConfig.getHost() + PATH);
                target = target.queryParam("api_key", "Up7hKBlxSNvsWc88FiNJXGuLB2939c9X");
                target = target.queryParam("q", searchFor);
                target = target.queryParam("rating", "G");
                target = target.queryParam("lang", "en");
		String requestBody = "{\"grant_type\":\"client_credentials\"}";
		//String authorization = String.format("client_id: %s, client_secret: %s", clientId, clientSecret);
		Builder b = target.request(MediaType.TEXT_HTML_TYPE)
				//.header("Authorization", authorization)
				//.header("Content-Type", "application/json")
				;
		// add query params
		try {
			//response = b.post(Entity.json(requestBody));
			response = b.get();
			if (response.getStatus() == 200) {
				JsonNode responseNode = response.readEntity(JsonNode.class);
		//System.out.println("giphies node:" + responseNode);
		System.out.println("giphies data:" + responseNode.get("data"));
				return responseNode;//.get("data").toString();
			} else {
				throw new ProcessingException("Error in post: " + response.getStatus());
			}
		} catch (ProcessingException ex) {
			logger.error("Error requesting access token", ex);
			return new TextNode(Response.serverError().toString());
		} finally {
			if (response != null) {
				response.close();
				response = null;
			}
		}
	}
	
}
