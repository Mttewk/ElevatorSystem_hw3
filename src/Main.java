import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final int NUMBER_OF_ELEVATORS = 3;
    private static final int NUMBER_OF_FLOORS = 10;
    private static final int ELEVATOR_CAPACITY = 8;

    public static void main(String[] args) {
        ElevatorSystem system = new ElevatorSystem(NUMBER_OF_ELEVATORS, NUMBER_OF_FLOORS, ELEVATOR_CAPACITY);
        system.start();

        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Elevator Control System ===");
        System.out.println("Commands:");
        System.out.println("  <from> <to> - Request elevator (e.g., 3 7)");
        System.out.println("  auto <count> - Generate random requests");
        System.out.println("  status - Show elevator status");
        System.out.println("  quit - Exit system");
        System.out.println("================================\n");

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+");

            if (parts.length == 0 || parts[0].isEmpty()) {
                continue;
            }

            if (parts[0].equalsIgnoreCase("quit")) {
                running = false;
            } else if (parts[0].equalsIgnoreCase("status")) {
                displayStatus(system);
            } else if (parts[0].equalsIgnoreCase("auto") && parts.length == 2) {
                handleAutoRequests(system, parts[1]);
            } else if (parts.length == 2) {
                handleSimpleRequest(system, parts);
            } else {
                System.out.println("Invalid command. Use: <from> <to>, auto <count>, status, or quit");
            }
        }

        system.shutdown();
        scanner.close();
        System.out.println("System terminated");
    }

    private static void handleSimpleRequest(ElevatorSystem system, String[] parts) {
        try {
            int fromFloor = Integer.parseInt(parts[0]);
            int toFloor = Integer.parseInt(parts[1]);

            if (fromFloor == toFloor) {
                System.out.println("Error: From and to floors cannot be the same");
                return;
            }

            Direction direction = (toFloor > fromFloor) ? Direction.UP : Direction.DOWN;
            system.requestElevator(fromFloor, direction, toFloor);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid floor number");
        }
    }

    private static void handleAutoRequests(ElevatorSystem system, String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            Random random = new Random();

            for (int i = 0; i < count; i++) {
                int fromFloor = random.nextInt(NUMBER_OF_FLOORS) + 1;
                int toFloor;
                do {
                    toFloor = random.nextInt(NUMBER_OF_FLOORS) + 1;
                } while (toFloor == fromFloor);

                Direction direction = (toFloor > fromFloor) ? Direction.UP : Direction.DOWN;

                system.requestElevator(fromFloor, direction, toFloor);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            System.out.println("Generated " + count + " random requests");
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid count number");
        }
    }

    private static void displayStatus(ElevatorSystem system) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                    ELEVATOR STATUS                        ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        for (Elevator elevator : system.getElevators()) {
            System.out.printf("║ Elevator %-2d │ Floor: %-2d │ Direction: %-4s │ Status: %-11s │ Passengers: %d/%-2d ║%n",
                    elevator.getId(),
                    elevator.getCurrentFloor(),
                    elevator.getDirection(),
                    elevator.getStatus(),
                    elevator.getCurrentPassengers(),
                    elevator.getMaxCapacity());
        }
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
    }
}
