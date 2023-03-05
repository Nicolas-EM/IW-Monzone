package es.ucm.fdi.iw.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.Owns;
import es.ucm.fdi.iw.model.Type;
import es.ucm.fdi.iw.model.User;

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
    private LocalData localData;

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You don't belong to this group") // 403
    public static class NoMemberException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You're not the moderator of this group") // 403
    public static class NoModeratorException extends RuntimeException {}

    /**
     * Group home page
     */
    @GetMapping("{id}")
    public String index(@PathVariable long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");
        // check if user belongs to the group
        Group group = entityManager.find(Group.class, id);
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        List<Expense> expenses = new ArrayList<Expense>();
        for (Owns o : group.getOwns()) {
            Expense e = o.getExpense();
            if (true)
                expenses.add(e);
        }
        model.addAttribute("expenses", expenses);
        model.addAttribute("groupId", id);
        return "group";
    }

    /**
     * Group configuration
     */
    @GetMapping("{id}/config")
    public String config(@PathVariable long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");
        Group group = entityManager.find(Group.class, id);
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        model.addAttribute("group", group);

        Set<Member> members = new HashSet<>();
        for (Member m : group.getMembers()) {
            if (m.isEnabled() && m.getUser().isEnabled()) {
                members.add(m);
            }
            
        }
        model.addAttribute("members", members);
        return "group_config";
    }

    /**
     * Remove member
     */
    @PostMapping("{id}/config")
    public String removeUser(@PathVariable long id, Model model, HttpSession session, @RequestParam(required = true) long removeId) {
        User user = (User) session.getAttribute("u");
        Group group = entityManager.find(Group.class, id);
        if (!group.isGroupAdmin(user)) {
            throw new NoModeratorException();
        }

        Set<User> members = new HashSet<>();
        for (Member m : group.getMembers()) {
            if(m.isEnabled() && m.getUser().getId() == removeId){
                m.setEnabled(false);
            }
            else if(m.isEnabled() && m.getUser().isEnabled())
                members.add(m.getUser());
        }
        model.addAttribute("groupMembers", members);
        return "group_config";
    }

    private void setExpenseAttributes(long groupId, long expenseId, Model model, Boolean newExpense) {
        Group group = entityManager.find(Group.class, groupId);
        List<User> members = new ArrayList<>();
        for (Member m : group.getMembers()) {
            members.add(m.getUser());
        }
        model.addAttribute("groupMembers", members);

        List<Type> types = entityManager.createNamedQuery("Type.getAllTypes", Type.class).getResultList();
        model.addAttribute("types", types);
        model.addAttribute("groupId", groupId);
        model.addAttribute("newExpense", newExpense);
    }

    /**
     * Edit Group expense
     */
    @GetMapping("{groupId}/{expenseId}")
    public String expense(@PathVariable long groupId, @PathVariable long expenseId, Model model) {
        setExpenseAttributes(groupId, expenseId, model, false);
        Expense expense = entityManager.find(Expense.class, expenseId);
        model.addAttribute("expense", expense);
        return "expense";
    }

    /**
     * Create Group expense
     */
    @GetMapping("{groupId}/new")
    public String createExpense(@PathVariable long groupId, Model model) {
        setExpenseAttributes(groupId, 0, model, true);
        return "expense";
    }

    /**
     * Returns the default expense pic
     * 
     * @return
     */
    private static InputStream defaultExpensePic() {
        return new BufferedInputStream(Objects.requireNonNull(
                GroupController.class.getClassLoader().getResourceAsStream("static/img/add-image.png")));
    }

    /**
     * Downloads a pic for an expense
     * 
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("{groupId}/{expenseId}/pic")
    public StreamingResponseBody expensePic(@PathVariable long groupId, @PathVariable long expenseId, Model model)
            throws IOException {
        File f = localData.getFile("expense", String.format("%d-%d.jpg", groupId, expenseId));
        InputStream in = new BufferedInputStream(
                f.exists() ? new FileInputStream(f) : GroupController.defaultExpensePic());
        return os -> FileCopyUtils.copy(in, os);
    }
}
