<%--
  - @(#)smartenRankSort.jsp Version: SmartenView <June 27, 2017>
  - Copyright 2015 Elegant MicroWeb Technologies Pvt. Ltd. (India). All Rights Reserved. Use is subject to license terms.
  - 
--%>
<%@page import="com.elegantjbi.service.dashboard.DashboardConstants"%>
<%@page import="com.elegantjbi.service.graph.GraphConstants"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="RANK_BUTTON_POSITION_TOP" value="<%=DashboardConstants.RANK_BUTTON_POSITION_TOP%>"/>
<c:set var="RANK_BUTTON_POSITION_BOTTOM" value="<%=DashboardConstants.RANK_BUTTON_POSITION_BOTTOM%>"/>

<div id="rankSmartenParent">
	<div id="rankParentDiv">
	<div style="text-align: right; margin-left: 5%;margin-right: 5%;">
		 <div style="display:inline;" id="smartenRankToggle"><input id="toggle-two" data-on="Top" data-off="Bottom" checked data-toggle="toggle" data-size="small" data-width="70" data-height="30" type="checkbox">
			 <script>
				  $(function() {
				    $('#toggle-two').bootstrapToggle({
				    	on: 'Top',
				        off: 'Bottom'
				    });
				  })
				</script>
		</div>
	</div>
	
	<div id="rank" class="rank-control rendered">
	<c:choose>
		<c:when test="${ranktype == 'Dropdown'}">
			<div class="col-lg-120">
				<select style="margin-left: 5%;margin-top: 0%;margin-right: 5%; width:95% !important;" class="form-control input-sm" id="rankdrop1" onchange="javascript:onChangeSelection1('','1');">
					<c:forEach var="i" begin="0" end="10" step="1">
						<option value="${i}">${i}</option>
					</c:forEach>					
				</select>
			</div>
			<div class="clearfix"></div>			
		</c:when>
		<c:otherwise>
			<div class="rank-slider-plugin" style="margin-left: 2%;margin-top: 0%;margin-right: 5%;margin-bottom: 5%; width:95% !important;">
				<div id="slider1"></div>
			</div>			 			
		</c:otherwise>		
	</c:choose>
	</div>
	
		<%-- <div class="top-bottom-rank">
			<div id="RefrashableObjects" class="displaynone">
		 	refreshable object map info
				<c:if test="${filterObjectList.size() gt 0}">
					<c:forEach var="objData" items="${filterObjectList}" varStatus="count1" >
						${objData}<br>
					</c:forEach>
				</c:if>
			 </div>
		 
			 <div id="RankRefrashableObjects" class="displaynone">
		 	refreshable object map info
				<c:if test="${rankRefreshList.size() gt 0}">
					<c:forEach var="rankId" items="${rankRefreshList}" varStatus="count1" >
						${rankId}<br>
					</c:forEach>
				</c:if>
			 </div>  
	 
			<input type="hidden" id="ranklevel1" value="top"/>
			<input type="hidden" id="rankvalue1" value="${rankvalue}"/>
			<div class="btn-group 1scale-rank-toggle" style="width: 100%;text-align: center;">
					<div class="btn" id='top1'>Top</div>
					<div class="btn" id='bottom1'>Bottom</div>	
			</div>
		</div> --%>
		
		<div>
			<strong style="margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;"><s:message code="LBL_SMARTENVIEW_RANK_DIMENSION" /></strong>
		</div>
		<div id="rankDimensionDiv">
			
		</div>
		<div>
			<strong style="margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;"><s:message code="LBL_ON_TEXT" /></strong>
		</div>
		<div id="rankMeasureDiv">
			
		</div>
		<button style="position: relative; margin: 5%;" id="smartenRankOk" class="btn btn-blue apply-but" type="button">
			<s:message code="LBL_ADDOBJECT" />
		</button>
		
		<!-- New Code Start -->
			<div id="smartenRankPropobjects">
				<jsp:include page="/jsp/smartview/smartenRankObject.jsp"/>
			</div>
		<!-- New Code End -->
		
		<button style="position: relative; margin: 5%;" id="smartenRankApply" class="btn btn-blue apply-but" type="button">
			<s:message code="LBL_APPLY" />
		</button>
		</div>
		</div>
	
<script>
$(".popupdialog-scroll-205").jScrollPane({autoReinitialise: true,showArrows: true,mouseWheelSpeed: 25, resize: true});
var ranktype=0;
	var rnkLimit = '${allRankList[0]}';
 	if($('#slider1')) {
 		if (!$('#slider1').is('.rendered')){
 	 		$('#slider1').labeledslider({
				value: parseFloat("2"),
				min : parseFloat("0"),
				max : parseFloat("10"),
				tickInterval : parseFloat("1"),
				slide: function ( e, ui ) {																
					var newList = new Array();
					newList.push(ui.value);
					if($("#slider1 > a").has( "p"))
					{
						$("#slider1 > a > p").remove();
					}
				//	$("#slider1 > a").append("<p>"+newList[0]+"</p>");	
					
					var curValue = newList[0];
					rnkLimit =  newList[0];
				    var tooltip = '<div class="tooltipSlider"><div class="tooltip-inner">' + curValue + '</div><div class="tooltip-arrow"></div></div>';
				    $('#slider1 .ui-slider-handle').html(tooltip);
				
				},
				change : function(event, ui) {					
					$("#slider1").val(ui.value);
					$("#rankvalue1").val(ui.value);

					if(!$("#top1").hasClass('active-ts') && !$("#bottom1").hasClass('active-ts')) {
						$("#top1").addClass('active-ts');
					} 
					//ajaxcall('./applyrank', '','rankvalue='+ui.value+'&ranklevel='+$("#ranklevel1").val()+'&divId=${divId}','refreshObjectDiv()',false,false,false);
					onChangeSelection1(ui.value,'1');
				}				
			});
 	 		$('#slider1').addClass('rendered');
 		}
 	}

	//Rank - Top Bottom Toggle Button
	/* $(document).ready(function() { */
		$('.1scale-rank-toggle .btn').click(function(e) {
			$('.1scale-rank-toggle div').each(function() {
				$(this).removeClass('active-ts');
			});
			
			if($(this).attr('id').indexOf('bottom') > -1){
				$("#ranklevel1").val('bottom');
			} else { 
				$("#ranklevel1").val('top');
			}
			$(this).addClass('active-ts');

			if(/* $("#rankvalue1").val() != '0' &&  */'${toprank}' == 'true' && '${bottomrank}' == 'true') {
				onChangeSelection1($("#rankvalue1").val(), '1');
			}
		});

		if('${ranklevel}' == 'top') {
			$("#top1").addClass('active-ts');
			$("#ranklevel1").val('${ranklevel}');
			if($("#bottom1").hasClass('active-ts'))
				$("#bottom1").removeClass('active-ts');				
		} else if('${ranklevel}' == 'bottom') {
			$("#ranklevel1").val('${ranklevel}');
			$("#bottom1").addClass('active-ts');
			if($("#top1").hasClass('active-ts'))
				$("#top1").removeClass('active-ts');				
		} else {
			if(('${toprank}' == 'true' && '${bottomrank}' == 'true') || '${toprank}' == 'true') {
				$("#ranklevel1").val('top');
				$("#top1").addClass('active-ts');
			} else if('${bottomrank}' == 'true') {
				$("#ranklevel1").val('bottom');
				$("#bottom1").addClass('active-ts');
			}
		}

		 if('${rankvalue}' == 0){
			$('.1scale-rank-toggle div').each(function() {
				$("#top1").removeClass('active-ts');
				$("#bottom1").removeClass('active-ts');
			});
		} 
		
		/* if($('#slider1') && parseInt('${rankvalue}') > 0) {
			$("#slider1").val('${rankvalue}');
			$("#slider1").slider('value',1);
		}  */
		if($("#rankdrop1") && parseInt('${rankvalue}') > 0){
			$("#rankdrop1").val('${rankvalue}');
		}
/* 	}); */
	var tooltip = '<div class="tooltipSlider"><div class="tooltip-inner"> ${allRankList[0]}</div><div class="tooltip-arrow"></div></div>';
	$('#slider1 .ui-slider-handle').html(tooltip);
											 	 	
function onChangeSelection1(rankvalue, id) {
		var addClass = false;
		$('.1scale-rank-toggle div').each(function() {
			if($(this).hasClass('active-ts')){
				addClass = true;
			}
		});
		
		if(addClass == false){
			if('${toprank}' == 'true'){
				$("#top1").addClass('active-ts');
			} else {
				$("#bottom1").addClass('active-ts');
			}
		}
		if((!(isiphone())) && (!isIpad())) {
			applyRankForDashboard1(rankvalue, id);
		}
	}

	function onChangeSelectionFromIphone1(rankvalue, id) {
		rankvalue = $("#rankvalue1").val();
		applyRankForDashboard1(rankvalue, id);		
	}

	function applyRankForDashboard1(rankvalue, id){
		
		var rankType = '${ranktype}';
		if(rankType == 'Dropdown') {
			rankvalue = $('#rankdrop1').val();
		}
		var time = new Date().getTime()+"";
		$('#1').attr('data-isClicked-time',time);
		$("#rankvalue1").val(rankvalue);
		rankId = 'rank-'+id;
		var param = 'rankvalue='+rankvalue+'&ranklevel='+$("#ranklevel1").val()+'&divId='+rankId;
		/* ajaxcall('./applyrank', '',param,'refreshObjectDiv("${divId}","","'+time+'")',false,false,false); */
		//ajaxcall('./applyrank', '',param,'',false,false,false); // As No Need To Refresh Explicitly
		//cancleActionForFilterSectionInIphone();
	}
	
	$(document).ready(function() {
		 //$("#rankParentDiv").height($('#smartenRank').height() - 30);
		loadRankMeasure();
		var baseId = "rankSmartenParent";
		var height = $("#smartenRank").height() - 30; 
		setSmartenScroll(baseId,height);
	});

	function loadRankMeasure()
	{
		// Rank Quick Setting: map original column names to aliases for readable dropdown labels.
		var aliasMap = {};
		<c:forEach items="${colLabelsAliasMap}" var="entry">
			aliasMap["${entry.key}"] = "${entry.value}";
		</c:forEach>
		
		//For dimensions
		var allDimension = '${allDimension}';
		var array = allDimension.split(',');
		var item;
		for (var i = 0, size = array.length; i < size; i++) {
			item = array[i];

		}
		var newDiv = document.getElementById("rankDimensionDiv");
		var selectHTML = "";
		selectHTML = "<select class='form-control input-sm' style='margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;' id='smartenRankDimension'>";
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
		
		// Top/Bottom Quick Setting: update rank limit and toggle state when dimension selection changes.
		 $('#smartenRankDimension').change(function(){
		 
			var selectedIndex = document.getElementById("smartenRankDimension").selectedIndex;
			var allRankArr = [ 
      			<c:forEach var="rank" items="${allRankList}">
      			   <c:out value="${rank}"/>,
      			</c:forEach>
     		];
			 var allSortArr = [ 
	      			<c:forEach var="sort" items="${allSortList}">
	      			   <c:out value="${sort}"/>,
	      			</c:forEach>
	     		];
			var rankValueSmarten = allRankArr[selectedIndex];
			rnkLimit = rankValueSmarten; 
			$('#slider1 .ui-slider-handle').css('left', rankValueSmarten+"0%");
				
			var isSort = allSortArr[selectedIndex];
			if(isSort == 'true' || isSort == true)
				$('#toggle-two').bootstrapToggle('off');
			else
				$('#toggle-two').bootstrapToggle('on');
				
			
						var tooltip = '<div class="tooltipSlider"><div class="tooltip-inner">'+ rankValueSmarten+'</div><div class="tooltip-arrow"></div></div>';
				$('#slider1 .ui-slider-handle').html(tooltip);
		 });
		// End dimension-change handler for Top/Bottom Quick Setting.
		
		//For dimensions end
		
		//For measures
		var allMeasure = '${allMeasures}';
		var array = allMeasure.split(',');
		var item;
		for (var i = 0, size = array.length; i < size; i++) {
			item = array[i];

		}
		var newDiv = document.getElementById("rankMeasureDiv");
		var selectHTML = "";
		selectHTML = "<select class='form-control input-sm' style='margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;' id='smartenRankMeasures'>";
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
		//For measures end
		
		//for slider position single dim
		var rankValueSmarten = '${allRankList[0]}';
		$('#slider1 .ui-slider-handle').css('left', rankValueSmarten+"0%");
		//for slider position
		
		// Top/Bottom Quick Setting: initialize toggle state from the selected dimension's saved sort direction.
		var isSort = '${allSortList[0]}';
		if(isSort == 'true' || isSort == true) 
			$('#toggle-two').bootstrapToggle('off');
		else
			$('#toggle-two').bootstrapToggle('on');
		// End Top/Bottom toggle initialization.
	}

	$("#toggle-two").change(function(){
	    if($(this).prop("checked") == true){
	    	ranktype=0;
	    }else{
	    	ranktype=1;
	    }
	});
	
	var delCount = 0;
	
	//New Delete Code start
	/**
	 * Below Function is used for Remove Selected Analysis-Rank(s).
	 */
	function deleteSmartenRank(rankListColumnName) {
		 var modeEnable = document.getElementById("smartenModeEnable").value;//Added code to maintain Smarten Mode after Rank apply
		 var param = "templateId=" + rankListColumnName+"&smartenMEnable="+modeEnable;
		 delCount++;
		ajaxSubmit("./deleteSmartenRank", "manageRankDiv", param,myFunction());
	}
	//New Delete Code end
	
	$("#smartenRankOk").click(function(){
		/* var dimensionText = $("#rankDimensionDiv :selected").text();
		var measureText = $("#rankMeasureDiv :selected").text(); */
		
		var dimensionText = $("#rankDimensionDiv :selected").val();
		var measureText = $("#rankMeasureDiv :selected").val();
		
		var modeEnable = document.getElementById("smartenModeEnable").value;//Added code to maintain Smarten Mode after Rank apply
		var param = "rnkDimension="+dimensionText+"&rnkMeasure="+measureText+"&rnkType="+ranktype+"&rnkLimit="+rnkLimit+"&addEditRankFlag="+"true"+"&smartenMEnable="+modeEnable;
		ajaxcall("./saveSmartenRank", 'manageRankDiv',param, 'showRankListSmarten()', true, true, true);//smartenrefreshData
	});
	
	function showRankListSmarten(response)
	{
		ajaxcall("./rankSmarten?objectType="+1, "manageRankDiv",'','');
	}
	
	function myFunction() {
	    setTimeout(function(){ showRankListSmarten() }, 100);
	}
	
	//
	//-------------------------sampling var in outliner---------------------------//	
		document.getElementById("outlinerPaginationCB").value = '${paginationCB}' 
		document.getElementById("outlinerSamplingCB").value = '${samplingCB}';
		document.getElementById("outlinerSnapshotCB").value = '${snapShotCB}'
	//-------------------------sampling var in outliner---------------------------//
	//This function is called to validateSampling
	function validateSamplingOnRankApply(graphType,param,paginationCB,samplingCB,snapShotCB)
	{
		var isD3PieRadar = false;
		var isTabular = false;
		var isAmchart = false;
		var mainResultSetCount = '${mainResultSetCount}';
		mainResultSetCount = parseInt(mainResultSetCount);
		var showConfirmatioinBox = false;
		var pagination  = '${paginationCB}';
		var sampling = '${samplingCB}';
		var snapShot = '${snapShotCB}';
		var checkboxToBeActivated = "ok";
		var maxCount = 300000;
		
		if(graphType == 61 || graphType == 62 || graphType == 63 || graphType == 64 || graphType == 65 || graphType == 14 || graphType == 15 || graphType == 6)
			isD3PieRadar = true;
		else if(graphType == 70)
			isTabular = true;
		else
			isAmchart = true;
		
		/* if(isD3PieRadar == true) */
				maxCount = getSamplingCountSmarten(graphType);
		if(isAmchart == true)
		{
			if(sampling == 'true' && pagination == 'false' && snapShot == 'false' && mainResultSetCount > maxCount)
			{
				showConfirmatioinBox = true;
				checkboxToBeActivated = "pagination";
			}
			if(sampling == 'false' && pagination == 'true' && snapShot == 'false' && mainResultSetCount > maxCount)
			{
				showConfirmatioinBox = true;
				checkboxToBeActivated = "sampling";
			}
			if(sampling == 'false' && pagination == 'false' && snapShot == 'false' && mainResultSetCount > maxCount)
			{
				showConfirmatioinBox = true;
				checkboxToBeActivated = "pagination&sampling";
			}
			
		}
		if(isD3PieRadar == true && snapShot == 'false' && mainResultSetCount > maxCount)
		{
			showConfirmatioinBox = true;
			checkboxToBeActivated = "snapShot";
		}
		if(isTabular == true)
		{
				if(sampling == 'false' && mainResultSetCount > maxCount)
				{
					checkboxToBeActivated = "sampling";
					showConfirmatioinBox = true;
				}
				if(pagination == 'false' && mainResultSetCount > 1000)
				{
					checkboxToBeActivated = "pagination";
					showConfirmatioinBox = true;
				}
				if(pagination == 'false' && sampling == 'false' && mainResultSetCount > maxCount)
				{
					showConfirmatioinBox = true;
					checkboxToBeActivated = "pagination&sampling";
				}
		}
		if(showConfirmatioinBox == true)
		{
			smartenSettingsConfirmDialog("manipulateCheckboxForRank(\""+checkboxToBeActivated+"\",\""+param+"\",\""+paginationCB+"\",\""+samplingCB+"\",\""+snapShotCB+"\")","confirmCancel()",
			"The number of data points is very high. Processing may take time or may not render meaningful display. Do you want to enable default settings?");
		}
		else
		{
			var modeEnable = document.getElementById("smartenModeEnable").value;//Added code to maintain Smarten Mode after Rank apply
			param += "&paginationCB="+paginationCB+"&samplingCB="+samplingCB+"&snapShotCB="+snapShotCB+"&smartenMEnable="+modeEnable;
			ajaxSubmit("./applySmartenRank", 'smartenRankAjax',param, smartenrefreshData, true, true, true);
		}
		
	}
	
	function manipulateCheckboxForRank(checkboxToBeActivated,param,paginationCB,samplingCB,snapShotCB)
	{
		//These var are in outliner jsp and would set on setOutliner in controller 
		if(checkboxToBeActivated == "snapShot")
		{
			snapShotCB = true;
		}
		if(checkboxToBeActivated == "sampling")
		{	
			samplingCB = true;
		}
		if(checkboxToBeActivated == "pagination")
		{
			paginationCB = true;
		}
		if(checkboxToBeActivated == "pagination&sampling")//pagination&sampling
		{
			paginationCB = true;
			samplingCB = true;
		}
		var modeEnable = document.getElementById("smartenModeEnable").value;//Added code to maintain Smarten Mode after Rank apply
		param += "&paginationCB="+paginationCB+"&samplingCB="+samplingCB+"&snapShotCB="+snapShotCB+"&smartenMEnable="+modeEnable;
		ajaxSubmit("./applySmartenRank", 'smartenRankAjax',param, smartenrefreshData, true, true, true);
	}
	//
	
	$("#smartenRankApply").click(function(){
		/* var dimensionText = $("#rankDimensionDiv :selected").text();
		var measureText = $("#rankMeasureDiv :selected").text(); */
		
		var dimensionText = $("#rankDimensionDiv :selected").val();
		var measureText = $("#rankMeasureDiv :selected").val();
		
		var smartenModeEnable = document.getElementById("smartenRankToggle").value;
		if(dimensionText == "" ||  measureText == "" ){
			_showInfoPage("Please select dimensions for Rank functionality" );
		}
		else {
			var paginationCB = '${paginationCB}';
			var samplingCB = '${samplingCB}';
			var snapShotCB = '${snapShotCB}';
			
			var param = "rnkDimension="+dimensionText+"&rnkMeasure="+measureText+"&rnkType="+ranktype+"&rnkLimit="+rnkLimit+"&addEditRankFlag="+"true";//+"&paginationCB="+paginationCB+"&samplingCB="+samplingCB+"&snapShotCB="+snapShotCB;
			if(document.getElementById("graphType") != null && delCount > 0)
			 	validateSamplingOnRankApply(document.getElementById("graphType").value,param,paginationCB,samplingCB,snapShotCB);
			else
			{
				var modeEnable = document.getElementById("smartenModeEnable").value;//Added code for save Point 5 Task #13184
				param += "&paginationCB="+paginationCB+"&samplingCB="+samplingCB+"&snapShotCB="+snapShotCB+"&smartenMEnable="+modeEnable;
				ajaxSubmit("./applySmartenRank", 'smartenRankAjax',param, smartenrefreshData, true, true, true);//
			}
		}
	});
</script>
