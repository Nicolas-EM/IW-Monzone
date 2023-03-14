package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.model.Type;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Transferable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import java.io.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
     * Exception to use when denying access to unauthorized users.
     * 
     * In general, admins are always authorized, but users cannot modify
     * each other's profiles.
     */
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You're not an admin, and this is not your profile") // 403
    public static class NotYourProfileException extends RuntimeException {}

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
     *  GET MAPPINGS
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

        List<Group> groups = new ArrayList<>();
        for (Member m : memberOf) {
            if(m.isEnabled())
                groups.add(m.getGroup());
        }

        model.addAttribute("memberOf", memberOf);
        model.addAttribute("groups", groups);

        return "home";
    }

    @GetMapping("/config")
    public String home(Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");
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
        for(Group.Currency g : Group.Currency.values()){
            currencies.add(g.name());
        }
        model.addAttribute("currencies", currencies);
        return "user";
    }

    /**
     * Returns JSON with all received messages
     */
    @GetMapping(path = "receivedNotifs", produces = "application/json")
	@Transactional // para no recibir resultados inconsistentes
	@ResponseBody  // para indicar que no devuelve vista, sino un objeto (jsonizado)
	public List<Notification.Transfer> retrieveMessages(HttpSession session) {
		long userId = ((User)session.getAttribute("u")).getId();		
		User u = entityManager.find(User.class, userId);
		log.info("Generating notification list for user {} ({} notifications)",  u.getUsername(), u.getNotifications().size());
		return u.getNotifications().stream().map(Transferable::toTransfer).collect(Collectors.toList());
	}

    /**
     * Returns JSON with count of unread messages 
     */
	@GetMapping(path = "unread", produces = "application/json")
	@ResponseBody
	public String checkUnread(HttpSession session) {
		long userId = ((User)session.getAttribute("u")).getId();		
		long unread = entityManager.createNamedQuery("Notification.countUnread", Long.class)
			.setParameter("userId", userId)
			.getSingleResult();
		session.setAttribute("unread", unread);
		return "{\"unread\": " + unread + "}";
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
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("{id}/pic")
    public StreamingResponseBody getPic(@PathVariable long id) throws IOException {
        File f = localData.getFile("user", "" + id + ".jpg");
        InputStream in = new BufferedInputStream(f.exists() ? new FileInputStream(f) : UserController.defaultPic());
        return os -> FileCopyUtils.copy(in, os);
    }


    /*
     * 
     *  POST MAPPINGS
     * 
    */

    /**
     * Alter or create a user
     */
    @PostMapping("/{id}")
    @Transactional
    public String postUser(HttpServletResponse response, @PathVariable long id, @ModelAttribute User edited, @RequestParam(required = false) String pass2, Model model, HttpSession session) throws IOException {
        User requester = (User) session.getAttribute("u");
        User target = null;
        if (id == -1 && requester.hasRole(Role.ADMIN)) {
            // create new user with random password
            target = new User();
            target.setPassword(encodePassword(generateRandomBase64Token(12)));
            entityManager.persist(target);
            entityManager.flush(); // forces DB to add user & assign valid id
            id = target.getId(); // retrieve assigned id from DB
        }

        // retrieve requested user
        target = entityManager.find(User.class, id);
        model.addAttribute("user", target);

        if (requester.getId() != target.getId() &&
                !requester.hasRole(Role.ADMIN)) {
            throw new NotYourProfileException();
        }

        if (edited.getPassword() != null) {
            if (!edited.getPassword().equals(pass2)) {
                // FIXME: complain
            } else {
                // save encoded version of password
                target.setPassword(encodePassword(edited.getPassword()));
            }
        }
        target.setUsername(edited.getUsername());
        target.setName(edited.getName());

        // update user session so that changes are persisted in the session, too
        if (requester.getId() == target.getId()) {
            session.setAttribute("u", target);
        }   

        return "user";
    }

    /**
     * Uploads a profile pic for a user id
     * 
     * @param id
     * @return
     * @throws IOException
     */
    @PostMapping("{id}/pic")
    @ResponseBody
    public String setPic(@RequestParam("photo") MultipartFile photo, @PathVariable long id,
            HttpServletResponse response, HttpSession session, Model model) throws IOException {

        User target = entityManager.find(User.class, id);
        model.addAttribute("user", target);

        // check permissions
        User requester = (User) session.getAttribute("u");
        if (requester.getId() != target.getId() && !requester.hasRole(Role.ADMIN)) {
            throw new NotYourProfileException();
        }

        log.info("Updating photo for user {}", id);
        File f = localData.getFile("user", "" + id + ".jpg");
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
