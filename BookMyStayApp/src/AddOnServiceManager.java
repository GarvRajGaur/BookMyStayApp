import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AddOnServiceManager
 *
 * Use Case 7: Add-On Service Selection
 *
 * Manages the one-to-many relationship between reservations and
 * their selected optional services using:
 *
 *   Map<String, List<AddOnService>>
 *   └── Key:   Reservation ID  (e.g., "SINGLE-101")
 *   └── Value: List of AddOnService objects selected for that reservation
 *
 * Why Map<String, List<AddOnService>>?
 * - Map provides O(1) lookup of services for any reservation ID
 * - List preserves the order in which services were selected
 * - List allows a guest to select multiple services (one-to-many)
 * - This combination cleanly models the real-world relationship:
 *   one booking → many optional extras
 *
 * Separation of Concerns:
 * This class has NO reference to RoomInventory, BookingRequestQueue,
 * or AllocationTracker. Add-on selection is entirely independent of
 * core booking and allocation logic — optional features never
 * interfere with critical workflows.
 *
 * @author GARV RAJ
 * @version 7.0
 */
public class AddOnServiceManager {

    // One-to-many: one reservation ID maps to a list of selected services
    private Map<String, List<AddOnService>> serviceMap;

    /**
     * Initializes the manager with an empty service map.
     */
    public AddOnServiceManager() {
        serviceMap = new HashMap<>();
    }

    /**
     * Attaches a single add-on service to the specified reservation.
     *
     * If the reservation ID has no services yet, a new List is created
     * automatically (lazy initialization). Multiple calls for the same
     * reservation ID accumulate services in the order they were added.
     *
     * Core booking and inventory state are NOT touched here.
     *
     * @param reservationId the unique room ID assigned during allocation
     *                      (e.g., "SINGLE-101", "DOUBLE-201")
     * @param service       the AddOnService to attach
     */
    public void addService(String reservationId, AddOnService service) {
        // computeIfAbsent creates a new ArrayList if key is not yet present
        serviceMap.computeIfAbsent(reservationId, k -> new ArrayList<>()).add(service);
        System.out.println("  Service added to " + reservationId
                + " --> " + service.getServiceName()
                + " ($" + service.getServiceCost() + ")");
    }

    /**
     * Calculates the total additional cost of all services selected
     * for a given reservation.
     *
     * Returns 0.0 if the reservation has no services attached.
     *
     * @param reservationId the reservation to calculate cost for
     * @return total add-on cost as a double
     */
    public double calculateTotalCost(String reservationId) {
        List<AddOnService> services = serviceMap.get(reservationId);
        if (services == null || services.isEmpty()) return 0.0;

        double total = 0.0;
        for (AddOnService s : services) {
            total += s.getServiceCost();
        }
        return total;
    }

    /**
     * Displays all services selected for a specific reservation,
     * along with the total additional cost.
     *
     * Read-only — does not modify any service or reservation state.
     *
     * @param reservationId the reservation to display services for
     */
    public void displayServicesForReservation(String reservationId) {
        List<AddOnService> services = serviceMap.get(reservationId);
        System.out.println("  Reservation ID : " + reservationId);

        if (services == null || services.isEmpty()) {
            System.out.println("  No add-on services selected.");
            return;
        }

        for (AddOnService s : services) {
            System.out.println("    - " + s.getServiceName() + " : $" + s.getServiceCost());
        }
        System.out.printf("  Total Add-On Cost : $%.2f%n", calculateTotalCost(reservationId));
    }

    /**
     * Displays a full summary report of all reservations and their
     * associated services across the entire system.
     *
     * Read-only — does not modify state.
     */
    public void displayAllServices() {
        if (serviceMap.isEmpty()) {
            System.out.println("No add-on services have been selected.");
            return;
        }

        System.out.println("Add-On Services Summary (All Reservations):");
        System.out.println("--------------------------------------------------");
        for (Map.Entry<String, List<AddOnService>> entry : serviceMap.entrySet()) {
            displayServicesForReservation(entry.getKey());
            System.out.println();
        }
    }

    /**
     * Returns the list of services attached to a reservation.
     * Returns an empty list if none are found.
     *
     * @param reservationId the reservation to look up
     * @return list of selected AddOnService objects
     */
    public List<AddOnService> getServicesForReservation(String reservationId) {
        return serviceMap.getOrDefault(reservationId, new ArrayList<>());
    }

    /**
     * Returns the total number of reservations that have at least
     * one add-on service attached.
     *
     * @return count of reservations with services
     */
    public int getTotalReservationsWithServices() {
        return serviceMap.size();
    }
}