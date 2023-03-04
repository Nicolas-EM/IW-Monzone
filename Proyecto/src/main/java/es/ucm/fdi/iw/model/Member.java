package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
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
public class Member {

    public enum GroupRole {
        GROUP_USER,			// normal users 
        GROUP_ADMIN,          // admin users
    }

    @EmbeddedId private MemberID mId;

	@ManyToOne
    @MapsId("groupID")
    private Group group;

    @ManyToOne
    @MapsId("userID")
    private User user;

    @Column(nullable = false)
    private GroupRole role;

    @Column(nullable = false)
    private Float budget;

}

