package com.hify.common.http;

import com.hify.common.exception.LlmApiException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * LLM API HTTP客户端
 *
 * 封装 RestTemplate 和 OkHttpClient，提供对 LLM API 的统一调用接口。
 * - 普通请求使用 RestTemplate（连接超时 5s，读超时 60s）
 * - 流式 SSE 请求使用 OkHttpClient（连接超时 5s，读超时 120s）
 *
 * 所有请求记录日志（URL、耗时、状态码），异常统一转为 LlmApiException。
 */
@Slf4j
public class LlmHttpClient {

    private final RestTemplate restTemplate;
    private final OkHttpClient okHttpClient;

    public LlmHttpClient() {
        this.restTemplate = createRestTemplate();
        this.okHttpClient = createOkHttpClient();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(60000);
        return new RestTemplate(factory);
    }

    private RestTemplate createRestTemplate(int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build();
    }

    /**
     * 普通POST请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param body 请求体（JSON字符串）
     * @return 响应体
     * @throws LlmApiException 调用失败时抛出
     */

    public String post(String url, Map<String, String> headers, String body) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                headers.forEach(httpHeaders::add);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );

            stopWatch.stop();
            log.info("LLM POST request: url={}, status={}, duration={}ms",
                url, response.getStatusCode().value(), stopWatch.getTotalTimeMillis());

            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new LlmApiException(LlmApiException.ErrorType.AUTH_FAILED,
                    "API认证失败: " + response.getBody());
            }
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new LlmApiException(LlmApiException.ErrorType.RATE_LIMITED,
                    "API限流: " + response.getBody());
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new LlmApiException(LlmApiException.ErrorType.API_ERROR,
                    "API调用失败: " + response.getStatusCode() + ", " + response.getBody());
            }

            return response.getBody();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            stopWatch.stop();
            log.warn("LLM POST request timeout: url={}, duration={}ms, error={}",
                url, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw new LlmApiException(LlmApiException.ErrorType.TIMEOUT, "请求超时", e);
        } catch (LlmApiException e) {
            throw e;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("LLM POST request error: url={}, duration={}ms, error={}",
                url, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            throw new LlmApiException(LlmApiException.ErrorType.NETWORK_ERROR, "网络错误", e);
        }
    }

    /**
     * 普通GET请求（用于连通性测试）
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param timeoutMs 超时时间（毫秒）
     * @return 响应体
     * @throws LlmApiException 调用失败时抛出
     */
    public String get(String url, Map<String, String> headers, int timeoutMs) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        RestTemplate customRestTemplate = createRestTemplate(timeoutMs);

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                headers.forEach(httpHeaders::add);
            }

            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = customRestTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );

            stopWatch.stop();
            log.info("LLM GET request: url={}, status={}, duration={}ms",
                url, response.getStatusCode().value(), stopWatch.getTotalTimeMillis());

            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new LlmApiException(LlmApiException.ErrorType.AUTH_FAILED,
                    "API认证失败: " + response.getBody());
            }
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new LlmApiException(LlmApiException.ErrorType.RATE_LIMITED,
                    "API限流: " + response.getBody());
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new LlmApiException(LlmApiException.ErrorType.API_ERROR,
                    "API调用失败: " + response.getStatusCode() + ", " + response.getBody());
            }

            return response.getBody();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            stopWatch.stop();
            log.warn("LLM GET request timeout: url={}, duration={}ms, error={}",
                url, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw new LlmApiException(LlmApiException.ErrorType.TIMEOUT, "请求超时", e);
        } catch (LlmApiException e) {
            throw e;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("LLM GET request error: url={}, duration={}ms, error={}",
                url, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            throw new LlmApiException(LlmApiException.ErrorType.NETWORK_ERROR, "网络错误", e);
        }
    }

    /**
     * 流式SSE请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param body 请求体（JSON字符串）
     * @param lineCallback 行回调，每接收到一行数据时调用
     * @throws LlmApiException 调用失败时抛出
     */
    public void stream(String url, Map<String, String> headers, String body, Consumer<String> lineCallback) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .post(RequestBody.create(body, MediaType.parse("application/json; charset=utf-8")));

        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            stopWatch.stop();
            log.info("LLM STREAM request: url={}, status={}, duration={}ms",
                url, response.code(), stopWatch.getTotalTimeMillis());

            if (response.code() == 401) {
                throw new LlmApiException(LlmApiException.ErrorType.AUTH_FAILED,
                    "API认证失败: " + response.body().string());
            }
            if (response.code() == 429) {
                throw new LlmApiException(LlmApiException.ErrorType.RATE_LIMITED,
                    "API限流: " + response.body().string());
            }
            if (!response.isSuccessful()) {
                throw new LlmApiException(LlmApiException.ErrorType.API_ERROR, response.code(),
                    "API调用失败: " + response.code() + ", " + response.body().string());
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new LlmApiException(LlmApiException.ErrorType.API_ERROR, "响应体为空");
            }

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseBody.byteStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCallback.accept(line);
                }
            }
        } catch (IOException e) {
            if (e instanceof java.net.SocketTimeoutException) {
                throw new LlmApiException(LlmApiException.ErrorType.TIMEOUT, "请求超时", e);
            }
            log.error("LLM STREAM request error: url={}, duration={}ms, error={}",
                url, stopWatch.getTotalTimeMillis(), e.getMessage(), e);
            throw new LlmApiException(LlmApiException.ErrorType.NETWORK_ERROR, "网络错误", e);
        }
    }
}