package es.ucm.fdi.iw.model;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Member ID
 */
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MemberID implements Serializable {
    @Getter
    @Column(name = "group_id")
    private long groupId;
    
    @Getter
    @Column(name = "user_id")
    private long userId;
}