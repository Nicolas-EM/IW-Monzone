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
public class UserNotification extends Notification {
    
    @ManyToOne
    private User recipient;


    // User notification constructor
    public UserNotification(NotificationType type, User sender, Group group, User recipient){
        super(type, sender, group);

        switch(type){
            case GROUP_INVITATION:
                invitationConstructor();
                break;
            case BUDGET_WARNING:
                budgetWarningConstructor();
                break;
            default:

        }
    }

    public void invitationConstructor(){
        StringBuilder sb = new StringBuilder();
        sb.append("You have been invited to join the group ");
        sb.append(this.group.getName());
        this.message = sb.toString();
    }

    public void budgetWarningConstructor(){

    }

    @AllArgsConstructor
    @Data
    public static class Transfer {
        private long id;
        private String msg;
        private LocalDateTime read;
        private NotificationType type;
        private LocalDateTime date;
        private long senderId;
        private boolean accepted;
        private long idGroup;
        private long idSender;
        private long idRecipient;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id, message, dateRead, type, dateSent, sender.getId(), recipient.getId(), false, -1, -1);
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

    public String getDateSent(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        return dateSent.format(format);  
    }
}

