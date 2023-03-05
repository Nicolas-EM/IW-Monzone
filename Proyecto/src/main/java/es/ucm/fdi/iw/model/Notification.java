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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="IWNotification")
public class Notification implements Transferable<Notification.Transfer> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false)
    private String desc;
    
    @Column(nullable = false)
    private boolean read;
    
    @Column(nullable = false)
    private boolean actionRequired;
    
    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne
    private User user;
  
    @AllArgsConstructor
    @Data
    public static class Transfer {
        private long id;
        private String desc;
        private boolean read;
        private boolean actionRequired;
        private LocalDateTime date;
        private long idUser;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id, desc, read, actionRequired, date, user.getId());
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

    public String getDate(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");  
        return date.format(format);  
    }

}

