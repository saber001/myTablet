package com.example.mytablet.ui.model;

import java.io.Serializable;
import java.util.List;

public class DayInfo implements Serializable{
    private int day;
    private String courseCount;
    private List<Course> courses; // 存储课程列表
    private boolean isToday;      // 是否是今天
    private boolean isEmptyDay;

    public DayInfo(int day, String courseCount, List<Course> courses) {
        this.day = day;
        this.courseCount = courseCount;
        this.courses = courses;
        this.isToday = false;
        this.isEmptyDay = false;
    }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public String getCourseCount() { return courseCount; }
    public void setCourseCount(String courseCount) { this.courseCount = courseCount; }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }

    public void setToday(boolean today) {
        isToday = today;
    }
    public void setEmptyDay(boolean emptyDay) {
        isEmptyDay = emptyDay;
    }
    public boolean isToday() {
        return isToday;
    }
    public boolean isEmptyDay() {
        return isEmptyDay;
    }
}


