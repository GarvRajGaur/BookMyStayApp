/**
 * MAIN CLASS UseCase7AddOnServiceSelection
 *
 * Use Case 7: Add-On Service Selection
 *
 * Description:
 * This class extends the confirmed bookings from Use Case 6 by allowing
 * guests to attach optional services to their reservations. Services are
 * managed independently of core booking and inventory logic, demonstrating
 * clean separation of optional features from critical workflows.
 *
 * New classes introduced:
 *   - AddOnService:        value object representing one optional service
 *   - AddOnServiceManager: manages Map<String, List<AddOnService>> —
 *                          one reservation → many services
 *
 * Scenario Demonstrated:
 * - UC6 allocation is replayed to produce confirmed reservation IDs
 * - Guests select different combinations of add-on services
 * - One guest selects no add-ons (valid scenario — services are optional)
 * - Total add-on cost is calculated per reservation
 * - Core inventory and allocation state remain completely unchanged
 *
 * Key Data Structure:
 *   Map<String, List<AddOnService>>
 *   - Map: O(1) lookup by reservation ID
 *   - List: preserves selection order, allows multiple services per booking
 *
 * @author GARV RAJ
 * @version 7.0
 */
public class UseCase7AddOnServiceSelection {

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 7: Add-On Service Selection          ");
        System.out.println("=================================================");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 1: Replay UC6 allocation to obtain confirmed reservation IDs
        // (In a real system these IDs would come from the BookingService)
        // ---------------------------------------------------------------
        RoomInventory inventory = new RoomInventory();
        inventory.updateAvailability("Single", -3);
        inventory.updateAvailability("Double", -2);
        inventory.updateAvailability("Suite",  -1);

        BookingRequestQueue requestQueue = new BookingRequestQueue();
        requestQueue.addRequest(new Reservation("Alice Johnson", "Single"));
        requestQueue.addRequest(new Reservation("Bob Smith",    "Double"));
        requestQueue.addRequest(new Reservation("Carol White",  "Suite"));
        requestQueue.addRequest(new Reservation("David Brown",  "Single"));
        requestQueue.addRequest(new Reservation("Eva Green",    "Double"));

        AllocationTracker tracker      = new AllocationTracker();
        BookingService    bookingService = new BookingService(requestQueue, inventory, tracker);

        System.out.println("--------------------------------------------------");
        System.out.println("Running Allocation (Use Case 6)...");
        System.out.println("--------------------------------------------------");
        bookingService.processAllRequests();
        System.out.println();

        // Confirmed reservation IDs produced by UC6 allocation:
        // Alice  → SINGLE-101
        // Bob    → DOUBLE-201
        // Carol  → SUITE-301
        // David  → SINGLE-102
        // Eva    → DECLINED (no Double rooms left)

        // ---------------------------------------------------------------
        // Step 2: Define available add-on services
        // New service types can be added here without touching any other class
        // (Open for extension — closed for modification)
        // ---------------------------------------------------------------
        AddOnService breakfast        = new AddOnService("Breakfast",        15.00);
        AddOnService airportTransfer  = new AddOnService("Airport Transfer",  30.00);
        AddOnService spaAccess        = new AddOnService("Spa Access",        50.00);
        AddOnService lateCheckout     = new AddOnService("Late Checkout",     20.00);
        AddOnService extraBed         = new AddOnService("Extra Bed",         25.00);

        // ---------------------------------------------------------------
        // Step 3: Initialize Add-On Service Manager
        // Map<String, List<AddOnService>> — reservation ID → service list
        // ---------------------------------------------------------------
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        System.out.println("--------------------------------------------------");
        System.out.println("Guests Selecting Add-On Services:");
        System.out.println("--------------------------------------------------");
        System.out.println();

        // Alice Johnson — SINGLE-101: Breakfast + Airport Transfer
        System.out.println("Alice Johnson (SINGLE-101) selects:");
        serviceManager.addService("SINGLE-101", breakfast);
        serviceManager.addService("SINGLE-101", airportTransfer);
        System.out.println();

        // Bob Smith — DOUBLE-201: Breakfast + Spa Access + Late Checkout
        System.out.println("Bob Smith (DOUBLE-201) selects:");
        serviceManager.addService("DOUBLE-201", breakfast);
        serviceManager.addService("DOUBLE-201", spaAccess);
        serviceManager.addService("DOUBLE-201", lateCheckout);
        System.out.println();

        // Carol White — SUITE-301: All available services
        System.out.println("Carol White (SUITE-301) selects:");
        serviceManager.addService("SUITE-301", breakfast);
        serviceManager.addService("SUITE-301", airportTransfer);
        serviceManager.addService("SUITE-301", spaAccess);
        serviceManager.addService("SUITE-301", lateCheckout);
        serviceManager.addService("SUITE-301", extraBed);
        System.out.println();

        // David Brown — SINGLE-102: No add-ons (valid — services are optional)
        System.out.println("David Brown (SINGLE-102): No add-on services selected.");
        System.out.println();

        // ---------------------------------------------------------------
        // Step 4: Display per-reservation service summary with cost breakdown
        // ---------------------------------------------------------------
        System.out.println("==================================================");
        System.out.println("Add-On Service Report:");
        System.out.println("==================================================");
        serviceManager.displayAllServices();

        // David's reservation explicitly shown as no services
        System.out.println("  Reservation ID : SINGLE-102");
        System.out.println("  No add-on services selected.");
        System.out.printf("  Total Add-On Cost : $%.2f%n", 0.00);
        System.out.println();

        // ---------------------------------------------------------------
        // Step 5: Verify inventory is unchanged — add-on selection is isolated
        // ---------------------------------------------------------------
        System.out.println("==================================================");
        System.out.println("Inventory State After Add-On Selection:");
        inventory.displayInventory();
        System.out.println("(Inventory unchanged — add-on selection is isolated)");
        System.out.println("--------------------------------------------------");
        System.out.println("Use Case 7 Complete.");
        System.out.println("=================================================");
    }
}