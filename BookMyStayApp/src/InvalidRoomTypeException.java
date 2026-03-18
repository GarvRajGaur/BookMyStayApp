/**
 * InvalidRoomTypeException
 *
 * Use Case 9: Error Handling & Validation
 *
 * Thrown when a guest or system provides a room type that does
 * not exist in the hotel's catalog (e.g., "Penthouse", "studio",
 * or a blank string).
 *
 * Why a custom exception?
 * Using a generic RuntimeException or IllegalArgumentException would
 * hide the domain context. A named exception makes the failure cause
 * immediately clear in stack traces, logs, and catch blocks —
 * no message parsing required to understand what went wrong.
 *
 * Extends RuntimeException (unchecked) so callers are not forced
 * to declare it in method signatures, keeping booking flow readable.
 *
 * @author GARV RAJ
 * @version 9.0
 */
public class InvalidRoomTypeException extends RuntimeException {

    private final String invalidRoomType;

    /**
     * Constructs the exception with the offending room type embedded
     * in a descriptive message.
     *
     * @param roomType the invalid room type string that triggered the error
     */
    public InvalidRoomTypeException(String roomType) {
        super("Invalid room type provided: \"" + roomType
                + "\". Accepted types are: Single, Double, Suite.");
        this.invalidRoomType = roomType;
    }

    /**
     * Returns the invalid room type string that triggered this exception.
     * Useful for logging and diagnostics.
     *
     * @return the invalid room type value
     */
    public String getInvalidRoomType() {
        return invalidRoomType;
    }
}