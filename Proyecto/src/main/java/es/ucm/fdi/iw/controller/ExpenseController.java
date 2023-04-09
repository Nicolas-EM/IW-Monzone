package es.ucm.fdi.iw.controller;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.FileCopyUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.NotificationSender;
import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.MemberID;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.ParticipatesID;
import es.ucm.fdi.iw.model.Transferable;
import es.ucm.fdi.iw.model.Type;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Notification.NotificationType;
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

    @Autowired
    private NotificationSender notifSender;

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
    public static class NoTransactionException extends RuntimeException {
    }

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
     * Get all expenses
     */
    @ResponseBody
    @Transactional
    @GetMapping("/getExpenses")
    public List<Expense.Transfer> getExpenses(@PathVariable long groupId, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null)
            throw new BadRequestException();

        // check if user belongs to the group
        MemberID mId = new MemberID(group.getId(), user.getId());
        Member member = entityManager.find(Member.class, mId);
        if (!user.hasRole(Role.ADMIN) && (member == null || !member.isEnabled())) {
            throw new NoMemberException();
        }

        // get expenses
        List<Expense> expenses = entityManager.createNamedQuery("Participates.getUniqueExpensesByGroup", Expense.class).setParameter("groupId", groupId).getResultList();

        return expenses.stream().map(Transferable::toTransfer).collect(Collectors.toList());
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
        List<Participates> participates = entityManager
                .createNamedQuery("Participates.getParticipants", Participates.class)
                .setParameter("groupId", group.getId()).setParameter("expenseId", expenseId).getResultList();

        // Change array to participantIds
        List<Long> participateIds = new ArrayList<>();
        for (Participates p : participates) {
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

    class PostParams {
        public Boolean valid = false;
        public User currUser;
        public Group group;
        public User paidBy;
        public Member paidByMember;
        public Type type;
        public LocalDate date;
        public List<User> participateUsers;
        public List<Member> participateMembers;
        //public MultipartFile file;
    }

    private PostParams validatedPostParams(HttpSession session, long groupId, String dateString, float amount,
            long paidById,
            List<String> participateIds, long typeId) {

        PostParams validated = new PostParams();
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());
        validated.currUser = user;

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException();
        validated.group = group;

        // check if user belongs to the group
        MemberID requesterMemberId = new MemberID(group.getId(), user.getId());
        Member requesterMember = entityManager.find(Member.class, requesterMemberId);
        if (requesterMember == null || !requesterMember.isEnabled())
            throw new NoMemberException(); // Requester not in group

        // check if user who paid exists
        User paidBy = entityManager.find(User.class, paidById);
        if (paidBy == null || !paidBy.isEnabled())
            throw new BadRequestException();
        validated.paidBy = paidBy;

        // check if user who paid belongs to the group
        MemberID paidByMemberId = new MemberID(groupId, paidById);
        Member paidByMember = entityManager.find(Member.class, paidByMemberId);
        if (paidByMember == null || !paidByMember.isEnabled())
            throw new NoMemberException();
        validated.paidByMember = paidByMember;

        // check if type exists
        Type type = entityManager.find(Type.class, typeId);
        if (type == null)
            throw new BadRequestException();
        validated.type = type;

        // check date format
        LocalDate date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            date = LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new BadRequestException();
        }
        validated.date = date;

        // check it has participants
        if (participateIds.isEmpty())
            throw new BadRequestException();

        // check participants exist
        List<User> participateUsers = new ArrayList<>();
        for (String idString : participateIds) {
            long pId = Long.parseLong(idString);
            User pUser = entityManager.find(User.class, pId);
            if (pUser == null)
                throw new BadRequestException();
            participateUsers.add(pUser);
        }
        validated.participateUsers = participateUsers;

        // check participants are members
        List<Member> participateMembers = new ArrayList<>();
        for (User u : participateUsers) {
            MemberID participateMemberId = new MemberID(groupId, u.getId());
            Member participateMember = entityManager.find(Member.class, participateMemberId);
            if (participateMember == null || !participateMember.isEnabled())
                throw new BadRequestException();
            participateMembers.add(participateMember);
        }
        validated.participateMembers = participateMembers;

        // check amount is not negative
        if (amount <= 0)
            throw new BadRequestException();

        validated.valid = true;
        return validated;
    }

    @Async
    private CompletableFuture<Void> createAndSendNotifs(NotificationType type, User sender, List<User> users, Group group, Expense e) {
        for (User u : users) {
            // Do not send notification to sender
            if (u.getId() == sender.getId())
                continue;

            Notification notif = new Notification(type, sender, u, group, e);
            entityManager.persist(notif);
            entityManager.flush();

            // Send notification
            notifSender.sendNotification(notif, "/user/" + u.getUsername() + "/queue/notifications");
        }

        return CompletableFuture.completedFuture(null);
    }

    /*
     * Add expense to group
     */
    @PostMapping("/newExpense")
    @Transactional
    @ResponseBody
    public String createExpense(@PathVariable long groupId, Model model, HttpSession session,
            @RequestBody JsonNode jsonNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        String name = objectMapper.convertValue(jsonNode.get("name"), String.class);
        String desc = objectMapper.convertValue(jsonNode.get("desc"), String.class);
        String dateString = objectMapper.convertValue(jsonNode.get("dateString"), String.class);
        float amount = objectMapper.convertValue(jsonNode.get("amount"), Float.class);
        long paidById = objectMapper.convertValue(jsonNode.get("paidById"), Long.class);
        List<String> participateIds = objectMapper.convertValue(jsonNode.get("participateIds"),
                new TypeReference<List<String>>() {
                });
        long typeId = objectMapper.convertValue(jsonNode.get("typeId"), Long.class);

        MultipartFile file = objectMapper.convertValue(jsonNode.get("file"), MultipartFile.class);
        
        PostParams params = validatedPostParams(session, groupId, dateString, amount, paidById, participateIds, typeId);

        if (!params.valid)
            throw new BadRequestException();

        /*
         * 
         * CREATE EXPENSE
         * 
         */
        if (desc == null)
            desc = "";

        // Create expense
        Expense exp = new Expense(name, desc, amount, params.date, params.type, params.paidBy);
        entityManager.persist(exp);
        entityManager.flush();

        // add balance to paidBy
        params.paidByMember.setBalance(params.paidByMember.getBalance() + amount);

        // add all participants
        for (User u : params.participateUsers) {
            ParticipatesID pId = new ParticipatesID(exp.getId(), u.getId());
            Participates participates = new Participates(pId, params.group, u, exp);
            entityManager.persist(participates);

            // add debts of balance
            MemberID memberID = new MemberID(params.group.getId(), u.getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() - exp.getAmount() / params.participateUsers.size());
        }

        // save the new expense image
        // try{
        //     String filename = String.valueOf(e); //ver ejemplo de user controller de la plantilla
        //     File dest = localData.getFile("expense", filename); //no molestarse en ver que había antes machacar lo anterior
        //     //dest.delete();
        //    file.transferTo(dest);
        // }
        // catch(IOException ex){
        //     return "Error IMAGEN " + ex.getMessage();
        // }

        // send notification ASYNC
        createAndSendNotifs(NotificationType.EXPENSE_CREATED, params.currUser, params.participateUsers, params.group, exp);

        // send expense to group ASYNC
        notifSender.sendTransfer(exp, "/topic/group/" + params.group.getId(), "EXPENSE", NotificationType.EXPENSE_CREATED);

        return "{\"action\": \"redirect\",\"redirect\": \"/group/" + groupId + "\"}";
    }

    /*
     * Edit group expense
     */
    @PostMapping("{expenseId}/updateExpense")
    @Transactional
    @ResponseBody
    public String editExpense(@PathVariable long groupId, @PathVariable long expenseId, Model model,
            HttpSession session, @RequestBody JsonNode jsonNode) {

        ObjectMapper objectMapper = new ObjectMapper();
        String name = objectMapper.convertValue(jsonNode.get("name"), String.class);
        String desc = objectMapper.convertValue(jsonNode.get("desc"), String.class);
        String dateString = objectMapper.convertValue(jsonNode.get("dateString"), String.class);
        float amount = objectMapper.convertValue(jsonNode.get("amount"), Float.class);
        long paidById = objectMapper.convertValue(jsonNode.get("paidById"), Long.class);
        List<String> participateIds = objectMapper.convertValue(jsonNode.get("participateIds"),
                new TypeReference<List<String>>() {
                });
        long typeId = objectMapper.convertValue(jsonNode.get("typeId"), Long.class);

        PostParams params = validatedPostParams(session, groupId, dateString, amount, paidById, participateIds, typeId);

        if (!params.valid)
            throw new BadRequestException();

        // check if expense exists
        Expense exp = entityManager.find(Expense.class, expenseId);
        if (exp == null || !exp.isEnabled())
            throw new BadRequestException();

        // check if expense belongs to the group
        if (!params.group.hasExpense(exp))
            throw new BadRequestException();

        // delete debts of balances
        List<Participates> participants = exp.getBelong();
        for (Participates p : participants) {
            MemberID memberID = new MemberID(params.group.getId(), p.getUser().getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() + exp.getAmount() / participants.size());
        }

        // delete owed from balance
        MemberID originalPaidByMemberID = new MemberID(params.group.getId(), exp.getPaidBy().getId());
        Member m = entityManager.find(Member.class, originalPaidByMemberID);
        m.setBalance(m.getBalance() - exp.getAmount());

        // delete removed participants
        for (Participates p : exp.getBelong()) {
            if (!participateIds.contains(String.valueOf(p.getUser().getId()))) {
                entityManager.remove(p);
            }
        }

        // save the new expense image
        // try{
        // String filename = String.valueOf(expenseId); //ver ejemplo de user controller
        // de la plantilla
        // File dest = localData.getFile("expense", filename); //no molestarse en ver
        // que había antes machacar lo anterior
        // dest.delete();
        // file.transferTo(dest);
        // }
        // catch(IOException e){
        // return "Error IMAGEN " + e.getMessage();
        // }

        // update expense
        exp.setName(name);
        if (desc == null)
            desc = "";
        exp.setDesc(desc);
        exp.setPaidBy(params.paidBy);
        exp.setType(params.type);
        exp.setDate(params.date);
        exp.setAmount(amount);

        // add balance to paidBy
        params.paidByMember.setBalance(params.paidByMember.getBalance() + amount);

        // add all participants
        for (User u : params.participateUsers) {
            ParticipatesID pId = new ParticipatesID(exp.getId(), u.getId());
            Participates participates = entityManager.find(Participates.class, pId);
            if (participates == null) {
                participates = new Participates(pId, params.group, u, exp);
                entityManager.persist(participates);
            }
            // add debts of balance
            MemberID memberID = new MemberID(params.group.getId(), u.getId());
            m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() - amount / params.participateUsers.size());
        }

        // create notification ASYNC
        createAndSendNotifs(NotificationType.EXPENSE_MODIFIED, params.currUser, params.participateUsers, params.group, exp);

        // send expense to group ASYNC
        notifSender.sendTransfer(exp, "/topic/group/" + params.group.getId(), "EXPENSE", NotificationType.EXPENSE_MODIFIED);

        return "{\"action\": \"none\"}";
    }

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

        // List of users to notify
        List<User> notifyUsers = new ArrayList<>();

        // delete debts of balances
        List<Participates> participants = exp.getBelong();
        for (Participates p : participants) {
            MemberID memberID = new MemberID(group.getId(), p.getUser().getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() + exp.getAmount() / participants.size());
            notifyUsers.add(p.getUser());
        }

        // delete owed from balance
        MemberID paidByMemberID = new MemberID(group.getId(), exp.getPaidBy().getId());
        Member m = entityManager.find(Member.class, paidByMemberID);
        m.setBalance(m.getBalance() - exp.getAmount());

        // delete participants
        for (Participates p : participants)
            entityManager.remove(p);

        // disable expense
        exp.setEnabled(false);

        // create notification ASYNC
        createAndSendNotifs(NotificationType.EXPENSE_DELETED, user, notifyUsers, group, exp);

        // send expense to group ASYNC
        notifSender.sendTransfer(exp, "/topic/group/" + group.getId(), "EXPENSE", NotificationType.EXPENSE_DELETED);

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
    public StreamingResponseBody expensePic(@PathVariable long groupId, @PathVariable long expenseId, Model model,
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
        // If expense == null, its a new expense
        Expense exp = entityManager.find(Expense.class, expenseId);
        if (exp != null && !exp.isEnabled())
            throw new BadRequestException();

        // check if expense belongs to the group
        if (exp != null && !group.hasExpense(exp))
            throw new BadRequestException();

        File f = localData.getFile("expense", String.valueOf(expenseId));
        InputStream in = new BufferedInputStream(
                f.exists() ? new FileInputStream(f) : ExpenseController.defaultExpensePic());
        return os -> FileCopyUtils.copy(in, os);
    }
}
