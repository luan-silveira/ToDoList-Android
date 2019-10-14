package br.com.luansilveira.todolist.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Luan Christian Nascimento da Silveira
 */
public class DateCalendar {

    private Calendar calendar;

    public DateCalendar() {
        this.calendar = Calendar.getInstance();
    }

    public DateCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public static DateCalendar now() {
        return new DateCalendar();
    }

    public static DateCalendar fromDate(Date date) {
        if (date == null) return null;
        return new DateCalendar().setTime(date);
    }

    public static DateCalendar fromTime(int time) {
        return new DateCalendar().setTime(time);
    }

    public static DateCalendar fromString(String date, String format) throws ParseException {
        return fromString(date, format, Locale.getDefault());
    }

    public static DateCalendar fromString(String date, String format, Locale locale) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format, locale);
        return fromDate(dateFormat.parse(date));
    }

    public static Date yesterday() {
        return new DateCalendar().clearHourMinuteSecond().subDays(1).getDate();
    }

    public static DateCalendar today() {
        return new DateCalendar().clearHourMinuteSecond();
    }

    public static DateCalendar tomorrow() {
        return new DateCalendar().clearHourMinuteSecond().addDays(1);
    }

    public DateCalendar setTime(Date time) {
        this.calendar.setTime(time);
        return this;
    }

    public Date getTime() {
        return this.calendar.getTime();
    }

    public long getTimeMillis() {
        return this.calendar.getTimeInMillis();
    }

    public DateCalendar setDay(int day) {
        this.calendar.set(Calendar.DAY_OF_MONTH, day);
        return this;
    }

    public int getDay() {
        return this.calendar.get(Calendar.DAY_OF_MONTH);
    }

    public DateCalendar addDays(int days) {
        this.calendar.add(Calendar.DAY_OF_MONTH, days);
        return this;
    }

    public DateCalendar subDays(int days) {
        this.addDays(-days);
        return this;
    }

    public DateCalendar setDayOfWeek(int dayOfWeek) {
        this.calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return this;
    }

    public DateCalendar addDaysOfWeek(int daysOfWeek) {
        this.calendar.add(Calendar.DAY_OF_WEEK, daysOfWeek);
        return this;
    }

    public DateCalendar subDaysOfWeek(int days) {
        this.addDaysOfWeek(-days);
        return this;
    }

    public int getDayOfWeek() {
        return this.calendar.get(Calendar.DAY_OF_WEEK);
    }

    public DateCalendar setMonth(int month) {
        this.calendar.set(Calendar.MONTH, month);
        return this;
    }

    public DateCalendar addMonths(int months) {
        this.calendar.add(Calendar.MONTH, months);
        return this;
    }

    public DateCalendar subMonths(int months) {
        this.addMonths(-months);
        return this;
    }

    public int getMonth() {
        return this.calendar.get(Calendar.MONTH);
    }

    public DateCalendar setYear(int year) {
        this.calendar.set(Calendar.YEAR, year);
        return this;
    }

    public DateCalendar addYears(int years) {
        this.calendar.add(Calendar.YEAR, years);
        return this;
    }

    public DateCalendar subYears(int years) {
        this.addYears(-years);
        return this;
    }

    public int getYear() {
        return this.calendar.get(Calendar.YEAR);
    }

    public DateCalendar setHour(int hour) {
        this.calendar.set(Calendar.HOUR_OF_DAY, hour);
        return this;
    }

    public DateCalendar addHours(int hours) {
        this.calendar.add(Calendar.HOUR_OF_DAY, hours);
        return this;
    }

    public DateCalendar subHours(int hours) {
        this.addHours(-hours);
        return this;
    }

    public int getHour() {
        return this.calendar.get(Calendar.HOUR_OF_DAY);
    }

    public DateCalendar setMinute(int minute) {
        this.calendar.set(Calendar.MINUTE, minute);
        return this;
    }

    public DateCalendar addMinutes(int minutes) {
        this.calendar.add(Calendar.MINUTE, minutes);
        return this;
    }

    public DateCalendar subMinutes(int minutes) {
        this.addMinutes(-minutes);
        return this;
    }

    public int getMinute() {
        return this.calendar.get(Calendar.MINUTE);
    }

    public DateCalendar setSecond(int second) {
        this.calendar.set(Calendar.SECOND, second);
        return this;
    }

    public DateCalendar addSeconds(int seconds) {
        this.calendar.add(Calendar.SECOND, seconds);
        return this;
    }

    public DateCalendar subSeconds(int seconds) {
        this.addSeconds(-seconds);
        return this;
    }

    public int getSecond() {
        return this.calendar.get(Calendar.SECOND);
    }

    public DateCalendar setMillisecond(int millisecond) {
        this.calendar.set(Calendar.MILLISECOND, millisecond);
        return this;
    }

    public int diff(int field, DateCalendar dateCalendar) {
        return diff(field, dateCalendar, false);
    }

    /**
     * Calcula a diferença entre uma data e outra.
     *
     * @param field
     * @param dateCalendar
     * @param absolute
     * @return
     */
    public int diff(int field, DateCalendar dateCalendar, boolean absolute) {
        int diff = this.calendar.get(field) - dateCalendar.getCalendar().get(field);
        return absolute ? Math.abs(diff) : diff;
    }

    public int diff(int field, Date date, boolean absolute) {
        return diff(field, DateCalendar.fromDate(date), absolute);
    }

    public DateCalendar clearHourMinuteSecond() {
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        return this;
    }

    public DateCalendar setTime(int millis) {
        this.calendar.setTimeInMillis(millis);
        return this;
    }

    public Calendar getCalendar() {
        return this.calendar;
    }

    public Date getDate() {
        return this.calendar.getTime();
    }

    public DateCalendar startOfDay() {
        return this.clearHourMinuteSecond();
    }

    public DateCalendar endOfDay() {
        return this.setHour(23).setMinute(59).setSecond(59).setMillisecond(999);
    }

    public DateCalendar startOfWeek() {
        return this.setDayOfWeek(calendar.getFirstDayOfWeek());
    }

    public DateCalendar endOfWeek() {
        return this.setDayOfWeek(calendar.getFirstDayOfWeek()).addDaysOfWeek(6);
    }

    public DateCalendar startOfMonth() {
        return this.setDay(1);
    }

    public DateCalendar endOfMonth() {
        return this.setDay(calendar.getActualMaximum(Calendar.MONTH));
    }

    public DateCalendar startOfYear() {
        return this.setMonth(Calendar.JANUARY);
    }

    public DateCalendar endOfYear() {
        return this.setMonth(Calendar.DECEMBER);
    }

    /**
     * Verifica se a data (apenas a data, não a hora ou o fuso horário) corresponde à de hoje.
     *
     * @return boolean
     */
    public boolean isToday() {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.DAY_OF_MONTH) == this.calendar.get(Calendar.DAY_OF_MONTH) &&
                today.get(Calendar.MONTH) == this.calendar.get(Calendar.MONTH) &&
                today.get(Calendar.YEAR) == this.calendar.get(Calendar.YEAR);
    }

    /**
     * Verifica se a data (apenas a data, não a hora ou o fuso horário) corresponde à de ontem.
     *
     * @return boolean
     */
    public boolean isYesterday() {
        Calendar yesterday = new DateCalendar().subDays(1).getCalendar();
        return yesterday.get(Calendar.DAY_OF_MONTH) == this.calendar.get(Calendar.DAY_OF_MONTH) &&
                yesterday.get(Calendar.MONTH) == this.calendar.get(Calendar.MONTH) &&
                yesterday.get(Calendar.YEAR) == this.calendar.get(Calendar.YEAR);
    }

    /**
     * Verifica se a data (apenas a data, não a hora ou o fuso horário) corresponde à de amanhã.
     *
     * @return boolean
     */
    public boolean isTomorrow() {
        Calendar tomorrow = new DateCalendar().addDays(1).getCalendar();
        return tomorrow.get(Calendar.DAY_OF_MONTH) == this.calendar.get(Calendar.DAY_OF_MONTH) &&
                tomorrow.get(Calendar.MONTH) == this.calendar.get(Calendar.MONTH) &&
                tomorrow.get(Calendar.YEAR) == this.calendar.get(Calendar.YEAR);
    }

    /**
     * Verifica se a data pertence à semana atual.
     *
     * @return boolean
     */
    public boolean isCurrentWeek() {
        DateCalendar dateCalendar = new DateCalendar();
        if (this.equals(dateCalendar)) return true;

        int day = this.getDay();
        int firstDay = dateCalendar.startOfWeek().getDay();
        int firstMonth = dateCalendar.getMonth();
        int lastDayOfMonth = dateCalendar.getCalendar().getActualMaximum(Calendar.DAY_OF_MONTH);
        int lastDay = dateCalendar.endOfWeek().getDay();
        int lastMonth = dateCalendar.getMonth();

        boolean currentWeek;
        if (firstMonth < lastMonth) {
            currentWeek = ((day >= firstDay && day <= lastDayOfMonth) || (day >= 1 && day <= lastDay)) &&
                    this.getMonth() >= firstMonth && this.getMonth() <= lastMonth;
        } else {
            currentWeek = day >= firstDay && day <= lastDay && this.getMonth() == firstMonth;
        }

        currentWeek = currentWeek && this.getYear() == dateCalendar.getYear();

        return currentWeek;

    }

    /**
     * Verifica se a data pertence ao ano atual.
     *
     * @return boolean
     */
    public boolean isCurrentYear() {
        return this.calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DateCalendar)) return false;

        return this.calendar.equals(((DateCalendar) obj).getCalendar());
    }

    @NonNull
    @Override
    public String toString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:sss", Locale.getDefault()).format(getDate());
    }

    public String format(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(getDate());
    }

    public String format(String format, Locale locale) {
        return new SimpleDateFormat(format, locale).format(getDate());
    }
}