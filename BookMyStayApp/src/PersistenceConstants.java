/**
 * PersistenceConstants
 *
 * Use Case 12: Data Persistence & System Recovery
 *
 * Central location for all file paths and format constants used
 * by the persistence layer. Keeping these in one place means
 * file paths never get scattered across multiple classes — a
 * single change here updates the entire persistence layer.
 *
 * @author GARV RAJ
 * @version 12.0
 */
public class PersistenceConstants {

    /** File that stores the room inventory snapshot */
    public static final String INVENTORY_FILE     = "inventory_state.txt";

    /** File that stores the full booking history */
    public static final String BOOKING_HISTORY_FILE = "booking_history.txt";

    /** Delimiter used to separate fields within a single record line */
    public static final String FIELD_DELIMITER    = "|";

    /** Regex-safe version of the delimiter for String.split() */
    public static final String FIELD_DELIMITER_REGEX = "\\|";

    /** Header line written at the top of every persisted file */
    public static final String FILE_HEADER        = "# BookMyStay Persistence File";

    /** Version tag written in every file for future compatibility checks */
    public static final String VERSION_TAG        = "VERSION:12.0";

    // Private constructor — this is a constants-only utility class
    private PersistenceConstants() {}
}