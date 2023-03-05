package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import javax.persistence.*;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;  

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
    private boolean enabled;

    @Column(nullable = false)
    private String name;

    private String desc;

    @Column(nullable = false)
    private long amount;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    private LocalDateTime date;

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
        private boolean enabled;
        private String name;
        private String desc;
        private long amount;
        private LocalDateTime date;
        private long typeID;
        private long paidByID;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id,	enabled, name, desc, amount, date, type.getId(), paidBy.getId());
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

    public String getDate(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
        return date.format(format);  
    }

}