package com.iflytek.integrated.platform.dto;

import lombok.Data;
import org.json.JSONObject;

import java.util.List;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/2/1 10:22
 */
@Data
public class RedisDto {

    private String ids;

    private List<RedisKeyDto> redisKeyDtoList;

    public RedisDto(){

    }

    public RedisDto(String ids){
        this.ids = ids;
    }

    public RedisDto(List<RedisKeyDto> redisKeyDtoList){
        this.redisKeyDtoList = redisKeyDtoList;
    }

    @Override
    public String toString(){
        return new JSONObject(this).toString();
    }
}
