package mops.rheinjug2;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rheinjug2")
public class Rheinjug2Controller {

    @GetMapping("/events")
    public String getEvents(){
        return "eventsoverview";
    }
    
    @GetMapping("/personalView")
    public String getMyEvents(){
        return "personalView";
    }
}
