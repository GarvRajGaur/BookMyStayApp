/**
 * InvalidGuestNameException
 *
 * Use Case 9: Error Handling & Validation
 *
 * Thrown when a guest name provided for a booking request is null,
 * empty, or contains only whitespace.
 *
 * A reservation without a valid guest name is meaningless — it cannot
 * be confirmed, reported on, or looked up. Catching this early prevents
 * unnamed records from polluting booking history and reports.
 *
 * Extends RuntimeException (unchecked) for clean integration into the
 * booking flow without forcing checked exception declarations.
 *
 * @author GARV RAJ
 * @version 9.0
 */
public class InvalidGuestNameException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive failure message.
     *
     * @param message explanation of why the guest name was rejected
     */
    public InvalidGuestNameException(String message) {
        super(message);
    }
}