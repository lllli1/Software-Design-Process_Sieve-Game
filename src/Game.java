import java.util.*;

public class Game {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("请选择游戏规则：");
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

        // 在测试模式下预设测试骰子序列
        int[] piece1Rolls = {11, 9, 2, 4, 5}; // 示例：棋子1的预设骰子序列
        int[] piece2Rolls = {1, 1, 1, 3, 2}; // 示例：棋子2的预设骰子序列
        int piece1RollIndex = 0;
        int piece2RollIndex = 0;

        // 使用栈来保存每个回合的状态
        Stack<GameState> gameHistory = new Stack<>();
        gameHistory.push(new GameState(piece1Position, piece2Position, piece1ReachedTurn, piece2ReachedTurn, piece1NewCoord, piece2NewCoord, piece1RollIndex, piece2RollIndex, roundCount));

        while (piece1Position != 0 && piece2Position != 0) {
            roundCount++;

            int roll1 = 0, roll2 = 0;

            if (isTestMode) {
                // 使用预设的骰子序列
                roll1 = piece1Rolls[piece1RollIndex];
                roll2 = piece2Rolls[piece2RollIndex];

                // 更新索引，确保每回合取下一个骰子
                piece1RollIndex = (piece1RollIndex + 1) % piece1Rolls.length;
                piece2RollIndex = (piece2RollIndex + 1) % piece2Rolls.length;

                System.out.println("回合 " + roundCount + "：");
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

            // 保存当前回合的状态到栈
            gameHistory.push(new GameState(piece1Position, piece2Position, piece1ReachedTurn, piece2ReachedTurn, piece1NewCoord, piece2NewCoord, piece1RollIndex, piece2RollIndex, roundCount));

            // 输出棋子1的状态
            System.out.println("回合 " + roundCount + "：");
            System.out.println("棋子1当前位置: " + piece1Position);
            System.out.println("棋子1投出的点数: " + roll1);
            piece1Position = movePiece(piece1Position, roll1, boardSize);
            System.out.println("棋子1新位置: " + piece1Position);

            // 棋子1到达拐点
            if (!piece1ReachedTurn) {
                if ((variant5 && piece1Position == 36) || (!variant5 && piece1Position == 18)) {
                    piece1ReachedTurn = true;
                    System.out.println("棋子1到达拐点，转为新坐标系统开始");
                }
            }

            // 输出棋子2的状态
            System.out.println("棋子2当前位置: " + piece2Position);
            System.out.println("棋子2投出的点数: " + roll2);
            piece2Position = movePiece(piece2Position, roll2, boardSize);
            System.out.println("棋子2新位置: " + piece2Position);

            // 棋子2到达拐点
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

            // 询问是否撤回
            System.out.println("是否撤回当前回合？(y/n)");
            String undoChoice = scanner.next();
            if (undoChoice.equalsIgnoreCase("y")) {
                // 撤回到上一个回合
                GameState previousState = gameHistory.pop();
                piece1Position = previousState.piece1Position;
                piece2Position = previousState.piece2Position;
                piece1ReachedTurn = previousState.piece1ReachedTurn;
                piece2ReachedTurn = previousState.piece2ReachedTurn;
                piece1NewCoord = previousState.piece1NewCoord;
                piece2NewCoord = previousState.piece2NewCoord;
                piece1RollIndex = previousState.piece1RollIndex;
                piece2RollIndex = previousState.piece2RollIndex;
                roundCount = previousState.roundCount;
                System.out.println("撤回到回合 " + roundCount);
            }
        }

        System.out.println("游戏结束，回合数: " + roundCount);
    }

    // 定义一个回合状态类
    static class GameState {
        int piece1Position;
        int piece2Position;
        boolean piece1ReachedTurn;
        boolean piece2ReachedTurn;
        int piece1NewCoord;
        int piece2NewCoord;
        int piece1RollIndex;
        int piece2RollIndex;
        int roundCount;

        GameState(int piece1Position, int piece2Position, boolean piece1ReachedTurn, boolean piece2ReachedTurn,
                  int piece1NewCoord, int piece2NewCoord, int piece1RollIndex, int piece2RollIndex, int roundCount) {
            this.piece1Position = piece1Position;
            this.piece2Position = piece2Position;
            this.piece1ReachedTurn = piece1ReachedTurn;
            this.piece2ReachedTurn = piece2ReachedTurn;
            this.piece1NewCoord = piece1NewCoord;
            this.piece2NewCoord = piece2NewCoord;
            this.piece1RollIndex = piece1RollIndex;
            this.piece2RollIndex = piece2RollIndex;
            this.roundCount = roundCount;
        }
    }

    // 校验输入是否升序
    public static boolean isAscending(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) <= s.charAt(i - 1)) return false;
        }
        return true;
    }

    // 棋子在大圆盘上移动
    public static int movePiece(int currentPosition, int roll, int boardSize) {
        currentPosition += roll;
        if (currentPosition > boardSize) {
            currentPosition -= boardSize;
        }
        return currentPosition;
    }

    // 普通规则：超过终点直接结束
    public static int[] handleNormalRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint) {
        String name = isPiece1 ? "棋子1" : "棋子2";
        if (reachedTurn) {
            newCoord += roll;
            if (newCoord >= endPoint) {
                System.out.println(name + "到达或超过终点，游戏结束！");
                return new int[]{0, newCoord};  // 返回0表示终点已到，游戏结束
            }
            System.out.println(name + "的新坐标: " + newCoord);
        } else {
            System.out.println(name + "当前位置: " + position);
        }
        return new int[]{position, newCoord}; // 返回当前坐标和新坐标
    }

    // 变体规则2：必须精确到达终点，超出则反向
    public static int[] handleVariantRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint, int boardSize) {
        String name = isPiece1 ? "棋子1" : "棋子2";
        int turnPoint = isPiece1 ? (boardSize == 36 ? 36 : 18) : (boardSize == 36 ? 18 : 9);

        if (reachedTurn) {
            newCoord += roll;
            if (newCoord > endPoint) {
                int overshoot = newCoord - endPoint;  // 计算超出的步数
                newCoord = endPoint - overshoot;  // 反向调整新坐标

                if (newCoord < 0) {
                    // 如果反向后坐标为负数，则回到起点并沿圆盘逆时针走
                    int remaining = -newCoord;
                    newCoord = 0;
                    position = turnPoint;
                    position -= remaining;
                    if (position <= 0) position += boardSize;  // 如果越过起点，重新回到末尾
                    System.out.println(name + "退回拐点后，继续在圆盘逆时针走，当前位置: " + position);
                } else {
                    System.out.println(name + "超出终点，反向移动到新坐标: " + newCoord);
                }
            }

            if (newCoord == endPoint) {
                System.out.println(name + "到达终点！游戏结束！");
                return new int[]{0, newCoord};  // 返回0表示终点已到，游戏结束
            }

            if (position != 0) System.out.println(name + "的新坐标: " + newCoord);
        } else {
            System.out.println(name + "当前位置: " + position);
        }

        return new int[]{position, newCoord}; // 返回当前坐标和新坐标
    }
}
