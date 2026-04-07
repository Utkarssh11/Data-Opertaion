<%--
  - @(#)dataOperation.jsp Version: 4.0 <May 12, 2014>
  - Copyright 2015 Elegant MicroWeb Technologies Pvt. Ltd. (India). All Rights Reserved. Use is subject to license terms.
  - 
--%><%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="com.elegantjbi.core.olap.ICubeConstants,com.elegantjbi.service.analysis.parts.ALSCommandNameList, com.elegantjbi.core.olap.ICubeResultSetSupport" %>
<c:set var="countOpr" value="<%=ICubeConstants.COUNT%>"/>
<c:set var="sumOpr" value="<%=ICubeConstants.SUM%>"/>
<c:set var="avgOpr" value="<%=ICubeConstants.AVERAGE%>"/>
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
		$("#data-operations #dlgSubmit").click(function(){
			var value = '';
			var oForm = document.getElementById('frmDataOperation');
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
				if('${isFromMap}' == "true") {
				 ajaxSubmit(contextUrl+"map/applyDataOperation", "data-operations", oForm, refreshData, true, true, true);
				} else {
					if('${isFromSmarten}' == 'true')
					{
						ajaxSubmit(contextUrl+"smartview/applyDataOperation", "data-operations", oForm, smartenrefreshData, true, true, true);
					}
					else
					{
				  		ajaxSubmit(contextUrl+"graph/applyDataOperation", "data-operations", oForm, refreshData, true, true, true);
					}
				}
			} else {
				var dataoperationvalue = '';
				dataoperationvalue = $("#"+columnName).val();
				value = columnName + "," + dataoperationvalue;
				
				param = "value="+value;
				if('${isFromMap}' == "true") {
					ajaxSubmit(contextUrl+"map/applyDataOperation", "data-operations", oForm, refreshData, true, true, true);
				} else {
					if('${isFromSmarten}' == 'true')
					{
						ajaxSubmit(contextUrl+"smartview/applyDataOperation", "data-operations", oForm, smartenrefreshData, true, true, true);
					}
					else
					{
						ajaxSubmit(contextUrl+"graph/applyDataOperation", "data-operations", oForm, refreshData, true, true, true);
					}
				}
			}
			
		});
	});
	
	function showLmRecentButton(columnNmae,value) {
		if(value == 102 || value == 101 || value == 103) {
			 $('#lmrecentbutton'+columnNmae).show();
		} else {
			$('#lmrecentbutton'+columnNmae).hide();
		}
	}
	function showLMRecentDiv(columnName,operation,value) {
		operation = $('#'+columnName).val();
		
		var div1Class = $('#lmrecentdiv'+columnName).attr('class');
		var div2Class = $('#distinctCount'+columnName).attr('class');
		if(value == "isOnchang") {
		if(div1Class == "row data-operation-section") {
			$('#lmrecentdiv'+columnName).toggleClass('displaynone');
		}
		if(div2Class == "row data-operation-section") {
			$('#distinctCount'+columnName).toggleClass('displaynone');
		}
		
		$('#imageIcon'+columnName).removeClass("icon-chevron-down");
		$('#imageIcon'+columnName).addClass("icon-chevron-right");
		} else {
			if(operation == '103') {
				$('#distinctCount'+columnName).toggleClass('displaynone');
				$('#lmrecentdiv'+columnName).addClass('displaynone');
			} else {
				$('#lmrecentdiv'+columnName).toggleClass('displaynone');
				$('#distinctCount'+columnName).addClass('displaynone');
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
<form:form method="post" action="./applyDataOperation" role="form" id="frmDataOperation" name="frmDataOperation" >
	<div class="col-lg-60">
		<label class="addTab-label col-lg-120"><s:message code="LBL_MEASURE"/></label>
	</div>
	<label class="addTab-label col-lg-60 hide-only-iphone"><s:message code="LBL_DATA_OPERATION"/></label>
	<div class="form-group">
		<div class="retrieval-parameters-box">
			<c:forEach items="${messureMap}" var="messureMap">
			<div class="input-group multi-state-drop" id="div_${messureMap.key}">
			
			<!-- old -->
				<%-- <span class="input-group-addon">${messureMap.key} </span> --%
            <%-- new --%>
				<span class="input-group-addon">${not empty messureDisplayMap[messureMap.key] ? messureDisplayMap[messureMap.key] : messureMap.key}</span>
				
				<select class="form-control input-sm" id="${messureMap.key}" name="${messureMap.key}" onchange="showLmRecentButton('${messureMap.key}',this.value);showLMRecentDiv('${messureMap.key}','${messureMap.value}','isOnchang')">
				<%--  <c:forEach items="${dataMap}" var="dataMap">
					<option value="${dataMap.value}" <c:if test="${fn:contains(messureMap.value, dataMap.value)}">selected</c:if>>${dataMap.key}</option>
					</c:forEach> --%>
					  <c:forEach items="${selectedoperation[messureMap.key]}" var="dataMap">
						<option value="${dataMap.value}" <c:if test="${messureMap.value == dataMap.value}">selected</c:if>>${dataMap.key}</option>
					</c:forEach> 
				</select>
		      <%-- <button type="button" id="lmrecentbutton" class="btn btn-blue" onclick="showLMRecentDialog('${messureMap.key}');"><s:message code="LBL_SETTINGS"/></button> --%>
			 <div id="lmrecentbutton${messureMap.key}" class="data-operation-section-options">	
	          <a class="pull-right"  href="#" onclick="showLMRecentDiv('${messureMap.key}','${messureMap.value}');"><i id="imageIcon${messureMap.key}" class="icon-chevron-right"></i></a>
	          </div>
			</div>
		
			<div class="row data-operation-section" id="lmrecentdiv${messureMap.key}">
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
						<option value="${aggDataMap.value}" <c:if test="${agregationMap[messureMap.key] == aggDataMap.value}">selected</c:if>>${aggDataMap.key}</option>
					</c:forEach>
				    <%-- <option value="<%=ICubeConstants.totalTypeSum%>" <c:if test="${0 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_TOTAL%></option>
				    <option value="<%=ICubeConstants.totalTypeAve%>" <c:if test="${1 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_AVERAGE%></option>
				    <option value="<%=ICubeConstants.totalTypeAve2%>" <c:if test="${2 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_VALID_AVERAGE%></option>
				    <option value="<%=ICubeConstants.totalTypeCount%>" <c:if test="${3 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_COUNT%></option>
				    <option value="<%=ICubeConstants.totalTypeCount2%>" <c:if test="${4 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_VALID_COUNT%></option>
				    <option value="<%=ICubeConstants.totalTypeMax%>" <c:if test="${6 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_MAX%></option>
				    <option value="<%=ICubeConstants.totalTypeMin%>" <c:if test="${7 == agregationMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_MIN%></option> --%>
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
		<div class="row data-operation-section" id="distinctCount${messureMap.key}">
		<div class="col-lg-120 analysis-filter-dialog">
			<div class="form-group">
				<label class="addTab-label"><s:message code="LBL_DIMENSION"/></label>
				<select class="form-control input-sm" name="${messureMap.key}_distinctCountdimension" id="${messureMap.key}_distinctCountdimension">
					<c:forEach items="${dimension}" var="dimension" >
					
					<!-- old -->
						<%-- <option value="${dimension}" <c:if test="${dimension == distinctCountMap[messureMap.key]}">selected</c:if>>${dimension}</option> --%>
						<!-- new -->
						<option value="${dimension}" <c:if test="${dimension == distinctCountMap[messureMap.key]}">selected</c:if>>${not empty dimensionDisplayMap[dimension] ? dimensionDisplayMap[dimension] : dimension}
						</option>
					</c:forEach>
				</select>
			</div>
			<div class="form-group">
				<label class="addTab-label"><s:message code="LBL_AGGREGATION"/></label>
				<select class="form-control input-sm" name="${messureMap.key}_agrrgation_val" id="${messureMap.key}_agrrgation_val">
					<option value="${countOpr}" <c:if test="${countOpr == distinctOprMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_COUNT%></option>
				    <option value="${sumOpr}" <c:if test="${sumOpr == distinctOprMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_TOTAL%></option>
				    <option value="${avgOpr}" <c:if test="${avgOpr == distinctOprMap[messureMap.key]}">selected</c:if>><%=ALSCommandNameList.VIEW_NAME_QT_AVERAGE%></option>
				</select>
			</div>
			</div>
		</div>
	<script type="text/javascript" >
	showLmRecentButton("${messureMap.key}","${messureMap.value}");
	showLMRecentDiv("${messureMap.key}","${messureMap.key}","isOpen");
	</script>
		</c:forEach>
		</div>
	</div>
</form:form>

<script type="text/javascript">

function showLMRecentDialog(columnName) {
	var param = "columnName="+columnName+"&operationtype="+$("#"+columnName).val();
	$("#data-operations").modal('hide');
	ajaxcall('./lmrecentdataoperation', 'm_21_28_dialog',param);
}

</script>