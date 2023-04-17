package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.controller.ExpenseController.ImageSavingFailed;
import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.model.Type;
import es.ucm.fdi.iw.model.Transferable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import java.io.*;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

import es.ucm.fdi.iw.exception.*;

/**
 * User management.
 *
 * Access to this end-point is authenticated.
 */
@Controller()
@RequestMapping("user")
public class UserController {

    private static final Logger log = LogManager.getLogger(UserController.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

    @Autowired
    private PasswordEncoder passwordEncoder;
 

    /**
     * Encodes a password, so that it can be saved for future checking. Notice
     * that encoding the same password multiple times will yield different
     * encodings, since encodings contain a randomly-generated salt.
     * 
     * @param rawPassword to encode
     * @return the encoded password (typically a 60-character string)
     *         for example, a possible encoding of "test" is
     *         {bcrypt}$2y$12$XCKz0zjXAP6hsFyVc8MucOzx6ER6IsC1qo5zQbclxhddR1t6SfrHm
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Generates random tokens. From https://stackoverflow.com/a/44227131/15472
     * 
     * @param byteLength
     * @return
     */
    public static String generateRandomBase64Token(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token); // base64 encoding
    }

    /*
     * 
     * GET MAPPINGS
     * 
     */

    /**
     * Landing page for a user profile
     */
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        List<Member> memberOf = user.getMemberOf();

        List<Member> enabledMemberOf = new ArrayList<>();
        for (Member m : memberOf) {
            if (m.isEnabled())
                enabledMemberOf.add(m);
        }

        model.addAttribute("memberOf", enabledMemberOf);

