package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import es.ucm.fdi.iw.model.Member.GroupRole;
import es.ucm.fdi.iw.model.User.Role;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A group of shared expenses
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Group.getAllGroups", query = "SELECT obj FROM Group obj")
})
@Table(name = "IWGroup")
public class Group implements Transferable<Group.Transfer> {
    
    private static final Logger log = LogManager.getLogger(Group.class);

    public enum Currency {
        EUR,
        USD,
        GBP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    @Column(nullable = false)
    private boolean enabled;

    private String desc;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private int numMembers;
    
    @Column(nullable = false)
    private float totBudget;
    
    @Column(nullable = false)
    private Currency currency;

    @OneToMany(mappedBy = "group")
    private List<Member> members;

    @OneToMany(mappedBy = "group")
    private List<Owns> owns;

    @OneToMany(mappedBy = "group")
    private List<Debt> debts;

    @Getter
    @Data
    @AllArgsConstructor
    public static class Transfer {
        private long id;
        private boolean enabled;
        private String name;
        private String desc;
        private int numMembers;
        private float totBudget;
        private Currency currency;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(id, enabled, name, desc, numMembers, totBudget, currency);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }

    public Boolean isMember(User u) {
        if (isGroupAdmin(u))
            return true;
        for (Member m : members) {
            if (m.getUser().getUsername().equals(u.getUsername()))
                return true;
        }
        return false;
    }

    public Boolean isGroupAdmin(User u) {
        if (u.hasRole(Role.ADMIN))
            return true;
        for (Member m : members) {
            if (m.getUser().getId() == u.getId() && m.getRole().equals(GroupRole.GROUP_MODERATOR))
                return true;
            else
                log.warn("User {}, Group {}, Role {}", u.getUsername(), id, m.getRole());
        }
        return false;
    }

}