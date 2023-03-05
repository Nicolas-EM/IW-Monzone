package es.ucm.fdi.iw.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * The relationship Group-User(DebtOwner)-User(Debtor)
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="IWDebt")
public class Debt implements Transferable<Debt.Transfer> {

    @EmbeddedId private DebtID dId;
    
    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private boolean paid;

	@ManyToOne
    @MapsId("groupID") private Group group;

    @ManyToOne
    @MapsId("debtOwnerID") private User debtOwner;

    @ManyToOne
    @MapsId("debtorID") private User debtor;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Transfer {
		private long groupID;
        private long debtOwnerID;
        private long debtorID;
        private float amount;
        private boolean paid;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(group.getId(), debtOwner.getId(), debtor.getId(), amount, paid);
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

}

