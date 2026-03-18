/**
 * MAIN CLASS UseCase8BookingHistoryReport
 *
 * Use Case 8: Booking History & Reporting
 *
 * Description:
 * This class extends Use Case 6 & 7 by introducing persistent (in-memory)
 * booking history and a report generation service for administrative use.
 *
 * New classes introduced:
 *   - BookingRecord:        immutable snapshot of one confirmed booking
 *   - BookingHistory:       List<BookingRecord> — ordered, append-only audit trail
 *   - BookingReportService: read-only reporting from stored booking data
 *
 * Updated class:
 *   - BookingService:       now accepts optional BookingHistory to record
 *                           each confirmed reservation immediately (UC8 hook)
 *
 * Scenario Demonstrated:
 * 1. Full allocation flow (UC6) runs with history recording enabled
 * 2. Add-on services are attached to confirmed reservations (UC7)
 * 3. Admin requests four different reports:
 *       Report 1 — Full chronological booking history
 *       Report 2 — Bookings grouped by room type
 *       Report 3 — Guest lookup by name
 *       Report 4 — Admin summary dashboard with percentage breakdown
 * 4. History and inventory are verified unchanged after all reports
 *
 * @author GARV RAJ
 * @version 8.0
 */
public class UseCase8BookingHistoryReport {

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 8: Booking History & Reporting       ");
        System.out.println("=================================================");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 1: Initialize core components
        // ---------------------------------------------------------------
        RoomInventory inventory = new RoomInventory();
        inventory.updateAvailability("Single", -3); // 5 → 2
        inventory.updateAvailability("Double", -2); // 3 → 1
        inventory.updateAvailability("Suite",  -1); // 2 → 1

        // UC8: Initialize BookingHistory — List<BookingRecord>
        BookingHistory bookingHistory = new BookingHistory();

        // ---------------------------------------------------------------
        // Step 2: Queue booking requests (UC5)
        // ---------------------------------------------------------------
        BookingRequestQueue requestQueue = new BookingRequestQueue();
        requestQueue.addRequest(new Reservation("Alice Johnson", "Single"));
        requestQueue.addRequest(new Reservation("Bob Smith",    "Double"));
        requestQueue.addRequest(new Reservation("Carol White",  "Suite"));
        requestQueue.addRequest(new Reservation("David Brown",  "Single"));
        requestQueue.addRequest(new Reservation("Eva Green",    "Double")); // will be declined

        System.out.println();

        // ---------------------------------------------------------------
        // Step 3: Allocate rooms (UC6) — with history recording enabled
        // BookingService now takes BookingHistory as 4th argument
        // Each confirmed booking is appended to history inside processSingleRequest()
        // ---------------------------------------------------------------
        AllocationTracker tracker = new AllocationTracker();
        BookingService bookingService = new BookingService(
                requestQueue, inventory, tracker, bookingHistory);

        System.out.println("--------------------------------------------------");
        System.out.println("Running Allocation with History Recording...");
        System.out.println("--------------------------------------------------");
        bookingService.processAllRequests();
        System.out.println();

        // ---------------------------------------------------------------
        // Step 4: Attach add-on services (UC7)
        // ---------------------------------------------------------------
        AddOnService breakfast       = new AddOnService("Breakfast",       15.00);
        AddOnService airportTransfer = new AddOnService("Airport Transfer", 30.00);
        AddOnService spaAccess       = new AddOnService("Spa Access",       50.00);
        AddOnService lateCheckout    = new AddOnService("Late Checkout",    20.00);
        AddOnService extraBed        = new AddOnService("Extra Bed",        25.00);

        AddOnServiceManager serviceManager = new AddOnServiceManager();
        System.out.println("--------------------------------------------------");
        System.out.println("Attaching Add-On Services (UC7)...");
        System.out.println("--------------------------------------------------");
        serviceManager.addService("SINGLE-101", breakfast);
        serviceManager.addService("SINGLE-101", airportTransfer);
        serviceManager.addService("DOUBLE-201", breakfast);
        serviceManager.addService("DOUBLE-201", spaAccess);
        serviceManager.addService("DOUBLE-201", lateCheckout);
        serviceManager.addService("SUITE-301",  breakfast);
        serviceManager.addService("SUITE-301",  airportTransfer);
        serviceManager.addService("SUITE-301",  spaAccess);
        serviceManager.addService("SUITE-301",  lateCheckout);
        serviceManager.addService("SUITE-301",  extraBed);
        System.out.println();

        // ---------------------------------------------------------------
        // Step 5: Admin requests reports via BookingReportService
        // All reports are READ-ONLY — history is never modified
        // ---------------------------------------------------------------
        BookingReportService reportService = new BookingReportService(bookingHistory);

        System.out.println();
        System.out.println("*** ADMIN REPORTING PANEL ***");
        System.out.println();

        // Report 1: Full chronological history
        reportService.generateFullHistoryReport();
        System.out.println();

        // Report 2: Bookings grouped by room type
        reportService.generateRoomTypeSummaryReport();
        System.out.println();

        // Report 3: Guest lookup
        reportService.generateGuestLookupReport("Carol White");
        System.out.println();

        // Report 4: Admin summary dashboard
        reportService.generateAdminSummaryReport();
        System.out.println();

        // ---------------------------------------------------------------
        // Step 6: Verify history size unchanged after reporting
        // ---------------------------------------------------------------
        System.out.println("==================================================");
        System.out.println("Post-Report Verification:");
        System.out.println("  History size : " + bookingHistory.getTotalBookings()
                + " records (unchanged after reporting)");
        System.out.println("  Inventory    :");
        inventory.displayInventory();
        System.out.println("--------------------------------------------------");
        System.out.println("Use Case 8 Complete.");
        System.out.println("Booking history preserved. Reports generated.");
        System.out.println("=================================================");
    }
}