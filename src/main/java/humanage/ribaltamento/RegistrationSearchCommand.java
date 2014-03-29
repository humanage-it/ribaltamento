package humanage.ribaltamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;
import com.saba.xml.XMLVisitorException;

public class RegistrationSearchCommand
	extends SabaWebCommand
{
	public RegistrationSearchCommand()
	{
		addInParam("actionKey", String.class, "Action to execute");
		addInParam("offeringId", String.class, "Offering Id");
		addInParam("courseId", String.class, "Course Id");
		
		RegistrationCount = 0;
	}
	
	public int RegistrationCount;
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		String actionKey = (String)getArg("actionKey");
		String offeringId = (String)getArg("offeringId");
		String courseId = (String)getArg("courseId");
		
		List<Registration> result = doSearch(actionKey, offeringId, courseId);
		visitResult(visitor, result);
	}
	
	private void visitResult(IXMLVisitor visitor, List<Registration> result) throws XMLVisitorException
	{
		visitor.beginVisit(null, "Result", null, null, null);
		
		for (Registration reg : result)
		{
			visitor.beginVisit(null, "Registration", null, null, null);
			visitor.visit(null, "StudentId", reg.StudentId);
			visitor.visit(null, "FirstName", reg.FirstName);
			visitor.visit(null, "LastName", reg.LastName);
			visitor.visit(null, "FiscalCode", reg.FiscalCode);
			visitor.visit(null, "TrainingCode", reg.TrainingCode);
			visitor.visit(null, "IsConfirmed", reg.IsConfirmed);
			visitor.visit(null, "RegistrationId", reg.Id);
			visitor.visit(null, "Presence", reg.Presence);
			visitor.endVisit(null, "Registration");
		}
		
		visitor.endVisit(null, "Result");
	}
	
	private List<Registration> doSearch(String actionKey, String offeringId, String courseId)
	{
		List<Registration> result = new ArrayList<Registration>();
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sqlSelect = "";
		if (actionKey.equals(RegistrationSearchCommand.kSearchRegistrations))
		{
			sqlSelect = RegistrationSearchCommand.kRegistrationSelect;
		}
		else if (actionKey.equals(RegistrationSearchCommand.kSearchStudents))
		{
			sqlSelect = RegistrationSearchCommand.kStudentSelect + 
					addTrainingCodes(courseId) +
					" order by LastName, FirstName";
		}
		
		try
		{
			ServiceLocator locator = super.getServiceLocator();
			String siteName = locator.getSabaPrincipal().getSiteName();
			String usedId = locator.getSabaPrincipal().getID();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(sqlSelect);
			ps.setString(1, offeringId);
			if (actionKey.equals(RegistrationSearchCommand.kSearchStudents))
			{
				ps.setString(2, usedId);
			}
			
			rs = ps.executeQuery();
			
			this.RegistrationCount = 0;
			while (rs.next())
			{
				Registration item = new Registration();
				item.StudentId = rs.getString(1);
				item.FirstName = rs.getString(2);
				item.LastName = rs.getString(3);
				item.FiscalCode = rs.getString(4);
				item.TrainingCode = rs.getString(5);
				item.Id = rs.getString(6);
				item.IsConfirmed = "true".equals(rs.getString(7));
				item.Presence = rs.getString(8);
				
				result.add(item);
				this.RegistrationCount++;
			}
		}
		catch (Exception ex)
		{
			System.out.println("RegistrationSearchCommand doSearch Exception = " + ex.getMessage());
			Debug.trace("RegistrationSearchCommand doSearch Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("RegistrationSearchCommand doSearch Exception finally = " + ex.getMessage());
			}
		}
		
		return result;
	}
	
	private String addTrainingCodes(String courseId)
	{
		List<String> codes = CourseEntityCommand.getTrainingCodes(courseId);
		final int size = codes.size();

		if (size == 0) return "";
		
		StringBuffer andCondition = new StringBuffer();
		andCondition.append("and p.person_type in (");
		
		for (int i = 0; i < size; i++)
		{
			andCondition.append("'" + codes.get(i) + "'");
			if (i < size - 1) andCondition.append(",");
		}
		
		andCondition.append(")");
		
		return andCondition.toString();
	}
	
	public static final String kSearchRegistrations = "SearchRegistrations";
	public static final String kSearchStudents = "SearchStudents";
	
	private static final String kRegistrationSelect =
		"select StudentId, FirstName, LastName, FiscalCode, TrainingCode, RegistrationId, 'true' as IsConfirmed, Presence " +
		"from UGF_FONSAI_REGISTRATIONS " +
		"where OfferingId = ? " +
		"order by LastName, FirstName";

	private static final String kStudentSelect =
		"select p.id as StudentId, p.fname as FirstName, p.lname as LastName, p.custom0 as FiscalCode, " +
		"p.person_type as TrainingCode, r.RegistrationId, " +
		"case when r.StudentId is null then 'false' else 'true' end as IsConfirmed, r.Presence " +
		"from CMT_PERSON p inner join CMT_ACNTBLTY a on a.is_accountable = p.id " +
		"left outer join UGF_FONSAI_REGISTRATIONS r on r.StudentId = p.id and r.OfferingId = ? " +
		"where (p.terminated_on is null or p.terminated_on > getdate()) " +
		"and a.accountable_to = ? ";
}
