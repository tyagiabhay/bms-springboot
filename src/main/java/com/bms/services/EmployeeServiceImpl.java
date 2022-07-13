package com.bms.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bms.entity.CoverageDetails;
import com.bms.entity.EmployeeInformation;
import com.bms.entity.ForgotPassword;
import com.bms.entity.LoginDetails;
import com.bms.entity.PlanDetails;
import com.bms.entity.PremiumInformation;
import com.bms.entity.Request_Information;
import com.bms.entity.TransactionInformation;
import com.bms.repository.CoverageDetailsRepository;
import com.bms.repository.EmployeeInformationRepository;
import com.bms.repository.PlanDetailsRepository;
import com.bms.repository.PremiumInformationRepository;
import com.bms.repository.RequestInformationRepository;
import com.bms.repository.TransactionInformationRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	private EmployeeInformationRepository employeeinformationrepository;

	@Autowired
	private PlanDetailsRepository plandetailsrepository;

	@Autowired
	private CoverageDetailsRepository coveragedetailsrepository;

	@Autowired
	private PremiumInformationRepository premiuminformationrepository;

	@Autowired
	private TransactionInformationRepository transactioninformationrepository;

	@Autowired
	private RequestInformationRepository requestinformationrepository;

	@Override
	public EmployeeInformation addEmployeeService(EmployeeInformation employeeinformation) {
		return employeeinformationrepository.save(employeeinformation);
	}

	@Override
	public void loginService(LoginDetails logindetails) {
		Optional<EmployeeInformation> empemail = employeeinformationrepository.findByEmail(logindetails.getEmail());
		if (empemail.isPresent()) {
			Optional<EmployeeInformation> empopt = employeeinformationrepository
					.findByEmailAndPassword(logindetails.getEmail(), logindetails.getPassword());

			if (empopt.isPresent()) {
				logindetails.setId(empopt.get().getEmpinfoid());
				logindetails.setRole(empopt.get().getRole());
				logindetails.setIsactive(empopt.get().getIsactive());
				logindetails.setResponse("Success");
			} else {
				logindetails.setResponse("Fail");
			}
		} else {
			logindetails.setResponse("UserInvalid");
		}

	}

	public List<EmployeeInformation> fetchDetailsService() {
		return employeeinformationrepository.findAll();
	}

	public PlanDetails savePlanService(PlanDetails plandetails) {
		return plandetailsrepository.save(plandetails);

	}

	public CoverageDetails saveCoverageService(CoverageDetails coveragedetails) {
		return coveragedetailsrepository.save(coveragedetails);
	}

	public PremiumInformation savePremiumService(PremiumInformation premiuminformation) {
		return premiuminformationrepository.save(premiuminformation);
	}

	public String saveTransactionService(TransactionInformation transactiondetails) {
		Optional<EmployeeInformation> empopt = employeeinformationrepository
				.findByEmail(transactiondetails.getEmployeeinformation().getEmail());
		if (empopt.isEmpty()) {
			transactioninformationrepository.save(transactiondetails);
			return "user_added";
		} else if (empopt.isPresent() && empopt.get().getIsactive().equals("false")) {
			return "user_inactive";
		}
		throw new RuntimeException("User already exists.....");
	}

	public TransactionInformation getTransactionService(long id) {
		Optional<TransactionInformation> transopt = transactioninformationrepository.findByEmpinfoidAndStatus(id,
				"current");
		if (transopt.isPresent()) {
			return transopt.get();
		}
		throw new RuntimeException("This user is not present....");
	}

	public List<PlanDetails> getPlanService() {
		return plandetailsrepository.findAll();
	}

	public List<CoverageDetails> getCoverageService() {
		return coveragedetailsrepository.findAll();
	}

	public Request_Information postCoverageRequestService(Request_Information requestinformation) {
		requestinformation.setRequestdescription("coverage_update");
		Optional<EmployeeInformation> empinfoopt = employeeinformationrepository
				.findByEmail(requestinformation.getEmail());
		requestinformation.setEmployeeinformation(empinfoopt.get());
		Optional<CoverageDetails> covopt = coveragedetailsrepository.findById(requestinformation.getRequestdetails());
		requestinformation.setRequestdetailsdescription(covopt.get().getCoveragelevel());
		return requestinformationrepository.save(requestinformation);
	}

	public Request_Information postPlanRequestService(Request_Information requestinformation) {
		requestinformation.setRequestdescription("plan_update");
		Optional<EmployeeInformation> empinfoopt = employeeinformationrepository
				.findByEmail(requestinformation.getEmail());
		requestinformation.setEmployeeinformation(empinfoopt.get());
		Optional<PlanDetails> planopt = plandetailsrepository.findById(requestinformation.getRequestdetails());
		requestinformation.setRequestdetailsdescription(planopt.get().getPlanname());
		return requestinformationrepository.save(requestinformation);
	}

	public Request_Information postCancelPolicyService(Request_Information requestinformation) {
		requestinformation.setRequestdescription("cancel_policy");
		Optional<EmployeeInformation> empinfoopt = employeeinformationrepository
				.findByEmail(requestinformation.getEmail());
		requestinformation.setEmployeeinformation(empinfoopt.get());
		requestinformation.setRequestdetailsdescription("cancel_policy");
		return requestinformationrepository.save(requestinformation);
	}

	@Transactional
	public List<String> postApproveButtonService(List<Long> requestlist) {
		Iterator<Long> iterator = requestlist.iterator();
		List<String> listtemp = new ArrayList<>();
		while (iterator.hasNext()) {
			long a = iterator.next();
			Optional<Request_Information> reqopt = requestinformationrepository.findById(a);
			if (reqopt.get().getRequestdescription().equalsIgnoreCase("coverage_update")) {
				Optional<TransactionInformation> transopt = transactioninformationrepository
						.findByEmpinfoidAndStatus(reqopt.get().getEmployeeinformation().getEmpinfoid(), "current");
				Optional<PremiumInformation> premiumopt = premiuminformationrepository
						.findById(transopt.get().getPremiuminformation().getPremiuminfoid());
				PremiumInformation premiuminfo = premiuminformationrepository.findByCoveragedetailsANDPlandetails(
						reqopt.get().getRequestdetails(), premiumopt.get().getPlandetails().getPlanid());
				TransactionInformation transactionInformation = new TransactionInformation();
				transopt.get().setStatus("history");
				transactioninformationrepository.save(transopt.get());
				transactionInformation.setPremiuminformation(premiuminfo);
				transactionInformation.setEmployeeinformation(reqopt.get().getEmployeeinformation());
				transactioninformationrepository.save(transactionInformation);
//				transactioninformationrepository.updatePremiuminfoid(premiuminfo.getPremiuminfoid(),
//						reqopt.get().getEmployeeinformation().getEmpinfoid());
//				requestinformationrepository.updateStatus("processed", reqopt.get().getRequestid());
				Request_Information requestinformation = reqopt.get();
				requestinformation.setRequeststatus("processed");
				listtemp.add("Request id: " + a + " of Coverage Update has been successfully updated");

			} else if (reqopt.get().getRequestdescription().equalsIgnoreCase("plan_update")) {
				Optional<TransactionInformation> transopt = transactioninformationrepository
						.findByEmpinfoidAndStatus(reqopt.get().getEmployeeinformation().getEmpinfoid(), "current");
				TransactionInformation transactioninformation = transopt.get();
				Optional<PremiumInformation> premiumopt = premiuminformationrepository
						.findById(transactioninformation.getPremiuminformation().getPremiuminfoid());
				PremiumInformation premiuminfo = premiuminformationrepository.findByCoveragedetailsANDPlandetails(
						premiumopt.get().getCoveragedetails().getCoverageid(), reqopt.get().getRequestdetails());
//				transactioninformationrepository.updatePremiuminfoid(premiuminfo.getPremiuminfoid(),
//						reqopt.get().getEmployeeinformation().getEmpinfoid());
//				requestinformationrepository.updateStatus("processed", reqopt.get().getRequestid());
				TransactionInformation transactionInformation = new TransactionInformation();
				transopt.get().setStatus("history");
				transactioninformationrepository.save(transopt.get());
				transactionInformation.setPremiuminformation(premiuminfo);
				transactionInformation.setEmployeeinformation(reqopt.get().getEmployeeinformation());
				transactioninformationrepository.save(transactionInformation);
				Request_Information requestinformation = reqopt.get();
				requestinformation.setRequeststatus("processed");
				listtemp.add("Request id: " + a + " of Plan Update has been successfully updated");

			} else if (reqopt.get().getRequestdescription().equalsIgnoreCase("cancel_policy")) {
				Optional<TransactionInformation> transopt = transactioninformationrepository
						.findByEmpinfoidAndStatus(reqopt.get().getEmployeeinformation().getEmpinfoid(), "current");
				EmployeeInformation employeeinformation = reqopt.get().getEmployeeinformation();
				employeeinformation.setIsactive("false");
				employeeinformationrepository.save(employeeinformation);
				transopt.get().setStatus("history");
				transactioninformationrepository.save(transopt.get());
				Request_Information requestinformation = reqopt.get();
				requestinformation.setRequeststatus("processed");
				listtemp.add("Request id: " + a + " of Cancel Policy has been successfully updated");

			}
		}
		return listtemp;
	}

	public List<Request_Information> getPendingRequestService() {
		return requestinformationrepository.findByRequeststatus("pending");
	}

	public Long getPendingRequestCountService() {
		return requestinformationrepository.findByRequeststatuscount();
	}

	public List<PremiumInformation> getPremiumService() {
		return premiuminformationrepository.findAll();
	}

	public ForgotPassword postForgotPasswordService(ForgotPassword forgotpassword) {
		Optional<EmployeeInformation> empopt = employeeinformationrepository.findByEmail(forgotpassword.getEmail());
		if (empopt.isPresent()) {
			if (empopt.get().getPassword().equals(forgotpassword.getOldpassword())) {
				if (forgotpassword.getNewpassword().equals(forgotpassword.getConfirmpassword())) {
					empopt.get().setPassword(forgotpassword.getNewpassword());
					employeeinformationrepository.save(empopt.get());
					forgotpassword.setResponse("successfully_updated");
				} else {
					forgotpassword.setResponse("confirm_password_invalid");

				}
			} else {
				forgotpassword.setResponse("old_password_invalid");
			}
		} else {
			forgotpassword.setResponse("email_invalid");
		}
		return forgotpassword;
	}

	public List<String> postDisapproveButtonService(List<Long> requestlist) {
		Iterator<Long> iterator = requestlist.iterator();
		List<String> listtemp = new ArrayList<>();
		while (iterator.hasNext()) {
			long a = iterator.next();
			Optional<Request_Information> reqopt = requestinformationrepository.findById(a);
			reqopt.get().setRequeststatus("denied");
			requestinformationrepository.save(reqopt.get());
			listtemp.add("Request Id: " + a + " has been successfully disapproved");
		}

		return listtemp;
	}

	public String saveInactiveTransactionService(TransactionInformation transactiondetails) {
		Optional<EmployeeInformation> empopt = employeeinformationrepository
				.findByEmail(transactiondetails.getEmployeeinformation().getEmail());
		if (empopt.isPresent()) {
			empopt.get().setIsactive("true");
			employeeinformationrepository.save(empopt.get());
			transactiondetails.setEmployeeinformation(empopt.get());
			transactioninformationrepository.save(transactiondetails);
			return "Employee Reactivated";
		}
		throw new RuntimeException("Unable to activate user.....");
	}

}
