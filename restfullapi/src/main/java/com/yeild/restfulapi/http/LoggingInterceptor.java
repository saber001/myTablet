package com.yeild.restfulapi.http;

import android.util.Log;

import com.yeild.restfulapi.errors.ApiException;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

public class LoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String TAG = "Rest API";

    public enum Level {
        /** No logs. */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    public interface Logger {
        void log(int level, String tag, String message);

        /** A {@link LoggingInterceptor.Logger} defaults output appropriate for the current platform. */
        LoggingInterceptor.Logger DEFAULT = new Logger() {
            @Override
            public void log(int level, String tag, String message) {
                Platform.get().log(level, message, null);
            }
        };
    }

    public LoggingInterceptor() {
        this(LoggingInterceptor.Logger.DEFAULT);
    }

    public LoggingInterceptor(LoggingInterceptor.Logger logger) {
        this.logger = logger;
    }

    private final LoggingInterceptor.Logger logger;

    private volatile Set<String> headersToRedact = Collections.emptySet();

    public void redactHeader(String name) {
        Set<String> newHeadersToRedact = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newHeadersToRedact.addAll(headersToRedact);
        newHeadersToRedact.add(name);
        headersToRedact = newHeadersToRedact;
    }

    private volatile LoggingInterceptor.Level level = LoggingInterceptor.Level.NONE;

    /** Change the level at which this interceptor logs. */
    public LoggingInterceptor setLevel(LoggingInterceptor.Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public LoggingInterceptor.Level getLevel() {
        return level;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        LoggingInterceptor.Level level = this.level;

        Request request = chain.request();
        if (level == LoggingInterceptor.Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == LoggingInterceptor.Level.BODY;
        boolean logHeaders = logBody || level == LoggingInterceptor.Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        StringBuilder requestMessage = new StringBuilder(getClass().getName());
        requestMessage.append("\n┌──────────────────────────────────────────Request──────────────────────────────────────────");
        requestMessage.append("\n├ ").append("URL\t").append(request.url());
        requestMessage.append("\n├ ").append("Method\t@").append(request.method());
        if(connection != null) {
            requestMessage.append("\n├ ").append("Protocol\t@").append(connection.protocol());
        }
        if (!logHeaders && hasRequestBody) {
            requestMessage.append("\n├ ").append("Content-Type\t@").append(requestBody.contentType());
            requestMessage.append("\n├ ").append("ContentLength\t@").append(requestBody.contentLength());
        }

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestMessage.append("\n├ ").append("Content-Type\t@").append(requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    requestMessage.append("\n├ ").append("ContentLength\t@").append(requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            requestMessage.append("\n├ ").append("Headers {");
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    requestMessage.append("\n├ ").append("\t").append(logHeader(headers, i));
                }
            }
            requestMessage.append("\n├ ").append("}");

            if (!logBody || !hasRequestBody) {
//                logger.log("--> END " + request.method());
            } else if (bodyHasUnknownEncoding(request.headers())) {
                requestMessage.append("\n├ ").append("encoded body omitted");
            } else if (requestBody.isDuplex()) {
                requestMessage.append("\n├ ").append("duplex request body omitted");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                requestMessage.append("\n├ ").append("RequestBody {");
                if (isPlaintext(buffer)) {
                    requestMessage.append("\n├ ").append("\t").append(buffer.readString(charset));
                } else {
                    requestMessage.append("\n├ ").append("\t").append("binary body omitted");
                }
                requestMessage.append("\n├ ").append("}");
            }
        }
        requestMessage.append("\n└────────────────────────────────────────────End────────────────────────────────────────────");
        logger.log(Log.DEBUG, TAG, requestMessage.toString());

        long startNs = System.nanoTime();
        StringBuilder responseMessage = new StringBuilder(getClass().getName());
        responseMessage.append("\n┌──────────────────────────────────────────Response──────────────────────────────────────────");
        responseMessage.append("\n├ ").append("URL\t").append(request.url());
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            responseMessage.append("\n├ ").append("HTTP FAILED ").append(e);
            responseMessage.append("\n└────────────────────────────────────────────End────────────────────────────────────────────");
            logger.log(Log.DEBUG, TAG, responseMessage.toString());
            try {
                throw e;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        responseMessage.append("\n├ ").append("Cost time\t").append(tookMs).append(" ms");
        responseMessage.append("\n├ ").append("Status code\t").append(response.code());
        if(!(response.message().isEmpty()))
            responseMessage.append("\n├ ").append("Message\t").append(response.message());
        if(!logHeaders)
            responseMessage.append("\n├ ").append("ContentLength\t").append(bodySize);

        if (logHeaders) {
            Headers headers = response.headers();
            responseMessage.append("\n├ ").append("Headers {");
            for (int i = 0, count = headers.size(); i < count; i++) {
                responseMessage.append("\n├ ").append("\t").append(logHeader(headers, i));
            }
            responseMessage.append("\n├ ").append("}");

            if (!logBody || !HttpHeaders.hasBody(response)) {
            } else if (bodyHasUnknownEncoding(response.headers())) {
                responseMessage.append("\n├ ").append("Body\t").append("encoded body omitted");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.getBuffer().clone();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    try (GzipSource gzippedResponseBody = new GzipSource(buffer)) {
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    }
                }

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    responseMessage.append("\n├ ").append("Body\t").append("binary body omitted");
                    responseMessage.append("\n└────────────────────────────────────────────End────────────────────────────────────────────");
                    return response;
                }

                if (contentLength != 0) {
                    responseMessage.append("\n├ ").append("Body\t").append(buffer.readString(charset));
                }

                if (gzippedLength != null) {
                    responseMessage.append("\n├ ").append("Body\t").append(buffer.size() + "-byte, ").append("gzipped-byte body omitted");
                } else {
//                    requestMessage.append("\n├ ").append("Body\t").append(buffer.size() + "-byte body");
                }
            }
        }
        responseMessage.append("\n└────────────────────────────────────────────End────────────────────────────────────────────");
        int level_resp = Log.DEBUG;
//        if (response.code() >= 500) level_resp = Log.ERROR;
//        else if(response.code() >= 400) level_resp = Log.WARN;
        logger.log(level_resp, TAG, responseMessage.toString());

        return response;
    }

    private String logHeader(Headers headers, int i) {
        String value = headersToRedact.contains(headers.name(i)) ? "██" : headers.value(i);
        return headers.name(i) + ": " + value;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }
}
