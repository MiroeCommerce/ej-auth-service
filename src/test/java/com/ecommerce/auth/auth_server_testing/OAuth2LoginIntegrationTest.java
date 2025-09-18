package com.ecommerce.auth.auth_server_testing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class OAuth2LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Assume a user 'testuser' with password 'password123' exists in the database.
    // You would typically create this user in a @BeforeEach method.

    @Test
    void whenLoginAndAuthorize_thenGetTokens() throws Exception {
        // Step 1: Perform a real form login to establish a session
        MvcResult loginResult = mockMvc.perform(formLogin("/login")
                        .user("testuser")
                        .password("password123"))
                .andExpect(authenticated()) // Verify login was successful
                .andReturn();

        // Get the session from the successful login
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Step 2: Now that we have a real session, request the authorization code
        Assertions.assertNotNull(session);
        MvcResult authResult = mockMvc.perform(get("/oauth2/authorize")
                        .session(session) // Use the established session
                        .param("response_type", "code")
                        .param("client_id", "frontend")
                        .param("scope", "openid profile")
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/frontend"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = authResult.getResponse().getRedirectedUrl();
        String authorizationCode = redirectedUrl.substring(redirectedUrl.indexOf("code=") + 5);

        // Step 3: Exchange the Authorization Code for Tokens
        mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "authorization_code")
                        .param("code", authorizationCode)
                        .param("redirect_uri", "http://127.0.0.1:8080/login/oauth2/code/frontend")
                        .with(httpBasic("frontend", "BrrBrrPatapim1245"))) // Use the correct secret
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.id_token").exists());
    }
}