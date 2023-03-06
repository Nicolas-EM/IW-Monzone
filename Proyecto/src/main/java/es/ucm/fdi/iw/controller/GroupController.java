package es.ucm.fdi.iw.controller;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Comparator;

import javax.persistence.Embeddable;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.persistence.EntityTransaction;

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
import es.ucm.fdi.iw.model.MemberID;
import es.ucm.fdi.iw.model.Owns;
import es.ucm.fdi.iw.model.OwnsID;
import es.ucm.fdi.iw.model.Type;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Group.Currency;
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

    @Autowired
    private LocalData localData;

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The group doesn't exist or has been removed") // 403
    public static class GroupNotExistException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You don't belong to this group") // 403
    public static class NoMemberException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You're not the moderator of this group") // 403
    public static class NoModeratorException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The expense does not exist or has been removed") // 403
    public static class ExpenseNotExistException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The expense does not belong to this group") // 403
    public static class ExpenseNotBelongException extends RuntimeException {}

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error with DB") // 500
    public static class NoTransactionException extends RuntimeException {}

    @GetMapping("/new")
    public String newGroup(HttpSession sessionm, Model model){
        List<String> currencies = new ArrayList<>();
        for(Group.Currency g : Group.Currency.values()){
            currencies.add(g.name());
        }
        model.addAttribute("currencies", currencies);

        return "group_config";
    }

    /**
     * Creates group
     */
    @Transactional
    @PostMapping("/new")
    public String postGroup(Model model, HttpSession session, @RequestParam String name, @RequestParam(required = false) String desc, @RequestParam Integer currId) {
        Currency curr = Currency.values()[currId];
        User u = (User) session.getAttribute("u");

        if (curr != null) {
            Group g = new Group(0, true, desc, name, 1, 0, curr, new ArrayList<Member>(), new ArrayList<Owns>(), new ArrayList<Debt>());
            entityManager.persist(g);

            // TODO: añadir usuario a grupo (Crear member)
        }

        return "home";
    }

    /**
     * Group home page
     */
    @GetMapping("{id}")
    public String index(@PathVariable long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");

        // check if group exists
        Group group = entityManager.find(Group.class, id);
        if (group == null || !group.isEnabled())
            throw new GroupNotExistException();

        // check if user belongs to the group        
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        List<Expense> expenses = new ArrayList<>();
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
        
        // check if group exists
        Group group = entityManager.find(Group.class, id);
        if (group == null || !group.isEnabled())
            throw new GroupNotExistException();

        // check if user belongs to the group        
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        model.addAttribute("group", group);

        List<Member> members = new ArrayList<>();
        for (Member m : group.getMembers()) {
            if (m.isEnabled() && m.getUser().isEnabled()) 
                members.add(m);
            }
        model.addAttribute("members", members);

        List<String> currencies = new ArrayList<>();
        for(Group.Currency g : Group.Currency.values()){
            currencies.add(g.name());
        }
        model.addAttribute("currencies", currencies);
        
        return "group_config";
    }

    /**
     * Remove member
     */
    @Transactional
    @PostMapping("{id}/config")
    public String removeUser(@PathVariable long id, Model model, HttpSession session, @RequestParam(required = true) long removeId) {
        User user = (User) session.getAttribute("u");
        
        // check if group exists
        Group group = entityManager.find(Group.class, id);
        if (group == null || !group.isEnabled())
            throw new GroupNotExistException();

        // check if user belongs to the group        
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        // only moderators can remove other members
        if (user.getId() != removeId && !group.isGroupAdmin(user)) {
            throw new NoModeratorException();
        }

        List<User> members = new ArrayList<>();
        for (Member m : group.getMembers()) {
            if (m.isEnabled() && m.getUser().getId() == removeId){
                m.setEnabled(false);
            }
            else if(m.isEnabled() && m.getUser().isEnabled())
                members.add(m.getUser());
        }
        model.addAttribute("groupMembers", members);
        return config(id, model, session);
    }

    private void setExpenseAttributes(Group group, long expenseId, Model model, boolean newExpense) {
        List<User> members = new ArrayList<>();
        for (Member m : group.getMembers()) {
            members.add(m.getUser());
        }
        model.addAttribute("groupMembers", members);

        List<Type> types = entityManager.createNamedQuery("Type.getAllTypes", Type.class).getResultList();
        model.addAttribute("types", types);
        model.addAttribute("groupId", group.getId());
        model.addAttribute("newExpense", newExpense);
    }

    /**
     * View group expense
     */
    @GetMapping("{groupId}/{expenseId}")
    public String expense(@PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new GroupNotExistException();

        // check if user belongs to the group        
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        // check if expense exists
        Expense expense = entityManager.find(Expense.class, expenseId);
        if (expense == null || !expense.isEnabled())
            throw new ExpenseNotExistException();

        // check if expense belongs to the group
        if (!group.hasExpense(expense))
            throw new ExpenseNotBelongException();

        setExpenseAttributes(group, expenseId, model, false);
        model.addAttribute("expense", expense);
        return "expense";
    }

    /*
     * Edit group expense
     */
    // TODO: Review
    @PostMapping("/{groupId}/{expenseId}")
    @Transactional
    public String postEditExpense(HttpServletResponse response, @PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session, @RequestParam String name, @RequestParam(required = false) String desc, @RequestParam String dateString, @RequestParam long amount, @RequestParam long paidById, @RequestParam long typeId) throws IOException {
        // Start transaction
        User user = (User) session.getAttribute("u");
        Group group = entityManager.find(Group.class, groupId);
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        Expense e = entityManager.find(Expense.class, expenseId);
        if(e != null){
            User paidBy = entityManager.find(User.class, paidById);
            Type type = entityManager.find(Type.class, typeId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
            LocalDate date = LocalDate.parse(dateString, formatter);

            if(paidBy != null && !e.getPaidBy().equals(paidBy)){
                e.setPaidBy(paidBy);
            }
            if(type != null && !e.getType().equals(type)){
                e.setType(type);
            }
            if(desc != null && !e.getDesc().equals(desc)){
                e.setDesc(desc);
            }
            if(!e.getDate().equals(date)){
                e.setDate(date);
            }
            if(e.getAmount() != amount){
                e.setAmount(amount);
            }
        }

        // Stop transaction
        return "expense";
    }

    /**
     * View: create group expense
     */
    @GetMapping("{groupId}/new")
    public String createExpense(@PathVariable long groupId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new GroupNotExistException();

        // check if user belongs to the group        
        if (!group.isMember(user)) {
            throw new NoMemberException();
        }

        setExpenseAttributes(group, 0, model, true);
        return "expense";
    }

    /*
     * Add expense to group
     */
    // TODO: Review
    @PostMapping("/{groupId}/new")
    @Transactional
    public String postExpense(HttpServletResponse response, @PathVariable long groupId, Model model, HttpSession session, @RequestParam String name, @RequestParam(required = false) String desc, @RequestParam String dateString, @RequestParam long amount, @RequestParam long paidById, @RequestParam long typeId) throws IOException {
        // Start transaction

        User paidBy = entityManager.find(User.class, paidById);
        Group group = entityManager.find(Group.class, groupId);
        Type type = entityManager.find(Type.class, typeId);

        if(desc == null){
            desc = "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
        LocalDate date = LocalDate.parse(dateString, formatter);
        Expense e = new Expense(0, true, name, desc, amount, date, type, paidBy, new ArrayList<Owns>());

        entityManager.persist(e);

        OwnsID ownsId = new OwnsID(e.getId(), paidById);
        Owns owns = new Owns(ownsId, true, group, paidBy, e);
        entityManager.persist(owns);

        // Stop transaction
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
