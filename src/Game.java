import java.util.Random;
import java.util.Scanner;

public class Game {
    public static void main(String[] args) {
        // 创建Scanner对象获取用户输入
        Scanner scanner = new Scanner(System.in);

        // 选择游戏规则
        System.out.println("请选择游戏规则：");
        System.out.println("1. 普通规则");
        System.out.println("2. 变体规则");
        int ruleChoice = scanner.nextInt();

        // 棋子初始位置
        int piece1Position = 1;
        int piece2Position = 10;

        // 标志，表示棋子是否已经经过拐点
        boolean piece1ReachedTurn = false;
        boolean piece2ReachedTurn = false;

        // 新坐标系统中的棋子位置
        int piece1NewCoord = 0; // 棋子1的新坐标（从1开始）
        int piece2NewCoord = 0; // 棋子2的新坐标（从1开始）

        // 随机数生成器
        Random random = new Random();

        // 回合计数器
        int roundCount = 0;

        // 游戏循环，直到两个棋子都到达终点
        while (piece1Position != 0 && piece2Position != 0) {
            roundCount++;  // 每次循环开始时增加回合计数

            // 投骰子，获取两个棋子的点数
            int roll1 = random.nextInt(6) + 1;
            int roll2 = random.nextInt(6) + 1;

            // 输出骰子点数
            System.out.println("回合 " + roundCount + "：");
            System.out.println("棋子1摇到的点数: " + roll1);
            System.out.println("棋子2摇到的点数: " + roll2);

            // 移动棋子1
            piece1Position = movePiece(piece1Position, roll1);

            // 棋子1到达18时，切换到新坐标系统
            if (piece1Position == 18 && !piece1ReachedTurn) {
                piece1ReachedTurn = true;
                System.out.println("棋子1到达18，转为新坐标系统开始");
            }

            // 移动棋子2
            piece2Position = movePiece(piece2Position, roll2);

            // 棋子2到达9时，切换到新坐标系统
            if (piece2Position == 9 && !piece2ReachedTurn) {
                piece2ReachedTurn = true;
                System.out.println("棋子2到达9，转为新坐标系统开始");
            }

            // 处理棋子1在新坐标系统中的运动
            if (piece1ReachedTurn) {
                piece1NewCoord += roll1; // 新坐标系统中加上投出的点数

                // 如果超出终点，普通规则直接结束（不反向）
                if (ruleChoice == 1 && piece1NewCoord > 3) {
                    System.out.println("棋子1超出终点，游戏结束！");
                    piece1Position = 0; // 游戏结束，棋子1胜利
                } else if (ruleChoice == 2 && piece1NewCoord > 3) {
                    // 变体规则：超出终点，反向走
                    int overshoot = piece1NewCoord - 3;
                    piece1NewCoord = 3 - overshoot; // 反向走回终点
                    System.out.println("棋子1超出终点，反向移动到新坐标: " + piece1NewCoord);
                }

                // 如果坐标变成负数，设置为0
                if (piece1NewCoord < 0) {
                    piece1NewCoord = 0;
                    System.out.println("棋子1的新坐标变为负数，调整为: " + piece1NewCoord);
                }

                // 输出棋子1的新坐标
                if (piece1NewCoord == 3) {
                    System.out.println("棋子1到达终点！");
                    piece1Position = 0; // 游戏结束，棋子1胜利
                } else {
                    System.out.println("棋子1的新坐标: " + piece1NewCoord);
                }
            } else {
                System.out.println("棋子1当前位置: 1"); // 仍在移动到拐点
            }

            // 处理棋子2在新坐标系统中的运动
            if (piece2ReachedTurn) {
                piece2NewCoord += roll2; // 新坐标系统中加上投出的点数

                // 如果超出终点，普通规则直接结束（不反向）
                if (ruleChoice == 1 && piece2NewCoord > 3) {
                    System.out.println("棋子2超出终点，游戏结束！");
                    piece2Position = 0; // 游戏结束，棋子2胜利
                } else if (ruleChoice == 2 && piece2NewCoord > 3) {
                    // 变体规则：超出终点，反向走
                    int overshoot = piece2NewCoord - 3;
                    piece2NewCoord = 3 - overshoot; // 反向走回终点
                    System.out.println("棋子2超出终点，反向移动到新坐标: " + piece2NewCoord);
                }

                // 如果坐标变成负数，设置为0
                if (piece2NewCoord < 0) {
                    piece2NewCoord = 0;
                    System.out.println("棋子2的新坐标变为负数，调整为: " + piece2NewCoord);
                }

                // 输出棋子2的新坐标
                if (piece2NewCoord == 3) {
                    System.out.println("棋子2到达终点！");
                    piece2Position = 0; // 游戏结束，棋子2胜利
                } else {
                    System.out.println("棋子2的新坐标: " + piece2NewCoord);
                }
            } else {
                System.out.println("棋子2当前位置: 1"); // 仍在移动到拐点
            }

            System.out.println("--------------------");
        }

        // 游戏结束，输出总回合数
        System.out.println("游戏结束，回合数: " + roundCount);
    }

    // 移动棋子，顺时针
    public static int movePiece(int currentPosition, int roll) {
        currentPosition += roll;

        // 如果位置超过18，绕回去
        if (currentPosition > 18) {
            currentPosition = currentPosition - 18;
        }

        return currentPosition;
    }
}
