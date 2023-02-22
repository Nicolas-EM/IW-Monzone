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
@NamedQueries({
        @NamedQuery(name="Group.byTitle",
                query="SELECT u FROM User u "
                        + "WHERE u.username = :username AND u.enabled = TRUE"),
})
@Table(name="IWGroup")
public class Group implements Transferable<Group.Transfer> {

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

	@OneToMany(mappedBy = "groupEntity")
	private List<Member> members = new ArrayList<>();

    @Getter
    @AllArgsConstructor
    public static class Transfer {
		private long id;
        @Getter
        private String name;
        @Getter
		private String desc;
        @Getter
        private Integer numMembers;
        @Getter
        private Float totBudget;
        @Getter
		private List<Member> members;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id,	name, desc, numMembers, totBudget, members);
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

}