package mops.rheinjug2;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
import static com.tngtech.archunit.library.GeneralCodingRules.USE_JODATIME;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "mops.rheinjug2")
public class CodingRulesTest {
  @ArchTest
  private final ArchRule no_access_to_standard_streams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

  @ArchTest
  private final ArchRule no_java_util_logging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

  public static final ArchCondition<JavaClass> USE_UTILTIME =
      dependOnClassesThat(
          resideInAPackage("java.util")
              .and(simpleName("Date")
                  .or(simpleName("Calendar")
                      .or(simpleName("TimeZone"))))
      ).as("use legacy time classes");

  @ArchTest
  static final ArchRule no_legacy_time_classes = noClasses()
      .should(USE_UTILTIME)
      .orShould(USE_JODATIME)
      .because("modern Java projects use the [java.time] API instead");
}
