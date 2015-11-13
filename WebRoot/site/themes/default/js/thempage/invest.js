$(function(){
		/*元素跳动*/
		$(".c_team").mouseover(function(){
			if(!($(".c_finance").is(":animated"))){   //防止其反复跳动  第四个_客户服务
				$(".c_finance").animate({"top":"108px",},200).animate({"top":"116px",},200)  
				.animate({"top":"100px",},200).animate({"top":"115px",},200)
				.animate({"top":"118px",},200).animate({"top":"108px",},200); 
			
		    } 
			if(!($(".c_user").is(":animated"))){   //防止其反复跳动  第二个_风控专员
				$(".c_user").animate({"top":"60px",},200).animate({"top":"80px",},200)  
				.animate({"top":"65px",},200).animate({"top":"89px",},200)  
				.animate({"top":"75px",},200).animate({"top":"80px",},200)  ;
				
		    } 
			if(!($(".c_service").is(":animated"))){   //防止其反复跳动  第三个_风控专员
				$(".c_service").animate({"top":"112px",},200).animate({"top":"135px",},200)  
				.animate({"top":"120px",},200).animate({"top":"130px",},200)  
				.animate({"top":"115px",},200).animate({"top":"120px",},200)  ;
				
		    } 
		    if(!($(".c_uer").is(":animated"))){   //防止其反复跳动  第一个_客户服务
				$(".c_uer").animate({"top":"145px",},200).animate({"top":"160px",},200)  
				.animate({"top":"150px",},200).animate({"top":"160px",},200)  
				.animate({"top":"155px",},200).animate({"top":"160px",},200)  ;
			
		    } 
		});
		$(".c_manage_money").mouseover(function(){  
			if(!($(".c_money_invest").is(":animated"))){   //防止其反复跳动
				$(".c_money_invest").animate({"top":"0",},200).animate({"top":"10px",},200)  
				.animate({"top":"30px",},200).animate({"top":"20px",},200)  
				.animate({"top":"35px",},200).animate({"top":"20px",},200)  
				.animate({"top":"35px",},200).animate({"top":"0",},200);  
		    }
		}); 
		$(".c_trust").mouseover(function(){  
			if(!($(".c_invest_man").is(":animated"))){   //防止其反复跳动
				$(".c_invest_man").animate({"bottom":"20px",},200).animate({"bottom":"40px",},200)  
				.animate({"bottom":"20px",},200).animate({"bottom":"40px",},200)  
				.animate({"bottom":"20px",},200).animate({"bottom":"30px",},200)  
				.animate({"bottom":"10px",},200).animate({"bottom":"20px",},200);  
		    } 
			if(!($(".c_loan_man").is(":animated"))){   //防止其反复跳动
				$(".c_loan_man").animate({"bottom":"130px",},200).animate({"bottom":"150px",},200)  
				.animate({"bottom":"130px",},200).animate({"bottom":"150px",},200)  
				.animate({"bottom":"130px",},200).animate({"bottom":"150px",},200)  
				.animate({"bottom":"140px",},200).animate({"bottom":"130px",},200);  
		    } 
		});
		//安全防护无限旋转
		for (var i = 0; i <= 100000; i++) {
			$(".c_security_img img").rotate({
				animateTo: i,
				duration: 10000000
			});
		}
	})