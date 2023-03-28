package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Notifications of the system.
 */
@Entity
@Inheritance (strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
	@NamedQuery(name="Notification.countUnread",
	query="SELECT COUNT(n) FROM Notification n "
			+ "WHERE n.user.id = :userId AND n.dateRead = null")
})
@Table(name="IWNotification")
public abstract class Notification implements Transferable<Notification.Transfer> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	protected long id;

    public enum NotificationType {
        GROUP_INVITATION,
        BUDGET_WARNING,
        // DEBT_WARNING,
        EXPENSE_CREATED,
        EXPENSE_MODIFIED,
        EXPENSE_DELETED
    }

    @ManyToOne
    protected User sender;

    @ManyToOne
    protected Group group;

    @Column(nullable = false)
    protected String message;

    @Column(nullable = false)
    protected NotificationType type;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    protected LocalDateTime dateSent;

    @Column(nullable = true)
    protected LocalDateTime dateRead = null;

    // User notification constructor
    public Notification(NotificationType type, User sender, Group group){
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.sender = sender;
        this.group = group;
    }

    @AllArgsConstructor
    @Data
    public static abstract class Transfer {}

	@Override
    public abstract Transfer toTransfer();
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

    public String getDateSent(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        return dateSent.format(format);  
    }
}

