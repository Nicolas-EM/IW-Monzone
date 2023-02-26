package es.ucm.fdi.iw.model;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Belong ID
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class BelongID implements Serializable {
    @Column(name = "group_id")
    private long groupId;
    
    @Column(name = "expense_id")
    private long expenseId;
    
    @Column(name = "user_id")
    private long userId;
}