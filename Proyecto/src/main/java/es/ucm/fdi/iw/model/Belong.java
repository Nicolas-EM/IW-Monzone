package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

/**
 * A relationship Group<>Expense<>User
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="IWBelong")
public class Belong {

    @EmbeddedId private BelongID bId;

	@ManyToOne
    private Group group;

    @ManyToOne
    @MapsId("userID")
    private User user;

    @ManyToOne
    @MapsId("expenseID")
    private Expense expense;

}

