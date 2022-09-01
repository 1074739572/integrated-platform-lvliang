package com.iflytek.integrated.platform.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DebugDto {
    private String authFlag;
    List<String> ids;
    List<String> createdTimeList;
}
