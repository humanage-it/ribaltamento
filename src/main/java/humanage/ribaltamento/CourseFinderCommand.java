package humanage.ribaltamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;

public class CourseFinderCommand
	extends SabaWebCommand
{
	public CourseFinderCommand()
	{
		
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		visitor.beginVisit(null, "Finder", null, null, null);
		
		visitValues(visitor, "Years", CourseFinderCommand.kYearsSelect);
		visitValues(visitor, "IvassDisciplines", CourseFinderCommand.kIvassDisciplinesSelect);
		visitValues(visitor, "TrainingCodes", CourseFinderCommand.kTrainingCodesSelect);
		
		visitor.endVisit(null, "Finder");
	}
	
	private void visitValues(IXMLVisitor visitor, String tagName, String sqlQuery) throws Exception
	{
		visitor.beginVisit(null, tagName, null, null, null);
		
		Map<String, String> map = getValues(sqlQuery);
		
		for (String key : map.keySet())
		{
			visitor.beginVisit(null, "Item", null, null, null);
			visitor.visit(null, "Value", key);
			visitor.visit(null, "Name", map.get(key));
			visitor.endVisit(null, "Item");
		}
		
		visitor.endVisit(null, tagName);
	}
	
	private Map<String, String> getValues(String sql)
	{
		Map<String, String> map = new LinkedHashMap<String, String>();
		
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
			
			ps = connection.prepareStatement(sql);
			ps.setString(1, locator.getSabaPrincipal().getLocale());
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				String key = rs.getString(1);
				String value = rs.getString(2);
				map.put(key, value);
			}
		}
		catch (Exception ex)
		{
			Debug.trace("CourseFinderCommand getValues Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("CourseFinderCommand getValues Exception finally = " + ex.getMessage());
			}
		}
		
		return map;
	}
	
	private static final String kYearsSelect =
		"select distinct custom10, custom10 " +
		"from let_ext_offering_template where locale_id = ? and custom10 is not null order by 1";
	
	private static final String kIvassDisciplinesSelect =
		"select s.id, s.name from TPT_EXT_NLEVEL_FOLDER s " +
		"inner join TPT_EXT_NLEVEL_FOLDER c on c.id = s.parent_id and c.locale_id = s.locale_id " +
		"where s.folder_type = 200 and s.flags like '1%' and s.locale_id = ? " +
		"and c.name = 'Disciplina ISVAP' order by 2";
	
	private static final String kTrainingCodesSelect =
		"select s.id, s.name from TPT_EXT_NLEVEL_FOLDER s " +
		"inner join TPT_EXT_NLEVEL_FOLDER c on c.id = s.parent_id and c.locale_id = s.locale_id " +
		"where s.folder_type = 200 and s.flags like '1%' and s.locale_id = ? " +
		"and c.name = 'Codice Formativo' order by 2";
}
