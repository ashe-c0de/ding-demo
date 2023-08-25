package org.ashe.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDTO {
    private String unionId;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String summary;
    private String description;
}
