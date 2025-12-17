import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator implements Runnable {
    private final int id;
    private final int maxFloor;
    private final int maxCapacity;
    private int currentFloor;
    private int currentPassengers;
    private Direction direction;
    private ElevatorStatus status;
    private final Set<Integer> targetFloors;
    private final ReentrantLock lock;
    private volatile boolean running;

    public Elevator(int id, int maxFloor, int maxCapacity) {
        this.id = id;
        this.maxFloor = maxFloor;
        this.maxCapacity = maxCapacity;
        this.currentFloor = 1;
        this.currentPassengers = 0;
        this.direction = Direction.IDLE;
        this.status = ElevatorStatus.STOPPED;
        this.targetFloors = new TreeSet<>();
        this.lock = new ReentrantLock();
        this.running = true;
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        lock.lock();
        try {
            return currentFloor;
        } finally {
            lock.unlock();
        }
    }

    public Direction getDirection() {
        lock.lock();
        try {
            return direction;
        } finally {
            lock.unlock();
        }
    }

    public ElevatorStatus getStatus() {
        lock.lock();
        try {
            return status;
        } finally {
            lock.unlock();
        }
    }

    public int getCurrentPassengers() {
        lock.lock();
        try {
            return currentPassengers;
        } finally {
            lock.unlock();
        }
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean hasCapacity() {
        lock.lock();
        try {
            return currentPassengers < maxCapacity;
        } finally {
            lock.unlock();
        }
    }

    public void addTarget(int floor) {
        lock.lock();
        try {
            if (floor >= 1 && floor <= maxFloor) {
                targetFloors.add(floor);
                Logger.logSystemEvent("Elevator " + id + " added target floor " + floor);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        Logger.logSystemEvent("Elevator " + id + " started (capacity: " + maxCapacity + " passengers)");

        while (running) {
            try {
                processNextTarget();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Logger.logSystemEvent("Elevator " + id + " stopped");
    }

    private void processNextTarget() throws InterruptedException {
        lock.lock();
        try {
            if (targetFloors.isEmpty()) {
                if (direction != Direction.IDLE) {
                    direction = Direction.IDLE;
                    status = ElevatorStatus.STOPPED;
                    Logger.logElevatorIdle(id, currentFloor);
                }
                return;
            }

            int nextFloor = getNextFloor();
            moveToFloor(nextFloor);

        } finally {
            lock.unlock();
        }
    }

    private int getNextFloor() {
        if (direction == Direction.UP || direction == Direction.IDLE) {
            for (int floor : targetFloors) {
                if (floor >= currentFloor) {
                    return floor;
                }
            }
        }

        if (direction == Direction.DOWN || direction == Direction.IDLE) {
            int maxBelow = -1;
            for (int floor : targetFloors) {
                if (floor <= currentFloor) {
                    maxBelow = Math.max(maxBelow, floor);
                }
            }
            if (maxBelow != -1) {
                return maxBelow;
            }
        }

        return targetFloors.iterator().next();
    }

    private void moveToFloor(int targetFloor) throws InterruptedException {
        if (currentFloor == targetFloor) {
            arriveAtFloor(targetFloor);
            return;
        }

        direction = (targetFloor > currentFloor) ? Direction.UP : Direction.DOWN;
        status = ElevatorStatus.MOVING;

        Logger.logElevatorMovement(id, direction, currentFloor, targetFloor);

        while (currentFloor != targetFloor) {
            Thread.sleep(1000);
            currentFloor += (direction == Direction.UP) ? 1 : -1;

            if (targetFloors.contains(currentFloor)) {
                arriveAtFloor(currentFloor);
            }
        }
    }

    private void arriveAtFloor(int floor) throws InterruptedException {
        status = ElevatorStatus.STOPPED;
        Logger.logElevatorArrival(id, floor);

        status = ElevatorStatus.DOORS_OPEN;
        Logger.logDoorsOpen(id, floor);

        simulatePassengerExchange(floor);

        Thread.sleep(2000);

        targetFloors.remove(floor);

        Logger.logDoorsClose(id, floor);
        Thread.sleep(1000);
        status = ElevatorStatus.STOPPED;
    }

    private void simulatePassengerExchange(int floor) {
        int passengersExiting = Math.min(currentPassengers, 2);
        currentPassengers -= passengersExiting;

        if (passengersExiting > 0) {
            Logger.logSystemEvent("Elevator " + id + " at floor " + floor + ": " + passengersExiting + " passenger(s) exited");
        }

        int availableSpace = maxCapacity - currentPassengers;
        int passengersEntering = Math.min(availableSpace, 2);
        currentPassengers += passengersEntering;

        if (passengersEntering > 0) {
            Logger.logSystemEvent("Elevator " + id + " at floor " + floor + ": " + passengersEntering + " passenger(s) entered");
        }

        Logger.logSystemEvent("Elevator " + id + " capacity: " + currentPassengers + "/" + maxCapacity);
    }

    public void shutdown() {
        running = false;
    }
}
