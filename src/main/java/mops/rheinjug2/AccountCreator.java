package mops.rheinjug2;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

public class AccountCreator {

  /**
   * Nimmt das Authentifizierungstoken von Keycloak und erzeugt ein AccountDTO für die Views.
   * IdToken wird geprüft, da nicht vorhanden bei Aufruf über Auth Header.
   *
   * @param token Keycloak Authentifizierungstoken
   * @return neuen Account der im Template verwendet wird
   */
  public static Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    var accessToken = principal.getKeycloakSecurityContext().getToken();

    return new Account(
        principal.getName(),
        accessToken.getEmail(),
        accessToken.getPicture(),
        token.getAccount().getRoles(),
        accessToken.getGivenName(),
        accessToken.getFamilyName());
  }
}
