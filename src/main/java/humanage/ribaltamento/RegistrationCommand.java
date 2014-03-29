package humanage.ribaltamento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import com.saba.businessrules.BusinessRuleManager;
import com.saba.businessrules.IBusinessRuleDataType;
import com.saba.businessrules.IntegerPolicyType;
import com.saba.client.content.GetContentDetailCommand;
import com.saba.client.order.OrderProcess;
import com.saba.content.registrationmodule.RegistrationModule;
import com.saba.content.registrationmodule.RegistrationModuleDetail;
import com.saba.content.registrationmodule.RegistrationModuleManager;
import com.saba.currency.Money;
import com.saba.currency.SabaCurrency;
import com.saba.domain.Domain;
import com.saba.exception.SabaException;
import com.saba.learning.order.LearningOrderManager;
import com.saba.learning.order.LearningOrderManagerHome;
import com.saba.learning.order.RegistrationItemDetail;
import com.saba.learning.pricing.PricingManager;
import com.saba.learning.registration.RegistrationDetail;
import com.saba.learning.registration.RegistrationManager;
import com.saba.offering.offeringaction.CompletionStatus;
import com.saba.order.Order;
import com.saba.order.OrderErrorHandler;
import com.saba.order.OrderItemDetail;
import com.saba.order.OrderResult;
import com.saba.learningoffering.ILTOffering;
import com.saba.learningoffering.ILTOfferingDetail;
import com.saba.learningoffering.ILTOfferingEntity;
import com.saba.learningoffering.OfferingDetail;
import com.saba.learningoffering.OfferingManager;
import com.saba.learningoffering.entity.session.ILTOfferingReference;
import com.saba.locator.Delegates;
import com.saba.locator.LocatorContextNotSetException;
import com.saba.locator.ServiceLocator;
import com.saba.order.LearningOrderDetail;
import com.saba.party.Party;
import com.saba.party.PartyManager;
import com.saba.party.Person;
import com.saba.party.organization.BusinessUnit;
import com.saba.party.person.Employee;
import com.saba.party.person.EmployeeDetail;
import com.saba.persist.ConnectionManager;
import com.saba.reference.GenericReference;
import com.saba.util.Debug;
import com.saba.util.LocaleUtil;
import com.saba.util.NotificationPost;
import com.saba.web.dk.SabaWebCommand;

