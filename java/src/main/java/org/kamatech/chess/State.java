package org.kamatech.chess;

/**
 * Represents the state of a chess piece with state machine support
 */
public class State implements Cloneable {
    // State machine states
    public enum PieceState {
        IDLE, // מנוחה
        MOVE, // תנועה
        REST, // מנוחה רגילה אחרי תנועה
        EXHAUST, // עייפות
        JUMP, // קפיצה/דילוג
        SHORT_REST // מנוחה קצרה אחרי קפיצה
    }

    private final Moves moves;
    private final Graphics graphics;
    private final Physics physics;
    private PieceState currentState;
    private long stateStartTime;
    private long stateDuration;

    public State(Moves moves, Graphics graphics, Physics physics) {
        this.moves = moves;
        this.graphics = graphics;
        this.physics = physics;
        this.currentState = PieceState.IDLE;
        this.stateStartTime = System.currentTimeMillis();
        this.stateDuration = 0;
    }

    public State(Moves moves, Graphics graphics, Physics physics, PieceState initialState) {
        this(moves, graphics, physics);
        this.currentState = initialState;
    }

    public Moves getMoves() {
        return moves;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public Physics getPhysics() {
        return physics;
    }

    /* ----------- State Machine Methods ----------- */
    public PieceState getCurrentState() {
        return currentState;
    }

    public void setState(PieceState newState) {
        this.currentState = newState;
        this.stateStartTime = System.currentTimeMillis();

        // Set appropriate duration for each state
        // Slow down state durations by doubling the base cooldown
        long baseCooldown = (moves != null) ? moves.getCooldown() * 2 : 2000; // Default to 2 seconds if moves is null

        switch (newState) {
            case MOVE:
                // MOVE state lasts until movement ends (key release) before cooldown
                this.stateDuration = Long.MAX_VALUE;
                break;
            case REST:
                // REST state duration is the cooldown period
                this.stateDuration = baseCooldown;
                break;
            case SHORT_REST:
                this.stateDuration = baseCooldown / 2; // Half cooldown for jump
                break;
            case JUMP:
                this.stateDuration = baseCooldown / 3; // Quick jump action
                break;
            case EXHAUST:
                this.stateDuration = baseCooldown * 2; // Double cooldown when exhausted
                break;
            case IDLE:
            default:
                this.stateDuration = 0;
                break;
        }
    }

    public boolean isStateFinished() {
        if (stateDuration == 0)
            return true;
        return (System.currentTimeMillis() - stateStartTime) >= stateDuration;
    }

    public long getRemainingStateTime() {
        if (stateDuration == 0)
            return 0;
        long elapsed = System.currentTimeMillis() - stateStartTime;
        return Math.max(0, stateDuration - elapsed);
    }

    public boolean canPerformAction() {
        return currentState == PieceState.IDLE || isStateFinished();
    }

    public void update() {
        // Update physics
        physics.update();

        // Automatic state transitions when duration elapses
        if (isStateFinished()) {
            switch (currentState) {
                case MOVE:
                    setState(PieceState.REST);
                    break;
                case JUMP:
                    setState(PieceState.SHORT_REST);
                    break;
                case REST:
                case SHORT_REST:
                case EXHAUST:
                    setState(PieceState.IDLE);
                    break;
                default:
                    // Remain in IDLE
                    break;
            }
        }
    }

    /**
     * Creates a clone of the current state
     * 
     * @return A new State instance with the same moves, graphics and physics
     */
    @Override
    public State clone() {
        State cloned = new State(
                moves.clone(),
                graphics.clone(),
                physics.clone(),
                currentState);
        cloned.stateStartTime = this.stateStartTime;
        cloned.stateDuration = this.stateDuration;
        return cloned;
    }
}
