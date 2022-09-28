package com.xiaoxiaoyi.reggie;

import org.junit.jupiter.api.Test;

public class FileUploadTest {

    @Test
    public void testGetFileSuffix() {
        String fileName = "233333333.jpg";
        System.out.println(fileName.substring(fileName.lastIndexOf(".")));
        System.out.println("1st".substring(0, "1st".length() - 2));
    }


    @Test
    public void countDays() {
        String year = "2022";
        String month = "Jan";
        String day = "2nd";
        int y = Integer.parseInt(year);
        int res = 0;
        res += Integer.parseInt(day.substring(0, day.length() - 2));
        switch (month) {
            // 12
            case "Dec":
                res += 30;
            case "Nov":
                res += 31;
            case "Oct":
                res += 30;
            case "Sep":
                res += 31;
            case "Aug":
                res += 31;
            case "Jul":
                res += 30;
            case "Jun":
                res += 31;
            case "May":
                res += 30;
            case "Apr":
                res += 31;
            case "Mar":
                if (y % 4 == 0 && y % 100 != 0 && y % 400 == 0) {
                    res += 29;
                } else {
                    res += 28;
                }
            case "Feb":
                res += 31;
            case "Jan":
            default:
        }
        System.out.println("day:" + res);
    }

    @Test
    public void test() {
        System.out.println(Math.ceil(3.2));
    }
}
