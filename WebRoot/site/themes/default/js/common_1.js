// JavaScript Document
$(document).ready(function() {	
	$(".ma").hide()
		$(".weixinn").hover(function(){
		$(".ma").show()
		},function(){
		$(".ma").hide()
			});	
	$('.frist').css("color","#3ec226");
	$('.frist').css("borderBottom","2px solid #3ec226");
	$(".nav li a ").click(function(){
		$('.nav li a').css("color","#333");
		$(".nav li a ").css("borderBottom","#fff");
		$(this).css("color","#61bc51");
		$(this).css("borderBottom","2px solid #3ec226");
		});

	$(".special_inner li").hover(function(){
		$(this).find(".spec_pic img").addClass("xuanzhuan");
		},function(){
		$(this).find(".spec_pic img").removeClass("xuanzhuan");
			});
	$(function() {
		});
	

	});