        return "home";
    }

    /**
     * End point
     */
    @ResponseBody
    @GetMapping("/getMonthly/{dateString}/{currId}")
    public float getMonthly(HttpSession session, @PathVariable String dateString, @PathVariable int currId) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        
        LocalDate formDate;
        try {
            formDate = LocalDate.parse(dateString + "-01");
        } catch (Exception e) {
            throw new BadRequestException(-21);
        }

        // check currency 
        if (currId < 0 || currId >= Group.Currency.values().length)
        throw new BadRequestException(-20);
        Group.Currency newCurr = Group.Currency.values()[currId];

        float total = 0f;
        List<Participates> participates = user.getExpenses();
        for (Participates p : participates) {
            Expense exp = p.getExpense();
            LocalDate eDate;
            try {
                eDate = LocalDate.parse(exp.getDate());
            } catch (Exception e) {
                throw new BadRequestException(-21);
            }
            
            if (eDate.getMonthValue() == formDate.getMonthValue() && eDate.getYear() == formDate.getYear())
                total += changeCurrency(exp.getAmount() / exp.getBelong().size(), p.getGroup().getCurrency(), newCurr);
        }
        float totalRounded = (float) Math.round(total * 100) / 100;
        return totalRounded;
    }

    private float changeCurrency(float amount, Group.Currency actualCurr, Group.Currency newCurr) {
        if (actualCurr.equals(newCurr))
            return amount; // No se pueden convertir de una divisa a sí misma
        // Conforme al orden: EUR, USD, GBP
        float[][] exchangeRates = new float[3][3]; // Group.Currency.values().length
        exchangeRates[0][1] = 1.19f; // 1 EUR = 1.19 USD
        exchangeRates[1][0] = 0.84f; // 1 USD = 0.84 EUR
        exchangeRates[0][2] = 0.85f; // 1 EUR = 0.85 GBP
        exchangeRates[2][0] = 1.18f; // 1 GBP = 1.18 EUR
        exchangeRates[1][2] = 0.71f; // 1 USD = 0.71 GBP
        exchangeRates[2][1] = 1.41f; // 1 GBP = 1.41 USD

        int actualCurrIndex = actualCurr.ordinal();
        int newCurrIndex = newCurr.ordinal();
        float exchangeRate = exchangeRates[actualCurrIndex][newCurrIndex];
        return amount * exchangeRate;
    }

    /**
     * End point
     */
    @ResponseBody
    @GetMapping("/getByType/{currId}")
        public List<Float> getByType(Model model, HttpSession session, @PathVariable int currId) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());
                
        // check currency 
        if (currId < 0 || currId >= Group.Currency.values().length)
        throw new BadRequestException(-20);
        Group.Currency newCurr = Group.Currency.values()[currId];

        List<Type> types = entityManager.createNamedQuery("Type.getAllTypes",Type.class).getResultList();
        List<Float> totals = new ArrayList<>(types.size());
        
        Collections.fill(totals, 0.0f);

        List<Participates> participates = user.getExpenses();
        for (Participates p : participates) {
            Expense e = p.getExpense();
            int index = (int)e.getType().getId();
            totals.set(index, totals.get(index) + changeCurrency(e.getAmount() / e.getBelong().size(), p.getGroup().getCurrency(), newCurr));
        }

        for (int i = 0; i < totals.size(); i++)
            totals.set(i, (float) Math.round(totals.get(i) * 100) / 100);

        return totals;
    }

    @GetMapping("/config")
    public String home(Model model, HttpSession session) {
        
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        model.addAttribute("user", user);
        List<Type> types = entityManager.createNamedQuery("Type.getAllTypes", Type.class).getResultList();
        model.addAttribute("types", types);

        List<Member> memberOf = user.getMemberOf();
        model.addAttribute("memberOf", memberOf);

        List<Group> groups = new ArrayList<>();
        for (Member m : memberOf) {
            groups.add(m.getGroup());
        }
        model.addAttribute("groups", groups);

        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }
        model.addAttribute("currencies", currencies);

        return "user";
    }

    /**
     * Returns JSON with all received USER messages
     */
    @GetMapping(path = "receivedNotifs", produces = "application/json")
    @Transactional // para no recibir resultados inconsistentes
    @ResponseBody // para indicar que no devuelve vista, sino un objeto (jsonizado)
    public List<Notification.Transfer> retrieveUserMessages(HttpSession session) {
        long userId = ((User) session.getAttribute("u")).getId();
        User u = entityManager.find(User.class, userId);
        log.info("Generating USER NOTIFS list for user {} ({} notifications)", u.getUsername(),
                u.getNotifications().size());
        return u.getNotifications().stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /**
     * Returns JSON with count of unread messages
     */
    @GetMapping(path = "unread", produces = "application/json")
    @ResponseBody
    public String checkUnread(HttpSession session) {
        User u = (User) session.getAttribute("u");
        long userId = u.getId();
        u = entityManager.find(User.class, userId);

        long unread = entityManager.createNamedQuery("Notification.countUnread", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        log.info("UNREAD - {} User notifications", unread);

        session.setAttribute("unread", unread);
        return "{\"unread\": " + unread + "}";
    }

    /**
     * Returns JSON with ids of groups that user belongs to
     */
    @GetMapping(path = "groups", produces = "application/json")
    @Transactional // para no recibir resultados inconsistentes
    @ResponseBody // para indicar que no devuelve vista, sino un objeto (jsonizado)
    public List<Long> getGroups(HttpSession session) {
        long userId = ((User) session.getAttribute("u")).getId();
        User u = entityManager.find(User.class, userId);

        List<Long> groupIds = new ArrayList<>();
        for (Member m : u.getMemberOf()) {
            if (m.isEnabled())
                groupIds.add(m.getGroup().getId());
        }

        log.info("Generating group list for user {} ({} groups)", u.getUsername(), groupIds.size());
        return groupIds;
    }

    /**
     * Returns the default profile pic
     * 
     * @return
     */
    private static InputStream defaultPic() {
        return new BufferedInputStream(Objects.requireNonNull(
                UserController.class.getClassLoader().getResourceAsStream(
                        "static/img/profile/default-profile.png")));
    }

    /**
     * Downloads a profile pic for a user id
     * 
     * @return
     * @throws IOException
     */
    @GetMapping("/{id}/pic")
    public StreamingResponseBody getPic(HttpSession session) throws IOException {
        long id = ((User) session.getAttribute("u")).getId();
        File f = localData.getFile("user", "" + id);
        InputStream in = new BufferedInputStream(f.exists() ? new FileInputStream(f) : UserController.defaultPic());
        return os -> FileCopyUtils.copy(in, os);
    }

    /*
     * 
     * POST MAPPINGS
     * 
     */

    @PostMapping("/{notifId}/read")
    @Transactional
    @ResponseBody
    public String markNotifRead(@PathVariable long notifId, HttpSession session) {
        long userId = ((User) session.getAttribute("u")).getId();

        // Check notification exists
        Notification notif = entityManager.find(Notification.class, notifId);
        if (notif == null)
            throw new ForbiddenException(-8);

        // Check notification belongs to user
        if (notif.getRecipient().getId() != userId)
            throw new ForbiddenException(-8);

        notif.setDateRead(LocalDateTime.now());

        return "{\"success\": \"ok\"}";
    }

    /**
     * Delete notification
     */
    @PostMapping("/{notifId}/delete")
    @Transactional
    @ResponseBody
    public String deleteNotif(@PathVariable long notifId, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // Check notification exists
        Notification notif = entityManager.find(Notification.class, notifId);
        if (notif == null)
            throw new ForbiddenException(-8);

        // Check notification belongs to user
        if (notif.getRecipient().getId() != user.getId())
            throw new ForbiddenException(-8);

        // delete notification
        user.getNotifications().remove(notif);
        entityManager.remove(notif);

        return "{\"success\": \"ok\"}";
    }

     /*
     * Alter or create a user
     */
    // @ResponseBody
    // @Transactional
    // @PostMapping("/{id}")
    // public String postUser(HttpSession session, @PathVariable long id, Model model, @RequestParam("name") String name,  @RequestParam("username") String username, @RequestParam("oldPwd") String oldPwd, @RequestParam("newPwd") String newPwd,@RequestParam(value = "avatar", required = false) MultipartFile imageFile) {
       
    //     User requester = (User) session.getAttribute("u");
    //     User target = null;

    //     if (id == -1 && requester.hasRole(Role.ADMIN)) {
    //         // create new user with random password
    //         target = new User();
    //         target.setPassword(encodePassword(generateRandomBase64Token(12)));
    //         entityManager.persist(target);
    //         entityManager.flush(); // forces DB to add user & assign valid id
    //         id = target.getId(); // retrieve assigned id from DB
    //     }

    //     // retrieve requested user
    //     target = entityManager.find(User.class, id);
    //     model.addAttribute("user", target);

    //     if (requester.getId() != target.getId() &&
    //             !requester.hasRole(Role.ADMIN)) {
    //         throw new NotYourProfileException();
    //     }

    //     if (newPwd != null) {
    //         if (!newPwd.equals(oldPwd)) {
    //             // FIXME: complain
    //         } else {
    //             // save encoded version of password
    //             target.setPassword(encodePassword(newPwd));
    //         }
    //     }
    //     target.setUsername(username);
    //     target.setName(name);

    //     // save the new user image
    //     if(imageFile != null){
    //         File f = localData.getFile("user", "" + id);
    //         try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
    //             byte[] imageBytes = imageFile.getBytes();
    //             stream.write(imageBytes);
    //             log.info("Uploaded photo for {} into {}!", id, f.getAbsolutePath());
    //         } catch (Exception e) {
    //             log.warn("Error uploading image " + id + " ", e);
    //             throw new ImageSavingFailed();
    //         }
    //     }

    //     // update user session so that changes are persisted in the session, too
    //     if (requester.getId() == target.getId()) {
    //         session.setAttribute("u", target);
    //     }

    //     return "{\"action\": \"none\"}";
    // }


    /*
     * Change data user
     */
    @ResponseBody
    @Transactional
    @PostMapping("/ChangeDataUser")
    public String postUserData(HttpSession session, Model model, @RequestParam("name") String name,  @RequestParam("username") String username, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        User target = (User) session.getAttribute("u");
        target = entityManager.find(User.class, target.getId());

        target.setUsername(username);
        target.setName(name);

        // save the new user image
        if(imageFile != null){
            File f = localData.getFile("user", "" + target.getId());
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
                byte[] imageBytes = imageFile.getBytes();
                stream.write(imageBytes);
                log.info("Uploaded photo for {} into {}!", target.getId(), f.getAbsolutePath());
            } catch (Exception e) {
                log.warn("Error uploading image " + target.getId() + " ", e);
                throw new ImageSavingFailed();
            }
        }

        session.setAttribute("u", target);

        return "{\"action\": \"none\"}";
    }


     /*
     * Change Password
     */
    @ResponseBody
    @Transactional
    @PostMapping("/ChangePassword")
    public String postChangePassword(HttpSession session, @RequestBody JsonNode jsonNode, Model model) {
        
        String oldPwd = jsonNode.get("oldPwd").asText();
        String newPwd = jsonNode.get("newPwd").asText();

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        if (newPwd != null && oldPwd != null) {

            if (passwordEncoder.matches(oldPwd, user.getPassword())) { // Comprobar que la contraseña actual del user es igual a oldPwd  
                if(newPwd.equals(oldPwd)){ // Si la contraseña antigua y la nueva coinciden
                    throw new BadRequestException(-19);
                }
                else{
            user.setPassword(encodePassword(newPwd)); //save encoded version of password
                }
            }
            else{ // Old password incorrecta
                throw new BadRequestException(-5);
            }

        }    
       
        return "{\"action\": \"none\"}";
    }


    // /**
    //  * Alter or create a user
    //  */
    // @PostMapping("/{id}")
    // @Transactional
    // public String postUser(HttpServletResponse response, @PathVariable long id, @ModelAttribute User edited,
    //         @RequestParam(required = false) String pass2, Model model, HttpSession session) throws IOException {
        
    //     User requester = (User) session.getAttribute("u");
    //     User target = null;
    //     if (id == -1 && requester.hasRole(Role.ADMIN)) {
    //         // create new user with random password
    //         target = new User();
    //         target.setPassword(encodePassword(generateRandomBase64Token(12)));
    //         entityManager.persist(target);
    //         entityManager.flush(); // forces DB to add user & assign valid id
    //         id = target.getId(); // retrieve assigned id from DB
    //     }

    //     // retrieve requested user
    //     target = entityManager.find(User.class, id);
    //     model.addAttribute("user", target);

    //     if (requester.getId() != target.getId() &&
    //             !requester.hasRole(Role.ADMIN)) {
    //         throw new NotYourProfileException();
    //     }

    //     if (edited.getPassword() != null) {
    //         if (!edited.getPassword().equals(pass2)) {
    //             // FIXME: complain
    //         } else {
    //             // save encoded version of password
    //             target.setPassword(encodePassword(edited.getPassword()));
    //         }
    //     }
    //     target.setUsername(edited.getUsername());
    //     target.setName(edited.getName());

    //     // update user session so that changes are persisted in the session, too
    //     if (requester.getId() == target.getId()) {
    //         session.setAttribute("u", target);
    //     }

    //     return "user";
    // }

    /**
     * Uploads a profile pic for a user id
     * 
     * @param id
     * @return
     * @throws IOException
     */
    @PostMapping("{id}/pic")
    @ResponseBody
    public String setPic(@RequestParam("avatar") MultipartFile photo, @PathVariable long id,
            HttpServletResponse response, HttpSession session, Model model) throws IOException {

        User target = entityManager.find(User.class, id);
        model.addAttribute("user", target);

        // check permissions
        User requester = (User) session.getAttribute("u");
        if (requester.getId() != target.getId() && !requester.hasRole(Role.ADMIN)) {
            // throw new NotYourProfileException();
        }

        log.info("Updating photo for user {}", id);
        File f = localData.getFile("user", "" + id);
        if (photo.isEmpty()) {
            log.info("failed to upload photo: emtpy file?");
        } else {
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
                byte[] bytes = photo.getBytes();
                stream.write(bytes);
                log.info("Uploaded photo for {} into {}!", id, f.getAbsolutePath());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.warn("Error uploading " + id + " ", e);
            }
        }
        return "{\"status\":\"photo uploaded correctly\"}";
    }
}
