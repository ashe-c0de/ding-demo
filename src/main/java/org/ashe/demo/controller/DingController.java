package org.ashe.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ashe.demo.dto.*;
import org.ashe.demo.service.DingService;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class DingController {

    private final DingService dingService;

    /**
     * 扫码登录
     */
    @GetMapping(value = "/ding")
    public String scanLogin(@RequestParam(value = "authCode") String authCode) {
        return dingService.scanLogin(authCode);
    }

    /**
     * 获取企业accessToken
     */
    @GetMapping(value = "/accessToken")
    public String accessToken() {
        return dingService.getAccessToken();
    }

    /**
     * 获取企业部门列表
     */
    @GetMapping("/department")
    public String department(@RequestBody DeptDTO dto) {
        return dingService.getDepartment(dto);
    }

    /**
     * 获取部门用户基础信息
     */
    @GetMapping("/department/user")
    public String departmentUser(@RequestBody DeptDTO dto) {
        return dingService.getDepartmentUser(dto);
    }

    /**
     * 查询用户详情
     */
    @GetMapping("/user/info")
    public String userInfo(@RequestBody UserDTO dto) {
        return dingService.getUserInfo(dto);
    }

    /**
     * 创建日程
     */
    @PostMapping("/create/event")
    public void createEvent(EventDTO dto) {
        dingService.createEvent(dto);
    }

    /**
     * 创建待办
     */
    @PostMapping("/todo-task")
    public void createTodoTask(TodoTaskDTO dto) {
        dingService.createTodoTask(dto);
    }

    /**
     * 获取报表假期数据
     */
    @GetMapping("/vacation")
    public String vacation(@RequestBody VacationDTO dto) {
        return dingService.vacation(dto);
    }

    /**
     * 获取加班时间
     */
    @GetMapping("/overtime")
    public String overtime(OverTimeDTO dto) {
        return  dingService.overtime(dto);
    }

    /**
     * 查询企业下用户待办列表
     */
    @GetMapping("/to-do")
    public String todoList(UserDTO dto) {
        return dingService.todoList(dto);
    }


}