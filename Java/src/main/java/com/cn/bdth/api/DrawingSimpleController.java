package com.cn.bdth.api;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.annotations.note.UserLastOperationTime;
import com.cn.bdth.common.DrawingSimpleCommon;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.dto.DrawingSimpleDto;
import com.cn.bdth.dto.CCoUISamDto;
import com.cn.bdth.entity.DrawingSimple;
import com.cn.bdth.entity.User;
import com.cn.bdth.exceptions.DrawingException;
import com.cn.bdth.exceptions.FrequencyException;
import com.cn.bdth.exceptions.ViolationsException;

import com.cn.bdth.mapper.DrawingSimpleMapper;
import com.cn.bdth.mapper.UserMapper;
import com.cn.bdth.msg.Result;
import com.cn.bdth.utils.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Function;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/drawing/simple")
@RequiredArgsConstructor
public class DrawingSimpleController {
    private final DrawingSimpleMapper drawingSimpleMapper;
    private final UserMapper userMapper;
    private final ChatUtils chatUtils;
    private final WeChatUtils weChatUtils;
    private final DrawingSimpleCommon drawingSimpleCommon;
    private final WebClient.Builder webClientBuilder;
//    绘图API所用的组件大致相同，添加所有组件，只渲染API所需的组件即可。
//    组件提供的输入信息量是足够的，但不同API的输入类型不一定相同，所以需要解析交互变量
//    API信息1(透明)
//        API名
//        所需组件
//        交互变量映射
//        交互变量解析
//        请求体类型
//        响应解析
//        是否异步
//    API信息2
//        API请求头(半透明，由"API名"决定)
//        API任务请求URL(半透明，由"API名"决定)
//        API结果请求URL(半透明，由"API名"决定)
//        API扣费函数(半透明，由"API名"决定)
//        API补偿函数(半透明，由"API名"决定)
//        API任务ID参数名(被"响应解析"蕴含，需重新定义)
//    添加服务器配置(强耦合(至少7处):config,common,dto,vo,impl,impl,使用处)

    //(API信息2)API任务ID参数名
    String[] taskid_arr = new String[]{"taskid", "taskId", "id","data"};
    public Object GetTaskId(JSONObject json)
    {
        Object taskid = null;
        for(int i = 0;i < taskid_arr.length;i++)
        {
            taskid = json.get(taskid_arr[i]);
            if (taskid != null)
                break;
        }
        return String.valueOf(taskid);
    }
    public String sendSamTask(final CCoUISamDto dto) {
        //扣费
        chatUtils.deplete(drawingSimpleCommon.getDrawingSimpleStructure().getCcoSamFrequency(), UserUtils.getCurrentLoginId());
//        //获取登录人ID
//        final Long currentLoginId = UserUtils.getCurrentLoginId();
        String result = webClientBuilder.baseUrl(drawingSimpleCommon.getDrawingSimpleStructure().getCcoUrl())
                .build()
                .post()
                .uri("/esgpt/SAM_single")
                .body(BodyInserters.fromMultipartData(ReflectionUtils.toMultipartData(dto)))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(t->{})
                .onErrorResume(t->Mono.just("")) // 忽略错误
                .block();
        if (result.isEmpty())
        {
            //失败补偿
            chatUtils.compensate(drawingSimpleCommon.getDrawingSimpleStructure().getCcoSamFrequency(), UserUtils.getCurrentLoginId());
            throw new DrawingException();
        }
        else
        {
            final JSONObject jsonObject = JSONObject.parseObject(result);
            return jsonObject.getString("data");
        }
    }
    public JSONObject sendDrawTask(final String url,final String headers,final String mediaType,final DrawingSimpleDto dto,final Boolean debug,final Long Frequency) throws IllegalAccessException {
        //扣费
        chatUtils.deplete(Frequency, UserUtils.getCurrentLoginId());
//        //获取登录人ID
//        final Long currentLoginId = UserUtils.getCurrentLoginId();
        if (dto.getEnv() != null)
        {
            if (dto.getEnv() != 0) {
                // 微信文字识别能力 防止用户发送色情 政治信息
                weChatUtils.filterText(dto.getPositive(), UserUtils.getCurrentOpenId());
            }
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers!=null)
        {
            String[] headers_arr = headers.split(",");
            for (String header : headers_arr) {
                String[] parts = header.split("==");
                httpHeaders.set(parts[0], parts[1]);
            }
        }

        if (debug) {
            if (mediaType.equals("JSON")) {
                System.out.println("JSON请求体: " + ReflectionUtils.toJSONObject(dto));
            } else {
                System.out.println("FORM请求体: " + ReflectionUtils.toMultipartData(dto));
            }
        }
        String result = webClientBuilder.baseUrl(url)
            .defaultHeaders(Headers -> Headers.clear())
            .defaultHeaders(Headers -> Headers.addAll(httpHeaders))
            .build()
            .post()
                .contentType(mediaType.equals("JSON")?MediaType.APPLICATION_JSON:MediaType.MULTIPART_FORM_DATA)
                .body(mediaType.equals("JSON")?BodyInserters.fromValue(ReflectionUtils.toJSONObject(dto).toString()):BodyInserters.fromMultipartData(ReflectionUtils.toMultipartData(dto)))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(t->{})
                .onErrorResume(Exception.class, ex -> {
                if (debug)
                    System.out.println("An error occurred: " + ex.getMessage());
                return Mono.just("");
                })
                .block();

        if (debug)
            System.out.println("绘画请求响应:" + result);
        if (result.isEmpty())
        {
            //失败补偿
            chatUtils.compensate(Frequency, UserUtils.getCurrentLoginId());
            throw new DrawingException();
        }
        else
        {
            final JSONObject jsonObject = JSONObject.parseObject(result);
            try{
                //插入数据库
                Object taskid = GetTaskId(jsonObject);
                if (debug)
                    System.out.println("任务ID:"+taskid);
                final DrawingSimple drawingSimple = new DrawingSimple()
                        .setTaskid((String) taskid)
                        .setUserid(UserUtils.getCurrentLoginId())
                        .setFrequency(Frequency);
                drawingSimpleMapper.insert(drawingSimple);
            }
            catch (Exception e)
            {
                System.out.println("DrawingSimple:数据库插入失败,"+e);
            }
            return jsonObject;
        }
    }

