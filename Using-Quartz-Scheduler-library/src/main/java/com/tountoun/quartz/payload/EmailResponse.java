package com.tountoun.quartz.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class EmailResponse {
    private boolean success;
    private String jobId;
    private String jobGroup;
    private String message;

    public EmailResponse(boolean success, String message){
        this.success = success;
        this.message = message;
    }
    public EmailResponse(boolean success, String jobId, String jobGroup, String message){
        this(success, message);
        this.jobId = jobId;
        this.jobGroup = jobGroup;
    }

}
