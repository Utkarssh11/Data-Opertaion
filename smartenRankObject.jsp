<%--
  - @(#)smartenRankObject.jsp Version: SmartenView <Feb 25, 2018>
  - Copyright 2015 Elegant MicroWeb Technologies Pvt. Ltd. (India). All Rights Reserved. Use is subject to license terms.
  - 
--%><%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div class="advance-filter-dropdown">                                               
<div id="manageRankDiv">
			<div class="col-lg-120">
				<div class="repository-table-section advance-filter-section">
				<%-- <c:if test="${!empty rankList}"> --%>
					<table style="width:100%;" cellspacing="0" cellpadding="0">
						<%-- <c:choose>
							<c:when test="${fn:length(rankList) gt 0}"> --%>
								
								<tbody role="alert" aria-live="polite" aria-relevant="all" class="action-box">
									<tr role="row">
										<%-- <th rowspan="1" colspan="1" aria-label="" class="table-width5">
											<input type="checkbox" class="checkall" id="checkall">
											<span class="lbl"></span>
										</th> --%>
										<%-- <th rowspan="1" colspan="1" class="table-width15"><s:message
												code="LBL_NAME" /></th> --%>
										<th rowspan="1" colspan="1" class="table-width30"><s:message
												code="LBL_RANK_DIMENSION" /></th>
										<th rowspan="1" colspan="1" class="table-width30"><s:message
												code="SCORECARD_MEASURE" /></th>
										<th rowspan="1" colspan="1" class="table-width30"><s:message
												code="LBL_RANK_CRITERIA" /></th>
										<th rowspan="1" colspan="1" class="table-width10"><s:message
												code="" /></th>
										<%-- <th rowspan="1" colspan="1" class="table-width9"><s:message 
												code="LBL_STATUS_HEADER"/></th>	
										<th rowspan="1" colspan="1" class="table-width15"><s:message
												code="LBL_CREATED_HEADER" /></th>
										<th rowspan="1" colspan="1" class="table-width15"><s:message
												code="LBL_UPDATED_HEADER" /></th> --%>
									</tr>
									<c:if test="${!empty rankList}">
									<c:forEach items="${rankList}" var="rankList" varStatus="row">
										<tr>
											<%-- <td><input type="checkbox" onclick="showActionButton(this, 'managerank-dialog');" id="${row.index }" name="${rankList.rankName}"> <span
												class="lbl"></span></td> --%>
											<%-- <td><c:out value="${rankList.rankName}" /></td> --%>
		  
		                                       <!-- old -->
											<%-- <td id="rankColumnName${row.index }"><c:out
													value="${rankList.columnName}" /></td> --%>
													
												<!-- Top-bottom after Add Rank Dimension >Apply -->	
												<td id="rankColumnName${row.index }">
                                                ${not empty colLabelsMap[rankList.columnName] ? colLabelsMap[rankList.columnName] : rankList.columnName}
                                                </td>
													
											<!-- old  for Measure-->		
											<%-- <td><c:out value="${rankList.dataColumn}" /></td> --%>
											
											<td>
                                            ${not empty colLabelsMap[rankList.dataColumn] ? colLabelsMap[rankList.dataColumn] : rankList.dataColumn}
                                           </td>
											
											<c:choose>
												<c:when test="${rankList.rankType eq 0}">
													<td><s:message	code="LBL_TOP" /></td>
												</c:when>
												<c:otherwise>
													<td><s:message	code="LBL_BOTTOM" /></td>
												</c:otherwise>
											</c:choose>
											<%-- <c:choose>
												<c:when test="${rankList.status}">
									     			<td><s:message code="LBL_ACTIVE"/></td>
									    		</c:when>
									     		<c:otherwise>
								       				<td><s:message code="LBL_INACTIVE"/></td>
								   		 		</c:otherwise>
								   		 	</c:choose>
		
											<td><span class="admin-name"><c:out
														value="${rankList.createBy}" /></span><br> <span
												class="greay-text"><fmt:formatDate
														pattern="${dateFormat}" value="${rankList.createdDate}" /></span></td>
											<td><span class="admin-name"><c:out
														value="${rankList.updatedBy}" /></span><br> <span
												class="greay-text"><fmt:formatDate
														pattern="${dateFormat}" value="${rankList.modifiedDate}" /></span></td> --%>
											<td>
												<label><i class="icon-delete deletecurrent" title="Delete" onclick="javascript:deleteSmartenRank('${rankList.columnName}');"></i></label>
											</td>
										</tr>
									</c:forEach>
									</c:if>
								</tbody>
							<%-- </c:when>
							<c:otherwise>
								<div class="data-not-found"><s:message code="LBL_NO_RECORD_FOUND"/></div>
					       </c:otherwise>
						</c:choose> --%>
					</table>
					<%-- </c:if> --%>
				</div>
			</div>
		</div>
</div> 