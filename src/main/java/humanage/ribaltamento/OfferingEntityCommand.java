package humanage.ribaltamento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.saba.calendar.timeperiod.TimeElement;
import com.saba.calendar.timeperiod.TimePeriodDetail;
import com.saba.client.party.PeopleBean;
import com.saba.currency.SabaCurrency;
import com.saba.customattribute.CustomAttributeValueDetail;
import com.saba.customattribute.ICustomAttributeValueDetail;
import com.saba.domain.Domain;
import com.saba.domain.DomainHome;
import com.saba.exception.LearningMessage;
import com.saba.exception.SabaException;
import com.saba.i18n.Language;
import com.saba.i18n.SabaTimeZone;
import com.saba.learningoffering.CancellationResourceType;
import com.saba.learningoffering.EnforcedSequence;
import com.saba.learningoffering.ILTOffering;
import com.saba.learningoffering.ILTOfferingDetail;
import com.saba.learningoffering.OfferingManager;
import com.saba.learningoffering.delivery.Delivery;
import com.saba.learningoffering.delivery.DeliveryModeDetail;
import com.saba.location.Location;
import com.saba.locator.Delegates;
import com.saba.locator.LocatorContextNotSetException;
import com.saba.locator.ServiceLocator;
import com.saba.offering.OfferingTemplate;
import com.saba.party.PartyManager;
import com.saba.party.Person;
import com.saba.party.governance.Governance;
import com.saba.party.governance.GovernanceDetail;
import com.saba.party.governance.GovernanceManager;
import com.saba.party.governance.GovernanceOwnerType;
import com.saba.party.governance.GovernanceType;
import com.saba.party.person.Employee;
import com.saba.party.person.EmployeeDetail;
import com.saba.persist.ConnectionManager;
import com.saba.pricing.PriceList;
import com.saba.pricing.PriceListEntry;
import com.saba.pricing.PriceListEntryDetail;
import com.saba.pricing.PriceListManager;
import com.saba.reference.IReference;
import com.saba.resource.ResourceManager;
import com.saba.resource.purpose.Purpose;
import com.saba.resource.resourceassignment.ResourceAssignmentDetail;
import com.saba.resource.resourcerequirement.EmployeeResourceRequirementDetail;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;


