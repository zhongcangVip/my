package com.bbsuper.task.web;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bbsuper.service.web.backend.BiddingService;
import com.bbsuper.service.web.backend.NewsService;

/**
 * 定时发布资讯
 * @author hugh
 * 2017年7月26日 下午14:23:54
 */
@Component
public class ReleaseNewsTask {
    private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(ReleaseNewsTask.class);
    @Autowired
    private BiddingService biddingService;
    @Autowired
    private NewsService newsService;

    /**
     * 定时发布资讯
     */
    public void exectueRelaseNews() {
        logger.info("ReleaseNewsTask start ...");
        try {
            logger.info("ReleaseNewsTask running ...");
            newsService.relaseTimingNews();
            biddingService.relaseTimingNews();
        } catch (Exception e) {
            logger.error("ReleaseNewsTask exception", e);
        }
        logger.info("ReleaseNewsTask end ...");
    }

}
