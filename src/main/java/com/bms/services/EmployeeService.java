package com.bms.services;

import com.bms.entity.EmployeeInformation;
import com.bms.entity.LoginDetails;

public interface EmployeeService {

	public EmployeeInformation addEmployeeService(EmployeeInformation employeeinformation);

	public void loginService(LoginDetails logindetails);

}
