package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * A relationship Group<>User with the user Role
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="IWMember")
public class Member implements Transferable<Member.Transfer> {

    public enum GroupRole {
        GROUP_USER,			    // normal users 
        GROUP_MODERATOR,        // admin users
    }

    @EmbeddedId private MemberID mId;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private GroupRole role;

    @Column(nullable = false)
    private float budget;

    @Column(nullable = false)
    private float balance;

	@ManyToOne
    @MapsId("groupID")
    private Group group;

    @ManyToOne
    @MapsId("userID")
    private User user;

    @Getter
    @Data
    @AllArgsConstructor
    public static class Transfer {
        private long idGroup;
        private long idUser;
        private boolean enabled;
        private GroupRole role;
        private float budget;
        private float balance;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(group.getId(), user.getId(), enabled, role, budget, balance);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }

}

