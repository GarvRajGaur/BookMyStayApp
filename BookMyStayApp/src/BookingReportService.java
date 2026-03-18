import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BookingReportService
 *
 * Use Case 8: Booking History & Reporting
 *
 * Generates administrative reports and summaries from stored
 * BookingHistory data. All operations are strictly read-only —
 * no records are added, removed, or modified during reporting.
 *
 * Separation of Data Storage and Reporting:
 * BookingHistory is responsible only for storing records.
 * BookingReportService is responsible only for analyzing and
 * presenting them. This single-responsibility design means
 * new report types can be added here without touching history storage.
 *
 * Reports available:
 *   1. Full booking history (chronological)
 *   2. Total bookings summary
 *   3. Bookings grouped by room type
 *   4. Guest lookup by name
 *
 * @author GARV RAJ
 * @version 8.0
 */
public class BookingReportService {

    private BookingHistory bookingHistory;

    /**
     * Constructs the report service with a reference to booking history.
     * Only read access is used — history is never modified here.
     *
     * @param bookingHistory the booking history to generate reports from
     */
    public BookingReportService(BookingHistory bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    /**
     * Report 1: Full Booking History
     *
     * Displays all confirmed bookings in the order they were confirmed.
     * Delegates display to BookingHistory — no re-sorting or re-ordering.
     */
    public void generateFullHistoryReport() {
        System.out.println("==================================================");
        System.out.println("  REPORT 1 : Full Booking History                ");
        System.out.println("==================================================");
        bookingHistory.displayHistory();
        System.out.println("  Total Confirmed Bookings: "
                + bookingHistory.getTotalBookings());
        System.out.println("==================================================");
    }

    /**
     * Report 2: Bookings Grouped by Room Type
     *
     * Counts how many bookings exist per room type.
     * Uses a HashMap to accumulate counts — O(n) single pass over history.
     * Read-only — history is accessed via getHistory() (unmodifiable list).
     */
    public void generateRoomTypeSummaryReport() {
        System.out.println("==================================================");
        System.out.println("  REPORT 2 : Bookings by Room Type               ");
        System.out.println("==================================================");

        List<BookingRecord> records = bookingHistory.getHistory();
        if (records.isEmpty()) {
            System.out.println("  No bookings to report.");
            System.out.println("==================================================");
            return;
        }

        // Accumulate counts per room type in one pass
        Map<String, Integer> typeCounts = new HashMap<>();
        for (BookingRecord record : records) {
            String type = record.getRoomType();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }

        // Display grouped summary
        System.out.println("  Room Type      | Bookings Confirmed");
        System.out.println("  " + "-".repeat(35));
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            System.out.printf("  %-16s | %d%n", entry.getKey(), entry.getValue());
        }
        System.out.println("==================================================");
    }

    /**
     * Report 3: Guest Lookup
     *
     * Searches the booking history for all reservations matching
     * a given guest name (case-insensitive). Useful for customer
     * service and issue resolution.
     *
     * Read-only — no records are modified during the search.
     *
     * @param guestName the guest name to search for
     */
    public void generateGuestLookupReport(String guestName) {
        System.out.println("==================================================");
        System.out.println("  REPORT 3 : Guest Lookup — \"" + guestName + "\"");
        System.out.println("==================================================");

        List<BookingRecord> records = bookingHistory.getHistory();
        boolean found = false;

        for (BookingRecord record : records) {
            if (record.getGuestName().equalsIgnoreCase(guestName)) {
                System.out.println(record);
                found = true;
            }
        }

        if (!found) {
            System.out.println("  No bookings found for guest: " + guestName);
        }
        System.out.println("==================================================");
    }

    /**
     * Report 4: Admin Summary Dashboard
     *
     * Provides a high-level overview of overall booking activity.
     * Combines total count with a per-type breakdown in one report.
     */
    public void generateAdminSummaryReport() {
        System.out.println("==================================================");
        System.out.println("  REPORT 4 : Admin Summary Dashboard             ");
        System.out.println("==================================================");

        List<BookingRecord> records = bookingHistory.getHistory();
        int total = bookingHistory.getTotalBookings();

        System.out.println("  Total Bookings Confirmed : " + total);

        if (total == 0) {
            System.out.println("  No booking data available.");
            System.out.println("==================================================");
            return;
        }

        // Count per room type
        Map<String, Integer> typeCounts = new HashMap<>();
        for (BookingRecord record : records) {
            String type = record.getRoomType();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }

        System.out.println("  ------------------------------------------");
        System.out.println("  Room Type Breakdown:");
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / total;
            System.out.printf("    %-10s : %d booking(s)  (%.1f%%)%n",
                    entry.getKey(), entry.getValue(), percentage);
        }

        System.out.println("  ------------------------------------------");
        System.out.println("  First Booking : " + records.get(0).getGuestName()
                + " (" + records.get(0).getRoomId() + ")");
        System.out.println("  Last Booking  : " + records.get(total - 1).getGuestName()
                + " (" + records.get(total - 1).getRoomId() + ")");
        System.out.println("==================================================");
    }
}