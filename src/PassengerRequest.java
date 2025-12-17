public class PassengerRequest {
    private final int fromFloor;
    private final Direction direction;
    private final int toFloor;
    private final long timestamp;

    public PassengerRequest(int fromFloor, Direction direction, int toFloor) {
        this.fromFloor = fromFloor;
        this.direction = direction;
        this.toFloor = toFloor;
        this.timestamp = System.currentTimeMillis();
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getToFloor() {
        return toFloor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Request{from=" + fromFloor + ", dir=" + direction + ", to=" + toFloor + "}";
    }
}
