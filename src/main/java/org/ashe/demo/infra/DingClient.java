package org.ashe.demo.infra;

import com.alibaba.fastjson.JSON;
import com.aliyun.dingtalkcontact_1_0.models.GetUserHeaders;
import com.aliyun.dingtalkcontact_1_0.models.GetUserResponse;
import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.taobao.api.ApiException;
import com.taobao.api.internal.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.ashe.demo.controller.OverTimeDTO;
import org.ashe.demo.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 钉钉接口客户端
 * <a href="https://open.dingtalk.com/document/orgapp/learning-map">钉钉接口文档__检索</a>
 * <a href="https://open-dev.dingtalk.com/fe/app#/corp/app">钉钉企业内部应用</a>
 */
@Component
@Slf4j
public class DingClient {

    @Value("${ding-talk.app-key}")
    private String appKey;
    @Value("${ding-talk.app-secret}")
    private String appSecret;
    private static final String HTTPS = "https";
    private static final String CENTRAL = "central";
    private static final String ZH_CH = "zh_CN";

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/tutorial-obtaining-user-personal-information#title-ts9-exq-xrh">api document</a>
     */
    public String scanLogin(String authCode) {
        com.aliyun.dingtalkoauth2_1_0.Client client = authClient();
        GetUserTokenRequest getUserTokenRequest = new GetUserTokenRequest()

                // 应用基础信息-应用信息的AppKey,请务必替换为开发的应用AppKey
                .setClientId(appKey)

                // 应用基础信息-应用信息的AppSecret，,请务必替换为开发的应用AppSecret
                .setClientSecret(appSecret)
                .setCode(authCode)
                .setGrantType("authorization_code");
        GetUserTokenResponse getUserTokenResponse;
        try {
            getUserTokenResponse = client.getUserToken(getUserTokenRequest);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        // 获取用户个人token
        String accessToken = getUserTokenResponse.getBody().getAccessToken();
        log.info("accessToken:{}", accessToken);
        return getUserinfo(accessToken);
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/obtain-the-access_token-of-an-internal-app">api document</a>
     */
    private String accessToken() {
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
     * <a href="https://open.dingtalk.com/document/orgapp/obtains-the-list-of-all-departments-of-an-enterprise">api document</a>
     */
    public String department(DeptDTO dto) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/listsub");
        OapiV2DepartmentListsubRequest req = new OapiV2DepartmentListsubRequest();
        req.setLanguage(ZH_CH);
        if (dto.getDeptId() == null) {
            // 获取企业通讯录根部门下的一级子部门信息
            req.setDeptId(1L);
        } else {
            req.setDeptId(dto.getDeptId());
        }
        try {
            OapiV2DepartmentListsubResponse rsp = client.execute(req, accessToken());
            return JSON.toJSONString(rsp.getResult());
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/queries-the-simple-information-of-a-department-user">api document</a>
     */
    public String departmentUser(DeptDTO dto){
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listsimple");
        OapiUserListsimpleRequest req = new OapiUserListsimpleRequest();
        req.setDeptId(dto.getDeptId());
        req.setCursor(0L);
        req.setSize(10L);
        req.setOrderField("modify_desc");
        req.setContainAccessLimit(false);
        req.setLanguage(ZH_CH);
        OapiUserListsimpleResponse rsp;
        try {
            rsp = client.execute(req, accessToken());
        } catch (ApiException e) {
            throw new ServiceException(e);
        }
        return JSON.toJSONString(rsp.getResult());
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/query-user-details">api document</a>
     */
    public String userInfo(UserDTO dto) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid(dto.getUserId());
        req.setLanguage(ZH_CH);
        OapiV2UserGetResponse rsp;
        try {
            rsp = client.execute(req, accessToken());
        } catch (ApiException e) {
            throw new ServiceException(e);
        }
        return JSON.toJSONString(rsp.getResult());
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/create-event#">api document</a>
     */
    public void createEvent(EventDTO dto) {
        com.aliyun.dingtalkcalendar_1_0.Client client = calendarClient();
        com.aliyun.dingtalkcalendar_1_0.models.CreateEventHeaders createEventHeaders = new com.aliyun.dingtalkcalendar_1_0.models.CreateEventHeaders();
        createEventHeaders.xAcsDingtalkAccessToken = accessToken();
        // 日程开始时间
        com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest.CreateEventRequestStart start = new com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest.CreateEventRequestStart()
                .setDateTime(dto.getStartTime())
                .setTimeZone("Asia/Shanghai");
        // 日程结束时间
        com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest.CreateEventRequestEnd end = new com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest.CreateEventRequestEnd()
                .setDateTime(dto.getEndTime())
                .setTimeZone("Asia/Shanghai");
        // 日程大于一天
        if (!dto.getStartDate().equals(dto.getEndDate())) {
            start.setDate(dto.getStartDate());
            end.setDate(dto.getEndDate());
        }
        com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest createEventRequest = new com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest()
                .setSummary(dto.getSummary())
                .setStart(start)
                .setDescription(dto.getDescription())
                .setEnd(end);
        try {
            client.createEventWithOptions(dto.getUnionId(), "primary", createEventRequest, createEventHeaders, new com.aliyun.teautil.models.RuntimeOptions());
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
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/add-dingtalk-to-do-task#">api document</a>
     */
    public void createTodoTask(TodoTaskDTO dto){
        com.aliyun.dingtalktodo_1_0.Client client = todoClient();
        com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskHeaders createTodoTaskHeaders = new com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskHeaders();
        createTodoTaskHeaders.xAcsDingtalkAccessToken = accessToken();
        com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskRequest.CreateTodoTaskRequestNotifyConfigs notifyConfigs = new com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskRequest.CreateTodoTaskRequestNotifyConfigs()
                // 仅支持取值为1，表示应用内DING
                .setDingNotify("1");
        com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskRequest createTodoTaskRequest = new com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskRequest()
                .setIsOnlyShowExecutor(dto.getIsOnlyShowExecutor())
                .setSubject(dto.getSubject())
                .setNotifyConfigs(notifyConfigs)
                // 截止时间，Unix时间戳，单位毫秒
                .setDueTime(DateUtil.getTimestampByOffSet(dto.getDueTime()))
                .setCreatorId(dto.getCreatorId())
                .setDescription(dto.getDescription())
                .setPriority(dto.getPriority())
                .setExecutorIds(
                        Arrays.asList(
                                dto.getExecutorIds().split(",")
                        )
                );
        try {
            client.createTodoTaskWithOptions(dto.getUnionId(), createTodoTaskRequest, createTodoTaskHeaders, new com.aliyun.teautil.models.RuntimeOptions());
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
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/obtains-the-holiday-data-from-the-smart-attendance-report">api document</a>
     */
    public String vacation(VacationDTO dto){
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
                .map(item -> new BigDecimal(item.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return vacationHour.toString();
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/queries-the-column-value-of-the-attendance-report">api document</a>
     */
    public String overtime(OverTimeDTO dto){
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/getcolumnval");
        OapiAttendanceGetcolumnvalRequest req = new OapiAttendanceGetcolumnvalRequest();
        req.setUserid(dto.getUserId());
        req.setColumnIdList(dto.getColumnIdList());
        // 默认计算当前月份
        req.setFromDate(StringUtils.parseDateTime(DateUtil.getFirstDayOfMonth()));
        req.setToDate(StringUtils.parseDateTime(DateUtil.getLastDayOfMonth()));
        OapiAttendanceGetcolumnvalResponse rsp;
        try {
            rsp = client.execute(req, accessToken());
        } catch (ApiException e) {
            throw new ServiceException(e);
        }
        OapiAttendanceGetcolumnvalResponse.ColumnForTopVo columnVo = rsp.getResult().getColumnVals().get(0).getColumnVo();
        log.info("columnVo:{}", JSON.toJSONString(columnVo));
        List<OapiAttendanceGetcolumnvalResponse.ColumnDayAndVal> list = rsp.getResult().getColumnVals().get(0).getColumnVals();
        // 累加加班时间
        BigDecimal overTime = list.stream()
                .map(e -> new BigDecimal(e.getValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return JSON.toJSONString(overTime.toString());
    }

    /**
     * <a href="https://open.dingtalk.com/document/orgapp/get-the-user-s-to-do-items">api document</a>
     */
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
     * calendarClient
     */
    public static com.aliyun.dingtalkcalendar_1_0.Client calendarClient() {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = HTTPS;
        config.regionId = CENTRAL;
        try {
            return new com.aliyun.dingtalkcalendar_1_0.Client(config);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * todoClient
     */
    public static com.aliyun.dingtalktodo_1_0.Client todoClient() {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = HTTPS;
        config.regionId = CENTRAL;
        try {
            return new com.aliyun.dingtalktodo_1_0.Client(config);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * ding-talk oauth2.0 client
     */
    public static com.aliyun.dingtalkoauth2_1_0.Client authClient() {
        Config config = new Config();
        config.protocol = HTTPS;
        config.regionId = CENTRAL;
        try {
            return new Client(config);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 获取用户个人信息
     */
    public String getUserinfo(String accessToken) {
        com.aliyun.dingtalkcontact_1_0.Client client = contactClient();
        GetUserHeaders getUserHeaders = new GetUserHeaders();
        getUserHeaders.xAcsDingtalkAccessToken = accessToken;
        // 获取用户个人信息，如需获取当前授权人的信息，unionId参数必须传me
        GetUserResponse rsp;
        try {
            rsp = client.getUserWithOptions("me", getUserHeaders, new RuntimeOptions());
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return JSON.toJSONString(rsp.getBody());
    }

    /**
     * contactClient
     */
    public static com.aliyun.dingtalkcontact_1_0.Client contactClient() {
        Config config = new Config();
        config.protocol = HTTPS;
        config.regionId = CENTRAL;
        try {
            return new com.aliyun.dingtalkcontact_1_0.Client(config);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }
}
