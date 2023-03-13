package es.ucm.fdi.iw.controller;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

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
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.ParticipatesID;
import es.ucm.fdi.iw.model.Type;
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
@RequestMapping("group/{groupId}/")
public class ExpenseController {

    // private static final Logger log =
    // LogManager.getLogger(GroupController.class);
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

    private static final Logger log = LogManager.getLogger(AdminController.class);

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request") // 400
    public static class BadRequestExpection extends RuntimeException {}

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

    private void setExpenseAttributes(Group group, long expenseId, Model model, boolean newExpense) {
        List<Member> members = entityManager.createNamedQuery("Member.getByGroupId", Member.class).setParameter("groupId", group.getId()).getResultList();
        model.addAttribute("members", members);

        List<Type> types = entityManager.createNamedQuery("Type.getAllTypes", Type.class).getResultList();
        model.addAttribute("types", types);
        model.addAttribute("groupId", group.getId());
        model.addAttribute("newExpense", newExpense);
    }

    /**
     * View group expense
     */
    @GetMapping("{expenseId}")
    public String expense(@PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestExpection();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
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
    @PostMapping("{expenseId}")
    @Transactional
    public String postEditExpense(HttpServletResponse response, @PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session, @RequestParam String name, @RequestParam(required = false) String desc, @RequestParam String dateString, @RequestParam long amount, @RequestParam long paidById, @RequestParam long typeId) throws IOException {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        Group group = entityManager.find(Group.class, groupId);
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new NoMemberException();
        }

        Expense e = entityManager.find(Expense.class, expenseId);
        if(e != null){
            User paidBy = entityManager.find(User.class, paidById);
            Type type = entityManager.find(Type.class, typeId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
            LocalDate date = LocalDate.parse(dateString, formatter);

            if(!e.getName().equals(name)){
                e.setName(name);
            }
            if(desc != null && !e.getDesc().equals(desc)){
                e.setDesc(desc);
            }
            if(paidBy != null && !e.getPaidBy().equals(paidBy)){
                e.setPaidBy(paidBy);
            }
            if(type != null && !e.getType().equals(type)){
                e.setType(type);
            }
            if(!e.getDate().equals(date)){
                e.setDate(date);
            }
            if(e.getAmount() != amount){
                e.setAmount(amount);
            }
        }
        else{
            throw new BadRequestExpection();
        }

        return "redirect:/group/" + groupId + "/" + expenseId;
    }

    /**
     * View: create group expense
     */
    @GetMapping("new")
    public String createExpense(@PathVariable long groupId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestExpection();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new NoMemberException();
        }

        setExpenseAttributes(group, 0, model, true);
        return "expense";
    }

    /*
     * Add expense to group
     */
    // TODO: Review
    @PostMapping("new")
    @Transactional
    public String postExpense(HttpServletResponse response, @PathVariable long groupId, Model model, HttpSession session, @RequestParam String name, @RequestParam(required = false) String desc, @RequestParam String dateString, @RequestParam long amount, @RequestParam long paidById, @RequestParam long typeId) throws IOException {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestExpection();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new NoMemberException();
        }

        User paidBy = entityManager.find(User.class, paidById);
        if(paidBy == null)
            throw new BadRequestExpection();

        Type type = entityManager.find(Type.class, typeId);
        if(type == null)
            throw new BadRequestExpection();

        if(desc == null){
            desc = "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
        LocalDate date = LocalDate.parse(dateString, formatter);
        Expense e = new Expense(0, true, name, desc, amount, date, type, paidBy, new ArrayList<Participates>());

        entityManager.persist(e);

        ParticipatesID pId = new ParticipatesID(e.getId(), paidById);
        Participates participates = new Participates(pId, group, paidBy, e);
        entityManager.persist(participates);

        return "redirect:/group/" + groupId;
    }

    /**
     * Returns the default expense pic
     * 
     * @return
     */
    private static InputStream defaultExpensePic() {
        return new BufferedInputStream(Objects.requireNonNull(
                ExpenseController.class.getClassLoader().getResourceAsStream("static/img/add-image.png")));
    }

    /**
     * Downloads a pic for an expense
     * 
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("{expenseId}/pic")
    public StreamingResponseBody expensePic(@PathVariable long groupId, @PathVariable long expenseId, Model model)
            throws IOException {
        File f = localData.getFile("expense", String.format("%d-%d.jpg", groupId, expenseId));
        InputStream in = new BufferedInputStream(
                f.exists() ? new FileInputStream(f) : ExpenseController.defaultExpensePic());
        return os -> FileCopyUtils.copy(in, os);
    }
}
