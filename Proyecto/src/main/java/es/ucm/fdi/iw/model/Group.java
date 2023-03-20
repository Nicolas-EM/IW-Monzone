package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A group of shared expenses
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Group.getAllGroups", query = "SELECT obj FROM Group obj")
})
@Table(name = "IWGroup")
public class Group implements Transferable<Group.Transfer>, Comparator<Group> {
    
    private static final Logger log = LogManager.getLogger(Group.class);

    public enum Currency {
        EUR,
        USD,
        GBP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    @Column(nullable = false)
    private boolean enabled;

    private String desc;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private int numMembers;
    
    @Column(nullable = false)
    private float totBudget;
    
    @Column(nullable = false)
    private Currency currency;

    @OneToMany(mappedBy = "group")
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Participates> owns = new ArrayList<>();

    public Group(String name, String desc, Currency currency){
        this.enabled = true;
        this.name = name;
        this.desc = desc;
        this.currency = currency;
        this.totBudget = 0;
        this.numMembers = 0;
    }

    @Getter
    @Data
    @AllArgsConstructor
    public static class Transfer {
        private long id;
        private boolean enabled;
        private String name;
        private String desc;
        private int numMembers;
        private float totBudget;
        private Currency currency;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(id, enabled, name, desc, numMembers, totBudget, currency);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }

    public boolean hasExpense(Expense e) {
        for (Participates p : owns)
            if (p.getExpense().getId() == e.getId())
                return true;
        return false;
    }

    public String getCurrencyText() {
        switch(currency){
            case EUR: 
                return "€";
            case USD:
                return "$";
            case GBP:
                return "£";
            default:
                return "";
        }
    }

    @Override
    public int compare(Group g1, Group g2) {
        return g1.getName().compareTo(g2.getName());
    }

    public void addMember(Member m){
        this.members.add(m);
    }
}