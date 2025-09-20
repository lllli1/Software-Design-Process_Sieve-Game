import java.util.*;

public class Game {
    // 用于变体1返回复杂结果
    static class MoveResult {
        int pos;
        boolean returnedToCircle;
        boolean reachedEndpoint;
        boolean inNewCoord;

        MoveResult(int pos, boolean returnedToCircle, boolean reachedEndpoint, boolean inNewCoord) {
            this.pos = pos;
            this.returnedToCircle = returnedToCircle;
            this.reachedEndpoint = reachedEndpoint;
            this.inNewCoord = inNewCoord;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 选择游戏模式
        System.out.println("请选择游戏模式：");
        System.out.println("1. 两人模式");
        System.out.println("2. 四人模式");
        String playerModeChoice = scanner.next();
        boolean isFourPlayerMode = playerModeChoice.equals("2");

        // 选择游戏规则
        System.out.println("请选择游戏规则：");
        System.out.println("1. 普通规则 (18格, 两个骰子)");
        System.out.println("2. 变体规则1 (精确到达终点，超出则反向)");
        System.out.println("3. 变体规则2 (追击规则)");
        System.out.println("4. 变体规则3 (18格, 一个骰子)");
        System.out.println("5. 变体规则4 (36格, 两个骰子, 拐点到终点=6格)");
        System.out.println("组合变体 (输入2345的顺序组合，比如23, 245, 2345)");
        String ruleChoice = scanner.next();

        boolean normalRule = ruleChoice.equals("1");
        boolean variant1 = ruleChoice.contains("2");
        boolean variant2 = ruleChoice.contains("3");
        boolean variant3 = ruleChoice.contains("4");
        boolean variant4 = ruleChoice.contains("5");

        // 校验输入必须是升序（仅针对组合变体）
        if (!normalRule && !isAscending(ruleChoice)) {
            System.out.println("输入无效！变体规则必须按顺序输入（例如2345），程序结束。");
            return;
        }

        // 询问是否进入测试模式
        System.out.println("是否进入测试模式？(y/n)");
        String testModeChoice = scanner.next();
        boolean isTestMode = testModeChoice.equalsIgnoreCase("y");

        // 初始化棋子的起始位置和拐点
        int[] piecePositions = isFourPlayerMode ? new int[]{1, 5, 10, 14} : new int[]{1, 10};
        int[] turnPoints = isFourPlayerMode ? new int[]{18, 4, 9, 13} : new int[]{18, 9};
        int[] initialPositions = isFourPlayerMode ? new int[]{1, 5, 10, 14} : new int[]{1, 10};
        int numberOfPlayers = isFourPlayerMode ? 4 : 2;

        boolean[] reachedTurn = new boolean[numberOfPlayers];
        int[] newCoords = new int[numberOfPlayers];
        Random random = new Random();
        int roundCount = 0;

        int boardSize = (variant4 ? 36 : 18);
        int endPoint = (variant4 ? 6 : 3);

        // 测试模式骰子预设
        int[][] pieceRolls = {
                {10, 9, 4, 1, 1},
                {1, 1, 1, 3, 2},
                {4, 6, 3, 5, 2},
                {3, 2, 6, 4, 1}
        };
        int[] pieceRollIndices = new int[numberOfPlayers];

        // 为每个棋子保存移动前的状态
        PieceSnapshot[] pieceSnapshots = new PieceSnapshot[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            pieceSnapshots[i] = new PieceSnapshot(piecePositions[i], reachedTurn[i], newCoords[i], pieceRollIndices[i]);
        }

        // 游戏循环
        outer:
        while (!gameOver(piecePositions, reachedTurn)) {
            roundCount++;
            System.out.println("回合 " + roundCount + "：");

            for (int i = 0; i < numberOfPlayers; i++) {
                // 保存移动前状态
                pieceSnapshots[i] = new PieceSnapshot(piecePositions[i], reachedTurn[i], newCoords[i], pieceRollIndices[i]);

                int roll;
                if (isTestMode) {
                    roll = pieceRolls[i][pieceRollIndices[i]];
                    pieceRollIndices[i] = (pieceRollIndices[i] + 1) % pieceRolls[i].length;
                } else {
                    if (variant3) {
                        roll = random.nextInt(6) + 1;
                    } else {
                        roll = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
                    }
                }

                String name = "棋子" + (i + 1);

                // 如果还没进入新坐标体系，先走圆盘
                if (!reachedTurn[i]) {
                    int oldPos = piecePositions[i];
                    piecePositions[i] = movePiece(piecePositions[i], roll, boardSize);
                    System.out.println(name + "当前位置: " + oldPos + " -> 投出的点数: " + roll + " -> 新位置: " + piecePositions[i]);

                    // 检查击中（变体规则2）
                    if (variant2) {
                        checkAndHandleCapture(i, piecePositions, reachedTurn, newCoords, initialPositions, numberOfPlayers, name);
                    }

                    if (piecePositions[i] == turnPoints[i]) {
                        reachedTurn[i] = true;
                        newCoords[i] = 0;
                        System.out.println(name + "到达拐点，进入新坐标系统");
                    }
                } else {
                    // 已进入新坐标体系
                    boolean returnedToCircle = false;
                    boolean reachedEndpoint = false;
                    int currentNewCoord = newCoords[i];

                    // 如果启用变体1，使用精确到达逻辑
                    if (variant1) {
                        MoveResult mr = handleVariant1(reachedTurn[i], roll, piecePositions[i], newCoords[i], endPoint, boardSize, name);
                        if (mr.reachedEndpoint) {
                            piecePositions[i] = 0;
                            newCoords[i] = 0;
                            reachedTurn[i] = false;
                            reachedEndpoint = true;
                        } else if (mr.returnedToCircle) {
                            piecePositions[i] = mr.pos;
                            newCoords[i] = 0;
                            reachedTurn[i] = false;
                            returnedToCircle = true;

                            // 如果回到圆盘后启用变体2，需要检查击中
                            if (variant2) {
                                checkAndHandleCapture(i, piecePositions, reachedTurn, newCoords, initialPositions, numberOfPlayers, name);
                            }
                        } else if (mr.inNewCoord) {
                            newCoords[i] = mr.pos;
                            piecePositions[i] = mr.pos;
                            currentNewCoord = mr.pos;
                        }
                    } else {
                        // 非变体1的情况，使用普通移动逻辑
                        int target = currentNewCoord + roll;

                        if (target >= endPoint) {
                            System.out.println(name + "当前位置(新坐标): " + currentNewCoord + " -> 投出的点数: " + roll + " -> 新位置: " + endPoint + "(新坐标)");
                            System.out.println(name + "到达或超过终点，游戏结束！");
                            piecePositions[i] = 0;
                            newCoords[i] = 0;
                            reachedTurn[i] = false;
                            reachedEndpoint = true;
                        } else {
                            System.out.println(name + "当前位置(新坐标): " + currentNewCoord + " -> 投出的点数: " + roll + " -> 新位置: " + target + "(新坐标)");
                            newCoords[i] = target;
                            piecePositions[i] = target;
                            currentNewCoord = target;
                        }
                    }

                    // 如果到达终点可以选择立即结束循环（可选）
                    if (reachedEndpoint) {
                        // 如果你希望游戏立即结束（跳出所有循环），可以取消下面的注释
                        // break outer;
                    }
                }

                // 每个棋子移动后询问是否撤回
                System.out.println("是否撤回" + name + "的这次移动？(y/n)");
                String undoChoice = scanner.next();
                if (undoChoice.equalsIgnoreCase("y")) {
                    // 恢复到移动前的状态
                    piecePositions[i] = pieceSnapshots[i].position;
                    reachedTurn[i] = pieceSnapshots[i].reachedTurn;
                    newCoords[i] = pieceSnapshots[i].newCoord;
                    pieceRollIndices[i] = pieceSnapshots[i].rollIndex;
                    System.out.println(name + "的移动已撤回");

                    // 撤回可能影响的其他玩家状态（击中相关）
                    // 这里进行简化处理，如果需要完整处理击中撤回，需要更复杂的状态管理
                    // 目前只恢复当前玩家状态
                }
            }

            System.out.println("--------------------");
        }

        System.out.println("游戏结束，回合数: " + roundCount);
    }

