<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wdk="http://www.saba.com/XML/WDK">
	<xsl:import href="../../wdk/xsl/view/wdk_defaultview.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_widgetlayout.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_pagetext.xsl"/>
	<xsl:import href="../../learning/paintTablepagewidget.xsl"/>

	<xsl:template match="wdk:model">
	
		<table width="570" border="0" cellspacing="2" cellpadding="0" align="left">
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates select="$wdkWidget[@name='wdkUpload']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates select="$wdkWidget[@name='wdkProcess']"/>
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
		
		<script type="text/javascript">
		function setMultiPart()
		{
			document.forms["theForm"].encoding = "multipart/form-data";
			return true;
		}
		</script>
		
	</xsl:template>
</xsl:stylesheet>
