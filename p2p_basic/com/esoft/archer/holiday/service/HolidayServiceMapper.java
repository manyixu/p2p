package com.esoft.archer.holiday.service;

import java.util.List;

import com.esoft.archer.holiday.model.HolidayTable;

public interface HolidayServiceMapper {
	
	public String HolidaySendPlatformTransfer(HolidayTable holiday);
	public List find(String sql);

}
