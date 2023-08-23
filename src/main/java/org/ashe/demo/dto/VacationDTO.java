package org.ashe.demo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

@Getter
@Setter
public class VacationDTO {

    private String userId;

    /**
     * 假期名称，多个用英文逗号分隔，最大长度20。
     */
    private String leaveNames;

    /**
     * 开始时间 yyyy-MM-dd
     */
    private String from;

    /**
     * 截止时间 yyyy-MM-dd
     */
    private String to;

    public void init() {
        Assert.hasLength(userId, "userId can not be null");
        Assert.hasLength(leaveNames, "leaveNames can not be null");
        Assert.hasLength(from, "from can not be null");
        Assert.hasLength(to, "to can not be null");
        if (from.length() == 10) {
            from = from + " 00:00:00";
            to = to + " 23:59:59";
        }
    }
}
