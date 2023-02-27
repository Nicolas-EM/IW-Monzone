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
@Table(name="IWOwns")
public class Owns {

    @EmbeddedId private OwnsID bId;

	@ManyToOne
    private Group group;

    @ManyToOne
    @MapsId("userID")
    private User user;

    @ManyToOne
    @MapsId("expenseID")
    private Expense expense;

}

