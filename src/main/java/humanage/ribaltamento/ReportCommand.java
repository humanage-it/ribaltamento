package humanage.ribaltamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;

public class ReportCommand
{
	public ReportCommand()
	{
		
	}
	
	public static Report findByName(String name)
	{
		Report report = new Report();
		
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
			
			ps = connection.prepareStatement(ReportCommand.kFindByName);
			ps.setString(1, name);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				report.DefinitionId = rs.getString(1);
				report.ModelId = rs.getString(2);
			}
		}
		catch (Exception ex)
		{
			System.out.println("ReportCommand findByName Exception = " + ex.getMessage());
			Debug.trace("ReportCommand findByName Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("RegistrationSearchCommand findByName Exception finally = " + ex.getMessage());
			}
		}
		
		return report;
	}
	
	private static final String kFindByName =
		"select id, rpt_mdl_id from FGT_EXT_RP_RPT_DEFN where name = ?";
}
