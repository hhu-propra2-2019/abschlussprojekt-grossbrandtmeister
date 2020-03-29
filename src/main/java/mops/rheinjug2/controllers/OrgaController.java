package mops.rheinjug2.controllers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import mops.rheinjug2.Account;
import mops.rheinjug2.AccountCreator;
import mops.rheinjug2.fileupload.FileCheckService;
import mops.rheinjug2.orgamodels.DelayedSubmission;
import mops.rheinjug2.services.EventService;
import mops.rheinjug2.services.OrgaService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Log4j2
@Controller
@RequestMapping("/rheinjug2/orga")
@SessionScope
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class OrgaController {

  private final transient Counter authenticatedAccess;
  private final transient EventService eventService;
  private final transient OrgaService orgaService;
  private transient String successMessage = "";
  private transient String errorMessage = "";
  private transient int numberOfEvaluationRequests;

  /**
   * Injected unsere Services und den Counter.
   */

  public OrgaController(final MeterRegistry registry,
                        final EventService eventService, final OrgaService orgaService) {
    authenticatedAccess = registry.counter("access.authenticated");
    this.eventService = eventService;
    this.orgaService = orgaService;
    numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
  }

  @Secured({"ROLE_orga"})
  @RequestMapping(value = {"/", ""})
  public String orgaBase() {
    return "redirect:/rheinjug2/orga/events";
  }

  /**
   * Event Übersicht für Orga Mitarbeiter.
   */
  @Secured({"ROLE_orga"})
  @GetMapping("/events")
  public String getEvents(final Model model, final KeycloakAuthenticationToken token) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    authenticatedAccess.increment();
    model.addAttribute("account", account);
    model.addAttribute("events", orgaService.getEvents());
    model.addAttribute("datenow", LocalDateTime.now());
    model.addAttribute("numberOfEvaluationRequests", numberOfEvaluationRequests);
    return "orga_events_overview";
  }

  /**
   * Gibt liste aller von Studenten angemeldte Veranstaltungen,
   * die ihre Zusammenfassung noch nicht abgegeben worde.
   *
   * @param model model
   * @return liste der Veranstaltungen.
   */
  @Secured({"ROLE_orga"})
  @GetMapping("/delayedSubmission")
  public String getDelayedSubmission(final Model model,
                                     final KeycloakAuthenticationToken token) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    authenticatedAccess.increment();
    if (!model.containsAttribute("delayedsubmissions")) {
      model.addAttribute("delayedsubmissions", orgaService.getDelayedSubmission());
    }
    model.addAttribute("account", account);
    model.addAttribute("numberOfEvaluationRequests", numberOfEvaluationRequests);
    model.addAttribute("successmessage", successMessage);
    model.addAttribute("errormessage", errorMessage);
    successMessage = "";
    errorMessage = "";
    return "orga_delayed_submission";
  }

  /**
   * Mapping der Annahme von Zusammenfassungen.
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/summaryaccepting")
  public String summaryAccepting(@RequestParam final Long eventid,
                                 @RequestParam final Long studentid,
                                 final Model model,
                                 final KeycloakAuthenticationToken token) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    authenticatedAccess.increment();
    model.addAttribute("account", account);
    if (orgaService.setSummaryAsAccepted(studentid, eventid)) {
      numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
      successMessage = "Zusammenfassung wurde erfolgreich als akzeptiert gespeichert.";
      return "redirect:/rheinjug2/orga/reports";
    }
    errorMessage = "Fehler.. Zusammenfassung wurde nicht gespeichert ";
    return "redirect:/rheinjug2/orga/reports";
  }

  /**
   * Übersicht der Veranstaltung, an denen Studenten teilgenommen haben aber
   * die zusammenfassung noch nichtabgegebn haben und Abgabefrist ist vorbei.
   *
   * @return .
   * @throws IOException .
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/summaryupload")
  public String summaryUpload(@RequestParam final Long studentId,
                              @RequestParam final Long eventId,
                              @RequestParam final String studentName,
                              @RequestParam final String summaryContent,
                              @RequestParam final MultipartFile file,
                              final Model model,
                              final KeycloakAuthenticationToken token
  ) throws IOException {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    authenticatedAccess.increment();
    if (file.isEmpty() && summaryContent.isEmpty()) {
      errorMessage = "Die Zusammenfassung ist noch erforderlich für eine Abgabe.";
    } else if (!file.isEmpty()) {
      if (FileCheckService.isMarkdown(file)) {
        try {
          if (!orgaService.summaryuploadFileContent(studentId, eventId, studentName, file)) {
            errorMessage =
                "Zusammenfassung wurde nicht hochgeladen, Student oder Event nicht gefunden.";
          } else {
            successMessage = "Zusammenfassung wurde erfolgreich als akzeptiert hochgeladen.";
          }
        } catch (final RuntimeException e) {
          errorMessage = "Zusammenfassung wurde nicht gespeichert: MinIO " + e.getMessage();
        }
      } else {
        errorMessage = "Zusammenfassung bitte in Markdown (.md) Format hochladen.";
      }
    } else {
      try {
        orgaService.summaryuploadStringContent(studentId, eventId, studentName, summaryContent);
        successMessage = "Zusammenfassung wurde erfolgreich als akzeptiert hochgeladen.";
      } catch (final RuntimeException e) {
        errorMessage = "Zusammenfassung wurde nicht gespeichert: MinIO " + e.getMessage();
      }
    }
    return "redirect:/rheinjug2/orga/delayedSubmission";
  }

  /**
   * Übersicht der noch unbewerteten Abgaben.
   */
  @Secured({"ROLE_orga"})
  @GetMapping("/reports")
  public String getReports(final Model model, final KeycloakAuthenticationToken token) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    authenticatedAccess.increment();
    model.addAttribute("summaries", orgaService.getSummaries());
    model.addAttribute("successmessage", successMessage);
    model.addAttribute("errormessage", errorMessage);
    model.addAttribute("numberOfEvaluationRequests", numberOfEvaluationRequests);
    successMessage = "";
    errorMessage = "";
    return "orga_reports_overview";
  }

  /**
   * Ruft die rheinjug Events manuell ab und speichert diese in die Datenbank.
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/events")
  public String getEventsFromApi(final KeycloakAuthenticationToken token, final Model model) {
    final Account user = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", user);
    log.info("User '" + user.getName() + "' requested event refresh");
    eventService.refreshRheinjugEvents(LocalDateTime.now(ZoneId.of("Europe/Berlin")));
    return "redirect:/rheinjug2/orga/events";
  }

  /**
   * Die methode gibt der gesuchte Student zuruck.
   *
   * @param redirectAttributes .
   * @return .
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/searchstudent")
  public String searchstudent(@RequestParam(name = "searchedName") final String searchedName,
                              final RedirectAttributes redirectAttributes,
                              final KeycloakAuthenticationToken token, final Model model) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    authenticatedAccess.increment();
    final List<DelayedSubmission> delayedsubmissions =
        orgaService.getDelayedSubmissionsForStudent(searchedName);
    if (delayedsubmissions.isEmpty()) {
      errorMessage =
          "Es konnten unter diesem Namen '" + searchedName
              + "' keine verspäteten Abgaben gefunden werden.";
      return "redirect:/rheinjug2/orga/delayedSubmission";
    }
    redirectAttributes.addFlashAttribute("delayedsubmissions", delayedsubmissions);
    return "redirect:/rheinjug2/orga/delayedSubmission";
  }

  /**
   * Die Methode gibt die gesuchte Veranstaltung zuruck.
   *
   * @param redirectAttributes .
   * @return .
   */
  @Secured({"ROLE_orga"})
  @PostMapping("/searchevent")
  public String searchevent(@RequestParam final String searchedName,
                            final RedirectAttributes redirectAttributes,
                            final KeycloakAuthenticationToken token, final Model model) {
    final Account account = AccountCreator.createAccountFromPrincipal(token);
    model.addAttribute("account", account);
    authenticatedAccess.increment();
    final List<DelayedSubmission> delayedsubmissions =
        orgaService.getDelayedSubmissionsForEvent(searchedName);
    if (delayedsubmissions.isEmpty()) {
      errorMessage =
          "Es konnten unter diesem Titel '" + searchedName
              + "' keine verspäteten Abgaben gefunden werden.";
      return "redirect:/rheinjug2/orga/delayedSubmission";
    }
    redirectAttributes.addFlashAttribute("delayedsubmissions", delayedsubmissions);
    return "redirect:/rheinjug2/orga/delayedSubmission";
  }

  @Scheduled(fixedDelayString = "${application.api-pump.delay}")
  public void refreshNumberOfEvaluationRequests() {
    numberOfEvaluationRequests = orgaService.getnumberOfEvaluationRequests();
  }
}
