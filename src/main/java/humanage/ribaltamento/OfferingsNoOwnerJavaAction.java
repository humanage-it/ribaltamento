package humanage.ribaltamento;

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
		OfferingsNoOwnerCommand command = new OfferingsNoOwnerCommand();
		Debug.trace("HUMANAGE: Start Java Action OfferingsNoOwnerJavaAction");
		command.moveToTrash();
		Debug.trace("HUMANAGE: End Java Action OfferingsNoOwnerJavaAction");
	}
}
