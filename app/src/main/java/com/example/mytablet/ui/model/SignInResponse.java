package com.example.mytablet.ui.model;

public class SignInResponse {
   private String userId;
   private String userName;
   private String total;
   private String signinCnt;
   private String signinTime;

   public String getUserId() { return userId; }
   public void setUserId(String userId) { this.userId = userId; }

   public String getUserName() { return userName; }
   public void setUserName(String userName) { this.userName = userName; }

   public String getTotal() { return total; }
   public void setTotal(String total) { this.total = total; }

   public String getSigninCnt() { return signinCnt; }
   public void setSigninCnt(String signinCnt) { this.signinCnt = signinCnt; }

   public String getSigninTime() { return signinTime; }
   public void setSigninTime(String signinTime) { this.signinTime = signinTime; }
}
