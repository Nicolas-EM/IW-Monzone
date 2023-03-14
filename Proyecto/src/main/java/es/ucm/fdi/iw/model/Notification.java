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
public class Notification implements Transferable<Notification.Transfer> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	protected long id;

    public enum NotificationType {
        BASIC,
        INVITATION,
        BUDGET_WARNING,
        DEBT_WARNING
    }

    @Column(nullable = false)
    protected String msg;
    
    protected LocalDateTime dateRead;
    
    @Column(nullable = false)
    protected NotificationType type;
    
    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    protected LocalDateTime dateSent;

    @ManyToOne
    protected User user;
  
    @AllArgsConstructor
    @Data
    public static class Transfer {
        private long id;
        private String msg;
        private LocalDateTime read;
        private NotificationType type;
        private LocalDateTime date;
        private long idUser;
        private boolean accepted;
        private long idGroup;
        private long idSender;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id, msg, dateRead, type, dateSent, user.getId(), false, -1, -1);
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

    public String getDate(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        return dateSent.format(format);  
    }

}

