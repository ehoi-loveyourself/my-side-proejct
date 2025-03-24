package com.project.ecommerce.common.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseUtil {

    public static Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", data);

        return result;
    }
}