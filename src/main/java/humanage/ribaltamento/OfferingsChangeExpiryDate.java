package humanage.ribaltamento;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.web.engine.SabaHttpServletRequest;
import com.saba.xml.IXMLVisitor;
import com.saba.xml.XMLVisitorException;

public class OfferingsChangeExpiryDate
extends SabaWebCommand
{
	public OfferingsChangeExpiryDate()
	{
		addInParam("actionKey", String.class, "actionKey");
		addInParam("tempFolder", String.class, "tempFolder");
		addInParam("csvFilename", String.class, "csvFilename");
		
		_processOutcome = new StringBuffer();
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		String actionKey = (String)getArg("actionKey");
		String tempFolder = (String)getArg("tempFolder");
		_csvFilename = (String)getArg("csvFilename");
		
		boolean doUpdate = actionKey.equals("process");
		
		List<Offering> result = new ArrayList<Offering>();
		
		Hashtable<String, String> items = loadCsv(request, tempFolder);
		Enumeration<String> keys = items.keys();
		while (keys.hasMoreElements())
		{
			String key = keys.nextElement();
			Offering offering = doSearch(key);
			if (offering.Id == null)
			{
				offering.OfferingNumber = key;
			}

			// borrow the EndTime field to store the new Expiry Date
			offering.EndTime = items.get(key);
		
			if (doUpdate)
			{
				if (offering.Id == null) continue; // skip records in CSV not matching existing offerings in DB
				updateExpiryDate(offering.Id, offering.EndTime);
				offering.ExpiryDate = parseCsvDate(offering.EndTime);
			}
			
			result.add(offering);
		}
		
		visitResult(visitor, result);
	}
	
	public String getProcessOutcome()
	{
		if (_processOutcome.toString().length() == 0)
		{
			_processOutcome.append("Operazione completata con successo.");
		}
		
		return _processOutcome.toString();
	}
	
	public String getCsvFilename()
	{
		return _csvFilename;
	}
	
	public void updateExpiryDate(String offeringId, String newExpiryDate)
	{
		try
		{
			Calendar date = Calendar.getInstance();
			date.setTime(parseCsvDate(newExpiryDate));
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			newExpiryDate = df.format(date.getTime()) + " 00:00:00.0";
			
			ConnectionManager conmanager = null;
			Connection connection = null;
			PreparedStatement ps = null;
			
			try
			{
				ServiceLocator locator = super.getServiceLocator();
				String siteName = locator.getSabaPrincipal().getSiteName();
				conmanager = locator.getConnectionManager();
				connection = conmanager.getConnection(siteName);
				
				ps = connection.prepareStatement("update LET_EXT_OFFERING_SESSION set custom0 = ? where id = ?");
				ps.setString(1,  newExpiryDate);
				ps.setString(2,  offeringId);
				ps.executeUpdate();
			}
			catch (Exception ex)
			{
				System.out.println("OfferingsChangeExpiryDate updateExpiryDate Exception = " + ex.getMessage());
				Debug.trace("OfferingsChangeExpiryDate updateExpiryDate Exception = " + ex.getMessage());
			}
			finally
			{
				try {
					ConnectionManager.closeStatement(ps, connection);
					conmanager.freeConnection(connection);
				}
				catch (Exception ex) {
					Debug.trace("OfferingsChangeExpiryDate updateExpiryDate Exception finally = " + ex.getMessage());
				}
			}
		}
		catch (Exception ex)
		{
			_processOutcome.append(ex.getMessage());
		}
	}
	
	private Hashtable<String, String> loadCsv(HttpServletRequest request, String tempFolder) throws IOException
	{
		Hashtable<String, String> items = new Hashtable<String, String>();
		File csv = null;
		
		if (request.getParameter("uploadfile") != null && (request instanceof SabaHttpServletRequest))
		{
			csv = ((SabaHttpServletRequest)request).getFile("uploadfile");
			if (csv != null)
			{
				// file found in request, save to temporary folder
				_csvFilename = csv.getName();
				FileWriter writer = new FileWriter(tempFolder + "/" + _csvFilename);
				BufferedReader reader = new BufferedReader(new FileReader(csv));

				try
				{
					String line = reader.readLine();
					while (line != null)
					{
						writer.write(line);
						writer.write("\r");
						line = reader.readLine();
					}
				}
				finally
				{
					reader.close();
					writer.close();
				}
			}
		}
		else
		{
			// file not found in request, read from temporary folder
			csv = new File(tempFolder + "/" + _csvFilename);
		}
			
		if (csv != null)
		{
			BufferedReader reader = new BufferedReader(new FileReader(csv));
			
			try
			{
				String line = reader.readLine();
				while (line != null)
				{
					String parts[] = line.split(",");
					if (parts != null && parts.length > 1)
					{
						items.put(parts[0], parts[1]);
					}
					
					line = reader.readLine();
				}
			}
			catch (IOException ex)
			{
				//
			}
			finally
			{
				reader.close();
			}
		}
		
		return items;
	}
	
	private void visitResult(IXMLVisitor visitor, List<Offering> result) throws XMLVisitorException
	{
		visitor.beginVisit(null, "Result", null, null, null);
		
		for (Offering o : result)
		{
			visitor.beginVisit(null, "Offering", null, null, null);
			visitor.visit(null, "Id", o.Id);
			visitor.visit(null, "Title", o.Title);
			visitor.visit(null, "OfferingNumber", o.OfferingNumber);
			visitor.visit(null, "Location", o.Location);
			visitor.visit(null, "StartDate", o.StartDate);
			visitor.visit(null, "ExpiryDate", o.ExpiryDate);
			visitor.visit(null, "NewExpiryDate", o.EndTime);
			visitor.endVisit(null, "Offering");
		}
		
		visitor.endVisit(null, "Result");
	}
	
	private Offering doSearch(String offeringNumber)
	{
		Offering item = new Offering();
		
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
			
			ps = connection.prepareStatement(OfferingsChangeExpiryDate.kSearchSelect);
			ps.setString(1,  offeringNumber);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				item.Id = rs.getString(1);
				item.Title = rs.getString(2);
				item.OfferingNumber = rs.getString(3);
				item.Location = rs.getString(4);
				item.StartDate = rs.getDate(5);
				item.ExpiryDate = parseDate(rs.getString(6));
			}
		}
		catch (Exception ex)
		{
			System.out.println("OfferingsChangeExpiryDate doSearch Exception = " + ex.getMessage());
			Debug.trace("OfferingsChangeExpiryDate doSearch Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingsChangeExpiryDate doSearch Exception finally = " + ex.getMessage());
			}
		}
		
		return item;
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
	
	private Date parseCsvDate(String dateStr)
	{
		if (dateStr == null) return null;
		
		try
		{
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			return df.parse(dateStr);
		}
		catch (ParseException ex)
		{
		}
		
		return null;
	}

	private static final String kSearchSelect =
		//"select Id, Title, OfferingNumber, Location, StartDate, ExpiryDate from UGF_FONSAI_OFFERINGS where OfferingNumber = ?";
		"select s.id, t.title, s.class_no, l.loc_name, s.start_date, s.custom0 " +
		"from LET_EXT_OFFERING_SESSION s " +
		"inner join LET_EXT_OFFERING_TEMPLATE t on t.id = s.offering_temp_id and t.locale_id = s.locale_id " +
		"inner join TPT_LOCATIONS l on l.id = s.location_id " +
		"where s.locale_id = 'local000000000000010' and s.class_no = ?";

	private StringBuffer _processOutcome;
	private String _csvFilename;
}
