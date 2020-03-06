package mops.rheinjug2.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rheinjug2")
public class Rheinjug2Controller {

  @GetMapping("/")
  public String getEvents() {
    return "index";
  }
}
