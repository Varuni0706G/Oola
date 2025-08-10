import java.util.*;

class Booking {
    int bookingId, customerId, pickupTime, dropTime, amount;
    char from, to;

    public Booking(int bookingId, int customerId, char from, char to, int pickupTime, int dropTime, int amount) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.from = from;
        this.to = to;
        this.pickupTime = pickupTime;
        this.dropTime = dropTime;
        this.amount = amount;
    }
}

class Taxi {
    int id;
    char currentPoint = 'A';
    int totalEarnings = 0;
    double totalCommission = 0;
    List<Booking> bookings = new ArrayList<>();

    public Taxi(int id) {
        this.id = id;
    }

    public boolean isAvailable(int requestTime) {
        if (bookings.isEmpty()) return true;
        Booking lastBooking = bookings.get(bookings.size() - 1);
        return lastBooking.dropTime <= requestTime;
    }

    public int calculateEarnings(char from, char to) {
        int distance = Math.abs(to - from) * 15;
        return 100 + Math.max(0, (distance - 5) * 10);
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        totalEarnings += booking.amount;
        double commission = booking.amount * 0.30;
        totalCommission += commission;
        currentPoint = booking.to;
    }
}

public class Oola {
    static List<Taxi> taxis = new ArrayList<>();
    static Map<Integer, String> customers = new HashMap<>();
    static Map<Integer, List<Booking>> customerBookings = new HashMap<>();
    static Scanner sc = new Scanner(System.in);
    static int customerCounter = 1;
    static int bookingCounter = 1;

    public static void main(String[] args) {
        System.out.print("Enter number of taxis: ");
        int numTaxis = sc.nextInt();
        initializeTaxis(numTaxis);

        while (true) {
            System.out.println("\n=== Zula Taxi Booking ===");
            System.out.println("1. Admin Login");
            System.out.println("2. Customer Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    adminMenu();
                    break;
                case 2:
                    customerMenu();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void initializeTaxis(int n) {
        for (int i = 1; i <= n; i++) {
            taxis.add(new Taxi(i));
        }
    }

   
    public static void adminMenu() {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Display Taxi Details");
            System.out.println("2. Display Admin Summary");
            System.out.println("3. Logout");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    displayTaxiDetails();
                    break;
                case 2:
                    displayAdminSummary();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    
    public static void customerMenu() {
        int customerId = loginOrRegisterCustomer();
        while (true) {
            System.out.println("\n=== Customer Menu ===");
            System.out.println("1. Book Taxi");
            System.out.println("2. View My Rides");
            System.out.println("3. Logout");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    bookTaxi(customerId);
                    break;
                case 2:
                    displayCustomerRides(customerId);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static int loginOrRegisterCustomer() {
        sc.nextLine();
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        for (Map.Entry<Integer, String> entry : customers.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                System.out.println("Welcome back, " + name + "!");
                return entry.getKey();
            }
        }
        int id = customerCounter++;
        customers.put(id, name);
        customerBookings.put(id, new ArrayList<>());
        System.out.println("Registered new customer: " + name + " (ID: " + id + ")");
        return id;
    }

    public static void bookTaxi(int customerId) {
        System.out.print("Enter Pickup Point (A-F): ");
        char pickup = sc.next().toUpperCase().charAt(0);
        if (pickup < 'A' || pickup > 'F') {
            System.out.println("Invalid pickup point. Must be between A and F.");
            return;
        }

        System.out.print("Enter Drop Point (A-F): ");
        char drop = sc.next().toUpperCase().charAt(0);
        if (drop < 'A' || drop > 'F') {
            System.out.println("Invalid drop point. Must be between A and F.");
            return;
        }

        System.out.print("Enter Pickup Time (integer hours): ");
        int pickupTime = sc.nextInt();
        if (pickupTime < 0) {
            System.out.println("Invalid pickup time.");
            return;
        }

        Taxi selectedTaxi = null;
        int minDistance = Integer.MAX_VALUE;

        for (Taxi taxi : taxis) {
            if (taxi.isAvailable(pickupTime)) {
                int distance = Math.abs(taxi.currentPoint - pickup);
                if (distance < minDistance ||
                        (distance == minDistance && taxi.totalEarnings < (selectedTaxi != null ? selectedTaxi.totalEarnings : Integer.MAX_VALUE))) {
                    selectedTaxi = taxi;
                    minDistance = distance;
                }
            }
        }

        if (selectedTaxi == null) {
            System.out.println("No taxis available at that time.");
            return;
        }

        int travelTime = Math.abs(drop - pickup);
        int dropTime = pickupTime + travelTime;
        int amount = selectedTaxi.calculateEarnings(pickup, drop);

        Booking booking = new Booking(bookingCounter++, customerId, pickup, drop, pickupTime, dropTime, amount);
        selectedTaxi.addBooking(booking);
        customerBookings.get(customerId).add(booking);

        System.out.println("Taxi-" + selectedTaxi.id + " is allocated. Fare: Rs." + amount);
    }

    public static void displayTaxiDetails() {
        for (Taxi taxi : taxis) {
            System.out.println("\nTaxi-" + taxi.id + " Total Earnings: Rs." + taxi.totalEarnings +
                    " | Commission to Admin: Rs." + taxi.totalCommission);
            System.out.printf("%-10s %-10s %-5s %-5s %-12s %-9s %-6s%n",
                    "BookingID", "CustomerID", "From", "To", "PickupTime", "DropTime", "Amount");
            for (Booking booking : taxi.bookings) {
                System.out.printf("%-10d %-10d %-5c %-5c %-12d %-9d %-6d%n",
                        booking.bookingId, booking.customerId, booking.from, booking.to,
                        booking.pickupTime, booking.dropTime, booking.amount);
            }
        }
    }

    public static void displayAdminSummary() {
        System.out.println("\n===== Admin Summary =====");
        double totalCommission = 0;
        int totalEarnings = 0;

        for (Taxi taxi : taxis) {
            System.out.println("Taxi-" + taxi.id +
                    " | Total Earnings: Rs." + taxi.totalEarnings +
                    " | Commission to Admin: Rs." + taxi.totalCommission);
            totalCommission += taxi.totalCommission;
            totalEarnings += taxi.totalEarnings;
        }

        System.out.println("\nTotal Earnings from all Taxis: Rs." + totalEarnings);
        System.out.println("Total Commission to Admin: Rs." + totalCommission);
    }

    public static void displayCustomerRides(int customerId) {
        List<Booking> rides = customerBookings.get(customerId);
        if (rides.isEmpty()) {
            System.out.println("No rides found.");
            return;
        }
        System.out.printf("%-10s %-5s %-5s %-12s %-9s %-6s%n",
                "BookingID", "From", "To", "PickupTime", "DropTime", "Amount");
        for (Booking b : rides) {
            System.out.printf("%-10d %-5c %-5c %-12d %-9d %-6d%n",
                    b.bookingId, b.from, b.to, b.pickupTime, b.dropTime, b.amount);
        }
    }
}
