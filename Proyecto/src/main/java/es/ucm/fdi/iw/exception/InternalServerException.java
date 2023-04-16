package es.ucm.fdi.iw.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerException extends RuntimeException {
    
    private String errorMessage;

    public InternalServerException(Integer error) {
        switch (error) {
            case -1:
                errorMessage = "Error: Bad data in DB.";
                break;
            case -2:
                errorMessage = "Error: Unknown.";
                break;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
