package com.cn.bdth.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.cn.bdth.exceptions.UploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MoonShotUploadUtils {

    private String APIUrl = "https://api.moonshot.cn/v1/files";

    @Value("${config.MoonShot_Key}")
    private String APIKey;
    private final WebClient.Builder webClientBuilder;

    public String uploadFile(final MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();

            assert originalFileName != null;

            MultiValueMap<String, Object> bodyValues = new LinkedMultiValueMap<>();
            bodyValues.add("file", file.getResource());
            bodyValues.add("purpose", "file-extract");

            return webClientBuilder.baseUrl(APIUrl)
                    .build()
                    .post()
                    .header("Authorization","Bearer " + APIKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyValues))
                    .retrieve()
                    .bodyToMono(String.class)
//                    .doOnError(t->log.error("上传文件时出错: {}", t.getMessage()))
//                    .onErrorResume(t-> Mono.just("出现错误: " + t.getMessage()))
                    .doOnError(t->{})
                    .onErrorResume(t-> Mono.just("")) // 忽略错误
                    .block();
        } catch (Exception e) {
            log.error("无法将图片上传到MoonShot。错误消息： {} 错误类： {}", e.getMessage(), e.getClass());
            throw new UploadException();
        }
    }

    public String getcontent(final String file_id) {
        try {

            return webClientBuilder.baseUrl(APIUrl)
                    .build()
                    .get()
                    .uri("/"+file_id+"/content")
                    .header("Authorization","Bearer " + APIKey)
                    .retrieve()
                    .bodyToMono(String.class)
//                    .doOnError(t->log.error("获取文件内容时出错: {}", t.getMessage()))
//                    .onErrorResume(t-> Mono.just("出现错误: " + t.getMessage()))
                    .doOnError(t->{})
                    .onErrorResume(t-> Mono.just("")) // 忽略错误
                    .block();
        } catch (Exception e) {
            log.error("无法从MoonShot获取文件{}的内容。错误消息： {} 错误类： {}", file_id, e.getMessage(), e.getClass());
            throw new UploadException();
        }
    }

    public String deleteFile(final String file_id) {
        try {
            return webClientBuilder.baseUrl(APIUrl)
                .build()
                .delete()
                .uri("/"+file_id)
                .header("Authorization","Bearer " + APIKey)
                .retrieve()
                .bodyToMono(String.class)
//                    .doOnError(t->log.error("删除文件时出错: {}", t.getMessage()))
//                    .onErrorResume(t-> Mono.just("出现错误: " + t.getMessage()))
                .doOnError(t->{})
                .onErrorResume(t-> Mono.just("")) // 忽略错误
                .block();
        } catch (Exception e) {
            log.error("无法从MoonShot删除图片。错误消息： {} 错误类： {}", e.getMessage(), e.getClass());
            throw new UploadException();
        }
    }

//    public String uploadImageFromUrl(String imageUrl, String path, String newFileName) {
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
//        try (InputStream inputStream = new URL(imageUrl).openStream()) {
//            String fileName = newFileName != null ? newFileName : UUID.randomUUID().toString();
//            String filePath = path + "/" + fileName;
//            ObjectMetadata objectMetadata = new ObjectMetadata();
//            objectMetadata.setContentType("image/jpeg"); // 根据实际情况设置图片类型
//            ossClient.putObject(bucketName, filePath, inputStream, objectMetadata);
//            return "/" + filePath;
//        } catch (IOException e) {
//            throw new UploadException();
//        } finally {
//            ossClient.shutdown();
//        }
//    }
}
