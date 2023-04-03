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
@Table(name="IWActionNotification")
public class ActionNotification extends Notification {
    
    String actionEndpoint;

    // User notification constructor
    public ActionNotification(NotificationType type, User sender, User recipient, Group group){
        super(type, sender, recipient, group);

        switch(type){
            case GROUP_INVITATION:
                invitationConstructor();
                break;
            default:

        }
    }

    public void invitationConstructor(){
        StringBuilder sb = new StringBuilder();
        sb.append("You have been invited to join the group ");
        sb.append(group.getName());
        this.message = sb.toString();
    }

    @Data
    public class Transfer extends Notification.Transfer {
        private String actionEndpoint;
    
        public Transfer(long id, String message, NotificationType type, String dateRead, String dateSent, long idGroup, long idSender, long idRecipient, String actionEndpoint) {
            super(id, message, type, dateRead, dateSent, idGroup, idSender, idRecipient);
            this.actionEndpoint = actionEndpoint;
        }
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id, message, type, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateRead), DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateSent), group.getId(), sender.getId(), recipient.getId(), actionEndpoint);
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

