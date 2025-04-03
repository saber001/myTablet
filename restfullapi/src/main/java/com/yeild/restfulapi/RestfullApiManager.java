package com.yeild.restfulapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.orhanobut.logger.Logger;
import com.yeild.restfulapi.adapter.RxJavaLiveCallAdapterFactory;
import com.yeild.restfulapi.http.AuthInterceptor;
import com.yeild.restfulapi.http.LoggingInterceptor;
import com.yeild.restfulapi.utils.SSLContextUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RestfullApiManager implements CookieJar {
    private static final Map<String, RestfullApiManager> sApiManagerMap = new HashMap<>();
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    public Converter.Factory converter = null;
    private Configure mConfig;
    private Retrofit mRetrofit;
    private OkHttpClient.Builder mOkHttpBuilder;
    private AuthInterceptor mAuthInterceptor;
    private Context mContext;
    private String mBaseUrl;
    private SharedPreferences mPreference;

    private RestfullApiManager(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    /**
     * 接口管理器
     *
     * @param baseUrl 接口root地址
     * @return
     */
    public static RestfullApiManager getInstance(String baseUrl) {
        if (baseUrl == null || baseUrl.length() < 1) {
            throw new IllegalArgumentException("Plase specify the baseUrl");
        }
        if (sApiManagerMap.get(baseUrl) == null) {
            synchronized (RestfullApiManager.class) {
                if (sApiManagerMap.get(baseUrl) == null) {
                    sApiManagerMap.put(baseUrl, new RestfullApiManager(baseUrl));
                }
            }
        }
        return sApiManagerMap.get(baseUrl);
    }

    public static RestfullApiManager queryByUrl(String url) {
        for (String url_base : sApiManagerMap.keySet()) {
            if (url.startsWith(url_base)) return sApiManagerMap.get(url_base);
        }
        return null;
    }

    public static Configure configure() {
        return new Configure();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
        if (cookies != null && cookies.size() > 0) {
            mAuthInterceptor.authNone();
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        if (cookies != null && cookies.size() > 0) {
            mAuthInterceptor.authNone();
        }
        return cookies == null ? new ArrayList<>() : cookies;
    }

    public RestfullApiManager config(Context context) {
        return config(context, new Configure());
    }

    public RestfullApiManager config(Context context, Configure config) {
        mContext = context;
        mConfig = config;
        boolean isDebug = isDebug(context);
        mPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (mRetrofit == null) {
            mAuthInterceptor = new AuthInterceptor(context);

            File cacheFile = new File(context.getExternalCacheDir(), "apicache");
            Cache cache = new Cache(cacheFile, 100 * 1024 * 1024);

            mOkHttpBuilder = new OkHttpClient.Builder()
                    .cache(cache)
                    .connectTimeout(mConfig.connectTimeout, TimeUnit.SECONDS)
                    .writeTimeout(mConfig.writeTimeout, TimeUnit.SECONDS)
                    .readTimeout(mConfig.readTimeout, TimeUnit.SECONDS);

            mOkHttpBuilder.addInterceptor(mAuthInterceptor);

            if (mBaseUrl.startsWith("https")) {
                SSLContext sslContext = SSLContextUtil.getDefaultSLLContext();
                if (sslContext != null) {
                    TrustManager[] trustAllCerts = new TrustManager[]{
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                                @Override
                                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                                @Override
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return new java.security.cert.X509Certificate[]{};
                                }
                            }
                    };
                    HostnameVerifier verifiedAllHostname = new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    };
                    try {
                        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    }
                    SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                    mOkHttpBuilder.sslSocketFactory(socketFactory, (X509TrustManager) trustAllCerts[0])
                            .hostnameVerifier(verifiedAllHostname);
                }
                mOkHttpBuilder.hostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
            }

            if (config.openSession) {
                mOkHttpBuilder.cookieJar(this);
            }

            if (isDebug) {
                LoggingInterceptor logging = new LoggingInterceptor(new LoggingInterceptor.Logger() {
                    @Override
                    public void log(int level, String tag, String message) {
                        Logger.log(level, tag, message, null);
                    }
                });
                logging.setLevel(LoggingInterceptor.Level.BODY);
                mOkHttpBuilder.addNetworkInterceptor(logging);
            }
            OkHttpClient httpClient = mOkHttpBuilder.build();

            ObjectMapper mapper = new ObjectMapper()
                    .setDateFormat(new SimpleDateFormat(mConfig.dateFormat, Locale.SIMPLIFIED_CHINESE))
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                    .enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .setTimeZone(TimeZone.getTimeZone("GMT+8"))
                    .enable(JsonParser.Feature.ALLOW_COMMENTS);
            this.converter = JacksonConverterFactory.create(mapper);

            mRetrofit = new Retrofit.Builder()
                    .addConverterFactory(this.converter)
                    .addCallAdapterFactory(RxJavaLiveCallAdapterFactory.create())
                    .client(httpClient)
                    .baseUrl(mBaseUrl)
                    .build();
        }
        return this;
    }

    public <T> T create(Class<T> service) {
        if (mRetrofit == null) {
            throw new IllegalArgumentException("Please config the RestfullApiManager before use");
        }
        return mRetrofit.create(service);
    }

    public RestfullApiManager authNone() {
        mAuthInterceptor.authNone();
        cookieStore.clear();
        return this;
    }

    public RestfullApiManager authToken(String token) {
        mAuthInterceptor.authToken(token);
        cookieStore.clear();
        return this;
    }

    public RestfullApiManager authBasic(String username, String password) {
        authBasic(username, password, Charset.forName("UTF-8"));
        return this;
    }

    public RestfullApiManager authBasic(String username, String password, Charset charset) {
        mAuthInterceptor.authBasic(username, password, charset);
        cookieStore.clear();
        return this;
    }

    private boolean isDebug(Context context) {
        return context != null
                && context.getApplicationInfo() != null
                && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public static class Configure {
        private int connectTimeout = 30;
        private int writeTimeout = 30;
        private int readTimeout = 30;
        private String dateFormat = "yyyy-MM-dd HH:mm:ss";
        private boolean openSession = true;

        /**
         * 连接超时，默认30
         *
         * @param connectTimeout 单位：秒
         */
        public Configure connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * 写数据超时，默认30
         *
         * @param writeTimeout 单位：秒
         */
        public Configure writeTimeout(int writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        /**
         * 读数据超时，默认30
         *
         * @param readTimeout 单位：秒
         */
        public Configure readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Date 格式化
         *
         * @param dateFormat 默认：yyyy-MM-dd HH:mm:ss
         */
        public Configure dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public Configure openSession(boolean openSession) {
            this.openSession = openSession;
            return this;
        }
    }
}
