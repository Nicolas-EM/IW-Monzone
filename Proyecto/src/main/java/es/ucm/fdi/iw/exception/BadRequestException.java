package es.ucm.fdi.iw.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    private String errorMessage;

    public BadRequestException(Integer error) {
        switch (error) {
            case -1:
                errorMessage = "Invalid data: Name, username and password must not be empty.";
                break;
            case -2:
                errorMessage = "Invalid username: Username is already taken.";
                break;
            case -3:
                errorMessage = "Passwords don't match.";
                break;
            case -4:
                errorMessage = "Invalid user: User doesn't exist.";
                break;
            case -5:
                errorMessage = "Wrong password.";
                break;
            case -6:
                errorMessage = "Invalid data: Name must not be empty.";
                break;
            case -7:
                errorMessage = "Invalid data: Budget must be a float greater or equal to 0.";
                break;
            case -8:
                errorMessage = "Invalid data: Name must be __ characters or less.";
                break;
            case -9:
                errorMessage = "Invalid data: Description must be __ characters or less.";
                break;
            case -10:
                errorMessage = "Error: A group with balances other than 0 can't be deleted.";
                break;
            case -11:
                errorMessage = "Error: A member with a balance other than 0 can't leave the group.";
                break;
            case -12:
                errorMessage = "Error: You can't leave the group being the only moderator.";
                break;
            case -13:
                errorMessage = "Invalid data: Username must not be empty.";
                break;
            case -14:
                errorMessage = "Invalid user: User already belongs to the group.";
                break;
            case -15:
                errorMessage = "Error: The group no longer exists.";
                break;
            case -16:
                errorMessage = "Invalid data: Date must be on or before today.";
                break;
            case -17:
                errorMessage = "Invalid data: Amount must be a float greater than 0.";
                break;
            case -18:
                errorMessage = "Invalid data: At least one member must participate in the expense.";
                break;
            case -19:
                errorMessage = "Invalid password: New password is the same as the old one.";
                break;
            case -20:
                errorMessage = "Invalid data: Currency must be one of the available.";
                break;
            case -21:
                errorMessage = "Invalid format date. Please choose from the calendar.";
                break;
            case -22:
                errorMessage = "Invalid data. Type must be one of the available.";
                break;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
