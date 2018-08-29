package com.bbsuper.task.web;

import com.bbsuper.common.Constants;
import com.bbsuper.dao.cache.Cache;
import com.bbsuper.service.web.bussiness.DriverService;
import com.bbsuper.service.web.bussiness.OwnerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 网站首页车源、货源定时更新
 * @author huangjinwen
 * 2017年8月8日 上午9:45:21
 */
@Component
public class IndexSourceUpdateTask {

    private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(IndexSourceUpdateTask.class);

    @Resource(name="redisCache")
    private Cache redisCache;

    @Autowired
    private DriverService driverService;

    @Autowired
    private OwnerService ownerService;

    /**
     * 更新缓存中的实时车源信息和实时货源信息
     */
    public void executeUpdateSource(){
        logger.info("IndexSourceUpdateTask executeUpdateSource start ...");
        try {
            logger.info("IndexSourceUpdateTask executeUpdateSource running ...");
            // 实时车源信息
            List<Map<String,Object>> realTimeVehicleSource = driverService.getVehicleSource(null);
            redisCache.put(Constants.LEAST_VEHICLE_CACHE_KEY, realTimeVehicleSource, Constants.SESSION_TIME*24);
            // 实时货源信息
            List<Map<String,Object>> realTimeGoodsSource = ownerService.getGoodsSource(null);
            redisCache.put(Constants.LEAST_GOODS_CACHE_KEY, realTimeGoodsSource, Constants.SESSION_TIME*24);
        } catch (Exception e) {
            logger.error("IndexSourceUpdateTask executeUpdateSource exception", e);
        }
        logger.info("IndexSourceUpdateTask executeUpdateSource end.");
    }
}
