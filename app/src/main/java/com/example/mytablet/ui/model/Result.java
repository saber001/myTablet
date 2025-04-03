package com.example.mytablet.ui.model;

import java.util.List;

public class Result<T> {
    private int total;   // 适用于分页接口
    private List<T> rows; // 适用于分页列表接口
    private T data;       // 适用于单对象或非分页列表
    private int code;
    private String msg;

    // Getter 和 Setter
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<T> getRows() { return rows; }
    public void setRows(List<T> rows) { this.rows = rows; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    // 适配不同接口的数据获取方式
    public List<T> getList() {
        return rows != null ? rows : (data instanceof List ? (List<T>) data : null);
    }
}

