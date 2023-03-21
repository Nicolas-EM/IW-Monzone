package es.ucm.fdi.iw.controller;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
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

/**
 * Group (and expenses) management.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("group/{groupId}")
public class ExpenseController {

    // private static final Logger log =
    // LogManager.getLogger(GroupController.class);
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

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

    private void setExpenseAttributes(Group group, long expenseId, Model model, boolean newExpense) {
        
        List<Participates> participants = new ArrayList<>();
        if (!newExpense) {
            entityManager.createNamedQuery("Participates.getParticipants", Participates.class)
                    .setParameter("groupId", group.getId())
                    .setParameter("expenseId", expenseId)
                    .getResultList();
        }
        model.addAttribute("participants", participants);
        List<Member> members = group.getMembers();
        model.addAttribute("members", members);
        List<Type> types = entityManager.createNamedQuery("Type.getAllTypes", Type.class).getResultList();
        model.addAttribute("types", types);
        model.addAttribute("groupId", group.getId());
        model.addAttribute("newExpense", newExpense);

    }

    /*
     * 
     * GET MAPPINGS
     * 
     */

     /*
     * View: create group expense
     */
    @GetMapping("/new")
    public String newExpense(@PathVariable long groupId, Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled())
            throw new NoMemberException();

        setExpenseAttributes(group, 0, model, true);
        model.addAttribute("group", group);

        return "expense";

    }

    /*
     * View: edit group expense
     */
    @GetMapping("{expenseId}")
    public String expense(@PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled()))
            throw new NoMemberException();

        // check if expense exists
        Expense expense = entityManager.find(Expense.class, expenseId);
        if (expense == null || !expense.isEnabled())
            throw new ExpenseNotExistException();

        // check if expense belongs to the group
        if (!group.hasExpense(expense))
            throw new ExpenseNotBelongException();

        setExpenseAttributes(group, expenseId, model, false);
        model.addAttribute("expense", expense);

        // Get array of participants
        List<Participates> participates = entityManager.createNamedQuery("Participates.getParticipants", Participates.class).setParameter("groupId", group.getId()).setParameter("expenseId", expenseId).getResultList();

        // Change array to participantIds
        List<Long> participateIds = new ArrayList<>();
        for(Participates p : participates){
            participateIds.add(p.getUser().getId());
        }
        
        model.addAttribute("participateIds", participateIds);
        model.addAttribute("group", group);

        return "expense";

    }

    /*
     * 
     * POST MAPPINGS
     * 
     */

     // TODO: Debería recibir lista participantes, y falta actualizar los balance
     /*
     * Add expense to group
     */   
    @PostMapping("/newExpense")
    @Transactional
    public String createExpense(@PathVariable long groupId, Model model, HttpSession session, @RequestParam String name,
            @RequestParam(required = false) String desc, @RequestParam String dateString, @RequestParam float amount,
            @RequestParam long paidById, @RequestParam List<String> participateIds, @RequestParam long typeId) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled())
            throw new NoMemberException();

        // check if user who paid exists
        User paidBy = entityManager.find(User.class, paidById);
        if (paidBy == null || !paidBy.isEnabled())
            throw new BadRequestException();
        
        // check if user who paid belongs to the group
        MemberID mpId = new MemberID(groupId, paidById);
        Member memberPaid = entityManager.find(Member.class, mpId);
        if (memberPaid == null || !memberPaid.isEnabled())
            throw new NoMemberException();

        // check if type is one of the availables
        Type type = entityManager.find(Type.class, typeId);
        if (type == null)
            throw new BadRequestException();

        // check date format
        LocalDate date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
            date = LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new BadRequestException();
        }

        // Get all participant users
        List<User> participateUsers = new ArrayList<>();
        for(String idString : participateIds){
            long pId = Long.parseLong(idString);
            User pUser = entityManager.find(User.class, pId);
            participateUsers.add(pUser);
        }

        // Check if all participants exist
        for(User u : participateUsers){
            if(u != null){
                MemberID mParticipatesId = new MemberID(groupId, paidById);
                Member participatesMember = entityManager.find(Member.class, mParticipatesId);
                if (participatesMember == null || !participatesMember.isEnabled())
                    throw new BadRequestException();
            }
            else {
                throw new BadRequestException();
            }
        }

        // create expense
        if (desc == null)
            desc = "";        
        
        Expense e = new Expense(name, desc, amount, date, type, paidBy);
        entityManager.persist(e);
        entityManager.flush(); // forces DB to add expense & assign valid id
        
        // add all participants
        for(User u : participateUsers){
            ParticipatesID pId = new ParticipatesID(e.getId(), u.getId());
            Participates participates = new Participates(pId, group, paidBy, e);
            entityManager.persist(participates);
            // add debts of balance
            MemberID memberID = new MemberID(group.getId(), u.getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() - e.getAmount() / participateUsers.size());
        }

        return "redirect:/group/" + groupId;
    }

    // TODO: Debería recibir lista participantes, y falta actualizar los balance
    /*
     * Edit group expense
     */    
    @PostMapping("{expenseId}/updateExpense")
    @Transactional
    public String editExpense(@PathVariable long groupId, @PathVariable long expenseId, Model model,
            HttpSession session, @RequestParam String name, @RequestParam(required = false) String desc,
            @RequestParam String dateString, @RequestParam float amount, @RequestParam long paidById,
            @RequestParam long typeId, @RequestParam List<String> participateIds) {
  
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled())
            throw new NoMemberException();

        // check if expense exists
        Expense exp = entityManager.find(Expense.class, expenseId);
        if (exp == null || !exp.isEnabled())
            throw new BadRequestException();
        
        // check if expense belongs to the group
        if (!group.hasExpense(exp))
            throw new BadRequestException();

        // check if user who paid exists
        User paidBy = entityManager.find(User.class, paidById);
        if (paidBy == null || !paidBy.isEnabled())
            throw new BadRequestException();
        
        // check if user who paid belongs to the group
        MemberID mpId = new MemberID(groupId, paidById);
        Member memberPaid = entityManager.find(Member.class, mpId);
        if (memberPaid == null || !memberPaid.isEnabled())
            throw new NoMemberException();

        // check if type is one of the availables
        Type type = entityManager.find(Type.class, typeId);
        if (type == null)
            throw new BadRequestException();

        // check date format
        LocalDate date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");    
            date = LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new BadRequestException();
        }

        // update expense
        exp.setName(name);
        if (desc == null)
            desc = "";
        exp.setDesc(desc);
        exp.setPaidBy(paidBy);
        exp.setType(type);
        exp.setDate(date);
        exp.setAmount(amount);       
        
        // delete debts of balances
        List<Participates> participants = exp.getBelong();
        for(Participates p : participants){
            MemberID memberID = new MemberID(group.getId(), p.getUser().getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() + exp.getAmount() / participants.size());
        }

        // Get all participant users
        List<User> participateUsers = new ArrayList<>();
        for(String idString : participateIds){
            long pId = Long.parseLong(idString);
            User pUser = entityManager.find(User.class, pId);
            participateUsers.add(pUser);
        }

        // Check if all participants exist
        for(User u : participateUsers){
            if(u != null){
                MemberID mParticipatesId = new MemberID(groupId, paidById);
                Member participatesMember = entityManager.find(Member.class, mParticipatesId);
                if (participatesMember == null || !participatesMember.isEnabled())
                    throw new BadRequestException();
            }
            else {
                throw new BadRequestException();
            }
        }

        // add all participants
        for(User u : participateUsers){
            ParticipatesID pId = new ParticipatesID(exp.getId(), u.getId());
            Participates participates = new Participates(pId, group, paidBy, exp);
            entityManager.persist(participates);
            // add debts of balance
            MemberID memberID = new MemberID(group.getId(), u.getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() - exp.getAmount() / participateUsers.size());
        }

        return "redirect:/group/" + groupId + "/" + expenseId;

    }

    // TODO: Falta actualizar los balance
    /*
     * Delete group expense
     */    
    @PostMapping("{expenseId}/delExpense")
    @Transactional
    public String deleteExpense(@PathVariable long groupId, @PathVariable long expenseId, Model model,
            HttpSession session) throws IOException {
  
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled())
            throw new NoMemberException();

        // check if expense exists
        Expense exp = entityManager.find(Expense.class, expenseId);
        if (exp == null || !exp.isEnabled())
            throw new BadRequestException();
        
        // check if expense belongs to the group
        if (!group.hasExpense(exp))
            throw new BadRequestException();

        // delete debts of balances
        List<Participates> participants = exp.getBelong();
        for(Participates p : participants){
            MemberID memberID = new MemberID(group.getId(), p.getUser().getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() + exp.getAmount() / participants.size());
        }

        // delete participants
        for (Participates p : participants)
            entityManager.remove(p);
        
        // disable expense
        exp.setEnabled(false);

        return "redirect:/group/" + groupId;

    }

    /*
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
    public StreamingResponseBody expensePic(@PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session) throws IOException {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();

        // check if user belongs to the group        
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled())
            throw new NoMemberException();


        // check if expense exists 
        // If expense == null, its a new expense
        Expense exp = entityManager.find(Expense.class, expenseId);
        if (exp != null && !exp.isEnabled())
            throw new BadRequestException();
        
        // check if expense belongs to the group
        if (exp != null && !group.hasExpense(exp))
            throw new BadRequestException();

        File f = localData.getFile("expense", String.format("%d-%d.jpg", groupId, expenseId));
        InputStream in = new BufferedInputStream(
                f.exists() ? new FileInputStream(f) : ExpenseController.defaultExpensePic());
        return os -> FileCopyUtils.copy(in, os);

    }

}
