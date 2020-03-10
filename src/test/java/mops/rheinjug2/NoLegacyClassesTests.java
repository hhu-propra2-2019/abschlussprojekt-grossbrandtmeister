package mops.rheinjug2;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

@AnalyzeClasses(packages = "mops.rheinjug2")
@DisplayName("We should not use legacy classes")
public class NoLegacyClassesTests {
  @ArchTest
  public ArchRule noLegacyClasses =
      noClasses()
          .should().accessClassesThat()
          .haveFullyQualifiedName(java.util.Date.class.getName())
          .orShould().accessClassesThat()
          .haveFullyQualifiedName(java.util.Calendar.class.getName())
          .orShould().accessClassesThat()
          .haveFullyQualifiedName(java.util.TimeZone.class.getName());
}
