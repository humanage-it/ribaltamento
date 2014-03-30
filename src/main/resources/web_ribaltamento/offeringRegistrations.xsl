<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wdk="http://www.saba.com/XML/WDK">
	<xsl:import href="../../wdk/xsl/view/wdk_defaultview.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_widgetlayout.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_pagetext.xsl"/>

	<xsl:template match="wdk:model">
	
		<table>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkLocation']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkLocation']"/>
				</td>
				<td><div width="25"></div></td>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Offering/wdk:widget[@name='wdkStartDate']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Offering/wdk:widget[@name='wdkStartDate']"/>
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
				<td colspan="5">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td colspan="4">
					<table width="100%" cellpadding="5">
						<tr>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkCancel']"/></td>
							<td><xsl:apply-templates select="$wdkWidget[@name='wdkSave']"/></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>

	</xsl:template>
</xsl:stylesheet>
