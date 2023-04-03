package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	@NamedQuery(name="UserNotification.countUnread",
	query="SELECT COUNT(n) FROM UserNotification n "
			+ "WHERE n.recipient.id = :userId AND n.dateRead = null")
})
@Table(name="IWUserNotification")
public class UserNotification extends Notification implements Transferable<UserNotification.Transfer> {
    
    @ManyToOne
    private User recipient;

    // User notification constructor
    public UserNotification(NotificationType type, User sender, Group group, User recipient){
        super(type, sender);

        switch(type){
            case GROUP_INVITATION:
                invitationConstructor(group);
                break;
            case BUDGET_WARNING:
                budgetWarningConstructor(group);
                break;
            default:

        }
    }

    public void invitationConstructor(Group group){
        StringBuilder sb = new StringBuilder();
        sb.append("You have been invited to join the group ");
        sb.append(group.getName());
        this.message = sb.toString();
    }

    public void budgetWarningConstructor(Group group){
        StringBuilder sb = new StringBuilder();
        sb.append("You have been invited to join the group ");
        sb.append(group.getName());
        this.message = sb.toString();
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
		return new Transfer(id, message, dateRead, type, dateSent, sender.getId(), false, recipient.getId(), -1, -1);
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

