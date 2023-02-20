package es.ucm.fdi.iw.model;

import java.io.Serializable;

import javax.persistence.*;

/**
 * Member ID
 */
@Embeddable
public class MemberID implements Serializable {
    @Column(name = "group_id")
    private long groupId;
    @Column(name = "user_id")
    private long userId;

    public MemberID(){

    }

    public MemberID(long group, long user){
        this.groupId = group;
        this.userId = user;
    }

    public long getGroup() {
        return groupId;
    }

    public long getUser() {
        return userId;
    }
}