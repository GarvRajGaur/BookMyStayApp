import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BookingValidator
 *
 * Use Case 9: Error Handling & Validation
 *
 * Central validation guard that enforces all booking rules BEFORE
 * any state change (inventory update, room allocation, history record)
 * is allowed to proceed.
 *
 * Fail-Fast Design:
 * Each validation method throws immediately upon detecting an error.
 * Processing halts at the first violation — no partial state is written,
 * no cascading failures occur, and the system remains consistent.
 *
 * Validations performed:
 *   1. Guest name — must not be null, empty, or blank
 *   2. Room type  — must match exactly one of the accepted types
 *                   (case-sensitive: "Single" not "single")
 *   3. Availability — inventory count must be > 0 for the requested type
 *
 * Guarding System State:
 * Validation happens before inventory.updateAvailability() is ever called.
 * This ensures availability counts can never go negative due to bad input.
 *
 * @author GARV RAJ
 * @version 9.0
 */
public class BookingValidator {

    // Valid room types — case-sensitive, matches Room domain model exactly
    private static final Set<String> VALID_ROOM_TYPES = new HashSet<>(
            Arrays.asList("Single", "Double", "Suite")
    );

    /**
     * Validates the guest name.
     *
     * Rules:
     * - Must not be null
     * - Must not be empty string
     * - Must not be blank (whitespace only)
     *
     * @param guestName the guest name to validate
     * @throws InvalidGuestNameException if the name fails any rule
     */
    public void validateGuestName(String guestName) {
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new InvalidGuestNameException(
                    "Guest name must not be null or blank. "
                            + "Received: \"" + guestName + "\"");
        }
    }

    /**
     * Validates the room type string against the accepted catalog.
     *
     * Note: Validation is CASE-SENSITIVE.
     * "Single" is valid. "single", "SINGLE", "sinGle" are all invalid.
     * This mirrors real system behavior where room types are controlled
     * vocabulary, not free-text fields.
     *
     * @param roomType the room type string to validate
     * @throws InvalidRoomTypeException if the type is not in the accepted set
     */
    public void validateRoomType(String roomType) {
        if (roomType == null || !VALID_ROOM_TYPES.contains(roomType)) {
            throw new InvalidRoomTypeException(roomType);
        }
    }

    /**
     * Validates that inventory has at least one room available for the type.
     *
     * Called AFTER validateRoomType() — assumes the type is already confirmed valid.
     * Reads inventory via the unmodifiable map (read-only access, UC4 pattern).
     *
     * @param roomType    the room type being requested
     * @param inventory   the centralized room inventory to check
     * @throws RoomNotAvailableException if inventory count is zero or missing
     */
    public void validateAvailability(String roomType, RoomInventory inventory) {
        Map<String, Integer> availabilityMap = inventory.getAvailabilityMap();
        Integer count = availabilityMap.get(roomType);
        if (count == null || count <= 0) {
            throw new RoomNotAvailableException(roomType);
        }
    }

    /**
     * Convenience method: runs all three validations in sequence.
     *
     * Order matters — guest name is checked first, then room type,
     * then availability. This ensures the most fundamental checks
     * fail before the more expensive inventory lookup.
     *
     * @param guestName the guest name to validate
     * @param roomType  the room type to validate
     * @param inventory the inventory to check availability against
     * @throws InvalidGuestNameException if guest name is invalid
     * @throws InvalidRoomTypeException  if room type is not recognised
     * @throws RoomNotAvailableException if no rooms of that type remain
     */
    public void validateBookingRequest(String guestName,
                                       String roomType,
                                       RoomInventory inventory) {
        validateGuestName(guestName);
        validateRoomType(roomType);
        validateAvailability(roomType, inventory);
    }
}