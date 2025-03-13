package com.example.ordersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommonErrorDto {
    private int status_code;
    private String status_message;
}
