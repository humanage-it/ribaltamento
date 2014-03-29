package humanage.ribaltamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;
import com.saba.xml.XMLVisitorException;

public class OfferingSearchCommand
	extends SabaWebCommand
{
	public OfferingSearchCommand()
	{
	    addInParam("title", String.class, "Filter by title");
	    addInParam("number", String.class, "Filter by offering number");
	    addInParam("startDateFrom", String.class, "Filter by offering start date");
	    addInParam("startDateTo", String.class, "Filter by offering start date");
	    addInParam("offeringStatus", String.class, "Filter by offering status");
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		String title = (String)getArg("title");
		String number = (String)getArg("number");
		String startDateFrom = (String)getArg("startDateFrom");
		String startDateTo = (String)getArg("startDateTo");
		String offeringStatus = (String)getArg("offeringStatus");
		
		List<Offering> result = doSearch(title, number, startDateFrom, startDateTo, offeringStatus);
		visitResult(visitor, result);
	}
	
	private void visitResult(IXMLVisitor visitor, List<Offering> result) throws XMLVisitorException
	{
		visitor.beginVisit(null, "Result", null, null, null);
		
		for (Offering o : result)
		{
			visitor.beginVisit(null, "Offering", null, null, null);
			visitor.visit(null, "Id", o.Id);
			visitor.visit(null, "CourseId", o.CourseId);
			visitor.visit(null, "Title", o.Title);
			visitor.visit(null, "OfferingNumber", o.OfferingNumber);
			visitor.visit(null, "Location", o.Location);
			visitor.visit(null, "StartDate", o.StartDate);
			visitor.visit(null, "IsArchived", o.IsArchived ? "Sì" : "No");
			visitor.endVisit(null, "Offering");
		}
		
		visitor.endVisit(null, "Result");
	}
	
	private List<Offering> doSearch(String title, String number, String startDateFrom, String startDateTo, String offeringStatus)
	{
		List<Offering> result = new ArrayList<Offering>();
		
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
			
			ps = prepareStatement(connection,
					locator.getSabaPrincipal().getID(), locator.getSabaPrincipal().getLocale(),
					title, number, startDateFrom, startDateTo, offeringStatus);
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				Offering item = new Offering();
				item.Id = rs.getString(1);
				item.Title = rs.getString(2);
				item.OfferingNumber = rs.getString(3);
				item.Location = rs.getString(4);
				item.StartDate = rs.getDate(5);
				item.IsArchived = "true".equals(rs.getString(6));
				item.CourseId = rs.getString(7);
				
				result.add(item);
			}
		}
		catch (Exception ex)
		{
			System.out.println("OfferingSearchCommand doSearch Exception = " + ex.getMessage());
			Debug.trace("OfferingSearchCommand doSearch Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingSearchCommand doSearch Exception finally = " + ex.getMessage());
			}
		}
		
		return result;
	}
	
	private PreparedStatement prepareStatement(Connection connection, String ownerId, String localeId,
			String title, String number, String startDateFrom, String startDateTo, String offeringStatus)
		throws SQLException, ParseException
	{
		StringBuilder sql = new StringBuilder(OfferingSearchCommand.kSqlSelect);
		TreeMap<String, Integer> params = new TreeMap<String, Integer>();
		int index = 3;  // first parameter position
		
		if (title != null && title.length() > 0)
		{
			sql.append("and Title like '%' + ? + '%' ");
			params.put("Title", index++);
		}
		if (number != null && number.length() > 0)
		{
			sql.append("and OfferingNumber = ? ");
			params.put("OfferingNumber", index++);
		}
		if (startDateFrom != null && startDateFrom.length() > 0)
		{
			sql.append("and StartDate >= ? ");
			params.put("StartDateFrom", index++);
		}
		if (startDateTo != null && startDateTo.length() > 0)
		{
			sql.append("and StartDate <= ? ");
			params.put("StartDateTo", index++);
		}
		if (offeringStatus != null && offeringStatus.length() > 0)
		{
			if (offeringStatus.equals("true")) {	// Archived Only
				sql.append("and IsArchived = 'true' ");
			}
			else if (offeringStatus.equals("false")) {	// Non-Archived Only
				sql.append("and (IsArchived is null or IsArchived != 'true') ");
			}
		}
		
		sql.append("order by Title asc, StartDate desc");
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		
		PreparedStatement ps = connection.prepareStatement(sql.toString());
		ps.setString(1, ownerId);
		ps.setString(2, localeId);
		if (params.containsKey("Title")) ps.setString(params.get("Title"), title);
		if (params.containsKey("OfferingNumber")) ps.setString(params.get("OfferingNumber"), number);
		if (params.containsKey("StartDateFrom")) {
			Calendar c = Calendar.getInstance();
			c.setTime(df.parse(startDateFrom));
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			
			ps.setDate(params.get("StartDateFrom"), new java.sql.Date(c.getTimeInMillis()));
		}
		if (params.containsKey("StartDateTo")) {
			Calendar c = Calendar.getInstance();
			c.setTime(df.parse(startDateTo));
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			
			ps.setDate(params.get("StartDateTo"), new java.sql.Date(c.getTimeInMillis()));
		}
		
		return ps;
	}
	
	private static final String kSqlSelect =
		"select Id, Title, OfferingNumber, Location, StartDate, IsArchived, CourseId " +
		"from UGF_FONSAI_OFFERINGS where OwnerId = ? and LocaleId = ? ";
}
