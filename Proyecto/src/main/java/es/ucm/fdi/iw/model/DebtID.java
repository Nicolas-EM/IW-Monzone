package es.ucm.fdi.iw.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

/**
 * Debt ID
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebtID implements Serializable {

    private long groupID;
    private long debtOwnerID;
    private long debtorID;

}