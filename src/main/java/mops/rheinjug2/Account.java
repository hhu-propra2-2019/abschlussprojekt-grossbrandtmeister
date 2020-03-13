package mops.rheinjug2;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class Account {
  private final String name;
  private final String email;
  private final String image;
  private final Set<String> roles;
  //
  private final String givenName;
  private final String familyName;
}