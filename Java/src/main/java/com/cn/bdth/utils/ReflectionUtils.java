package com.cn.bdth.utils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;

public class ReflectionUtils {
    public static <T> MultiValueMap<String, Object> toMultipartData(T obj) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value==null) continue;
                if (value instanceof MultipartFile) {
                    // 如果字段是 MultipartFile 类型，将其转换为字节数组并添加到请求体中
                    MultipartFile file = (MultipartFile) value;
                    byte[] fileBytes = file.getBytes();
                    ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };
                    map.add(field.getName(), resource);
                } else {
                    // 如果字段不是 MultipartFile 类型，直接添加到请求体中
                    map.add(field.getName(), value);
                }
            } catch (IllegalAccessException | IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
    public static <T> JSONObject toJSONObject(T obj) throws IllegalAccessException {

        // 将Java实例转换为JSON对象
        JSONObject jsonObject = new JSONObject();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            if (value != null) {
                jsonObject.put(field.getName(), value);
            }
        }
        return jsonObject;
    }

}

