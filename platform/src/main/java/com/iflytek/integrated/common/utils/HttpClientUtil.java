package com.iflytek.integrated.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import com.iflytek.integrated.common.HttpResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

/**
 * httpClient工具类
 *
 * @author JourWon
 * @date Created on 2018年4月19日
 */
public class HttpClientUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * JSON格式
     */
    public static final String JSON = "JSON";

    /**
     * 表单格式
     */
    public static final String FORM = "FORM";
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;
    /**
     * 指客户端从服务器读取数据包的间隔超时时间
     */
    public static final int READ_TIMEOUT = 60000;
    /**
     * 长连接保持时间(单位s)
     */
    public static final int KEEP_ALIVE_TIME = 10;
    /**
     * 编码格式。发送编码格式统一用UTF-8
     */
    public static final String ENCODING = "UTF-8";
    /**
     * 指客户端和服务器建立连接的超时时间(单位ms)
     */
    public static final int CONNECT_TIMEOUT = 3000;
    /**
     * 连接池的最大连接数，0代表不限；0需要考虑连接泄露导致系统崩溃的后果
     */
    public static final int MAX_TOTAL_CONNECT = 1000;
    /**
     * 每个路由的最大连接数,如果只调用一个地址,可以将其设置为最大连接数
     */
    public static final int MAX_CONNECT_PER_ROUTE = 200;
    /**
     * 从连接池获取连接的超时时间,不宜过长,单位ms
     */
    public static final int CONNECTION_REQUEST_TIMEOUT = 200;
    /**
     * 针对不同的地址,特别设置不同的长连接保持时间
     */
    public static Map<String,Integer> KEEP_ALIVE_TARGET_HOST;

    /**
     * 发送get请求；不带请求头和请求参数
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpResult doGet(String url) throws Exception {
        return doGet(url, null, null);
    }

    /**
     * 发送get请求；带请求参数
     * @param url    请求地址
     * @param params 请求参数集合
     * @return
     * @throws Exception
     */
    public static HttpResult doGet(String url, Map<String, Object> params) throws Exception {
        return doGet(url, null, params);
    }

    /**
     * 发送get请求；带请求头和请求参数
     *
     * @param url     请求地址
     * @param headers 请求头集合
     * @param params  请求参数集合
     * @return
     * @throws Exception
     */
    public static HttpResult doGet(String url, Map<String, String> headers, Map<String, Object> params) throws Exception {
        // 创建httpClient对象
        CloseableHttpClient httpClient = HttpClientUtil.createHttpClient(MAX_TOTAL_CONNECT, MAX_CONNECT_PER_ROUTE, RETRY_TIMES);
        // 创建访问的地址
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            Set<Entry<String, Object>> entrySet = params.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                uriBuilder.setParameter(entry.getKey(), disposeNull(entry.getValue()));
            }
        }
        // 创建http对象
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();
        httpGet.setConfig(requestConfig);
        // 设置请求头
        httpGet.setHeader("Accept", "*/*");
        httpGet.setHeader("Connection", "Keep-Alive");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        packageHeader(headers, httpGet);
        // 执行请求并获得响应结果
        return resolveHttpClientResult(httpClient, httpGet);
    }

    /**
     * 发送post请求；不带请求头和请求参数
     *
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpResult doPost(String url) throws Exception {
        return doPost(url, null, null);
    }

    /**
     * 发送post请求；带请求参数(表单传值)
     * @param url    请求地址
     * @param params 参数集合
     * @return HttpResult
     * @throws Exception
     */
    public static HttpResult doPost(String url, Map<String, Object> params) throws Exception {
        return doPost(url, null, FORM, params);
    }

    /**
     * 发送post请求；带请求参数
     *
     * @param url    请求地址
     * @param format 数据格式
     * @param params 参数集合
     * @return
     * @throws Exception
     */
    public static HttpResult doPost(String url, String format, Map<String, Object> params) throws Exception {
        return doPost(url, null, format, params);
    }

    /**
     * 发送post请求；带请求头和请求参数
     * @param url     请求地址
     * @param headers 请求头集合
     * @param params  请求参数集合
     * @return
     * @throws Exception
     */
    public static HttpResult doPost(String url, Map<String, String> headers, String format, Map<String, Object> params) throws Exception {
        // 创建httpClient对象
        CloseableHttpClient httpClient = HttpClientUtil.createHttpClient(MAX_TOTAL_CONNECT, MAX_CONNECT_PER_ROUTE, RETRY_TIMES);
        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.setHeader("Connection", "Keep-Alive");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        packageHeader(headers, httpPost);
        // 封装请求参数
        packageParam(params, format, httpPost);
        // 执行请求并获得响应结果
        return resolveHttpClientResult(httpClient, httpPost);
    }

    public static HttpResult doPost(String url, String body) throws Exception {
        // 创建httpClient对象
        CloseableHttpClient httpClient = HttpClientUtil.createHttpClient(MAX_TOTAL_CONNECT, MAX_CONNECT_PER_ROUTE, RETRY_TIMES);
        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.setHeader("Connection", "Keep-Alive");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        packageHeader(null, httpPost);
        // 封装请求参数
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        // 设置到请求的http对象中
        httpPost.setEntity(new StringEntity(body, ENCODING));
        return resolveHttpClientResult(httpClient, httpPost);
    }

    /**
     * 上传文件操作
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public static HttpResult doPostBinary(String url, Map<String, Object> params) throws Exception {
        return doPostBinary(url, null, params);
    }

    /**
     * 上传文件操作
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws Exception
     */
    public static HttpResult doPostBinary(String url, Map<String, String> headers, Map<String, Object> params) throws Exception {
        // 创建httpClient对象
        CloseableHttpClient httpClient = HttpClientUtil.createHttpClient(MAX_TOTAL_CONNECT, MAX_CONNECT_PER_ROUTE, RETRY_TIMES);
        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.setHeader("Connection", "Keep-Alive");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        packageHeader(headers, httpPost);
        // 封装请求参数
        if (!Objects.isNull(params)) {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setCharset(Charset.forName(HttpClientUtil.ENCODING));
            for (Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) entry.getValue();
                    entityBuilder.addPart(entry.getKey(), new ByteArrayBody(file.getBytes(), file.getOriginalFilename()));
                } else if (entry.getValue() instanceof File) {
                    entityBuilder.addPart(entry.getKey(), new FileBody((File) entry.getValue()));
                } else {
                    entityBuilder.addTextBody(entry.getKey(), disposeNull(entry.getValue()), ContentType.create(MediaType.TEXT_PLAIN_VALUE, ENCODING));
                }
            }
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // 设置到请求的http对象中
            httpPost.setEntity(entityBuilder.build());
        }
        // 执行请求并获得响应结果
        return resolveHttpClientResult(httpClient, httpPost);
    }

    /**
     * 发送post请求；带请求头和请求参数（智法使用）
     * @param url     请求地址
     * @param headers 请求头集合
     * @param params  请求参数集合
     * @return
     * @throws Exception
     */
    public static HttpResult doPostRepair(String url, Map<String, String> headers, String format, Map<String, Object> params,String token) throws Exception {
        // 创建httpClient对象
        CloseableHttpClient httpClient = HttpClientUtil.createHttpClient(MAX_TOTAL_CONNECT, MAX_CONNECT_PER_ROUTE, RETRY_TIMES);
        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.setHeader("Cookie", StringUtils.EMPTY);
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("credential", token);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
        packageHeader(headers, httpPost);
        // 封装请求参数
        packageParam(params, format, httpPost);
        // 执行请求并获得响应结果
        return resolveHttpClientResult(httpClient, httpPost);
    }

    /**
     * 发送put请求；不带请求参数(默认FORM格式)
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpResult doPut(String url) throws Exception {
        return doPut(url, null, null);
    }

    /**
     * 发送put请求；带请求参数
     * @param url    请求地址
     * @param format 参数格式
     * @param params 参数集合
     * @return
     * @throws Exception
     */
    public static HttpResult doPut(String url, String format, Map<String, Object> params) throws Exception {
        CloseableHttpClient httpClient = HttpClientUtil.createHttpClient(MAX_TOTAL_CONNECT, MAX_CONNECT_PER_ROUTE, RETRY_TIMES);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(requestConfig);
        packageParam(params, format, httpPut);
        return resolveHttpClientResult(httpClient, httpPut);
    }

    /**
     * 发送delete请求；不带请求参数
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpResult doDelete(String url) throws Exception {
        CloseableHttpClient httpClient = HttpClientUtil.createHttpClient(MAX_TOTAL_CONNECT, MAX_CONNECT_PER_ROUTE, RETRY_TIMES);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(requestConfig);
        return resolveHttpClientResult(httpClient, httpDelete);
    }

    /**
     * 发送delete请求；带请求参数
     * @param url    请求地址
     * @param format 参数格式
     * @param params 参数集合
     * @return
     * @throws Exception
     */
    public static HttpResult doDelete(String url, String format, Map<String, Object> params) throws Exception {
        if (Objects.isNull(params)) {
            params = Maps.newHashMap();
        }
        params.put("_method", "delete");
        return doPost(url, format, params);
    }

    /**
     * 创建HttpClient连接
     * @param maxTotal
     * @param maxPerRoute
     * @param retryCount
     * @return
     */
    public static CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute, int retryCount) throws Exception {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        try {
            // 设置信任ssl访问
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, auth) -> true).build();
            httpClientBuilder.setSSLContext(sslContext);
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                // 注册https和http请求
                .register("https", new SSLConnectionSocketFactory(sslContext, hostnameVerifier))
                .register("http", PlainConnectionSocketFactory.getSocketFactory()).build();
            // 使用Httpclient连接池配置
            PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
            // 最大连接数
            manager.setMaxTotal(maxTotal);
            // 同路由并发数
            manager.setDefaultMaxPerRoute(maxPerRoute);
            // 配置连接池
            httpClientBuilder.setConnectionManager(manager);
            // 设置默认请求头
            httpClientBuilder.setDefaultHeaders(customHeaders());
            // 设置长连接保持策略
            httpClientBuilder.setKeepAliveStrategy(connectionKeepAliveStrategy());
            // 设置请求重试处理
            httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, Boolean.TRUE));

            return httpClientBuilder.build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
            throw new Exception("初始化HTTP连接池出错", ex);
        }
    }

    /**
     * 封装请求头
     * @param params
     * @param httpMethod
     */
    public static void packageHeader(Map<String, String> params, HttpRequestBase httpMethod) {
        if (!Objects.isNull(params)) {
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                // 设置到请求头到HttpRequestBase对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 封装请求参数
     * @param params     参数
     * @param format     数据格式:JSON、FORM
     * @param httpMethod 请求方法
     * @throws UnsupportedEncodingException
     */
    public static void packageParam(Map<String, Object> params, String format, HttpEntityEnclosingRequestBase httpMethod) throws UnsupportedEncodingException {
        // 封装请求参数
        if (!Objects.isNull(params)) {
            if (FORM.equals(format)) {
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                Set<Entry<String, Object>> entrySet = params.entrySet();
                for (Entry<String, Object> entry : entrySet) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), disposeNull(entry.getValue())));
                }
                // 设置到请求的http对象中
                httpMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, ENCODING));
            } else {
                String object = JSONObject.toJSONString(params, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
                httpMethod.setHeader("Content-Type", "application/json;charset=UTF-8");
                // 设置到请求的http对象中
                httpMethod.setEntity(new StringEntity(object, ENCODING));
            }
        }
    }

    /**
     * 获得响应结果
     * @param httpClient
     * @param httpMethod
     * @return
     * @throws Exception
     */
    public static HttpResult resolveHttpClientResult(CloseableHttpClient httpClient, HttpRequestBase httpMethod) throws Exception {
        CloseableHttpResponse httpResponse = httpClient.execute(httpMethod);
        try {
            // 获取返回结果
            String content = StringUtils.EMPTY;
            if (httpResponse != null && httpResponse.getStatusLine() != null) {
                if (!Objects.isNull(httpResponse.getEntity())) {
                    content = EntityUtils.toString(httpResponse.getEntity(), ENCODING);
                }
                return new HttpResult(httpResponse.getStatusLine().getStatusCode(), content);
            }
            return new HttpResult(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } finally {
            release(httpResponse, httpClient);
        }
    }

    /**
     * 释放资源
     * @param httpResponse
     * @param httpClient
     * @throws IOException
     */
    public static void release(CloseableHttpResponse httpResponse, CloseableHttpClient httpClient) throws IOException {
        if (!Objects.isNull(httpResponse)) {
            httpResponse.close();
        }
        if (!Objects.isNull(httpClient)) {
            httpClient.close();
        }
    }

    /**
     * 配置长连接保持策略
     * @return
     */
    public static ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            HeaderIterator headerIterator = response.headerIterator(HTTP.CONN_KEEP_ALIVE);
            HeaderElementIterator iterator = new BasicHeaderElementIterator(headerIterator);
            while (iterator.hasNext()) {
                HeaderElement element = iterator.nextElement();
                if (element.getValue() != null && "timeout".equalsIgnoreCase(element.getName())) {
                    try {
                        return Long.parseLong(element.getValue()) * 1000;
                    } catch (NumberFormatException ignore) {
                        LOGGER.error("解析长连接过期时间异常", ignore);
                    }
                }
            }
            HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
            // 如果请求目标地址,单独配置了长连接保持时间,使用该配置
            Optional<Entry<String, Integer>> any = Optional.ofNullable(KEEP_ALIVE_TARGET_HOST).orElseGet(HashMap::new)
                .entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(target.getHostName())).findAny();
            // 否则使用默认长连接保持时间
            return any.map(en -> en.getValue() * 1000L).orElse(KEEP_ALIVE_TIME * 1000L);
        };
    }

    /**
     * 处理 null 参数
     * @param object
     * @return
     */
    public static String disposeNull(Object object) {
        if (!Objects.isNull(object)) {
            return String.valueOf(object);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 设置请求头
     * @return
     */
    public static List<Header> customHeaders() {
        BasicHeader agent = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        BasicHeader encoding = new BasicHeader("Accept-Encoding", "gzip, deflate, br");
        BasicHeader language = new BasicHeader("Accept-Language", "zh-CN,zh;q=0.9");
        BasicHeader connection = new BasicHeader("Connection", "Keep-Alive");
        return Arrays.asList(agent, encoding, language, connection);
    }
}