    public JSONObject getDrawResult(final String url, final String headers,final Function<JSONObject, Integer> compensateFun,final String taskid,final Boolean debug) {

        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers!=null)
        {
            String[] headers_arr = headers.split(",");

            for (String header : headers_arr) {
                String[] parts = header.split("==");
                httpHeaders.set(parts[0], parts[1]);
            }
        }
        byte[] result_byte = webClientBuilder.baseUrl(url)
                .defaultHeaders(Headers -> Headers.clear())
                .defaultHeaders(Headers -> Headers.addAll(httpHeaders))
                .build()
                .get()
                .retrieve()
                .bodyToMono(byte[].class)
                .doOnError(t->{})
                .onErrorResume(Exception.class, ex -> {
                    if (debug)
                        System.out.println("An error occurred: " + ex.getMessage());
                    return Mono.empty();
                })
                .block();

        JSONObject json_response;
        if (result_byte==null)//#网络错误(可能图片大小超出内存)
        {
            json_response = new JSONObject();
        }
        else
        {
            String result = new String(result_byte, StandardCharsets.UTF_8);
            json_response = JSONObject.parseObject(result);
        }
        if (debug)
            System.out.println("结果请求响应:" + json_response);

        try {
            if (compensateFun.apply(json_response) == 2)//补偿
            {
                DrawingSimple drawingSimple = drawingSimpleMapper.selectOne(new QueryWrapper<DrawingSimple>().eq("taskid", taskid));
                chatUtils.compensate(drawingSimple.getFrequency(), UserUtils.getCurrentLoginId());
            }
        }catch(Exception e) {
            System.out.println("DrawingSimple:补偿失败,"+e);
        }


