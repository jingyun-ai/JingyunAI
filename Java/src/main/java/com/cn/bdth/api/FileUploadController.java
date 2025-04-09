package com.cn.bdth.api;


import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.exceptions.OrdersException;
import com.cn.bdth.exceptions.UploadException;
import com.cn.bdth.msg.Result;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.MoonShotUploadUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileUploadController {

    private final AliUploadUtils aliUploadUtils;
    private final MoonShotUploadUtils moonShotUploadUtils;
    /**
     * 上传图片
     *
     * @param file the file
     * @return the result
     */
    @PostMapping(value = "/upload/image", name = "图片上传OSS", consumes = "multipart/form-data")
    public Result imageUploadOSS(@Valid @NotNull(message = "图片文件不能为空") final MultipartFile file) {
        try {
            final String uri = aliUploadUtils.uploadFile(file, FileEnum.DialogFIle.getDec(), null, true);
            return Result.data(uri);
        } catch (UploadException e) {
            return Result.error(e.getMessage());
        }

    }
    /**
     * 上传文件
     *
     * @param file the file
     * @return the result
     */
    @PostMapping(value = "/upload", name = "文件上传OSS", consumes = "multipart/form-data")
    public Result fileUploadOSS(@Valid @NotNull(message = "文件不能为空") final MultipartFile file) {
        try {
            final String uri = aliUploadUtils.uploadFile(file, FileEnum.DialogFIle.getDec(), null, false);
            return Result.data(uri);
        } catch (UploadException e) {
            return Result.error(e.getMessage());
        }

    }

    @PostMapping(value = "/upload/moonshot", name = "文件上传MoonShot", consumes = "multipart/form-data")
    public Result fileUploadMoonShot(@Valid @NotNull(message = "文件不能为空") final MultipartFile file) {
        try {
            final String result = moonShotUploadUtils.uploadFile(file);
            return Result.data(JSONObject.parseObject(result));
        } catch (UploadException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping(value = "/get/moonshot/{file_id}", name = "文件内容获取MoonShot", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getContentMoonShot(@PathVariable final String file_id) {
        try {
            final String result = moonShotUploadUtils.getcontent(file_id);
            return Result.data(JSONObject.parseObject(result));
        } catch (UploadException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping(value = "/delete/moonshot/{file_id}", name = "删除文件MoonShot", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result fileDeleteMoonShot(@PathVariable final String file_id) {
        try {
            final String result = moonShotUploadUtils.deleteFile(file_id);
            return Result.data(JSONObject.parseObject(result));
        } catch (UploadException e) {
            return Result.error(e.getMessage());
        }
    }

}
