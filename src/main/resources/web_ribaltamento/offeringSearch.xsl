<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wdk="http://www.saba.com/XML/WDK">
	<xsl:import href="../../wdk/xsl/view/wdk_defaultview.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_widgetlayout.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_pagetext.xsl"/>
	<xsl:import href="../../learning/paintTablepagewidget.xsl"/>

	<xsl:template match="wdk:model">
	
		<xsl:variable name="finderForm">
			<table>
				<tr>
					<td class="sbInputLabelCell">
						<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkTitle']"/>
					</td>
					<td class="sbInputFormCell">
						<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkTitle']"/>
					</td>
					<td class="sbInputLabelCell">
						<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkNumber']"/>
					</td>
					<td class="sbInputFormCell">
						<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkNumber']"/>
					</td>
				</tr>
				<tr>
					<td class="sbInputLabelCell">
						<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkStartFrom']"/>
					</td>
					<td class="sbInputFormCell">
						<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkStartFrom']"/>
					</td>
					<td class="sbInputLabelCell">
						<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkStartTo']"/>
					</td>
					<td class="sbInputFormCell">
						<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkStartTo']"/>
					</td>
				</tr>
				<tr>
					<td class="sbInputLabelCell">
						<xsl:apply-templates mode="WidgetLabel" select="Finder/OfferingStatuses/wdk:widget[@name='wdkStatus']"/>
					</td>
					<td class="sbInputFormCell">
						<xsl:apply-templates mode="WidgetNoLabel" select="Finder/OfferingStatuses/wdk:widget[@name='wdkStatus']"/>
					</td>
					<td colspan="2" align="right">
						<xsl:apply-templates select="$wdkWidget[@name='wdkSearch']"/>
					</td>
				</tr>
				<tr>
					<td colspan="4">
						<xsl:call-template name="contentSpacer"/>
					</td>
				</tr>
			</table>
		</xsl:variable>
		
		<table width="570" border="0" cellspacing="2" cellpadding="0" align="left">
			<tr>
				<td>
					<xsl:call-template name="paintTable">
						<xsl:with-param name="html" select="$finderForm" />
					</xsl:call-template>
				</td>				
			</tr>
			<tr>
				<td>
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td>
					<xsl:apply-templates select="Result/wdk:widget[@name='wdkResult']"/>
				</td>
			</tr>
		</table>
		
	</xsl:template>
</xsl:stylesheet>
