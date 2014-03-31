package humanage.ribaltamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.saba.domain.Domain;
import com.saba.domain.DomainHome;
import com.saba.exception.SabaException;
import com.saba.learningoffering.OfferingManager;
import com.saba.learningoffering.entity.session.ILTOfferingReference;
import com.saba.locator.Delegates;
import com.saba.locator.LocatorContextNotSetException;
import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;
import com.saba.xml.XMLVisitorException;

public class OfferingsNoOwnerCommand
extends SabaWebCommand
{
	public OfferingsNoOwnerCommand()
	{
		
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		ServiceLocator locator = super.getServiceLocator();
		List<Offering> result = doSearch(locator);
		visitResult(visitor, result);
	}
	
	public void moveToTrash() throws LocatorContextNotSetException, SabaException
	{
		moveToTrash(ServiceLocator.getClientInstance());
	}
	
	public void moveToTrash(ServiceLocator locator) throws LocatorContextNotSetException, SabaException
	{
		DomainHome domainHome = (DomainHome)locator.getHome(Delegates.kDomain);
		Collection domains = domainHome.findByName("Trash");
		Domain trash = (Domain)domains.iterator().next();
		
		OfferingManager offeringManager = (OfferingManager)locator.getManager(Delegates.kOfferingManager);

		List<Offering> result = doSearch(locator);
		for (Offering o : result)
		{
			ILTOfferingReference offering = (ILTOfferingReference)ServiceLocator.getReference(o.Id);
			offeringManager.changeSecurityDomain(offering, trash);
		}
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
			visitor.endVisit(null, "Offering");
		}
		
		visitor.endVisit(null, "Result");
	}
	
	private List<Offering> doSearch(ServiceLocator locator)
	{
		List<Offering> result = new ArrayList<Offering>();
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(OfferingsNoOwnerCommand.kSearchSelect);
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				Offering item = new Offering();
				item.Id = rs.getString(1);
				item.Title = rs.getString(2);
				item.OfferingNumber = rs.getString(3);
				item.Location = rs.getString(4);
				item.StartDate = rs.getDate(5);
				
				result.add(item);
			}
		}
		catch (Exception ex)
		{
			System.out.println("OfferingsNoOwnerCommand doSearch Exception = " + ex.getMessage());
			Debug.trace("OfferingsNoOwnerCommand doSearch Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("OfferingsNoOwnerCommand doSearch Exception finally = " + ex.getMessage());
			}
		}
		
		return result;
	}

	private static final String kSearchSelect =
		"select Id, Title, OfferingNo, Location, StartDate from UGF_FONSAI_OFFERINGS_NO_OWNER order by StartDate desc, Location";
}
