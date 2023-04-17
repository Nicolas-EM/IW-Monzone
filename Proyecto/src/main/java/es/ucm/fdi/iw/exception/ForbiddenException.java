package es.ucm.fdi.iw.exception;

public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(Integer error) {
        super(getErrorMessage(error));
    }

    private static String getErrorMessage(Integer error) {
        switch (error) {
            case -1:
                return "FORBIDDEN: The group doesn't exist or you are not allowed to access it.";
            case -2:
                return "FORBIDDEN: You can't delete a group unless you are the moderator.";
            case -3:
                return "FORBIDDEN: You can't remove other members unless you are the moderator.";
            case -4:
                return "FORBIDDEN: You can't invite a member unless you are the moderator.";
            case -5:
                return "FORBIDDEN: You can't edit a group unless you are the moderator.";
            case -6:
                return "FORBIDDEN: The expense doesn't exist or you are not allowed to access it.";
            case -7:
                return "FORBIDDEN: User doesn't exist or doesn't belong to the group.";
            case -8:
                return "FORBIDDEN: Group doesn't exist or you are not invited to it.";
            default:
                return "Unknown error";
        }
    }

}
