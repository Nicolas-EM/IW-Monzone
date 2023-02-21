package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.User;

/**
 *  Site administration.
 *
 *  Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("group")
public class GroupController {

	//private static final Logger log = LogManager.getLogger(GroupController.class);
    @Autowired
	private EntityManager entityManager;

	@GetMapping("{id}")
    public String group(@PathVariable long id, Model model) {
        return "group";
    }

    @GetMapping("{id}/config")
    public String groupConfig(@PathVariable long id, Model model) {
        Group group = entityManager.find(Group.class, id);
        List<User> members = new ArrayList<>();
        for(Member m : group.getMembers()){
            members.add(m.getUserEntity());
        }
		model.addAttribute("groupMembers", members);
        return "group_config";
    }

    @GetMapping("{id}/{expense}")
    public String groupExpense(Model model) {
        return "expense";
    }
}
