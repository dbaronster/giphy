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

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.dto.UserRegistrationDto;
import com.example.model.Favorite;
import com.example.model.User;
import com.example.service.GiphyService;
import com.example.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Controller
@SpringBootApplication
public class Main {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final ObjectMapper mapper = new ObjectMapper();

	@Value("${spring.datasource.url}")
	private String dbUrl;

//	@Autowired
//	private DataSource dataSource;

	@Autowired
	private GiphyService giphyService;

	@Autowired
	private UserService userService;

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
        model.addAttribute("user", new User());
		log.debug("in login");
		return "login";
	}

    // Login form with error  
    @RequestMapping("/login-error")  
    public String loginError(Model model) {  
        model.addAttribute("loginError", true);  
        return "login";  
    }

    @RequestMapping("/registration")
    public String createUserForm(Model model) {
		log.debug("in registration");
        model.addAttribute("user", new UserRegistrationDto());
        return "createuser";
    }

    @PostMapping("/register")
	String register(@ModelAttribute UserRegistrationDto user, Model model) {
		log.debug("in register");
		userService.create(user);
		return "login";
	}

    @PostMapping(value="/loginSecure")
    public String login(@RequestAttribute("username") String userName, @RequestAttribute("password")  String password) {

		log.debug("in loginSecure");

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
		log.debug("got search:" + search.getText());
		JsonNode searchResults = giphyService.search(search.getText());
		@SuppressWarnings("unchecked")
		List<String> gifUrls = (List<String>)parseJson(searchResults, "$.data[*].images.original.url");
		model.addAttribute("results", gifUrls);
		model.addAttribute("favorite", new Favorite());
		//log.debug("giphies#:" + gifUrls);
		search.setResults(gifUrls);
		return "results";
	}

	@GetMapping("/results")
	String searchResultsForm(Model model) {
		model.addAttribute("favorite", new Favorite());
		return "results";
	}

	private static final String DUMMY_FAVS_JSON = "{\"url\":\"https://media0.giphy.com/media/nxG4i6YuQnHzi/giphy.gif?cid=98fb8510ce6813bdb441ef8f818e55d068c61421c8b32ea7&rid=giphy.gif\",\"category\":\"sports\"}";
	private static Favorite dummyFavorite;
	static {
		try {
			dummyFavorite = mapper.readValue(DUMMY_FAVS_JSON, new TypeReference<Favorite>(){});
		}
		catch (Exception e) {
			System.err.println("error deparsing JSON");
		}

	}

	@PostMapping("/add")
	String addFavorite(@ModelAttribute Favorite favorite, Model model, Principal user) {
		//TODO: get the user; add this favorite to the list
		log.debug("add:" + favorite.toString());
		log.debug("user:" + user);
		User guser = userService.findByEmail(user.getName());
		guser.setFavorites(addFavorite(guser.getFavorites(), favorite));
		userService.save(guser);
		dummyFavorite = favorite;
		return "redirect:favorites";
	}

	private List<Favorite> getFavorites(String favoritesJson) {
		List<Favorite> favorites = new ArrayList<>();
		if (favoritesJson != null) {
			try {
				favorites = mapper.readValue(favoritesJson, new TypeReference<List<Favorite>>(){});
			}
			catch (IOException jpe) {
				log.warn("error deserializing favorites " + favoritesJson);
				favorites = new ArrayList<>();
			}
		}
		return favorites;
	}

	private String addFavorite(String favoritesJson, Favorite favorate) {
		List<Favorite> favorites = getFavorites(favoritesJson);
		favorites.add(favorate);
		try {
			favoritesJson = mapper.writeValueAsString(favorites);
		}
		catch (IOException jpe) {
			log.warn("error deserializing favorites " + favoritesJson);
		}
		return favoritesJson;
	}

	@GetMapping("/favorites")
	String favorites(Model model, Principal user) {
		User guser = userService.findByEmail(user.getName());
		List<Favorite> favorites = getFavorites(guser.getFavorites());
		model.addAttribute("favorites", favorites);
		return "favorites";
	}

	@ModelAttribute("categoryValues")
	public String[] getMultiCheckboxAllValues() {
		return new String[] {"-select category-", "Humor", "Sports", "Entertainment", "Political", "Health", "Home", "Work"};
	}

	/*DDL
	 *  
	 CREATE TABLE giphyuser
	 (
     id SERIAL PRIMARY KEY NOT NULL,
     first varchar(100),
     last varchar (100),
     email varchar (100) NOT NULL,
     password varchar (64) NOT NULL,
     company varchar (150),
     city varchar(100),
     favorites varchar (1000)
	);
	
	CREATE TABLE users_roles
	(
	user_id SERIAL NOT NULL,
	role_id SERIAL NOT NULL
	);
	
	CREATE TABLE role
	(
     id SERIAL PRIMARY KEY NOT NULL,
     name varchar(100)
    );
     
	insert into giphyuser(first, last, email, password, company, city) values ('doug', 'baron', 'dbaronster@gmail.com', '******', 'dnb', 'austin');
	*/

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
