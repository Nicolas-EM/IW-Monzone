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
	@NamedQuery(name="GroupNotification.countUnread",
	query="SELECT COUNT(*) FROM GroupNotification n "
			+ "WHERE n.group.id = :groupId AND n.dateRead = null")
})
@Table(name="IWGroupNotification")
public class GroupNotification extends Notification implements Transferable<GroupNotification.Transfer> {
    private String expenseName;

    @ManyToOne
    private Group group;
  
    // Group Notification constructor
    public GroupNotification(NotificationType type, User sender, Group group, String expenseName) {
        super(type, sender);
        this.expenseName = expenseName;
        this.group = group;
        buildMessage(type);
    }

    private void buildMessage(NotificationType type) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.sender.getName());

        switch(type) {
            case EXPENSE_CREATED:
                sb.append(" created the expense \"");
                break;
            case EXPENSE_MODIFIED:
                sb.append(" updated the expense \"");
                break;
            case EXPENSE_DELETED:
                sb.append(" deleted the expense \"");
                break;
            default: {}                
        }

        sb.append(this.expenseName);
        sb.append("\" in group ");
        sb.append(this.group.getName());

        this.message = sb.toString();
    }

    @AllArgsConstructor
    @Data
    public static class Transfer {
        private long id;
        private NotificationType type;
        private String dateSent;
        private String message;
        private String senderName;
        private long senderId;
        private String groupName;
        private long groupId;
        private String expenseName;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(id, type, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateSent), message, sender.getName(), sender.getId(), group.getName(), group.getId(), expenseName);
    }
}

