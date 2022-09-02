package com.iflytek.integrated.platform.dto;

import lombok.Data;

import java.util.List;

@Data
public class DebugDto {
    private String authFlag;
    String ids;
    List<String> createdTimeList;
}
