<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wdk="http://www.saba.com/XML/WDK">
	<xsl:import href="../../wdk/xsl/view/wdk_defaultview.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_widgetlayout.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_pagetext.xsl"/>

	<xsl:template match="wdk:model">
		<xsl:variable name="IsArchived" select="Offering/IsArchived"/>
		
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
			<tr>
				<td colspan="5">
					<xsl:apply-templates select="Result/wdk:widget[@name='wdkResult']"/>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<xsl:apply-templates select="$wdkWidget[@name='wdkPrint']"/>
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
		
		function openReport(classId, rptdfId, rptmdId)
		{
			wdkPopupSubmitWithTitle(
				"?"+"reportModelId="+sabaEncodeURI(rptmdId)+'&amp;'+"reportDefinitionId="+sabaEncodeURI(rptdfId)+'&amp;'+"objectIdKey="+sabaEncodeURI(classId)+'&amp;'+"executeReportDefinition="+sabaEncodeURI("executeReportDefinition")+'&amp;'+"isPopUp="+sabaEncodeURI("false"),
				"window_1178652715",
				"height=550,width=800,screenX=50,left=50,screenY=50,top=50,titlebar=yes,toolbar=yes,status=yes,directories=yes,hotkeys=yes,scrollbars=yes,resizable=yes,dependent=yes");
		}
		</script>

	</xsl:template>
</xsl:stylesheet>
