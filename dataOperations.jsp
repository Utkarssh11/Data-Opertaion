<%--
  - @(#)dataOperations.jsp Version: SmartenView <May 15, 2017>
  - Copyright 2015 Elegant MicroWeb Technologies Pvt. Ltd. (India). All Rights Reserved. Use is subject to license terms.
  - 
--%><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="com.elegantjbi.core.olap.ICubeConstants,com.elegantjbi.service.analysis.parts.ALSCommandNameList, com.elegantjbi.core.olap.ICubeResultSetSupport" %>

<script type="text/javascript">	
	$(document).ready(function() {
		var columnName ='';
		var param = '';
		<c:forEach items="${messureMap}" var="columnname" >
			if(columnName == '')
				columnName = '${columnname.key}';
			else
				columnName += ',' + '${columnname.key}';
		</c:forEach>
		$("#dataOperationsApply").click(function(){
			var value = '';
			var oForm = document.getElementById('frmDataOperationSmarten');//frmDataOperation
			if(columnName.indexOf(",") > -1 ) {
				var colArr = columnName.split(",");
				for(var i=0; i<colArr.length; i++) {
					
					if(value == "") {
						value = colArr[i] + "," + $("#"+colArr[i]).val();
					} else {
						value += "@$" + colArr[i] + "," + $("#"+colArr[i]).val();
					}
				}
				param = "value="+value;
				ajaxSubmit(contextUrl+"smartview/applyDataOperation", "data-operations", oForm, smartenrefreshData, true, true, true);
			} else {
				var dataoperationvalue = '';
				dataoperationvalue = $("#"+columnName).val();
				value = columnName + "," + dataoperationvalue;
				
				param = "value="+value;
				ajaxSubmit(contextUrl+"smartview/applyDataOperation", "data-operations", oForm, smartenrefreshData, true, true, true);
			}
			$('#data-operation-smarten').css("display","block !important");
		});
	});
	
	function showLmRecentButtonSmarten(columnNmae,value) {
		if(value == 'Least Recent Values' || value == 'Most Recent Values' || value == 'Distinct Count') {
			 $('#lmrecentbuttonsmarten'+columnNmae).show();
		} else {
			$('#lmrecentbuttonsmarten'+columnNmae).hide();
		}
	}
	function showLMRecentDivSmarten(columnName,operation,value) {
		operation = $('#'+columnName).val();
		
		var div1Class = $('#lmrecentdivsmarten'+columnName).attr('class');
		var div2Class = $('#distinctCountSmarten'+columnName).attr('class');
		if(value == "isOnchang") {
		if(div1Class == "row data-operation-section") {
			$('#lmrecentdivsmarten'+columnName).toggleClass('displaynone');
		}
		if(div2Class == "row data-operation-section") {
			$('#distinctCountSmarten'+columnName).toggleClass('displaynone');
		}
		
		$('#imageIcon'+columnName).removeClass("icon-chevron-down");
		$('#imageIcon'+columnName).addClass("icon-chevron-right");
		} else {
			if(operation == 'Distinct Count') {
				$('#distinctCountSmarten'+columnName).toggleClass('displaynone');
				$('#lmrecentdivsmarten'+columnName).addClass('displaynone');
			} else {
				$('#lmrecentdivsmarten'+columnName).toggleClass('displaynone');
				$('#distinctCountSmarten'+columnName).addClass('displaynone');
			}
			if($('#imageIcon'+columnName).attr('class') == "icon-chevron-right"){
				$('#imageIcon'+columnName).removeClass("icon-chevron-right");
				$('#imageIcon'+columnName).addClass("icon-chevron-down");
			} else {
				$('#imageIcon'+columnName).removeClass("icon-chevron-down");
				$('#imageIcon'+columnName).addClass("icon-chevron-right");
			}
		}
		if(value == "isOpen") {
			$('#imageIcon'+columnName).removeClass("icon-chevron-down");
			$('#imageIcon'+columnName).addClass("icon-chevron-right");
		}
	}
</script>
<form:form method="post" action="./applyDataOperation" role="form" id="frmDataOperationSmarten" name="frmDataOperationSmarten" >
	<%-- <div class="col-lg-60">
		<label class="addTab-label col-lg-120"><strong><s:message code="LBL_MEASURE"/></strong></label>
	</div>
	<label class="addTab-label col-lg-60 hide-only-iphone"><strong><s:message code="LBL_DATA_OPERATION"/></strong></label> --%>
	<div class="form-group">
		<div>
			<c:forEach items="${messureMap}" var="messureMap">
			<div class="input-group multi-state-drop" id="div_${messureMap.key}" style="min-width:30px; margin-left: 5%;margin-top: 0%;margin-right: 5%; width:90%;">
				<div style="margin-bottom: 1%;margin-top: 1%;">
				<div>${messureMap.key} </div>
				</div>
				<div>
				<select class="form-control input-sm" id="${messureMap.key}" name="${messureMap.key}" onchange="showLmRecentButtonSmarten('${messureMap.key}',this.value);showLMRecentDivSmarten('${messureMap.key}','${messureMap.value}','isOnchang')">
					<%-- <c:forEach items="${dataMap}" var="dataMap">
					<option value="${dataMap.value}" <c:if test="${fn:contains(messureMap.value, dataMap.value)}">selected</c:if>>${dataMap.key}</option>
					</c:forEach> --%>
					<c:forEach items="${selectedoperation[messureMap.key]}" var="dataMap">
						<option value="${dataMap.value}" <c:if test="${messureMap.value eq dataMap.value}">selected</c:if>>${dataMap.key}</option>
					</c:forEach>
				</select>
				</div>
		      <%-- <button type="button" id="lmrecentbutton" class="btn btn-blue" onclick="showLMRecentDialog('${messureMap.key}');"><s:message code="LBL_SETTINGS"/></button> --%>
			 <div id="lmrecentbuttonsmarten${messureMap.key}" class="data-operation-section-options">	
	          <a class="pull-right"  href="#" onclick="showLMRecentDivSmarten('${messureMap.key}','${messureMap.value}');"><i id="imageIcon${messureMap.key}" class="icon-chevron-right"></i></a>
	          </div>
			</div>
		
			<div class="row data-operation-section" id="lmrecentdivsmarten${messureMap.key}">
		<div class="col-lg-120 analysis-filter-dialog">
			<div class="form-group">
				<label class="addTab-label">Date Dimension</label>
				<select class="form-control input-sm" name="${messureMap.key}_timedimension" id="${messureMap.key}_timedimension">
					<c:forEach items="${datedimension}" var="datedimension" >
						<option value="${datedimension}" <c:if test="${datedimension == dateDimensions[messureMap.key]}">selected</c:if>>${datedimension}</option>
					</c:forEach>
				</select>
			</div>
			<div class="form-group">
				<label class="addTab-label">Aggregation</label>
				<select class="form-control input-sm" name="${messureMap.key}_agrrgation"  id="${messureMap.key}_agrrgation">				
				    <option value="<%=ICubeConstants.totalTypeNone%>" <c:if test="${-1 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_SUMMARY_NONE%></option>
				    <c:forEach items="${selectedpostoperation[messureMap.key]}" var="aggDataMap">
						<option value="${aggDataMap.value}" <c:if test="${fn:contains(agregationMap[messureMap.key],aggDataMap.value)}">selected</c:if>>${aggDataMap.key}</option>
					</c:forEach>
				</select>			
			</div>
			<div class="form-group">
				<label class="addTab-label">Post Aggregation</label>
				<select class="form-control input-sm" name="${messureMap.key}_postaggregation"  id="${messureMap.key}_postaggregation">
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_TOTAL%>" <c:if test="${'CMD_NAME_QT_TOTAL' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_NONE"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_PERCENT_ROW%>" <c:if test="${'CMD_NAME_QT_PERCENT_ROW' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_ROW_PERCENT"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_GROUP_PERCENT_ROW%>" <c:if test="${'CMD_NAME_QT_GROUP_PERCENT_ROW' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_ROW_GROUP_PERCENT"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_PERCENT_COL%>" <c:if test="${'CMD_NAME_QT_PERCENT_COL' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_COLUMN_PERCENT"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_GROUP_PERCENT_COL%>" <c:if test="${'CMD_NAME_QT_GROUP_PERCENT_COL' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_COLUMN_GROUP_PERCENT"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_PERCENT_ALL%>" <c:if test="${'CMD_NAME_QT_PERCENT_ALL' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_ALL_PERCENT"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_INCREASE%>" <c:if test="${'CMD_NAME_QT_INCREASE' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_RELATIVE_ROW_GROUP_DIFFERENCE"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_RATE%>" <c:if test="${'CMD_NAME_QT_RATE' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_RELATIVE_ROW_GROUP_DIFFERENCE_PERCENT"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_REL_COL_DIFF%>" <c:if test="${'CMD_NAME_QT_REL_COL_DIFF' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_RELATIVE_COLUMN_GROUP_DIFFERENCE"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_REL_COL_DIFF_PERCENT%>" <c:if test="${'CMD_NAME_QT_REL_COL_DIFF_PERCENT' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_RELATIVE_COLUMN_GROUP_DIFFERENCE_PERCENT"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_ROW_CUMULATIVE_SUM%>" <c:if test="${'CMD_NAME_QT_ROW_CUMULATIVE_SUM' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_ROW_GROUP_CUMULATIVE_SUM"/></option>
					<option value="<%=ALSCommandNameList.CMD_NAME_QT_COL_CUMULATIVE_SUM%>" <c:if test="${'CMD_NAME_QT_COL_CUMULATIVE_SUM' == postaggregationMap[messureMap.key]}">selected</c:if>><s:message code="LBL_COLUMN_GROUP_CUMULATIVE_SUM"/></option>								    
				</select>			
			</div>
		</div>
	</div>
		<div class="row data-operation-section" id="distinctCountSmarten${messureMap.key}">
		<div class="col-lg-120 analysis-filter-dialog">
			<div class="form-group">
				<label class="addTab-label"><s:message code="LBL_DIMENSION"/></label>
				<select class="form-control input-sm" name="${messureMap.key}_distinctCountdimension" id="${messureMap.key}_distinctCountdimension">
					<c:forEach items="${dimension}" var="dimension" >
						<option value="${dimension}" <c:if test="${dimension == distinctCountMap[messureMap.key]}">selected</c:if>>${dimension}</option>
					</c:forEach>
				</select>
			</div>
			</div>
		</div>
	<script type="text/javascript" >
		showLmRecentButtonSmarten("${messureMap.key}","${messureMap.value}");
		showLMRecentDivSmarten("${messureMap.key}","${messureMap.key}","isOpen");
	</script>
	</c:forEach>
	</div>
</div>
<button style="position: relative;margin-left: 5%;" id="dataOperationsApply" class="btn btn-blue apply-but" type="button">
														<s:message code="LBL_APPLY" /></button>
</form:form>