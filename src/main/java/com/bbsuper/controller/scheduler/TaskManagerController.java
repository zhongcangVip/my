package com.bbsuper.controller.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbsuper.common.Constants;
import com.bbsuper.common.page.Pagination;
import com.bbsuper.common.util.StringUtil;
import com.bbsuper.controller.BaseController;
import com.bbsuper.core.TaskOperator;
import com.bbsuper.core.TaskProxy;
import com.bbsuper.core.TaskScheduler;
import com.bbsuper.dao.cache.Cache;
import com.bbsuper.model.scheduler.TaskErrorInfo;
import com.bbsuper.model.scheduler.TaskManager;
import com.bbsuper.model.scheduler.enums.TaskManagerStatusEnum;
import com.bbsuper.service.scheduler.TaskErrorInfoService;
import com.bbsuper.service.scheduler.TaskManagerService;

/**
 * 定时器任务管理器
 * @author yinyuqiao
 * 2017年6月22日 下午2:07:30
 */
@Controller
public class TaskManagerController extends BaseController {
	@Resource
	private TaskManagerService taskManagerService;
	@Resource
	private TaskErrorInfoService taskErrorInfoService;
	@Resource
	private TaskProxy taskProxy;
	@Resource(name="redisCache")
	private Cache redisCache;
	
