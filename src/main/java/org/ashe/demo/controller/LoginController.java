package org.ashe.demo.controller;

import com.alibaba.fastjson.JSON;
import com.aliyun.dingtalkcontact_1_0.models.GetUserHeaders;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiAttendanceGetleavetimebynamesRequest;
import com.dingtalk.api.request.OapiUserListsimpleRequest;
import com.dingtalk.api.request.OapiV2DepartmentListsubRequest;
import com.dingtalk.api.request.OapiWorkrecordGetbyuseridRequest;
import com.dingtalk.api.response.OapiAttendanceGetleavetimebynamesResponse;
import com.dingtalk.api.response.OapiUserListsimpleResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.dingtalk.api.response.OapiWorkrecordGetbyuseridResponse;
import com.taobao.api.ApiException;
import com.taobao.api.internal.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.ashe.demo.dto.DeptDTO;
import org.ashe.demo.dto.UserDTO;
import org.ashe.demo.dto.VacationDTO;
import org.ashe.demo.infra.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class LoginController {

    @Value("${ding-talk.app-key}")
    private String appKey;
    @Value("${ding-talk.app-secret}")
    private String appSecret;
    private static final String HTTPS = "https";
    private static final String CENTRAL = "central";

    /**
     * ding-talk oauth2.0 client
     */
    public static com.aliyun.dingtalkoauth2_1_0.Client authClient() {
        Config config = new Config();
        config.protocol = HTTPS;
        config.regionId = CENTRAL;
        try {
            return new com.aliyun.dingtalkoauth2_1_0.Client(config);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
    }

    /**
     * 扫码登录
     * <a href="https://open.dingtalk.com/document/orgapp/tutorial-obtaining-user-personal-information#title-ts9-exq-xrh">api document</a>
     */
    // 接口地址：http://127.0.0.1:8080/api/v1/auth/ding与钉钉登录与分享的回调域名地址一致
    @GetMapping(value = "/ding")
    public String scanLogin(@RequestParam(value = "authCode") String authCode) throws Exception {
        com.aliyun.dingtalkoauth2_1_0.Client client = authClient();
        GetUserTokenRequest getUserTokenRequest = new GetUserTokenRequest()

                // 应用基础信息-应用信息的AppKey,请务必替换为开发的应用AppKey
                .setClientId(appKey)

                // 应用基础信息-应用信息的AppSecret，,请务必替换为开发的应用AppSecret
                .setClientSecret(appSecret)
                .setCode(authCode)
                .setGrantType("authorization_code");
        GetUserTokenResponse getUserTokenResponse = client.getUserToken(getUserTokenRequest);
        // 获取用户个人token
        String accessToken = getUserTokenResponse.getBody().getAccessToken();
        log.info("accessToken:{}", accessToken);
        return getUserinfo(accessToken);
    }

    /**
     * 获取企业accessToken
     * <a href="https://open.dingtalk.com/document/orgapp/obtain-the-access_token-of-an-internal-app">api document</a>
     */
    @GetMapping(value = "/accessToken")
    public String accessToken() {
        com.aliyun.dingtalkoauth2_1_0.Client client = authClient();
        com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest getAccessTokenRequest = new com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest()
                .setAppKey(appKey)
                .setAppSecret(appSecret);
        try {
            GetAccessTokenResponse accessTokenResponse = client.getAccessToken(getAccessTokenRequest);
            log.info(accessTokenResponse.getBody().getAccessToken());
            return accessTokenResponse.getBody().getAccessToken();
        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.error("fail", err);
            }
        } catch (Exception ex) {
            TeaException err = new TeaException(ex.getMessage(), ex);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.error("fail", ex);
            }
        }
        return null;
    }

    /**
     * 获取企业部门列表
     * <a href="https://open.dingtalk.com/document/orgapp/obtains-the-list-of-all-departments-of-an-enterprise">api document</a>
     */
    @GetMapping("/department")
    public String department(@RequestBody DeptDTO dto) {
        // 企业accessToken
        String accessToken = accessToken();
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/listsub");
        OapiV2DepartmentListsubRequest req = new OapiV2DepartmentListsubRequest();
        req.setLanguage("zh_CN");
        if (dto.getDeptId() == null) {
            // 获取企业通讯录根部门下的一级子部门信息
            req.setDeptId(1L);
        } else {
            req.setDeptId(dto.getDeptId());
        }
        try {
            OapiV2DepartmentListsubResponse rsp = client.execute(req, accessToken);
            return JSON.toJSONString(rsp.getResult());
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
    }

    /**
     * 获取部门用户基础信息
     * <a href="https://open.dingtalk.com/document/orgapp/queries-the-simple-information-of-a-department-user">api document</a>
     */
    @GetMapping("/department/user")
    public String departmentUser(@RequestBody DeptDTO dto){
        // 企业accessToken
        String accessToken = accessToken();
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listsimple");
        OapiUserListsimpleRequest req = new OapiUserListsimpleRequest();
        req.setDeptId(dto.getDeptId());
        req.setCursor(0L);
        req.setSize(10L);
        req.setOrderField("modify_desc");
        req.setContainAccessLimit(false);
        req.setLanguage("zh_CN");
        OapiUserListsimpleResponse rsp;
        try {
            rsp = client.execute(req, accessToken);
        } catch (ApiException e) {
            throw new ServiceException(e);
        }
        return JSON.toJSONString(rsp.getResult());
    }

    /**
     * 获取报表假期数据
     * <a href="https://open.dingtalk.com/document/orgapp/obtains-the-holiday-data-from-the-smart-attendance-report">api document</a>
     */
    @GetMapping("/vacation")
    public String vacation(@RequestBody VacationDTO dto){
        // 参数校验 & 初始化
        dto.init();
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/getleavetimebynames");
        OapiAttendanceGetleavetimebynamesRequest req = new OapiAttendanceGetleavetimebynamesRequest();
        req.setUserid(dto.getUserId());
        req.setLeaveNames(dto.getLeaveNames());
        req.setFromDate(StringUtils.parseDateTime(dto.getFrom()));
        req.setToDate(StringUtils.parseDateTime(dto.getTo()));
        OapiAttendanceGetleavetimebynamesResponse rsp;
        try {
            rsp = client.execute(req, accessToken());
        } catch (ApiException e) {
            throw new ServiceException(e);
        }
        OapiAttendanceGetleavetimebynamesResponse.ColumnValListForTopVo result = rsp.getResult();
        List<OapiAttendanceGetleavetimebynamesResponse.ColumnDayAndVal> list = result.getColumns().get(0).getColumnvals();
        log.info("columnVo:{}", JSON.toJSONString(result.getColumns().get(0).getColumnvo()));
        // 累加请假时长不为零的数据
        BigDecimal vacationHour = list.stream()
                .filter(item -> !"0.0".equals(item.getValue()))
                .map(item -> new BigDecimal(item.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return vacationHour.toString();
    }

    /**
     * 查询企业下用户待办列表
     * <a href="https://open.dingtalk.com/document/orgapp/get-the-user-s-to-do-items">api document</a>
     */
    @GetMapping("/to-do")
    public String todoList(UserDTO dto){
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/workrecord/getbyuserid");
        OapiWorkrecordGetbyuseridRequest req = new OapiWorkrecordGetbyuseridRequest();
        req.setUserid(dto.getUserId());
        req.setOffset(0L);
        req.setLimit(50L);
        req.setStatus(0L);
        OapiWorkrecordGetbyuseridResponse rsp;
        try {
            rsp = client.execute(req, accessToken());
        } catch (ApiException e) {
            throw new ServiceException(e);
        }
        return JSON.toJSONString(rsp.getRecords());
    }

    /**
     * 获取用户个人信息
     */
    public String getUserinfo(String accessToken) throws Exception {
        com.aliyun.dingtalkcontact_1_0.Client client = contactClient();
        GetUserHeaders getUserHeaders = new GetUserHeaders();
        getUserHeaders.xAcsDingtalkAccessToken = accessToken;
        // 获取用户个人信息，如需获取当前授权人的信息，unionId参数必须传me
        return JSON.toJSONString(client.getUserWithOptions("me", getUserHeaders, new RuntimeOptions()).getBody());
    }

    public static com.aliyun.dingtalkcontact_1_0.Client contactClient() throws Exception {
        Config config = new Config();
        config.protocol = HTTPS;
        config.regionId = CENTRAL;
        return new com.aliyun.dingtalkcontact_1_0.Client(config);
    }

}