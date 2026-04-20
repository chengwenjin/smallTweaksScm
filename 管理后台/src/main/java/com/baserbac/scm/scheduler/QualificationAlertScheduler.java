package com.baserbac.scm.scheduler;

import com.baserbac.scm.service.QualificationAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QualificationAlertScheduler {

    private final QualificationAlertService alertService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkQualificationExpiry() {
        log.info("开始执行资质到期预警定时任务...");
        try {
            alertService.checkAndCreateAlerts();
            log.info("资质到期预警定时任务执行完成");
        } catch (Exception e) {
            log.error("资质到期预警定时任务执行失败", e);
        }
    }
}
