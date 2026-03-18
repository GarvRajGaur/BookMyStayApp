import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * InventoryPersistenceService
 *
 * Use Case 12: Data Persistence & System Recovery
 *
 * Handles serialization and deserialization of room inventory state
 * to and from a plain-text file.
 *
 * Serialization Format (inventory_state.txt):
 * ─────────────────────────────────────────────
 * # BookMyStay Persistence File
 * VERSION:12.0
 * Single|2
 * Double|1
 * Suite|0
 * ─────────────────────────────────────────────
 *
 * Why plain text over Java Serialization (.ser files)?
 * - Human-readable — admins can inspect and verify the saved state
 * - No dependency on Java class versions (no serialVersionUID issues)
 * - Easier to migrate to a database format later
 * - Directly maps to SQL INSERT rows conceptually
 *
 * Failure Tolerance:
 * If the file is missing, empty, or corrupt, the service returns
 * a null map and logs a warning. The calling class then initializes
 * a safe default state instead of crashing.
 *
 * @author GARV RAJ
 * @version 12.0
 */
public class InventoryPersistenceService {

    /**
     * Serializes the current inventory availability map to a text file.
     *
     * Each room type is written as one line: "RoomType|Count"
     * A header and version tag are prepended for identification.
     *
     * @param availabilityMap the current inventory map to persist
     */
    public void saveInventory(Map<String, Integer> availabilityMap) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(PersistenceConstants.INVENTORY_FILE))) {

            // Write header and version
            writer.write(PersistenceConstants.FILE_HEADER);
            writer.newLine();
            writer.write(PersistenceConstants.VERSION_TAG);
            writer.newLine();

            // Serialize each room type entry as "Type|Count"
            for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
                writer.write(entry.getKey()
                        + PersistenceConstants.FIELD_DELIMITER
                        + entry.getValue());
                writer.newLine();
            }

            System.out.println("  [SAVED]    Inventory written to: "
                    + PersistenceConstants.INVENTORY_FILE);

        } catch (IOException e) {
            System.out.println("  [ERROR]    Failed to save inventory: " + e.getMessage());
        }
    }

    /**
     * Deserializes inventory state from the persistence file back into memory.
     *
     * Reads each "Type|Count" line and reconstructs the availability map.
     * Lines beginning with '#' (comments) or 'VERSION' are skipped.
     *
     * Failure Tolerance:
     * Returns null if the file does not exist or cannot be parsed.
     * The caller must handle null by initializing a safe default state.
     *
     * @return reconstructed availability map, or null if recovery failed
     */
    public Map<String, Integer> loadInventory() {
        Map<String, Integer> recovered = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(PersistenceConstants.INVENTORY_FILE))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip header lines, version tags, and blank lines
                if (line.isEmpty()
                        || line.startsWith("#")
                        || line.startsWith("VERSION")) {
                    continue;
                }

                // Parse "Type|Count" record
                String[] parts = line.split(
                        PersistenceConstants.FIELD_DELIMITER_REGEX);
                if (parts.length == 2) {
                    String  roomType = parts[0].trim();
                    int     count    = Integer.parseInt(parts[1].trim());
                    recovered.put(roomType, count);
                } else {
                    System.out.println("  [WARN]     Skipping malformed line: " + line);
                }
            }

            System.out.println("  [LOADED]   Inventory restored from: "
                    + PersistenceConstants.INVENTORY_FILE);
            return recovered;

        } catch (java.io.FileNotFoundException e) {
            System.out.println("  [WARN]     Inventory file not found — "
                    + "system will start with defaults.");
            return null;

        } catch (IOException | NumberFormatException e) {
            System.out.println("  [ERROR]    Failed to load inventory: "
                    + e.getMessage() + " — starting with defaults.");
            return null;
        }
    }
}