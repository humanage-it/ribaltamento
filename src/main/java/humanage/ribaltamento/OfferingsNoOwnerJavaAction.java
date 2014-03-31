package humanage.ribaltamento;

import com.saba.exception.SabaException;
import com.saba.locator.ServiceLocator;
import com.saba.notify.IJavaNotificationAction;

public class OfferingsNoOwnerJavaAction
implements IJavaNotificationAction
{
	public void executeAction(String sourceId, String sourceDomainId, ServiceLocator locator)
	throws SabaException
	{
		OfferingsNoOwnerCommand command = new OfferingsNoOwnerCommand();
		command.moveToTrash(locator);
	}
}
