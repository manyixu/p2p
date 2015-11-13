// JavaScript Document
$(document).ready(function() {	
	
	$('.frist').css("color","#3ec226");
	$('.frist').css("borderBottom","2px solid #3ec226");
	$(".nav li a ").click(function(){
		$('.nav li a').css("color","#333");
		$(".nav li a ").css("borderBottom","#fff");
		$(this).css("color","#61bc51");
		$(this).css("borderBottom","2px solid #3ec226");
		});
	$('.new_m').kxbdSuperMarquee({
				distance:23,
				isMarquee:true,
				isEqual:true,
				time:5,
				scrollDelay:120,
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
	$('.progressbar').each(function(){
		var t = $(this),
		dataperc = t.attr('data-perc'),
		barperc = Math.round(dataperc*0.88);
		t.find('.bar').animate({width:barperc}, dataperc*25);
		t.find('.label').append('<div class="perc"></div>');
		function perc(){
			var length = t.find('.bar').css('width'),
			perc = Math.floor(parseInt(length)/0.88),
			labelpos = (parseInt(length)-2);
			/*t.find('.label').css('left', labelpos);*/
			t.find('.perc').text(perc+'%');
		}
		perc();
		setInterval(perc, 0); 
	});
	});