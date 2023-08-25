package org.ashe.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToDoTaskDTO {

    /**
     * 待办标题
     */
    private String subject;

    /**
     * 待办内容
     */
    private String description;

    /**
     * 截止时间与当前时间的偏移量
     */
    private int dueTime;

    /**
     * 待办创建人
     */
    private String creatorId;

    /**
     * 待办执行者    多个之间用英文逗号隔开
     */
    private String executorIds;

    /**
     * 当前访问资源所归属用户的unionId，和创建者的unionId保持一致
     */
    private String unionId;

    /**
     * 优先级，取值：
     *     10：较低
     *     20：普通
     *     30：紧急
     *     40：非常紧急
     */
    private Integer priority = 20;

    /**
     * 生成的待办是否仅展示在执行者的待办列表中
     */
    private Boolean isOnlyShowExecutor = true;

}
