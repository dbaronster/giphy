/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.model.Favorite;
import com.example.service.GiphyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@SpringBootApplication
public class Main {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ObjectMapper mapper = new ObjectMapper();

	@Value("${spring.datasource.url}")
	private String dbUrl;

//	@Autowired
//	private DataSource dataSource;

	@Autowired
	private GiphyService giphyService;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@RequestMapping("/")
	String root() {
		return "index";
	}

	@RequestMapping("/index")
	String index() {
		return "favorites";
	}

	@GetMapping("/login")
	String login(Model model) {
		System.out.println("in login");
		return "login";
	}


    // Login form with error  
    @RequestMapping("/login-error")  
    public String loginError(Model model) {  
        model.addAttribute("loginError", true);  
        return "login";  
    }  

    @PostMapping(value="/loginSecure")
    public String login(@RequestAttribute("username") String userName, @RequestAttribute("password")  String password) {

		System.out.println("in loginSecure");

        //does the authentication
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userName,
                        password);
        SecurityContextHolder.getContext().setAuthentication(token);
        return "favorites";
    }
    
	@GetMapping("/search")
	String searchForm(Model model) {
		model.addAttribute("search", new GiphySearch());
		return "search";
	}

	@PostMapping("/search")
	String searchSubmit(@ModelAttribute GiphySearch search, Model model) {
		System.out.println("got search:" + search.getText());
		JsonNode searchResults = giphyService.search(search.getText());
		@SuppressWarnings("unchecked")
		List<String> gifUrls = (List<String>)parseJson(searchResults, "$.data[*].images.original.url");
		model.addAttribute("results", gifUrls);
		model.addAttribute("favorite", new Favorite());
		//System.out.println("giphies#:" + gifUrls);
		search.setResults(gifUrls);
		return "results";
	}

	@GetMapping("/results")
	String searchResultsForm(Model model) {
		model.addAttribute("favorite", new Favorite());
		return "results";
	}

	@PostMapping("/add")
	String addFavorite(@ModelAttribute Favorite favorite, Model model) {
		//TODO: get the user; add this favorite to the list
		System.out.println("add:" + favorite.getUrl());
		//model.addAttribute("favorite", new GiphyFavorite());
		return "redirect:favorites";
	}


	@GetMapping("/favorites")
	String favorites(Model model) {
		//TODO: get the user; get the favorites list
		List<Favorite> favorites = dummyFavorites();//user.getFavorites());
		model.addAttribute("favorites", favorites);
		return "favorites";
	}

	private static final String DUMMY_FAVS_JSON = "[{\"url\":\"https://media0.giphy.com/media/nxG4i6YuQnHzi/giphy.gif?cid=98fb8510ce6813bdb441ef8f818e55d068c61421c8b32ea7&rid=giphy.gif\",\"category\":\"sports\"}]";
	
	private List<Favorite> dummyFavorites() {
		List<Favorite> dummyFavorites = Collections.emptyList();
		try {
			dummyFavorites = mapper.readValue(DUMMY_FAVS_JSON, new TypeReference<List<Favorite>>(){});
			System.out.println("deparsed JSON: "+dummyFavorites);
		}
		catch (Exception e) {
			System.out.println("error deparsing JSON");
		}
		return dummyFavorites;
	}

	@ModelAttribute("categoryValues")
	public String[] getMultiCheckboxAllValues() {
		return new String[] {"-select category-", "Sports", "Entertainment", "Political", "Health", "Home"};
	}

//	@RequestMapping("/db")
//	String db(Map<String, Object> model) {
//		try (Connection connection = dataSource.getConnection()) {
//			Statement stmt = connection.createStatement();
//			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
//			stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
//			ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
//
//			ArrayList<String> output = new ArrayList<String>();
//			while (rs.next()) {
//				output.add("Read from DB: " + rs.getTimestamp("tick"));
//			}
//
//			model.put("records", output);
//			return "db";
//		} catch (Exception e) {
//			model.put("message", e.getMessage());
//			return "error";
//		}
//	}
//
//	@Bean
//	public DataSource dataSource() throws SQLException {
//		if (dbUrl == null || dbUrl.isEmpty()) {
//			return new HikariDataSource();
//		} else {
//			HikariConfig config = new HikariConfig();
//			config.setJdbcUrl(dbUrl);
//			return new HikariDataSource(config);
//		}
//	}

	static private Object parseJson(JsonNode json, String path) {
		Configuration configuration = Configuration.builder().build();

		DocumentContext dc = JsonPath.parse(json.toString(), configuration);
		return dc.read(path);
	}

	private static class GiphySearch {
		private String text;
		private List<String> results;

		public String getText() {
			return text;
		}
		public void setText(String s) {
			text = s;
		}
		public List<String> getResults() {
			return results;
		}
		public void setResults(List<String> r) {
			results = r;
		}
	}

}
