package com.yeild.restfulapi.models;

public class ApiResponse<T> {
    public int code;
    public String message;
    public T detail;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResponse(int code, String message, T detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public int code() {
        return code;
    }

    public ApiResponse<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String message() {
        return message;
    }

    public ApiResponse<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T detail() {
        return detail;
    }

    public ApiResponse<T> setDetail(T detail) {
        this.detail = detail;
        return this;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "\n\tcode=" + code +
                "\n\t, message='" + message + '\'' +
                "\n\t, detail=" + detail +
                "\n}";
    }
}
