package com.example.registration;

import com.example.registration.model.User;
import com.example.registration.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    @Test
    void registerPageIsPublic() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void homeRequiresLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void registerThenLogin() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("username", "alice")
                        .param("email", "Alice@Example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("registered", true));

        User saved = userRepository.findByUsername("alice").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();

        mvc.perform(formLogin().user("alice").password("secret123"))
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("alice"));
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("username", "a!")
                        .param("email", "not-an-email")
                        .param("password", "short")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form",
                        "username", "email", "password", "passwordConfirmed"));
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void rejectsDuplicateUsernameAndEmail() throws Exception {
        User existing = new User();
        existing.setUsername("bob");
        existing.setEmail("bob@example.com");
        existing.setPassword(passwordEncoder.encode("secret123"));
        userRepository.save(existing);

        mvc.perform(post("/register").with(csrf())
                        .param("username", "bob")
                        .param("email", "BOB@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("form", "username", "duplicate"))
                .andExpect(model().attributeHasFieldErrorCode("form", "email", "duplicate"));
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
