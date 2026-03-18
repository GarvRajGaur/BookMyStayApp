/**
 * AddOnService
 *
 * Use Case 7: Add-On Service Selection
 *
 * Represents a single optional service that a guest can attach
 * to an existing reservation. Each service has a name and a
 * fixed additional cost.
 *
 * Design Note — Composition over Inheritance:
 * AddOnService is a standalone value object that is COMPOSED
 * with a reservation via AddOnServiceManager. It does NOT extend
 * Room or Reservation. This keeps the class hierarchy flat and
 * allows any number of new service types to be added simply by
 * creating new AddOnService instances — no subclassing required.
 *
 * Examples of services:
 *   - Breakfast         ($15.00)
 *   - Airport Transfer  ($30.00)
 *   - Spa Access        ($50.00)
 *   - Late Checkout     ($20.00)
 *   - Extra Bed         ($25.00)
 *
 * @author GARV RAJ
 * @version 7.0
 */
public class AddOnService {

    private String serviceName;
    private double serviceCost;

    /**
     * Constructs an AddOnService with a name and associated cost.
     *
     * @param serviceName the name of the optional service
     * @param serviceCost the additional charge for this service
     */
    public AddOnService(String serviceName, double serviceCost) {
        this.serviceName = serviceName;
        this.serviceCost = serviceCost;
    }

    /**
     * Returns the name of this service.
     *
     * @return service name string
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Returns the cost of this service.
     *
     * @return service cost as a double
     */
    public double getServiceCost() {
        return serviceCost;
    }

    /**
     * Returns a human-readable summary of this service.
     *
     * @return formatted service string
     */
    @Override
    public String toString() {
        return "Service [Name: " + serviceName + ", Cost: $" + serviceCost + "]";
    }
}