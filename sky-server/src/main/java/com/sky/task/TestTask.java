package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author 薛坤
 * @version 1.0
 */
@Component
@Slf4j
public class TestTask {

    // @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask() {
        log.info("每隔5秒执行一次:" + LocalDateTime.now());
    }

}
