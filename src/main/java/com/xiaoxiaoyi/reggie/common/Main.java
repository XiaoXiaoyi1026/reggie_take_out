package com.xiaoxiaoyi.reggie.common;

import java.util.Scanner;

public class Main {

    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        int n = INPUT.nextInt();
        System.out.println(fibonacci(n-1));
    }

    public static Integer fibonacci(int n) {
        if (n == 0) {
            return 1;
        }
        if (n == 1) {
            return 2;
        }
        int num1 = 1;
        int num2 = 2;
        for (int i = 2; i < n + 1; i++) {
            num1 = num2;
            if ((i & 1) == 0) {
                num2 = (int) ((num1 * 2) % (Math.pow(10, 9) + 7));
            } else {
                num2 = num1 + 1;
            }
        }
        return num2;
    }

    public static void p2() {
        int res = 0;
        // 总数
        int a = INPUT.nextInt();
        // 隔板数
        int b = INPUT.nextInt();
        // 每一个箱子最多分成几块
        int k = INPUT.nextInt();
        // 每一块最多放几个物体
        int v = INPUT.nextInt();
        while (a > 0) {
            // 1. 拿一个箱子过来
            res++;
            // 2. 计算使用当前剩余的隔板和这个箱子能不能装下a个物品
            if (k > b + 1) {
                if ((b + 1) * v >= a) {
                    System.out.println(res);
                    return;
                } else {
                    // 把当前箱子装满
                    a -= (b + 1) * v;
                    // 挡板用光了
                    b = 0;
                }
            } else {
                if (Math.ceil(a / v) <= k) {
                    System.out.println(res);
                    return;
                } else {
                    // 先把当前箱子装了
                    a -= k * v;
                    b -= k - 1;
                }
            }
            // 3. 如果能装下则直接返回res;
            // 4. 如果不能则先装下能装的物品，然后再去拿箱子
        }
    }

    public static void p1() {
        // 霸榜天数
        int n = INPUT.nextInt();
        // 其与粉丝数
        int m = INPUT.nextInt();
        int[][] nums = new int[n][m];
        int max = 0, tmp = 0, count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                nums[i][j] = INPUT.nextInt();
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                tmp += nums[j][i];
                count++;
                if (count == 8) {
                    tmp++;
                    count = 0;
                }
            }
            if (tmp > max) {
                max = tmp;
            }
            tmp = 0;
        }
        System.out.println(max+1);
    }
}