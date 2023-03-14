package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.MemberID;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.model.Group.Currency;
import es.ucm.fdi.iw.model.Member.GroupRole;
import es.ucm.fdi.iw.model.Debt;

/**
 * Group (and expenses) management.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("group")
public class GroupController {

    // private static final Logger log =
    // LogManager.getLogger(GroupController.class);
    @Autowired
    private EntityManager entityManager;

    private static final Logger log = LogManager.getLogger(AdminController.class);

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request") // 400
    public static class BadRequestException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You don't belong to this group") // 403
    public static class NoMemberException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You're not the moderator of this group") // 403
    public static class NoModeratorException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The expense does not exist or has been removed") // 403
    public static class ExpenseNotExistException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The expense does not belong to this group") // 403
    public static class ExpenseNotBelongException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You cannot leave a group if your balance is not 0.") // 403
    public static class BalanceNotZero extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error with DB") // 500
    public static class NoTransactionException extends RuntimeException {}


    /*
     * 
     *  GET MAPPINGS
     * 
    */

    /*
     * View: new group
     */
    @GetMapping("/new")
    public String newGroup(HttpSession session, Model model){
        
        List<String> currencies = new ArrayList<>();
        for(Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }
        model.addAttribute("currencies", currencies);

        return "group_config";

    }

    /*
     * View: group home page
     */
    @GetMapping("{groupId}")
    public String index(@PathVariable long groupId, Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new NoMemberException();
        }

        // get expenses
        List<Expense> expenses = new ArrayList<>();
        for (Participates p : group.getOwns()) {
            Expense e = p.getExpense();
            expenses.add(e);
        }

        // get balance
        List<Debt> debts = group.getDebts();

        model.addAttribute("expenses", expenses);
        model.addAttribute("debts", debts);
        model.addAttribute("groupId", groupId);
        return "group";

    }

    /*
     * View: group configuration
     */
    @GetMapping("{groupId}/config")
    public String config(@PathVariable long groupId, Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new NoMemberException();
        }        

        // get members
        List<Member> members = group.getMembers();
        
        // get currencies
        List<String> currencies = new ArrayList<>();
        for(Group.Currency g : Group.Currency.values()){
            currencies.add(g.name());
        }

        model.addAttribute("group", group);
        model.addAttribute("isGroupAdmin", member.getRole() == GroupRole.GROUP_MODERATOR);
        model.addAttribute("members", members);
        model.addAttribute("currencies", currencies);        
        return "group_config";

    }


    /*
     * 
     *  POST MAPPINGS
     * 
    */

    /*
     * Creates group
     */
    @Transactional
    @PostMapping("/newGroup")
    public String newGroup(HttpSession session, @RequestParam(required = true) String name, @RequestParam(required = false) String desc, @RequestParam(required = true) Integer currId, @RequestParam(required = true) Float budget) {       
        
        User u = (User) session.getAttribute("u");
        u = entityManager.find(User.class, u.getId());       

        // parse budget
        if (budget < 0)
            throw new BadRequestException();

        // parse curr        
        if (currId < 0 || currId >= Currency.values().length)
            throw new BadRequestException();
        Currency curr = Currency.values()[currId];

        // create group
        if (desc == null)
            desc = "";
        Group g = new Group(name, desc, curr);
        entityManager.persist(g);
        entityManager.flush(); // forces DB to add group & assign valid id

        log.warn("ID de grupo creado es {}", g.getId());

        // create member
        Member m = new Member(new MemberID(g.getId(), u.getId()), true, GroupRole.GROUP_MODERATOR, budget, u.getId(), g, u);
        entityManager.persist(m);
        entityManager.flush(); // forces DB to add group & assign valid id
        // add member to the group
        g.getMembers().add(m);
        u.getMemberOf().add(m);
        // update group
        g.setNumMembers(1);
        g.setTotBudget(budget);       

        return "redirect:/user/";
        
    }

    /*
     * Updates group
     */
    @Transactional
    @PostMapping("{groupId}/updateGroup")
    public String updateGroup(HttpSession session, @PathVariable long groupId, @RequestParam(required = true) String name, @RequestParam(required = false) String desc, @RequestParam(required = true) Integer currId) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group  
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) {
            throw new NoMemberException();
        }

        // only moderators can edit group settings
        if (member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new NoModeratorException();
        }

        // parse curr        
        if (currId < 0 || currId >= Currency.values().length)
            throw new BadRequestException();
        Currency curr = Currency.values()[currId];

        // update group
        if (desc == null)
            desc = "";
        group.setDesc(desc);
        group.setName(name);
        group.setCurrency(curr);        

        return "redirect:/group/{groupId}";

    }

    /*
     * Delete group
     */
    @Transactional
    @PostMapping("{groupId}/delGroup")
    public String delGroup(HttpSession session, @PathVariable long groupId){       
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());
        
        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group  
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) {
            throw new NoMemberException();
        }

        // only moderators can delete group
        if (member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new NoModeratorException();
        }
        
        // check all balances = 0 and remove member from the group
        List<Member> members = group.getMembers();
        for (Member m : members) {
            if (m.getBalance() != 0)
                throw new BadRequestException();
            m.setEnabled(false);;
        }

        // disable expenses
        List<Participates> owns = group.getOwns();
        for (Participates o : owns) {
            Expense e = o.getExpense();
            e.setEnabled(false);
        }

        // disable group
        group.setEnabled(false);        

        return "redirect:/user/";

    }

    /*
     * Remove member
     */
    @Transactional
    @PostMapping("{groupId}/delMember")
    public String removeMember(@PathVariable long groupId, Model model, HttpSession session, @RequestParam(required = true) long removeId) {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());
        
        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group  
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) {
            throw new NoMemberException();
        }

        // only moderators can remove other members
        if (user.getId() != removeId && member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new NoModeratorException();
        }

        // moderator can't leave the group
        if (user.getId() == removeId && member.getRole() == GroupRole.GROUP_MODERATOR) {
            throw new BadRequestException();
        }

        // balance must be 0
        if (member.getBalance() != 0) {
            throw new BadRequestException();
        }

        // remove member
        member.setEnabled(false);
        // update group budget
        group.setNumMembers(group.getNumMembers() - 1);
        group.setTotBudget(group.getTotBudget() - member.getBudget());

        if (user.getId() == removeId) 
            return "redirect:/user/";
        else // TODO: CAMBIAR A AJAX
            return config(groupId, model, session);
    }

    /*
     * TODO: Invite member
     */
    @Transactional
    @PostMapping("{id}/inviteMember")
    public String inviteMember(@PathVariable long id, Model model, HttpSession session, @RequestParam(required = true) long userId) {
        User requestingUser = (User) session.getAttribute("u");
        requestingUser = entityManager.find(User.class, requestingUser.getId());
        
        // check if group exists
        Group group = entityManager.find(Group.class, id);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        
        Member m = entityManager.find(Member.class, new MemberID(id, requestingUser.getId()));
        // check if user belongs to the group    
        if (m == null || !m.isEnabled()) {
            throw new NoMemberException();
        }

        // only moderators can add other members
        if (m.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new NoModeratorException();
        }

        // check if user to add exists
        User u = entityManager.find(User.class, userId);
        if (u == null || !u.isEnabled()) {
            throw new BadRequestException();
        }

        // // check if user didn't belong to the group
        // m = entityManager.find(Member.class, new MemberID(id, userId));
        // if (m != null && !m.isEnabled()){
        //     // m.setEnabled(true);
        //     // update group budget
        //     // group.setTotBudget(group.getTotBudget() + m.getBudget());
        // }
        // else if (m == null) {
        //     // m = new Member(new MemberID(group.getId(), userId), true, GroupRole.GROUP_USER, 0, userId, group, u);
        //     // entityManager.persist(m);
        //     // update group budget
        //     // group.setTotBudget(group.getTotBudget() + 0);
        // }
        // else { // User already belongs to the group
        //     return "error"; // CHECK: Devolver error, mostrar dialog..
        // }

        entityManager.flush();
        List<Member> members = group.getMembers();
        model.addAttribute("members", members);
        return config(id, model, session);
    }

    /*
     * TODO: Accept invite
     */

     /*
     * TODO: Edit member
     */
    
}