import java.util.Random;
import java.util.Scanner;

public class Game {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("请选择游戏规则：");
        System.out.println("1. 普通规则 (18格, 两个骰子)");
        System.out.println("2. 变体规则 (精确到达终点，超出则反向)");
        System.out.println("3. 变体规则2 (追击规则)");
        System.out.println("4. 变体规则3 (18格, 一个骰子)");
        System.out.println("5. 变体规则4 (36格, 两个骰子, 拐点到终点=6格)");
        int ruleChoice = scanner.nextInt();

        System.out.println("是否开启测试模式？(1=是, 0=否)");
        boolean testMode = scanner.nextInt() == 1;

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

            // 生成或输入点数（原始值保存用于打印）
            int roll1Orig, roll2Orig;
            if (testMode) {
                System.out.print("请输入棋子1的点数: ");
                roll1Orig = scanner.nextInt();
                System.out.print("请输入棋子2的点数: ");
                roll2Orig = scanner.nextInt();
            } else {
                if (ruleChoice == 4) {
                    roll1Orig = random.nextInt(6) + 1;
                    roll2Orig = random.nextInt(6) + 1;
                } else {
                    roll1Orig = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
                    roll2Orig = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
                }
            }

            // 复制到可变变量，内部可能会被改为“进入新坐标时剩余的步数”
            int roll1 = roll1Orig;
            int roll2 = roll2Orig;

            System.out.println("回合 " + roundCount + "：");
            System.out.println("棋子1摇到的点数: " + roll1Orig);
            System.out.println("棋子2摇到的点数: " + roll2Orig);

            // 处理：如果未到拐点但投掷跨过拐点 -> 直接进入新坐标系统（把多余步数作为 newCoord 的步数）
            // 计算每个棋子的拐点
            int turnPoint1 = (ruleChoice == 5) ? 36 : 18; // 棋子1 的拐点
            int turnPoint2 = (ruleChoice == 5) ? 18 : 9;  // 棋子2 的拐点

            // --- 处理棋子1移动（在调用 movePiece 或转换进 newCoord 之前） ---
            if (!piece1ReachedTurn) {
                int prev = piece1Position;
                // 顺时针到拐点所需步数（若当前位置已经等于拐点，结果为 0）
                int stepsToTurn = (turnPoint1 - prev + boardSize) % boardSize;
                // 如果 stepsToTurn == 0，说明当前位置就是拐点（尚未标记为 reachedTurn），也算进入新坐标
                if (roll1 >= stepsToTurn) {
                    // 消耗到达拐点所需的步数，剩下的变成 newCoord 的步数
                    int remainder = roll1 - stepsToTurn;
                    // 标记为到达拐点
                    piece1ReachedTurn = true;
                    // 将盘面坐标设置为拐点（新坐标系统开始）
                    piece1Position = turnPoint1;
                    // 把剩余步数留给后续处理（handler 会把 roll 加到 newCoord）
                    roll1 = remainder;
                    System.out.println("棋子1经过拐点，进入新坐标，剩余步数将用于新坐标推进。");
                } else {
                    // 没到拐点，按普通圆盘移动
                    piece1Position = movePiece(piece1Position, roll1, boardSize);
                }
            } else {
                // 已经在新坐标区域或之前到达过拐点：仍然按原逻辑（position 已是拐点或不影响）
                // 但 position 保持不变（在新坐标时 position 表示是否为 0 终止），handler 会处理 newCoord
            }

            // --- 处理棋子2移动（同上） ---
            if (!piece2ReachedTurn) {
                int prev = piece2Position;
                int stepsToTurn = (turnPoint2 - prev + boardSize) % boardSize;
                if (roll2 >= stepsToTurn) {
                    int remainder = roll2 - stepsToTurn;
                    piece2ReachedTurn = true;
                    piece2Position = turnPoint2;
                    roll2 = remainder;
                    System.out.println("棋子2经过拐点，进入新坐标，剩余步数将用于新坐标推进。");
                } else {
                    piece2Position = movePiece(piece2Position, roll2, boardSize);
                }
            } else {
                // 已到拐点，newCoord 由 handler 增加
            }

            // 根据规则处理（注：此处传入的 roll1/roll2 对于已进入新坐标的棋子，表示“进入新坐标后应加的步数”）
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
            } else if (ruleChoice == 3) {
                int[] result1 = handleVariant2Rule(piece1ReachedTurn, roll1, piece1Position, piece1NewCoord,
                        true, 3, boardSize,
                        piece2Position, piece2NewCoord, piece2ReachedTurn, 1, testMode, scanner, random);
                piece1Position = result1[0];
                piece1NewCoord = result1[1];

                int[] result2 = handleVariant2Rule(piece2ReachedTurn, roll2, piece2Position, piece2NewCoord,
                        false, 3, boardSize,
                        piece1Position, piece1NewCoord, piece1ReachedTurn, 10, testMode, scanner, random);
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
            }

            System.out.println("--------------------");
        }

        System.out.println("游戏结束，回合数: " + roundCount);
    }

    // 普通规则：超过终点直接结束
    public static int[] handleNormalRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint) {
        String name = isPiece1 ? "棋子1" : "棋子2";
        if (reachedTurn) {
            // roll 在主循环里已调整为"进入新坐标后要走的步数"
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

    // 追击规则（不需要精确到达）
    public static int[] handleVariant2Rule(boolean reachedTurn, int roll, int position, int newCoord,
                                           boolean isPiece1, int endPoint, int boardSize,
                                           int otherPosition, int otherNewCoord, boolean otherReachedTurn,
                                           int initPosition, boolean testMode, Scanner scanner, Random random) {
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

        // 检查追击（只在圆盘上触发）
        if (!reachedTurn && position == otherPosition) {
            System.out.println(name + "追上了对方，把对方送回起点！");

            // 对方回到起点
            otherPosition = initPosition;

            // 对方立刻重新摇骰子
            int extraRoll;
            if (testMode) {
                System.out.print("请输入被送回的棋子额外点数: ");
                extraRoll = scanner.nextInt();
            } else {
                extraRoll = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
            }

            System.out.println("被送回的棋子额外摇到点数: " + extraRoll);
            otherPosition = movePiece(otherPosition, extraRoll, boardSize);

            if (otherReachedTurn) {
                otherNewCoord += extraRoll;
                System.out.println("被送回的棋子的新坐标: " + otherNewCoord);
            } else {
                System.out.println("被送回的棋子当前位置: " + otherPosition);
            }
        }

        return new int[]{position, newCoord};
    }

    // 变体规则3：一个骰子，逻辑同普通规则
    public static int[] handleOneDiceRule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint) {
        return handleNormalRule(reachedTurn, roll, position, newCoord, isPiece1, endPoint);
    }

    // 变体规则4：36格棋盘，拐点到终点 6 格
    public static int[] handleVariant4Rule(boolean reachedTurn, int roll, int position, int newCoord, boolean isPiece1, int endPoint, int boardSize) {
        return handleNormalRule(reachedTurn, roll, position, newCoord, isPiece1, endPoint);
    }

    // 棋子在大圆盘上移动（顺时针，超过 boardSize 则回到 1）
    public static int movePiece(int currentPosition, int roll, int boardSize) {
        currentPosition += roll;
        while (currentPosition > boardSize) {
            currentPosition -= boardSize;
        }
        return currentPosition;
    }
}
