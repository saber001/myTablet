package com.example.mytablet.ui.model;

public class ViewCourses {

    private String name;
    private String hours;
    private String teacher;
    private String description;
    private String schedule;

    public ViewCourses(String name, String hours, String teacher, String description, String schedule) {
        this.name = name;
        this.hours = hours;
        this.teacher = teacher;
        this.description = description;
        this.schedule = schedule;
    }

    public String getName() { return name; }
    public String getHours() { return hours; }
    public String getTeacher() { return teacher; }
    public String getDescription() { return description; }
    public String getSchedule() { return schedule; }

}
