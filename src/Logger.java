import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void log(String message) {
        String timestamp = LocalTime.now().format(TIME_FORMATTER);
        System.out.println("[" + timestamp + "] " + message);
    }

    public static void logRequest(PassengerRequest request) {
        log("REQUEST: " + request);
    }

    public static void logElevatorAssignment(int elevatorId, PassengerRequest request) {
        log("ASSIGNED: Elevator " + elevatorId + " -> " + request);
    }

    public static void logElevatorMovement(int elevatorId, Direction direction, int fromFloor, int toFloor) {
        log("MOVING: Elevator " + elevatorId + " " + direction + " from floor " + fromFloor + " to " + toFloor);
    }

    public static void logElevatorArrival(int elevatorId, int floor) {
        log("ARRIVED: Elevator " + elevatorId + " at floor " + floor);
    }

    public static void logDoorsOpen(int elevatorId, int floor) {
        log("DOORS_OPEN: Elevator " + elevatorId + " at floor " + floor);
    }

    public static void logDoorsClose(int elevatorId, int floor) {
        log("DOORS_CLOSE: Elevator " + elevatorId + " at floor " + floor);
    }

    public static void logElevatorIdle(int elevatorId, int floor) {
        log("IDLE: Elevator " + elevatorId + " waiting at floor " + floor);
    }

    public static void logSystemEvent(String event) {
        log("SYSTEM: " + event);
    }
}
