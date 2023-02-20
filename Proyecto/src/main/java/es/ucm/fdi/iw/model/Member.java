package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A relationship Group<>User with the user Role
 */
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
    @Getter
    private Group groupEntity;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @Getter
    private User userEntity;

    @Getter
    private Role role;

    public Member(){}

    public Member(Group group, User user, Role role){
        this.groupEntity = group;
        this.userEntity = user;
        this.role = role;
    }
}

