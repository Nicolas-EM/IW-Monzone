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
    @MapsId("groupId")
    private Group groupEntity;

    @ManyToOne
    @MapsId("userId")
    private User userEntity;

    @ManyToOne
    @MapsId("expenseId")
    private Expense expenseEntity;
}

