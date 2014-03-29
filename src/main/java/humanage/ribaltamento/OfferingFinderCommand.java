package humanage.ribaltamento;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.saba.web.dk.SabaWebCommand;
import com.saba.xml.IXMLVisitor;

public class OfferingFinderCommand
	extends SabaWebCommand
{
	public OfferingFinderCommand()
	{
		
	}
	
	public void doExecute(HttpServletRequest request, IXMLVisitor visitor) throws Exception
	{
		visitor.beginVisit(null, "Finder", null, null, null);
		
		visitValues(visitor, "OfferingStatuses");
		
		visitor.endVisit(null, "Finder");
	}
	
	private void visitValues(IXMLVisitor visitor, String tagName) throws Exception
	{
		visitor.beginVisit(null, tagName, null, null, null);
		
		Map<String, String> map = getOfferingStatuses();
		
		for (String key : map.keySet())
		{
			visitor.beginVisit(null, "Item", null, null, null);
			visitor.visit(null, "Value", key);
			visitor.visit(null, "Name", map.get(key));
			visitor.endVisit(null, "Item");
		}
		
		visitor.endVisit(null, tagName);
	}
	
	private Map<String, String> getOfferingStatuses()
	{
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("*", "Tutte");
		map.put("true", "Solo archiviate");
		map.put("false", "Solo non archiviate");
		
		return map;
	}
}
