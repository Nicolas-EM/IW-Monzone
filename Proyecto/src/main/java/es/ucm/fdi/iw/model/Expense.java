package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import javax.persistence.*;

import java.util.List;
import java.text.SimpleDateFormat;
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
    private String name;

    private String desc;

    @Column(nullable = false)
    private long amount;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    private LocalDateTime date;

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
        private LocalDateTime date;
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

    public String getDate(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        return date.format(format);  
    }
}