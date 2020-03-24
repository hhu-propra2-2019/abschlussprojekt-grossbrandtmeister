package mops.rheinjug2;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.util.Set;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


public class KeycloakTokenMock {

  public static void setupTokenMock(final Account account) {
    final String userName = account.getName();
    final String userEmail = account.getEmail();
    final Set<String> roles = account.getRoles();
    final KeycloakPrincipal principal = mock(KeycloakPrincipal.class, RETURNS_DEEP_STUBS);
    when(principal.getName()).thenReturn(userName);
    when(principal.getKeycloakSecurityContext().getIdToken().getEmail()).thenReturn(userEmail);
    final SimpleKeycloakAccount keyaccount = new SimpleKeycloakAccount(principal, roles,
        mock(RefreshableKeycloakSecurityContext.class));
    final KeycloakAuthenticationToken authenticationToken = new KeycloakAuthenticationToken(keyaccount, true);
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    securityContext.setAuthentication(authenticationToken);
  }

}