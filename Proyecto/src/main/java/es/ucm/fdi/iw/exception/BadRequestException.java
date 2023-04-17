package es.ucm.fdi.iw.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(Integer error) {
        super(getErrorMessage(error));
    }

    private static String getErrorMessage(Integer error) {
        switch (error) {
            case -1:
                return "Invalid data: Name, username and password must not be empty.";
            case -2:
                return "Invalid username: Username is already taken.";
            case -3:
                return "Passwords don't match."; 
            case -4:
                return "Invalid user: User doesn't exist.";
            case -5:
                return "Wrong password.";
            case -6:
                return "Invalid data: Name must not be empty.";
            case -7:
                return "Invalid data: Budget must be a float greater or equal to 0.";
            case -8:
                return "Invalid data: Name must be __ characters or less.";
            case -9:
                return "Invalid data: Description must be __ characters or less.";
            case -10:
                return "Error: A group with balances other than 0 can't be deleted.";
            case -11:
                return "Error: A member with a balance other than 0 can't leave the group.";
            case -12:
                return "Error: You can't leave the group being the only moderator.";
            case -13:
                return "Invalid data: Username must not be empty.";
            case -14:
                return "Invalid user: User already belongs to the group.";
            case -15:
                return "Error: The group no longer exists.";
            case -16:
                return "Invalid data: Date must be on or before today.";
            case -17:
                return "Invalid data: Amount must be a float greater than 0.";
            case -18:
                return "Invalid data: At least one member must participate in the expense.";
            case -19:
                return "Invalid password: New password is the same as the old one.";
            case -20:
                return "Invalid data: Currency must be one of the available.";
            case -21:
                return "Invalid format date. Please choose from the calendar.";
            case -22:
                return "Invalid data. Type must be one of the available.";
            default:
                return "Unknown error.";
        }
    }

}
