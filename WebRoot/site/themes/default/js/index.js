// JavaScript Document
$(document).ready(function() {		
	$('.new_m').kxbdSuperMarquee({
				isMarquee:true,
				isEqual:false,
				scrollDelay:80,
				controlBtn:{up:'#goUM',down:'#goDM'},
				direction:'up'
	});	
	$(".special_inner li").hover(function(){
		$(this).find(".spec_pic img").addClass("xuanzhuan");
		},function(){
		$(this).find(".spec_pic img").removeClass("xuanzhuan");
			});
	$(function() {
		});
	});