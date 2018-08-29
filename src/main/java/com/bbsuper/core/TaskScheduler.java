package com.bbsuper.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.bbsuper.model.scheduler.enums.TaskRunningStatusEnum;

/**
 * scheduler模块管理
 * @author yinyuqiao
 * 2017年6月22日 下午2:05:45
 */
public class TaskScheduler {
	private static final Logger logger = Logger.getLogger(TaskScheduler.class);
	private static Scheduler scheduler = null; //任务计划
	// 当前所有任务执行状态
	private static Map<Integer, TaskRunningStatusEnum> taskRunningStatusMap = new HashMap<Integer, TaskRunningStatusEnum>();

	/**
	 * 获取定时器对象
	 * @return
	 */
	public static Scheduler getScheduler() {
		if (scheduler == null) {
			try {
				// 创建定时任务
				scheduler = new StdSchedulerFactory("/quartz/quartz.properties").getScheduler();
			} catch (SchedulerException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return scheduler;
	}
	
	/**
	 * 获取当前任务执行状态
	 * @return
	 */
	public static Map<Integer, TaskRunningStatusEnum> getTaskRunningStatusMap() {
		return taskRunningStatusMap;
	}
	
	/**
	 * 获取当前任务执行状态
	 * Integer : taskMangerId
	 * @return
	 */
	public static TaskRunningStatusEnum getTaskRunningStatus(Integer id) {
		TaskRunningStatusEnum taskRunningStatusEnum = taskRunningStatusMap.get(id);
		// 默认为停止状态
		if(taskRunningStatusEnum == null) {
			return TaskRunningStatusEnum.STOP;
		}
		
		return taskRunningStatusEnum;
	}
	
	/**
	 * 设置执行状态
	 * @param id
	 * @param taskStatusEnum
	 * @return
	 */
	public static void setTaskStatusMap(Integer id, TaskRunningStatusEnum taskStatusEnum) {
		taskRunningStatusMap.put(id, taskStatusEnum);
	}
	
	/**
	 * 删除执行状态
	 * @param id
	 * @return
	 */
	public static void removeTaskStatusMapById(Integer id) {
		taskRunningStatusMap.remove(id);
	}
	
}
