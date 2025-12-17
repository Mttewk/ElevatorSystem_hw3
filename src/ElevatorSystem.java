import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ElevatorSystem {
    private final List<Elevator> elevators;
    private final ElevatorDispatcher dispatcher;
    private final ExecutorService executorService;
    private final int numberOfElevators;
    private final int numberOfFloors;

    public ElevatorSystem(int numberOfElevators, int numberOfFloors) {
        this.numberOfElevators = numberOfElevators;
        this.numberOfFloors = numberOfFloors;
        this.elevators = new ArrayList<>();

        for (int i = 1; i <= numberOfElevators; i++) {
            elevators.add(new Elevator(i, numberOfFloors));
        }

        this.dispatcher = new ElevatorDispatcher(elevators);
        this.executorService = Executors.newFixedThreadPool(numberOfElevators + 1);
    }

    public void start() {
        Logger.logSystemEvent("Starting Elevator System with " + numberOfElevators + " elevators and " + numberOfFloors + " floors");

        executorService.submit(dispatcher);

        for (Elevator elevator : elevators) {
            executorService.submit(elevator);
        }

        Logger.logSystemEvent("Elevator System started successfully");
    }

    public void requestElevator(int fromFloor, Direction direction, int toFloor) {
        if (!isValidFloor(fromFloor) || !isValidFloor(toFloor)) {
            Logger.logSystemEvent("Invalid floor request: from=" + fromFloor + ", to=" + toFloor);
            return;
        }

        if (!isValidDirection(fromFloor, toFloor, direction)) {
            Logger.logSystemEvent("Invalid direction for request: from=" + fromFloor + ", to=" + toFloor + ", direction=" + direction);
            return;
        }

        PassengerRequest request = new PassengerRequest(fromFloor, direction, toFloor);
        dispatcher.submitRequest(request);
    }

    private boolean isValidFloor(int floor) {
        return floor >= 1 && floor <= numberOfFloors;
    }

    private boolean isValidDirection(int fromFloor, int toFloor, Direction direction) {
        if (fromFloor == toFloor) {
            return false;
        }

        if (toFloor > fromFloor && direction != Direction.UP) {
            return false;
        }

        if (toFloor < fromFloor && direction != Direction.DOWN) {
            return false;
        }

        return true;
    }

    public void shutdown() {
        Logger.logSystemEvent("Shutting down Elevator System...");

        dispatcher.shutdown();

        for (Elevator elevator : elevators) {
            elevator.shutdown();
        }

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        Logger.logSystemEvent("Elevator System shut down successfully");
    }

    public List<Elevator> getElevators() {
        return new ArrayList<>(elevators);
    }

    public int getNumberOfElevators() {
        return numberOfElevators;
    }

    public int getNumberOfFloors() {
        return numberOfFloors;
    }
}
