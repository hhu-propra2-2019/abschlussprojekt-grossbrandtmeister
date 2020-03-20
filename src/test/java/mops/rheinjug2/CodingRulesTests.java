package mops.rheinjug2;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beAnnotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
import static com.tngtech.archunit.library.GeneralCodingRules.USE_JODATIME;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Good practice coding rules")
public class CodingRulesTests {

  @AnalyzeClasses(packages = "mops.rheinjug2")
  public static class AllClassesRulesTests {

    @ArchTest
    private final ArchRule noAccessToStandardStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    private final ArchRule noJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @SuppressWarnings({"checkstyle:abbreviationaswordinname", "checkstyle:membername"})
    private final transient ArchCondition<JavaClass> USE_UTILTIME =
        dependOnClassesThat(
            resideInAPackage("java.util")
                .and(simpleName("Date")
                    .or(simpleName("Calendar")
                        .or(simpleName("TimeZone"))))
        ).as("use legacy time classes");

    @ArchTest
    private final ArchRule noLegacyTimeClasses = noClasses()
        .should(USE_UTILTIME)
        .orShould(USE_JODATIME)
        .because("modern Java projects use the [java.time] API instead");
  }

  @AnalyzeClasses(packages = "mops.rheinjug2",
      importOptions = {ImportOption.DoNotIncludeTests.class})
  public static class NotTestClassesRulesTests {
    //see https://github.com/rweisleder/ArchUnit/commit/8bfe5a85e32afc4aafd0f59c0823b99c81fc5ce1
    private static final ArchCondition<JavaField> USE_FIELD_INJECTION
        = useFieldInjection().as("use field injection");

    private static ArchCondition<JavaField> useFieldInjection() {
      final ArchCondition<JavaField> annotatedWithAutowired
          = beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired");
      final ArchCondition<JavaField> annotatedWithValue
          = beAnnotatedWith("org.springframework.beans.factory.annotation.Value");
      final ArchCondition<JavaField> annotatedWithInject
          = beAnnotatedWith("javax.inject.Inject");
      final ArchCondition<JavaField> annotatedWithResource
          = beAnnotatedWith("javax.annotation.Resource");
      return annotatedWithAutowired
          .or(annotatedWithValue)
          .or(annotatedWithInject)
          .or(annotatedWithResource);
    }

    @ArchTest
    private final ArchRule noFieldInjection =
        noFields()
            .should(USE_FIELD_INJECTION)
            .because("field injection is considered harmful");
  }
}
