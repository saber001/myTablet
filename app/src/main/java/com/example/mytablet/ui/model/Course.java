package com.example.mytablet.ui.model;


import java.io.Serializable;

public class Course implements Serializable {
    private String id;
    private String clazzName;
    private String clazzDate;
    private String clazzBegin;
    private String clazzEnd;
    private String teacherName;
    private String roomName;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClazzName() { return clazzName; }
    public void setClazzName(String clazzName) { this.clazzName = clazzName; }

    public String getClazzDate() { return clazzDate; }
    public void setClazzDate(String clazzDate) { this.clazzDate = clazzDate; }

    public String getClazzBegin() { return clazzBegin; }
    public void setClazzBegin(String clazzBegin) { this.clazzBegin = clazzBegin; }

    public String getClazzEnd() { return clazzEnd; }
    public void setClazzEnd(String clazzEnd) { this.clazzEnd = clazzEnd; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}

