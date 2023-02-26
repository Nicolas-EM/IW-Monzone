package es.ucm.fdi.iw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *  Site administration.
 *
 *  Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("admin")
public class AdminController {

	//private static final Logger log = LogManager.getLogger(AdminController.class);

	@GetMapping("/")
    public String index(Model model) {
        return "login"; // FALTA IMPLEMENTAR
    }
    
}
