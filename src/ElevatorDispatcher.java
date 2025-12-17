import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ElevatorDispatcher implements Runnable {
    private final List<Elevator> elevators;
    private final BlockingQueue<PassengerRequest> requestQueue;
    private volatile boolean running;

    public ElevatorDispatcher(List<Elevator> elevators) {
        this.elevators = elevators;
        this.requestQueue = new LinkedBlockingQueue<>();
        this.running = true;
    }

    public void submitRequest(PassengerRequest request) {
        try {
            requestQueue.put(request);
            Logger.logRequest(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        Logger.logSystemEvent("Dispatcher started");

        while (running || !requestQueue.isEmpty()) {
            try {
                PassengerRequest request = requestQueue.take();
                processRequest(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Logger.logSystemEvent("Dispatcher stopped");
    }

    private void processRequest(PassengerRequest request) {
        Elevator bestElevator = findBestElevator(request);

        if (bestElevator != null) {
            Logger.logElevatorAssignment(bestElevator.getId(), request);
            bestElevator.addTarget(request.getFromFloor());
            bestElevator.addTarget(request.getToFloor());
        } else {
            Logger.logSystemEvent("No available elevator found for request " + request);
        }
    }

    private Elevator findBestElevator(PassengerRequest request) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (!elevator.hasCapacity()) {
                continue;
            }

            int distance = calculateDistance(elevator, request);

            if (distance < minDistance) {
                minDistance = distance;
                bestElevator = elevator;
            }
        }

        if (bestElevator == null) {
            for (Elevator elevator : elevators) {
                int distance = calculateDistance(elevator, request);

                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
        }

        return bestElevator;
    }

    private int calculateDistance(Elevator elevator, PassengerRequest request) {
        int currentFloor = elevator.getCurrentFloor();
        Direction elevatorDirection = elevator.getDirection();
        int requestFloor = request.getFromFloor();
        Direction requestDirection = request.getDirection();

        if (elevatorDirection == Direction.IDLE) {
            return Math.abs(currentFloor - requestFloor);
        }

        if (elevatorDirection == requestDirection) {
            if (elevatorDirection == Direction.UP && requestFloor >= currentFloor) {
                return requestFloor - currentFloor;
            } else if (elevatorDirection == Direction.DOWN && requestFloor <= currentFloor) {
                return currentFloor - requestFloor;
            }
        }

        return Math.abs(currentFloor - requestFloor) + 100;
    }

    public void shutdown() {
        running = false;
    }
}