        return json_response;
    }


    public boolean isCCoUIServerStateAndFrequency() {
        final DrawingSimpleCommon.DrawingSimpleStructure DrawingSimpleStructure = drawingSimpleCommon.getDrawingSimpleStructure();

        if (!(userMapper.selectCount(new QueryWrapper<User>()
                .lambda().eq(User::getUserId, UserUtils.getCurrentLoginId())
                .ge(User::getFrequency, DrawingSimpleStructure.getCcoImgFrequency())
        ) >= 1)) {
            throw new FrequencyException();
        }
        return NetUtils.checkUrlConnectivity(DrawingSimpleStructure.getCcoUrl()+"/esgpt/connectivity");
    }


    /**
     * 添加分割
     * Add task.
     *
     * @param dto the dto
     * @return the result
     */
    @PostMapping(value = "/sam", name = "(ccoui)分割模型", consumes = "multipart/form-data")
    @UserLastOperationTime
    public Result SendSamTask(@Valid CCoUISamDto dto) {
        try {
            return Result.data(sendSamTask(dto));
        } catch (FrequencyException | DrawingException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加绘图任务
     * Add task.
     *
     * @param dto the dto
     * @return the result
     */
    @PostMapping(value = "/drawing", name = "异步绘画API", produces = MediaType.APPLICATION_JSON_VALUE)
    @UserLastOperationTime
    public Result SendDrawTask(@Valid final DrawingSimpleDto dto) {
        try {
            String urlType = dto.getUrlType();
            dto.setUrlType(null);
            String mediaType = dto.getMediaType();
            dto.setMediaType(null);

            DrawingSimpleCommon.DrawingSimpleStructure drawingsimple_config = drawingSimpleCommon.getDrawingSimpleStructure();
            Boolean debug = drawingsimple_config.getDebug() != null && drawingsimple_config.getDebug() == 1;

            //(API信息2)API任务请求URL
            HashMap<String, String> APIInfo2_UrlDict = new HashMap<>();
            APIInfo2_UrlDict.put("easysd", drawingsimple_config.getCcoUrl() + "/esgpt/exeflow");
            APIInfo2_UrlDict.put("xbt_esgpt", APIInfo2_UrlDict.get("easysd"));
            APIInfo2_UrlDict.put("gqfd_esgpt", APIInfo2_UrlDict.get("easysd"));
            APIInfo2_UrlDict.put("xgss", APIInfo2_UrlDict.get("easysd"));
            APIInfo2_UrlDict.put("ksch", APIInfo2_UrlDict.get("easysd"));
            APIInfo2_UrlDict.put("outPaintGenerate", drawingsimple_config.getTuKeLi_Url() + "/outPaintGenerate");
            APIInfo2_UrlDict.put("promeaigenerate", drawingsimple_config.getTuKeLi_Url() + "/promeai/generate");
            APIInfo2_UrlDict.put("supermodelGenerate", drawingsimple_config.getTuKeLi_Url() + "/supermodelGenerate");

            //(API信息2)API请求头
            HashMap<String, String> APIInfo2_HeadersDict = new HashMap<>();
            APIInfo2_HeadersDict.put("easysd", null);
            APIInfo2_HeadersDict.put("xbt_esgpt", null);
            APIInfo2_HeadersDict.put("gqfd_esgpt", null);
            APIInfo2_HeadersDict.put("xgss", null);
            APIInfo2_HeadersDict.put("ksch", null);
            APIInfo2_HeadersDict.put("outPaintGenerate", "APIKEY=="+drawingsimple_config.getTuKeLi_Key());
            APIInfo2_HeadersDict.put("promeaigenerate", APIInfo2_HeadersDict.get("outPaintGenerate"));
            APIInfo2_HeadersDict.put("supermodelGenerate", APIInfo2_HeadersDict.get("outPaintGenerate"));


            //(API信息2)API扣费函数(提前扣费,预防白嫖)
            HashMap<String, Function<DrawingSimpleDto, Long>> APIInfo2_Deplete  = new HashMap<>();
            APIInfo2_Deplete.put("easysd", (DTO)-> drawingsimple_config.getCcoImgFrequency());
            APIInfo2_Deplete.put("xbt_esgpt", (DTO)-> (DTO.getPrompt()==null?0:drawingsimple_config.getCcoGPTFrequency())+drawingsimple_config.getCcoImgFrequency());
            APIInfo2_Deplete.put("gqfd_esgpt", (DTO)-> drawingsimple_config.getCcoImgFrequency());
            APIInfo2_Deplete.put("xgss", (DTO)-> 0L);
            APIInfo2_Deplete.put("ksch", (DTO)-> 0L);
            APIInfo2_Deplete.put("outPaintGenerate", (DTO)-> drawingsimple_config.getTkloutPaintGenerateFrequency());
            APIInfo2_Deplete.put("promeaigenerate", (DTO)-> drawingsimple_config.getTklpromeaigenerateFrequency());
            APIInfo2_Deplete.put("supermodelGenerate", (DTO)-> drawingsimple_config.getTklsupermodelGenerateFrequency());

            if (APIInfo2_UrlDict.get(urlType) == null)
            {
                throw new DrawingException();
            }
            if (debug)
            {
                System.out.println("绘画请求url:" + APIInfo2_UrlDict.get(urlType));
                System.out.println("Header:" + APIInfo2_HeadersDict.get(urlType));
                System.out.println("mediaType:" + mediaType);
            }
            return Result.data(sendDrawTask(APIInfo2_UrlDict.get(urlType),APIInfo2_HeadersDict.get(urlType),mediaType,dto,debug,APIInfo2_Deplete .get(urlType).apply(dto)));
        } catch (FrequencyException | ViolationsException | DrawingException | IllegalAccessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查并获取文件
     *
     * @return the result
     */
    @GetMapping(value = "/getimg", name = "获取图片", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result GetDrawResult(@RequestParam("urltype") String urlType,@RequestParam(value="p1", required=false) String p1,@RequestParam(value="p2", required=false) String p2,@RequestParam(value="p3", required=false) String p3) {
        String url = "?";
        JSONObject param = new JSONObject();
        if (p1!=null)
        {
            String[] parts = p1.split("=");
            url = url + parts[0] + "=" + parts[1] + "&";
            param.put(parts[0],parts[1]);
        }
        if (p2!=null)
        {
            String[] parts = p2.split("=");
            url = url + parts[0] + "=" + parts[1] + "&";
            param.put(parts[0],parts[1]);
        }
        if (p3!=null)
        {
            String[] parts = p3.split("=");
            url = url + parts[0] + "=" + parts[1] + "&";
            param.put(parts[0],parts[1]);
        }


        try {
            DrawingSimpleCommon.DrawingSimpleStructure drawingsimple_config = drawingSimpleCommon.getDrawingSimpleStructure();
            Boolean debug = drawingsimple_config.getDebug() != null && drawingsimple_config.getDebug() == 1;

//        API补偿函数(半透明，由"API名"决定)
//        API任务ID(被"响应解析"蕴含，需重新定义)

            //(API信息2)API结果请求URL
            HashMap<String, String> APIInfo2_ResultUrlDict = new HashMap<>();
            APIInfo2_ResultUrlDict.put("easysd", drawingsimple_config.getCcoUrl()+"/esgpt/getimg");
            APIInfo2_ResultUrlDict.put("xbt_esgpt", APIInfo2_ResultUrlDict.get("easysd"));
            APIInfo2_ResultUrlDict.put("gqfd_esgpt", APIInfo2_ResultUrlDict.get("easysd"));
            APIInfo2_ResultUrlDict.put("xgss", APIInfo2_ResultUrlDict.get("easysd"));
            APIInfo2_ResultUrlDict.put("ksch", APIInfo2_ResultUrlDict.get("easysd"));
            APIInfo2_ResultUrlDict.put("outPaintGenerate", drawingsimple_config.getTuKeLi_Url()+"/outPaintResult");
            APIInfo2_ResultUrlDict.put("promeaigenerate", drawingsimple_config.getTuKeLi_Url()+"/promeai/getResult");
            APIInfo2_ResultUrlDict.put("supermodelGenerate", drawingsimple_config.getTuKeLi_Url()+"/supermodelResult");

            //(API信息2)API请求头
            HashMap<String, String> APIInfo2_HeadersDict = new HashMap<>();
            APIInfo2_HeadersDict.put("easysd", null);
            APIInfo2_HeadersDict.put("xbt_esgpt", null);
            APIInfo2_HeadersDict.put("gqfd_esgpt", null);
            APIInfo2_HeadersDict.put("xgss", null);
            APIInfo2_HeadersDict.put("ksch", null);
            APIInfo2_HeadersDict.put("outPaintGenerate", "APIKEY=="+drawingsimple_config.getTuKeLi_Key());
            APIInfo2_HeadersDict.put("promeaigenerate", APIInfo2_HeadersDict.get("outPaintGenerate"));
            APIInfo2_HeadersDict.put("supermodelGenerate", APIInfo2_HeadersDict.get("outPaintGenerate"));

            //(API信息2)API补偿函数
            HashMap<String, Function<JSONObject, Integer>> APIInfo2_Compensate  = new HashMap<>();
            APIInfo2_Compensate.put("easysd", (json)-> json.getInteger("code"));
            APIInfo2_Compensate.put("xbt_esgpt", APIInfo2_Compensate.get("easysd"));
            APIInfo2_Compensate.put("gqfd_esgpt", APIInfo2_Compensate.get("easysd"));
            APIInfo2_Compensate.put("xgss", (json)-> 0);
            APIInfo2_Compensate.put("ksch", (json)-> 0);
            APIInfo2_Compensate.put("outPaintGenerate", (json)-> 1);
            APIInfo2_Compensate.put("promeaigenerate", (json)-> 1);
            APIInfo2_Compensate.put("supermodelGenerate", (json)-> 1);

            Object taskid = GetTaskId(param);
            if (debug)
                System.out.println("任务ID:"+taskid);

            if (debug)
            {
                System.out.println("结果查询url:" + APIInfo2_ResultUrlDict.get(urlType) + url.substring(0, url.length() - 1));
                System.out.println("Header:" + APIInfo2_HeadersDict.get(urlType));
            }
            return Result.data(getDrawResult(APIInfo2_ResultUrlDict.get(urlType) + url.substring(0, url.length() - 1),APIInfo2_HeadersDict.get(urlType),APIInfo2_Compensate.get(urlType),(String) taskid,debug));
        } catch (DrawingException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping(value = "/ccouiconnectivity", name = "检查CCoUI网络连通性", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result CCoConnectivity() {
        try {
            return Result.data(isCCoUIServerStateAndFrequency());
        } catch (FrequencyException | ViolationsException e) {
            return Result.error(e.getMessage());
        }
    }
}
