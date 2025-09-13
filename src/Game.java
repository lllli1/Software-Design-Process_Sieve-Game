import java.util.Random;
import java.util.Scanner;

public class Game {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("请选择游戏规则：");
        System.out.println("1. 普通规则 (18格, 两个骰子)");
        System.out.println("2. 变体规则 (18格, 两个骰子，需精确到达终点，超出则反向)");
        System.out.println("3. 变体规则2 (预留)");
        System.out.println("4. 变体规则3 (18格, 一个骰子)");
        System.out.println("5. 变体规则4 (36格, 两个骰子, 拐点到终点=6格)");
        int ruleChoice = scanner.nextInt();

        int piece1Position = (ruleChoice == 5) ? 1 : 1;
        int piece2Position = (ruleChoice == 5) ? 19 : 10;

        boolean piece1ReachedTurn = false;
        boolean piece2ReachedTurn = false;

        int piece1NewCoord = 0;
        int piece2NewCoord = 0;

        Random random = new Random();
        int roundCount = 0;

        int boardSize = (ruleChoice == 5) ? 36 : 18;
        int endPoint = (ruleChoice == 5) ? 6 : 3;

        while (piece1Position != 0 && piece2Position != 0) {
            roundCount++;

            int roll1, roll2;
            if (ruleChoice == 4) {
                // 变体规则3：一个骰子
                roll1 = random.nextInt(6) + 1;
                roll2 = random.nextInt(6) + 1;
            } else {
                // 其它规则：两个骰子相加
                roll1 = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
                roll2 = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
            }

            System.out.println("回合 " + roundCount + "：");
            System.out.println("棋子1摇到的点数: " + roll1);
            System.out.println("棋子2摇到的点数: " + roll2);

            // 棋子1移动
            piece1Position = movePiece(piece1Position, roll1, boardSize);
            if (!piece1ReachedTurn) {
                if ((ruleChoice == 5 && piece1Position == 36) || (ruleChoice != 5 && piece1Position == 18)) {
                    piece1ReachedTurn = true;
                    System.out.println("棋子1到达拐点，转为新坐标系统开始");
                }
            }

            // 棋子2移动
            piece2Position = movePiece(piece2Position, roll2, boardSize);
            if (!piece2ReachedTurn) {
                if ((ruleChoice == 5 && piece2Position == 18) || (ruleChoice != 5 && piece2Position == 9)) {
                    piece2ReachedTurn = true;
                    System.out.println("棋子2到达拐点，转为新坐标系统开始");
                }
            }

            // 根据规则处理
            if (ruleChoice == 1) {
                int[] result1 = handleNormalRule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord, true, 3);
                piece1Position = result1[0];
                piece1NewCoord = result1[1];

                int[] result2 = handleNormalRule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord, false, 3);
                piece2Position = result2[0];
                piece2NewCoord = result2[1];
            } else if (ruleChoice == 2) {
                int[] result1 = handleVariantRule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord, true, 3, boardSize);
                piece1Position = result1[0];
                piece1NewCoord = result1[1];

                int[] result2 = handleVariantRule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord, false, 3, boardSize);
                piece2Position = result2[0];
                piece2NewCoord = result2[1];
            } else if (ruleChoice == 4) {
                int[] result1 = handleOneDiceRule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord, true, 3);
                piece1Position = result1[0];
                piece1NewCoord = result1[1];

                int[] result2 = handleOneDiceRule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord, false, 3);
                piece2Position = result2[0];
                piece2NewCoord = result2[1];
            } else if (ruleChoice == 5) {
                int[] result1 = handleVariant4Rule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord, true, endPoint, boardSize);
                piece1Position = result1[0];
                piece1NewCoord = result1[1];

                int[] result2 = handleVariant4Rule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord, false, endPoint, boardSize);
                piece2Position = result2[0];
                piece2NewCoord = result2[1];
            } else if (ruleChoice == 3) {
                System.out.println("变体规则2尚未实现。");
            }

            System.out.println("--------------------");
        }

        System.out.println("游戏结束，回合数: " + roundCount);
    }

    // 普通规则：超过终点直接结束
    public static int[] handleNormalRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint) {
        String name = isPiece1 ? "棋子1" : "棋子2";
        if (reachedTurn) {
            newCoord += roll;
            if (newCoord > endPoint) {
                System.out.println(name + "超出终点，游戏结束！");
                return new int[]{0, newCoord};
            }
            if (newCoord == endPoint) {
                System.out.println(name + "到达终点！");
                return new int[]{0, newCoord};
            }
            System.out.println(name + "的新坐标: " + newCoord);
        } else {
            System.out.println(name + "当前位置: " + position);
        }
        return new int[]{position, newCoord};
    }

    // 变体规则：必须精确到达终点，超出则反向
    public static int[] handleVariantRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint, int boardSize) {
        String name = isPiece1 ? "棋子1" : "棋子2";
        int turnPoint = isPiece1 ? (boardSize == 36 ? 36 : 18) : (boardSize == 36 ? 18 : 9);
        if (reachedTurn) {
            newCoord += roll;
            if (newCoord > endPoint) {
                int overshoot = newCoord - endPoint;
                newCoord = endPoint - overshoot;
                if (newCoord < 0) {
                    int remaining = -newCoord;
                    newCoord = 0;
                    position = turnPoint;
                    position -= remaining;
                    if (position <= 0) position += boardSize;
                    System.out.println(name + "退回拐点后，继续在圆盘逆时针走，当前位置: " + position);
                } else {
                    System.out.println(name + "超出终点，反向移动到新坐标: " + newCoord);
                }
            }
            if (newCoord == endPoint) {
                System.out.println(name + "到达终点！");
                return new int[]{0, newCoord};
            }
            if (position != 0) System.out.println(name + "的新坐标: " + newCoord);
        } else {
            System.out.println(name + "当前位置: " + position);
        }
        return new int[]{position, newCoord};
    }

    // 变体规则3：一个骰子，逻辑同普通规则
    public static int[] handleOneDiceRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint) {
        return handleNormalRule(reachedTurn, roll, position, newCoord, isPiece1, endPoint);
    }

    // 变体规则4：36格棋盘，拐点到终点 6 格
    public static int[] handleVariant4Rule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint, int boardSize) {
        // 和普通规则一样，只是终点是6，棋盘是36格
        return handleNormalRule(reachedTurn, roll, position, newCoord, isPiece1, endPoint);
    }

    // 棋子在大圆盘上移动
    public static int movePiece(int currentPosition, int roll, int boardSize) {
        currentPosition += roll;
        if (currentPosition > boardSize) {
            currentPosition -= boardSize;
        }
        return currentPosition;
    }
}
