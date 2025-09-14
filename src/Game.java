import java.util.Random;
import java.util.Scanner;

public class Game {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("请选择游戏规则：");
        System.out.println("1. 普通规则 (18格, 两个骰子)");
        System.out.println("1. 普通规则 (18格, 两个骰子)");
        System.out.println("2. 变体规则 (精确到达终点，超出则反向)");
        System.out.println("3. 变体规则2 (追击规则)");
        System.out.println("4. 变体规则3 (18格, 一个骰子)");
        System.out.println("5. 变体规则4 (36格, 两个骰子, 拐点到终点=6格)");
        System.out.println("组合变体 (输入2345的顺序组合，比如23, 245, 2345)");
        String ruleChoice = scanner.next();

        boolean normalRule = ruleChoice.equals("1");
        boolean variant2 = ruleChoice.contains("2");
        boolean variant3 = ruleChoice.contains("3");
        boolean variant4 = ruleChoice.contains("4");
        boolean variant5 = ruleChoice.contains("5");

        // 校验输入必须是升序（保证顺序性）
        if (!normalRule && !isAscending(ruleChoice)) {
            System.out.println("输入无效！变体规则必须按顺序输入（例如2345），程序结束。");
            return;
        }

        // 询问是否进入测试模式
        System.out.println("是否进入测试模式？(y/n)");
        String testModeChoice = scanner.next();
        boolean isTestMode = testModeChoice.equalsIgnoreCase("y");

        int piece1Position = (variant5 ? 1 : 1);
        int piece2Position = (variant5 ? 19 : 10);

        boolean piece1ReachedTurn = false;
        boolean piece2ReachedTurn = false;

        int piece1NewCoord = 0;
        int piece2NewCoord = 0;

        Random random = new Random();
        int roundCount = 0;

        int boardSize = (variant5 ? 36 : 18);
        int endPoint = (variant5 ? 6 : 3);

        while (piece1Position != 0 && piece2Position != 0) {
            roundCount++;

            int roll1, roll2;
            if (isTestMode) {
                // 在测试模式下，手动输入骰子点数
                System.out.println("回合 " + roundCount + "：请输入棋子1的骰子点数：");
                roll1 = scanner.nextInt();
                System.out.println("请输入棋子2的骰子点数：");
                roll2 = scanner.nextInt();
            } else {
                // 默认随机掷骰子
                if (variant4) {
                    // 一个骰子
                    roll1 = random.nextInt(6) + 1;
                    roll2 = random.nextInt(6) + 1;
                } else {
                    // 两个骰子相加
                    roll1 = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
                    roll2 = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
                }
            }

            System.out.println("回合 " + roundCount + "：");
            System.out.println("棋子1摇到的点数: " + roll1);
            System.out.println("棋子2摇到的点数: " + roll2);

            // 棋子1移动
            piece1Position = movePiece(piece1Position, roll1, boardSize);
            if (!piece1ReachedTurn) {
                if ((variant5 && piece1Position == 36) || (!variant5 && piece1Position == 18)) {
                    piece1ReachedTurn = true;
                    System.out.println("棋子1到达拐点，转为新坐标系统开始");
                }
            }

            // 棋子2移动
            piece2Position = movePiece(piece2Position, roll2, boardSize);
            if (!piece2ReachedTurn) {
                if ((variant5 && piece2Position == 18) || (!variant5 && piece2Position == 9)) {
                    piece2ReachedTurn = true;
                    System.out.println("棋子2到达拐点，转为新坐标系统开始");
                }
            }

            // 处理规则
            int[] result1, result2;

            if (normalRule) {
                result1 = handleNormalRule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord, true, endPoint);
                piece1Position = result1[0];
                piece1NewCoord = result1[1];

                result2 = handleNormalRule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord, false, endPoint);
                piece2Position = result2[0];
                piece2NewCoord = result2[1];
            } else {
                // 组合规则
                if (variant2) {
                    result1 = handleVariantRule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord, true, endPoint, boardSize);
                    piece1Position = result1[0];
                    piece1NewCoord = result1[1];

                    result2 = handleVariantRule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord, false, endPoint, boardSize);
                    piece2Position = result2[0];
                    piece2NewCoord = result2[1];
                } else {
                    result1 = handleNormalRule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord, true, endPoint);
                    piece1Position = result1[0];
                    piece1NewCoord = result1[1];

                    result2 = handleNormalRule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord, false, endPoint);
                    piece2Position = result2[0];
                    piece2NewCoord = result2[1];
                }

                // 追击规则 (在终点判断前处理)
                if (variant3 && piece1Position != 0 && piece2Position != 0) {
                    if (piece1Position == piece2Position && !piece1ReachedTurn && !piece2ReachedTurn) {
                        System.out.println("棋子1追上了棋子2！棋子2被送回起点！");
                        piece2Position = (variant5 ? 19 : 10);
                        int extraRoll = random.nextInt(6) + 1;
                        System.out.println("棋子2额外摇到的点数: " + extraRoll);
                        piece2Position = movePiece(piece2Position, extraRoll, boardSize);
                        System.out.println("棋子2当前位置: " + piece2Position);
                    } else if (piece2Position == piece1Position && !piece1ReachedTurn && !piece2ReachedTurn) {
                        System.out.println("棋子2追上了棋子1！棋子1被送回起点！");
                        piece1Position = 1;
                        int extraRoll = random.nextInt(6) + 1;
                        System.out.println("棋子1额外摇到的点数: " + extraRoll);
                        piece1Position = movePiece(piece1Position, extraRoll, boardSize);
                        System.out.println("棋子1当前位置: " + piece1Position);
                    }
                }
            }

            System.out.println("--------------------");
        }

        System.out.println("游戏结束，回合数: " + roundCount);
    }

    // 校验输入是否升序
    public static boolean isAscending(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) <= s.charAt(i - 1)) return false;
        }
        return true;
    }

    // 普通规则：超过终点直接结束
    public static int[] handleNormalRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint) {
        String name = isPiece1 ? "棋子1" : "棋子2";
        if (reachedTurn) {
            newCoord += roll;
            if (newCoord >= endPoint) {
                System.out.println(name + "到达或超过终点，游戏结束！");
                return new int[]{0, newCoord};
            }
            System.out.println(name + "的新坐标: " + newCoord);
        } else {
            System.out.println(name + "当前位置: " + position);
        }
        return new int[]{position, newCoord};
    }

    // 变体规则2：必须精确到达终点，超出则反向
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
                System.out.println(name + "到达终点！游戏结束！");
                return new int[]{0, newCoord};
            }
            if (position != 0) System.out.println(name + "的新坐标: " + newCoord);
        } else {
            System.out.println(name + "当前位置: " + position);
        }
        return new int[]{position, newCoord};
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
