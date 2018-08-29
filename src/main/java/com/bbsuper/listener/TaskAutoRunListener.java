package com.bbsuper.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.bbsuper.core.TaskOperator;
import com.bbsuper.core.TaskScheduler;
import com.bbsuper.model.scheduler.TaskManager;
import com.bbsuper.model.scheduler.enums.TaskManagerStatusEnum;
import com.bbsuper.service.scheduler.impl.TaskManagerServiceImpl;

/**
 * 定时任务监听器
 * @author yinyuqiao
 * 2017年6月22日 下午2:05:14
 */
@Component
@SuppressWarnings("rawtypes")
public class TaskAutoRunListener implements ApplicationListener {
	private static final org.slf4j.Logger logger  = LoggerFactory.getLogger(TaskAutoRunListener.class);
	@Resource
	private TaskManagerServiceImpl taskManagerService;
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			// 启动定时器
			try {
				TaskScheduler.getScheduler().start();
			} catch (SchedulerException e) {
				logger.error("启动定时器失败", e);
			}
			
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("status", TaskManagerStatusEnum.ENABLE);
			List<TaskManager> taskList = taskManagerService.selectByMap(paramMap);
			
			if(null == taskList || taskList.size() == 0) {
				return;
			}
			for(TaskManager t: taskList) {
				try {
					// 开始任务
					TaskOperator.startTask(t);
				} catch (Exception e) {
					logger.error("监听定时器任务启动失败", e);
				}
			}
		}
	}

}
