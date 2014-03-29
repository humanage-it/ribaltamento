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

public class LocationFinderCommand
extends SabaWebCommand
{
	public LocationFinderCommand()
	{
		
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		visitor.beginVisit(null, "Finder", null, null, null);
		
		visitValues(visitor, "Locations");
		
		visitor.endVisit(null, "Finder");
	}
	
	private void visitValues(IXMLVisitor visitor, String tagName) throws Exception
	{
		visitor.beginVisit(null, tagName, null, null, null);
		
		Map<String, String> map = getLocations();
		
		for (String key : map.keySet())
		{
			visitor.beginVisit(null, "Location", null, null, null);
			visitor.visit(null, "Id", key);
			visitor.visit(null, "Name", map.get(key));
			visitor.endVisit(null, "Location");
		}
		
		visitor.endVisit(null, tagName);
	}
	
	private Map<String, String> getLocations()
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
			
			ps = connection.prepareStatement(LocationFinderCommand.kLocationSelect);
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
			Debug.trace("LocationFinderCommand getLocations Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("LocationFinderCommand getLocations Exception finally = " + ex.getMessage());
			}
		}
		
		return map;
	}
	
	private static final String kLocationSelect =
		"select l.id, l.loc_name from TPT_LOCATIONS l " +
		"inner join FGT_DOMAIN d on d.id = l.split " +
		"where d.name = 'Reti' and l.custom1 = 'true'";
}
