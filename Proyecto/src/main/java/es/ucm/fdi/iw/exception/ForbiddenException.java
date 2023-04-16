package es.ucm.fdi.iw.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    
    private String errorMessage;

    public ForbiddenException(Integer error) {
        switch (error) {
            case -1:
                errorMessage = "FORBIDDEN: The group doesn't exist or you are not allowed to access it.";
                break;
            case -2:
                errorMessage = "FORBIDDEN: You can't delete a group unless you are the moderator.";
                break;
            case -3:
                errorMessage = "FORBIDDEN: You can't remove other members unless you are the moderator.";
                break;
            case -4:
                errorMessage = "FORBIDDEN: You can't invite a member unless you are the moderator.";
                break;
            case -5:
                errorMessage = "FORBIDDEN: You can't edit a group unless you are the moderator.";
                break;
            case -6:
                errorMessage = "FORBIDDEN: The expense doesn't exist or you are not allowed to access it.";
                break;
            case -7:
                errorMessage = "FORBIDDEN: User doesn't exist or doesn't belong to the group.";
                break;
            case -8:
                errorMessage = "FORBIDDEN: Group doesn't exist or you are not invited to it.";
                break;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
