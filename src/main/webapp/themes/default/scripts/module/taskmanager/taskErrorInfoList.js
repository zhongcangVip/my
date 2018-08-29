Package("com.bbsuper.taskmanager.taskErrorInfoList");
com.bbsuper.taskmanager.taskErrorInfoList = {	
	tabLigerList : function() {
		var docH = $("body").height();
		tabelH = docH-80;
		$list_dataGrid = $("#tableContainer").ligerGrid($.extend($list_defaultGridParam,
	{
		columns : [
				{
					display : '任务名',
					name : 'taskManager.name',
					align : 'center',
					width : 400
				},
				{
					display : '异常时间',
					name : 'createTimeStr',
					align : 'center',
					width : 230
				},
				{
					display : '异常內容',
					name : 'info',
					align : 'center',
					width : 180,
					render : function(record, rowindex, value, column) {
						return "<a href=javascript:com.bbsuper.taskmanager.taskErrorInfoList.errorInfoDetail("
								+ record.id + ")>查看详细错误信息</a>";
					}
				},
				{
					display : '操作',
					name : 'id',
					align : 'center',
					width : 120,
					render : function(record,rowindex,value, column) {
						return "<a href=javascript:com.bbsuper.taskmanager.taskErrorInfoList.deleteInfo('"
								+ value+ "')>刪除</a>";
					}
				} ],
		pageSize : 28,
		rownumbers : false,
		url : ctx + '/taskmanager/viewErrorInfo/query?id=' + $("#id").val()
	}));
	},

	searchData : function() {
		$list_dataParam['taskId']=$("#taskId").val();
		resetList();
	},

	
	errorInfoDetail : function(id){
		art.dialog.open(ctx + "/taskmanager/viewErrorInfo/detail?id=" + id, {
			id : 'errorInfoDetail',
			title : '查看异常內容',
			lock : true,
			cancel : true,
			resize : true,
			padding : 0,
			width : 800,
			height : 600
		});
	},
	
	
	deleteInfo : function(id){
		art.dialog.confirm("是否确定刪除?", function() {
		$.ajax({
			 type:"POST",
			 url:ctx+"/taskmanager/deleteErrorInfo",
			 data:{"id":id},
			 success:function(data){
				 data = $.parseJSON(data);
				 if(data.flag)
				 {
				    art.dialog.tips('刪除成功');
				    com.bbsuper.taskmanager.taskErrorInfoList.searchData();
				 }else
				 {
					 art.dialog.tips('刪除失败');
				 }
				}
			 });
		});
	}
}
$(document).ready(function() {
	com.bbsuper.taskmanager.taskErrorInfoList.tabLigerList();
})