<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wdk="http://www.saba.com/XML/WDK">
	<xsl:import href="../../wdk/xsl/view/wdk_defaultview.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_widgetlayout.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_pagetext.xsl"/>

	<xsl:template match="wdk:model">
		<xsl:variable name="IsArchived" select="Offering/IsArchived"/>
		<xsl:variable name="Step" select="Step"/>
		<xsl:variable name="AttachmentUrl" select="AttachmentUrl"/>
		
		<table>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkTitle']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkTitle']"/>
				</td>
				<td><div width="25"></div></td>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkStartTime']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkStartTime']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkOfferingNumber']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkOfferingNumber']"/>
				</td>
				<td><div width="25"></div></td>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkEndTime']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkEndTime']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkInstructor']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkInstructor']"/>
				</td>
				<td><div width="25"></div></td>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkExpiryDate']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkExpiryDate']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkStartDate']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkStartDate']"/>
				</td>
				<td><div width="25"></div></td>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkIvass']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkIvass']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkLocation']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkLocation']"/>
				</td>
				<td><div width="25"></div></td>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkIvassDisciplines']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkIvassDisciplines']"/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td></td>
				<td></td>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkDuration']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkDuration']"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:apply-templates select="$wdkWidget[@name='wdkSections']"/>
				</td>
			</tr>
			
			<xsl:if test="$Step = 'step1'">
			<tr>
				<td class="sbInputLabelCell" colspan="3">
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkUpload']"/>
				</td>
				<td class="sbInputFormCell" colspan="2">
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkUpload']"/>
				</td>
			</tr>
			<tr>
				<td colspan="5" align="right">
					<xsl:apply-templates select="$wdkWidget[@name='wdkNext']"/>
				</td>
			</tr>
			</xsl:if>
			
			<xsl:if test="$Step = 'step2'">
			<tr>
				<td colspan="5">
					<iframe width="100%" height="300px" src="about:blank">
						<xsl:attribute name="src"><xsl:value-of select='$AttachmentUrl'/></xsl:attribute>
					</iframe>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkInstructions']"/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td></td>
				<td></td>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkBack']"/>
				</td>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkNext']"/>
				</td>
			</tr>
			</xsl:if>
			
			<xsl:if test="$Step = 'step3'">
				<xsl:if test="not($IsArchived = 'true')">
				<tr>
					<td class="sbInputLabelCell" colspan="3">
						<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkUpload']"/>
					</td>
					<td class="sbInputFormCell" colspan="2">
						<table>
							<tr>
								<td><xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkUpload']"/></td>
								<td><xsl:apply-templates select="$wdkWidget[@name='wdkUpdate']"/></td>
							</tr>
						</table>
					</td>
				</tr>
				</xsl:if>
			<tr>
				<td colspan="5">
					<xsl:apply-templates select="$wdkWidget[@name='wdkOpenFile']"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:apply-templates select="Result/wdk:widget[@name='wdkResult']"/>
				</td>
			</tr>
			<tr>
				<td colspan="4"></td>
				<td valign="bottom">
					<xsl:apply-templates select="$wdkWidget[@name='wdkProcess']"/>
				</td>
			</tr>
			</xsl:if>
			
			<xsl:if test="$Step = 'step4'">
			<tr>
				<td colspan="5">
					<table>
						<tr>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkSummary']"/></td>
						</tr>
						<tr>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkTotalRegistered']"/></td>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkTotalPresent']"/></td>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkTotalAbsent']"/></td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:apply-templates select="Result/wdk:widget[@name='wdkResult']"/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td></td>
				<td></td>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkBack']"/>
				</td>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkConfirm']"/>
				</td>
			</tr>
			</xsl:if>
			
			<xsl:if test="$Step = 'step5'">
			<tr>
				<td>
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkLabel']"/>
				</td>
				<td colspan="4">
					<xsl:apply-templates select="$wdkWidget[@name='wdkOpenFile']"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:apply-templates select="Result/wdk:widget[@name='wdkResult']"/>
				</td>
			</tr>
			</xsl:if>
			
			<tr>
				<td colspan="5">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td colspan="4">
					<table width="100%" cellpadding="5">
						<tr>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkEdit']"/></td>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkCancel']"/></td>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkClose']"/></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		
		<script type="text/javascript">
		var isArchived = "<xsl:value-of select='$IsArchived'/>";
		var wdkEdit = document.getElementById("wdkEdit");
		if (wdkEdit &amp;&amp; isArchived == "true") {
			wdkEdit.disabled = true;
			wdkEdit.href = "javascript:return false;";
			wdkEdit.onclick = "";
			wdkEdit.style.color = "#A0A0A0";
		}
		
		var wdkCancel = document.getElementById("wdkCancel");
		if (wdkCancel &amp;&amp; isArchived == "true") {
			wdkCancel.disabled = true;
			wdkCancel.href = "javascript:return false;";
			wdkCancel.onclick = "";
			wdkCancel.style.color = "#A0A0A0";
		}
		
		function confirmCancel()
		{
			return confirm("Confermare la cancellazione dell'edizione?");
		}
		
		function setMultiPart()
		{
			document.forms["theForm"].encoding = "multipart/form-data";
			return true;
		}
		
		function checkFileUpload()
		{
			var uploadfile = document.getElementsByName("uploadfile")[0];
			if (uploadfile.value)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		<xsl:if test="$Step = 'step1'">
		setMultiPart();
		</xsl:if>
		
		<xsl:if test="$Step = 'step3'">
		var wdkProcessHref = document.getElementById("wdkProcess").href;
		var wdkProcessOnclick = document.getElementById("wdkProcess").onclick;
		function enableProcess(enable)
		{
			var wdkProcess = document.getElementById("wdkProcess");
			wdkProcess.disabled = !enable;
			wdkProcess.href = enable ? wdkProcessHref : "javascript:return false;";
			wdkProcess.onclick = enable ? wdkProcessOnclick : "";
			wdkProcess.style.color = enable ? "#101010" : "#A0A0A0";
		}
		
		enableProcess(false);
		</xsl:if>
		
		var presenceCount = 0;
		function countPresenceElements()
		{
			var elements = document.forms["theForm"].elements;
			
			for (var i = 0; i &lt; elements.length; i++)
			{
				if (elements[i].tagName === "INPUT" &amp;&amp; elements[i].type === "radio" &amp;&amp; elements[i].name.indexOf("presence_") == 0)
				{
					presenceCount++;
				}
			}
			
			presenceCount = presenceCount / 2;
		}
		
		countPresenceElements();
		
		
		function selectPresence(id)
		{
			var presence = document.forms["theForm"].elements["presence_" + id];
			
			presenceCount--;
			if (presenceCount == 0)
			{
				enableProcess(true);
			}
		}
		
		function downloadAttachment(attachmentUrl)
		{
			msg = open(attachmentUrl, "Viewer", "toolbar=yes,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=620,height=560");
		}
		</script>

	</xsl:template>
</xsl:stylesheet>
