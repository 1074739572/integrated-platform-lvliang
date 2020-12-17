package com.iflytek.integrated.platform.dto;


import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author
 */
@Data
public class SchemaDto {

    private static String SCHEMA = "avro.schema";
    private static String JSON = "json";

    private String schema;

    private String json;

    public SchemaDto(JSONObject jsonT) {
        schema = jsonT.getString(SCHEMA);
        json = jsonT.getString(JSON);
    }
}
