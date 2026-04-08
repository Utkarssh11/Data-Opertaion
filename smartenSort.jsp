<%--
  - @(#)smartenSort.jsp Version: SmartenView <Jun 29, 2017>
  - Copyright 2015 Elegant MicroWeb Technologies Pvt. Ltd. (India). All Rights Reserved. Use is subject to license terms.
--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 

<div id="sortSmartenParent">
<div id="sortParentDiv">
	<form:form name="addSmartenSort" id="addSmartenSort"> <!-- modelAttribute="sortInfo" -->
		<div id="smartenSortDiv">
			<strong style="margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;"><s:message code="LBL_DIMENSION" />
			</strong>
		</div>
		<div  id="sortDimensionDiv">
		</div>
		<div>
			<strong style="margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;"><s:message code="LBL_SORT_ORDER"/>
			</strong>
		</div>
		<div id="smartenSortOrderDiv">
			<select class='form-control input-sm' style="margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;">
				<%-- <option value="-1"><s:message code="LBL_NONE" /></option> --%>
				<option value="0"><s:message code="LBL_ASCENDING_SORT" /></option>
				<option value="1"><s:message code="LBL_SORT_REPOSITORY_DESENDING" /></option>
			</select>
		</div>
		<div>
			<strong style="margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;"><s:message code="LBL_ON_TEXT"/>
			</strong>
		</div>
		<div id="sortMeasureDiv">
		</div>
		<div>
		<button style="position: relative;  margin: 5%;" id="smartenSortOk" class="btn btn-blue apply-but" type="button"
				onclick="onSortApply()">
			<s:message code="LBL_ADDOBJECT" /></button>
	</div>
	</form:form>
	
	<!-- New Code Start -->
		<div id="smartenSortPropobjects">
			<jsp:include page="/jsp/smartview/smartenSortObject.jsp"/>
		</div>
	<!-- New Code End -->
	
	<button style="position: relative; margin: 5%;" id="smartenSortApply" class="btn btn-blue apply-but" type="button">
		<s:message code="LBL_APPLY" />
	</button>
</div>
</div>
<script type="text/javascript">

$(document).ready(function() {
	loadSortMeasure();
	var baseId = "sortSmartenParent";//
	var height = $("#src").height();//$("#smartenSortAjax").height()-1; 
	setSmartenScroll(baseId,height);
});

function loadSortMeasure()
{
	// Sort Quick Setting: map technical column names to user-friendly aliases for dropdown labels.
	var aliasMap = {};
	<c:forEach items="${colLabelsAliasMap}" var="entry">
		aliasMap["${entry.key}"] = "${entry.value}";
	</c:forEach>
	
	//For measures
	var allMeasure = '${allMeasures}';
	var array = allMeasure.split(',');
	var item;
	for (var i = 0, size = array.length; i < size; i++) {
		item = array[i];

	}
	var newDiv = document.getElementById("sortMeasureDiv");
	var selectHTML = "";
	selectHTML = "<select class='form-control input-sm' style='margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;' id='smartenSortMeasures'>";
	selectHTML += "<option value=-1>None</option>";
	/* for (i = 0; i < array.length; i = i + 1) {
		selectHTML += "<option value='"+i+"'>" + array[i] + "</option>";
	} */
	
	for (i = 0; i < array.length; i = i + 1) {
		var originalName = array[i];
		var displayName = (aliasMap[originalName] && aliasMap[originalName].trim() !== "") ? aliasMap[originalName] : originalName;
		// Keep option value as original column name so sort requests remain backend-compatible.
		selectHTML += "<option value='"+originalName+"'>" + displayName + "</option>";
	}
	selectHTML += "</select>";
	newDiv.innerHTML = selectHTML;
	
	//For measures end
	
	//For dimensions
	var allDimension = '${allDimension}';
	var array = allDimension.split(',');
	var item;
	for (var i = 0, size = array.length; i < size; i++) {
		item = array[i];

	}
	var newDiv = document.getElementById("sortDimensionDiv");
	var selectHTML = "";
	selectHTML = "<select class='form-control input-sm' style='margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;' id='smartenSortDimension'>";
	/* for (i = 0; i < array.length; i = i + 1) {
		selectHTML += "<option value='"+i+"'>" + array[i] + "</option>";
	} */
	
	for (i = 0; i < array.length; i = i + 1) {
		var originalName = array[i];
		var displayName = (aliasMap[originalName] && aliasMap[originalName].trim() !== "") ? aliasMap[originalName] : originalName;
		selectHTML += "<option value='"+originalName+"'>" + displayName + "</option>";
	}
	
	selectHTML += "</select>";
	newDiv.innerHTML = selectHTML;
	//For dimensions end
}

function removeSortSmarten(sortColumnName) {
	var param = "templateId=" + sortColumnName;
	ajaxSubmit("./deleteSmartenSort", "manageSortDiv", param,mySortFunction());
}

function onSortApply()
{
	/* var dimensionText = $("#sortDimensionDiv :selected").text();
	var sortDirection = $("#smartenSortOrderDiv :selected").val();
	var measureText = $("#sortMeasureDiv :selected").text(); */
	
	var dimensionText = $("#sortDimensionDiv :selected").val();
	var sortDirection = $("#smartenSortOrderDiv :selected").val();
	var measureText = $("#sortMeasureDiv :selected").val();
	
	var param = "srtDimension="+dimensionText+"&srtMeasure="+measureText+"&sortDirection="+sortDirection+"&addEditSortFlag="+false;
	ajaxcall("./saveSmartenSort", 'manageSortDiv',param, 'showSortListSmarten()', true, true, true);
}

function showSortListSmarten(response)
{
	ajaxcall("./sortSmarten", "manageSortDiv",'','');
}

function mySortFunction() {
    setTimeout(function(){ showSortListSmarten() }, 100);
}

$("#smartenSortApply").click(function(){
	/* var dimensionText = $("#sortDimensionDiv :selected").text();
	var sortDirection = $("#smartenSortOrderDiv :selected").val();
	var measureText = $("#sortMeasureDiv :selected").text(); */
	
	var dimensionText = $("#sortDimensionDiv :selected").val();
	var sortDirection = $("#smartenSortOrderDiv :selected").val();
	var measureText = $("#sortMeasureDiv :selected").val();
	//var smartenModeEnable = document.getElementById("smartenRankToggle").value;
	
	var param = "srtDimension="+dimensionText+"&srtMeasure="+measureText+"&sortDirection="+sortDirection+"&addEditSortFlag="+false;
	ajaxSubmit("./applySmartenSort", 'smartenSortAjax',param, smartenrefreshData, true, true, true);//
});
</script>
