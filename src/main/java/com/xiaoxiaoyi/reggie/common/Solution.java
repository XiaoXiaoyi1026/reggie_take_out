package com.xiaoxiaoyi.reggie.common;

import java.util.Arrays;

public class Solution {

    public static void main(String[] args) {
        int[] arr = {3, 2, 1};
        int k = 2;
        popSort(arr);
        int[] res = new int[k];
        System.arraycopy(arr, 0, res, 0, k);
        System.out.println(Arrays.toString(res));;
    }

    public static void popSort(int[] arr) {
        // 哨兵，如果内循环没有发生交换则直接返回
        boolean flag = false;
        // 外循环N-1次
        for (int i = 0; i < arr.length - 1; i++) {
            // 内循环(N-1) + (N-2) + (N-3)....+1
            for(int j = 0; j < arr.length - i - 1; j++) {
                // 前面的大于后面的
                if (arr[j] > arr[j + 1]) {
                    // 交换
                    int tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                    // 发生交换
                    flag = true;
                }
            }
            // 如果未发生交换，则直接返回
            if (!flag) {
                break;
            }
        }
    }
}