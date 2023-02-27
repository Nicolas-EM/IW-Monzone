package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.List;
import java.util.Date;

/**
 * An authorized user of the system.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name="Expense.byId",
                query="SELECT u FROM Expense u "
                        + "WHERE u.id = :id")
})
@Table(name="IWExpense")
public class Expense implements Transferable<Expense.Transfer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false)
    private String name;

    private String desc;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private String picture;

    @ManyToOne
    private Type type;

    @ManyToOne
    private User paidBy;

    @OneToMany(mappedBy = "expense")
    private List<Owns> belong;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transfer {
		private long id;
        private String name;
        private String desc;
        private long amount;
        private Date date;
        private String picture;
        private long typeID;
        private long paidByID;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id,	name, desc, amount, date, picture, type.getId(), paidBy.getId());
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

}