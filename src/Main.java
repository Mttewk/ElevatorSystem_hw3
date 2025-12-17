import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final int NUMBER_OF_ELEVATORS = 3;
    private static final int NUMBER_OF_FLOORS = 10;

    public static void main(String[] args) {
        ElevatorSystem system = new ElevatorSystem(NUMBER_OF_ELEVATORS, NUMBER_OF_FLOORS);
        system.start();

        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Elevator Control System ===");
        System.out.println("Commands:");
        System.out.println("  request <from> <direction> <to> - Request elevator (direction: UP/DOWN)");
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

            switch (parts[0].toLowerCase()) {
                case "request":
                    if (parts.length == 4) {
                        handleRequest(system, parts);
                    } else {
                        System.out.println("Usage: request <from> <direction> <to>");
                    }
                    break;

                case "auto":
                    if (parts.length == 2) {
                        handleAutoRequests(system, parts[1]);
                    } else {
                        System.out.println("Usage: auto <count>");
                    }
                    break;

                case "status":
                    displayStatus(system);
                    break;

                case "quit":
                    running = false;
                    break;

                default:
                    System.out.println("Unknown command: " + parts[0]);
            }
        }

        system.shutdown();
        scanner.close();
        System.out.println("System terminated");
    }

    private static void handleRequest(ElevatorSystem system, String[] parts) {
        try {
            int fromFloor = Integer.parseInt(parts[1]);
            Direction direction = Direction.valueOf(parts[2].toUpperCase());
            int toFloor = Integer.parseInt(parts[3]);

            system.requestElevator(fromFloor, direction, toFloor);
        } catch (NumberFormatException e) {
            System.out.println("Invalid floor number");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid direction. Use UP or DOWN");
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
            System.out.println("Invalid count number");
        }
    }

    private static void displayStatus(ElevatorSystem system) {
        System.out.println("\n=== Elevator Status ===");
        for (Elevator elevator : system.getElevators()) {
            System.out.println("Elevator " + elevator.getId() +
                    ": Floor " + elevator.getCurrentFloor() +
                    ", Direction: " + elevator.getDirection() +
                    ", Status: " + elevator.getStatus());
        }
        System.out.println("=======================\n");
    }
}