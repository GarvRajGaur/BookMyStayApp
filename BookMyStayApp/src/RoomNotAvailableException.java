/**
 * RoomNotAvailableException
 *
 * Use Case 9: Error Handling & Validation
 *
 * Thrown when a booking is attempted for a room type whose inventory
 * count has reached zero — i.e., all rooms of that type are fully booked.
 *
 * This is distinct from InvalidRoomTypeException:
 *   - InvalidRoomTypeException → the room TYPE does not exist at all
 *   - RoomNotAvailableException → the type exists but no rooms are left
 *
 * Separating these two failure modes gives callers the ability to
 * respond differently — e.g., suggest an alternative room type for
 * RoomNotAvailableException, but reject the request outright for an
 * invalid type.
 *
 * Extends RuntimeException (unchecked) for clean booking flow integration.
 *
 * @author GARV RAJ
 * @version 9.0
 */
public class RoomNotAvailableException extends RuntimeException {

    private final String roomType;

    /**
     * Constructs the exception for the fully booked room type.
     *
     * @param roomType the room type that has no availability remaining
     */
    public RoomNotAvailableException(String roomType) {
        super("No rooms available for type: \"" + roomType
                + "\". All rooms of this type are currently booked.");
        this.roomType = roomType;
    }

    /**
     * Returns the room type for which availability was exhausted.
     *
     * @return the unavailable room type string
     */
    public String getRoomType() {
        return roomType;
    }
}