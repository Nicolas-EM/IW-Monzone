package es.ucm.fdi.iw.model;

import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.ucm.fdi.iw.model.Notification.NotificationType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper Class to send notifications
 */
public class NotificationSender {

    private SimpMessagingTemplate messagingTemplate;
    private static NotificationSender instance;
    private static final Logger log = LogManager.getLogger(NotificationSender.class);

    public static NotificationSender getInstance(SimpMessagingTemplate messagingTemplate) {
        if (instance == null) {
            instance = new NotificationSender();
            instance.messagingTemplate = messagingTemplate;
        }
        return instance;
    }    

    @Async
    public CompletableFuture<Void> sendNotification(Notification notif, String endpoint) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonNotif = mapper.writeValueAsString(notif.toTransfer());
            log.info("Sending a notification to {} with contents '{}'", endpoint, jsonNotif);

            String json = "{\"type\" : \"NOTIFICATION\", \"notification\" : " + jsonNotif + "}";

            messagingTemplate.convertAndSend(endpoint, json);
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse notification - {}}", notif);
            log.error("Exception {}", exception);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public <T> CompletableFuture<Void> sendTransfer(Transferable<T> obj, String endpoint, String objType, NotificationType type) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonTransfer = mapper.writeValueAsString(obj.toTransfer());

            log.info("Sending a {} to {} with contents '{}'", objType, endpoint, jsonTransfer);

            String json = "{\"type\" : \"" + objType.toUpperCase() +"\", \"action\" : \"" + type + "\",\"" + objType.toLowerCase() + "\" : " + jsonTransfer + "}";

            messagingTemplate.convertAndSend(endpoint, json);
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse transfer - {}}", obj);
            log.error("Exception {}", exception);
        }

        return CompletableFuture.completedFuture(null);
    }
    
}