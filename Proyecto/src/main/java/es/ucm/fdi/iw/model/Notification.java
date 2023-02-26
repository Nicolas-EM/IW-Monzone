package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

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
    @Column(nullable = false)
    private Date date;

    @ManyToOne
    private User user;
  
    @AllArgsConstructor
    @Data
    public static class Transfer {
        private String desc;
        private boolean read;
        private boolean actionRequired;
        private Date date;
        private long idUser;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(desc, read, actionRequired, date, user.getId());
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

}

