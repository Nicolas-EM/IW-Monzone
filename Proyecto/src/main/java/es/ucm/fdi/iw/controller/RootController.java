package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *  Non-authenticated requests only.
 */
@Controller
public class RootController {

	private static final Logger log = LogManager.getLogger(RootController.class);

	@GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    // BEA
    @GetMapping("/")
    public String signup(Model model) {
        return "signup";
    }
    // BEA
    
    // SARA
    @GetMapping("/group")
    public String groupI(Model model) {
        return "group";
    }
    
}