public class RegistrationCommand
	extends SabaWebCommand
{
	public RegistrationCommand()
	{
		
	}
	
	public String confirmRegistration(String studentId, String offeringId)
	{
		String resultMessage = "";
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
		
			Person learner = (Person)ServiceLocator.getReference(studentId);
			ILTOfferingReference offering = (ILTOfferingReference)ServiceLocator.getReference(offeringId);
			
			PartyManager pmgr = (PartyManager)locator.getManager(Delegates.kPartyManager);
			EmployeeDetail ed = (EmployeeDetail)pmgr.getDetail(learner);
			Party bTo = ed.getCompany();
			Domain domain = ed.getHomeDomain();
			
			BusinessUnit bu = ed.getCompany();
			SabaCurrency curr = pmgr.getDetail(bu).getCurrency();
			
			Party bCut = (Party)learner;
			BigDecimal price = getOfferingItemPrice(offering, bTo, learner, bCut, curr);
			
			LearningOrderDetail lod = new LearningOrderDetail(
				null, // client
				bTo,  // billTo
				null,  // contact
				curr,  // currency
				domain, // split
				bCut, // baseCustomer
				null); // soldBy
			
			boolean isLearnerPreReqCheckOff = checkLearnerPreReqBR(learner, lod.getSplit());
			OrderItemDetail itemDet = new RegistrationItemDetail(
				"",
				price,
				offering,
				"",
				(Party)learner
			);
			
			((RegistrationItemDetail) itemDet).setPrerequisiteWaived(isLearnerPreReqCheckOff);
			
			LearningOrderManagerHome home = (LearningOrderManagerHome)locator.getHome(Delegates.kLearningOrderManager);
			LearningOrderManager mgr = home.create(lod);
			
			OrderResult result = mgr.addToOrder(itemDet);
			resultMessage = buildMessage(result, locator);
			
			if (resultMessage.length() > 0) return resultMessage;

			mgr.saveOrder();

			resultMessage = "Iscrizione effettuata con successo.";
		}
		catch (Exception ex)
		{
			resultMessage = ex.getLocalizedMessage();
		}
		
		return resultMessage;
	}
	
	private String buildMessage(OrderResult result, ServiceLocator locator) throws Exception
 	{
		StringBuffer buffer = new StringBuffer();
		
		List warnings = result.getWarnings();
		if (warnings != null && warnings.size() >= 1)
		{
			Iterator warningsIter = warnings.iterator();
			while (warningsIter.hasNext())
			{
				OrderErrorHandler err = (OrderErrorHandler)warningsIter.next();
				String errMesg = err.getException().getMessage(LocaleUtil.getLocale(locator));
				buffer.append(errMesg);
				buffer.append(" ");
			}
		}
		
		List errors = result.getErrors();
		if (errors != null && errors.size() >= 1)
		{
			Iterator errorsIter = errors.iterator();
			while (errorsIter.hasNext())
			{
				OrderErrorHandler err = (OrderErrorHandler)errorsIter.next();
				String errMesg = err.getException().getMessage(LocaleUtil.getLocale(locator));
				buffer.append(errMesg);
				buffer.append(" ");
			}
		}
		
		return buffer.toString();
 	}
	
	private boolean checkLearnerPreReqBR(Person learner, Domain domain) throws SabaException
	{
		final int kIntLearnerReqdPreSetttings = 40030;
		
		int polVal = getBusinessRuleValueInt(kIntLearnerReqdPreSetttings, domain);			
		
		if(polVal == 0){
			return true;
		}
		return false;
	}
	
	private int getBusinessRuleValueInt(int policynumber, Domain domain) throws SabaException
	{
		ServiceLocator locator = ServiceLocator.getClientInstance();
		
		BusinessRuleManager brMgr = (BusinessRuleManager)locator.getManager(Delegates.kBusinessRuleManager);
		IBusinessRuleDataType intPolType = brMgr.getValue(brMgr.findBusRule(policynumber), domain);
		Integer polVal = (Integer)intPolType.getValue(IntegerPolicyType.kIntegerSubType, locator);
		
		return polVal.intValue();
	}
	
	private BigDecimal getOfferingItemPrice(ILTOfferingReference offering, Party billto, Party learner, Party basecustomer, SabaCurrency currency)
		throws Exception
	{
		Money price = null;

		if (learner == null)
		{
			learner = basecustomer;
		}

		if (billto == null)	//bill to learner {person browsing}
		{
			billto = learner;
		}

		PricingManager priceMgr = (PricingManager)getServiceLocator().getManager(Delegates.kPricingManager);

		if (billto != null)
		{
			price = (Money)priceMgr.getEffectivePrice(
				currency,
				offering,
				billto,
				basecustomer,
				learner);
		}
		else
		{
			price = (Money)priceMgr.getEffectivePrice(currency, offering);
		}

		if (price == null)
		{
			return null;
		}

		return price.getAmount();	  
	}
			
	public String cancelRegistration(String registrationId) throws LocatorContextNotSetException, SabaException
	{
		return cancelRegistration(registrationId, null);
	}
	
	public String cancelRegistration(String registrationId, String offeringId) throws LocatorContextNotSetException, SabaException
	{
		String message = "Iscrizione annullata.";
		
		ServiceLocator locator = ServiceLocator.getClientInstance();
		
		// Agent can cancel a registration until the day before start date
		if (offeringId != null)
		{
			boolean canCancel = checkCanCancel(offeringId, locator);
			if (!canCancel)
			{
				return "Non è possibile annullare questa iscrizione durante e dopo la data di inizio dell'edizione.";
			}
		}
		
		String orderId = getOrderId(registrationId, locator);
		
		Employee agent = (Employee)ServiceLocator.getReference(Employee.class, locator.getSabaPrincipal().getID());
		PartyManager partyManager = (PartyManager)locator.getManager(Delegates.kPartyManager);
		EmployeeDetail studentDetail = (EmployeeDetail)partyManager.getDetail(agent);
		
		OrderProcess process = new OrderProcess();
		process.cancelOrder(orderId, "Annullato da " + studentDetail.getNameInfo().getFullName(locator), false);

		return message;
	}
	
	private boolean checkCanCancel(String offeringId, ServiceLocator locator) throws SabaException
	{
		OfferingManager offeringManager = (OfferingManager)locator.getManager(Delegates.kOfferingManager);
		ILTOffering iltOffering = (ILTOffering)ServiceLocator.getReference(ILTOffering.class, offeringId);
		ILTOfferingDetail offeringDetail = offeringManager.getDetail(iltOffering);
		
		Date startDate = offeringDetail.getStartDate();
		
		Calendar cStartDate = Calendar.getInstance();
		cStartDate.setTime(startDate);
		cStartDate.set(Calendar.HOUR_OF_DAY, 0);
		cStartDate.set(Calendar.MINUTE, 0);
		cStartDate.set(Calendar.SECOND, 0);
		cStartDate.set(Calendar.MILLISECOND, 0);
		
		Calendar cNow = Calendar.getInstance();
		cNow.set(Calendar.HOUR_OF_DAY, 0);
		cNow.set(Calendar.MINUTE, 0);
		cNow.set(Calendar.SECOND, 0);
		cNow.set(Calendar.MILLISECOND, 0);
		
		// Can cancel until the day before Start Date
		return cNow.compareTo(cStartDate) <	 0;
	}
	
	public void markPresence(java.util.Hashtable<String, String> presence)
	{
		Debug.trace("[UNIPOL] Entering markPresence");
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		
		try
		{
			ServiceLocator locator = super.getServiceLocator();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			Enumeration<String> keys = presence.keys();
			Debug.trace("[UNIPOL] keys: " + keys.hasMoreElements());
			
			while (keys.hasMoreElements())
			{
				String regId = keys.nextElement();
				Debug.trace("[UNIPOL] regId: " + regId + " = " + presence.get(regId));
				
				RegistrationManager regManager = (RegistrationManager)locator.getManager(Delegates.kRegistrationManager);
				com.saba.learning.registration.Registration regEntity = (com.saba.learning.registration.Registration)ServiceLocator.getReference(regId);
				RegistrationDetail regDetail = regManager.getDetail(regEntity);
				OfferingDetail offeringDetail = regDetail.getOffering().getOfferingEntity(locator).getOfferingDetail();

				boolean isPresent = presence.get(regId).equals("Y");
				Debug.trace("[UNIPOL] isPresent: " + isPresent);
				
				String presenceLabel = isPresent ? "Presente" : "Assente";
				String duration = isPresent ? String.valueOf((int)offeringDetail.getDuration()) : "0";

				PreparedStatement ps = connection.prepareStatement(RegistrationCommand.kMarkPresence);
				ps.setString(1, presenceLabel);
				ps.setString(2, duration);
				ps.setString(3, regId);
				ps.executeUpdate();
				
				ConnectionManager.closeStatement(ps, connection);
				
				if (isPresent)
				{
					// mark offering session complete
					Domain domain = offeringDetail.getSecurityDomain();
					
					markSessionComplete(regId, domain, locator);
				}
				else
				{
					// cancel registration with status No Show
					cancelRegistration(regId);
					setNoShow(regId);
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println("RegistrationCommand markPresence Exception = " + ex.getMessage());
			Debug.trace("RegistrationCommand markPresence Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("RegistrationCommand markPresence Exception finally = " + ex.getMessage());
			}
		}
	}
		
	private void markSessionComplete(String registrationId, Domain domain, ServiceLocator locator) throws Exception
	{
		// get current completion status for Sessions module
		com.saba.learning.registration.Registration regBean = (com.saba.learning.registration.Registration)ServiceLocator.getReference(com.saba.learning.registration.Registration.class, registrationId);
		RegistrationModuleManager regManager = (RegistrationModuleManager)locator.getManager(Delegates.kRegistrationModuleManager);
		RegistrationModule regModule = regManager.findSessionModule(regBean);
		RegistrationModuleDetail regDetail = regManager.getDetail(regModule);
		CompletionStatus currentRegStatus = regDetail.getModuleStatus();

		// set completion status successful for the Sessions module
		if (currentRegStatus != CompletionStatus.kSUCCESSFUL)
		{
			GetContentDetailCommand offeringModulescommand = new GetContentDetailCommand(locator);
			offeringModulescommand.saveSessions(registrationId, CompletionStatus.kSUCCESSFUL);

			// trigger notification #9000000
			NotificationPost.postNotification(
				new GenericReference(registrationId, ""),
				9000000,
				locator.getSabaPrincipal().getUsername(),
				domain,
				locator);
		}
	}
	
	private void setNoShow(String regId)
	{
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		
		try
		{
			ServiceLocator locator = super.getServiceLocator();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);

			ps = connection.prepareStatement(RegistrationCommand.kSetNoShow);
			ps.setString(1, regId);
			ps.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("RegistrationCommand setNoShow Exception = " + ex.getMessage());
			Debug.trace("RegistrationCommand setNoShow Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("RegistrationCommand setNoShow Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private String getOrderId(String registrationId, ServiceLocator locator)
	{
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String orderId = null;
		
		try
		{
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(RegistrationCommand.kOrderIdSelect);
			ps.setString(1, registrationId);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				orderId = rs.getString(1);
			}
		}
		catch (Exception ex)
		{
			System.out.println("RegistrationCommand getOrderId Exception = " + ex.getMessage());
			Debug.trace("RegistrationCommand getOrderId Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("RegistrationCommand getOrderId Exception finally = " + ex.getMessage());
			}
		}
		
		return orderId;
	}
	
	private static final String kOrderIdSelect =
		"select order_id from TPT_OE_ORDER_ITEMS where reg_id = ?";
	
	private static final String kMarkPresence =
		"update TPT_REGISTRATION set custom0 = ?, custom2 = ? where id = ?";
	
	private static final String kSetNoShow =
		"update TPT_REGISTRATION set flags = '1000000000' where id = ?";
	
}