	/**
	 * 任务调度
	 */
	@RequestMapping("/taskmanager/list")
	public String list(ModelMap model) {
		boolean flag = getLoginedUser();
		if(!flag){
			return this.pageNotFound();
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		// 分页查询数据列表列表
		List<TaskManager> taskList = taskManagerService.selectByMap(paramMap);
		if(null != taskList && taskList.size() > 0) {
			for(TaskManager task: taskList) {
				task.setRunningStatus(TaskScheduler.getTaskRunningStatus(task.getId()).toString());
			}
		}
		
		model.put("taskList", taskList);
		return "taskmanager/TaskManagerList";
	}
	
	@ResponseBody
	@RequestMapping("/taskmanager/list/query")
	public void listQuery(Pagination<TaskManager> pagination, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String name = getString("name");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		if(StringUtil.isNotEmpty(name)) {
			paramMap.put("name", name);
		}
		
		// 分页查询数据列表列表
		pagination = taskManagerService.selectByMap(paramMap, pagination);
		
		List<TaskManager> list = pagination.getItems();
		if(null != list && list.size() > 0) {
			for(TaskManager task: list) {
				task.setRunningStatus(TaskScheduler.getTaskRunningStatus(task.getId()).toString());
			}
		}
		
		resultMap.put("items", list);
		resultMap.put("recordCount", pagination.getRecordCount());
		resultMap.put("pageSize", pagination.getPageSize());
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 新增、编辑
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping("/taskmanager/toSaveOrUpdate")
	public String toSaveOrUpdate(Model model, Integer id) {
		if(id != null) {
			model.addAttribute("taskManager", taskManagerService.getTaskManagerById(id));
		}
		
		return "taskmanager/TaskManagerEdit";
	}
	
	/**
	 * 保存
	 * @param taskManager
	 * @param response
	 */
	@ResponseBody
	@RequestMapping("/taskmanager/saveOrUpdate")
	public void saveOrUpdate(TaskManager taskManager, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("flag", false);
		
		if(StringUtil.isEmpty(taskManager.getName()) || taskManager.getName().length() > 50) {
			resultMap.put("errorMsg", "任务名称不能为空且不能超过50的字符!");
			// 输出JSON
			outPrint(response, JSONObject.fromObject(resultMap).toString());
			return;
		}
		
		if(StringUtil.isEmpty(taskManager.getParam()) || taskManager.getParam().length() > 50) {
			resultMap.put("errorMsg", "时间表达式不能为空且不能超过50的字符!");
			// 输出JSON
			outPrint(response, JSONObject.fromObject(resultMap).toString());
			return;
		}
		
		if(StringUtil.isEmpty(taskManager.getClassName()) || taskManager.getClassName().length() > 50) {
			resultMap.put("errorMsg", "类名不能为空且不能超过50的字符!");
			// 输出JSON
			outPrint(response, JSONObject.fromObject(resultMap).toString());
			return;
		}
		
		if(StringUtil.isEmpty(taskManager.getMethodName()) || taskManager.getMethodName().length() > 50) {
			resultMap.put("errorMsg", "方法名称不能为空且不能超过50的字符!");
			// 输出JSON
			outPrint(response, JSONObject.fromObject(resultMap).toString());
			return;
		}
		
		boolean flag = false;
		if(taskManager.getId() == null) {
			taskManager.setStatus(TaskManagerStatusEnum.ENABLE);
			taskManager.setCreateTime(new Date());
			flag = taskManagerService.insert(taskManager); // 新增
		}else {
			flag = taskManagerService.update(taskManager); // 更新
		}
		
		if(flag) {
			resultMap.put("flag", true);
		}else {
			resultMap.put("errorMsg", "操作失敗，請重新嘗試！");
		}
		// 启动任务
		try {
			TaskOperator.startTask(taskManager);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 更改任务状态
	 * @param taskManager
	 * @param response
	 */
	@ResponseBody
	@RequestMapping("/taskmanager/changeStatus")
	public void changeStatus(TaskManager taskManager, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("flag", taskManagerService.update(taskManager));
		// 获得实体类完整信息
		taskManager = taskManagerService.getTaskManagerById(taskManager.getId());
		// 启用
		if(taskManager.getStatus() == TaskManagerStatusEnum.ENABLE) {
			try {
				TaskOperator.startTask(taskManager);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("errorMsg", e.getMessage());
			}
		}else {
			try {
				TaskOperator.stopTask(taskManager);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("errorMsg", e.getMessage());
			}
		}
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 启动任务
	 * @param id
	 * @param response
	 */
	@ResponseBody
	@RequestMapping("/taskmanager/startTask")
	public void startTask(Integer id, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		// 获得实体类完整信息
		TaskManager taskManager = taskManagerService.getTaskManagerById(id);
		
		try {
			// 当前任务状态为无效
			if(taskManager.getStatus() == TaskManagerStatusEnum.DISABLE) {
				resultMap.put("flag", false);
				resultMap.put("errorMsg", "请先启用任务");
			}else {
				TaskOperator.startTask(taskManager);
				resultMap.put("flag", true);
			}
		} catch (Exception e) {
			resultMap.put("flag", false);
			e.printStackTrace();
			resultMap.put("errorMsg", e.getMessage());
		}
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 停止任务
	 * @param id
	 * @param response
	 */
	@ResponseBody
	@RequestMapping("/taskmanager/stopTask")
	public void stopTask(Integer id, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			TaskOperator.stopTask(taskManagerService.getTaskManagerById(id));
			resultMap.put("flag", true);
		} catch (Exception e) {
			resultMap.put("flag", false);
			e.printStackTrace();
			resultMap.put("errorMsg", e.getMessage());
		}
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 运行任务
	 * @param id
	 * @param response
	 */
	@ResponseBody
	@RequestMapping("/taskmanager/startForOneTask")
	public void startForOneTask(Integer id, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			taskProxy.excuteByReflection(taskManagerService.getTaskManagerById(id));
			resultMap.put("flag", true);
		} catch (Exception e) {
			resultMap.put("flag", false);
			e.printStackTrace();
			resultMap.put("errorMsg", e.getMessage());
		}
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 删除任务
	 * @param id
	 * @param response
	 */
	@ResponseBody
	@RequestMapping("/taskmanager/deleteTask")
	public void deleteTask(Integer id, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			TaskOperator.deleteTask(taskManagerService.getTaskManagerById(id));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		resultMap.put("flag", true);
		// 刪除
		if(!taskManagerService.deleteById(id)) {
			resultMap.put("flag", false);
		}
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 查看错误日志
	 * @param modelMap
	 * @param id
	 * @return
	 */
	@RequestMapping("/taskmanager/viewErrorInfo")
	public String viewErrorInfo(ModelMap modelMap, Integer id) {
		modelMap.put("id", id);
		
		return "taskmanager/TaskErrorInfoList";
	}
	
	/**
	 * 查看日志详情
	 * @param modelMap
	 * @param id
	 * @return
	 */
	@RequestMapping("/taskmanager/viewErrorInfo/detail")
	public String viewErrorInfoDetail(ModelMap modelMap, Integer id) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("id", id);
		modelMap.put("taskErrorInfo", taskErrorInfoService.selectByMap(paramMap).get(0));
		
		return "taskmanager/TaskErrorInfoDetail";
	}
	
	@ResponseBody
	@RequestMapping("/taskmanager/viewErrorInfo/query")
	public void viewErrorInfo(Integer id, HttpServletResponse response, Pagination<TaskErrorInfo> pagination) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("taskManagerId", id);
		
		// 分页查询数据列表列表
		pagination = taskErrorInfoService.selectByMap(paramMap, pagination);
		
		resultMap.put("items", pagination.getItems());
		resultMap.put("recordCount", pagination.getRecordCount());
		resultMap.put("pageSize", pagination.getPageSize());
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	/**
	 * 删除异常
	 * @param id
	 * @param response
	 */
	@ResponseBody
	@RequestMapping("/taskmanager/deleteErrorInfo")
	public void deleteErrorInfo(Integer id, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if(taskErrorInfoService.deleteById(id)) {
			resultMap.put("flag", true);
		}else {
			resultMap.put("flag", false);
		}
		
		// 输出JSON
		outPrint(response, JSONObject.fromObject(resultMap).toString());
	}
	
	@RequestMapping("/pageNotFound")
	public String pageNotFound() {
		return "Error";
	}
	/**
	 * 获经登录信息
	 * @param request
	 * @return
	 */
	private boolean getLoginedUser(){
		String val = (String) redisCache.get(Constants.LOGIN_SCHEDULER_CACHE_KEY);
		if(StringUtil.isNotEmpty(val) && val.equals(Constants.LOGIN_SCHEDULER_CACHE_KEY.toString())){
			return true;
		}
		return false;
	}

}
