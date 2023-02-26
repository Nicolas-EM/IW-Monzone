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

    public enum Role {
        GROUP_USER,			// normal users 
        GROUP_ADMIN,          // admin users
    }

    @EmbeddedId private MemberID mId;

	@ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group groupEntity;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User userEntity;

    private Role role;

    private Float budget;
}

