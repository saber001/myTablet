package com.example.mytablet.ui.api;

import com.example.mytablet.ui.model.BoardInfo;
import com.example.mytablet.ui.model.ClassRoomBean;
import com.example.mytablet.ui.model.Course;
import com.example.mytablet.ui.model.CourseBean;
import com.example.mytablet.ui.model.HomeBean;
import com.example.mytablet.ui.model.HomeDetail;
import com.example.mytablet.ui.model.Notice;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.SignInResponse;
import com.example.mytablet.ui.model.SubjectBean;
import com.example.mytablet.ui.model.TeacherIntro;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("ccms/bp/info")
    Call<Result<BoardInfo>> getBoardInfo();
    @GET("ccms/bp/teachers")
    Call<Result<TeacherIntro>> getTeachers();
    @GET("ccms/bp/rooms")
    Call<Result<ClassRoomBean>> getRooms();
    @GET("ccms/bp/notices")
    Call<Result<List<Notice>>> getLatestNotices();
    @GET("ccms/bp/teacher/{userId}")
    Call<Result<TeacherIntro>> getTeacherDetail(@Path("userId") String userId);

    @GET("ccms/bp/courses")
    Call<Result<CourseBean>> getCourses(
            @Query("subjectId") String subjectId,
            @Query("pageSize") int pageSize,
            @Query("pageNum") int pageNum
    );
    @GET("ccms/bp/courses")
    Call<Result<CourseBean>> getCourses(
            @Query("pageSize") int pageSize,
            @Query("pageNum") int pageNum
    );

    @GET("ccms/bp/course/{courseId}")
    Call<Result<HomeDetail>> getCourseDetail(@Path("courseId") String courseId);
    @GET("ccms/bp/course/calendar")
    Call<Result<Map<String, List<Course>>>> getCourseCalendar(
            @Query("courseId") String courseId,
            @Query("month") String month
    );
    @POST("ccms/bp/signin")
    @FormUrlEncoded
    Call<Result<SignInResponse>> signIn(@Field("signinCode") String signinCode);
    @GET("ccms/bp/course/subject")
    Call<Result<List<SubjectBean>>> getSubjects();
    @GET("ccms/bp/clazz/today")
    Call<Result<List<HomeBean>>> getTodayClasses();

    @GET("/ccms/bp/heart")
    Call<Result<Void>> sendHeartbeat();
}
