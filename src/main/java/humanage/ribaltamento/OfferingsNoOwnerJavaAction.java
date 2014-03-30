package humanage.ribaltamento;

import java.io.*;
import java.util.Date;

import com.saba.exception.SabaException;
import com.saba.locator.ServiceLocator;
import com.saba.notify.IJavaNotificationAction;
import com.saba.util.Debug;

public class OfferingsNoOwnerJavaAction
implements IJavaNotificationAction
{
	public void executeAction(String sourceId, String sourceDomainId, ServiceLocator locator)
	throws SabaException
	{
		
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("e:\\SabaWeb\\humanage.txt", true)));
		    out.println(new Date().toString());
		    out.close();
		} catch (IOException e) {
			System.out.println("HUMANAGE:  OfferingsNoOwnerJavaAction error creating flag file");
		}
		
		
		OfferingsNoOwnerCommand command = new OfferingsNoOwnerCommand();
		Debug.trace("HUMANAGE: Start Java Action OfferingsNoOwnerJavaAction");
		System.out.println("HUMANAGE: Start Java Action OfferingsNoOwnerJavaAction");
		command.moveToTrash(locator);
		Debug.trace("HUMANAGE: End Java Action OfferingsNoOwnerJavaAction");
		System.out.println("HUMANAGE: End Java Action OfferingsNoOwnerJavaAction");
		
	}
}
