package es.ucm.fdi.iw.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Debt ID
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class DebtID implements Serializable {

    private long groupID;
    private long debtOwnerID;
    private long debtorID;

    public long getGroup() {
        return groupID;
    }

    public long getDebtOwner() {
        return debtOwnerID;
    }

    public long getDebtor() {
        return debtorID;
    }

}