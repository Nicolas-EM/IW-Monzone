package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import es.ucm.fdi.iw.model.Debt;
import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.MemberID;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Notification.NotificationType;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.model.Group.Currency;
import es.ucm.fdi.iw.model.Member.GroupRole;
import es.ucm.fdi.iw.NotificationSender;
import es.ucm.fdi.iw.model.Transferable;

import es.ucm.fdi.iw.exception.*;

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
	private NotificationSender notifSender;

    private static final Logger log = LogManager.getLogger(GroupController.class);

    /*
     * 
     * GET MAPPINGS
     * 
     */

    /*
     * View: new group
     */
    @GetMapping("/new")
    public String newGroupView(HttpSession session, Model model) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // get currencies
        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }

        model.addAttribute("currencies", currencies);
        model.addAttribute("group", null);
        model.addAttribute("userId", user.getId());
        model.addAttribute("budget", 0);
        model.addAttribute("isGroupAdmin", true);

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
            throw new ForbiddenException(-1);

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new ForbiddenException(-1);
        }

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
            throw new ForbiddenException(-1);

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new ForbiddenException(-1);
        }

        // Get budget
        model.addAttribute("budget", member.getBudget());

        // get members
        List<Member> members = group.getMembers();

        // get currencies
        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }
        
        model.addAttribute("currencies", currencies);
        model.addAttribute("group", group);
        model.addAttribute("userId", user.getId());
        model.addAttribute("isGroupAdmin", member.getRole() == GroupRole.GROUP_MODERATOR);
        model.addAttribute("members", members);

        return "group_config";

    }

    /*
     * Get groupconfig
     */
    @ResponseBody
    @GetMapping("{groupId}/getGroupConfig")
    public Group.Transfer getGroupConfig(@PathVariable long groupId, HttpSession session) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(-1);

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new ForbiddenException(-1);
        }

        return group.toTransfer();
    }

    /*
     * Get members
     */
    @ResponseBody
    @GetMapping("{groupId}/getMembers")
    public List<Member.Transfer> getMembers(@PathVariable long groupId, HttpSession session){
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(-1);

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new ForbiddenException(-1);
        }

        // get members
        List<Member> members = group.getMembers();
        
        return members.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * Get and calculate debts
     */
    @ResponseBody
    @GetMapping("{groupId}/getDebts")
    public List<Debt.Transfer> getDebts(@PathVariable long groupId, HttpSession session){
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(-1);

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new ForbiddenException(-1);
        }

        // get debts
        List<Debt> debts = group.getDebts();
        
        return debts.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * 
     * POST MAPPINGS
     * 
     */

    @Async
    private CompletableFuture<Void> createAndSendNotifs(NotificationType type, User sender, Group group) {
        
        for (Member m : group.getMembers()) {
            // Do not send notification to sender
            if (m.getUser().getId() == sender.getId() || !m.isEnabled())
                continue;

            Notification notif = new Notification(type, sender, m.getUser(), group);
            entityManager.persist(notif);
            entityManager.flush();

            // Send notification
            notifSender.sendNotification(notif, "/user/" + m.getUser().getUsername() + "/queue/notifications");
        }

        return CompletableFuture.completedFuture(null);

    }

    /*
     * Creates group
     */
    @ResponseBody
    @Transactional
    @PostMapping("/newGroup")
    public String newGroup(HttpSession session, @RequestBody JsonNode jsonNode) {

        User u = (User) session.getAttribute("u");
        u = entityManager.find(User.class, u.getId());

        String name = jsonNode.get("name").asText();
        String desc = jsonNode.get("desc").asText();
        Float budget = Float.parseFloat(jsonNode.get("budget").asText());
        Integer currId = jsonNode.get("currId").asInt();       

        // Check group name
        if(name == "")
            throw new BadRequestException(-6);

        // parse budget
        if (budget < 0)
            throw new BadRequestException(-7);

        // parse curr
        if (currId < 0 || currId >= Currency.values().length)
            throw new BadRequestException(-20);
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

        return "{\"action\": \"redirect\",\"redirect\": \"/user/\"}";

    }

    /*
     * Updates group
     */
    @ResponseBody
    @Transactional
    @PostMapping("{groupId}/updateGroup")
    public String updateGroup(HttpSession session, @PathVariable long groupId, @RequestBody JsonNode jsonNode) {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(-1);

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) {
            throw new ForbiddenException(-1);
        }

        String name = jsonNode.get("name").asText();
        String desc = jsonNode.get("desc").asText();
        Float budget = Float.parseFloat(jsonNode.get("budget").asText());
        Integer currId = jsonNode.get("currId").asInt();        

        // only moderators can edit group settings
        if (member.getRole() == GroupRole.GROUP_MODERATOR) {
            // check name
            if(name == "")
                throw new BadRequestException(-6);

            // parse curr
            if (currId < 0 || currId >= Currency.values().length)
                throw new BadRequestException(-20);
            Currency curr = Currency.values()[currId];

            // update group
            if (desc == null)
                desc = "";
            group.setDesc(desc);
            group.setName(name);
            group.setCurrency(curr);
        }
        
        // check member budget
        if (budget < 0)
            throw new BadRequestException(-7);

        group.setTotBudget(group.getTotBudget() - member.getBudget());
        member.setBudget(budget);
        group.setTotBudget(group.getTotBudget() + budget);

        // Send notif
        createAndSendNotifs(NotificationType.GROUP_MODIFIED, user, group);

        // send group to other members
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_MODIFIED);

        return "{\"action\": \"none\"}";
    }

    /*
     * Delete group
     */
    @ResponseBody
    @Transactional
    @PostMapping("{groupId}/delGroup")
    public String delGroup(HttpSession session, @PathVariable long groupId) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(-1);

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) {
            throw new ForbiddenException(-1);
        }

        // only moderators can delete group
        if (member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new ForbiddenException(-2);
        }

        // check all balances = 0 and remove member from the group
        List<Member> members = group.getMembers();
        for (Member m : members) {
            if (m.getBalance() != 0)
                throw new BadRequestException(-10);
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

        // send notif
        createAndSendNotifs(NotificationType.GROUP_DELETED, user, group);
        
        // send group to other members
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_DELETED);

        return "{\"action\": \"redirect\",\"redirect\": \"/user/\"}";

    }

    /*
     * Remove member
     */
    @Transactional
    @ResponseBody
    @PostMapping("{groupId}/delMember")
    public String removeMember(@PathVariable long groupId, Model model, HttpSession session, @RequestBody JsonNode node) {
                
        long removeId = node.get("removeId").asLong();

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(-1);

        // check if requesting user belongs to the group
        MemberID mId = new MemberID(groupId, user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) {
            throw new ForbiddenException(-1);
        }

        // check if member to remove belongs to group (only if not leaving group)
        Member removeMember = entityManager.find(Member.class, new MemberID(groupId, removeId));
        if (removeId != user.getId()){
            if (removeMember == null || !removeMember.isEnabled()) {
                throw new ForbiddenException(-7);
            }
        }

        // only moderators can remove other members
        if (user.getId() != removeId && member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new ForbiddenException(-2);
        }

        // if only member, delete group
        if(group.getMembers().size() == 1){
            log.warn("Deleting EMPTY group {}", group);
            delGroup(session, groupId);
            return "{\"action\": \"redirect\",\"redirect\": \"/user/\"}";
        }

        /*
         *  moderator can't leave the group if there are no other admins
         */
        if (user.getId() == removeId && member.getRole() == GroupRole.GROUP_MODERATOR) {
            List<Member> moderators = entityManager.createNamedQuery("Member.getGroupAdmins",Member.class).setParameter("groupId", groupId).getResultList();
            if(moderators.size() == 1)
                throw new BadRequestException(-12);
        }

        // balance must be 0
        if (removeMember.getBalance() != 0) {
            throw new BadRequestException(-11);
        }

        // remove member
        removeMember.setEnabled(false);
        // update group budget
        group.setNumMembers(group.getNumMembers() - 1);
        group.setTotBudget(group.getTotBudget() - removeMember.getBudget());

        log.warn("Removed user from group {}", group);

        // Send notification to members
        createAndSendNotifs(NotificationType.GROUP_MEMBER_REMOVED, removeMember.getUser(), group);

        // Send group transfer to user (to render if on /user/)
        notifSender.sendTransfer(group, "/user/" + user.getUsername() + "/queue/notifications", "GROUP", NotificationType.GROUP_MEMBER_REMOVED);
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_MEMBER_REMOVED);

        if (user.getId() == removeId)
            return "{\"action\": \"redirect\",\"redirect\": \"/user/\"}";

        return "{\"action\": \"none\"}";
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
            throw new ForbiddenException(-1);

        // check if sender belongs to the group
        MemberID mId = new MemberID(group.getId(), sender.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled()) 
            throw new ForbiddenException(-1);

        // only moderators can invite new members
        if (member.getRole() != GroupRole.GROUP_MODERATOR) 
            throw new ForbiddenException(-2);

        // Check invited user
        List<User> userList = entityManager.createNamedQuery("User.byUsername", User.class).setParameter("username", username).getResultList();

        if (userList.isEmpty() || !userList.get(0).isEnabled())
            throw new BadRequestException(-4);

        if (userList.size() > 1)
            throw new InternalServerException(-1);
        
        User user = userList.get(0);
        mId = new MemberID(group.getId(), user.getId());
        member = entityManager.find(Member.class, mId);
        // check user not already member
        if (member == null || !member.isEnabled()) {
            // check user not already invited
            List<Notification> invites = entityManager.createNamedQuery("Notification.byUserAndGroup", Notification.class).setParameter("userId", user.getId()).setParameter("groupId", group.getId()).getResultList();

            if(invites.isEmpty()){
                Notification invite = new Notification(NotificationType.GROUP_INVITATION, sender, user, group);
                entityManager.persist(invite);
                entityManager.flush();

                // Send notification
                notifSender.sendNotification(invite, "/user/"+ user.getUsername() +"/queue/notifications");

                return "{\"status\":\"invited\"}";
            } else {
                throw new BadRequestException(-23);
            }
        } else {
            // user is already in a group
            log.info("User {} cannot join group {}", member.getUser().getUsername(), group.getName());
            throw new BadRequestException(-14);
        }
    }

    /**
     * Accept invite to group
     */
    @PostMapping("/{groupId}/acceptInvite")
    @Transactional
    @ResponseBody
    public String acceptInvite(@PathVariable long groupId, HttpSession session){
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if an invite for user exists
        List<Notification> invites = entityManager.createNamedQuery("Notification.byUserAndGroup", Notification.class).setParameter("userId", user.getId()).setParameter("groupId", groupId).getResultList();

        if(invites.size() < 1)
            throw new ForbiddenException(-8);

        // check if group still exists
        Group group = entityManager.find(Group.class, groupId);
        if(group == null || !group.isEnabled())
            throw new BadRequestException(-15);

        for(Notification invite : invites){
            // Check if sender is an admin in group
            User sender = invite.getSender();
            Member m = entityManager.find(Member.class, new MemberID(group.getId(), sender.getId()));
            if(m == null || m.getRole() != GroupRole.GROUP_MODERATOR){
                // notification should be deleted as it is no longer valid
                user.getNotifications().remove(invite);
                entityManager.remove(invite);

                return "{\"status\": \"expired\"}";
            }

            // notification valid, check if user is not already member
            Member newMember = entityManager.find(Member.class, new MemberID(group.getId(), user.getId()));
            if(newMember != null && newMember.isEnabled()){
                // notification should be deleted as it is no longer valid
                user.getNotifications().remove(invite);
                entityManager.remove(invite);

                return "{\"status\": \"already_in_group\"}";
            }
            else if(newMember == null){
                newMember = new Member(new MemberID(group.getId(), user.getId()), true, GroupRole.GROUP_USER, 0, 0, group, user);
                entityManager.persist(newMember);
            } else {
                newMember.setEnabled(true);
            }            
            // Update user and group
            user.getMemberOf().add(newMember);
            group.getMembers().add(newMember);
            group.setNumMembers(group.getNumMembers() + 1);

            // Delete notification
            user.getNotifications().remove(invite);
            entityManager.remove(invite);
        }

        // Send notification
        createAndSendNotifs(NotificationType.GROUP_INVITATION_ACCEPTED, user, group);

        // Send group transfer to user (to render if on /user/)
        notifSender.sendTransfer(group, "/user/" + user.getUsername() + "/queue/notifications", "GROUP", NotificationType.GROUP_INVITATION_ACCEPTED);
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_INVITATION_ACCEPTED);

        return "{\"status\": \"ok\"}";
    }

}