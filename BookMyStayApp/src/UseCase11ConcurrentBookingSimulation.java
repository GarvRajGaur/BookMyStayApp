import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * MAIN CLASS UseCase11ConcurrentBookingSimulation
 *
 * Use Case 11: Concurrent Booking Simulation (Thread Safety)
 *
 * Description:
 * This class simulates multiple guests submitting booking requests
 * simultaneously, demonstrating how synchronized data structures
 * prevent race conditions, double allocation, and inventory corruption
 * under concurrent load.
 *
 * New classes introduced:
 *   - SharedBookingQueue:     synchronized Queue<Reservation> — safe multi-thread enqueue/dequeue
 *   - SynchronizedInventory:  atomic checkAndAllocate() — prevents double-booking
 *   - BookingThread:          Runnable representing one guest's concurrent booking attempt
 *
 * Two Simulations Run:
 *
 *   Simulation A — UNSYNCHRONIZED (demonstrates the problem):
 *   A plain HashMap inventory is updated from multiple threads without
 *   any locking. Race conditions cause inventory to go negative or allow
 *   more bookings than rooms available.
 *
 *   Simulation B — SYNCHRONIZED (demonstrates the solution):
 *   SynchronizedInventory wraps all reads and writes in synchronized blocks.
 *   No matter how threads interleave, final allocation count never exceeds
 *   the available room count. Inventory stays consistent.
 *
 * Thread Model:
 *   - 8 guest threads created simultaneously
 *   - All share ONE SharedBookingQueue and ONE SynchronizedInventory
 *   - Thread.sleep(10ms) inside each thread amplifies the race window
 *   - Thread.join() in main ensures all threads finish before reporting
 *
 * @author GARV RAJ
 * @version 11.0
 */
public class UseCase11ConcurrentBookingSimulation {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=================================================");
        System.out.println("   BookMyStay Hotel Booking System               ");
        System.out.println("   Use Case 11: Concurrent Booking Simulation    ");
        System.out.println("               (Thread Safety)                   ");
        System.out.println("=================================================");
        System.out.println();

        // ---------------------------------------------------------------
        // SIMULATION A: Unsynchronized access — demonstrates race condition
        // ---------------------------------------------------------------
        System.out.println("=================================================");
        System.out.println("SIMULATION A: WITHOUT Synchronization");
        System.out.println("(Demonstrates race condition / inventory corruption)");
        System.out.println("=================================================");
        System.out.println();

        runUnsynchronizedSimulation();

        System.out.println();

        // ---------------------------------------------------------------
        // SIMULATION B: Synchronized access — demonstrates thread safety
        // ---------------------------------------------------------------
        System.out.println("=================================================");
        System.out.println("SIMULATION B: WITH Synchronization");
        System.out.println("(Demonstrates thread-safe allocation)");
        System.out.println("=================================================");
        System.out.println();

        runSynchronizedSimulation();

