package es.ucm.fdi.iw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import java.util.List;

import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Transferable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 *  Site administration.
 *
 *  Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("admin")
public class AdminController {

	private static final Logger log = LogManager.getLogger(AdminController.class);

    @Autowired
	private EntityManager entityManager;
    
	@GetMapping("/")
    public String index(Model model, HttpSession session) {
        User u = (User)session.getAttribute("u");
        log.warn("Usuario {} ha accedido a admin", u.getUsername());

        return "admin";
    }

    @ResponseBody
    @Transactional
    @GetMapping("/getAllGroups")
    public List<Group.Transfer> getAllGroups(HttpSession session) {
        List<Group> groups = entityManager.createNamedQuery("Group.getAllGroups", Group.class).getResultList();
        return groups.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * Search groups
     */
    @ResponseBody
    @GetMapping("searchGroup/{groupName}")
    public List<Long> searchGroupsIds(@PathVariable String groupName){
        List<Long> ids = entityManager.createNamedQuery("Group.getGroupIdsLike", Long.class).setParameter("groupName", "%" + groupName + "%").getResultList();
        return ids;
    }

    

    /*
     * Search users
     */
    @ResponseBody
    @GetMapping("searchUser/{userName}")
    public List<Long> searchUsersIds(@PathVariable String userName){
        List<Long> ids = entityManager.createNamedQuery("User.getUserIdsLike", Long.class).setParameter("userName", "%" + userName + "%").getResultList();
        return ids;
    }

    @GetMapping("/{groupId}")
    public String index(Model model, HttpSession session, @PathVariable long groupId) {
        return "admin_group";
    }

    @ResponseBody
    @Transactional
    @GetMapping("/getAllUsers")
    public List<User.Transfer> getAllUsers(HttpSession session) {
        List<User> users = entityManager.createNamedQuery("User.getAllUsers", User.class).getResultList();
        return users.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }


}