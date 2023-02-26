package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.List;

/**
 * Type of expense.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="IWType")
public class Type implements Transferable<Type.Transfer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String icon;

    @OneToMany(mappedBy = "type")
    private List<Expense> expenses;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Transfer {
		private long id;
        private String name;
        private String icon;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id,	name, icon);
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

}