    // 棋子状态快照
    static class PieceSnapshot {
        int position;
        boolean reachedTurn;
        int newCoord;
        int rollIndex;

        PieceSnapshot(int position, boolean reachedTurn, int newCoord, int rollIndex) {
            this.position = position;
            this.reachedTurn = reachedTurn;
            this.newCoord = newCoord;
            this.rollIndex = rollIndex;
        }
    }

    // 检查并处理击中事件
    public static void checkAndHandleCapture(int currentPlayer, int[] piecePositions, boolean[] reachedTurn,
                                             int[] newCoords, int[] initialPositions, int numberOfPlayers, String currentPlayerName) {
        int currentPosition = piecePositions[currentPlayer];

        // 检查是否有其他玩家在同一位置
        for (int j = 0; j < numberOfPlayers; j++) {
            if (j != currentPlayer && piecePositions[j] == currentPosition && piecePositions[j] != 0) {
                // 发现击中事件
                String capturedPlayerName = "棋子" + (j + 1);
                System.out.println(currentPlayerName + " 击中了 " + capturedPlayerName + "！");

                // 将被击中的玩家送回初始位置
                piecePositions[j] = initialPositions[j];
                reachedTurn[j] = false;  // 重置状态
                newCoords[j] = 0;       // 重置新坐标

                System.out.println(capturedPlayerName + " 被送回初始位置: " + initialPositions[j]);
                break; // 一次只能击中一个玩家
            }
        }
    }

