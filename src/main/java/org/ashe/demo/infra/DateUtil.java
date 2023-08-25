package org.ashe.demo.infra;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private DateUtil() {
    }

    public static String getLastDayOfMonth() {
        YearMonth yearMonth = YearMonth.now();
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return lastDayOfMonth.atTime(23, 59, 59).format(formatter);
    }

    public static String getFirstDayOfMonth() {
        YearMonth yearMonth = YearMonth.now();
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return firstDayOfMonth.atTime(0, 0, 0).format(formatter);
    }

    /**
     * 获取当前时间+offSet小时后的时间戳
     * @param offSet 偏移量(小时)
     */
    public static Long getTimestampByOffSet(int offSet) {
        Date currentDate = new Date();

        // 创建一个 Calendar 对象，并将当前时间设置为基准
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // 偏移offSet小时
        calendar.add(Calendar.HOUR_OF_DAY, offSet);

        // 获取增加后的时间
        Date afterAddingHours = calendar.getTime();

        // 获取时间戳
        return afterAddingHours.getTime();
    }

}
