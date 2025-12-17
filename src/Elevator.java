import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator implements Runnable {
    private final int id;
    private final int maxFloor;
    private int currentFloor;
    private Direction direction;
    private ElevatorStatus status;
    private final Set<Integer> targetFloors;
    private final ReentrantLock lock;
    private volatile boolean running;

    public Elevator(int id, int maxFloor) {
        this.id = id;
        this.maxFloor = maxFloor;
        this.currentFloor = 1;
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

    public void addTarget(int floor) {
        lock.lock();
        try {
            if (floor >= 1 && floor <= maxFloor) {
                targetFloors.add(floor);
                System.out.println("Elevator " + id + ": Added target floor " + floor);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        System.out.println("Elevator " + id + " started");

        while (running) {
            try {
                processNextTarget();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("Elevator " + id + " stopped");
    }

    private void processNextTarget() throws InterruptedException {
        lock.lock();
        try {
            if (targetFloors.isEmpty()) {
                direction = Direction.IDLE;
                status = ElevatorStatus.STOPPED;
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

        System.out.println("Elevator " + id + ": Moving " + direction + " from floor " + currentFloor + " to floor " + targetFloor);

        while (currentFloor != targetFloor) {
            Thread.sleep(1000);
            currentFloor += (direction == Direction.UP) ? 1 : -1;
            System.out.println("Elevator " + id + ": Now at floor " + currentFloor);

            if (targetFloors.contains(currentFloor)) {
                arriveAtFloor(currentFloor);
            }
        }
    }

    private void arriveAtFloor(int floor) throws InterruptedException {
        status = ElevatorStatus.STOPPED;
        System.out.println("Elevator " + id + ": Arrived at floor " + floor);

        status = ElevatorStatus.DOORS_OPEN;
        System.out.println("Elevator " + id + ": Doors opening at floor " + floor);
        Thread.sleep(2000);

        targetFloors.remove(floor);

        System.out.println("Elevator " + id + ": Doors closing at floor " + floor);
        Thread.sleep(1000);
        status = ElevatorStatus.STOPPED;
    }

    public void shutdown() {
        running = false;
    }
}
