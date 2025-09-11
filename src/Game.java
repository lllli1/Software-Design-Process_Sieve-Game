import java.util.Random;

public class Game {
    public static void main(String[] args) {
        // Initial positions of pieces
        int piece1Position = 1;
        int piece2Position = 10;

        // Flags to track if the piece has passed the turning point
        boolean piece1ReachedTurn = false;
        boolean piece2ReachedTurn = false;

        // Variables to track new coordinate positions
        int piece1NewCoord = 1; // New coordinate for piece 1 (starting at 1)
        int piece2NewCoord = 1; // New coordinate for piece 2 (starting at 1)

        // Random number generator
        Random random = new Random();

        // Game loop until both pieces reach the target
        while (piece1Position != 0 && piece2Position != 0) {
            // Roll the dice for both pieces
            int roll1 = random.nextInt(6) + 1;
            int roll2 = random.nextInt(6) + 1;

            // Print dice rolls
            System.out.println("棋子1摇到的点数: " + roll1);
            System.out.println("棋子2摇到的点数: " + roll2);

            // Move piece 1
            piece1Position = movePiece(piece1Position, roll1);

            // If piece 1 reaches 18, change flag to start moving in the new coordinate system
            if (piece1Position == 18 && !piece1ReachedTurn) {
                piece1ReachedTurn = true;
                System.out.println("棋子1到达18，转为新坐标系统开始");
            }

            // Move piece 2
            piece2Position = movePiece(piece2Position, roll2);

            // If piece 2 reaches 9, change flag to start moving in the new coordinate system
            if (piece2Position == 9 && !piece2ReachedTurn) {
                piece2ReachedTurn = true;
                System.out.println("棋子2到达9，转为新坐标系统开始");
            }

            // Handle movement in new coordinate system for piece 1 (after turning point)
            if (piece1ReachedTurn) {
                piece1NewCoord += roll1; // Add the dice roll to the new coordinate

                // Output piece 1 position in the new coordinate system
                if (piece1NewCoord > 2) {
                    System.out.println("棋子1到达终点！");
                    piece1Position = 0; // End game for piece 1
                } else {
                    System.out.println("棋子1的新坐标: " + piece1NewCoord);
                }
            } else {
                System.out.println("棋子1当前位置: 1"); // Still moving to turning point
            }

            // Handle movement in new coordinate system for piece 2 (after turning point)
            if (piece2ReachedTurn) {
                piece2NewCoord += roll2; // Add the dice roll to the new coordinate

                // Output piece 2 position in the new coordinate system
                if (piece2NewCoord > 2) {
                    System.out.println("棋子2到达终点！");
                    piece2Position = 0; // End game for piece 2
                } else {
                    System.out.println("棋子2的新坐标: " + piece2NewCoord);
                }
            } else {
                System.out.println("棋子2当前位置: 1"); // Still moving to turning point
            }

            System.out.println("--------------------");
        }
    }

    // Move a piece, only clockwise
    public static int movePiece(int currentPosition, int roll) {
        currentPosition += roll;

        // If it exceeds 18, wrap around
        if (currentPosition > 18) {
            currentPosition = currentPosition - 18;
        }

        return currentPosition;
    }
}
