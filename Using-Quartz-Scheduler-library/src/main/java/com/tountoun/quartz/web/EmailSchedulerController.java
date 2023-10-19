package com.tountoun.quartz.web;

import com.tountoun.quartz.jobs.EmailJob;
import com.tountoun.quartz.payload.EmailRequest;
import com.tountoun.quartz.payload.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@RestController
@Slf4j
public class EmailSchedulerController {

    @Autowired
    private Scheduler scheduler;

    @PostMapping(value = "/schedule/email", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmailResponse>  scheduleEmail(@Valid @RequestBody EmailRequest request){
        try{
            ZonedDateTime dateTime = ZonedDateTime.of(request.getDateTime(), request.getZoneId());
            if(dateTime.isBefore(ZonedDateTime.now())){
                EmailResponse response = new EmailResponse(false, "The dateTime is not set correctly");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            JobDetail jobDetail = buildJobDetail(request);
            Trigger trigger = buildTrigger(jobDetail, dateTime);
            scheduler.scheduleJob(jobDetail, trigger);

            EmailResponse response = new EmailResponse(
                    true, jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email schedule successfully"
                    );
            return ResponseEntity.ok(response);
        }catch (SchedulerException e){
            log.error("Error while scheduling the email ", e);
            EmailResponse response = new EmailResponse(false, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/get")
    public ResponseEntity<String> testController(){
        ZoneId zone = ZoneId.systemDefault();
        System.out.println("zone.toString() = " + zone.toString());
        return ResponseEntity.ok("Ok, everything is correct");
    }
    // This helps build a job detail for each email request
    private JobDetail buildJobDetail(EmailRequest  request){
        JobDataMap map = new JobDataMap();
        map.put("email", request.getEmail());
        map.put("subject", request.getSubject());
        map.put("body", request.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send email job")
                .usingJobData(map)
                .storeDurably()
                .build();
    }
    // This helps build a trigger for each email request
    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send email trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}