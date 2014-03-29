package humanage.ribaltamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;
import com.saba.xml.XMLVisitorException;

public class CourseSearchCommand
	extends SabaWebCommand
{
	public CourseSearchCommand()
	{
	    addInParam("title", String.class, "Filter by title");
	    addInParam("number", String.class, "Filter by course number");
	    addInParam("year", String.class, "Filter by publication year");
	    addInParam("code", String.class, "Filter by training code");
	    addInParam("ivass", String.class, "Filter by IVASS discipline");
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		String title = (String)getArg("title");
		String number = (String)getArg("number");
		String year = (String)getArg("year");
		String code = (String)getArg("code");
		String ivass = (String)getArg("ivass");
		
		List<Course> result = doSearch(title, number, year, code, ivass);
		visitResult(visitor, result);
	}
	
	private void visitResult(IXMLVisitor visitor, List<Course> result) throws XMLVisitorException
	{
		visitor.beginVisit(null, "Result", null, null, null);
		
		for (Course c : result)
		{
			visitor.beginVisit(null, "Course", null, null, null);
			visitor.visit(null, "Id", c.Id);
			visitor.visit(null, "Title", c.Title);
			visitor.visit(null, "CourseNumber", c.CourseNumber);
			visitor.visit(null, "Duration", c.Duration);
			visitor.visit(null, "PublicationYear", c.PublicationYear);
			visitor.visit(null, "IvassDisciplines", c.IvassDisciplines);
			visitor.visit(null, "TrainingCodes", c.TrainingCodes);
			visitor.endVisit(null, "Course");
		}
		
		visitor.endVisit(null, "Result");
	}
	
	private List<Course> doSearch(String title, String number, String year, String code, String ivass)
	{
		List<Course> result = new ArrayList<Course>();
		
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
			
			ps = prepareStatement(connection, locator.getSabaPrincipal().getLocale(), title, number, year, code, ivass);
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				Course item = new Course();
				item.Id = rs.getString(1);
				item.Title = rs.getString(2);
				item.CourseNumber = rs.getString(3);
				item.Duration = rs.getString(4);
				item.PublicationYear = rs.getString(5);
				item.IvassDisciplines = trimValue(rs.getString(6));
				item.TrainingCodes = trimValue(rs.getString(7));
				
				result.add(item);
			}
		}
		catch (Exception ex)
		{
			System.out.println("CourseSearchCommand doSearch Exception = " + ex.getMessage());
			Debug.trace("CourseSearchCommand doSearch Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("CourseSearchCommand doSearch Exception finally = " + ex.getMessage());
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
	
	private PreparedStatement prepareStatement(Connection connection, String localeId,
			String title, String number, String year, String code, String ivass) throws SQLException
	{
		StringBuilder sql = new StringBuilder(CourseSearchCommand.kSqlSelect);
		TreeMap<String, Integer> params = new TreeMap<String, Integer>();
		int index = 2;  // first parameter position
		
		if (title != null && title.length() > 0)
		{
			sql.append("and Title like '%' + ? + '%' ");
			params.put("Title", index++);
		}
		if (number != null && number.length() > 0)
		{
			sql.append("and CourseNumber = ? ");
			params.put("CourseNumber", index++);
		}
		if (year != null && year.length() > 0)
		{
			sql.append("and PublicationYear = ? ");
			params.put("PublicationYear", index++);
		}
		if (code != null && code.length() > 0)
		{
			sql.append("and TrainingCodeIds like '%' + ? + '%' ");
			params.put("TrainingCodeIds", index++);
		}
		if (ivass != null && ivass.length() > 0)
		{
			sql.append("and IvassDisciplineIds like '%' + ? + '%' ");
			params.put("IvassDisciplineIds", index++);
		}
		
		sql.append("order by PublicationYear desc, AvailFrom desc, Title asc");
		//System.out.println("*** " + sql.toString());
		
		PreparedStatement ps = connection.prepareStatement(sql.toString());
		ps.setString(1, localeId);
		if (params.containsKey("Title")) ps.setString(params.get("Title"), title);
		if (params.containsKey("CourseNumber")) ps.setString(params.get("CourseNumber"), number);
		if (params.containsKey("PublicationYear")) ps.setString(params.get("PublicationYear"), year);
		if (params.containsKey("TrainingCodeIds")) ps.setString(params.get("TrainingCodeIds"), code);
		if (params.containsKey("IvassDisciplineIds")) ps.setString(params.get("IvassDisciplineIds"), ivass);
		
		return ps;
	}
	
	private static final String kSqlSelect =
		"select Id, Title, CourseNumber, Duration, PublicationYear, IvassDisciplines, TrainingCodes " +
		"from UGF_FONSAI_COURSES where LocaleId = ? ";
}
