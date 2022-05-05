package com.iflytek.integrated.platform.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class HttpClientCallSoapUtil {
	static int socketTimeout = 15000;// 请求超时时间
	static int connectTimeout = 5000;// 传输超时时间

	static void packageHeaders(HttpPost httpPost , Map<String , String> headerMap ) {
		 if (!Objects.isNull(headerMap)) {
	         Set<Entry<String, String>> entrySet = headerMap.entrySet();
	         for (Entry<String, String> entry : entrySet) {
	             // 设置到请求头到HttpRequestBase对象中
	        	 httpPost.setHeader(entry.getKey(), entry.getValue());
	         }
	     }
	}

	/**
	 * 使用SOAP1.1发送消息
	 * 
	 * @param postUrl
	 * @param soapXml
	 * @param soapAction
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws KeyManagementException 
	 */
	public static String doPostSoap1_1(String postUrl, String soapXml, String soapAction , Map<String , String> headerMap, Integer readTimeout) throws NoSuchAlgorithmException, ParseException, IOException, KeyManagementException {
		String retStr = "";
		// 创建HttpClientBuilder
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		if(postUrl.startsWith("https")) {
			SSLContext ctx = SSLContext.getInstance("TLS");
	        X509TrustManager tm = new X509TrustManager() {
	                @Override
	                public void checkClientTrusted(X509Certificate[] chain,
	                        String authType) throws CertificateException {
	                }
	                @Override
	                public void checkServerTrusted(X509Certificate[] chain,
	                        String authType) throws CertificateException {
	                }
	                @Override
	                public X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }
	        };
	        ctx.init(null, new TrustManager[]{tm}, null);
			httpClientBuilder.setSSLContext(ctx);
			httpClientBuilder.setSSLHostnameVerifier((hostName, sslSession) -> {
				   return true; // 证书校验通过
			});
		}
		// HttpClient
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		HttpPost httpPost = new HttpPost(postUrl);
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeout).setAuthenticationEnabled(true)
				.setConnectionRequestTimeout(readTimeout)
				.setConnectTimeout(connectTimeout).build();
		httpPost.setConfig(requestConfig);
		httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
		httpPost.setHeader("SOAPAction", soapAction);
		packageHeaders(httpPost , headerMap);
		StringEntity data = new StringEntity(soapXml, Charset.forName("UTF-8"));
		httpPost.setEntity(data);
		CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
		HttpEntity httpEntity = response.getEntity();
		if (httpEntity != null) {
			// 打印响应内容
			retStr = EntityUtils.toString(httpEntity, "UTF-8");
		}
		// 释放资源
		closeableHttpClient.close();
		return retStr;
	}

	/**
	 * 使用SOAP1.2发送消息
	 * 
	 * @param postUrl
	 * @param soapXml
	 * @param soapAction
	 * @return
	 */
	public static String doPostSoap1_2(String postUrl, String soapXml, String soapAction , Map<String , String> headerMap) {
		String retStr = "";
		// 创建HttpClientBuilder
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		// HttpClient
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		HttpPost httpPost = new HttpPost(postUrl);
		// 设置请求和传输超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout)
				.setConnectTimeout(connectTimeout).build();
		httpPost.setConfig(requestConfig);
		try {
			httpPost.setHeader("Content-Type", "application/soap+xml;charset=UTF-8");
			httpPost.setHeader("SOAPAction", soapAction);
			packageHeaders(httpPost, headerMap);
			StringEntity data = new StringEntity(soapXml, Charset.forName("UTF-8"));
			httpPost.setEntity(data);
			CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				// 打印响应内容
				retStr = EntityUtils.toString(httpEntity, "UTF-8");
			}
			// 释放资源
			closeableHttpClient.close();
		} catch (Exception e) {
		}
		return retStr;
	}
	
}
