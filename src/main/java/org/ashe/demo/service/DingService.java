package org.ashe.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ashe.demo.controller.OverTimeDTO;
import org.ashe.demo.dto.*;
import org.ashe.demo.infra.DingClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DingService {

    private final DingClient dingClient;


    public String getAccessToken() {
        return dingClient.accessToken();
    }

    public String getDepartment(DeptDTO dto) {
        return dingClient.department(dto);
    }

    public String getDepartmentUser(DeptDTO dto) {
        return dingClient.departmentUser(dto);
    }

    public String getUserInfo(UserDTO dto) {
        return dingClient.userInfo(dto);
    }

    public void createEvent(EventDTO dto) {
        dingClient.createEvent(dto);
    }

    public void createTodoTask(TodoTaskDTO dto) {
        dingClient.createTodoTask(dto);
    }

    public String vacation(VacationDTO dto) {
        // 参数校验 & 初始化
        dto.init();
        return dingClient.vacation(dto);
    }

    public String overtime(OverTimeDTO dto) {
        return dingClient.overtime(dto);
    }

    public String todoList(UserDTO dto) {
        return dingClient.todoList(dto);
    }

    public String scanLogin(String authCode) {
        return dingClient.scanLogin(authCode);
    }
}
