package com.example.mytablet.ui.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HomeDetail implements Serializable {
    private CourseBean course;
    private UserBean user;

    public CourseBean getCourse() { return course; }
    public UserBean getUser() { return user; }

    public static class CourseBean implements Serializable {
        /**
         * id : 1900375243529134082
         * name : 插花艺术
         * subjectId : 1897815649560928258
         * subjectName : 手工园艺
         * teacherId : 1897561023741710337
         * teacherName : 张丽
         * quota : 12
         * ageMin : 12
         * ageMax : 20
         * roomId : 1897265241058713608
         * roomName : 手工园艺教室2
         * classCnt : 8
         * info : null
         * regStartTime : 2025-03-26 00:00:00
         * regEndTime : 2025-03-29 23:59:59
         * regNote : null
         * regCnt : 3
         * classFinishCnt : 0
         * status : afoot
         * classStatus : 0
         */

        private String id;
        private String name;
        private String subjectId;
        private String subjectName;
        private String teacherId;
        private String teacherName;
        private int quota;
        private int ageMin;
        private int ageMax;
        private String roomId;
        private String roomName;
        private int classCnt;
        private String info;
        private String regStartTime;
        private String regEndTime;
        private Object regNote;
        private int regCnt;
        private int classFinishCnt;
        private String status;
        private int classStatus;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getTeacherId() {
            return teacherId;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public int getQuota() {
            return quota;
        }

        public int getAgeMin() {
            return ageMin;
        }

        public int getAgeMax() {
            return ageMax;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getRoomName() {
            return roomName;
        }

        public int getClassCnt() {
            return classCnt;
        }

        public String getInfo() {
            return info;
        }

        public String getRegStartTime() {
            return regStartTime;
        }

        public String getRegEndTime() {
            return regEndTime;
        }

        public Object getRegNote() {
            return regNote;
        }

        public int getRegCnt() {
            return regCnt;
        }

        public int getClassFinishCnt() {
            return classFinishCnt;
        }

        public String getStatus() {
            return status;
        }

        public int getClassStatus() {
            return classStatus;
        }
    }

    public static class UserBean implements Serializable {
        private String userId;
        private String userName;
        private String birthday;
        private String sex;
        private String level;
        private String phone;
        private String jobNo;
        private String info;
        private String photo;
        private String photoUrl;
        private String type;
        private Object openid;
        private Object wxId;
        private String status;
        private String createBy;
        private String createTime;
        private Object subjectIds;
        private String subject;
        private int age;

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getBirthday() {
            return birthday;
        }

        public String getSex() {
            return sex;
        }

        public String getLevel() {
            return level;
        }

        public String getPhone() {
            return phone;
        }

        public String getJobNo() {
            return jobNo;
        }

        public String getInfo() {
            return info;
        }

        public String getPhoto() {
            return photo;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getType() {
            return type;
        }

        public Object getOpenid() {
            return openid;
        }

        public Object getWxId() {
            return wxId;
        }

        public String getStatus() {
            return status;
        }

        public String getCreateBy() {
            return createBy;
        }

        public String getCreateTime() {
            return createTime;
        }

        public Object getSubjectIds() {
            return subjectIds;
        }

        public String getSubject() {
            return subject;
        }

        public int getAge() {
            return age;
        }
    }
}
