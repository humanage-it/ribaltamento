<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wdk="http://www.saba.com/XML/WDK">
	<xsl:import href="../../wdk/xsl/view/wdk_defaultview.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_widgetlayout.xsl"/>
	<xsl:import href="../../wdk/xsl/view/wdk_pagetext.xsl"/>

	<xsl:template match="wdk:model">
		<xsl:variable name="SaveResult" select="SaveResult"/>
		
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
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkInstructor']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkInstructor']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkStartDate']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkStartDate']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="Finder/Locations/wdk:widget[@name='wdkLocation']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="Finder/Locations/wdk:widget[@name='wdkLocation']"/>
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
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkStartTimeHour']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkStartTimeHour']"/>
					:
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkStartTimeMin']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkEndTime']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkEndTime']"/>
				</td>
			</tr>
			<tr>
				<td class="sbMainPageInstructions" colspan="2">
					Per i corsi di durata superiore alle 5 ore viene conteggiata automaticamente un'ora di pausa.
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkExpiryDate']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkExpiryDate']"/>
				</td>
			</tr>
			<tr>
				<td class="sbInputLabelCell">
					<xsl:apply-templates mode="WidgetLabel" select="$wdkWidget[@name='wdkIvass']"/>
				</td>
				<td class="sbInputFormCell">
					<xsl:apply-templates mode="WidgetNoLabel" select="$wdkWidget[@name='wdkIvass']"/>
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
				<td colspan="2">
					<xsl:call-template name="contentSpacer"/>
				</td>
			</tr>
			<tr>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkCancel']"/>
				</td>
				<td>
					<xsl:apply-templates select="$wdkWidget[@name='wdkSave']"/>
				</td>
			</tr>
		</table>
		
		<script type="text/javascript">
		function validateStartDate(dateFieldName, selectedDateString)
		{
			try
			{
				if (selectedDateString == "-")
				{
					selectedDateString = document.getElementsByName(dateFieldName)[0].value;
				}
				
				var parts = selectedDateString.split('/');
				var selectedDate = new Date(parts[2], parts[1]-1, parts[0]);
				
				var currentDate = new Date();
				currentDate.setDate(currentDate.getDate() - 1);
				
				if (selectedDate &lt; currentDate)
				{
					alert("La data di inizio aula non può essere precedente alla data attuale.");
					return false;
				}
				
				var endOfYear = new Date(currentDate.getFullYear(), 11, 31);
				if (selectedDate &gt; endOfYear)
				{
					alert("La data di inizio aula deve essere nell'anno corrente.");
					return false;
				}
				
				document.getElementsByName(dateFieldName)[0].value = selectedDateString;
			}
			catch(err)
			{
				alert("Formato data non valido. - " + err.message);
				return false;
			}
			
			return true;
		}
		
		function validateEndTime()
		{
			var endTime = document.getElementsByName("endTime")[0];
			var endH = parseInt(endTime.value.substring(0, 2), 10);
			var endM = parseInt(endTime.value.substring(3), 10);
			
			if ((endH == 20 &amp;&amp; endM > 0) || endH > 20)
			{
				alert("L'orario di fine edizione non deve essere oltre le ore 20:00.");
				return false;
			}
			
			return true;
		}
		
		function updateEndTime()
		{
			var duration = document.getElementsByName("duration")[0];
			var startTimeHour = document.getElementsByName("startTimeHour")[0];
			var startTimeMinute = document.getElementsByName("startTimeMin")[0];
			var endTime = document.getElementsByName("endTime")[0];
			
			var startH = parseInt(startTimeHour.value, 10);
			var startM = parseInt(startTimeMinute.value, 10);
			var duratH = parseInt(duration.value.substring(0, 2), 10);
			var duratM = parseInt(duration.value.substring(3), 10);
			var endH = startH + duratH;
			var endM = startM + duratM;
			
			if (endM > 60)
			{
				endH++;
				endM -= 60;
			}
			
			if (duratH &gt; 5)
			{
				endH++;
			}
			
			var strEndH = endH &lt; 10 ? "0" + endH : endH;
			var strEndM = endM &lt; 10 ? "0" + endM : endM;
			
			endTime.value = strEndH + ":" + strEndM;
		}
		
		function showSaveResult()
		{
			var saveResult = "<xsl:value-of select='$SaveResult'/>";
			if (saveResult != "")
			{
				alert(saveResult);
			}
		}
		
		updateEndTime();
		
		setTimeout(showSaveResult, 100);
		</script>
		
	</xsl:template>
</xsl:stylesheet>
