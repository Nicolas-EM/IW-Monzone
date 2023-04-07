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
	query="SELECT COUNT(*) FROM Notification n WHERE n.recipient.id = :userId AND n.dateRead = null"),
    @NamedQuery(name="Notification.byUserAndGroup",
    query="SELECT n FROM Notification n WHERE recipient.id = :userId AND group.id = :groupId")
})
@Table(name="IWNotification")
public class Notification implements Transferable<Notification.Transfer>  {
    
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
        EXPENSE_DELETED,

        GROUP_MODIFIED,
        GROUP_DELETED
    }

    @ManyToOne
    protected User sender;

    @ManyToOne
    protected User recipient;

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

    public Notification(NotificationType type, User sender, User recipient, Group group){
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.sender = sender;
        this.recipient = recipient;
        this.group = group;

        messageBuilder();
    }

    // Expense notifications
    public Notification(NotificationType type, User sender, User recipient, Group group, Expense e){
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.sender = sender;
        this.recipient = recipient;
        this.group = group;

        messageBuilder(e);
    }

    private String actionBuilder(){
        switch(type){
            case EXPENSE_CREATED:
            case EXPENSE_MODIFIED:
            case EXPENSE_DELETED:
                return "/user/" + this.id + "/read";
            case GROUP_INVITATION:
                return "/group/" + this.group.getId() + "/acceptInvite";
            default:
                return ""; 
        }
    }

    private void messageBuilder(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.sender.getName());

        switch(type) {
            case GROUP_INVITATION:
                sb.append(" has invited you to join \"");
                break;
            default: {}                
        }

        sb.append(this.group.getName());
        sb.append("\"");

        this.message = sb.toString();
    }

    // Expense message builder
    private void messageBuilder(Expense e){
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

        sb.append(e.getName());
        sb.append("\" in group ");
        sb.append(this.group.getName());

        this.message = sb.toString();
    }

    public String getDateSent(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        return dateSent.format(format);  
    }

    @AllArgsConstructor
    @Data
    public class Transfer {
        private long id;
        private String message;
        private NotificationType type;
        private String dateRead;
        private String dateSent;
        private long idGroup;
        private long idSender;
        private long idRecipient;
        private String actionEndpoint;
    }

    @Override
    public Transfer toTransfer() {
        String dateReadString = "";
        if(dateRead != null)
            dateReadString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateRead);

        return new Transfer(id, message, type, dateReadString, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateSent), group.getId(), sender.getId(), recipient.getId(), actionBuilder());
    }
}

