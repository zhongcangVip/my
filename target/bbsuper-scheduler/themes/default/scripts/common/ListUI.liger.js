var $list_dataGrid;//表格引用
var $list_dataParam = {};//主要用于搜索
var $list_currentPageData;//当前页
var $list_editUrl;//编辑及查看url
var $list_addUrl;//新增url
var $list_editWidth;
var $list_editHeight;
var $list_deleteUrl;//刪除url
var $list_dataType;//数据名称
var $list_defaultGridParam = {
	width: '99%', 
    height: '99%', 
    checkbox: false,
    rownumbers:true,
    enabledSort:false,
    pageSize:20,
    pageParmName:'currentPage',
    pagesizeParmName:'pageSize',
    root:'items',
    record:'recordCount',
    pageSize:30,
    pageSizeOptions:[20,30,40,60,100],
    onBeforeShowData:function(data){
    	$list_currentPageData = data;
    	function updateColumnData(column){
    		var name = column.columnname;
    		if(name && name.indexOf('.')>0){
    			var names = name.split('.');
    			if(data.items){
	       			 for(var i = 0; i < data.items.length; i++){
	       				 var tmp = data.items[i];
	       				 for(var j = 0;j < names.length; j++){
	       					 if(tmp[names[j]]!=null){
	       						 tmp = tmp[names[j]];
	       					 }else{
	       						 tmp = null;
	       						 break;
	       					 }
	       				 }
	       				data.items[i][name] = tmp;
	       			 }
    			}
    		}
    		if(column.columns && column.columns.length > 0){
    			for(var i = 0; i < column.columns.length;i++){
    				updateColumnData(column.columns[i]);
    			}
    		}
    	}
    	if(this.columns){
    		for(var i = 0; i < this.columns.length;i++){
    			updateColumnData(this.columns[i]);
    		}
    	}
    	return true;
    },
    onDblClickRow:function(rowData,rowIndex,rowDomElement){
    	viewRow(rowData);
    }
};
//增加行
function addRow(source,param){
	if($list_addUrl && $list_addUrl!=''){
		var paramStr = '';
		if($list_addUrl.indexOf('?')>0){
			paramStr = '&VIEWSTATE=ADD';
		}else{
			paramStr = '?VIEWSTATE=ADD';
		}
		if(param){
			for(var p in param){
				paramStr = paramStr + '&' + p + '=' + param[p];
			}
		}
		
		var addDlg = function saveAddData(){
			if(dlg.iframe.contentWindow && dlg.iframe.contentWindow.saveAdd){
				dlg.iframe.contentWindow.saveAdd();
			}
			return false;
		};
		
		var dlg = art.dialog.open($list_addUrl+paramStr,
				{title:getTitle('ADD'),
				 lock:true,
				 width:$list_editWidth||'auto',
				 height:$list_editHeight||'auto',
				 id:$list_dataType+"-ADD",
				 button:[{name:'确定',callback:addDlg},{name:'取消'}],
				 close:resetList
				});
	}
}

//查看行
function viewRow(rowData){
	if($list_editUrl && $list_editUrl!=''){
		var paramStr;
		if($list_editUrl.indexOf('?')>0){
			paramStr = '&VIEWSTATE=VIEW&id='+rowData.id;
		}else{
			paramStr = '?VIEWSTATE=VIEW&id='+rowData.id;
		}
		art.dialog.open($list_editUrl+paramStr,
				{title:getTitle('VIEW'),
				lock:true,
				width:$list_editWidth||'auto',
				height:$list_editHeight||'auto',
				id:$list_dataType+'-VIEW',
				button:[{name:'关闭'}]}
		);
	}
}
function editRow(rowData){
	if($list_editUrl && $list_editUrl!=''){
		var paramStr;
		if($list_editUrl.indexOf('?')>0){
			paramStr = '&VIEWSTATE=EDIT&id='+rowData.id;
		}else{
			paramStr = '?VIEWSTATE=EDIT&id='+rowData.id;
		}
		var editDlg = function saveEditData(){
			if(dlg.iframe.contentWindow && dlg.iframe.contentWindow.saveEdit){
				dlg.iframe.contentWindow.saveEdit();
			}
			return false;
		};
		var dlg = art.dialog.open($list_editUrl+paramStr,
				{title:getTitle('EDIT'),
				 lock:true,
				 width:$list_editWidth||'auto',
				 height:$list_editHeight||'auto',
				 id:$list_dataType+"-EDIT",
				 button:[{name:'确定',callback:editDlg},{name:'取消'}],
				 close:refresh
				});
	}
}

