package com.example.buyonline.controller;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.example.buyonline.util.MKOResponse;
import com.example.buyonline.util.MKOResponseCode;
import com.example.buyonline.util.MKOResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kevin on 2/18/17.
 */
public class BaseController {

    private final static String SPECIAL_Token = "DGSC_Token_";
    private final static String SPECIAL_UserToken = "DGSC_UserToken_";

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    public MKOResponse makeResponse(MKOResponseCode response, Object data) {
        MKOResponse result = new MKOResponse();
        result.put("code", response.getCode());
        result.put("response", data);
        result.put("msg", response.getDesc());
        return result;
    }

    public MKOResponse makeResponse(MKOResponseCode response, Object data, String desc) {
        MKOResponse result = new MKOResponse();
        result.put("code", response.getCode());
        result.put("response", data);
        if (desc != null) {
            result.put("msg", desc);
        }
        return result;
    }

    public MKOResponse makeSuccessResponse(Object data) {
        return this.makeResponse(MKOResponseCode.Success, data);
    }

    public MKOResponse makeResponseByMKOResult(MKOResult mkoResult) {
        MKOResponse result = new MKOResponse();
        if (mkoResult.success)
            result.put("code", MKOResponseCode.Success.getCode());
        else
            result.put("code", MKOResponseCode.BusinessError);
        if (mkoResult.message != null)
            result.put("msg", mkoResult.message);
        return result;
    }

    public MKOResponse makeBussessErrorResponse(String desc) {
        return this.makeResponse(MKOResponseCode.BusinessError, "", desc);
    }

    public MKOResponse makeParamsLackResponse(String desc) {
        return this.makeResponse(MKOResponseCode.ParamsLack, "", desc);
    }

    public String MD5String(String str) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        md5.update(StandardCharsets.UTF_8.encode(str));
        return String.format("%032x", new BigInteger(1, md5.digest())).toLowerCase();
    }
    /**
     * @param list
     * @param page        当前页数
     * @param pageCount   总页数
     * @param count       当前页数数量
     * @param countNumber 总数量
     * @return
     */
    public Object listToString(List list, int page, int pageCount, int count, int countNumber) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 当前页数
        map.put("page", page);
        // 总页数
        map.put("pageCount", (list.size() / count) + 1);
        // 当前页数数量
        map.put("count", count);
        // 总数量
        map.put("countNumber", countNumber);
        // 数据
        map.put("datas", list);
        return map;
    }

    public Object convertPageResult(Page data) {

        Map<String, Object> map = new HashMap<String, Object>();
        // 当前页数
        map.put("page", data.getNumber() + 1);
        // 总页数
        map.put("pageCount", data.getTotalPages());
        // 当前页数数量
        map.put("count", data.getSize());
        // 总数量
        map.put("countNumber", data.getTotalElements());
        // 数据
        map.put("datas", data.getContent());

        return map;
    }

    private String formatTokenKey(String token) {
        return String.format("%s%s", SPECIAL_Token, token);
    }

//    private String formatUserTokenKey(String userName, String platform) {
//        String md5 = MD5Util.getMd5(userName);
//        return String.format("%s%s_%s", SPECIAL_UserToken, platform, md5);
//    }

    public JSONObject getUserInfo(HttpServletRequest request) {
        JSONObject r = new JSONObject();
//		r.put("groupId", "QYWX000010");
//		r.put("userName", "Kevin");
//		return r;
        String token = request.getHeader("MKOTEAM-ACCESS-TOKEN");
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String user = stringRedisTemplate.opsForValue().get(formatTokenKey(token));
        return JSONObject.parseObject(user);
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object missingParamterHandler(MissingServletRequestParameterException exception) {
        return this.makeParamsLackResponse(String.format("缺少[%s]参数", exception.getParameterName()));
    }

}
