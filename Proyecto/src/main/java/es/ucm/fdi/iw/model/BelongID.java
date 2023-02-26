package es.ucm.fdi.iw.model;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Belong ID
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BelongID implements Serializable {
    
    private long expenseID;
    private long userID;

}