(function() {
	var _skin='default', _jQuery;
	var _search = window.location.search;
	if (_search) {
		_skin = _search.split('demoSkin=')[1];		
		_jQuery = _search.indexOf('jQuery=true') !== -1;
		if (_jQuery) document.write('<scr'+'ipt src="'+ctx+'/themes/default/scripts/plugin/jquery.min.js"></sc'+'ript>');
	};
	document.write('<scr'+'ipt src="'+ctx+'/themes/default/scripts/plugin/artDialog/artDialog.source.js?skin=' + (_skin || 'default') +'"></sc'+'ript>');
	window._isDemoSkin = !!_skin;
})();

/***
 * 调用子页面方法（可返回子页面jquery对象）
 * param1 artDialog Id
 * param2 方法名称 
 * param....  可以传多个参数 。
 */
function callDialogFun(){
	var  dialogId= arguments[0];
	if(!dialogId){
		art.dialog.tips("调用的页面不存在！");
        return false; 		
	}
    var dialogObj= art.dialog.list[dialogId];	
    alert(dialogId);
	var iframeObj=getIframe(dialogId);
	var  funName= arguments[1];	
	if(funName){
		var params=[dialogObj];
		for(var i=2;i<arguments.length;i++){
			params.push(arguments[i]);
		}
		var funNames= funName.split(".");
		try {
			var fun = iframeObj.contentWindow[funNames[0]];
			for (var i = 1; i < funNames.length; i++) {
				fun = fun[funNames[i]];
			}
			fun.apply(window, params);
		}catch(e){
			alert(e);
		}
	}else{
	     return $(iframeObj.contentWindow.document);
	}
}
function getIframe(dialogId){
	var temps= $(self.parent.document).find("iframe[name='Open"+dialogId+"']");
	var iframeObj;
	if (temps != null && temps.size() > 0) {
		iframeObj=temps.get(0);
	}else{
		iframeObj=$(document).find("iframe[name='Open" + dialogId + "']").get(0);
	}
	return iframeObj;
}