        System.out.println();
        System.out.println("=================================================");
        System.out.println("Conclusion:");
        System.out.println("  Simulation A: Unsynchronized access allows      ");
        System.out.println("  multiple threads to read stale availability and  ");
        System.out.println("  over-allocate rooms beyond actual inventory.     ");
        System.out.println();
        System.out.println("  Simulation B: Synchronized checkAndAllocate()   ");
        System.out.println("  makes read+write atomic — only one thread can   ");
        System.out.println("  allocate at a time. Inventory never goes below 0.");
        System.out.println("  Confirmed bookings never exceed available rooms. ");
        System.out.println("=================================================");
        System.out.println("Use Case 11 Complete.");
        System.out.println("=================================================");
    }

    // -------------------------------------------------------------------
    // SIMULATION A: Unsynchronized HashMap — shows what CAN go wrong
    // -------------------------------------------------------------------
    private static void runUnsynchronizedSimulation() throws InterruptedException {

        // Plain mutable HashMap — NO synchronization
        final java.util.Map<String, Integer> unsafeInventory
                = new java.util.HashMap<>();
        unsafeInventory.put("Single", 2); // Only 2 Single rooms
        unsafeInventory.put("Double", 1); // Only 1 Double room
        unsafeInventory.put("Suite",  1); // Only 1 Suite room

        System.out.println("Initial Inventory (Unsynchronized):");
        printMap(unsafeInventory);
        System.out.println();

        // 6 guests all request Single rooms — but only 2 are available
        // Without synchronization, multiple threads may all read count=2
        // and all proceed to allocate — resulting in over-allocation
        List<Thread> threads      = new ArrayList<>();
        List<String> unsafeResults = Collections.synchronizedList(new ArrayList<>());

        String[] guests = {
                "Alice", "Bob", "Carol", "David", "Eva", "Frank"
        };

        for (String guest : guests) {
            Thread t = new Thread(() -> {
                // Unsynchronized read — any thread can read stale value
                Integer count = unsafeInventory.get("Single");
                try { Thread.sleep(5); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // Unsynchronized write — multiple threads may decrement simultaneously
                if (count != null && count > 0) {
                    unsafeInventory.put("Single", unsafeInventory.get("Single") - 1);
                    unsafeResults.add("  [ALLOCATED - UNSAFE] " + guest
                            + " → SINGLE room (count was: " + count + ")");
                } else {
                    unsafeResults.add("  [DECLINED  - UNSAFE] " + guest
                            + " → No Single rooms available.");
                }
            });
            threads.add(t);
        }

        // Launch all threads simultaneously
        System.out.println("Launching 6 threads simultaneously for 2 Single rooms...");
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(); // Wait for all to finish

        System.out.println("Thread execution results:");
        for (String result : unsafeResults) {
            System.out.println(result);
        }

        long allocated = unsafeResults.stream()
                .filter(r -> r.contains("ALLOCATED")).count();
        System.out.println();
        System.out.println("  Rooms available    : 2");
        System.out.printf ("  Rooms allocated    : %d%n", allocated);
        System.out.printf ("  Final inventory    : %d (should be 0, may be negative!)%n",
                unsafeInventory.get("Single"));
        if (allocated > 2) {
            System.out.println("  ⚠ RACE CONDITION DETECTED: More rooms allocated than available!");
        } else {
            System.out.println("  (Race condition may not manifest every run — it is timing-dependent)");
        }
    }

    // -------------------------------------------------------------------
    // SIMULATION B: SynchronizedInventory — guaranteed thread safety
    // -------------------------------------------------------------------
    private static void runSynchronizedSimulation() throws InterruptedException {

        // Thread-safe inventory — Single:3, Double:2, Suite:1
        SynchronizedInventory inventory = new SynchronizedInventory(3, 2, 1);

        System.out.println("Initial Inventory (Synchronized):");
        inventory.displayInventory();
        System.out.println();

        // 8 guests submitting requests simultaneously
        // More requests than available rooms to prove no over-allocation
        SharedBookingQueue sharedQueue = new SharedBookingQueue();

        String[][] requests = {
                {"Alice Johnson",  "Single"},
                {"Bob Smith",      "Double"},
                {"Carol White",    "Suite"},
                {"David Brown",    "Single"},
                {"Eva Green",      "Double"},
                {"Frank Lee",      "Single"},
                {"Grace Kim",      "Double"}, // will be declined — only 2 Doubles
                {"Henry Ford",     "Single"}  // will be declined — only 3 Singles
        };

        System.out.println("Enqueueing 8 booking requests into shared queue...");
        for (String[] req : requests) {
            sharedQueue.enqueue(new Reservation(req[0], req[1]));
        }
        System.out.println("  Total requests in queue: " + sharedQueue.size());
        System.out.println();

        // Create one thread per request — all start simultaneously
        List<String>  resultLog = Collections.synchronizedList(new ArrayList<>());
        List<Thread>  threads   = new ArrayList<>();

        for (int i = 1; i <= requests.length; i++) {
            BookingThread bookingTask = new BookingThread(
                    "Thread-" + i, sharedQueue, inventory, resultLog);
            threads.add(new Thread(bookingTask));
        }

        // Launch ALL threads at the same time to maximize concurrency
        System.out.println("Launching " + threads.size()
                + " threads simultaneously...");
        System.out.println("--------------------------------------------------");
        for (Thread t : threads) t.start();

        // Main thread waits for ALL booking threads to complete
        for (Thread t : threads) t.join();

        // Display results in order they were logged
        System.out.println("Thread execution results:");
        for (String result : resultLog) {
            System.out.println(result);
        }

        // Count confirmed vs declined
        long confirmed = resultLog.stream()
                .filter(r -> r.contains("[CONFIRMED]")).count();
        long declined  = resultLog.stream()
                .filter(r -> r.contains("[DECLINED]")).count();

        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println("Post-Simulation Inventory (Synchronized):");
        inventory.displayInventory();
        System.out.println();

        // Verify allocations never exceeded inventory
        System.out.println("--------------------------------------------------");
        System.out.println("Results Summary:");
        System.out.println("  Total requests  : " + requests.length);
        System.out.printf ("  Confirmed       : %d%n", confirmed);
        System.out.printf ("  Declined        : %d%n", declined);
        System.out.println("  Expected max confirmations: 6 (3 Single + 2 Double + 1 Suite)");

        Map<String, Integer> snapshot = inventory.getSnapshot();
        boolean inventoryValid = snapshot.values().stream()
                .allMatch(v -> v >= 0);
        System.out.println("  Inventory non-negative : " + inventoryValid);
        System.out.println("  Double-booking prevented: "
                + (confirmed <= 6 ? "YES ✓" : "NO ✗ — race condition!"));
        System.out.println("--------------------------------------------------");
    }

    /** Helper to print a plain map for Simulation A display */
    private static void printMap(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            System.out.println("    " + e.getKey() + " Rooms : " + e.getValue());
        }
    }
}