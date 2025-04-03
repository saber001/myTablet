package com.example.mytablet.ui.model;


public class CourseBean {
    private String id;  // 课程ID

    private String name;  // 课程名称

    private String subjectId;  // 学科ID

    private String subjectName;  // 学科名称

    private String teacherId;  // 教师ID

    private String teacherName;  // 教师姓名

    private int quota;  // 课程容量

    private int ageMin;  // 最小适合年龄

    private int ageMax;  // 最大适合年龄

    private String roomId;  // 教室ID

    private String roomName;  // 教室名称

    private int classCnt;  // 课时数量

    private String info;  // 课程简介

    private String regStartTime;  // 报名开始时间 (格式: yyyy-MM-dd HH:mm:ss)

    private String regEndTime;  // 报名结束时间 (格式: yyyy-MM-dd HH:mm:ss)

    private String regNote;  // 报名须知

    private int regCnt;  // 已报名人数

    private int classFinishCnt;  // 已上课次数

    private String status;  // 课程状态

    private int classStatus;  // 课程上课状态

    // Getter 和 Setter 方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public int getQuota() { return quota; }
    public void setQuota(int quota) { this.quota = quota; }

    public int getAgeMin() { return ageMin; }
    public void setAgeMin(int ageMin) { this.ageMin = ageMin; }

    public int getAgeMax() { return ageMax; }
    public void setAgeMax(int ageMax) { this.ageMax = ageMax; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getClassCnt() { return classCnt; }
    public void setClassCnt(int classCnt) { this.classCnt = classCnt; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public String getRegStartTime() { return regStartTime; }
    public void setRegStartTime(String regStartTime) { this.regStartTime = regStartTime; }

    public String getRegEndTime() { return regEndTime; }
    public void setRegEndTime(String regEndTime) { this.regEndTime = regEndTime; }

    public String getRegNote() { return regNote; }
    public void setRegNote(String regNote) { this.regNote = regNote; }

    public int getRegCnt() { return regCnt; }
    public void setRegCnt(int regCnt) { this.regCnt = regCnt; }

    public int getClassFinishCnt() { return classFinishCnt; }
    public void setClassFinishCnt(int classFinishCnt) { this.classFinishCnt = classFinishCnt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getClassStatus() { return classStatus; }
    public void setClassStatus(int classStatus) { this.classStatus = classStatus; }
}