public class OfferingEntityCommand
	extends SabaWebCommand
{
	public OfferingEntityCommand()
	{
		addInParam("id", String.class, "Offering Id");
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		String id = (String)getArg("id");
		
		Offering result = doSearch(id);
		visitResult(visitor, result);
	}
	
	private void visitResult(IXMLVisitor visitor, Offering offering) throws LocatorContextNotSetException, SabaException
	{
		visitor.beginVisit(null, "Offering", null, null, null);
		
		visitor.visit(null, "Id", offering.Id);
		visitor.visit(null, "Title", offering.Title);
		visitor.visit(null, "OfferingNumber", offering.OfferingNumber);
		visitor.visit(null, "Instructor", getInstructorName(offering.OwnerId));
		visitor.visit(null, "LocationId", offering.LocationId);
		visitor.visit(null, "Location", offering.Location);
		visitor.visit(null, "StartDate", offering.StartDate);
		visitor.visit(null, "StartTime", offering.StartTime);
		visitor.visit(null, "EndTime", offering.EndTime);
		visitor.visit(null, "ExpiryDate", formatDate(offering.ExpiryDate));
		visitor.visit(null, "IsValidIvass", offering.IsValidIvass ? "Sì" : "No");
		visitor.visit(null, "IsArchived", offering.IsArchived);
		
		visitor.endVisit(null, "Offering");
	}
	
	private Offering doSearch(String offeringId)
	{
		Offering result = new Offering();
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			ServiceLocator locator = super.getServiceLocator();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			String localeId = locator.getSabaPrincipal().getLocale();
			ps = connection.prepareStatement(OfferingEntityCommand.kOfferingSelect);
			ps.setString(1, offeringId);
			ps.setString(2, localeId);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				result.Id = rs.getString(1);
				result.Title = rs.getString(2);
				result.OfferingNumber = rs.getString(3);
				result.OwnerId = rs.getString(4);
				result.LocationId = rs.getString(5);
				result.Location = rs.getString(6);
				result.StartDate = rs.getDate(7);
				result.StartTime = rs.getString(8);
				result.EndDate = rs.getDate(9);
				result.EndTime = rs.getString(10);
				result.ExpiryDate = parseDate(rs.getString(11));
				result.IsValidIvass = "true".equals(rs.getString(12));
				result.IsArchived = "true".equals(rs.getString(13));
			}
		}
		catch (Exception ex)
		{
			System.out.println("OfferingEntityCommand doSearch Exception = " + ex.getMessage());
			Debug.trace("OfferingEntityCommand doSearch Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingEntityCommand doSearch Exception finally = " + ex.getMessage());
			}
		}
		
		return result;
	}
	
	private String getInstructorName(String personId) throws LocatorContextNotSetException, SabaException
	{
		Person person = (Person)ServiceLocator.getReference(personId);
		PartyManager personManager = (PartyManager)ServiceLocator.getClientInstance().getManager(Delegates.kPartyManager);
		EmployeeDetail personDetail = (EmployeeDetail)personManager.getDetail(person);
		
		return personDetail.getNameInfo().getFullName(ServiceLocator.getClientInstance());
	}
	
	private Date parseDate(String dateStr)
	{
		if (dateStr == null) return null;
		
		try
		{
			// 2014-12-31 00:00:00.0
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return df.parse(dateStr);
		}
		catch (ParseException ex)
		{
		}
		
		return null;
	}
	
	private String formatDate(Date date)
	{
		if (date == null) return null;
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(date);
	}
	
	public String save(Offering entity) throws SabaException
	{
		String offeringId = entity.Id;
		
		if (entity.Id == null || entity.Id.length() == 0)
		{
			offeringId = create(entity);
		}
		else
		{
			offeringId = update(entity);
		}
		
		return offeringId;
	}
	
	public void cancel(String offeringId) throws LocatorContextNotSetException, SabaException
	{
		// cancel existing registrations
		RegistrationCommand command = new RegistrationCommand();
		ArrayList<String> existingRegistrations = getOfferingRegistrations(offeringId);
		for (String registrationId : existingRegistrations)
		{
			command.cancelRegistration(registrationId);
		}
		
		// remove owner so the offering is no longer visible to the agent
		removeOwner(offeringId);
		
		// set custom0 (expiry date) = cancellation date
		Calendar cancellationDate = Calendar.getInstance();
		
		OfferingManager offeringManager = (OfferingManager)ServiceLocator.getClientInstance().getManager(Delegates.kOfferingManager);
		ILTOffering iltOffering = (ILTOffering)ServiceLocator.getReference(ILTOffering.class, offeringId);
		ILTOfferingDetail offeringDetail = offeringManager.getDetail(iltOffering);
		offeringDetail.setCustomValue(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom0, new Timestamp(cancellationDate.getTimeInMillis())));
		
		offeringManager.setDetail(iltOffering, offeringDetail);
		offeringManager.markOfferingCancelled(iltOffering, "UnipolSAI");
	}
	
	public void archive(String offeringId) throws LocatorContextNotSetException, SabaException
	{
		OfferingManager offeringManager = (OfferingManager)ServiceLocator.getClientInstance().getManager(Delegates.kOfferingManager);
		ILTOffering iltOffering = (ILTOffering)ServiceLocator.getReference(ILTOffering.class, offeringId);
		ILTOfferingDetail offeringDetail = offeringManager.getDetail(iltOffering);
		offeringDetail.setCustomValue(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom6, "SI"));
		
		offeringManager.setDetail(iltOffering, offeringDetail);
	}
	
	private String create(Offering entity) throws SabaException
	{
		ServiceLocator locator = ServiceLocator.getClientInstance();
		
		if (checkConflict(entity))
		{
			return "{NEW-CONFLICT}";
		}

		OfferingTemplate offeringTemplate = (OfferingTemplate)ServiceLocator.getReference(OfferingTemplate.class, entity.CourseId);
		Location location = (Location)ServiceLocator.getReference(Location.class, entity.LocationId);
		Employee owner = (Employee)ServiceLocator.getReference(Employee.class, entity.OwnerId);
		Language italian = (Language)ServiceLocator.getReference(Language.class, OfferingEntityCommand.kItalianId);
		Delivery delivery = (Delivery)ServiceLocator.getReference(Delivery.class, OfferingEntityCommand.kDeliveryId);

		DomainHome domainHome = (DomainHome)locator.getHome(Delegates.kDomain);
		Collection domains = domainHome.findByName("Reti");
		Domain reti = (Domain)domains.iterator().next();
		
		DeliveryModeDetail deliveryModeDetail = getDeliveryMode(entity.CourseId, OfferingEntityCommand.kDeliveryId);
		
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(entity.StartDate);
		
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(startDate.getTime());
		
		Calendar now = Calendar.getInstance();
		
		ILTOfferingDetail detail = new ILTOfferingDetail(
			getOfferingNumber(now.getTime()), // classNo 
			offeringTemplate,
			location,
			startDate.getTime(),
			endDate.getTime(),
			0, // maxIntConf
			deliveryModeDetail.getMaxEnroll(), // maxCt
			deliveryModeDetail.getWaitlistMax(), // maxBook
			deliveryModeDetail.getMinEnroll(), // minCt
			0, // studCt
			0, // studBook
			100, // status
			null, // crs
			reti, // split
			getSessionTemplateName(startDate.getTime()), // sessionName
			null, // whenCancel
			italian,
			false, // broadcast
			false, // dispForWeb
			true, // dispForCallCenter
			1, // trainingUnits // Value of the attribute 'Training Units' cannot be less than 1 
			null, // facility
			false, // rescheduled
			null, // vendor
			null, // abstract
			null, // openEnroll
			null, // openEnrollForAll
			null, // enrollClose
			delivery,
			null, // parentClass
			false, // isTest
			null, // priceBand
			null, // priceBandUnit
			null, // postOrder
			null, // postCompletion
			null, // stopAutoPromotion
			deliveryModeDetail.getDuration(),
			false, // isPrivate
			false, // isClassBased
			false, // isClassForOtherOrg
			null // organization_id
		);
		
		detail.setDoNotDropPost(now.getTime());
		detail.setEnforcedSequence(EnforcedSequence.ENFORCEDCOMPLETION);

		// define the time periods for the session
		SabaTimeZone timezone = (SabaTimeZone)ServiceLocator.getReference(SabaTimeZone.class, OfferingEntityCommand.kTimeZoneId);
		TimePeriodDetail timePeriodDetail = new TimePeriodDetail(timezone);
		timePeriodDetail.addElement(startDate.getTime(), endDate.getTime(), entity.StartTime, entity.EndTime);
		
		Calendar endOfYear = Calendar.getInstance();
		endOfYear.set(Calendar.MONTH, 11);
		endOfYear.set(Calendar.DAY_OF_MONTH, 31);
		endOfYear.set(Calendar.HOUR_OF_DAY, 0);
		endOfYear.set(Calendar.MINUTE, 0);
		endOfYear.set(Calendar.SECOND, 0);
		endOfYear.set(Calendar.MILLISECOND, 0);
		
		Collection<CustomAttributeValueDetail> customFields = new Vector<CustomAttributeValueDetail>();
		customFields.add(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom0, new Timestamp(endOfYear.getTimeInMillis())));
		customFields.add(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom1, "false"));
		customFields.add(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom2, "true"));
		customFields.add(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom4, "false"));
		customFields.add(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom5, "Agente"));
		customFields.add(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom8, CourseEntityCommand.getExCustomString(entity.CourseId, "ExCustom1")));
		customFields.add(new CustomAttributeValueDetail(ICustomAttributeValueDetail.kCustom10, "100"));
		detail.setCustomValues(customFields);

		// create the offering
		OfferingManager offeringManager = (OfferingManager)locator.getManager(Delegates.kOfferingManager);
		ILTOffering iltOffering = offeringManager.createOffering(detail, timePeriodDetail);
		
		nullTrainingUnits(iltOffering.getId());
		
		// add Owner
		GovernanceDetail governanceDetail = new GovernanceDetail(iltOffering, owner, GovernanceOwnerType.kPerson, GovernanceType.kOwner);
		GovernanceManager governanceManager = (GovernanceManager)locator.getManager(Delegates.kGovernanceManager);
		governanceManager.create(governanceDetail);
		
		// add Instructor as resource (if not already) with a rate
		SabaCurrency currency = (SabaCurrency)ServiceLocator.getReference(SabaCurrency.class, OfferingEntityCommand.kEuroId);

		PeopleBean peopleBean = new PeopleBean();
		boolean isResource = peopleBean.isResource(owner);
		if (!isResource)
		{
			addRate(entity.OwnerId, 0, currency);
			peopleBean.markPersonAsResource(owner);
		}
		
		// assign Instructor
		Purpose purpose = (Purpose)ServiceLocator.getReference(Purpose.class, OfferingEntityCommand.kInstructorId);
		
		EmployeeResourceRequirementDetail requirementDetail = new EmployeeResourceRequirementDetail(
			"Instructor",
			location,
			false, // qualified
			false, // available
			1, // priority
			"", // resName
			new BigDecimal(0.0F), // rate
			currency);

		ResourceAssignmentDetail assignmentDetail = new ResourceAssignmentDetail(
			purpose, // Purpose purpose
			iltOffering, // Project project
			1, // int resQty
			requirementDetail, // IResourceRequirementDetail resReq
			null, // IAllocationInfoDetail allocnInfo
			timePeriodDetail, // TimePeriodDetail timePeriod
			owner // Resource resource
		);
		
		ResourceManager resourceManager = (ResourceManager)locator.getManager(Delegates.kResourceManager);
		resourceManager.createResourceAssignment(assignmentDetail);
		
		return iltOffering.getId();
	}
	
	private boolean checkConflict(Offering entity)
	{
		boolean conflict = false;
		
		int h = Integer.valueOf(entity.StartTime.substring(0, 2), 10);
		int m = Integer.valueOf(entity.StartTime.substring(3), 10);
		
		Calendar offeringStartDate = Calendar.getInstance();
		offeringStartDate.setTime(entity.StartDate); // contains only date
		offeringStartDate.set(Calendar.HOUR_OF_DAY, h);
		offeringStartDate.set(Calendar.MINUTE, m);
		offeringStartDate.set(Calendar.SECOND, 0);
		offeringStartDate.set(Calendar.MILLISECOND, 0);
		
		ArrayList<InstructorAssignment> assignments = getInstructorAssignmentDates(entity.OwnerId, entity.Id);
		for (InstructorAssignment assignment : assignments)
		{
			conflict = (offeringStartDate.after(assignment.getStartDate()) && offeringStartDate.before(assignment.getEndDate()));
			if (conflict) break;
		}
		
		return conflict;
	}
	
	private class InstructorAssignment
	{
		public InstructorAssignment(java.sql.Date startDate, java.sql.Date endDate)
		{
			_startDate = Calendar.getInstance();
			_startDate.setTimeInMillis(startDate.getTime());

			_endDate = Calendar.getInstance();
			_endDate.setTimeInMillis(endDate.getTime());
		}
		
		public Calendar getStartDate()
		{
			return _startDate;
		}
		
		public Calendar getEndDate()
		{
			return _endDate;
		}
		
		private Calendar _startDate;
		private Calendar _endDate;
	}
	
	private ArrayList<InstructorAssignment> getInstructorAssignmentDates(String ownerId, String offeringId)
	{
		ArrayList<InstructorAssignment> assignments = new ArrayList<InstructorAssignment>();
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			ServiceLocator locator = super.getServiceLocator();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			String sql = offeringId == null ? OfferingEntityCommand.kInstructorAssignmentDatesSelectNew : OfferingEntityCommand.kInstructorAssignmentDatesSelectUpd; 
			ps = connection.prepareStatement(sql);
			ps.setString(1, ownerId);
			if (offeringId != null) ps.setString(2, offeringId);
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				assignments.add(new InstructorAssignment(rs.getDate(1), rs.getDate(2)));
			}
		}
		catch (Exception ex)
		{
			System.out.println("OfferingEntityCommand getInstructorAssignmentDates Exception = " + ex.getMessage());
			Debug.trace("OfferingEntityCommand getInstructorAssignmentDates Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingEntityCommand getInstructorAssignmentDates Exception finally = " + ex.getMessage());
			}
		}
		
		return assignments;
	}

	private void nullTrainingUnits(String offeringId)
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

			ps = connection.prepareStatement("update LET_EXT_OFFERING_SESSION set training_units = null where id = ?");
			ps.setString(1, offeringId);
			ps.executeUpdate();

			// TPT_DUMMY_OFFERING automatically updated by a trigger on LET_EXT_OFFERING_SESSION
			//ps = connection.prepareStatement("update TPT_DUMMY_OFFERING set train_unit = null where id = ?");
			//ps.setString(1, offeringId);
			//ps.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("OfferingEntityCommand nullTrainingUnits Exception = " + ex.getMessage());
			Debug.trace("OfferingEntityCommand nullTrainingUnits Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingEntityCommand nullTrainingUnits Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private void addRate(String resourceId, double amount, SabaCurrency currency)
		throws SabaException
	{
	    IReference part = (IReference)ServiceLocator.getReference(resourceId);
	    PriceListManager priceListManager = (PriceListManager)getServiceLocator().getManager(Delegates.kPriceListManager);
	    PriceList priceList = priceListManager.getCurrentMasterPriceList();
	
	    if (priceList == null)
	    {
	      throw new SabaException(LearningMessage.kNoActiveMasterPriceList);
	    }
	    
	    PriceListEntry priceListEntry = priceListManager.getPriceListEntry(priceList, part, currency);
	    if (priceListEntry == null)
	    {
	    	PriceListEntryDetail entryDetail = new PriceListEntryDetail(priceList, part, currency, amount);
	    	entryDetail.setPriceType(1);
	
	    	priceListEntry = priceListManager.createPriceListEntry(entryDetail);
	    }
	}
	
	private String update(Offering entity) throws SabaException
	{
		ServiceLocator locator = ServiceLocator.getClientInstance();
		
		if (checkConflict(entity))
		{
			return "{UPD-CONFLICT}";
		}
		
		// update the offering
		OfferingManager offeringManager = (OfferingManager)locator.getManager(Delegates.kOfferingManager);
		ILTOffering iltOffering = (ILTOffering)ServiceLocator.getReference(ILTOffering.class, entity.Id);
		ILTOfferingDetail offeringDetail = offeringManager.getDetail(iltOffering);
	
		Location location = (Location)ServiceLocator.getReference(Location.class, entity.LocationId);
		if (!entity.LocationId.equals(offeringDetail.getLocation().getId()))
		{
			offeringManager.updateLocation(iltOffering, location, CancellationResourceType.kRetainResourcesFailConflict, "");
		}
		
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(entity.StartDate);
		
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(startDate.getTime());
		
		Calendar offeringStartDate = Calendar.getInstance();
		offeringStartDate.setTime(offeringDetail.getStartDate());
		
		// Different day of week: update session template name 
		if (startDate.get(Calendar.DAY_OF_WEEK) != offeringStartDate.get(Calendar.DAY_OF_WEEK))
		{
			String sessionName = getSessionTemplateName(startDate.getTime());
			offeringDetail.setSessionTemplate(sessionName);
		}
		
		TimePeriodDetail timePeriodDetail = offeringManager.getTimePeriodDetail(iltOffering);
		Collection<TimeElement> timeElements = (Collection<TimeElement>)timePeriodDetail.getTimeElements();
		String offeringStartTime = timeElements.iterator().next().getStartTime();
		
		// New start date or time: update sessions
		if (startDate.compareTo(offeringStartDate) != 0 || !entity.StartTime.equals(offeringStartTime))
		{
			// define the time periods for the session
			timePeriodDetail.removeAllElements();
			timePeriodDetail.setStartDate(startDate.getTime());
			timePeriodDetail.setEndDate(endDate.getTime());
			timePeriodDetail.addElement(startDate.getTime(), endDate.getTime(), entity.StartTime, entity.EndTime);

			offeringDetail.setStartDate(startDate.getTime());
			offeringDetail.setEndDate(endDate.getTime());
			offeringManager.setDetail(iltOffering, offeringDetail);
			
			// Update start date and session with direct DB access as API throws exception of different day of week 
			//offeringManager.setTimePeriod(iltOffering, timePeriodDetail);
			//offeringManager.updateSessions(iltOffering, timePeriodDetail, CancellationResourceType.kRetainResourcesFailConflict, "");
			setTimeElement(iltOffering.getId(), startDate, endDate, entity.StartTime, entity.EndTime);
			
			offeringDetail.setDoNotDropPost(startDate.getTime());
		}
		
		offeringManager.setDetail(iltOffering, offeringDetail);
		
		return iltOffering.getId();
	}
	
	private void setTimeElement(String offeringId, Calendar startDate, Calendar endDate, String startTime, String endTime)
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

			ps = connection.prepareStatement("update LET_EXT_OFFERING_SESSION set start_date = ? where id = ?");
			ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
			ps.setString(2, offeringId);
			ps.executeUpdate();

			ps = connection.prepareStatement("update TPT_DUMMY_OFFERING set start_date = ? where id = ?");
			ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
			ps.setString(2, offeringId);
			ps.executeUpdate();
			
			String dayMap = "1000000";
			int dayWeek = startDate.get(Calendar.DAY_OF_WEEK);
			switch (dayWeek)
			{
				case 1: dayMap = "1000000"; break;	// Sunday
				case 2: dayMap = "0100000"; break;	// Monday
				case 3: dayMap = "0010000"; break;
				case 4: dayMap = "0001000"; break;
				case 5: dayMap = "0000100"; break;
				case 6: dayMap = "0000010"; break;
				case 7: dayMap = "0000001"; break;	// Saturday
			}
			
			ps = connection.prepareStatement("update FGT_TIME_ELEMENT set start_date = ?, end_date = ?, start_time = ?, end_time = ?, day_map = ? where owner_id = ?");
			ps.setDate(1, new java.sql.Date(startDate.getTimeInMillis()));
			ps.setDate(2, new java.sql.Date(endDate.getTimeInMillis()));
			ps.setString(3, startTime);
			ps.setString(4, endTime);
			ps.setString(5, dayMap);
			ps.setString(6, offeringId);
			ps.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("OfferingEntityCommand setTimeElement Exception = " + ex.getMessage());
			Debug.trace("OfferingEntityCommand setTimeElement Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingEntityCommand setTimeElement Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private DeliveryModeDetail getDeliveryMode(String courseId, String deliveryId) throws SabaException
	{
		Collection<DeliveryModeDetail> deliveryModeDetails = CourseEntityCommand.getDeliveryModes(courseId);
		Iterator<DeliveryModeDetail> details = deliveryModeDetails.iterator();
		
		while (details.hasNext())
		{
			DeliveryModeDetail detail = details.next();
			if (detail.getDelivery().getId().equals(deliveryId))
			{
				return detail;
			}
		}
		
		return null;
	}
	
	private String getSessionTemplateName(Date date)
	{
		DateFormat df = new SimpleDateFormat("EEEE", Locale.ITALY);
		String name = df.format(date.getTime());
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		
		return name;
	}
	
	private String getOfferingNumber(Date date)
	{
		// generate Offering Number from current time in milliseconds reduced to fit into 8 hexadecimal characters
		long millis = date.getTime() - 1390000000000L;
		
		return String.format("%08X", millis);
	}
	
	private void removeOwner(String offeringId) throws SabaException
	{
		GovernanceManager governanceManager = (GovernanceManager)ServiceLocator.getClientInstance().getManager(Delegates.kGovernanceManager);
		
		ArrayList<String> owners = getOfferingOwners(offeringId);
		for (String govId : owners)
		{
			Governance owner = (Governance)ServiceLocator.getReference(Governance.class, govId);
			governanceManager.remove(owner);
		}
	}
	
	private ArrayList<String> getOfferingOwners(String offeringId)
	{
		ArrayList<String> owners = new ArrayList<String>();
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			ServiceLocator locator = super.getServiceLocator();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(OfferingEntityCommand.kGovernanceSelect);
			ps.setString(1, offeringId);
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				String govId = rs.getString(1);
				owners.add(govId);
			}
		}
		catch (Exception ex)
		{
			System.out.println("OfferingEntityCommand getOfferingOwners Exception = " + ex.getMessage());
			Debug.trace("OfferingEntityCommand getOfferingOwners Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingEntityCommand getOfferingOwners Exception finally = " + ex.getMessage());
			}
		}
		
		return owners;
	}
	
	private ArrayList<String> getOfferingRegistrations(String offeringId)
	{
		ArrayList<String> registrations = new ArrayList<String>();
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			ServiceLocator locator = super.getServiceLocator();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(OfferingEntityCommand.kRegistrationSelect);
			ps.setString(1, offeringId);
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				String regId = rs.getString(1);
				registrations.add(regId);
			}
		}
		catch (Exception ex)
		{
			System.out.println("OfferingEntityCommand getOfferingRegistrations Exception = " + ex.getMessage());
			Debug.trace("OfferingEntityCommand getOfferingRegistrations Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingEntityCommand getOfferingRegistrations Exception finally = " + ex.getMessage());
			}
		}
		
		return registrations;
	}

	private static final String kDeliveryId = "eqcat000000000000004";
	
	private static final String kItalianId = "lange000000000000008";
	
	private static final String kTimeZoneId = "tzone000000000000023";
	
	private static final String kInstructorId = "recat000000000000504";
	
	public static final String kEuroId = "crncy000000000000052";
	
	private static final String kOfferingSelect =
		"select Id, Title, OfferingNumber, OwnerId, LocationId, Location, StartDate, StartTime, EndDate, EndTime, ExpiryDate, IsValidIvass, IsArchived " +
		"from UGF_FONSAI_OFFERINGS " +
		"where Id = ? and LocaleId = ?";

	private static final String kGovernanceSelect =
		"select id from CMT_GOVERNANCE " +
		"where owner_type = 100 and type = 100 " +
		"and parent_id = ?";

	private static final String kRegistrationSelect =
		"select RegistrationId from UGF_FONSAI_REGISTRATIONS " +
		"where OfferingId = ?";
	
	private static final String kInstructorAssignmentDatesSelectNew =
		"select start_date, end_date from FGT_TIME_ELEMENT " +
		"where owner_id in (select id from FGT_ASSIGNMENTS " +
		"where resource_type = 200 and resource_id = ? " +
		"and project_id in (select id from LET_EXT_OFFERING_SESSION where status = 100))";

	private static final String kInstructorAssignmentDatesSelectUpd =
		"select start_date, end_date from FGT_TIME_ELEMENT " +
		"where owner_id in (select id from FGT_ASSIGNMENTS " +
		"where resource_type = 200 and resource_id = ? and project_id != ? " +
		"and project_id in (select id from LET_EXT_OFFERING_SESSION where status = 100))";
}
