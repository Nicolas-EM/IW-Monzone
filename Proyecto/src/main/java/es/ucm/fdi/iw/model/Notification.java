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
@Table(name="IWNotification")
public abstract class Notification {
    
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
    public Notification(NotificationType type, User sender){
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.sender = sender;
    }

    public String getDateSent(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        return dateSent.format(format);  
    }
}

