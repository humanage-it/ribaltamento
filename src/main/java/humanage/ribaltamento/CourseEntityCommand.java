package humanage.ribaltamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.saba.exception.SabaException;
import com.saba.learningoffering.delivery.DeliveryMode;
import com.saba.learningoffering.delivery.DeliveryModeDetail;
import com.saba.learningoffering.delivery.DeliveryModeEntity;
import com.saba.learningoffering.delivery.DeliveryModeHome;
import com.saba.locator.Delegates;
import com.saba.locator.ServiceLocator;
import com.saba.offering.OfferingTemplate;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;
import com.saba.xml.XMLVisitorException;

public class CourseEntityCommand
	extends SabaWebCommand
{
	public CourseEntityCommand()
	{
	    addInParam("id", String.class, "Course Id");
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		String id = (String)getArg("id");
		
		Course result = doSearch(id);
		visitResult(visitor, result);
	}
	
	public String CourseTitle;
	
	public static Collection<DeliveryModeDetail> getDeliveryModes(String courseId)
		throws SabaException
	{
		ServiceLocator locator = ServiceLocator.getClientInstance();
		OfferingTemplate template = (OfferingTemplate)ServiceLocator.getReference(courseId);
		DeliveryModeHome home = (DeliveryModeHome)locator.getHome(Delegates.kDeliveryMode);

		Iterator deliveries = home.getDeliveryModesForOfferingTemplate(template).iterator();
		List<DeliveryModeDetail> result = new ArrayList<DeliveryModeDetail>();
		while (deliveries.hasNext())
		{
			DeliveryMode handle = (DeliveryMode)deliveries.next();
			DeliveryModeEntity entity = handle.getEntity(locator);
			DeliveryModeDetail detail = entity.getDetail();
			detail.setId(handle.getId());
			result.add(detail);
		}
		
		return Collections.unmodifiableList(result);
	}
	
	public static List<String> getTrainingCodes(String courseId)
	{
		List<String> codes = new ArrayList<String>();

		CourseEntityCommand cmd = new CourseEntityCommand();
		Course course = cmd.doSearch(courseId);
		
		if (course.TrainingCodes == null) return codes;
				
		StringTokenizer tokens = new StringTokenizer(course.TrainingCodes, "-");
		
		while (tokens.hasMoreTokens())
		{
			String token = tokens.nextToken();
			codes.add(token.trim());
		}
		
		return codes;
	}
	
	public static String getExCustomString(String courseId, String excustomName)
	{
		final String kExCustomSelect =
			"select c.str_value from FGT_DD_CUSTOM c " +
			"where c.attr_id = ( " +
			"select a.id from FGT_PERSIST_ATTR a " +
			"inner join FGT_PERSIST_OBJECT o on o.id = a.cid " +
			"where o.name = 'Offering Template' and a.name = ?) " +
			"and c.owner_id = ?";
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String value = null;
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(kExCustomSelect);
			ps.setString(1, excustomName);
			ps.setString(2, courseId);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				value = rs.getString(1);
			}
		}
		catch (Exception ex)
		{
			System.out.println("CourseEntityCommand getExCustomString Exception = " + ex.getMessage());
			Debug.trace("CourseEntityCommand getExCustomString Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("CourseEntityCommand getExCustomString Exception finally = " + ex.getMessage());
			}
		}
		
		return value;
	}
	
	private void visitResult(IXMLVisitor visitor, Course course) throws XMLVisitorException
	{
		visitor.beginVisit(null, "Course", null, null, null);
		
		visitor.visit(null, "Id", course.Id);
		visitor.visit(null, "Title", course.Title);
		visitor.visit(null, "CourseNumber", course.CourseNumber);
		visitor.visit(null, "Abstract", course.Abstract);
		visitor.visit(null, "Description", course.Description);
		visitor.visit(null, "Duration", course.Duration);
		visitor.visit(null, "PublicationYear", course.PublicationYear);
		visitor.visit(null, "IvassDisciplines", course.IvassDisciplines);
		visitor.visit(null, "TrainingCodes", course.TrainingCodes);
		
		visitor.beginVisit(null, "Attachments", null, null, null);
		for (Attachment a : course.Attachments)
		{
			visitor.beginVisit(null, "Attachment", null, null, null);
			visitor.visit(null, "Id", a.Id);
			visitor.visit(null, "Name", a.Name);
			visitor.visit(null, "Description", a.Description);
			visitor.visit(null, "Target", a.IsPrivate ? "Docente" : "Tutti");
			visitor.endVisit(null, "Attachment");
		}
		visitor.endVisit(null, "Attachments");

		visitor.endVisit(null, "Course");
		
		// set public properties
		this.CourseTitle = course.Title;
	}
	
	private Course doSearch(String courseId)
	{
		Course result = new Course();
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			String localeId = locator.getSabaPrincipal().getLocale();
			ps = prepareCourseStatement(connection, courseId, localeId);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				result.Id = rs.getString(1);
				result.Title = rs.getString(2);
				result.CourseNumber = rs.getString(3);
				result.Abstract = rs.getString(4);
				result.Description = rs.getString(5);
				result.Duration = rs.getString(6);
				result.PublicationYear = rs.getString(7);
				result.IvassDisciplines = trimValue(rs.getString(8));
				result.TrainingCodes = trimValue(rs.getString(9));
				
				setAttachments(locator, connection, result);
			}
		}
		catch (Exception ex)
		{
			System.out.println("CourseEntityCommand doSearch Exception = " + ex.getMessage());
			Debug.trace("CourseEntityCommand doSearch Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("CourseEntityCommand doSearch Exception finally = " + ex.getMessage());
			}
		}
		
		return result;
	}
	
	private String trimValue(String val)
	{
		if (val == null || val.length() < 3) return val;
		
		if (val.startsWith(" - ")) return val.substring(3);
		
		return val;
	}
	
	private void setAttachments(ServiceLocator locator, Connection connection, Course course)
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			ps = prepareAttachmentStatement(connection, course.Id);
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				Attachment att = new Attachment();
				
				att.Id = rs.getString(1);
				att.Name = rs.getString(2);
				att.Description = rs.getString(3);
				att.IsPrivate = rs.getBoolean(4);
				
				course.Attachments.add(att);
			}
		}
		catch (Exception ex)
		{
			System.out.println("CourseEntityCommand getAttachments Exception = " + ex.getMessage());
			Debug.trace("CourseEntityCommand getAttachments Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
			}
			catch (Exception ex) {
				Debug.trace("CourseEntityCommand getAttachments Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private PreparedStatement prepareCourseStatement(Connection connection, String courseId, String localeId) throws SQLException
	{
		PreparedStatement ps = connection.prepareStatement(CourseEntityCommand.kCourseSelect);
		ps.setString(1, courseId);
		ps.setString(2, localeId);
		
		return ps;
	}
	
	private PreparedStatement prepareAttachmentStatement(Connection connection, String courseId) throws SQLException
	{
		PreparedStatement ps = connection.prepareStatement(CourseEntityCommand.kAttachmentSelect);
		ps.setString(1, courseId);
		
		return ps;
	}

	private static final String kCourseSelect =
		"select Id, Title, CourseNumber, Abstract, Description, Duration, PublicationYear, IvassDisciplines, TrainingCodes " +
		"from UGF_FONSAI_COURSES where Id = ? and LocaleId = ?";

	private static final String kAttachmentSelect =
		"select Id, Name, Description, IsPrivate " +
		"from UGF_ATTACHMENTS where OwnerId = ?";
}
