package com.ecommerce.auth.auth_server_testing;

import com.ecommerce.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Still does not work and not sure if it is even needed
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class OAuth2LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        var registerRequest = new RegisterRequest("oauthtestuser1", "oauthtest1@example.com", "password123", "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void whenLoginAndAuthorize_thenGetTokens() throws Exception {
        // Step 1: Perform a real form login to establish a session
        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("oauthtestuser1")
                        .password("password123"))
                .andExpect(authenticated())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Step 2: Request the authorization code
        MvcResult authResult = mockMvc.perform(get("/oauth2/authorize")
                        .session(session)
                        .param("response_type", "code")
                        .param("client_id", "frontend")
                        .param("scope", "openid profile")
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/frontend")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = authResult.getResponse().getRedirectedUrl();
        String authorizationCode = redirectedUrl.substring(redirectedUrl.indexOf("code=") + 5);

        // Step 3: Exchange the Authorization Code for Tokens
        mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "authorization_code")
                        .param("code", authorizationCode)
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/frontend")
                        .with(httpBasic("frontend", "secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists());
    }
}