package org.ashe.demo.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverTimeDTO {

    private String userId;

    /**
     * {
     * "extension":"null",
     * "sub_type":0,
     * "name":"工作日加班",
     * "alias":"overtime_工作日加班",
     * "expression_id":737328856,
     * "id":737335856,
     * "type":0,
     * "status":0
     * },
     * {
     * "extension":"null",
     * "sub_type":0,
     * "name":"休息日加班",
     * "alias":"overtime_休息日加班",
     * "expression_id":737328857,
     * "id":737335857,
     * "type":0,
     * "status":0
     * },
     * {
     * "extension":"null",
     * "sub_type":0,
     * "name":"节假日加班",
     * "alias":"overtime_节假日加班",
     * "expression_id":737328858,
     * "id":737335858,
     * "type":0,
     * "status":0
     * }
     */
    private String columnIdList; // 传对应id,多个之间用英文逗号隔开

}
