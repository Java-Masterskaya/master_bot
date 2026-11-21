package ru.yandex.masterbot;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AnalyzeClasses(
    packages = "ru.yandex.masterbot",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

  @SuppressWarnings("unused")
  @ArchTest
  static final ArchRule domain_and_application_should_not_depend_on_adapter =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..", "..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..adapter..")
          .because("Ядро (domain и application) не должно зависеть от адаптеров")
          .allowEmptyShould(true);

  @SuppressWarnings("unused")
  @ArchTest
  static final ArchRule domain_should_not_depend_on_spring =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework..")
          .because("Доменный слой должен быть свободен от Spring")
          .allowEmptyShould(true);
}
