/**
 * MAIN CLASS UseCase9ErrorHandlingValidation
 *
 * Use Case 9: Error Handling & Validation
 *
 * Description:
 * This class demonstrates structured validation and custom exception
 * handling applied to the booking system. All invalid inputs are caught
 * early (fail-fast), a clear error message is displayed, and the system
 * continues running — no crash, no corrupted state.
 *
 * New classes introduced:
 *   - InvalidGuestNameException  : thrown for null/blank guest names
 *   - InvalidRoomTypeException   : thrown for unrecognised room types
 *   - RoomNotAvailableException  : thrown when inventory is exhausted
 *   - BookingValidator           : central guard — validates before any state change
 *
 * Scenarios Demonstrated:
 *   Scenario 1 — Valid booking                   → CONFIRMED
 *   Scenario 2 — Null guest name                 → InvalidGuestNameException
 *   Scenario 3 — Blank guest name (spaces only)  → InvalidGuestNameException
 *   Scenario 4 — Invalid room type ("studio")    → InvalidRoomTypeException
 *   Scenario 5 — Wrong case room type ("single") → InvalidRoomTypeException
 *   Scenario 6 — Room type fully booked          → RoomNotAvailableException
 *   Scenario 7 — Valid booking after failures    → CONFIRMED (system still stable)
 *
 * @author GARV RAJ
 * @version 9.0
 */
public class UseCase9ErrorHandlingValidation {

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 9: Error Handling & Validation       ");
        System.out.println("=================================================");
        System.out.println();

        // ---------------------------------------------------------------
        // Initialize core system components
        // ---------------------------------------------------------------
        RoomInventory inventory = new RoomInventory();
        // Set controlled stock: 1 Single, 1 Double, 0 Suite (fully booked)
        inventory.updateAvailability("Single", -4); // 5 → 1
        inventory.updateAvailability("Double", -2); // 3 → 1
        inventory.updateAvailability("Suite",  -2); // 2 → 0

        System.out.println();
        System.out.println("Initial Inventory State:");
        inventory.displayInventory();
        System.out.println();

        BookingValidator      validator     = new BookingValidator();
        AllocationTracker     tracker       = new AllocationTracker();
        BookingHistory        history       = new BookingHistory();

        System.out.println("=================================================");
        System.out.println("Running Validation Scenarios...");
        System.out.println("=================================================");

        // ---------------------------------------------------------------
        // Scenario 1: Valid booking — should pass all validations
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 1: Valid booking request");
        System.out.println("--------------------------------------------------");
        try {
            validator.validateBookingRequest("Alice Johnson", "Single", inventory);
            System.out.println("  [VALIDATION PASSED] Guest: Alice Johnson | Room Type: Single");
            inventory.updateAvailability("Single", -1);
            history.addRecord("Alice Johnson", "Single", "SINGLE-101");
            tracker.allocate("Single", "SINGLE-101");
            System.out.println("  [CONFIRMED] Assigned Room ID: SINGLE-101");
        } catch (InvalidGuestNameException | InvalidRoomTypeException
                 | RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Scenario 2: Null guest name
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 2: Null guest name");
        System.out.println("--------------------------------------------------");
        try {
            validator.validateBookingRequest(null, "Double", inventory);
            System.out.println("  [CONFIRMED] This line should NOT be reached.");
        } catch (InvalidGuestNameException e) {
            System.out.println("  [CAUGHT InvalidGuestNameException]");
            System.out.println("  " + e.getMessage());
        } catch (InvalidRoomTypeException | RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Scenario 3: Blank guest name (whitespace only)
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 3: Blank guest name (spaces only)");
        System.out.println("--------------------------------------------------");
        try {
            validator.validateBookingRequest("     ", "Double", inventory);
            System.out.println("  [CONFIRMED] This line should NOT be reached.");
        } catch (InvalidGuestNameException e) {
            System.out.println("  [CAUGHT InvalidGuestNameException]");
            System.out.println("  " + e.getMessage());
        } catch (InvalidRoomTypeException | RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Scenario 4: Completely invalid room type
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 4: Invalid room type (\"studio\")");
        System.out.println("--------------------------------------------------");
        try {
            validator.validateBookingRequest("Bob Smith", "studio", inventory);
            System.out.println("  [CONFIRMED] This line should NOT be reached.");
        } catch (InvalidGuestNameException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        } catch (InvalidRoomTypeException e) {
            System.out.println("  [CAUGHT InvalidRoomTypeException]");
            System.out.println("  " + e.getMessage());
        } catch (RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Scenario 5: Wrong case room type — "single" instead of "Single"
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 5: Wrong case room type (\"single\" instead of \"Single\")");
        System.out.println("--------------------------------------------------");
        try {
            validator.validateBookingRequest("Carol White", "single", inventory);
            System.out.println("  [CONFIRMED] This line should NOT be reached.");
        } catch (InvalidGuestNameException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        } catch (InvalidRoomTypeException e) {
            System.out.println("  [CAUGHT InvalidRoomTypeException]");
            System.out.println("  " + e.getMessage());
            System.out.println("  Note: Room type validation is CASE-SENSITIVE.");
        } catch (RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Scenario 6: Valid type but fully booked (Suite = 0)
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 6: Room type fully booked (Suite = 0 available)");
        System.out.println("--------------------------------------------------");
        try {
            validator.validateBookingRequest("David Brown", "Suite", inventory);
            System.out.println("  [CONFIRMED] This line should NOT be reached.");
        } catch (InvalidGuestNameException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        } catch (InvalidRoomTypeException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        } catch (RoomNotAvailableException e) {
            System.out.println("  [CAUGHT RoomNotAvailableException]");
            System.out.println("  " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Scenario 7: Valid booking after all failures — system still stable
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("Scenario 7: Valid booking after all failures (system stability check)");
        System.out.println("--------------------------------------------------");
        try {
            validator.validateBookingRequest("Eva Green", "Double", inventory);
            System.out.println("  [VALIDATION PASSED] Guest: Eva Green | Room Type: Double");
            inventory.updateAvailability("Double", -1);
            history.addRecord("Eva Green", "Double", "DOUBLE-201");
            tracker.allocate("Double", "DOUBLE-201");
            System.out.println("  [CONFIRMED] Assigned Room ID: DOUBLE-201");
        } catch (InvalidGuestNameException | InvalidRoomTypeException
                 | RoomNotAvailableException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }

        // ---------------------------------------------------------------
        // Final state — inventory and history unchanged by failed attempts
        // ---------------------------------------------------------------
        System.out.println();
        System.out.println("=================================================");
        System.out.println("Final System State After All Scenarios:");
        System.out.println("=================================================");
        System.out.println();
        System.out.println("Inventory (only valid bookings affected it):");
        inventory.displayInventory();
        System.out.println();

        System.out.println("Booking History (only confirmed bookings recorded):");
        BookingReportService reportService = new BookingReportService(history);
        reportService.generateFullHistoryReport();

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("Observation:");
        System.out.println("  Scenarios 2-6 threw exceptions — zero state change.");
        System.out.println("  Scenarios 1 & 7 passed validation — confirmed & recorded.");
        System.out.println("  System remained stable throughout all error scenarios.");
        System.out.println("--------------------------------------------------");
        System.out.println("Use Case 9 Complete.");
        System.out.println("=================================================");
    }
}