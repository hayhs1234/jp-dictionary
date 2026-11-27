package com.jpdictionary.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpdictionary.auth.entity.User;
import com.jpdictionary.auth.enums.Role;
import com.jpdictionary.auth.repository.UserRepository;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionaryFormRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import com.jpdictionary.dictionary.service.DictionaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.search-index.sync-on-startup=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DictionaryService dictionaryService;

    @MockitoBean
    private DictionaryEntryRepository dictionaryEntryRepository;

    @MockitoBean
    private DictionaryFormRepository dictionaryFormRepository;

    @MockitoBean
    private DictionarySearchIndexRepository dictionarySearchIndexRepository;

    @MockitoBean
    private KanjiEntryRepository kanjiEntryRepository;

    private User standardUser;
    private User adminUser;

    @BeforeEach
    void setUpUsers() {
        standardUser = User.builder()
                .id(1L)
                .username("komorebi_user")
                .email("user@komorebi.jp")
                .passwordHash(passwordEncoder.encode("User@123"))
                .displayName("Minh Anh")
                .jlptTarget("N3")
                .role(Role.USER)
                .active(true)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("komorebi_admin")
                .email("admin@komorebi.jp")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .displayName("Admin Komorebi")
                .jlptTarget("N1")
                .role(Role.ADMIN)
                .active(true)
                .build();

        when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("komorebi_user", "komorebi_user"))
                .thenReturn(Optional.of(standardUser));
        when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("user@komorebi.jp", "user@komorebi.jp"))
                .thenReturn(Optional.of(standardUser));
        when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("komorebi_admin", "komorebi_admin"))
                .thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("admin@komorebi.jp", "admin@komorebi.jp"))
                .thenReturn(Optional.of(adminUser));
        when(userRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(standardUser, adminUser));
        when(dictionaryService.getTrendingKanji()).thenReturn(List.of());
    }

    @Test
    @DisplayName("Public dictionary endpoints work without auth while /auth/me requires JWT")
    void testPublicAndProtectedBoundaries() throws Exception {
        mockMvc.perform(get("/dictionary/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Users can login by username or email and wrong credentials return 401")
    void testLoginByUsernameOrEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"komorebi_user\",\"password\":\"User@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("komorebi_user"))
                .andExpect(jsonPath("$.data.user.role").value("USER"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"user@komorebi.jp\",\"password\":\"User@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email").value("user@komorebi.jp"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"komorebi_user\",\"password\":\"WrongPass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("RBAC enforces USER vs ADMIN roles and revokes token on logout")
    void testRbacAndLogoutRevocation() throws Exception {
        String userToken = loginAndExtractToken("komorebi_user", "User@123");
        String adminToken = loginAndExtractToken("admin@komorebi.jp", "Admin@123");

        mockMvc.perform(get("/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("USER"));

        mockMvc.perform(get("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }

    private String loginAndExtractToken(String identifier, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("identifier", identifier, "password", password)
        );
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}

