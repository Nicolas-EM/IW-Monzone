package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.format.DateTimeFormatter;

/**
 * Notifications of the system.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="IWInvitation")
public class Invitation extends Notification {
    
    @Column(nullable = false)
    private boolean accepted = false;
    
    @ManyToOne
    private Group group;

    @ManyToOne
    private User sender;

	@Override
    public Transfer toTransfer() {
		return new Transfer(id, msg, dateRead, type, dateSent, user.getId(), accepted, group.getId(), sender.getId());
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

