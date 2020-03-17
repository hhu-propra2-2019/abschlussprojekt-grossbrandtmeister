package mops.rheinjug2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AccountCreatorTests {

  @Mock
  private static KeycloakAuthenticationToken keycloakAuthenticationToken;

  @Mock
  private static OidcKeycloakAccount keycloakAccount;

  @Mock
  private static KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal;

  @Mock
  private static KeycloakSecurityContext keycloakSecurityContext;

  @Mock
  private static AccessToken accessToken;

  private static final Set<String> roles = new HashSet<>();

  /**
   * Setze ein KeycloakAuthenticationToken mit Mock-Daten auf.
   */
  @BeforeEach
  public void setUp() {
    roles.add("tester");

    MockitoAnnotations.initMocks(this);

    when(keycloakAuthenticationToken.getPrincipal()).thenReturn(keycloakPrincipal);
    when(keycloakAuthenticationToken.getAccount()).thenReturn(keycloakAccount);
    when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
    when(keycloakPrincipal.getName()).thenReturn("login");
    when(keycloakAccount.getRoles()).thenReturn(roles);
  }

  /**
   * Setze ein IDToken fest aus dem dann die E-Mail gelesen werden kann.
   * Erwarte einen Account mit korrektem Namen, E-Mail und Rollen.
   */
  @Test
  public void createAccountWithIdToken() {
    when(keycloakSecurityContext.getToken()).thenReturn(accessToken);
    when(accessToken.getEmail()).thenReturn("test@e.mail");
    when(accessToken.getGivenName()).thenReturn("Maria");
    when(accessToken.getFamilyName()).thenReturn("Musterfrau");

    Account account = AccountCreator.createAccountFromPrincipal(keycloakAuthenticationToken);

    assertEquals("login", account.getName());
    assertEquals("test@e.mail", account.getEmail());
    assertNull(account.getImage());
    assertEquals(roles, account.getRoles());
    assertEquals("Maria", account.getGivenName());
    assertEquals("Musterfrau", account.getFamilyName());
  }
}
