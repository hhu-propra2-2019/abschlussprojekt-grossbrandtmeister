package mops.rheinjug2;

import javax.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  private final Environment env;

  SecurityConfig(@Autowired final Environment env) {
    this.env = env;
  }

  @Autowired
  public void configureGlobal(final AuthenticationManagerBuilder auth) {
    final KeycloakAuthenticationProvider keycloakAuthenticationProvider
        = keycloakAuthenticationProvider();
    keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
    auth.authenticationProvider(keycloakAuthenticationProvider);
  }

  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new RegisterSessionAuthenticationStrategy(
        new SessionRegistryImpl());
  }

  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST,
      proxyMode = ScopedProxyMode.TARGET_CLASS)
  public AccessToken getAccessToken() {
    final HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes()).getRequest();
    return ((KeycloakPrincipal) request.getUserPrincipal())
        .getKeycloakSecurityContext().getToken();
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    super.configure(http);
    http.authorizeRequests()
        .antMatchers("/h2-console/**").permitAll()
        .antMatchers("/actuator/**")
        .hasRole("monitoring")
        .anyRequest()
        .permitAll();

    if (env.acceptsProfiles(Profiles.of("dev"))) {
      //h2-console needs following
      http.csrf().disable();
      http.headers().frameOptions().disable();
    }
  }

  /**
   * Declaring this class enables us to use the Spring specific.
   * {@link org.springframework.security.access.annotation.Secured} annotation
   * or the JSR-250 Java Standard
   * {@link javax.annotation.security.RolesAllowed} annotation
   * for Role-based authorization
   */
  @Configuration
  @EnableGlobalMethodSecurity(
      prePostEnabled = true,
      securedEnabled = true,
      jsr250Enabled = true)
  public static class MethodSecurityConfig
      extends GlobalMethodSecurityConfiguration {
  }
}
