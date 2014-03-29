package humanage.ribaltamento;

import java.util.ArrayList;
import java.util.List;

class Course
{
	public String Id;
	public String Title;
	public String CourseNumber;
	public String Abstract;
	public String Description;
	public String Duration;
	public String PublicationYear;
	public String IvassDisciplines;
	public String TrainingCodes;
	
	List<Attachment> Attachments = new ArrayList<Attachment>();
}
