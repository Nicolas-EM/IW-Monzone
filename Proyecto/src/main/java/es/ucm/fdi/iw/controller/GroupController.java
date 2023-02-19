package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@RequestMapping("group")
public class GroupController {

	//private static final Logger log = LogManager.getLogger(GroupController.class);

	@GetMapping("{id}")
    public String group(Model model) {
        return "group";
    }

    @GetMapping("{id}/config")
    public String groupConfig(Model model) {
        return "group_config";
    }
}
