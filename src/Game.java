import java.util.*;

// Game main class
public class Game {
    public static void main(String[] args) {
        GameController controller = new GameController();
        controller.startGame();
    }
}

// Game controller - manages game flow
class GameController {
    private GameConfig config;
    private GameBoard board;
    private DiceRoller dice;
    private UserInterface ui;
    private int roundCount;

    public void startGame() {
        try {
            ui = new UserInterface();
            config = new GameConfig(ui);
            board = new GameBoard(config);
            dice = new DiceRoller(config);

            runGameLoop();
            ui.showGameEnd(roundCount);

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + ", program terminated.");
        }
    }

    private void runGameLoop() {
        roundCount = 0;
        while (!board.isGameOver()) {
            playRound();
        }
    }

    private void playRound() {
        roundCount++;
        ui.showRoundStart(roundCount);

        for (GamePiece piece : board.getPieces()) {
            if (board.isGameOver()) break;
            playPieceTurn(piece);
        }

        ui.showRoundEnd();
    }

    private void playPieceTurn(GamePiece piece) {
        PieceState beforeMove = piece.saveState();
        int roll = dice.roll(piece.getId());

        MoveExecutor moveExecutor = new MoveExecutor(config, board, ui);
        moveExecutor.executeMove(piece, roll);

        if (ui.askForUndo(piece.getName())) {
            piece.restoreState(beforeMove);
            ui.showUndoComplete(piece.getName());
        }
    }
}

// Game configuration
class GameConfig {
    private boolean isFourPlayerMode;
    private Set<GameRule> enabledRules;
    private boolean isTestMode;
    private int boardSize;
    private int endPoint;

    public GameConfig(UserInterface ui) {
        setupPlayerMode(ui);
        setupGameRules(ui);
        setupTestMode(ui);
        calculateDerivedValues();
    }

    private void setupPlayerMode(UserInterface ui) {
        String choice = ui.getPlayerModeChoice();
        this.isFourPlayerMode = "2".equals(choice);
    }

    private void setupGameRules(UserInterface ui) {
        String ruleChoice = ui.getGameRuleChoice();
        this.enabledRules = new HashSet<>();

        if ("1".equals(ruleChoice)) {
            enabledRules.add(GameRule.NORMAL);
        } else {
            validateRuleSequence(ruleChoice);
            parseVariantRules(ruleChoice);
        }
    }

    private void setupTestMode(UserInterface ui) {
        String choice = ui.getTestModeChoice();
        this.isTestMode = "y".equalsIgnoreCase(choice);
    }

    private void calculateDerivedValues() {
        this.boardSize = hasRule(GameRule.VARIANT4) ? 36 : 18;
        this.endPoint = hasRule(GameRule.VARIANT4) ? 6 : 3;
    }

    private void validateRuleSequence(String ruleChoice) {
        for (int i = 1; i < ruleChoice.length(); i++) {
            if (ruleChoice.charAt(i) <= ruleChoice.charAt(i - 1)) {
                throw new IllegalArgumentException("Invalid input! Variant rules must be entered in order");
            }
        }
    }

    private void parseVariantRules(String ruleChoice) {
        if (ruleChoice.contains("2")) enabledRules.add(GameRule.VARIANT1);
        if (ruleChoice.contains("3")) enabledRules.add(GameRule.VARIANT2);
        if (ruleChoice.contains("4")) enabledRules.add(GameRule.VARIANT3);
        if (ruleChoice.contains("5")) enabledRules.add(GameRule.VARIANT4);
    }

    // Getters
    public boolean isFourPlayerMode() { return isFourPlayerMode; }
    public boolean hasRule(GameRule rule) { return enabledRules.contains(rule); }
    public boolean isTestMode() { return isTestMode; }
    public int getBoardSize() { return boardSize; }
    public int getEndPoint() { return endPoint; }
    public int getPlayerCount() { return isFourPlayerMode ? 4 : 2; }
}

// Game rules enumeration
enum GameRule {
    NORMAL, VARIANT1, VARIANT2, VARIANT3, VARIANT4
}

// Game board
class GameBoard {
    private List<GamePiece> pieces;
    private int[] initialPositions;
    private int[] turnPoints;

    public GameBoard(GameConfig config) {
        this.initialPositions = config.isFourPlayerMode() ?
                new int[]{1, 5, 10, 14} : new int[]{1, 10};
        this.turnPoints = config.isFourPlayerMode() ?
                new int[]{18, 4, 9, 13} : new int[]{18, 9};

        createPieces(config.getPlayerCount());
    }

    private void createPieces(int playerCount) {
        pieces = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            pieces.add(new GamePiece(i, initialPositions[i], turnPoints[i]));
        }
    }

    public List<GamePiece> getPieces() { return pieces; }

    public GamePiece findPieceAt(int position, GamePiece excludePiece) {
        return pieces.stream()
                .filter(p -> p != excludePiece && p.getPosition() == position && position != 0)
                .findFirst()
                .orElse(null);
    }

    public boolean isGameOver() {
        return pieces.stream().anyMatch(p ->
                p.getPosition() == 0 && !p.isInNewCoordinates());
    }

    public int getInitialPosition(int pieceIndex) {
        return initialPositions[pieceIndex];
    }
}

