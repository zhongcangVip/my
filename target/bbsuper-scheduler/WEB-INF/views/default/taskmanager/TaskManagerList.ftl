<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>任务调度列表</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	</head>
	<style>
        table,table tr th, table tr td {border:1px solid}
    </style>
<body>
	<div position="center">	
        <ul class="itemList wrapfix">
            <li><a class="mini-button"  href="javascript:com.bbsuper.taskmanager.taskManagerList.saveOrUpdateTask(0);"><span class="mini-button-text">新增任务</span></a></li>
        </ul>
	</div>
	<div id="tableContainer" >
		<table width="100%" border="1" cellspacing="0" cellpadding="0"> 
        	<thead >
	      		<tr key="listHead" > 
		        	<th width=100>任务名</th>
					<th width=100>执行类</th>
					<th width=100>执行方法</th>
					<th width=100>时间参数</th>
					<th width=100>开始时间</th>
					<th width=''>结束时间</th>
		        	<th width=100>今日状态</th>
		        	<th width=100>启用状态</th>
		        	<th width=100>执行状态</th>
		        	<th width=100>执行异常</th>
		        	<th width=200>操作</th>
	      		</tr>
	      		<#if taskList??>
	      		<#list taskList as task>
	      		
		      		<tr border="1">
						<td width=100>${(task.name)!''}</td>
						<td width=100>${(task.className)!''}</td>
						<td width=100>${(task.methodName)!''}</td>
						<td width=100>${(task.param)!''}</td>
						<td width=100>${(task.startTimeStr)!''}</td>
						<td width=100>${(task.endTimeStr)!''}</td>
						<td width=100>
							<#if (task.isRunToday)??>
								已执行
							<#else>
								未执行
							</#if>	
						</td>
						<td width=100>${(task.statusStr)!''}</td>
						<td width=100>${(task.runningStatus)!''}</td>
						<td width=100>
							<#if (task.errorCount)??&&task.errorCount&gt;0>
								<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.viewErrorInfo("${task.id}")'>查看</a>
							<#else>無</#if>	
						</td>
	                    <td width=120 class="text_c">
	                    	<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.startForOneTask("${task.id}")'>运行</a>&nbsp;
							<#if (task.statusStr)??&&task.statusStr == '禁用'>
								<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.enableTask("${task.id}")'>启用</a>&nbsp;
							<#else>
								<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.disableTask("${task.id}")'>禁用</a>&nbsp;
							</#if>
							
							<#if (task.runningStatus)??&&(task.runningStatus == 'STOP' || task.runningStatus == 'WAIT')>
								<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.startTask("${task.id}")'>启动</a>&nbsp;
							<#else>
								<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.stopTask("${task.id}")'>停止</a>&nbsp;
							</#if>
							<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.saveOrUpdateTask("${task.id}")'>修改</a>&nbsp;
							<a href='javascript:void(0)' onclick='com.bbsuper.taskmanager.taskManagerList.deleteTask("${task.id}")'>刪除</a>
						</td>
					</tr>
	      		
	      		</#list>
	      		</#if>
	      		
	  		</thead>
	  	</table>
	</div>
</body>
</html>
<script type="text/javascript" src="${base}/scripts/plugin/jquery.min.js"></script>
<script type="text/javascript" src="${base}/scripts/common/core.js"></script>
<script type="text/javascript" src="${base}/scripts/common/InitArtDialog.js"></script>
<script type="text/javascript" src="${base}/scripts/plugin/artDialog/plugins/iframeTools.source.js"></script>
<script type="text/javascript" src="${base}/scripts/module/taskmanager/taskManagerList.js"></script>