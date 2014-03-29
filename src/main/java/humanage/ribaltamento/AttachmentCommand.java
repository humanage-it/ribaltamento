package humanage.ribaltamento;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import com.saba.exception.SabaException;
import com.saba.locator.LocatorContextNotSetException;
import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.util.Debug;
import com.saba.web.util.WDKSabaUtil;

public class AttachmentCommand
{
	public AttachmentCommand(String attachmentId) throws Exception
	{
		if (attachmentId == null || attachmentId.length() == 0)
		{
			throw new Exception("AttachmentCommand: attachmentId is null");
		}
		
		// attachmentId starts with ntdch* (FGT_NOT_DOCS_HEADER table)
		_id = attachmentId;
		loadAttachment(attachmentId);
	}
	
	public static AttachmentCommand findByName(String ownerId, String name) throws Exception
	{
		String attachmentId = null;
		
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
			
			ps = connection.prepareStatement(AttachmentCommand.kFindByName);
			ps.setString(1, ownerId);
			ps.setString(2, name);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				attachmentId = rs.getString(1);
			}
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand findByName Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand findByName Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand findByName Exception finally = " + ex.getMessage());
			}
		}
		
		return attachmentId == null ? null : new AttachmentCommand(attachmentId);
	}
	
	public static AttachmentCommand findByCategory(String ownerId, String category) throws Exception
	{
		String attachmentId = null;
		
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
			
			ps = connection.prepareStatement(AttachmentCommand.kFindByCategory);
			ps.setString(1, ownerId);
			ps.setString(2, category);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				attachmentId = rs.getString(1);
			}
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand findByCategory Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand findByCategory Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand findByCategory Exception finally = " + ex.getMessage());
			}
		}
		
		return attachmentId == null ? null : new AttachmentCommand(attachmentId);
	}
	
	public static String createTemporary(HttpServletRequest request, String ownerId, String name, String category) throws Exception
	{
		com.saba.client.appframework.AttachmentCommand attachmentCommand = new com.saba.client.appframework.AttachmentCommand(ServiceLocator.getClientInstance());
		Calendar now = Calendar.getInstance();
		
		// docId starts with notdc* (FGT_NOT_DOCS table)
		String docId = attachmentCommand.create(
			ownerId, 
			name + now.getTimeInMillis(),	// make unique
			category,
			ServiceLocator.getClientInstance().getSabaPrincipal().getLocale(),
			"",		// url
			false,	// is url
			request,
			"",		// reason
			false	// is private
		);
		
		String attachmentId = getAttachmentIdFromDocId(docId);
		
		// attachment not yet confirmed, remove the owner
		makeTemporary(attachmentId);
		
		return attachmentId;
	}
	
	public static void delete(String attachmentId) throws LocatorContextNotSetException, SabaException
	{
		String docId = getDocIdFromAttachmentId(attachmentId);
		deleteAttachmentContent(docId);
		deleteAttachmentHeader(attachmentId);
	}
	
	public static void save(String offeringId, String attachmentId) throws Exception
	{
		// check whether an attachment with the same category already exists
		// and remove it before saving the new one
		AttachmentCommand command = findByCategory(offeringId, "Lista presenze");
		if (command != null)
		{
			String existingAttachmentId = command.getAttachmentId();
			if (existingAttachmentId != null)
			{
				delete(existingAttachmentId);
			}
		}
		
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(AttachmentCommand.kSave);
			ps.setString(1, attachmentId);
			ps.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand save Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand save Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand save Exception finally = " + ex.getMessage());
			}
		}
	}
	
	public static String buildSabaUrl(HttpServletRequest request, String attachmentId, String disposition) throws SabaException
	{
		String[][] params = {
			{ "attachmentId", attachmentId },
			{ "disposition", disposition }
		};
		
		return WDKSabaUtil.createWDKPageURL(request, "/custom/ribaltamento/downloadAttachment.rdf", params);
	}
	
	public String getAttachmentId()
	{
		return _id;
	}
	
	public String getDocumentId()
	{
		return _docId;
	}

	public String getFileName()
	{
		return _filename;
	}
	
	public String getDocType()
	{
		return _doctype;
	}

	public String getAttachType()
	{
		return _attachtype;
	}

	public String getUrl()
	{
		return _url;
	}

	public long getFileSize()
	{
		return _filesize;
	}

	public InputStream getStream()
	{
		return _stream;
	}
	
	private static String getAttachmentIdFromDocId(String docId)
	{
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String attachmentId = "";
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(AttachmentCommand.kAttachmentIdFromDocId);
			ps.setString(1, docId);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				attachmentId = rs.getString(1);
			}
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand getAttachmentIdFromDocId Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand getAttachmentIdFromDocId Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand getAttachmentIdFromDocId Exception finally = " + ex.getMessage());
			}
		}
		
		return attachmentId;
	}
	
	private static String getDocIdFromAttachmentId(String attachmentId)
	{
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String docId = "";
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(AttachmentCommand.kDocIdFromAttachmentId);
			ps.setString(1, attachmentId);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				docId = rs.getString(1);
			}
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand getDocIdFromAttachmentId Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand getDocIdFromAttachmentId Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand getDocIdFromAttachmentId Exception finally = " + ex.getMessage());
			}
		}
		
		return docId;
	}

	private static void makeTemporary(String attachmentId)
	{
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(AttachmentCommand.kTemporaryUpdate);
			ps.setString(1, attachmentId);
			ps.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand makeTemporary Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand makeTemporary Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand makeTemporary Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private static void deleteAttachmentContent(String docId)
	{
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(AttachmentCommand.kDeleteAttach);
			ps.setString(1, docId);
			ps.executeUpdate();

			ps = connection.prepareStatement(AttachmentCommand.kDeleteDoc);
			ps.setString(1, docId);
			ps.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand deleteAttachmentContent Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand deleteAttachmentContent Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand deleteAttachmentContent Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private static void deleteAttachmentHeader(String attachmentId)
	{
		ConnectionManager conmanager = null;
		Connection connection = null;
		PreparedStatement ps = null;
		
		try
		{
			ServiceLocator locator = ServiceLocator.getClientInstance();
			String siteName = locator.getSabaPrincipal().getSiteName();
			conmanager = locator.getConnectionManager();
			connection = conmanager.getConnection(siteName);
			
			ps = connection.prepareStatement(AttachmentCommand.kDeleteHeader);
			ps.setString(1, attachmentId);
			ps.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand deleteAttachmentHeader Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand deleteAttachmentHeader Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand deleteAttachmentHeader Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private void loadAttachment(String attachmentId)
	{
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
			
			ps = connection.prepareStatement(AttachmentCommand.kAttachmentSelect);
			ps.setString(1, attachmentId);
			rs = ps.executeQuery();
			
			if (rs.next())
			{
				_filename = rs.getString(1);
				_doctype = rs.getString(2);
				_attachtype = rs.getString(3);
				_url = rs.getString(4);
				_docId = rs.getString(5);
				_filesize = rs.getLong(6);
				_stream = rs.getBinaryStream(7);
			}
		}
		catch (Exception ex)
		{
			System.out.println("AttachmentCommand loadAttachment Exception = " + ex.getMessage());
			Debug.trace("AttachmentCommand loadAttachment Exception = " + ex.getMessage());
		}
		finally
		{
			try {
				rs.close();
				ConnectionManager.closeStatement(ps, connection);
				conmanager.freeConnection(connection);
			}
			catch (Exception ex) {
				Debug.trace("AttachmentCommand loadAttachment Exception finally = " + ex.getMessage());
			}
		}
	}
	
	private String _id;
	private String _docId;
	private String _filename;
	private String _doctype;
	private String _attachtype;
	private String _url;
	private long _filesize;
	private InputStream _stream;
	
	private static final String kAttachmentSelect =
		"select a.Filename, a.DocType, a.AttachType, a.Url, a.AttachId, b.total_size as TotalSize, b.attachment as AttachmentStream " +
		"from UGF_ATTACHMENTS a inner join FGT_NOT_ATTACH b on b.id = a.AttachId " +
		"where a.Id = ?";

	private static final String kAttachmentIdFromDocId =
		"select owner_id from FGT_NOT_DOCS where id = ?";

	private static final String kDocIdFromAttachmentId =
		"select id from FGT_NOT_DOCS where owner_id = ?";
	
	private static final String kTemporaryUpdate =
		"update FGT_NOT_DOCS_HEADER set	description = id + owner_id, owner_id = 'RIBALTAMENTO_FONSAI0' " +
		"where id = ?";

	private static final String kSave =
		"update FGT_NOT_DOCS_HEADER set owner_id = SUBSTRING(description, 21, 20), description = doc_type " +
		"where id = ?";
	
	private static final String kDeleteAttach =
		"delete FGT_NOT_ATTACH where id = ?";

	private static final String kDeleteDoc =
		"delete FGT_NOT_DOCS where id = ?";

	private static final String kDeleteHeader =
		"delete FGT_NOT_DOCS_HEADER where id = ?";
	
	private static final String kFindByName =
		"select Id from UGF_ATTACHMENTS where OwnerId = ? and Name = ? ";

	private static final String kFindByCategory =
		"select Id from UGF_ATTACHMENTS where OwnerId = ? and Description = ? ";
}
