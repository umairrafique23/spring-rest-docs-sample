package com.meistermeier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meistermeier.beer.BeerApplication;
import com.meistermeier.beer.Brewery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BeerApplication.class)
@WebAppConfiguration
public class BeerRepositoryTest {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
//                        .uris()
//                            .withScheme("https://")
//                            .withHost("example.com")
//                            .withPort(443)
                )
                .build();
    }

    @Test
    public void index() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.get("/")).andExpect(status().isOk())
                .andDo(document("index-links", links(
                        linkWithRel("beerapi:beers").description("The <<beers,Beer resources>>"),
                        linkWithRel("curies").description("Curies for documentation"),
                        linkWithRel("profile").description("The ALPS profile for the service")
                        ),
                        responseFields(fieldWithPath("_links").description("<<index-links-links,Links>> to other resources"))
                ));
    }

    @Test
    public void beers() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.get("/beers")).andExpect(status().isOk())
                .andDo(document("beer-list", links(
                        linkWithRel("self").ignored(),
                        linkWithRel("profile").description("The ALPS profile for the service"),
                        linkWithRel("curies").ignored()
                        ),
                        responseFields(
                                fieldWithPath("_embedded.beerapi:beers").description("A list of <<beers, Beer resources>>"),
                                fieldWithPath("_links").description("<<beers-links,Links>> to other resources")
                        )

                ));
    }

    @Test
    public void createBeer() throws Exception {
        Map<String, Object> beer = new HashMap<>();
        beer.put("name", "Wolters Pilsener Premium");
        Brewery brewery = new Brewery();
        brewery.setName("Wolters");
        beer.put("brewery", brewery);
        mockMvc.perform(post("/beers")
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andDo(document("beer-create",
                        requestFields(
                                fieldWithPath("name").description("Name of your beer"),
                                fieldWithPath("brewery").description("Producer")
                                        .attributes(Attributes.key("constraints").value("must provide a `name` property")))));
    }

    @Test
    public void getOneBeer() throws Exception {
        // Set up a fresh beer
        Map<String, Object> beer = new HashMap<>();
        beer.put("name", "Wolters Pilsener Premium");
        beer.put("brewery", "Wolters");
        String beerLocation = mockMvc.perform(post("/beers")
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        //////////////////////

        mockMvc.perform(RestDocumentationRequestBuilders.get(beerLocation)).andExpect(status().isOk())
                .andDo(document("beer-get", links(
                        linkWithRel("self").ignored(),
                        linkWithRel("beerapi:beer").description("The <<beers, Beer resource>> itself"),
                        linkWithRel("curies").ignored()
                        ),
                        responseFields(
                                fieldWithPath("name").description("The name of the tasty fresh liquid"),
                                fieldWithPath("_links").description("<<beer-links,Links>> to other resources")
                        )

                ));
    }
    @Test
    public void removeOneBeer() throws Exception {
        // Set up a fresh beer
        Map<String, Object> beer = new HashMap<>();
        beer.put("name", "Wolters Pilsener Premium");
        beer.put("brewery", "Wolters");
        String beerLocation = mockMvc.perform(post("/beers")
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        //////////////////////

        mockMvc.perform(RestDocumentationRequestBuilders.delete(beerLocation)).andExpect(status().isNoContent())
                .andDo(document("beer-delete"));
    }

}
