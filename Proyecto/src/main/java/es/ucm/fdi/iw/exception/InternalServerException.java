package es.ucm.fdi.iw.exception;

public class InternalServerException extends RuntimeException {
    
    public InternalServerException(Integer error) {
        super(getErrorMessage(error));
    }

    private static String getErrorMessage(Integer error) {
        switch (error) {
            case -1:
                return "Error: Bad data in DB.";
            default:
                return "Error: Unknown.";                
        }
    }

}
