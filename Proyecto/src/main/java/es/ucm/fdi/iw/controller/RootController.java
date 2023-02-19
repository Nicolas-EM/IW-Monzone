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

    @GetMapping("/")
    public String signup(Model model) {
        return "signup";
    }

    @GetMapping("/group")
    public String group(Model model) {
        return "group";
    }

    @GetMapping("/user")
    public String user(Model model) {
        return "user";
    }

    @GetMapping("/home")
    public String home(Model model) {
        return "home";
    }

}
