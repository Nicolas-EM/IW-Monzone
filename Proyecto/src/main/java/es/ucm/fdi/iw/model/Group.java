package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of shared expenses
 */
@Entity
@Data
@NoArgsConstructor
@Table(name="IWGroup")
public class Group implements Transferable<Group.Transfer> {

    public enum Currency {
        EUR,
        USD,
        GBP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String desc;
    @Column(nullable = false)
    private Integer numMembers;
    @Column(nullable = false)
    private Float totBudget;
    @Column(nullable = false)
    private Currency currency;

	@OneToMany(mappedBy = "groupEntity")
	private List<Member> members = new ArrayList<>();

    @Getter
    @Data
    @AllArgsConstructor
    public static class Transfer {
		private long id;
        private String name;
		private String desc;
        private Integer numMembers;
        private Float totBudget;
        private Currency currency;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id,	name, desc, numMembers, totBudget, currency);
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

}