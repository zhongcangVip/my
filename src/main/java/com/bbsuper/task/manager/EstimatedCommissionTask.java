package com.bbsuper.task.manager;


import com.bbsuper.service.manager.CommissionSetService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 预设提成比例生效
 * @author hugh
 * 2017年7月26日 下午14:23:54
 */
@Component
public class EstimatedCommissionTask {

    private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(EstimatedCommissionTask.class);

    @Autowired
    private CommissionSetService commissionSetService;

    /**
     * 定时发布资讯
     */
    public void exectue() {
        logger.info("EstimatedCommissionTask start ...");
        try {
            logger.info("EstimatedCommissionTask running ...");
            commissionSetService.updateCommissionRate();
        } catch (Exception e) {
            logger.error("ReleaseNewsTask exception", e);
        }
        logger.info("ReleaseNewsTask end ...");
    }

}
