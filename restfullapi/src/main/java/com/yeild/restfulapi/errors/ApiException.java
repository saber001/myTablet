package com.yeild.restfulapi.errors;

import com.yeild.restfulapi.models.ApiResponse;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import retrofit2.HttpException;
import retrofit2.Response;

public class ApiException extends RuntimeException {
    public int error;
    public ApiResponse<Map<String, Object>> response;
    public String reason;
    public Request original_req;
    public Response original_resp;

    public ApiException(Throwable cause) {
        this(cause==null?"":cause.getMessage(), cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof HttpException) {
            HttpException httpException = (HttpException) cause;
            if (httpException.response() != null) {
                this.original_resp = httpException.response();
                if (this.original_resp != null)
                    this.original_req = this.original_resp.raw().request();
            }
        }
    }

    public ApiException(int error, String reason) {
        super(reason);
        this.error = error;
        this.reason = reason;
    }

    public ApiException(int error, String reason, Throwable cause) {
        this(reason, cause);
        this.error = error;
        this.reason = reason;
    }

    public ApiException(Throwable cause, ApiResponse<Map<String, Object>> response) {
        this(cause);
        this.response = response;
        this.error = response.code();
        this.reason = response.message();
    }

    public ApiException(ApiResponse<Map<String, Object>> response) {
        this(null, response);
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");
    public static String logRequest(Request request, boolean logHeaders, boolean logBody) throws IOException {
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        StringBuilder requestMessage = new StringBuilder();
        requestMessage.append("\n┌──────────────────────────────────────────Request──────────────────────────────────────────");
        requestMessage.append("\n├ ").append("URL\t").append(request.url());
        requestMessage.append("\n├ ").append("Method\t@").append(request.method());
        if (!logHeaders && hasRequestBody) {
            requestMessage.append("\n├ ").append("Content-Type\t@").append(requestBody.contentType());
            requestMessage.append("\n├ ").append("ContentLength\t@");
            try {
                requestMessage.append(requestBody.contentLength());
            } catch (IOException e) {
                requestMessage.append("Error: ").append(e.getMessage());
            }
        }
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestMessage.append("\n├ ").append("Content-Type\t@").append(requestBody.contentType());
                }
                requestMessage.append("\n├ ").append("ContentLength\t@");
                try {
                    requestMessage.append(requestBody.contentLength());
                } catch (IOException e) {
                    requestMessage.append("Error: ").append(e.getMessage());
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
        return requestMessage.toString();
    }

    public static String logResponse(Response response, boolean logHeaders, boolean logBody) throws IOException {
        StringBuilder responseMessage = new StringBuilder();
        responseMessage.append("\n┌──────────────────────────────────────────Response──────────────────────────────────────────");
        responseMessage.append("\n├ ").append("URL\t").append(response.raw().request().url());
        ResponseBody responseBody = response.errorBody();
        long contentLength = responseBody == null ? -1:responseBody.contentLength();
        responseMessage.append("\n├ ").append("Status code\t").append(response.code());
        if(!(response.message().isEmpty()))
            responseMessage.append("\n├ ").append("Message\t").append(response.message());
        if(!logHeaders)
            responseMessage.append("\n├ ").append("ContentLength\t")
                    .append(contentLength != -1 ? contentLength + "-byte" : "unknown-length");
        if (logHeaders) {
            Headers headers = response.headers();
            responseMessage.append("\n├ ").append("Headers {");
            for (int i = 0, count = headers.size(); i < count; i++) {
                responseMessage.append("\n├ ").append("\t").append(logHeader(headers, i));
            }
            responseMessage.append("\n├ ").append("}");

            if (!logBody || !HttpHeaders.hasBody(response.raw())) {
            } else if (bodyHasUnknownEncoding(response.headers())) {
                responseMessage.append("\n├ ").append("Body\t").append("encoded body omitted");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.getBuffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
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
                    return responseMessage.toString();
                }

                if (contentLength != 0) {
                    responseMessage.append("\n├ ").append("Body\t").append(buffer.clone().readString(charset));
                }

                if (gzippedLength != null) {
                    responseMessage.append("\n├ ").append("Body\t").append(buffer.size() + "-byte, ").append("gzipped-byte body omitted");
                } else {
                }
            }
        }
        responseMessage.append("\n└────────────────────────────────────────────End────────────────────────────────────────────");
        return responseMessage.toString();
    }

    private static String logHeader(Headers headers, int i) {
        String value = headers.value(i);
        return headers.name(i) + ": " + value;
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private static boolean isPlaintext(Buffer buffer) {
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

    public String getErrorDetail() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (getCause() == null) this.printStackTrace(new PrintStream(outputStream));
        else getCause().printStackTrace(new PrintStream(outputStream));
        String cause = outputStream.toString();
        String result = "\nApiException {" +
                "\n\terror=" + error +
                "\n\t, reason='" + reason + "'";
        if (response != null) result += "\n\t, response = " + response.toString().replaceAll("\n","\n\t");
        if (original_req != null) {
            try {
                result += logRequest(original_req,true, true);
            } catch (IOException e) {
                result += "\n\t Request Print error: "+e.getMessage();
            }
        }
        if (original_resp != null) {
            try {
                result += logResponse(original_resp,true, true);
            } catch (IOException e) {
                result += "\n\t Response Print error: "+e.getMessage();
            }
        }
        result += "\n\t, Cause： " + cause +
                "\n}";
        return result;
    }
}
