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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.MemberID;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Notification.NotificationType;
import es.ucm.fdi.iw.model.UserNotification;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.model.Group.Currency;
import es.ucm.fdi.iw.model.Member.GroupRole;
import es.ucm.fdi.iw.model.DebtCalculator;

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

    @Autowired
	private SimpMessagingTemplate messagingTemplate;

    private static final Logger log = LogManager.getLogger(AdminController.class);

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request") // 400
    public static class BadRequestException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You don't belong to this group") // 403
    public static class NoMemberException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You're not the moderator of this group") // 403
    public static class NoModeratorException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The expense does not exist or has been removed") // 403
    public static class ExpenseNotExistException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The expense does not belong to this group") // 403
    public static class ExpenseNotBelongException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You cannot leave a group if your balance is not 0.") // 403
    public static class BalanceNotZero extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error with DB") // 500
    public static class BadDataInDB extends RuntimeException {
    }

    /*
     * 
     * GET MAPPINGS
     * 
     */

    /*
     * View: new group
     */
    @GetMapping("/new")
    public String newGroup(HttpSession session, Model model) {

        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
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
        List<Expense> expenses = entityManager.createNamedQuery("Participates.getUniqueExpensesByGroup", Expense.class).setParameter("groupId", groupId).getResultList();

        // get debts
        DebtCalculator dc = new DebtCalculator(group.getMembers());
        List<DebtCalculator.Tuple> debts = dc.calculateDebts();

        model.addAttribute("expenses", expenses);
        model.addAttribute("debts", debts);
        model.addAttribute("groupId", groupId);
        model.addAttribute("group", group);
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

        // Get budget
        model.addAttribute("budget", member.getBudget());

        // get members
        List<Member> members = group.getMembers();

        // get total budget
        float totalBudget = 0;
        for (Member m : members) {
            totalBudget += m.getBudget();
        }

        // get currencies
        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }

        model.addAttribute("group", group);
        model.addAttribute("userId", user.getId());
        model.addAttribute("isGroupAdmin", member.getRole() == GroupRole.GROUP_MODERATOR);
        model.addAttribute("members", members);
        model.addAttribute("currencies", currencies);
        model.addAttribute("totalBudget", totalBudget);
        return "group_config";

    }

    /*
     * 
     * POST MAPPINGS
     * 
     */

    /*
     * Creates group
     */
    @Transactional
    @PostMapping("/newGroup")
    public String newGroup(HttpSession session, @RequestParam(required = true) String name,
            @RequestParam(required = false) String desc, @RequestParam(required = true) Integer currId,
            @RequestParam(required = true) Float budget) {

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
        Member m = new Member(new MemberID(g.getId(), u.getId()), true, GroupRole.GROUP_MODERATOR, budget, 0, g,
                u);
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
    public String updateGroup(HttpSession session, @PathVariable long groupId,
            @RequestParam(required = true) String name, @RequestParam(required = false) String desc, @RequestParam float budget,
            @RequestParam(required = true) Integer currId) {

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
        if (member.getRole() == GroupRole.GROUP_MODERATOR) {
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
        }
        
        // Anyone can update their budget
        // update member
        member.setBudget(budget);

        // TODO cambiar a AJAX
        return "redirect:/group/{groupId}";
    }

    /*
     * Delete group
     */
    @Transactional
    @PostMapping("{groupId}/delGroup")
    public String delGroup(HttpSession session, @PathVariable long groupId) {

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
            m.setEnabled(false);
            ;
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
    public String removeMember(@PathVariable long groupId, Model model, HttpSession session,
            @RequestParam(required = true) long removeId) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group
        MemberID mId = new MemberID(groupId, user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) {
            throw new NoMemberException();
        }

        // only moderators can remove other members
        if (user.getId() != removeId && member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new NoModeratorException();
        }

        // if only member, delete group
        if(group.getMembers().size() == 1){
            delGroup(session, groupId);
            return "redirect:/user/";
        }

        /*
         *  moderator can't leave the group if there are no other admins
         */
        if (user.getId() == removeId && member.getRole() == GroupRole.GROUP_MODERATOR) {
            List<Member> moderators = entityManager.createNamedQuery("Member.getGroupAdmins",Member.class).setParameter("groupId", groupId).getResultList();
            if(moderators.size() == 1)
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

    /**
     * Invites a user to a group
     */
    @PostMapping("/{id}/inviteMember")
    @Transactional
    @ResponseBody
    public String inviteMember(@PathVariable long id, @RequestBody JsonNode o, HttpSession session) throws JsonProcessingException  {
        
        String username = o.get("username").asText();
        User sender = (User) session.getAttribute("u");
        sender = entityManager.find(User.class, sender.getId());
        
        // check if group exists
        Group group = entityManager.find(Group.class, id);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if sender belongs to the group
        MemberID mId = new MemberID(group.getId(), sender.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) 
            throw new NoMemberException();

        // only moderators can invite new members
        if (member.getRole() != GroupRole.GROUP_MODERATOR) 
            throw new NoModeratorException();

        // Check invited user
        List<User> userList = entityManager.createNamedQuery("User.byUsername", User.class).setParameter("username", username).getResultList();

        if (userList.isEmpty())
            throw new BadRequestException();

        if (userList.size() > 1)
            throw new BadDataInDB();
        
        User user = userList.get(0);
        mId = new MemberID(group.getId(), user.getId());
        member = entityManager.find(Member.class, mId);
        // check user not already member
        if (member == null) {
            // user is not already in the group, invite
            Notification invite = new UserNotification(NotificationType.GROUP_INVITATION, user, group, sender);
            entityManager.persist(invite);
            entityManager.flush();
            log.info("User {} invited to group {}", username, group.getName());
            return "{\"status\":\"invited\"}";
        } else {
            // user is already in a group
            log.info("User {} cannot join group {}", member.getUser().getUsername(), group.getName());
            return "{\"status\":\"already_in_group\"}";
        }
        
    }

    @PostMapping("/{id}/notify")
    @Transactional
    @ResponseBody
    public String sendGroupNotif(@PathVariable long id, @RequestBody JsonNode o) throws JsonProcessingException {
        Notification notif = new Notification();

        ObjectMapper mapper = new ObjectMapper();
        String jsonNotif = mapper.writeValueAsString(notif.toTransfer());

        messagingTemplate.convertAndSend("/group/"+ id +"/queue/notifications", jsonNotif);
        return "{\"result\": \"ok\"}";
    }

    /*
     * TODO: Accept invite
     */

    /*
     * TODO: Edit member
     */

}