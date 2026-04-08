<%--
  - @(#)smartenSortObject.jsp Version: SmartenView <Feb 26, 2018>
  - Copyright 2015 Elegant MicroWeb Technologies Pvt. Ltd. (India). All Rights Reserved. Use is subject to license terms.
  - 
--%><%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div class="advance-filter-dropdown">                                               
<div id="manageSortDiv">
			<div class="col-lg-120">
				<div class="repository-table-section advance-filter-section">
					<table width="100%" cellspacing="0" cellpadding="0">
						<tbody role="alert" aria-live="polite" aria-relevant="all" class="action-box">
							<tr role="row">
								<%-- <th rowspan="1" colspan="1" aria-label="" class="table-width5">
									<input type="checkbox" class="checkall" id="checkall">
									<span class="lbl"></span>
								</th>
								<th rowspan="1" colspan="1" class="table-width11"><s:message code="LBL_NAME" /></th> --%>
								<th rowspan="1" colspan="1" class="table-width30"><s:message code="LBL_SMARTENVIEW_RANK_DIMENSION" /></th>
								<th rowspan="1" colspan="1" class="table-width30"><s:message code="SCORECARD_MEASURE" /></th>
								<th rowspan="1" colspan="1" class="table-width30"><s:message code="LBL_SORT_TYPE" /></th>
								<%-- <th rowspan="1" colspan="1" class="table-width9"><s:message code="LBL_STATUS_HEADER"/></th>
								<th rowspan="1" colspan="1" class="table-width15"><s:message code="LBL_CREATED_HEADER" /></th>
								<th rowspan="1" colspan="1" class="table-width15"><s:message code="LBL_UPDATED_HEADER" /></th> --%>
								<th rowspan="1" colspan="1" class="table-width10"><s:message code="" /></th>
							</tr>
							<c:if test="${!empty orderByInfo}">
							<c:forEach items="${orderByInfo}" var="sortInfo"
								varStatus="sIndex">
								<c:choose>
									<c:when test="${sortInfo.sortType ne 2}">
										<tr>
											<%-- <td><input type="checkbox" onclick="showActionButton(this, 'managesort-dialog');" id="${sIndex.index}" name="${sortInfo.sortName}">
												<span class="lbl"></span></td>
											<td>${sortInfo.sortName}</td> --%>
											<%-- <td id="sortColumn${sIndex.index}">${sortInfo.name}</td> --%>
											<td id="sortColumn${sIndex.index}">
												<!-- Sort Quick Setting: show alias for the sort dimension in the applied list. -->

    ${not empty colLabelsMap[sortInfo.name] ? colLabelsMap[sortInfo.name] : sortInfo.name}

</td>
											<%-- <td>${sortInfo.targetName}</td> --%>
											<td>
												<!-- Sort Quick Setting: show alias for the selected measure in the applied list. -->
												${not empty colLabelsMap[sortInfo.targetName] ? colLabelsMap[sortInfo.targetName] : sortInfo.targetName}
											</td>
											
											
											<c:choose>
												<c:when test="${sortInfo.sortType eq 1}">
													<td><s:message code="LBL_SORT_ADVANCED"/></td>
												</c:when>
												<c:when test="${sortInfo.sortType eq 2}">
													<td><s:message code="LBL_NONE"/></td>
												</c:when>
												<c:when test="${sortInfo.sortType eq 3}">
													<td><s:message code="LBL_CUSTOM_SORT"/></td>
												</c:when>
												<c:when test="${sortInfo.descOrder eq false}">
													<td><s:message code="LBL_ASCENDING_SORT"/></td>
												</c:when>
												<c:otherwise>
													<td><s:message code="LBL_SORT_REPOSITORY_DESENDING"/></td>
												</c:otherwise>
											</c:choose>
						   		 			<%-- <c:choose>
												<c:when test="${sortInfo.status}">
							     	 				<td><s:message code="LBL_ACTIVE"/></td>
							    				</c:when>
							     				<c:otherwise>
						       		 				<td><s:message code="LBL_INACTIVE"/></td>
						   		 				</c:otherwise>
						   		 			</c:choose>
											
											<td><span class="admin-name">${sortInfo.createdBy}</span><br>
												<span class="greay-text"><fmt:formatDate
														pattern="${dateFormat}" value="${sortInfo.createdDate}" /></span></td>
											<td><span class="admin-name">${sortInfo.updateBy}</span><br>
												<span class="greay-text"><fmt:formatDate
														pattern="${dateFormat}" value="${sortInfo.updatedDate}" /></span></td> --%>
											<td>
												<label><i class="icon-delete deletecurrent" title="Delete" onclick="javascript:removeSortSmarten('${sortInfo.name}');"></i></label>
											</td>
										</tr>
									</c:when>
								</c:choose>
							</c:forEach>
							</c:if>
						</tbody>
					
			</table>
				</div>
			</div>
		</div>
</div> 
