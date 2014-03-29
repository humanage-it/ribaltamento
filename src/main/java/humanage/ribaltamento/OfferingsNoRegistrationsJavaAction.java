package humanage.ribaltamento;

import com.saba.exception.SabaException;
import com.saba.locator.ServiceLocator;
import com.saba.notify.IJavaNotificationAction;

public class OfferingsNoRegistrationsJavaAction
implements IJavaNotificationAction
{
	public void executeAction(String sourceId, String sourceDomainId, ServiceLocator locator)
	throws SabaException
	{
		OfferingsNoRegistrationsCommand command = new OfferingsNoRegistrationsCommand();
		command.moveToTrash();
	}
}
