import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingHistoryPersistenceService
 *
 * Use Case 12: Data Persistence & System Recovery
 *
 * Handles serialization and deserialization of booking history records
 * to and from a plain-text file.
 *
 * Serialization Format (booking_history.txt):
 * ─────────────────────────────────────────────────────────────
 * # BookMyStay Persistence File
 * VERSION:12.0
 * 1|Alice Johnson|Single|SINGLE-101
 * 2|Bob Smith|Double|DOUBLE-201
 * 3|Carol White|Suite|SUITE-301
 * ─────────────────────────────────────────────────────────────
 *
 * Each line represents one BookingRecord:
 *   Field 1 — booking index (sequence number)
 *   Field 2 — guest name
 *   Field 3 — room type
 *   Field 4 — allocated room ID
 *
 * This format maps directly to a database table row:
 *   booking_id | guest_name | room_type | room_id
 * Conceptually preparing learners for JDBC/ORM integration.
 *
 * Failure Tolerance:
 * Returns an empty list (not null) if the file is missing or corrupt.
 * An empty list is a valid, safe starting state for booking history.
 *
 * @author GARV RAJ
 * @version 12.0
 */
public class BookingHistoryPersistenceService {

    /**
     * Serializes the full booking history list to a text file.
     *
     * Each BookingRecord is written as one pipe-delimited line.
     * Records are written in their original insertion order (List order).
     *
     * @param history the BookingHistory object whose records to persist
     */
    public void saveBookingHistory(BookingHistory history) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(PersistenceConstants.BOOKING_HISTORY_FILE))) {

            // Write header and version
            writer.write(PersistenceConstants.FILE_HEADER);
            writer.newLine();
            writer.write(PersistenceConstants.VERSION_TAG);
            writer.newLine();

            // Serialize each BookingRecord as "index|guest|type|roomId"
            for (BookingRecord record : history.getHistory()) {
                writer.write(
                        record.getBookingIndex()
                                + PersistenceConstants.FIELD_DELIMITER
                                + record.getGuestName()
                                + PersistenceConstants.FIELD_DELIMITER
                                + record.getRoomType()
                                + PersistenceConstants.FIELD_DELIMITER
                                + record.getRoomId()
                );
                writer.newLine();
            }

            System.out.println("  [SAVED]    Booking history written to: "
                    + PersistenceConstants.BOOKING_HISTORY_FILE
                    + " (" + history.getTotalBookings() + " records)");

        } catch (IOException e) {
            System.out.println("  [ERROR]    Failed to save booking history: "
                    + e.getMessage());
        }
    }

    /**
     * Deserializes booking history from the persistence file.
     *
     * Parses each pipe-delimited line back into a BookingRecord and
     * returns the full list in original confirmation order.
     *
     * Failure Tolerance:
     * Returns an empty list if the file is missing or a line is malformed.
     * Malformed lines are skipped with a warning — partial recovery is
     * better than a full crash.
     *
     * @return list of recovered BookingRecord objects (empty if none found)
     */
    public List<BookingRecord> loadBookingHistory() {
        List<BookingRecord> recovered = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(PersistenceConstants.BOOKING_HISTORY_FILE))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip header, version, and blank lines
                if (line.isEmpty()
                        || line.startsWith("#")
                        || line.startsWith("VERSION")) {
                    continue;
                }

                // Parse "index|guest|type|roomId" record
                String[] parts = line.split(
                        PersistenceConstants.FIELD_DELIMITER_REGEX);
                if (parts.length == 4) {
                    int    index    = Integer.parseInt(parts[0].trim());
                    String guest    = parts[1].trim();
                    String roomType = parts[2].trim();
                    String roomId   = parts[3].trim();
                    recovered.add(new BookingRecord(index, guest, roomType, roomId));
                } else {
                    System.out.println("  [WARN]     Skipping malformed record: " + line);
                }
            }

            System.out.println("  [LOADED]   Booking history restored from: "
                    + PersistenceConstants.BOOKING_HISTORY_FILE
                    + " (" + recovered.size() + " records)");

        } catch (java.io.FileNotFoundException e) {
            System.out.println("  [WARN]     Booking history file not found — "
                    + "starting with empty history.");

        } catch (IOException | NumberFormatException e) {
            System.out.println("  [ERROR]    Failed to load booking history: "
                    + e.getMessage() + " — starting with empty history.");
        }

        return recovered;
    }
}