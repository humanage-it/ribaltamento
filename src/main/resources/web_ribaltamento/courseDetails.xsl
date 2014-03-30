<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wdk="http://www.saba.com/XML/WDK">
	<xsl:import href="../../wdk/xsl/view/wdk_defaultview.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_widgetlayout.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_pagetext.xsl"/>

	<xsl:template match="wdk:model">
	
		<table>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkTitle']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkTitle']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkCourseNumber']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkCourseNumber']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkAbstract']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkAbstract']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkDescription']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkDescription']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkDuration']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkDuration']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkPublicationYear']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkPublicationYear']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkIvassDisciplines']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkIvassDisciplines']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Course/wdk:widget[@name='wdkTrainingCodes']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Course/wdk:widget[@name='wdkTrainingCodes']"/>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
		</table>
		
		<table width="570" border="0" cellspacing="2" cellpadding="0" align="left">
			<tr>
				<td colspan="2">
					<xsl:apply-templates select="Course/Attachments/wdk:widget[@name='wdkAttachments']"/>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkBack']"/>
				</td>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkNewOffering']"/>
				</td>
			</tr>
		</table>
		
		<script type="text/javascript">
		function disableTextAreas()
		{
			var abstract = document.forms["theForm"].elements["wdkAbstract"];
			if (abstract) {
				abstract.readOnly = true;
				abstract.style.backgroundColor = "#E0E0E0";
			}
			
			var description = document.forms["theForm"].elements["wdkDescription"];
			if (description) {
				description.readOnly = true;
				description.style.backgroundColor = "#E0E0E0";
			}
		}
		
		disableTextAreas();
		</script>
	</xsl:template>
</xsl:stylesheet>
