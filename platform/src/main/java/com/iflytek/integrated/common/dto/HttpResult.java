package com.iflytek.integrated.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 封装httpClient响应结果
 * @author JourWon
 * @date Created on 2018年4月19日
 */
public class HttpResult implements Serializable {

    private static final long serialVersionUID = 1978024638813747210L;

    /**
     * 响应状态码
     */
    private int status;

    /**
     * 响应数据
     */
    private String content;

    public HttpResult() {
        super();
    }

    public HttpResult(int status) {
        this.status = status;
    }

    public HttpResult(String content) {
        this.content = content;
    }

    public HttpResult(int status, String content) {
        this.status = status;
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 校验 Result 是否成功
     * @return
     */
    @JsonIgnore
    public boolean isSuccess() {
        return HttpStatus.OK.value() == this.status;
    }
}