//刪除行
function deleteRow(rowData){
	if($list_deleteUrl && $list_deleteUrl!=''){
		art.dialog.confirm('确定删除该行数据?',function(){
			$.post($list_deleteUrl,{id:rowData.id},function(res){
				art.dialog.tips('<span>刪除成功</span>');
				refresh();
			});
			return true;
		},function(){
			return true;
		});
	}
}

function resizeDialog(id){
	if(art.dialog.list[id]){
		var dlg = art.dialog.list[id];
		var _isIE6 = window.VBArray && !window.XMLHttpRequest;
		var aConfig = dlg.config;
		var iwin = dlg.iframe.contentWindow;
		var $idoc = $(iwin.document);
		ibody = iwin.document.body;
		var iWidth = aConfig.width === 'auto'
		? $idoc.width() + (_isIE6 ? 0 : parseInt($(ibody).css('marginLeft')))
		: aConfig.width;
		
		var iHeight = aConfig.height === 'auto'
		? $idoc.height()
		: aConfig.height;
		
		dlg.size(iWidth, iHeight);
		
		aConfig.follow
		? dlg.follow(aConfig.follow)
		: dlg.position(aConfig.left, aConfig.top);
	}
}

function getTitle(viewstate){
	if(!$list_dataType){
		$list_dataType = "数据";
	}
	switch(viewstate){
		case 'ADD':return $list_dataType+'-新增';
		case 'VIEW':return $list_dataType+'-查看';
		case 'EDIT':return $list_dataType+'-编辑';
		default:return $list_dataType;
	}
}

/**
 * 表格重置查詢
 */
function resetList(){
	if($list_dataGrid){
		$list_dataGrid.setOptions({
			parms:$list_dataParam
		});
		$list_dataGrid.loadData();
		$list_dataGrid.changePage('first');
	}
}

function refresh(){
	$list_dataGrid.loadData();
}

function searchData(){
	var kw = $('#searchKeyWord').val();
	kw = kw.replace(/^\s+|\s+$/g,'');
	$('#searchKeyWord').val(kw);
	if(kw==$('#searchKeyWord').attr('defaultValue')){
		kw='';
	}
	if(kw==null || kw == ''){
		$list_dataParam['key'] = null;
	}else{
		$list_dataParam['key'] = kw;
	}
	resetList();
}

$(document).ajaxError(function(event,request, settings){
	$('.bodyMask').hide();
	var content = '不好意思,出错啦。';
	var detailContent = '<div>'+request.responseText.substring(request.responseText.indexOf('<body>')+6,request.responseText.indexOf('</body>'))+'</div>';
	function showErrorMsg(){
		var errorLog = art.dialog({
			title:"详细信息",
			content:detailContent,
			lock:true,
			height:320,
			width:400,
			button:[{name:"确定"}]
		});
		return false;
	}
	function closeErrorDialog(){
		dialog.close();
	}
	var dialog = art.dialog({
		title:"信息提示",
		icon:"error",
	    content: content,
	    id: "ajaxErrorDialog",
	    lock:true,
	    button:[{name:"详细信息",callback:showErrorMsg},{name:"确定",callback:closeErrorDialog}]
	});
	dialog.show();
});
/**yyyy-mm-dd**/
function format2date(str){
	if(str==null || str.length == 0){
		return null;
	}
	var year = parseInt(str.substring(0,str.indexOf('-')),10);
	var month = parseInt(str.substring(str.indexOf('-')+1,str.lastIndexOf('-')),10);
	var date = parseInt(str.substring(str.lastIndexOf('-')+1));
	return new Date(year,month-1,date);
}