// Game piece
class GamePiece {
    private final int id;
    private final String name;
    private final int turnPoint;
    private final int initialPosition;
    private int position;
    private boolean inNewCoordinates;
    private int newCoordinate;

    public GamePiece(int id, int initialPosition, int turnPoint) {
        this.id = id;
        this.name = "Piece" + (id + 1);
        this.position = initialPosition;
        this.turnPoint = turnPoint;
        this.initialPosition = initialPosition;
        this.inNewCoordinates = false;
        this.newCoordinate = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public boolean isInNewCoordinates() { return inNewCoordinates; }
    public void setInNewCoordinates(boolean inNew) { this.inNewCoordinates = inNew; }
    public int getNewCoordinate() { return newCoordinate; }
    public void setNewCoordinate(int coord) { this.newCoordinate = coord; }
    public int getTurnPoint() { return turnPoint; }
    public int getInitialPosition() { return initialPosition; }

    public void resetToBase() {
        this.position = initialPosition;
        this.inNewCoordinates = false;
        this.newCoordinate = 0;
    }

    public PieceState saveState() {
        return new PieceState(position, inNewCoordinates, newCoordinate);
    }

    public void restoreState(PieceState state) {
        this.position = state.position;
        this.inNewCoordinates = state.inNewCoordinates;
        this.newCoordinate = state.newCoordinate;
    }
}

// Piece state
class PieceState {
    final int position;
    final boolean inNewCoordinates;
    final int newCoordinate;

    public PieceState(int position, boolean inNewCoordinates, int newCoordinate) {
        this.position = position;
        this.inNewCoordinates = inNewCoordinates;
        this.newCoordinate = newCoordinate;
    }
}

// Dice roller
class DiceRoller {
    private Random random = new Random();
    private GameConfig config;
    private TestDiceData testData;

    public DiceRoller(GameConfig config) {
        this.config = config;
        if (config.isTestMode()) {
            this.testData = new TestDiceData();
        }
    }

    public int roll(int pieceId) {
        if (config.isTestMode()) {
            return testData.getNextRoll(pieceId);
        }

        if (config.hasRule(GameRule.VARIANT3)) {
            return random.nextInt(6) + 1;
        } else {
            return (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
        }
    }
}

// Test dice data
class TestDiceData {
    private int[][] rolls = {
            {10, 9, 4, 1, 1},
            {1, 1, 1, 3, 2},
            {4, 6, 3, 5, 2},
            {3, 2, 6, 4, 1}
    };
    private int[] indices = new int[4];

    public int getNextRoll(int pieceId) {
        int result = rolls[pieceId][indices[pieceId]];
        indices[pieceId] = (indices[pieceId] + 1) % rolls[pieceId].length;
        return result;
    }
}

// Move executor
class MoveExecutor {
    private GameConfig config;
    private GameBoard board;
    private UserInterface ui;

    public MoveExecutor(GameConfig config, GameBoard board, UserInterface ui) {
        this.config = config;
        this.board = board;
        this.ui = ui;
    }

    public void executeMove(GamePiece piece, int roll) {
        if (!piece.isInNewCoordinates()) {
            executeCircleMove(piece, roll);
        } else {
            executeNewCoordinateMove(piece, roll);
        }
    }

    private void executeCircleMove(GamePiece piece, int roll) {
        int oldPos = piece.getPosition();
        int newPos = calculateCirclePosition(oldPos, roll);
        piece.setPosition(newPos);

        ui.showPieceMove(piece.getName(), oldPos, roll, newPos);

        if (config.hasRule(GameRule.VARIANT2)) {
            checkAndHandleCapture(piece);
        }

        if (piece.getPosition() == piece.getTurnPoint()) {
            piece.setInNewCoordinates(true);
            piece.setNewCoordinate(0);
            ui.showTurnPointReached(piece.getName());
        }
    }

    private void executeNewCoordinateMove(GamePiece piece, int roll) {
        if (config.hasRule(GameRule.VARIANT1)) {
            executeVariant1Move(piece, roll);
        } else {
            executeNormalNewMove(piece, roll);
        }
    }

    private void executeVariant1Move(GamePiece piece, int roll) {
        int current = piece.getNewCoordinate();
        int target = current + roll;
        int endPoint = config.getEndPoint();

        if (target == endPoint) {
            ui.showNewCoordinateMove(piece.getName(), current, roll, endPoint);
            ui.showExactWin(piece.getName());
            finishPiece(piece);
        } else if (target < endPoint) {
            ui.showNewCoordinateMove(piece.getName(), current, roll, target);
            piece.setNewCoordinate(target);
            piece.setPosition(target);
        } else {
            handleOvershoot(piece, roll, current, target, endPoint);
        }
    }

