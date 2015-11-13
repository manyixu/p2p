// JavaScript Document

$(document).ready(function() {
	$(".js_zhankai").hide();
	$(".js_zhankai:first").show();
	$(".js_btn_chakan:first").html("收起");
	$(".js_btn_chakan").click(function(){
		var tD = $(this).parent().siblings(".js_zhankai").css("display");
		if(tD=="none"){
			$(this).parent().siblings(".js_zhankai").slideDown("fast");
			$(this).html("收起");
		}else{
			$(this).parent().siblings(".js_zhankai").slideUp("fast");
			$(this).html("查看");
		}
	});
			
	
		$(".nav ul li a ").hover(function(){
		$(".nav ul li a").css("color","#50b33c");
		$(".nav ul li a ").css("background","#f2f2f2");
		$(this).css("color","#fff");
		$(this).css("background","#50b33c");
		},function(){
		});
		});