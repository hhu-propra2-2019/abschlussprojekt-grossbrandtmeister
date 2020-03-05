package mops.rheinjug2;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@AnalyzeClasses(packages = "mops.rheinjug2")
class ControllerRulesTests {
  @ArchTest
  public ArchRule controllerClassesAnnotatedWithRequestMapping =
      classes().that().areAnnotatedWith(Controller.class)
          .should().beAnnotatedWith(RequestMapping.class);
}