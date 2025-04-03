package com.example.mytablet.ui.model;

import java.io.Serializable;

public class Notice implements Serializable {

    private String id;
    private String subjectId;
    private String subjectName;
    private String channel;
    private String content;
    private String pubType;
    private String pubStatus;
    private String pubTime;
    private String bpShow;
    private String bpEndTime;
    private String createBy;
    private String createTime;

    public String getId() {
        return id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getChannel() {
        return channel;
    }

    public String getContent() {
        return content;
    }

    public String getPubType() {
        return pubType;
    }

    public String getPubStatus() {
        return pubStatus;
    }

    public String getPubTime() {
        return pubTime;
    }

    public String getBpShow() {
        return bpShow;
    }

    public String getBpEndTime() {
        return bpEndTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public String getCreateTime() {
        return createTime;
    }
}

