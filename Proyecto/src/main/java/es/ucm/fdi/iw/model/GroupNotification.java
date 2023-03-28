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
public class GroupNotification extends Notification {
    private float amount;
  
    // Group Notification constructor
    public GroupNotification(NotificationType type, User sender, Group group){
        super(type, sender, group);

        switch(type){
            case EXPENSE_CREATED:
                expenseCreated();
                break;
            case EXPENSE_MODIFIED:
                expenseModified();
                break;
            case EXPENSE_DELETED:
                expenseDeleted();
                break;
            default:
        }
    }

    private void expenseCreated(){

    }

    private void expenseModified(){
        
    }

    private void expenseDeleted(){
        
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
        return new Transfer(id, msg, dateRead, type, dateSent, user.getId(), false, -1, -1);
    }
}