    // 修复后的gameOver函数：只有当位置为0且不在新坐标系统中时，才表示到达终点
    public static boolean gameOver(int[] piecePositions, boolean[] reachedTurn) {
        for (int i = 0; i < piecePositions.length; i++) {
            if (piecePositions[i] == 0 && !reachedTurn[i]) {
                return true;
            }
        }
        return false;
    }

    // 保存状态
    static class GameState {
        int[] piecePositions;
        boolean[] reachedTurn;
        int[] newCoords;
        int[] pieceRollIndices;
        int roundCount;

        GameState(int[] piecePositions, boolean[] reachedTurn, int[] newCoords, int[] pieceRollIndices, int roundCount) {
            this.piecePositions = piecePositions.clone();
            this.reachedTurn = reachedTurn.clone();
            this.newCoords = newCoords.clone();
            this.pieceRollIndices = pieceRollIndices.clone();
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

    // 棋子在圆盘上移动
    public static int movePiece(int currentPosition, int roll, int boardSize) {
        currentPosition += roll;
        if (currentPosition > boardSize) {
            currentPosition -= boardSize;
        }
        return currentPosition;
    }

    // 普通规则（返回新的新坐标值，或者返回0表示到达终点）
    public static int handleNormalRule(boolean reachedTurn, int roll, int position, int newCoord, int endPoint, String name) {
        if (reachedTurn) {
            int target = newCoord + roll;
            if (target >= endPoint) {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll + " -> 新位置: " + endPoint + "(新坐标)");
                System.out.println(name + "到达或超过终点，游戏结束！");
                return 0;
            } else {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll + " -> 新位置: " + target + "(新坐标)");
                return target;
            }
        }
        return position;
    }

    // 变体规则1: 必须精确到达终点，否则反走剩余步数
    public static MoveResult handleVariant1(boolean reachedTurn, int roll, int position, int newCoord, int endPoint, int boardSize, String name) {
        if (!reachedTurn) {
            return new MoveResult(position, false, false, false);
        }

        int target = newCoord + roll;

        if (target == endPoint) {
            System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll + " -> 新位置: " + endPoint + "(新坐标)");
            System.out.println(name + "恰好到达终点，游戏结束！");
            return new MoveResult(0, false, true, false);
        } else if (target < endPoint) {
            System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll + " -> 新位置: " + target + "(新坐标)");
            return new MoveResult(target, false, false, true);
        } else {
            int overshoot = target - endPoint;
            System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll + " -> 超出终点，反走 " + overshoot + " 步");

            int backPos = endPoint - overshoot;

            if (backPos > 0) {
                // 反走后仍在新坐标系统中
                System.out.println(name + "反走后新位置: " + backPos + "(新坐标)");
                return new MoveResult(backPos, false, false, true);
            } else if (backPos == 0) {
                // 反走后回到拐点（新坐标系统的起点）
                System.out.println(name + "反走后回到拐点: " + backPos + "(新坐标)");
                return new MoveResult(0, false, false, true);
            } else {
                // 反走超过拐点，回到圆盘
                int stepsOnCircle = -backPos;
                int circlePos = position - stepsOnCircle;
                while (circlePos <= 0) circlePos += boardSize;

                System.out.println(name + "反走超过拐点，回到圆盘继续倒退 " + stepsOnCircle + " 步 -> 新位置: " + circlePos);
                return new MoveResult(circlePos, true, false, false);
            }
        }
    }

    // 变体规则2: 包含击中规则
    public static int handleVariant2(boolean reachedTurn, int roll, int position, int newCoord, int endPoint, String name) {
        if (reachedTurn) {
            int target = newCoord + roll;
            if (target >= endPoint) {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll + " -> 新位置: " + endPoint + "(新坐标)");
                System.out.println(name + "到达或超过终点，游戏结束！");
                return 0;
            } else {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll + " -> 新位置: " + target + "(新坐标)");
                return target;
            }
        }
        return position;
    }

    // 变体规则3
    public static int handleVariant3(boolean reachedTurn, int roll, int position, int newCoord, int endPoint, String name) {
        if (reachedTurn) {
            int target = newCoord + roll;
            if (target >= endPoint) {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll
                        + " -> 新位置: " + endPoint + "(新坐标)");
                System.out.println(name + "到达或超过终点，游戏结束！");
                return 0;
            } else {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll
                        + " -> 新位置: " + target + "(新坐标)");
                return target;
            }
        }
        return position;
    }

    // 变体规则4
    public static int handleVariant4(boolean reachedTurn, int roll, int position, int newCoord, int endPoint, int boardSize, String name) {
        if (reachedTurn) {
            int target = newCoord + roll;
            if (target >= endPoint) {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll
                        + " -> 新位置: " + endPoint + "(新坐标)");
                System.out.println(name + "到达或超过终点，游戏结束！");
                return 0;
            } else {
                System.out.println(name + "当前位置(新坐标): " + newCoord + " -> 投出的点数: " + roll
                        + " -> 新位置: " + target + "(新坐标)");
                return target;
            }
        }
        return position;
    }
}
