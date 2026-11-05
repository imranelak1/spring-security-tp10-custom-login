package ma.fstg.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomLoginSecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void laPageLoginPersonnaliseeEstPublique() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Authentification")));
    }

    @Test
    void accesSansAuthentificationRedirigeVersLogin() throws Exception {
        mvc.perform(get("/user/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void connexionReussieRedirigeVersHome() throws Exception {
        mvc.perform(formLogin("/authenticate").user("user").password("1111"))
                .andExpect(authenticated().withUsername("user").withRoles("USER"))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void connexionAdminReussie() throws Exception {
        mvc.perform(formLogin("/authenticate").user("admin").password("1234"))
                .andExpect(authenticated().withUsername("admin").withRoles("ADMIN"))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void mauvaisMotDePasseRedirigeVersLoginErreur() throws Exception {
        mvc.perform(formLogin("/authenticate").user("user").password("mauvais"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void deconnexionRedirigeVersLoginLogout() throws Exception {
        mvc.perform(logout())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    void utilisateurNePeutPasAccederEspaceAdmin() throws Exception {
        mvc.perform(get("/admin/dashboard").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administrateurAccedeAEspaceAdmin() throws Exception {
        mvc.perform(get("/admin/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Espace Administrateur")));
    }

    @Test
    void utilisateurAccedeAEspaceUtilisateur() throws Exception {
        mvc.perform(get("/user/dashboard").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Espace Utilisateur")));
    }

    @Test
    void administrateurAccedeAussiAEspaceUtilisateur() throws Exception {
        mvc.perform(get("/user/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void pageHomeAccessibleUneFoisAuthentifie() throws Exception {
        mvc.perform(get("/home").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("application sécurisée")));
    }
}
