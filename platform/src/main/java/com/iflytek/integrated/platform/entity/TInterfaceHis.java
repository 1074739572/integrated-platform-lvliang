package com.iflytek.integrated.platform.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * TInterface is a Querydsl bean type
 */
@Data
public class TInterfaceHis extends TInterface implements Serializable {
    private String originInterfaceId;
}