    private void executeNormalNewMove(GamePiece piece, int roll) {
        int current = piece.getNewCoordinate();
        int target = current + roll;
        int endPoint = config.getEndPoint();

        if (target >= endPoint) {
            ui.showNewCoordinateMove(piece.getName(), current, roll, endPoint);
            ui.showGameWin(piece.getName());
            finishPiece(piece);
        } else {
            ui.showNewCoordinateMove(piece.getName(), current, roll, target);
            piece.setNewCoordinate(target);
            piece.setPosition(target);
        }
    }

    private void handleOvershoot(GamePiece piece, int roll, int current, int target, int endPoint) {
        int overshoot = target - endPoint;
        System.out.println(piece.getName() + " current position (new coordinates): " + current + " -> roll: " + roll + " -> overshoots endpoint, moves back " + overshoot + " steps");

        int backPos = endPoint - overshoot;

        if (backPos > 0) {
            System.out.println(piece.getName() + " after moving back, new position: " + backPos + " (new coordinates)");
            piece.setNewCoordinate(backPos);
            piece.setPosition(backPos);
        } else if (backPos == 0) {
            System.out.println(piece.getName() + " after moving back, returns to turn point: " + backPos + " (new coordinates)");
            piece.setNewCoordinate(0);
            piece.setPosition(0);
        } else {
            int stepsOnCircle = -backPos;
            int circlePos = piece.getPosition() - stepsOnCircle;
            while (circlePos <= 0) circlePos += config.getBoardSize();

            System.out.println(piece.getName() + " overshoots turn point, returns to circle and moves back " + stepsOnCircle + " steps -> new position: " + circlePos);
            piece.setPosition(circlePos);
            piece.setNewCoordinate(0);
            piece.setInNewCoordinates(false);

            if (config.hasRule(GameRule.VARIANT2)) {
                checkAndHandleCapture(piece);
            }
        }
    }

    private int calculateCirclePosition(int current, int roll) {
        current += roll;
        if (current > config.getBoardSize()) {
            current -= config.getBoardSize();
        }
        return current;
    }

    private void checkAndHandleCapture(GamePiece attacker) {
        GamePiece target = board.findPieceAt(attacker.getPosition(), attacker);
        if (target != null) {
            ui.showCapture(attacker.getName(), target.getName());
            target.resetToBase();
            ui.showReturnToBase(target.getName(), target.getInitialPosition());
        }
    }

    private void finishPiece(GamePiece piece) {
        piece.setPosition(0);
        piece.setNewCoordinate(0);
        piece.setInNewCoordinates(false);
    }
}

// User interface
class UserInterface {
    private Scanner scanner = new Scanner(System.in);

    public String getPlayerModeChoice() {
        System.out.println("Select game mode:");
        System.out.println("1. Two-player mode");
        System.out.println("2. Four-player mode");
        return scanner.next();
    }

    public String getGameRuleChoice() {
        System.out.println("Select game rules:");
        System.out.println("1. Normal rules (18 spaces, two dice)");
        System.out.println("2. Variant rule 1 (exact endpoint arrival, overshoot moves backward)");
        System.out.println("3. Variant rule 2 (capture rule)");
        System.out.println("4. Variant rule 3 (18 spaces, one die)");
        System.out.println("5. Variant rule 4 (36 spaces, two dice, turn point to endpoint = 6 spaces)");
        System.out.println("Combine variants (enter 2345 in sequence, e.g., 23, 245, 2345)");
        return scanner.next();
    }

    public String getTestModeChoice() {
        System.out.println("Enter test mode? (y/n)");
        return scanner.next();
    }

    public boolean askForUndo(String pieceName) {
        System.out.println("Undo " + pieceName + "'s move? (y/n)");
        return "y".equalsIgnoreCase(scanner.next());
    }

    public void showRoundStart(int roundCount) {
        System.out.println("Round " + roundCount + ":");
    }

    public void showRoundEnd() {
        System.out.println("--------------------");
    }

    public void showGameEnd(int roundCount) {
        System.out.println("Game over, total rounds: " + roundCount);
    }

    public void showPieceMove(String name, int oldPos, int roll, int newPos) {
        System.out.println(name + " current position: " + oldPos + " -> roll: " + roll + " -> new position: " + newPos);
    }

    public void showNewCoordinateMove(String name, int oldCoord, int roll, int newCoord) {
        System.out.println(name + " current position (new coordinates): " + oldCoord + " -> roll: " + roll + " -> new position: " + newCoord + " (new coordinates)");
    }

    public void showTurnPointReached(String name) {
        System.out.println(name + " reached turn point, entering new coordinate system");
    }

    public void showCapture(String attackerName, String capturedName) {
        System.out.println(attackerName + " captured " + capturedName + "!");
    }

    public void showReturnToBase(String name, int basePosition) {
        System.out.println(name + " returned to initial position: " + basePosition);
    }

    public void showUndoComplete(String name) {
        System.out.println(name + "'s move has been undone");
    }

    public void showGameWin(String name) {
        System.out.println(name + " reached or passed the endpoint, game over!");
    }

    public void showExactWin(String name) {
        System.out.println(name + " exactly reached the endpoint, game over!");
    }
}

