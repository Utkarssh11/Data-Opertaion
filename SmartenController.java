package com.elegantjbi.controller.smarten;

/*
 * @(#)GraphController.java Version 4.0 <Jan 12, 2014>
 *
 * Copyright 2015 Elegant MicroWeb Technologies Pvt. Ltd. (India). All Rights Reserved. Use is subject to license terms.
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.spark.sql.Row;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.elegantjbi.AppContext;
import com.elegantjbi.AppMainCommandList;
import com.elegantjbi.amcharts.vo.GraphData;
import com.elegantjbi.amcharts.vo.Graphs;
import com.elegantjbi.controller.ObjectController;
import com.elegantjbi.core.cubeio.IDAIFQTDOperator;
import com.elegantjbi.core.fileio.FileEx;
import com.elegantjbi.core.mdx.MDXException;
import com.elegantjbi.core.olap.CubeColumnExpExecutor;
import com.elegantjbi.core.olap.CubeColumnInfo;
import com.elegantjbi.core.olap.CubeCondition;
import com.elegantjbi.core.olap.CubeConditionInfo;
import com.elegantjbi.core.olap.CubeConditionList;
import com.elegantjbi.core.olap.CubeDataExpExecutor;
import com.elegantjbi.core.olap.CubeDataLabelSource;
import com.elegantjbi.core.olap.CubeDataLabelTarget;
import com.elegantjbi.core.olap.CubeException;
import com.elegantjbi.core.olap.CubeLabelExpExecutor;
import com.elegantjbi.core.olap.CubeLabelInfo;
import com.elegantjbi.core.olap.CubeOrderByInfo;
import com.elegantjbi.core.olap.CubeRankDataLabel;
import com.elegantjbi.core.olap.CubeVector;
import com.elegantjbi.core.olap.CubeViewInfo;
import com.elegantjbi.core.olap.ICubeConstants;
import com.elegantjbi.core.olap.ICubeResultSetMetaData;
import com.elegantjbi.core.olap.ICubeResultSetSupport;
import com.elegantjbi.core.realtime.RealTimeCubeException;
import com.elegantjbi.dao.CurrentTenantIdentifierResolverImpl;
import com.elegantjbi.entity.FolderInfo;
import com.elegantjbi.entity.IDataObject;
import com.elegantjbi.entity.IEntity;
import com.elegantjbi.entity.WhatIfConfigurationInfo;
import com.elegantjbi.entity.admin.CubeInfo;
import com.elegantjbi.entity.admin.GeneralConfigurationInfo;
import com.elegantjbi.entity.admin.GeographicalColumnInfo;
import com.elegantjbi.entity.admin.GroupInfo;
import com.elegantjbi.entity.admin.PDFPageSetupInfo;
import com.elegantjbi.entity.admin.RProfileInfo;
import com.elegantjbi.entity.admin.UserInfo;
import com.elegantjbi.entity.dashboard.DashboardInfo;
import com.elegantjbi.entity.mashups.datasets.DataSetsInfo;
import com.elegantjbi.entity.nlp.NlpInfo;
import com.elegantjbi.entity.smarten.SmartenInfo;
import com.elegantjbi.entity.tooltemplate.ActiveFilterInfo;
import com.elegantjbi.entity.tooltemplate.ActiveGlobalVariableInfo;
import com.elegantjbi.entity.tooltemplate.ActiveUDDCInfo;
import com.elegantjbi.entity.tooltemplate.ActiveUDHCInfo;
import com.elegantjbi.entity.tooltemplate.UddcTemplateInfo;
import com.elegantjbi.exception.CubeAccessException;
import com.elegantjbi.exception.CubeNotFoundException;
import com.elegantjbi.exception.DatabaseOperationException;
import com.elegantjbi.exception.ObjectAccessException;
import com.elegantjbi.exception.ObjectNotFoundException;
import com.elegantjbi.exception.ServiceException;
import com.elegantjbi.security.APICustomAuthenticationProvider;
import com.elegantjbi.service.ObjectService;
import com.elegantjbi.service.admin.IApplicationConfigurationService;
import com.elegantjbi.service.admin.UploadImageServiceUtil;
import com.elegantjbi.service.admin.UserManagementServiceUtil;
import com.elegantjbi.service.analysis.parts.ALSCommandNameList;
import com.elegantjbi.service.analysis.parts.ALSException;
import com.elegantjbi.service.analysis.parts.LMRecentInfo;
import com.elegantjbi.service.graph.GraphConstants;
import com.elegantjbi.service.kpi.KPIConstants;
import com.elegantjbi.service.kpi.KPIException;
import com.elegantjbi.service.kpiGroup.KPIGroupService;
import com.elegantjbi.service.mashups.MashupsConstants;
import com.elegantjbi.service.mashups.dataset.DataSetConstant;
import com.elegantjbi.service.mashups.mashup.MashUpConstants;
import com.elegantjbi.service.repository.RepositoryService;
import com.elegantjbi.service.smarten.SmartenConstants;
import com.elegantjbi.service.smarten.SmartenHierarchyTree;
import com.elegantjbi.service.smarten.SmartenService;
import com.elegantjbi.service.util.AccessRightServiceUtil;
import com.elegantjbi.service.util.CubeMetadataServiceUtil;
import com.elegantjbi.service.util.RecentlyUsedServiceUtil;
import com.elegantjbi.service.util.RepositoryServiceUtil;
import com.elegantjbi.spark.service.util.SparkServiceUtil;
import com.elegantjbi.spark.sql.BIDataset;
import com.elegantjbi.util.AppConstants;
import com.elegantjbi.util.AppContextUtil;
import com.elegantjbi.util.CalendarUtil;
import com.elegantjbi.util.CubeUtil;
import com.elegantjbi.util.DefaultConfUtil;
import com.elegantjbi.util.ExportServiceUtil;
import com.elegantjbi.util.GeneralFiltersUtil;
import com.elegantjbi.util.GeneralUtil;
import com.elegantjbi.util.HashtableEx;
import com.elegantjbi.util.LoggedInUser;
import com.elegantjbi.util.Pagination;
import com.elegantjbi.util.RScriptException;
import com.elegantjbi.util.ResourceManager;
import com.elegantjbi.util.StringUtil;
import com.elegantjbi.util.TemplateUtil;
import com.elegantjbi.util.logger.ApplicationLog;
import com.elegantjbi.util.xml.XMLDimensionColumn;
import com.elegantjbi.vo.ApiTokenVo;
import com.elegantjbi.vo.ColumnNameDisplayVo;
import com.elegantjbi.vo.OutlinerBean;
import com.elegantjbi.vo.PageFilterNew;
import com.elegantjbi.vo.Pair;
import com.elegantjbi.vo.ParamItem;
import com.elegantjbi.vo.RScriptInputOutputVO;
import com.elegantjbi.vo.SelectItem;
import com.elegantjbi.vo.analysis.RankVO;
import com.elegantjbi.vo.graph.Group;
import com.elegantjbi.vo.mashups.datasource.FieldVo;
import com.elegantjbi.vo.properties.ActiveTemplateProperties;
import com.elegantjbi.vo.properties.FontProperties;
import com.elegantjbi.vo.properties.NumberFormat;
import com.elegantjbi.vo.properties.graph.GraphLineSettingProperties;
import com.elegantjbi.vo.properties.graph.GraphProperties;
import com.elegantjbi.vo.properties.graph.ReferenceLine;
import com.elegantjbi.vo.properties.graph.SmartenProperties;
import com.elegantjbi.vo.properties.graph.SmartenSampling;
import com.elegantjbi.vo.properties.kpi.TrendDataValueProperties;
import com.elegantjbi.vo.properties.kpi.TrendLineProperties;
import com.elegantjbi.vo.properties.kpi.YaxisTrendProperties;
import com.elegantjbi.vo.repository.Repository;
/**
 * The controller for the graph. 
 * @author NS
 *
 */
@Controller
@Scope("session")
@RequestMapping("/smartview")
public class SmartenController extends ObjectController implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String objectTypeName = ResourceManager.getString("LBL_SMARTVIEW_LINK");

	private SmartenInfo graphInfo;
	
	private SmartenService smartenService;
	
	private String keyFilter = null;

	@Autowired
	private UploadImageServiceUtil uploadImageServiceUtil;

	@Autowired
	private RecentlyUsedServiceUtil recentlyUsedServiceUtil;
	@Autowired
	private AccessRightServiceUtil accessRightServiceUtil;
	
	@Autowired
	private CubeMetadataServiceUtil metadataServiceUtil;
	
	@Autowired
	private UserManagementServiceUtil userManagementServiceUtil;

	@Autowired
	protected SparkServiceUtil sparkServiceUtil;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private RepositoryServiceUtil repositoryUtil;
	
	@Autowired
	private KPIGroupService kpiGroupService;
	
	Map<String,ModelAndView> ObjectInitializationMap = new HashMap<String,ModelAndView>();
	Map<String,Thread> ObjectInitializationThreadMap = new HashMap<String,Thread>();
	
	private int pageNumer = 1;
	private boolean isFromGetPage;
	

	
	@RequestMapping(value="/new")
	public ModelAndView createNewGraph(ModelMap map, @LoggedInUser UserInfo userInfo){
		graphInfo = new SmartenInfo();
		
		String graphId = String.valueOf(System.currentTimeMillis());
		smartenService =(SmartenService) AppContext.getApplicationContext().getBean("smartenService");
		getServiceMap().put(graphId, smartenService);
		GeneralConfigurationInfo generalConfigurationInfo = generalConfigurationServiceUtil.getGeneralConfigurationInfo();
		String googleMapKey = generalConfigurationInfo.getGoogleMapKey();
		map.put("googleKey", googleMapKey);
		getDetailInfoMap().put(graphId, graphInfo);
		graphInfo.setNewGraphId(graphId);
		
		smartenService.setLoggedInUserId(userInfo.getUserId());
		smartenService.setIsFromAnalysis(false);
		map.addAttribute("Mode", AppConstants.NEW_MODE);
		try {
			int permission =  accessRightServiceUtil.getSmartenPermission(userInfo, null);
			int operation = AppConstants.WRITE_RIGHTS_DB;
			if(!((operation & permission) == operation)){
				map.put("errorMessage", ResourceManager.getString("ERROR_NO_ACCESS_PERMISSION"));
				return new ModelAndView("permissionErrorPage");
			}
		} catch (DatabaseOperationException e1) {
			map.put("errorMessage", e1.getLocalizedMessage());
			return new ModelAndView("permissionErrorPage");
		} catch (ServiceException e1) {
			map.put("errorMessage", e1.getLocalizedMessage());
			return new ModelAndView("permissionErrorPage");
		}
		
		graphInfo.setGraphName(ResourceManager.getString("New SmartenView"));
		graphInfo.setCreatedBy(userInfo);
		try {
			graphInfo.setPdfPageSetup(pageSetupServiceUtil.getPDFPageSetupInfo());
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_SET_PDFPAGESETUP",
					new Object[] { userInfo.getUsername() }), e);
		}
		graphInfo.setGraphMode(AppConstants.NEW_MODE);
		if (graphInfo.getGraphType() ==  GraphConstants.NUMERIC_DIAL_GAUGE) {
			map.put("isGaugeGraph", true);
		} else {
			map.put("isGaugeGraph", false);	
		}
		map.put("measureLegend", false);
		map.addAttribute("Mode", AppConstants.NEW_MODE);
		map.addAttribute("objectType", AppConstants.SMARTEN);
		map.put("graphProperties", graphInfo.getGraphProperties());
		String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
		map.addAttribute("strDateFormat",strDateFormat);
		map.addAttribute("graphInfo",graphInfo);
		auditUserActionLog(ResourceManager.getString("LBL_CREATE_GRAPH"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("smartenOutliner");
	}
	
	@RequestMapping(value = "/{strObjectId:.+}")
	public ModelAndView showGraph(
			@PathVariable String strObjectId,
			
			@RequestParam(value = "screenWidth", required = false, defaultValue = "0") int windowScreenWidth,
			@RequestParam(value = "screenHeight", required = false, defaultValue = "0") int windowScreenHeight,
			@RequestParam(value = "objFolderId", required = false) String strObjFolderId,
			@RequestParam(value = "isDefaultHomePage", required = false,defaultValue="false") boolean isDefaultHomePage,
			@RequestParam(value = "isFromRestore", required = false,defaultValue="false") boolean isFromRestore,
			@RequestParam(value = "isFromMobile", required = false,defaultValue="false") boolean isFromMobile,
			@RequestParam(value = "isSaveAnalysis", required = false,defaultValue="false") boolean isSaveAnalysis,
			@RequestParam(value="isFromDashboard" ,required=false, defaultValue="false") boolean isFromDashboard,
			@RequestParam(value ="isFromLinkDashboardObject", required = false,defaultValue="false") boolean isFromLinkDashboardObject,
			ModelMap map,
			@LoggedInUser UserInfo userInfo, HttpSession session, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="filterKey", required=false ) String filterKey,
			@RequestParam(value="exportToken", required=false ) String exportToken,
			@RequestParam(value = "isFromNlpExplorer", required = false,defaultValue="false") boolean isFromNlpExplorer) throws ALSException{
		long start = System.currentTimeMillis();
			
		boolean mobReq = request != null && request.getHeader("User-Agent") != null && request.getHeader("User-Agent").contains("Mobile");
		map.put("isMobile", mobReq);
		
		DashboardInfo dashboardInfo = null;
		if (filterKey != null) {
			keyFilter = filterKey;
		}
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		
			isFromDefaultHomePage = isDefaultHomePage;
			if(isFromDashboard){
				if(graphInfo != null){
					dashboardInfo =  graphInfo.getDashboardInfo();
				}
			}
			request.getSession().setAttribute("isFromMobile", isFromMobile);
			map.put("isFromMobile", session.getAttribute("isFromMobile"));
			if(windowScreenWidth <= 0) {
				windowScreenWidth = userInfo.getDeviceWidth();
			}
			if(windowScreenHeight <= 0) {
				windowScreenHeight = userInfo.getDeviceHeight();
			}
			NlpInfo nlpInfo = null;
			if(graphInfo != null && graphInfo.getNlpInfo() != null && isFromNlpExplorer) {
				nlpInfo = graphInfo.getNlpInfo();
			}
			if(!isSaveAnalysis) {
				smartenService = (SmartenService) AppContext.getApplicationContext().getBean("smartenService");
				SmartenService smartenServiceObj = null;
				if (getServiceMap().get(strObjectId) != null) {
					smartenServiceObj = (SmartenService) getServiceMap().get(strObjectId);
					if (smartenServiceObj.getLinkedObjectFilterValMap() != null
							&& smartenServiceObj.getLinkedObjectFilterValMap().size() > 0) {
						smartenService.setLinkedObjectFilterValMap(smartenServiceObj.getLinkedObjectFilterValMap());
					}
				}
				getServiceMap().put(strObjectId, smartenService);
				graphInfo = new SmartenInfo();
				getDetailInfoMap().put(strObjectId, graphInfo);
			} else {
				smartenService = (SmartenService) getServiceMap().get(strObjectId);
				graphInfo = (SmartenInfo) getDetailInfoMap().get(strObjectId);
			}
			
			getServiceMap().put(strObjectId, smartenService);
			if(!isFromLinkDashboardObject){
				smartenService.setLinkedObjectFilterValMap(new HashMap<>());
			}
			if(isExportCall(exportToken)) {
				smartenService = (SmartenService) getExportRelatedService(exportToken, strObjectId, smartenService);
				graphInfo = (SmartenInfo) getExportRelatedEntityInfo(exportToken, strObjectId, graphInfo);
				isSaveAnalysis = true;	
			}
			detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Open SmartenView","Initialize SmartenView",Thread.currentThread(),userInfo,null);			
			SmartenInfo smartenObjInfo = getGraphObjectFromMap(strObjectId);
			SmartenService smartenServiceObj = getGraphServiceFromMap(strObjectId);

			smartenService.setLoggedInUserId(userInfo.getUserId());
			GeneralConfigurationInfo generalConfigurationInfo = generalConfigurationServiceUtil.getGeneralConfigurationInfo();
			GroupInfo groupInfo	= null;
			if(userInfo.getGroupInfo() != null){
				groupInfo=userManagementServiceUtil.getGroupInfoById(userInfo.getGroupInfo().getGroupId());
			}	
			boolean isTrue = false;
			String googleMapKey = generalConfigurationInfo.getGoogleMapKey();
			map.put("googleKey", googleMapKey);
			if (strObjectId != null && strObjectId.trim().length() > 0) {
				smartenService.setObjectMode(AppConstants.OPEN_MODE);
			} else {
				strObjectId = "";
			}
			
			if (smartenService.getObjectMode() == AppConstants.OPEN_MODE) {
				
				String smartenName="";
				if(!(strObjectId != null && strObjectId.endsWith(AppConstants.SMARTEN_FILE_EXT))){
					smartenName = strObjectId;
				}
				else{
					smartenName = strObjectId.substring(0,
							strObjectId.indexOf(AppConstants.SMARTEN_FILE_EXT));	
				}
				 
				if (generalConfigurationInfo.isShowToolbar() || userInfo.isShowToolbar() || (groupInfo != null && groupInfo.isShowToolbar())) {
					isTrue = true;
				}
				
				params.put("objectPath", strObjFolderId + "/" + smartenName);
				params.put("user", userInfo.getUserId());
				params.put("ownerID", userInfo.getUserId());
				params.put("filecmd", getGraphObjectFromMap(strObjectId).getGraphMode() + "");
				params.put("objectId", strObjectId);				
				params.put("windowScreenWidth", windowScreenWidth);
				params.put("windowScreenHeight", windowScreenHeight);
				map.put("isDefaultHomePage", isDefaultHomePage);
				map.put("showToolbars", isTrue);
				map.put("isSetAsHome",userInfo.getHomePage());	
			
			
			
			try {
				String isFromDashBoardLink = ""+isFromLinkDashboardObject;
				long start1 = System.currentTimeMillis();
				smartenObjInfo = smartenService.initializeGraph(smartenService.getObjectMode(), params, userInfo, true);
				ApplicationLog.info("Smarten View initializeGraph  === >>> "+(System.currentTimeMillis()-start1));
				graphInfo = smartenObjInfo;
				graphInfo.setNlpInfo(nlpInfo);
				getDetailInfoMap().put(strObjectId, graphInfo);
				
				graphInfo = getGraphObjectFromMap(strObjectId);
				smartenServiceObj = getGraphServiceFromMap(strObjectId);
				
				/*if (graphInfo.getGraphProperties().getTitleProperties().isTitleVisible()) {
					HashtableEx ddvmList = smartenServiceObj.getActiveDDVMs(graphInfo, userInfo.getUserId());

					smartenServiceObj.setObjectPageTitle(graphInfo.getGraphId(),graphInfo
							.getActiveFilterInfo(userInfo.getUserId()),
							smartenServiceObj.getPageFilterNew(graphInfo),
							smartenServiceObj.getActiveVariableMap(), smartenServiceObj.getResultSetMetaData(),
							(IDataObject)graphInfo.getCubeInfo(), graphInfo.getGraphProperties().getTitleProperties(),
							userInfo, ddvmList);
				}*/
				
				
				session.removeAttribute("isFromDashBoardLink");
				boolean isFromUseCase = false;
				List<String> list = DefaultConfUtil.getExploreObjectList();
				if(list != null && !list.isEmpty()) {
					if(list.contains(strObjectId)){
						isFromUseCase = true;
					}
				}
				map.put("isFromUseCase", isFromUseCase);
			
				if(!isFromUseCase && (!isDefaultHomePage && null == dashboardInfo && !("true").equals(isFromDashBoardLink) && !userInfo.isAnonymousUser())){
					recentlyUsedServiceUtil.saveRecentlyUsed(strObjectId, graphInfo.getGraphName(), userInfo, AppConstants.SMARTEN,0);
				}
				setStrParentHierchy(smartenServiceObj.getFolderDisplayPathFromFolderInfo(graphInfo.getFolderInfo(), userInfo.getUserId()));
				
				if (isFromDashboard){
					graphInfo.setDashboardInfo(dashboardInfo);
					graphInfo.setFromDashBoardLink(true);
				}
				map.put("graphInfo", graphInfo);
				
				HashMap conditionMap = (HashMap) smartenServiceObj.getLinkedObjectFilterValMap();
				boolean isRetrievalOnload =	smartenServiceObj.retrievalonloadcheck(smartenObjInfo,userInfo);
				if(isRetrievalOnload && !isFromRestore && !isDefaultHomePage && !isSaveAnalysis && (isFromDashBoardLink == null || !("true").equals(isFromDashBoardLink))) {
				WhatIfConfigurationInfo whatIfConfigurationInfo = graphInfo.getWhatIfConfigurationInfo();
				if(whatIfConfigurationInfo != null) {
					if(whatIfConfigurationInfo.getShowRetrievalOnLoad() != null && whatIfConfigurationInfo.getShowRetrievalOnLoad().equalsIgnoreCase("true")) {
						map.put("isforRetrievalOnLoad", "true");
						
						graphInfo.setGraphMode(smartenServiceObj.getObjectMode());
						graphInfo.setReadFromCache(false);
						if (graphInfo.getGraphType() ==  GraphConstants.NUMERIC_DIAL_GAUGE) {
							map.put("isGaugeGraph", true);
						} else {
							map.put("isGaugeGraph", false);	
						}
						map.addAttribute("Mode", smartenServiceObj.getObjectMode());
						map.addAttribute("objectType", AppConstants.SMARTEN);
						map.put("graphProperties", graphInfo.getGraphProperties());
						String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
						map.addAttribute("strDateFormat",strDateFormat);
						if(graphInfo != null){
							graphInfo.setDashboardInfo(dashboardInfo);
						}
						showAppliedFilter(map, userInfo, null);
						map.addAttribute("graphInfo",getGraphObjectFromMap(strObjectId));
						
						
						map.put("includeSmartenJSP",true);
						map.put("fromSave",true);
						map.put("showRetrival",true);
						if(null != graphInfo.getGraphProperties().getSunburst()) {
							map.put("d3sunburstRadius", graphInfo.getGraphProperties().getSunburst().getRadiusInPer());	
						}
						else {
							map.put("d3sunburstRadius", 45);
						}
						String strCubeId="";
						try
						{
							IDataObject cubeInfo = graphInfo.getCubeInfo();

							strCubeId = graphInfo.getCubeInfo().getDataObjectId();

							Vector<String> vector = new Vector<String>();
							if (strCubeId == null) {
								
								List<ActiveGlobalVariableInfo> activeGolbalVariableList = graphInfo.getActiveTemplateProperties().getActiveGlobalVariableInfo(userInfo.getUserId());
								if(activeGolbalVariableList != null && activeGolbalVariableList.size()>0){
									for (ActiveGlobalVariableInfo activeGlobalVariableInfo : activeGolbalVariableList) {
										vector.add(activeGlobalVariableInfo.getGlobalVariableInfo().getGlobalVariableName());
									}
								}	
							}

							HashMap<String, Vector<String>> dimensionMap = metadataServiceUtil
									.getColumnsAndMeasuresMap(cubeInfo, userInfo,
											vector,true,true,false,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
							List<String> dimensionList = new ArrayList<String>();
							dimensionList.addAll(dimensionMap.get("1"));
							dimensionList.addAll(dimensionMap.get("2"));
							dimensionList.addAll(dimensionMap.get("3"));
							
							dimensionList.addAll(dimensionMap.get("5"));
							dimensionList.addAll(dimensionMap.get("6"));
							List<String> measureList = metadataServiceUtil.getMeasureList(cubeInfo, userInfo,true,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
							OutlinerBean outlinerBean = new OutlinerBean();
							outlinerBean.setPtreeEnable(true);
							try {
								outlinerBean = smartenService.getOutlinerData(outlinerBean, graphInfo, userInfo, dimensionList, cubeInfo, false);
							} catch (CubeException e) {
								ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
							} catch( DatabaseOperationException e){
								ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
							} catch (Exception e) {
								ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
							}
							map.put("dimensionList", dimensionList);			
							map.put("measureList", measureList);
							map.put("outlinerBean", outlinerBean);
							map.put("selectedCubeId", strCubeId);
							map.put("objectType", AppConstants.SMARTEN);
							map.put("selectedGraphType", graphInfo.getGraphType());
							map.put("selectedRecommendedGraphType", graphInfo.getGraphType());
							map.put("recommendedGraphType", graphInfo.getRecommendGraphType());
							map.put("smartenChartHeight",graphInfo.getGraphProperties().getSmartenChartHeight());
							map.put("fromSave",true);
							map.put("smartenModeAfterSave",graphInfo.isSmartenMode());
							map.put("isRefreshReq", true);
							if(graphInfo.isSmartenMap())
								map.put("fromSave",false);
							
						}
						catch (CubeException e) {
							ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
						} catch (DatabaseOperationException ex) {
							ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), ex);
						}
						return new ModelAndView("smartenSave");
						
					}
				}
				}
				String strObjectKey =  strObjectId+AppConstants.API_KEY_SEPERATOR+keyFilter;
				String tenant = CurrentTenantIdentifierResolverImpl.getCurrentTenantIdentifier();
				
				List<ApiTokenVo> tokensByTenant = APICustomAuthenticationProvider.getTokensByTenantMap().get(tenant);
				if (tokensByTenant != null && !tokensByTenant.isEmpty()) {
					ApiTokenVo token = tokensByTenant.stream().filter(t-> t != null && t.getObjectKey().equals(strObjectKey)).findFirst().orElse(null);
			
		            if(token != null){
		            	Map<String, List<String>> filterMap = token.getCustomWebDetail().getFilterMap();
		                if(filterMap != null && !filterMap.isEmpty()) {
		                	graphInfo.setApiFilterMap(filterMap);
		                	graphInfo.setReadFromCache(false);
		                	Map<String,String> iteams = new HashMap<>();
							List<ActiveGlobalVariableInfo> acGv= graphInfo.getActiveGlobalVariableInfo(userInfo.getUsername());
							for(ActiveGlobalVariableInfo acGlob : acGv) {
								iteams.put(acGlob.getGlobalVariableInfo().getGlobalVariableName().replace("$", ""), acGlob.getGlobalVariableInfo().getTypeString());
							}
		                	request.setAttribute("objectId", strObjectId);
		                	String gv = GeneralFiltersUtil.getGvConditionString(filterMap,graphInfo.getCubeInfo().getId(),iteams);
							if(gv != null && !gv.equals("")) {
								setGlobalVariable(gv, userInfo, false,request);
							}
		                }
		            }
				}
			
				if(isFromDashBoardLink != null && ("true").equals(isFromDashBoardLink)) {
					if(smartenServiceObj.getLinkedObjectActiveGlobalVariableList() != null && !smartenServiceObj.getLinkedObjectActiveGlobalVariableList().isEmpty() && graphInfo.getActiveTemplateProperties() != null) {
						graphInfo.getActiveTemplateProperties().setActiveGlobalVariableInfo(smartenServiceObj.getLinkedObjectActiveGlobalVariableList());
						smartenServiceObj.setActiveGlobalVariableMapForLinkedObject(smartenServiceObj.getLinkedObjectActiveGlobalVariableList());
					}else if(session.getAttribute("activeGlobalList") != null && graphInfo.getActiveTemplateProperties() != null) {
						graphInfo.getActiveTemplateProperties().setActiveGlobalVariableInfo((List<ActiveGlobalVariableInfo>) session.getAttribute("activeGlobalList"));
						session.removeAttribute("activeGlobalList");
						smartenServiceObj.setActiveGlobalVariableMapForLinkedObject(smartenServiceObj.getLinkedObjectActiveGlobalVariableList());
					}
					HashMap objectConditionMap;
					conditionMap = (HashMap) smartenServiceObj.getLinkedObjectFilterValMap();
					Map<String,String> dashboardFilterInfoMap = (HashMap) session.getAttribute("dashboardFilterInfoMap");
					session.removeAttribute("dashboardFilterInfoMap");
					if(conditionMap != null && !conditionMap.isEmpty()) {
						objectConditionMap=((SmartenService)getServiceMap().get(strObjectId)).getFilterConditions();
						if(objectConditionMap!=null && !objectConditionMap.isEmpty()) {
							conditionMap.forEach((k, v) -> objectConditionMap.put(k,v));
							conditionMap = objectConditionMap;
						}
						CubeVector testCube = (CubeVector) conditionMap.get("PrelodingParameterList");
						conditionMap.remove("PrelodingParameterList");
						if(testCube != null && testCube.size() > 0) {
							smartenServiceObj.setFilterConditionsFromDashboard(conditionMap, graphInfo);
							smartenServiceObj.setDBPreloadingParameterList(testCube);
						} else {
							smartenServiceObj.setFilterConditionsFromDashboard(conditionMap, graphInfo);
						}
						graphInfo.setReadFromCache(false);
						
						graphInfo.setDashboardFilterInfoMap(dashboardFilterInfoMap);
					}
					dashboardInfo = (DashboardInfo) session.getAttribute("dashboardObj");
					session.removeAttribute("dashboardObj");
					graphInfo.setDashboardInfo(dashboardInfo);
					graphInfo.setFromDashBoardLink(true);
					}
			
				
				boolean isOnLoadInfo = false;
				if(!isFromRestore && !isDefaultHomePage && !isSaveAnalysis &&  !("true").equals(isFromDashBoardLink) && graphInfo.getDashboardInfo() == null){
				/*Map<String,String> strTmp = smartenService.readActiveTemplateProperties(graphInfo.getGraphId());
				
				if(strTmp != null && !strTmp.isEmpty()) {
					isOnLoadInfo = true;
					graphInfo.setOnLoadObjectInfo(strTmp);
				} else {
					graphInfo.setOnLoadObjectInfo(null);
				}*/
					if(graphInfo.getOnLoadObjectInfo() != null && !graphInfo.getOnLoadObjectInfo().isEmpty()) {
						if(!graphInfo.getOnLoadObjectInfo().get(0).isEmpty() || !graphInfo.getOnLoadObjectInfo().get(1).isEmpty()) {
							isOnLoadInfo = true;
						}
					}	
				}
				map.put("isOnLoadInfo", isOnLoadInfo);
				
			} catch (DatabaseOperationException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZE_SMARTEN_VIEW",
						new Object[] { userInfo.getUsername(), smartenServiceObj.getObjectMode() }), e);
			} catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZE_SMARTEN_VIEW",
						new Object[] { userInfo.getUsername(), smartenServiceObj.getObjectMode() }), e);
			} catch (ObjectAccessException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (ObjectNotFoundException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (CubeNotFoundException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (CubeAccessException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (IOException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZE_SMARTEN_VIEW",
						new Object[] { userInfo.getUsername(), smartenServiceObj.getObjectMode() }), e);
			} catch (NotBoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZE_SMARTEN_VIEW",
						new Object[] { userInfo.getUsername(),smartenServiceObj.getObjectMode() }), e);
			}
		}
			
		graphInfo.setGraphMode(smartenServiceObj.getObjectMode());

		map.addAttribute("Mode", smartenServiceObj.getObjectMode());
		map.addAttribute("objectType", AppConstants.SMARTEN);		
		map.put("graphProperties", smartenObjInfo.getGraphProperties());
		map.addAttribute("isDataValueOn", smartenObjInfo.getGraphProperties().getDataValueProperties().getDataValuePoint().isDataValuePointVisible());
		String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
		map.addAttribute("strDateFormat",strDateFormat);
		
		showAppliedFilter(map,userInfo, null);
		
			map.addAttribute("graphInfo",getGraphObjectFromMap(strObjectId));
		auditUserActionLog(ResourceManager.getString("LBL_OPEN_OBJECT"), AppConstants.USER_ACCESS,userInfo);

		map.put("fromShowGraph", true);
		
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		if(request != null) {
			if(params.get("isRefresh") == null || params.get("isRefresh").toString().equals("true"))
			{
				request.setAttribute("forceRefresh",true);
			}
			else
			{
				request.setAttribute("forceRefresh",false);
			}
			request.setAttribute("calltiles",true);
		}
		
		graphInfo.setSmartenHomePage(isDefaultHomePage);
		if (isFromDashboard){
			graphInfo.setFromDashBoardLink(false);
		}
		if(isFromLinkDashboardObject) {
			graphInfo.setFromDashBoardLink(true);
		}
		
		
		ApplicationLog.info("Smarten View before Refresh Time == >> "+(System.currentTimeMillis()-start));
		
		return refreshObjectData(request,response, userInfo, map);
	}
	
	
	@RequestMapping(value = "/loadObject")
	public ModelAndView loadGraphObject(@RequestParam String tokenId) {
		Thread thread = ObjectInitializationThreadMap.get(tokenId);
		try {
			thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();  
			ApplicationLog.error(e);
		}
		return ObjectInitializationMap.get(tokenId);
	}
	
	public ModelAndView prepareGraph(String strObjectId, int windowScreenWidth, int windowScreenHeight, String strObjFolderId, boolean isDefaultHomePage, boolean isFromRestore, boolean isSaveAnalysis, boolean isFromDashboard, ModelMap map, UserInfo userInfo, HttpSession session){
		DashboardInfo dashboardInfo = null;
		GeneralConfigurationInfo generalConfigurationInfo =  generalConfigurationServiceUtil.getGeneralConfigurationInfo();
		GroupInfo groupInfo	= null;
		if(userInfo.getGroupInfo() != null){
			groupInfo=userManagementServiceUtil.getGroupInfoById(userInfo.getGroupInfo().getGroupId());
		}
		boolean isTrue = false;
		isFromDefaultHomePage = isDefaultHomePage;
		if(isFromDashboard){
			if(graphInfo != null){
				dashboardInfo =  graphInfo.getDashboardInfo();
			}
		}
				if(graphInfo != null && smartenService.isDrillUpPossible(graphInfo))
				{
					isSaveAnalysis = false;
					smartenService.checkAndResetDrillUp(graphInfo, true);
				}
		if(windowScreenWidth <= 0) {
			windowScreenWidth = userInfo.getDeviceWidth();
		}
		if(windowScreenHeight <= 0) {
			windowScreenHeight = userInfo.getDeviceHeight();
		}
		
		if(!isSaveAnalysis) {
			smartenService = (SmartenService) AppContext.getApplicationContext().getBean("graphService");
			getServiceMap().put(strObjectId, smartenService);
			graphInfo = new SmartenInfo();
			getDetailInfoMap().put(strObjectId, graphInfo);
		} else {
			smartenService = (SmartenService) getServiceMap().get(strObjectId);
			graphInfo = (SmartenInfo) getDetailInfoMap().get(strObjectId);
			setSaveasProcess(false);
		}
		
		
		graphInfo = new SmartenInfo();
		
		getDetailInfoMap().put(strObjectId, graphInfo);
		
		SmartenInfo smartenObjInfo = getGraphObjectFromMap(strObjectId);
		SmartenService smartenServiceObj = getGraphServiceFromMap(strObjectId);
		
		smartenService.setLoggedInUserId(userInfo.getUserId());
		smartenService.setIsFromAnalysis(false);
		Map<String, String> requiredItemsParams = null;
		if (strObjectId != null && strObjectId.trim().length() > 0) {
			smartenService.setObjectMode(AppConstants.OPEN_MODE);
		} else {
			strObjectId = "";
		}
		
		if (smartenService.getObjectMode() == AppConstants.OPEN_MODE) {
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			if (generalConfigurationInfo.isShowToolbar() || userInfo.isShowToolbar() || (groupInfo != null && groupInfo.isShowToolbar())) {
				isTrue = true;
			}
			params.put("objectId", strObjectId);
			params.put("windowScreenWidth", windowScreenWidth);
			params.put("windowScreenHeight", windowScreenHeight);
			map.put("isDefaultHomePage", isDefaultHomePage);
			map.put("showToolbars", isTrue);
			try {
				if(!isSaveAnalysis) {
					graphInfo = smartenService.initializeGraph(smartenService.getObjectMode(), params, userInfo, true);
					getDetailInfoMap().put(strObjectId, graphInfo);
					if(graphInfo.getDataColumns().size() <= 0) {
						map.put("isGraphVisible", false);
						map.put("errorMessage", "");
					}
				}
				
				 smartenObjInfo = getGraphObjectFromMap(strObjectId);
				 smartenServiceObj = getGraphServiceFromMap(strObjectId);
				
				if (smartenObjInfo.getGraphProperties().getTitleProperties().isTitleVisible()) {
					HashtableEx ddvmList = smartenServiceObj.getActiveDDVMs(smartenObjInfo, userInfo.getUserId());

					smartenServiceObj.setObjectPageTitle(smartenObjInfo.getGraphId(),smartenObjInfo
							.getActiveFilterInfo(userInfo.getUserId()),
							smartenServiceObj.getPageFilterNew(smartenObjInfo),
							smartenServiceObj.getActiveVariableMap(), smartenServiceObj.getResultSetMetaData(),
							(IDataObject)smartenObjInfo.getCubeInfo(), smartenObjInfo.getGraphProperties().getTitleProperties(),
							userInfo, ddvmList);
				}
				String isFromDashBoardLink = (String) session.getAttribute("isFromDashBoardLink");
				session.removeAttribute("isFromDashBoardLink");
				if(!isDefaultHomePage && null == dashboardInfo && !("true").equals(isFromDashBoardLink) && !userInfo.isAnonymousUser()){
					recentlyUsedServiceUtil.saveRecentlyUsed(strObjectId, smartenObjInfo.getGraphName(), userInfo, AppConstants.SMARTEN,0);
				}
				setStrParentHierchy(smartenServiceObj.getFolderDisplayPathFromFolderInfo(smartenObjInfo.getFolderInfo(), userInfo.getUserId()));
				HashMap conditionMap = (HashMap) session.getAttribute("filtersessionCondMap");
				boolean isRetrievalOnload =	smartenServiceObj.retrievalonloadcheck(smartenObjInfo,userInfo);
				if(isRetrievalOnload && !isFromRestore && !isDefaultHomePage && !isSaveAnalysis && isFromDashBoardLink == null && !("true").equals(isFromDashBoardLink)) {
				WhatIfConfigurationInfo whatIfConfigurationInfo = smartenObjInfo.getWhatIfConfigurationInfo();
				if(whatIfConfigurationInfo != null) {
					if(whatIfConfigurationInfo.getShowRetrievalOnLoad() != null && whatIfConfigurationInfo.getShowRetrievalOnLoad().equalsIgnoreCase("true")) {
						map.put("isforRetrievalOnLoad", "true");
						smartenObjInfo.setGraphMode(smartenServiceObj.getObjectMode());
						graphInfo.setReadFromCache(false);
						if (graphInfo.getGraphType() ==  GraphConstants.NUMERIC_DIAL_GAUGE) {
							map.put("isGaugeGraph", true);
						} else {
							map.put("isGaugeGraph", false);	
						}
						map.addAttribute("Mode", smartenServiceObj.getObjectMode());
						map.addAttribute("objectType", AppConstants.SMARTEN);
						map.put("graphProperties", smartenObjInfo.getGraphProperties());
						String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
						map.addAttribute("strDateFormat",strDateFormat);
						if(smartenObjInfo != null){
							smartenObjInfo.setDashboardInfo(dashboardInfo);
						}
						showAppliedFilter(map, userInfo, null);
						map.addAttribute("graphInfo",getGraphObjectFromMap(strObjectId));
						return new ModelAndView("graph");
					}
				}
				}
				detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Open SmartenView","Initialize SmartenView",Thread.currentThread(),userInfo,null);
				if(isFromDashBoardLink != null && ("true").equals(isFromDashBoardLink)) {
					if(smartenServiceObj.getLinkedObjectActiveGlobalVariableList() != null && !smartenServiceObj.getLinkedObjectActiveGlobalVariableList().isEmpty() && graphInfo.getActiveTemplateProperties() != null) {
						graphInfo.getActiveTemplateProperties().setActiveGlobalVariableInfo(smartenServiceObj.getLinkedObjectActiveGlobalVariableList());
					}else if(session.getAttribute("activeGlobalList") != null && graphInfo.getActiveTemplateProperties() != null) {
						graphInfo.getActiveTemplateProperties().setActiveGlobalVariableInfo((List<ActiveGlobalVariableInfo>) session.getAttribute("activeGlobalList"));
						session.removeAttribute("activeGlobalList");
					}
					HashMap objectConditionMap;
					conditionMap = (HashMap) session.getAttribute("filtersessionCondMap");
					Map<String,String> dashboardFilterInfoMap = (HashMap) session.getAttribute("dashboardFilterInfoMap");
					session.removeAttribute("dashboardFilterInfoMap");
					if(conditionMap != null && !conditionMap.isEmpty()) {
						objectConditionMap=((SmartenService)getServiceMap().get(strObjectId)).getFilterConditions();
						if(objectConditionMap!=null && !objectConditionMap.isEmpty()) {
							conditionMap.forEach((k, v) -> objectConditionMap.put(k,v));
							conditionMap = objectConditionMap;
						}
						CubeVector testCube = (CubeVector) conditionMap.get("PrelodingParameterList");
						conditionMap.remove("PrelodingParameterList");
						if(testCube != null && testCube.size() > 0) {
							smartenServiceObj.setFilterConditionsFromDashboard(conditionMap, smartenObjInfo);
							smartenServiceObj.setDBPreloadingParameterList(testCube);
						} else {
							smartenServiceObj.setFilterConditionsFromDashboard(conditionMap, smartenObjInfo);
						}
						graphInfo.setReadFromCache(false);
						session.removeAttribute("filtersessionCondMap");
						
					}
					graphInfo.setDashboardFilterInfoMap(dashboardFilterInfoMap);
					dashboardInfo = (DashboardInfo) session.getAttribute("dashboardObj");
					session.removeAttribute("dashboardObj");
					graphInfo.setDashboardInfo(dashboardInfo);
					graphInfo.setFromDashBoardLink(true);
					}
			} catch (DatabaseOperationException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenServiceObj.getObjectMode() }), e);
			} catch (MDXException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (RealTimeCubeException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				return new ModelAndView("permissionErrorPage");
			} catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenServiceObj.getObjectMode() }), e);
			} catch (IOException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenServiceObj.getObjectMode() }), e);
			} catch (NotBoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(),smartenServiceObj.getObjectMode() }), e);
			} catch (ObjectAccessException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (ObjectNotFoundException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (CubeNotFoundException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} catch (CubeAccessException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			}catch (ALSException e) {
				map.addAttribute("Mode", smartenServiceObj.getObjectMode());
				map.addAttribute("objectType", AppConstants.SMARTEN);
				map.put("errorMessage", e.getLocalizedMessage());
				map.put("isGraphVisible", false);
				graphInfo.setErrorMessage(e.getLocalizedMessage());
				return new ModelAndView("permissionErrorPage");
			} finally {
				detailedMonitorEndpoint.removeActiveRequest(Thread.currentThread().getId());
			} 
			
			map.put("issaveGraph", isSaveAnalysis);
			//2018 merge
			if(isSaveAnalysis)//Bug #12389
			{
				graphInfo.getGraphData().setFromAnalysis(false);
			}
			//2018 merge
			refreshObjectData(null, null, userInfo, map);
			
			long currentTimestamp = System.currentTimeMillis();
			requiredItemsParams = new HashMap<String, String>();
			requiredItemsParams.put("sCmd", AppMainCommandList.NEW_GRAPH.getM_strCommandName());
			requiredItemsParams.put("firstTime" , "true");
			requiredItemsParams.put("currentTimeStamp", currentTimestamp + "");

			generateRequiredItemsForGraph(map, requiredItemsParams);
			map.put("currentTimeStamp", currentTimestamp);
		} /*else {
			graphObjInfo.setGraphName(ResourceManager.getString("GRAPH_NEW"));
			graphObjInfo.setCreatedBy(userInfo);
			try {
				graphObjInfo.setPdfPageSetup(pageSetupServiceUtil.getPDFPageSetupInfo());
			} catch (DatabaseOperationException e) {
				ApplicationLog.error(ResourceManager.getString(
						"LOG_ERROR_MSG_FAILED_TO_SET_PDFPAGESETUP",
						new Object[] { userInfo.getUsername() }), e);
			}
		}*/

		smartenObjInfo.setGraphMode(smartenServiceObj.getObjectMode());
		if (graphInfo.getGraphType() ==  GraphConstants.NUMERIC_DIAL_GAUGE) {
			map.put("isGaugeGraph", true);
		} else {
			map.put("isGaugeGraph", false);	
		}
		map.addAttribute("Mode", smartenServiceObj.getObjectMode());
		map.addAttribute("objectType", AppConstants.SMARTEN);
		map.put("graphProperties", smartenObjInfo.getGraphProperties());
		String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
		map.addAttribute("strDateFormat",strDateFormat);
		//For Back to dashboard breadcrumb
		if(smartenObjInfo != null){
			smartenObjInfo.setDashboardInfo(dashboardInfo);
		}
		showAppliedFilter(map, userInfo, null);// Check whether filter is applied or not.
		map.addAttribute("graphInfo",getGraphObjectFromMap(strObjectId));
		return new ModelAndView("graph");
	}
	
	/**
	 * This method is use to Show outliner Dialog
	 * @param map
	 * @return model Map
	 * @throws ALSException 
	 */
	@RequestMapping (value = "/outliner")
	public ModelAndView showOutliner(HttpServletRequest request, ModelMap map, @LoggedInUser UserInfo userInfo, HttpServletResponse response) throws ALSException {

		
		HashMap<String, String> requestParamMap = new HashMap<String, String>();
		Enumeration<String> requestEnum = request.getParameterNames();

		while (requestEnum.hasMoreElements()) {
			String paramName = requestEnum.nextElement();
			String paramValue = StringUtil.null2String(request.getParameter(paramName));

			requestParamMap.put(paramName, paramValue);
		}
		String strCubeId="";
		int  windowScreenWidth=0;
		int windowScreenHeight=0;		
		if(requestParamMap.get("selectedCubeId")!=null)
			strCubeId=requestParamMap.get("selectedCubeId");
		if(requestParamMap.get("screenWidth")!=null)
			windowScreenWidth=Integer.parseInt(requestParamMap.get("screenWidth"));
		if(requestParamMap.get("screenHieght")!=null)
			windowScreenHeight=Integer.parseInt(requestParamMap.get("screenHieght"));
		
		
		if(strCubeId!="" && strCubeId != null) {
			//session.removeAttribute("cubeList");
			Hashtable<String, Object> params = new Hashtable<String, Object>();

			
			
			params.put("objectPath", "");
			params.put("cubeId", strCubeId);
			params.put("windowScreenWidth", windowScreenWidth);
			params.put("windowScreenHeight", windowScreenHeight);
			
			//smarten Title setting
			params.put("titlesDropdown",""+graphInfo.getGraphProperties().getSmartenProperties().getTitlesDropdown());
			
			graphInfo.setWindowScreenHeight(windowScreenHeight);
			graphInfo.setWindowScreenWidth(windowScreenWidth); 
			try {
				graphInfo = smartenService.initializeGraph(AppConstants.NEW_MODE, params, userInfo, false);
				getDetailInfoMap().put(graphInfo.getGraphId(), graphInfo);
				//modified by harsh on 16 dec
				GeneralConfigurationInfo generalConfiguration =	generalConfigurationServiceUtil.getGeneralConfigurationInfo();
				
				if(generalConfiguration.getEditByCreator()!= null && generalConfiguration.getEditByCreator().intValue() == 0)
				{
					graphInfo.getGraphProperties().setEditByCreator(true);
				}
				else
				{
					graphInfo.getGraphProperties().setEditByCreator(false);
				}
			
		
				smartenService.setFirstTime(true);
				smartenService.setGraphTypeWizard(true);
				smartenService.setSmartenFirstTime(true);//Added for default tool-tip of Graphs with Measures
			} catch (DatabaseOperationException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (IOException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (NotBoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (ObjectAccessException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (ObjectNotFoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (CubeNotFoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (CubeAccessException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} 
		} else{
			strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();
		}
		map.put("selectedCubeId", strCubeId);
		
		
		
		try {
			IDataObject cubeInfo = graphInfo.getCubeInfo();
			Vector<String> vector = new Vector<String>();
			if (strCubeId == null) {
				//cubeId = graphInfo.getCubeInfo().getCubeId();
				//strCubeId = graphInfo.getCubeInfo().getCubeId();
				List<ActiveGlobalVariableInfo> activeGolbalVariableList = graphInfo.getActiveTemplateProperties().getActiveGlobalVariableInfo(userInfo.getUserId());
				if(activeGolbalVariableList != null && activeGolbalVariableList.size()>0){
					for (ActiveGlobalVariableInfo activeGlobalVariableInfo : activeGolbalVariableList) {
						vector.add(activeGlobalVariableInfo.getGlobalVariableInfo().getGlobalVariableName());
					}
				}	
			}
			HashMap<String, Vector<String>> dimensionMap = metadataServiceUtil
					.getColumnsAndMeasuresMap(cubeInfo, userInfo,
							vector,true,true,false,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			List<String> dimensionList = new ArrayList<String>();
			Vector<String> dateDementionVector = cubeMetadataServiceUtil.getCubeDateColumn(cubeInfo, userInfo, false);
			Map<String,Integer> dateDemention = new HashMap();
			for (String object : dateDementionVector) {
				dateDemention.put(object,CubeUtil.getColumnType(object, graphInfo.getCubeInfo()));
			}
			//Added code to show count next to Dimension
			List<String> dimensions = new ArrayList<String>();
			List<String> dimensionListWithCount = new ArrayList<String>();
			dimensions.addAll(dimensionMap.get("1"));
			if(null != dimensionMap.get("6") && !dimensionMap.get("6").isEmpty())
				dimensions.addAll(dimensionMap.get("6"));
			
			if(null != dimensions) {
				try {
					String distinctFile = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/" + cubeInfo.getDataObjectId() +"/" + IDAIFQTDOperator.FILE_DISTINCT_PARQUEET;
					File file = new File(distinctFile);
					if(!file.exists()) {
						distinctFile = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/" + cubeInfo.getDataObjectId() + DataSetConstant.PUBLISH_DATASET+"/"+IDAIFQTDOperator.FILE_DISTINCT_PARQUEET;
					}
					for(int i = 0; i < dimensions.size(); i++) {
						String colName = (String)dimensions.get(i);
						long count = 0l;
						String cubePath = "";
						if(cubeInfo.getDataObjectId().endsWith(MashUpConstants.DATASETS_FILE_EXT)) {
							cubePath = distinctFile+"/"+colName+".parquet";
							file = new File(cubePath);
							if(!file.exists()) {
								cubePath = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/"
										+ cubeInfo.getDataObjectId()+ DataSetConstant.PUBLISH_DATASET;
								File publishFile = new File(cubePath);
								if(!publishFile.exists()) {
									cubePath = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/"
											+ cubeInfo.getDataObjectId()+ DataSetConstant.DEVELOPMENT;
								}
								count = sparkServiceUtil.biDataSetFromParquet(cubePath,cubeInfo.getDataObjectId()).select(colName).count();
							} else {
								count = cubeDataServiceUtil.biDistinctCountColumn(cubePath);
							}
						} else {
							cubePath = AppContextUtil.getAppPath() + AppConstants.CUBES_DIR + "/"+ cubeInfo.getDataObjectId() + "/" + IDAIFQTDOperator.FILE_DISTINCT_PARQUEET+"/"+colName+".parquet";
							count = cubeDataServiceUtil.biDistinctCountColumn(cubePath);
						}


						dimensionListWithCount.add(colName + " ("+ count+")");
					}


				} catch(Exception exc) {
					ApplicationLog.error(exc);
				}
			}
			if(dimensionListWithCount.isEmpty()) {
				for(int i = 0; i < dimensions.size(); i++) {
					dimensionListWithCount.add((String)dimensions.get(i));
				}
			}
			map.put("dimensionListForCount", dimensions);
			map.put("dimensionListWithCount", dimensionListWithCount);
			map.put("dateDemention", dateDemention);
			graphInfo.setDimensionListWithCount(dimensionListWithCount);
			//Added code to show count end
			
			dimensionList.addAll(dimensionMap.get("1"));
			dimensionList.addAll(dimensionMap.get("2"));
			//dimensionList.addAll(dimensionMap.get("3")); Resolved bug 14341
			//modified by harsh on 4 dec
			//dimensionList.addAll(dimensionMap.get("4"));
			dimensionList.addAll(dimensionMap.get("5"));
			dimensionList.addAll(dimensionMap.get("6"));

			List<String> measureList = metadataServiceUtil.getMeasureList(cubeInfo, userInfo,false,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));			OutlinerBean outlinerBean = new OutlinerBean();
			
			if (strCubeId != null) {
				graphInfo.getRowColumns().clear();;
				graphInfo.getColColumns().clear();;
				graphInfo.getDataColumns().clear();;
			}
			
			outlinerBean.setPtreeEnable(true);
			try {
				outlinerBean = smartenService.getOutlinerData(outlinerBean, graphInfo, userInfo, dimensionList, cubeInfo, false);
			} catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
			} catch( DatabaseOperationException e){
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
			} catch (Exception e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
			}
			map.put("dimensionList", dimensionList);			
			map.put("measureList", measureList);
			map.put("outlinerBean", outlinerBean);

		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
		} catch (DatabaseOperationException ex) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), ex);
		} catch (Exception e1) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e1);
		}

		////This is to show same color as the first color of the chart
		graphInfo.getGraphProperties().setColor("#8daacb");
		graphInfo.getGraphProperties().setLinecolor("#8daacb");
		map.put("measureLegend", false);
		map.put("objectType", AppConstants.SMARTEN);
		map.put("selectedGraphType", graphInfo.getGraphType());
		map.put("selectedRecommendedGraphType", graphInfo.getRecommendGraphType());
		map.put("recommendedGraphType", graphInfo.getRecommendGraphType());
		map.put("smartenLabelProperties", graphInfo.getGraphProperties().getSmartenProperties());
		map.put("graphProperties", graphInfo.getGraphProperties());
		map.put("smartenMeasureCurrentTabName", "M"+0);
		map.put("smartenMeasureSelectedTabNames", "M"+0);	
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());

		if(strCubeId != null) {
			map.put("Mode", AppConstants.NEW_MODE);			
			smartenService.setObjectMode(AppConstants.NEW_MODE);
			return new ModelAndView("smartview/outliner");	
		} else {
			map.put("Mode", AppConstants.OPEN_MODE);			
			smartenService.setObjectMode(AppConstants.OPEN_MODE);
			return new ModelAndView("graphOutliner");
		}
	}
	
	/**
	 * Show New Graph Type wizard
	 * @param map
	 * @return
	 * @throws ALSException 
	 */
	@RequestMapping(value="/smartenSelecttionWizard")
	@ResponseBody
	public ModelAndView showGraphTypeWizard(
			@RequestParam(value = "selectedCubeId", required = false) String strCubeId,
			@RequestParam(value = "screenWidth", required = false, defaultValue = "0") int windowScreenWidth,
			@RequestParam(value = "screenHeight", required = false, defaultValue = "0") int windowScreenHeight,
			@LoggedInUser UserInfo loggedInUser, HttpSession session,
			ModelMap map) throws ALSException {

		if(strCubeId != null) {
			session.removeAttribute("cubeList");
			Hashtable<String, Object> params = new Hashtable<String, Object>();

			params.put("objectPath", "");
			params.put("cubeId", strCubeId);
			params.put("windowScreenWidth", windowScreenWidth);
			params.put("windowScreenHeight", windowScreenHeight);
			try {
				graphInfo = smartenService.initializeGraph(AppConstants.NEW_MODE, params, loggedInUser, false);
				getDetailInfoMap().put(graphInfo.getGraphId(), graphInfo);
				//modified by harsh on 16 dec
				GeneralConfigurationInfo generalConfiguration =	generalConfigurationServiceUtil.getGeneralConfigurationInfo();
				
				if(generalConfiguration.getEditByCreator()!= null && generalConfiguration.getEditByCreator().intValue() == 0)
				{
					graphInfo.getGraphProperties().setEditByCreator(true);
				}
				else
				{
					graphInfo.getGraphProperties().setEditByCreator(false);
				}
			
		
				smartenService.setFirstTime(true);
			} catch (DatabaseOperationException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} catch (IOException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} catch (NotBoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} catch (ObjectAccessException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} catch (ObjectNotFoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} catch (CubeNotFoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} catch (CubeAccessException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { loggedInUser.getUsername(), smartenService.getObjectMode() }), e);
			} 

		} else{
			strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();
		}
		map.put("selectedCubeId", strCubeId);
		return new ModelAndView("graphTypeWizard");
	}
	
	/**
	 * Show save as dialog
	 * @param map
	 * @return
	 */
	@RequestMapping(value="/showsaveassmarten")
	@ResponseBody
	public ModelAndView showSaveAsDialog(@LoggedInUser UserInfo userInfo, ModelMap map,
			@RequestParam(value = "smartenChartHeight", required = false) String smartenChartHeight){
		
		boolean flag = userInfo.isSuperAdmin();

		Map<String, Object> treeNodeMap = repositoryServiceUtil.getTreeObjectMap(userInfo, AppConstants.PROJECTS_DIR, flag);

		FolderInfo currentFolderInfo = new FolderInfo();
		currentFolderInfo.setParentFolderId("folders");
		currentFolderInfo.setParentFolderHierarchy("folders");

		graphInfo.getGraphProperties().setSmartenChartHeight(Integer.parseInt(smartenChartHeight));
		map.put("currentFolderInfo", currentFolderInfo);
		map.put("treeNodeMap", treeNodeMap);
		map.put("graphInfo", graphInfo);
		
		if (graphInfo.getGraphMode() == AppConstants.NEW_MODE)
			return new ModelAndView("smartenSaveDialog");
		else
			return new ModelAndView("smartensaveas");
	}
	
	/**
	 * Show export dialog
	 */
	@RequestMapping(value="/showexportsmarten")
	@ResponseBody
	public ModelAndView showExportDialog(ModelMap map,@LoggedInUser UserInfo userInfo,@RequestParam(value = "objectid", required = false) String strObjectId){
		map.put("objectid",strObjectId);
		return new ModelAndView("exportsmarten");
	}
	
	/**
	 * show graph Drill down browsing Dialog 
	 * @param map
	 * @return
	 */
	@RequestMapping(value="/showdrilldownbrowsing")
	@ResponseBody
	public ModelAndView showDrillDownBrowsingDialog(@RequestParam("cTime") long lTime, @LoggedInUser UserInfo userInfo, ModelMap map){

		SmartenHierarchyTree graphHierachyTree = null;
		boolean bColLabelFirst = false;
		boolean bCombineGraph = false;
		HashtableEx ddvmValuesMap = null;
		try {
			ddvmValuesMap = smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());
			graphHierachyTree = smartenService.getGraphHierarchyTree(graphInfo,ddvmValuesMap);
			Vector  theRowColLabels = graphInfo.getRowColumns();
			Vector  theRowColLabels2 = graphInfo.getLineGraphRowLabelsForCombinedGraph();
			Vector  theColColLabels =graphInfo.getColColumns();
			if( graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH )
			{
				bCombineGraph = true;
				if( theRowColLabels.size() <= 1 && theRowColLabels2.size() <= 1 )
					bColLabelFirst = true;
			}
			else
			{
				/*if( theRowColLabels.size() <= 1 || graphInfo.getGraphPanel().getMultipleYAxisLabelsEnable())
					bColLabelFirst = true;*/
				if(theRowColLabels.size() <=1 && theColColLabels.size() > 0){
					bColLabelFirst = true;
				}
			}
			
			smartenService.setPackDDVMInfo(graphInfo, ddvmValuesMap);
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_OPEN_DRILLDOWNBROWSING_DIALOG",
					new Object[] { userInfo.getUsername(), getObjectDisplayName() }), e);
		}
		map.put("graphHierachyTree", graphHierachyTree);
		map.put("isColFirst", bColLabelFirst);
		map.put("time", lTime);
		map.put("rowPath", graphInfo.getDrilldownRowPath());
		map.put("colPath", graphInfo.getDrilldownColumnPath());
		map.put("bCombineGraph", bCombineGraph);
		map.put("ddvmValuesMap", ddvmValuesMap);
		return new ModelAndView("drilldownvrowsing");
	}	
	
	/**
	 * show Data operation Dialog 
	 * @param map
	 * @return ModelAndView
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/showdataoperation")
	@ResponseBody
	public ModelAndView showDataOperatinoDialog(ModelMap map, @LoggedInUser UserInfo userInfo){
		
		Vector dateDimensions = null;
		Vector dimension = null;
		try {
			dateDimensions = metadataServiceUtil.getDateTimension(graphInfo.getCubeInfo(),
					userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			dimension = metadataServiceUtil.getDimensionColumns(graphInfo.getCubeInfo(), userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
		} catch (Exception e) {
			ApplicationLog.error(e);
		}
		Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
		if(dateDimensions != null && dateDimensions.size() > 0) {
		 dataMap = StringUtil.getDataOperationMap(true, isMDXCube()); 
		} else {
		 dataMap = StringUtil.getDataOperationMap(false, isMDXCube());	
		}
		Map<String,String> messureMap = new HashMap<String, String>();
		Map<String,String> postaggregationMap = new HashMap<String, String>();
		Map<String,Integer> yAxisComputationType = new HashMap<String, Integer>();
		Map<String,String> timeDimensionMap = new HashMap<String, String>();
		Map<String,String> agregationMap = new HashMap<String, String>();
		yAxisComputationType = graphInfo.getyAxisComputationType();
		Map<String,String> distinctCountMap = new HashMap<String, String>();
		Vector<String> dataLabels = (Vector<String>) graphInfo.getDataColumns().clone();
		
		if (graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH) {
			if(graphInfo.getLineGraphDataLabelsForCombinedGraph() != null && graphInfo.getLineGraphDataLabelsForCombinedGraph().size() > 0){
				dataLabels.addAll(graphInfo.getLineGraphDataLabelsForCombinedGraph());
			}	
		}
		
		//Setting Measures in Data Operations dialogue for Map
		if (graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP && null != graphInfo.getMeasureTitleList() && !graphInfo.getMeasureTitleList().isEmpty()) {
			dataLabels = new Vector<String>();
			dataLabels.addAll(graphInfo.getMeasureTitleList());
		}
		
		List<ActiveUDDCInfo> actUddcInfo = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
		if(actUddcInfo != null && actUddcInfo.size() > 0) {
			for (ActiveUDDCInfo activeUDDCInfo : actUddcInfo) {
				dataLabels.add(activeUDDCInfo.getUddcTemplateInfo().getColumnName());
			}
		}
		if(!graphInfo.isPerformAggregation())
			dataLabels.remove(graphInfo.getCategoryMeasureLabel());
		if (dataLabels != null && dataLabels.size() > 0) {
			for (String dataLabel : dataLabels) {
				String intTotal = "0";
				String postTotal = "0";

				if(yAxisComputationType != null && yAxisComputationType.size() > 0){
					if(yAxisComputationType.containsKey(dataLabel)) {    
						intTotal = ""+yAxisComputationType.get(dataLabel);
					}
				}
				postTotal = intTotal;
				LMRecentInfo lmRecentInfo = null;
				if(graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo() != null) {
					lmRecentInfo = (LMRecentInfo) graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo().get(dataLabel);
				}
				if (lmRecentInfo == null) {
					lmRecentInfo = new LMRecentInfo();
				} else {
					if(lmRecentInfo.getOperationType() == ICubeConstants.LEASTRECENT) {
						intTotal = Integer.toString(ICubeConstants.LEAST_RECENT);
					} else {
						intTotal = Integer.toString(ICubeConstants.MOST_RECENT);
					}
					postTotal = StringUtil.getDataOperationCMDMap().get(Integer.parseInt(postTotal));
				}
				
				String distinctCountColumn = null;
				if(graphInfo.getObjectCubeDefinition().getDistinctCountMap() != null) {
					distinctCountColumn = (String) graphInfo.getObjectCubeDefinition().getDistinctCountMap().get(dataLabel);
				}
				if(distinctCountColumn == null) {
					distinctCountColumn = "";
				} else {
					intTotal = Integer.toString(ICubeConstants.DISTINCT);
				}
				messureMap.put(dataLabel, intTotal);
				postaggregationMap.put(dataLabel, postTotal);
				String timeDimension = lmRecentInfo.getTimeDimension();
				int agregation = lmRecentInfo.getAggregationType();
				timeDimensionMap.put(dataLabel, timeDimension);
				agregationMap.put(dataLabel, ""+agregation);
				distinctCountMap.put(dataLabel, distinctCountColumn);
			}
		}
		Map<String, Map<String,Object>> measureDataMap = smartenService.getSelectedDataOperation(graphInfo, messureMap, 
				(dateDimensions != null && dateDimensions.size() > 0),false);
		Map<String, Map<String,Object>> measureAggDataMap = smartenService.getSelectedDataOperation(graphInfo, messureMap, 
				(dateDimensions != null && dateDimensions.size() > 0),true);
		map.put("datedimension", dateDimensions);
		map.put("timeDimensionMap", timeDimensionMap);
		map.put("agregationMap", agregationMap);
		map.put("postaggregationMap", postaggregationMap);
		map.put("dataMap", dataMap);
		map.put("messureMap", messureMap);
		
		//For Main Data Operation Dialog: create display map to show column aliases in UI. Keeps original measure keys unchanged for backend processing
		Map<String, String> colLabels = graphInfo.getGraphProperties().getColLabelsMap();
		Map<String, String> messureDisplayMap = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : messureMap.entrySet()) {
		    String k = entry.getKey();

		    messureDisplayMap.put(k, (colLabels != null && colLabels.containsKey(k))
		                             ? colLabels.get(k) : k);
		}

		map.put("messureDisplayMap", messureDisplayMap);
		map.put("dimension", dimension);
		
		// Dimension Display Map (for UI only)
		Map<String, String> dimensionDisplayMap = new LinkedHashMap<String, String>();
		if (dimension != null) {
		    for (Object dim : dimension) {
		        String d = dim.toString();
		        dimensionDisplayMap.put(d, (colLabels != null && colLabels.containsKey(d)) 
		                                   ? colLabels.get(d) : d);
		    }
		}
		map.put("dimensionDisplayMap", dimensionDisplayMap);
		map.put("distinctCountMap", distinctCountMap);
		map.put("distinctOprMap", graphInfo.getObjectCubeDefinition().getDistinctCountOperationMap());
		map.put("selectedoperation", measureDataMap);
		map.put("selectedpostoperation", measureAggDataMap);
		map.put("isFromSmarten", true);
		return new ModelAndView("dataoperationgraph");
	}

	@RequestMapping(value = "/lmrecentdataoperation")
	@ResponseBody
	public ModelAndView lmrecentdataoperation(
			ModelMap modelMap,
			@RequestParam(value = "columnName", required = false) String strColumnName,
			@RequestParam(value = "operationtype", required = false) String strOperationtype, @LoggedInUser UserInfo userInfo) {
		
		String strPostAgg = ALSCommandNameList.CMD_NAME_QT_TOTAL;
		String strOpertionType = "";
		if(strOperationtype.equals(ResourceManager.getString("LBL_LEAST_RECENT"))) {
			strOpertionType = ""+ICubeConstants.LEASTRECENT;
		} else {
			strOpertionType = ""+ICubeConstants.MOSTRECENT;
		}
		
		Vector dateDimensions = null;
		try {
			dateDimensions = metadataServiceUtil.getDateTimension(graphInfo.getCubeInfo(),
					userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
		} catch (Exception e) {
			ApplicationLog.error(e);
		}
		
		LMRecentInfo lmRecentInfo = (LMRecentInfo) graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo().get(strColumnName);
		if (lmRecentInfo == null) {
			lmRecentInfo = new LMRecentInfo();
		}
		Map<String,Integer> yAxisComputationType = new HashMap<String, Integer>();
		yAxisComputationType = graphInfo.getyAxisComputationType();
		
		/*Vector<String> dataLabels = graphInfo.getDataColumns();*/
		
			int intTotal = 0;
			if(yAxisComputationType != null && yAxisComputationType.size() > 0){
				if(yAxisComputationType.containsKey(strColumnName)) {    
					intTotal = yAxisComputationType.get(strColumnName);
				}
			}
			strPostAgg = StringUtil.getDataOperationCMDMap().get(intTotal);
		String timeDimension = lmRecentInfo.getTimeDimension();
		int agregation = lmRecentInfo.getAggregationType();
		
		
		modelMap.put("datedimension", dateDimensions);
		modelMap.put("timeDimension", timeDimension);
		modelMap.put("agregation", agregation);
		modelMap.put("objectType", 0);
		modelMap.put("selColumn", strColumnName);
		modelMap.put("bmostleast", true);
		modelMap.put("rlmrecent", strOpertionType);
		modelMap.put("postAgg", strPostAgg);
		
		if(strOpertionType.equals(""+ICubeConstants.LEASTRECENT))
			return new ModelAndView("analysisLeastRecentDataOperation");
		else 
			return new ModelAndView("analysisMostRecentDataOperation");
	}
	
	@RequestMapping(value = "/applyrecentdataoperation")
	@ResponseBody
	public Object applyLMRecentDataOperation(
			ModelMap modelMap,
			@RequestParam(value = "objectType", required = false) String strObjectType,
			@RequestParam(value = "selColumn", required = false) String columnName,
			@RequestParam(value = "bmostleast", required = false) String bmostleast,
			@RequestParam(value = "rlmrecent", required = false) String operaitontype,
			@RequestParam(value = "agrrgation", required = false) String agrrgation,
			@RequestParam(value = "timedimension", required = false) String timedimension, 
			@RequestParam(value = "cellreferance", required = false) String strCellRef,
			@RequestParam(value = "postaggregation", required = false) String strPostOperation,
			HttpServletResponse response, @LoggedInUser UserInfo userInfo) {

		try {
			if (operaitontype.equals("-1")) {
				HashMap tConditions = (HashMap) graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo();
				tConditions.remove(columnName);
				graphInfo.getObjectCubeDefinition().setLmRecentSettingsInfo(tConditions);
			} else {

				LMRecentInfo lmRecentInfo = new LMRecentInfo();
				lmRecentInfo.setAggregationType(Integer.parseInt(agrrgation));
				lmRecentInfo.setColumnName(columnName);
				lmRecentInfo.setOperationType(Integer.parseInt(operaitontype));
				lmRecentInfo.setTimeDimension(timedimension);
				graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo().put(columnName, lmRecentInfo);
				Map<String,Integer> dataMap = new LinkedHashMap<String,Integer>();
				dataMap = StringUtil.getDataOperationValueMap();
				int dataType = dataMap.get(strPostOperation);
				graphInfo.getyAxisComputationType().put(columnName, dataType);
				smartenService.checkAndResetDrillUp(graphInfo, true);
			}

		} catch (Exception e) {
           ApplicationLog.error(e);
		}
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_GRAPH_DATA_OPERATION"), AppConstants.DETAIL,userInfo);
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return refreshObjectData(null,response, userInfo, modelMap);
	}
	
	/**
	 * apply data operation .
	 * @param value 
	 * @return String
	 */
	@RequestMapping(value="/applyDataOperation")
	@ResponseBody
	public Object applyDataOperation(@RequestParam(value="value", required=false, defaultValue="") String strValue,
			HttpServletResponse response, ModelMap map, @LoggedInUser UserInfo userInfo,HttpServletRequest request) {

		Map<String,Integer> yAxisComputationType = new HashMap<String, Integer>();
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Data operation","set Data operation",Thread.currentThread(),userInfo,null);
		try {
			Vector<String> dataLabels = (Vector<String>) graphInfo.getDataColumns().clone();
			if (graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH) {
			if(graphInfo.getLineGraphDataLabelsForCombinedGraph() != null && graphInfo.getLineGraphDataLabelsForCombinedGraph().size() > 0){
				dataLabels.addAll(graphInfo.getLineGraphDataLabelsForCombinedGraph());
			}
			}

			//Applying Multiple Measures in Data Operations (Left Panel) for Map
			if (graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP && null != graphInfo.getMeasureTitleList() && !graphInfo.getMeasureTitleList().isEmpty()) {
				dataLabels = new Vector<String>();
				dataLabels.addAll(graphInfo.getMeasureTitleList());
			}
			List<ActiveUDDCInfo> actUddcInfo = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
			if(actUddcInfo != null && actUddcInfo.size() > 0) {
				for (ActiveUDDCInfo activeUDDCInfo : actUddcInfo) {
					dataLabels.add(activeUDDCInfo.getUddcTemplateInfo().getColumnName());
				}
			}
			if(!graphInfo.isPerformAggregation())
				dataLabels.remove(graphInfo.getCategoryMeasureLabel());
			for (String columnName : dataLabels) {
				    int iType = 0;
					String computationType = StringUtil.null2String(request.getParameter(columnName));
					
					if(computationType.equals(""+ICubeConstants.totalTypeNone))
					{
						graphInfo.setNoneDataOperation(true);
						computationType = ""+ICubeConstants.totalTypeNone;//set it to sum
					}
					else
						graphInfo.setNoneDataOperation(false);
					
					if(computationType.equals(""))
						computationType = "0";
					
					if(Integer.parseInt(computationType)==ICubeConstants.LEAST_RECENT || Integer.parseInt(computationType) == ICubeConstants.MOST_RECENT) {
						int operaitontype = 0;
						LMRecentInfo lmRecentInfo = new LMRecentInfo();
						lmRecentInfo.setAggregationType(Integer.parseInt(StringUtil.null2String(request.getParameter(columnName+"_agrrgation"))));
						lmRecentInfo.setColumnName(columnName);
						if(Integer.parseInt(computationType)==ICubeConstants.LEAST_RECENT) {
							operaitontype = ICubeConstants.LEASTRECENT;
						} else {
							operaitontype = ICubeConstants.MOSTRECENT;
						}
						lmRecentInfo.setOperationType(operaitontype);
						lmRecentInfo.setTimeDimension(StringUtil.null2String(request.getParameter(columnName+"_timedimension")));
						if(graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo() == null){
							graphInfo.getObjectCubeDefinition().setLmRecentSettingsInfo(new HashMap());
						}
						graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo().put(columnName, lmRecentInfo);
						Map<String,Integer> dataMap = new LinkedHashMap<String,Integer>();
						dataMap = StringUtil.getDataOperationValueMap();
						iType = dataMap.get(StringUtil.null2String(request.getParameter(columnName+"_postaggregation")));
						if(graphInfo.getObjectCubeDefinition().getDistinctCountMap() != null) {
						     graphInfo.getObjectCubeDefinition().getDistinctCountMap().remove(columnName);
						}
					} else if(Integer.parseInt(computationType)==ICubeConstants.DISTINCT){
						if(graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo() != null) {
						     graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo().remove(columnName);
						}
						String dimensionName = StringUtil.null2String(request.getParameter(columnName+"_distinctCountdimension"));
						int operation = 1;
						if(request.getParameter(columnName+"_agrrgation_val") != null)
							operation = Integer.parseInt(StringUtil.null2String(request.getParameter(columnName+"_agrrgation_val")));
						else
							operation = 2;//left pannel date operation(count) 10 Apr 2018
						if(graphInfo.getObjectCubeDefinition().getDistinctCountMap() == null) {
							graphInfo.getObjectCubeDefinition().setDistinctCountMap(new HashMap());
						}
						if(graphInfo.getObjectCubeDefinition().getDistinctCountOperationMap() == null) {
							graphInfo.getObjectCubeDefinition().setDistinctCountOperationMap(new HashMap());
						}
						graphInfo.getObjectCubeDefinition().getDistinctCountMap().put(columnName, dimensionName);
						graphInfo.getObjectCubeDefinition().getDistinctCountOperationMap().put(columnName, operation);
					} else {
						try {
							iType = Integer.parseInt(computationType);
							if(graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo() != null) {
						     graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo().remove(columnName);
							}
							if(graphInfo.getObjectCubeDefinition().getDistinctCountMap() != null) {
							     graphInfo.getObjectCubeDefinition().getDistinctCountMap().remove(columnName);
							}
						} catch (Exception ex) {}
					}
					
					yAxisComputationType.put(columnName, iType);
					
				}
			graphInfo.setyAxisComputationType(yAxisComputationType);
			smartenService.checkAndResetDrillUp(graphInfo, true);
		} catch (Exception ex) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_APPLY_DATA_OPERATION", 
					new Object[] { userInfo.getUsername(), getObjectDisplayName() }), ex);	
			return ResourceManager.getString("ERROR_MSG_FAILED_APPLY_DATA_OPERATION", new Object[] {ex.getMessage()});
		}
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_GRAPH_DATA_OPERATION"), AppConstants.DETAIL,userInfo);
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return refreshObjectData(null,response, userInfo, map);
	}

	/**
	 * Show Group dialog.
	 * 
	 * @param map
	 *            model map object
	 * @return model and view response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/showgroupdailog")
	@ResponseBody
	public ModelAndView showGroupDialog(ModelMap map, @LoggedInUser UserInfo userInfo) {
		
		CubeVector cols = getColLabelNameVector(null);
		CubeVector rows = getRowLabelNameVector(null);
		List<Group> groupList = graphInfo.getGroupList();
		List<String> dimension = new ArrayList<String>();
		dimension.addAll(cols);
		dimension.addAll(rows);
		if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
				graphInfo.getGraphType() == GraphConstants.D3_TREEMAP ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR)
		{
			map.addAttribute("dimensionList", graphInfo.getDimensionTitleList());
			
		}else {
			map.addAttribute("dimensionList", dimension);
		}	
		
		map.addAttribute("groupList", groupList);
		map.addAttribute("groupVO", new Group());
		map.addAttribute("isFromSmarten", true);
		return new ModelAndView("groupgraph");
	}
	
	/*@RequestMapping(value="/showdatagroupdailog")
	public @ResponseBody ModelAndView showDataGroupDialog(ModelMap map, @LoggedInUser UserInfo userInfo,
			@RequestParam(value="isEdit", defaultValue="false") boolean isEdit,
			@RequestParam(value="groupName",  required=false) String groupName,
			@RequestParam(value="groupLabel", required=false) String groupLabel,
			@RequestParam(value="groupId", required=false) Integer groupId,
			@RequestParam(value="columnName", required=false) String columnName,
			@RequestParam(value="values", required=false) String values,
			@RequestParam(value = "appendVal", required=false, defaultValue = "0") String appendVal,
			@RequestParam(value="groupIndex", required=false) Integer index) {
		
		CubeVector cols = getColLabelNameVector(null);
		CubeVector rows = getRowLabelNameVector(null);
		List<Group> groupList = graphInfo.getGroupList();
		List<String> dimension = new ArrayList<String>();
		dimension.addAll(cols);
		dimension.addAll(rows);
		map.addAttribute("dimensionList", dimension);
		map.addAttribute("groupList", groupList);
		map.addAttribute("groupVO", new Group());
		map.addAttribute("isForEdit",isEdit);
		if(isEdit == true) {
			Group group = new Group();
			group.setGroupId(groupId);
			group.setColumnName(columnName);
			group.setGroupName(groupName);
			group.setGroupLabel(groupLabel);
			group.setValues(values);
			
			map.addAttribute("groupVO",group);
			map.addAttribute("groupIndex",index);
			map.put("appendVal", appendVal);
			map.put("selectedValues", values.split(","));
		}
		
		return new ModelAndView("filters/addEditDataGroup");
	}*/
	
	/*@RequestMapping(value="/showsmartengrouplist")
	@ResponseBody
	public ModelAndView showSmartenGroupDialog(ModelMap map, @LoggedInUser UserInfo userInfo) {
		
		CubeVector cols = getColLabelNameVector(null);
		CubeVector rows = getRowLabelNameVector(null);
		List<Group> groupList = graphInfo.getGroupList();
		List<String> dimension = new ArrayList<String>();
		dimension.addAll(cols);
		dimension.addAll(rows);
		if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
				graphInfo.getGraphType() == GraphConstants.D3_TREEMAP ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR)
		{
			map.addAttribute("dimensionList", graphInfo.getDimensionTitleList());
			
		}else {
			map.addAttribute("dimensionList", dimension);
		}	
		
		map.addAttribute("groupList", groupList);
		map.addAttribute("groupVO", new Group());
		map.addAttribute("isFromSmarten", true);
		return new ModelAndView("filters/dataGroupList");
	}*/
	
	/**
	 * Perform Save Group Operation.
	 * 
	 * @return 'Success' if operation is successful otherwise error message.
	 */
	@RequestMapping (value = "/saveGroup")
	@ResponseBody
	public Object saveGroup(@LoggedInUser UserInfo userInfo,  ModelMap map, HttpServletResponse response) {
		
		Object status = "";
		try {
			smartenService.setGroupDetail(graphInfo);
			auditUserActionLog(ResourceManager.getString("LBL_SAVE_AND_APPLY_GRAPH_GROUP"), AppConstants.DETAIL,userInfo);
			status = refreshObjectData(null,response, userInfo, map);
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_SAVE_GROUP", new Object[]{userInfo.getUsername(), getObjectDisplayName() }), e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_SAVE");
		}
		return status;
	}
	
	/**
	 * Perform Add Group Operation.
	 * 
	 * @param map
	 *            model map object
	 * @param groupVO
	 *            group value object
	 * @return model and view response
	 */
	@RequestMapping(value="/addGroup")
	@ResponseBody
	public ModelAndView addNewGroup(ModelMap map, @ModelAttribute Group groupVO,@LoggedInUser UserInfo userInfo) {
		
		List<Group> groupList = graphInfo.getGroupList();
		int id = 0;
		if(groupVO.getGroupId() <= groupList.size())
			id = groupList.size()+100;
		else
			id = groupVO.getGroupId();
		String value = StringUtil.unescapeHtmlUtil(groupVO.getValues());
		String label = StringUtil.unescapeHtmlUtil(groupVO.getGroupLabel());
		String name = StringUtil.unescapeHtmlUtil(groupVO.getGroupName());
		groupVO.setGroupId(id);
		groupVO.setValues(value);
		groupVO.setGroupLabel(label);
		groupVO.setGroupName(name);
		groupList.add(groupVO);
		auditUserActionLog(ResourceManager.getString("LBL_ADD_GRAPH_GROUP"), AppConstants.DETAIL,userInfo);
		map.addAttribute("groupList", groupList);
		return new ModelAndView("graph/addGroupResponse");
	}
	
	/**
	 * This method is use to get Cube Dimension List for group Access By User
	 * 
	 * @param strSearchStr
	 *            search string
	 * @param strDimensionName
	 *            dimnesion name
	 * @param hideUdhc
	 *            flag for hide udhc or not
	 * @param showGV
	 *            flag for show global variable or not
	 * @param showAllData
	 *            flag for show all data
	 * @return list of selected item objecs
	 */
	
	
	/**
	 * Perform Edit Group Label Operation.
	 * 
	 * @param newValue
	 *            new group label
	 * @param index
	 *            editable group index
	 * @return 'Success' if operation is successful otherwise error message.
	 */
	@RequestMapping (value = "/editGroup")
	@ResponseBody
	public String editGroupLabel(@RequestParam("value") String newValue, @RequestParam("name") String index, @LoggedInUser UserInfo userInfo) {
		
		int ind = -1;
		String status = "";
		try {
			if (newValue != null && index != null && !newValue.trim().isEmpty()) {
				List<Group> groupList = graphInfo.getGroupList();
				try { ind = Integer.parseInt(index); }catch(Exception e){}
				Group objGroup = groupList.get(ind);
				if (objGroup != null) {
					objGroup.setGroupLabel(newValue);
				}
			} else {
				return "";
			}
			status = AppConstants.SUCCESS_STATUS;
		}catch(Exception ex) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_UPDATE_GROUP", new Object[]{userInfo.getUsername(), getObjectDisplayName() }), ex);
			status = ResourceManager.getString("ERROR_FAILED_TO_UPDATE_OBJECT");
		}
		auditUserActionLog(ResourceManager.getString("LBL_EDIT_GRAPH_GROUP_LABEL"), AppConstants.DETAIL,userInfo);
		return status;
	}
	
	/**
	 * Perform Remove Group Operation.
	 * 
	 * @param groupIndex
	 *            deleted group index
	 * @return 'Success' if operation is successful otherwise error message.
	 */
	@RequestMapping (value = "/deleteGroup")
	@ResponseBody
	public String removeGroup(@RequestParam("groupId") Integer groupIndex, @LoggedInUser UserInfo userInfo) {
		
		String strGroupName = "";
		String status = "";
		int grpIndex = 0;
		try {
		if (groupIndex != null ) {
			List<Group> groupList = graphInfo.getGroupList();
			
			//Added code to solve bug #11194 start
			for(int j=0;j<groupList.size();j++)
			{
				if(groupList.get(j).getGroupId() == groupIndex.intValue())
				{
					grpIndex = j;
				}
			}
			//Added code to solve bug #11194 end
			
			Group deleteGrp = groupList.get(grpIndex);
			if (deleteGrp != null) {
			strGroupName = deleteGrp.getGroupName(); 
			String groupValues = deleteGrp.getValues();
			String selectedValeus[] = StringUtil.tokenize(groupValues, ",");
			removeFromPackColumnDDVM(deleteGrp.getColumnName(), selectedValeus[0], graphInfo,userInfo);
			groupList.remove(grpIndex);
			}
		}
		status = AppConstants.SUCCESS_STATUS;
		} catch(CubeException cx) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_DELETE_GROUP", new Object[]{userInfo.getUsername(), getObjectDisplayName() }), cx);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_DELETE")+ " : "+strGroupName;
		} catch (Exception e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_DELETE_GROUP", new Object[]{userInfo.getUsername(), getObjectDisplayName() }), e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_DELETE")+ " : "+strGroupName;
		}
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_GRAPH_GROUP"), AppConstants.DETAIL,userInfo);
		return status;
	}
	
	/**
	 * This method is used to unpack column from packed column.
	 * 
	 * @param colName
	 *            the column name
	 * @param actualTextValues
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void removeFromPackColumnDDVM(String colName, String actualTextValues, SmartenInfo graphInfo,UserInfo userInfo) throws CubeException {

		if ( graphInfo.getPackDDVMValues() != null) {
			Vector packValuesVect = (Vector) graphInfo.getPackDDVMValues().get(colName);
			if (packValuesVect != null && actualTextValues != null && !"".equals(actualTextValues)) {

				String[] actualText = actualTextValues.split(",");
				Vector packIndexesToDelete = new Vector();
				Vector tempPackValuesVect = new Vector();
				for (int i = 0; i < actualText.length; i++) {
					for (int j = 0; j < packValuesVect.size(); j++) {
						String[] values = (String[]) packValuesVect.get(j);
						if (actualText[i].equals(values[0])) {
							packIndexesToDelete.add(new Integer(j));
							break;
						}
					}
				}
				for (int i = 0; i < packValuesVect.size(); i++) {
					if (!packIndexesToDelete.contains(new Integer(i))) {
						tempPackValuesVect.add(packValuesVect.get(i));
					}
				}
				if (tempPackValuesVect.size() == 0) {
					graphInfo.getPackDDVMValues().remove(colName);
				} else {
					graphInfo.getPackDDVMValues().put(colName, tempPackValuesVect);
				}
			}
		}
	}

	/**
	 * show Group dialog.
	 * @param map ModelMap
	 * @param userInfo UserInfo
	 * @return ModelAndView
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/showgraphdata")
	@ResponseBody
	public ModelAndView showGraphDataDialog(ModelMap map, @LoggedInUser UserInfo userInfo) { 
		ApplicationLog.debug("showGraphDataDialog START");
		long showGraphDataDialog = System.currentTimeMillis();	
		Pagination pagination = new Pagination();
		int iPageNo=1;
		int iSortColumn = 0;
		List graphLDataList = null;
		List headerList = null;
		int iTotalRecord = 0;
		try {
			detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"show data","Read data",Thread.currentThread(),userInfo,null);
			boolean isNumber = false;
			long formatStart = System.currentTimeMillis();
			ApplicationLog.debug(" getFormatedDataList START");
			graphLDataList = (List) smartenService.getFormatedDataList(graphInfo, userInfo).clone();
			long formatEnd = System.currentTimeMillis();
			//String timeTake = CalendarUtil.getHMSfromMillSec(formatEnd - formatStart);
			ApplicationLog.debug(" getFormatedDataList ENDS "+(formatEnd - formatStart));
			
			headerList = (List) graphLDataList.get(0);
			if(graphInfo.getGraphType() == GraphConstants.CANDLE_STICK_GRAPH
					 || graphInfo.getGraphType() == GraphConstants.HIGH_LOW_OPEN_CLOSE_GRAPH)
			{
				String[] splitString = ((String) headerList.get(0)).split(",");
				headerList.set(0, splitString[0]);
			}
			
			int ilength = headerList.size(); 
			iSortColumn = ilength - 1;
			if(headerList != null && headerList.size() >= iSortColumn) {
			  String columnName = (String) headerList.get(iSortColumn);
			  int type = CubeUtil.getColumnType(columnName, graphInfo.getCubeInfo());
			  int sortColumnType = GeneralFiltersUtil.getColType(type);
			  if(sortColumnType == GeneralFiltersUtil.NUMERIC_DIMENSION_COL) {
				  isNumber = true;
			  }
			}
			graphLDataList.remove(0);
			iTotalRecord = graphLDataList.size();
			pagination = GeneralUtil.getPaginationInfo(iPageNo, iTotalRecord,15);
			////do not sort until sort order is changed by user(2 JAN 2018)
			//graphLDataList = sortGraphData(graphLDataList,iSortColumn,0,isNumber);
			ApplicationLog.debug(" formattedGraphData START");
			long sublist = System.currentTimeMillis();
			graphLDataList =  graphLDataList.subList(pagination.getStartIndex(), pagination.getEndIndex());
			long sublistEnd = System.currentTimeMillis();
			//String sublistTime = CalendarUtil.getHMSfromMillSec(sublistEnd - sublist);
			ApplicationLog.debug(" sublist Time"+(sublistEnd - sublist));
		} catch(Exception ex) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_OPEN_GRAPH_DATA", new Object[]{userInfo.getUsername(), getObjectDisplayName() }), ex);
		}
		map.addAttribute("headerList", headerList);
		map.addAttribute("sortOption", iSortColumn +":0");
		map.addAttribute("sortType", iSortColumn);
		map.addAttribute("totalPage",  pagination.getTotalPage());
		map.addAttribute("pageNo", 1);
		map.addAttribute("startPageIndex", 1);
		map.addAttribute("endPageIndex", 10);
		if(iTotalRecord < 10)
			map.addAttribute("endPageIndex", iTotalRecord);
		map.addAttribute("iTotalRecord", iTotalRecord);
		map.addAttribute("isSmarten", true);
		map.put("graphLDataList", graphLDataList);
		auditUserActionLog(ResourceManager.getString("LBL_SHOW_GRAPH_DATA_DIALOG"), AppConstants.DETAIL,userInfo);
		long showGraphDataDialogEnd = System.currentTimeMillis();
		//String timeTake = CalendarUtil.getHMSfromMillSec(showGraphDataDialogEnd - showGraphDataDialog);
		ApplicationLog.debug(" showGraphDataDialog ENDS time "+(showGraphDataDialogEnd - showGraphDataDialog));
		return new ModelAndView("/graph/graphData");
	}
	
	/**
	 * @param graphData graphDataList
	 * @param sortIndex int
	 * @param sortOrder int 
	 * @return List of graphdata
	 */
	public List<List> sortGraphData(List<List> graphData,int sortIndex,int sortOrder,boolean isNumber){
		List<List> tmpGraphData = graphData;
		for (int i = 0; i < tmpGraphData.size(); i++) {
			for (int j = i+1; j < tmpGraphData.size(); j++) {
				List l1 = tmpGraphData.get(i);
				List l2 = tmpGraphData.get(j);
				Object val1 = l1.get(sortIndex);
				Object val2 = l2.get(sortIndex);
				int result = 0;
				boolean flag = false;
				if(isNumber) {
					try {
					if(Integer.parseInt(val1.toString()) > Integer.parseInt(val2.toString())){
						result = 1;
					} else {
						result = -1;
					}
					} catch (Exception e) {
						ApplicationLog.error(e);
						flag = true;
					} finally{
						if (flag) {
							result = compareData(val1,val2);
						}
					}
				} else {
					result = compareData(val1,val2);
				}
				if(sortOrder == 0){
					if(result == 0){
						
					} else if(result > 0) {
						graphData.set(i, l2);
						graphData.set(j, l1);
					} else {
						graphData.set(i, l1);
						graphData.set(j, l2);
					}
				} else {
					if(result == 0){
						
					} else if(result < 0) {
						graphData.set(i, l2);
						graphData.set(j, l1);
					} else {
						graphData.set(i, l1);
						graphData.set(j, l2);
					}
				}
			}	
		}
		return graphData;
		
	}
	
	/**
	 * @param val1 object value
	 * @param val2 object value
	 * @return int
	 */
	private int compareData(Object val1,Object val2){
		if (Byte.class.isInstance(val1)) {
			return ((Byte) val1).compareTo((Byte) val2);
		} else if (Character.class.isInstance(val1)) {
			return ((Character) val1).compareTo((Character) val2);
		} else if (Short.class.isInstance(val1)) {
			return ((Short) val1).compareTo((Short) val2);
		} else if (Integer.class.isInstance(val1)) {
			return ((Integer) val1).compareTo((Integer) val2);
		} else if (Long.class.isInstance(val1)) {
			return ((Long) val1).compareTo((Long) val2);
		} else if (Float.class.isInstance(val1)) {
			return ((Float) val1).compareTo((Float) val2);
		} else if (Double.class.isInstance(val1)) {
			return ((Double) val1).compareTo((Double) val2);
		} else if (Boolean.class.isInstance(val1)) {
			return ((Boolean) val1).compareTo((Boolean) val2) * -1;				//Change for Getting True values first in Ascending..
		} else if (Date.class.isInstance(val1)) {
			return ((Date) val1).compareTo((Date) val2);
		}
		
		return val1.toString().toLowerCase().compareTo(val2.toString().toLowerCase());
	}
	
	/**
	 * Get the graphdata List .
	 * 
	 * @param modelMap
	 *            Model Map Object
	 * @param session
	 *            Session Object
	 * @param iPageNo
	 *            page number
	 * @param iSortColumn
	 *            sort type
	 * @param strSortOrder
	 *            sort order
	 * @return refreshed UDDC List response.
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping (value = "/reloadGraphDataList")
	@ResponseBody
	public ModelAndView reloadGraphDataList(ModelMap modelMap, HttpSession session,
			@RequestParam(value="pageNo", required=false, defaultValue="1") int iPageNo,
			@RequestParam(value="sortType", required=false, defaultValue="0") int iSortColumn,
			@RequestParam (value = "sortOrder", required = false, defaultValue = "0") String strSortOrder, @LoggedInUser UserInfo userInfo) {
		
		Pagination pagination = new Pagination();
		int sortOrderNumber = 0;
		if (strSortOrder.equals("0")) {
			sortOrderNumber = 0;
			
		} else {
			sortOrderNumber = 1;
			strSortOrder = IApplicationConfigurationService.SORT_DESENDING;
		}
		
		boolean isNumber = false;
		List graphLDataList = (List) graphInfo.getFormatedDataList().clone();
		Vector headerList = (Vector) graphLDataList.get(0);
		if(headerList != null && headerList.size() >= iSortColumn) {
		  String columnName = (String) headerList.get(iSortColumn);
		  int type = CubeUtil.getColumnType(columnName, graphInfo.getCubeInfo());
		  int sortColumnType = GeneralFiltersUtil.getColType(type);
		  if(sortColumnType == GeneralFiltersUtil.NUMERIC_DIMENSION_COL) {
			  isNumber = true;
		  }
		}
		graphLDataList.remove(0);
		int iTotalRecord = graphLDataList.size();
		pagination = GeneralUtil.getPaginationInfo(iPageNo, iTotalRecord,15);
		try {
			if(headerList != null && iSortColumn != (headerList.size() - 1))//do not sort until sort order is changed by user(2 JAN 2018)
				graphLDataList = sortGraphData(graphLDataList,iSortColumn,sortOrderNumber,isNumber);
		} catch(Exception ex) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_REFRESH_GRAPH_DATA_LIST", new Object[]{userInfo.getUsername(), getObjectDisplayName() }), ex);
		}
		graphLDataList = graphLDataList.subList(pagination.getStartIndex(), pagination.getEndIndex());
		
		modelMap.addAttribute("headerList", headerList);
		modelMap.addAttribute("graphLDataList", graphLDataList);
		modelMap.addAttribute("totalPage",  pagination.getTotalPage());
		modelMap.addAttribute("sortOption", iSortColumn +":"+sortOrderNumber);
		modelMap.addAttribute("sortType", iSortColumn);
		modelMap.addAttribute("pageNo", iPageNo);
		modelMap.addAttribute("startPageIndex", ((iPageNo*10) - 9));
		modelMap.addAttribute("endPageIndex", (iPageNo*10));
		if((iPageNo*10) > iTotalRecord)
			modelMap.addAttribute("endPageIndex", iTotalRecord);
		modelMap.addAttribute("iTotalRecord", iTotalRecord);
		modelMap.addAttribute("isSmarten", true);
		auditUserActionLog(ResourceManager.getString("LBL_RELOAD_GRAPH_DATA"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("/graph/graphData");
	}

	/**
	 * @param bExportCsv true or false
	 * @param response HttpServletResponse
	 * @param bExportXml true or false
	 * @param userInfo UserInfo
	 */
	@RequestMapping(value = "/exportGraphData")
	@ResponseBody
	public void exportGraphData(
			@RequestParam(value = "exportToCsv", required = false) boolean bExportCsv,
			HttpServletResponse response,
			@RequestParam(value = "exportToXml", required = false) boolean bExportXml,
			@LoggedInUser UserInfo userInfo) {

		String sFileName = "";
		try {
			String graphName = graphInfo.getGraphName();
			if(graphName == null) {
				graphName = "New Graph"; 
			}

			if (bExportCsv) {
				sFileName = CalendarUtil.getFileSuffix(graphName) +AppConstants.CSV_EXT; 
				response.setContentType("text/csv");
				response.setHeader("Content-Disposition",
						"attachment; filename=" + sFileName);
				response.setCharacterEncoding("UTF-8");
				OutputStream sout = new BufferedOutputStream(response.getOutputStream());
				if (sout != null) {
					smartenService.exportGraphDataCSV(sout,graphInfo,userInfo);
				}
				sout.close();
				response.flushBuffer();
			} else if (bExportXml) {
				sFileName = CalendarUtil.getFileSuffix(graphName)  +AppConstants.XML_EXT;
				response.setContentType("text/xml");
				response.setHeader("Content-Disposition",
						"attachment; filename=" + sFileName);
				response.setCharacterEncoding("UTF-8");
				OutputStream sout = new BufferedOutputStream(response.getOutputStream());
				if (sout != null) {
					smartenService.exportGraphDataXML(sout,graphInfo,userInfo,-1);
				}
				sout.close();
				response.flushBuffer();
			}
		}
		catch (IOException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_DISPLAY_GRAPH_DATA",
					new Object[] {graphInfo.getGraphName(),userInfo.getUsername() }), e);
		}
		auditUserActionLog(ResourceManager.getString("LBL_EXPORT_GRAPH_DATA_CSV_XML"), AppConstants.DETAIL,userInfo);
	}
	
	/**
	 * show Group dialog.
	 * @param map ModelMap
	 * @param UserInfo userinfo
	 * @return ModelAndView
	 */
	@RequestMapping(value="/showobjectinfo")
	@ResponseBody
	public ModelAndView showObjectInfoDialog(ModelMap modelMap,
			@LoggedInUser UserInfo userinfo){
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Object information","open object information",Thread.currentThread(),userinfo,null);
		modelMap = (ModelMap) smartenService.showObjectInformation(graphInfo, userinfo,modelMap, getCubeInfo(null));
		auditUserActionLog(ResourceManager.getString("LBL_SHOW_GRAPH_OBJECT_INFORMATION_DIALOG"), AppConstants.DETAIL,userinfo);
		return new ModelAndView("objectinfograph");
	}
	
	@ResponseBody
	@RequestMapping(value = "/addNote")
	public ModelAndView showAddNote(ModelMap map,@LoggedInUser UserInfo userinfo) {
		return new ModelAndView("addGraphnote");
	}

	/**
	 * Returns the Cube User Column Expression Executor.
	 * 
	 * @return CubeColumnExpExecutor Object
	 * @throws CubeException
	 * @throws ALSException
	 */
	@Override
	public CubeColumnExpExecutor getColumnExpressionExecutor() throws CubeException, ALSException {
		return smartenService.getColumnExpressionExecutor();
	}

	/**
	 * Returns the Cube User Label Expression Executor.
	 * 
	 * @return CubeLabelExpExecutor Object
	 * @throws CubeException
	 * @throws ALSException
	 */
	@Override
	public CubeLabelExpExecutor getLabelExpressionExecutor() throws CubeException, ALSException {
		return smartenService.getLabelExpressionExecutor();
	}

	/**
	 * Get Cube Name
	 * 
	 * @return cube name.
	 */
	@Override
	public IDataObject getCubeInfo(String objectId) {
		return graphInfo.getCubeInfo();
	}

	/**
	 * Object Id like AnalysisId, GraphId...
	 * 
	 * @return String object id
	 */
	@Override
	public String getObjectId() {
		return graphInfo.getGraphId();
	}

	/**
	 * Set Active Templates info cube Through Appropriate Service.
	 * 
	 * @param activeTemplateIds
	 *            active templates array
	 * @param iType
	 *            template type like uddc-5, udhc-6
	 * @throws ALSException
	 * @throws CubeException 
	 * @throws RScriptException
	 */
	@Override
	public void setActiveTemplates(Object[] activeTemplateIds, int iType,UserInfo userInfo, boolean isRefresh) throws MDXException, RealTimeCubeException, CubeException, RScriptException {
		if (iType == TemplateUtil.ANALYSIS_OUTLINER) {
			smartenService.setActiveTemplates(iType, activeTemplateIds, graphInfo, userInfo.getUserId(), false);
			Object object1 = activeTemplateIds[0];
			if(object1 != null && object1 instanceof CubeVector) {
				graphInfo.setOutlinerFilter((CubeVector) object1);
				smartenService.checkAndResetDrillUp(graphInfo, false);
			}else {
				graphInfo.setOutlinerFilter(new CubeVector());
				smartenService.checkAndResetDrillUp(graphInfo, false);
			}
		} else if (iType == TemplateUtil.ANA_USER_UDDC) {
			List<ActiveUDDCInfo> activeUDDCList = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
			activeTemplateIds = new Object[activeUDDCList.size()];
			for (int cnt = 0; cnt < activeUDDCList.size(); cnt++) {
				ActiveUDDCInfo activeuddcInfo = activeUDDCList.get(cnt);
				activeTemplateIds[cnt] = activeuddcInfo.getUddcTemplateInfo();
			}
			smartenService.setActiveTemplates(iType, activeTemplateIds, graphInfo, userInfo.getUserId(), false);
		} else if (iType == TemplateUtil.ANALYSIS_MAINFILTER || iType == TemplateUtil.ANALYSIS_FILTER) {
			smartenService.setActiveTemplates(iType, activeTemplateIds, graphInfo, userInfo.getUserId(), false);
		}
		
		auditUserActionLog(ResourceManager.getString("LBL_SET_GRAPH_ACTIVE_TEMPLATE"), AppConstants.DETAIL,userInfo);
	}

	//Not Require in graph
	/**
	 * Set UDDC Item Properties.
	 * 
	 * @param uddcTemplateInfo
	 *            uddc template info
	 * @throws ALSException
	 * @throws CubeException
	 */
	@Override
	public void setUddcItemProperties(UddcTemplateInfo uddcTemplateInfo) throws ALSException, CubeException {}

	//Not Require in graph
	/**
	 * Get Selected Column Name from the Right Click
	 * 
	 * @param cellRefId
	 *            cell refrence id
	 * @return string column name
	 */
	@Override
	public String getColumnNameFmCellReference(String cellRefId) { return ""; }

	/**
	 * Get Col Lable Name List
	 * 
	 * @return list of column label(s)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CubeVector getColLabelNameVector(String objectId) {
		
		CubeVector colVevtor = new CubeVector();
		Vector outLinerData = smartenService.getOutLinerDataWithoutSysGeneratedFields(false, graphInfo);
		Vector<String> colDataLabels = (Vector<String>) outLinerData.get(1);
		if (colDataLabels != null) {
			colVevtor.addAll(colDataLabels);
		}
		
		return colVevtor;
	}

	/**
	 * Get Row Lable Name List
	 * 
	 * @return list of row label(s)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CubeVector getRowLabelNameVector(String objectId) {
		
		CubeVector rowVector = new CubeVector();
		Vector outLinerData = smartenService.getOutLinerDataWithoutSysGeneratedFields(false, graphInfo);
		Vector<String> rowDataLabels = (Vector<String>) outLinerData.get(0);
		if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH) {
		if(outLinerData.size() > 4) {
			if(rowDataLabels == null)
				rowDataLabels = (Vector<String>) outLinerData.get(3);
			else
			rowDataLabels.addAll((Vector<String>) outLinerData.get(3));
		}
		}
		if (rowDataLabels != null) {
			rowVector.addAll(rowDataLabels);
		}
		return rowVector;
	}

	/**
	 * Get Data Name List
	 * 
	 * @return list of data name(s)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CubeVector getDataNameVector(UserInfo userInfo) {
		
		CubeVector dataVector = new CubeVector();
		dataVector.addAll(graphInfo.getDataColumns());
		if (graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH) {
			dataVector.addAll(graphInfo.getLineGraphDataLabelsForCombinedGraph());
		}
		List<ActiveUDDCInfo> activeUDDCList = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
		for (int cnt = 0; cnt < activeUDDCList.size(); cnt++) {
			ActiveUDDCInfo activeuddcInfo = activeUDDCList.get(cnt);
			dataVector.add(activeuddcInfo.getUddcTemplateInfo().getColumnName());
		}
		return dataVector;
	}
	
	/**
	 * Get All Active Template Properties
	 * 
	 * @return ActiveTemplateProperties object
	 */
	@Override
	public ActiveTemplateProperties getActiveTemplateProperties(String objectId) {
		//below code commented for 14128[as discussed with chintan sir]
/*		//Added for bug no 11356
				if(graphInfo.getGraphData().isGraphFromDashboard())
				{
					graphInfo.getActiveTemplateProperties().setRankList(new ArrayList<CubeRankDataLabel>());
					return graphInfo.getActiveTemplateProperties();
				}
				//bug no 11356 end
*/		return graphInfo.getActiveTemplateProperties();
	}

	/**
	 * Returns type of column name provided.
	 * 
	 * @param columnName
	 *            Name of column to get type of
	 * @return Type of column name
	 * @throws ALSException
	 * @throws CubeException
	 */
	@Override
	public int getColumnTypeByColumnName(String columnName, boolean isBackEnd)
			throws ALSException, CubeException {

		return smartenService.getColumnTypeByColumnName(columnName, graphInfo, isBackEnd);
	}

	/**
	 * Object display name like Analysis Name, Graph Name...
	 * 
	 * @return String object name
	 */
	@Override
	public String getObjectDisplayName() {
		
		return graphInfo.getGraphName();
	}

	/**
	 * This method will returns information of selected Page dimensions for the
	 * object.
	 * 
	 * @return Information of Page dimensions for the object
	 * @throws CubeException
	 */
	public String[][] getPageFilters(UserInfo userInfo, String objectId) throws CubeException {
		
		return smartenService.getPageFilters(graphInfo);
	}

	public String[][] getRetrivalParameter(UserInfo userInfo,HttpServletRequest request) throws CubeException {
		return smartenService.getRetrivalParameter(graphInfo);
	}
	/**
	 * This method will returns Map of formatted values of cube conditions to be
	 * displayed.
	 * 
	 * @param columnName
	 *            Name of column
	 * @param cubeId
	 *            Cube identifier
	 * @return Map of filter conditions as formatted String array
	 * @throws CubeException
	 * @throws DatabaseOperationException 
	 */
	@Override
	public Map<Integer, String[]> generateStringFromConditionMap(
			String columnName, String cubeId,HttpServletRequest request) throws CubeException, DatabaseOperationException {
		return smartenService.generateStringFromConditionMap(columnName, cubeId);
	}

	
	@Override
	public String getObjectTypeName() {
		return objectTypeName;
	}

	/**
	 * This method will returns selected values for page filter component for
	 * the column specified.
	 * 
	 * @param columnName
	 *            Column Name
	 * @param cubeId
	 *            Cube identifier
	 * @return Selected page filter value for the column name
	 * @throws CubeException
	 * @throws DatabaseOperationException 
	 */
	@Override
	public String getPageFilterSelectedLOV(String columnName, String cubeId,HttpServletRequest request)
			throws CubeException, DatabaseOperationException {
		return smartenService.getPageFilterSelectedLOV(columnName, cubeId);
	}

	/** 
	 * This method is use in show timeSeries For get dataLabel List
	 * @return Vector<String> 
	 */	
	@SuppressWarnings("unchecked")
	@Override
	public Vector<String> getDataLabelListTimeSeries(boolean pageFilter, String pageFilterColumnName,UserInfo uInfo) {
		
		Vector<String> dataLabels =  new Vector<String>();
		
		
		if (pageFilter) {
			dataLabels.add(pageFilterColumnName);
		} else {
			if(graphInfo.getGraphType()== GraphConstants.SMARTENVIEW_TABULAR)
				dataLabels.addAll(graphInfo.getMeasureTitleList());
			else
				dataLabels.addAll(graphInfo.getDataColumns());			
			
			if (graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH) {
				dataLabels.addAll(graphInfo.getLineGraphDataLabelsForCombinedGraph());
			}
			List<ActiveUDDCInfo> activeUDDCList = graphInfo.getActiveUDDCInfo(uInfo.getUserId());
			for (int cnt = 0; cnt < activeUDDCList.size(); cnt++) {
				ActiveUDDCInfo activeuddcInfo = activeUDDCList.get(cnt);
				if(!dataLabels.contains(activeuddcInfo.getUddcTemplateInfo().getColumnName().toString()))
				dataLabels.add(activeuddcInfo.getUddcTemplateInfo().getColumnName());
			}
		}
		
		
		return dataLabels;
	}

	/**
	 * This method is use for get filterCondiotn For Timefilter
	 * 
	 * @param dataLabel
	 *            columnName
	 * @return ConditonVector
	 */
	@Override
	public CubeVector getFilterConditions(String dataLabel, String objectId) throws CubeException, ALSException{
		
		CubeVector condition = null;
		condition = smartenService.getFilterConditions(dataLabel);
		return condition;
	}
	
	/**
	 * This method is use for get TimeSeriesCondiotn For TimeSeries
	 * 
	 * @param dataLabel
	 *            columnName
	 * @return ConditonVector
	 */
	@Override
	public CubeVector getConditionVect(String dataLabel) {
		
		CubeVector condition = null;
		Map filterMap = graphInfo.getTimeConditions();
		if(filterMap == null)
	    {
			filterMap = new HashMap();
	    }   
		condition = (CubeVector) filterMap.get(dataLabel);
		return condition;
	}

	/**
	 * This method is use to get TimeSeries Map
	 * 
	 * @param conditions
	 *            Condition Vector and cube id
	 * @param strCubeId
	 *            cube Id
	 * @return Hashmap
	 * @throws CubeException 
	 */
	@Override
	public HashMap getConditionMap(CubeVector conditions, String strCubeId, String objectId) {
		
		HashMap conditionMap = null;
		try {
			conditionMap = (HashMap) smartenService.getConditionMap(conditions, strCubeId);
		} catch (CubeException e) {
			ApplicationLog.error(e);
		}
		if (conditionMap == null) {
			conditionMap = new HashMap();
		}
		return conditionMap;
	}

	/**
	 * @param obj
	 *            object
	 * @param objmode
	 *            mode
	 * @return object
	 */
	@Override
	public Object getSelectedValue(Object obj, Object objmode,HttpServletRequest request) {
		Object selected = null;
    	selected = smartenService.getSelectedValue(obj, objmode);
    	return selected;
	}

	/**
	 * for apply timeFilter
	 * 
	 * @param requestParamMap
	 * @throws CubeException 
	 */
	@Override
	public void applyTimeFilter(Map<String, String> requestParamMap, UserInfo userInfo,HttpServletRequest request) throws CubeException {
			smartenService.applyTimeFilter(requestParamMap, graphInfo);
		// Added below code for when user put time dimension in page filter and
		// apply from advanced pageFilter means (time series).
			//smartenService.setDHTMLGraph(graphInfo, userInfo.getUserId());
			smartenService.refreshPageFilterTooltip(graphInfo);
	}

	/**
	 * @param strValue
	 * @param strCubeName
	 * @return
	 */
	@Override
	public String getConditionValueType(String strValue, String strCubeName) {

		return smartenService.getConditionValueType(strValue,strCubeName);
	}

	/**
	 * @param strCol
	 *            column
	 * @return integer
	 */
	@Override
	public int getColumnType(String strCol,UserInfo userInfo) {
		int colType = 0;
    	try {
    		colType = CubeUtil.getColumnType(strCol, smartenService.getOriginalResultSetMetaData(),getGraphInfo().getCubeInfo().getDataObjectId());
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_GET_COLUMN_TYPE",
					new Object[] {userInfo.getUsername(), getObjectDisplayName()}), e);
		}
    	return colType;
	}
	
	/**
	 * The cube's meta data is returned.
	 * 
	 * @return inquiry result
	 * @exception CubeException
	 */
	@Override
	public ICubeResultSetMetaData getOriginalResultSetMetaData() throws CubeException {
		
		return smartenService.getOriginalResultSetMetaData();
	}

	/**
	 * for apply timeFilter
	 * 
	 * @param requestParamMap
	 */
	@Override
	public void applyPageFilter(Map<String, String> requestParams, String cubeId, HttpServletRequest request, HttpServletResponse response,UserInfo userInfo)
			throws DatabaseOperationException, ALSException, MDXException, RealTimeCubeException, CubeException {

		String preventPivotCall = requestParams.get("preventPivotCall");
		boolean preventCallPivotTableFlag = (preventPivotCall != null && preventPivotCall.equals("true"));

		if (preventCallPivotTableFlag) {
			boolean refreshReq = graphInfo.isRefreshReq();
			graphInfo.setRefreshReq(false);

			smartenService.applyPageFilter(requestParams, cubeId, graphInfo, userInfo);

			graphInfo.setRefreshReq(refreshReq);

			requestParams.remove("preventPivotCall");
		} else {
			smartenService.applyPageFilter(requestParams, cubeId, graphInfo, userInfo);
		}
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_SMARTEN_PAGE_FILTER"), AppConstants.DETAIL,userInfo);
	}

	/**
	 * Get Row Column Display Name(s) Map
	 * 
	 * @return display name map
	 * @throws ALSException
	 * @throws CubeException
	 */
	@Override
	public Map<String, String> getRowColumnDisplayNameMap() throws CubeException {
		
		return smartenService.getRowColumnDisplayNameMap(graphInfo,graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH ? true : false);
	}

	/**
	 * Get Measure Display Name(s) Map
	 * 
	 * @return display name map
	 * @throws ALSException
	 * @throws CubeException
	 */
	@Override
	public Map<String, String> getMeasureDisplayNameMap(UserInfo userInfo) throws CubeException {

		return smartenService.getMeasureDisplayNameMap(graphInfo, graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH ? true : false, userInfo.getUserId());
	}


	/**
	 * Object Folder Id 
	 * @return String object id
	 */
	@Override
	public String getFolderId(HttpServletRequest request) {
		
		return graphInfo.getFolderInfo().getFolderId();
	}
	
	@Override
	public List<SelectItem> getLOVList(String strDimensionName,
			String strSearchStr, boolean isCustomSort, boolean hideUdhc, 
			boolean showGV,boolean showAllData,String strCubeId,UserInfo userInfo,
			int lastIndexValue,boolean isFromPageFilter,boolean isBackEnd,HttpServletRequest request, String filterCondition) {
		
		try {
			List<SelectItem> values = new ArrayList<SelectItem>();
			values = smartenService.getLovData(strDimensionName, strSearchStr, graphInfo.getCubeInfo().getDataObjectId(), userInfo, graphInfo,isCustomSort,hideUdhc,showGV,showAllData, lastIndexValue, isFromPageFilter,true,isBackEnd, filterCondition);

			return values;
		} catch (Exception e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_GET_LOV_DATA",
					new Object[] {userInfo.getUsername(), getObjectDisplayName()}), e);
			return new ArrayList<SelectItem>();
		} 
	}


	/**
	 * Schedulername 
	 * 
	 * @return String Schedulername
	 */
	@Override
	public String getSchedulerName(HttpServletRequest request){
		return getObjectId();
	}
	
	@Override
	public String getSchedulerDisplayName(HttpServletRequest request){
		return getObjectDisplayName();
	}
	
	/**
	 * get ForecastOptions
	 * @return ForecastOptions
	 */
	/*public ForecastOptions getForecastOptions(){
		return graphInfo.getForecastOptions();
	}*/
	
	/**
	 * get forecasting column Map
	 * @return map
	 * @throws CubeException 
	 */
	@Override
	public Map<SelectItem, String> getColumnMapForForecasting() throws CubeException {
		
		return smartenService.getColumnMapForForecasting(graphInfo);
	}
	
	/**
	 * set ForecastOptions
	 * @param forecastOptions ForecastOptions
	 */
	/*public void setProcessForecasting(ForecastOptions forecastOptions){
			graphInfo.setForecastOptions(forecastOptions);
	}*/
	//Not Needed To Set in graph
	@Override
	public void setRetrivalParameterList(UserInfo userInfo) throws CubeException {
		smartenService.checkAndResetDrillUp(graphInfo, true);
	}

	@Override
	public ArrayList<ParamItem> getRetrivalParameters(HttpServletRequest request) {
		return getActiveTemplateProperties(null).getPreloadingParameters();
	}

	@Override
	public void setRetrivalParameters(
			ArrayList<ParamItem> retrivalParam,HttpServletRequest request) {
		getActiveTemplateProperties(null).setPreloadingParameters(retrivalParam);
		
	}
	
	//Not Needed To Set in graph
	/**
	 * Sets activated global variable list.
	 * @param activeVariablesTable
	 * @throws ALSException 
	 * @throws CubeException 
	 */
	@Override
	public void setActiveVariableMap (Hashtable activeVariablesTable,HttpServletRequest request) throws KPIException,ALSException, CubeException {
		smartenService.setActiveVariableMap(activeVariablesTable);
	}

	@Override
	public String getColumnDisplayName(String strColumnName) throws  CubeException {
		return smartenService.getAxisDisplayName(strColumnName, graphInfo);
	}

	public ModelAndView refreshObjectData(HttpServletRequest request,HttpServletResponse response, UserInfo userInfo, ModelMap map) {
		long startRefreshTime = System.currentTimeMillis();
		
		boolean isFromDash = false;
		if(graphInfo != null) {
			graphInfo.isFromDashBoardLink();
		}	
		clearNarrativeInsightsByObjId(graphInfo.getGraphId());
		map.addAttribute("isDefaultHomePageSmarten", graphInfo.isSmartenHomePage());
		graphInfo.setSmartenHomePage(false);
		
		if(graphInfo.getSmartenSampling() != null) {
			graphInfo.getSmartenSampling().setApplied(true);
		}
		
		graphInfo.setDisplayBarIndexList(new ArrayList<>());
		graphInfo.setDisplayLineIndexList(new ArrayList<>());
		
		long startOutlinetime=0;
		if(map.get("setOutlinerStart")!=null)
			 startOutlinetime = (long)(map.get("setOutlinerStart")); 
		long startTime = System.currentTimeMillis();
		Map<String, String> params = new HashMap<String, String>();
		params.put("isRefreshReq", "true");
		params.put("loggedInUserId", userInfo.getUserId());
		String jsonString = "";
		
		map.put("smartenMeasureCurrentTabName", "M"+0);
		map.put("smartenMeasureSelectedTabNames", "M"+0);
		
		String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
		map.addAttribute("strDateFormat",strDateFormat);
		map.put("isFirstTime",smartenService.isFirstTime());
		map.put("smartenLabelProperties", graphInfo.getGraphProperties().getSmartenProperties());
		map.addAttribute("isDataValueOn", graphInfo.getGraphProperties().getDataValueProperties().getDataValuePoint().isDataValuePointVisible());
		
		//marker size when msr in size(29 Nov 2017)
		
		/*SETTING DEFAULT FOR MOBILE*/
		if(map.get("isFromMobile") != null && map.get("isFromMobile").toString().equals("true")) {
			smartenService.setMobileDefaultView(graphInfo);
		}
		
		
		Vector theRowMeasureLabels = graphInfo.getRowMeasure();
		if(theRowMeasureLabels != null && theRowMeasureLabels.size() > 0)
		{
			//List rowListTemp = new ArrayList(theRowMeasureLabels);
			//graphInfo.getGraphData().setSizeList(sizeListTemp);
			//graphInfo.getGraphData().setSizeMeasure(true);
			/*graphInfo.getGraphData().setColorMeasureLabel(theRowMeasureLabels.get(0).toString());
			graphInfo.getGraphData().setRowMeasure(true);*/
			graphInfo.getGraphData().setColorMeasureLabel(theRowMeasureLabels.get(0).toString());
			//graphData.setColorMeasureLabel(theRowMeasureLabels.get(0).toString());
			graphInfo.getGraphData().setRowMeasure(true);
			//graphData.setRowMeasure(true);
			
		}
		else
		{
			graphInfo.getGraphData().setRowMeasureValueList(new ArrayList());
			//graphInfo.getGraphData().setSizeMeasure(true);
			graphInfo.getGraphData().setColorMeasureLabel(null);
		}
		GeneralConfigurationInfo generalConfigurationInfo = generalConfigurationServiceUtil.getGeneralConfigurationInfo();
		String googleMapKey=generalConfigurationInfo.getGoogleMapKey();
		
		map.put("googleKey", googleMapKey);
		
		int measureLegendCount = 0;
		if(graphInfo.getGraphData().isSizeMeasure())
			measureLegendCount++;
		if(graphInfo.getGraphData().isShapeMeasure())
			measureLegendCount++;
		if(graphInfo.getGraphData().isRowMeasure())
			measureLegendCount++;
		
		map.put("measureLegendCount", measureLegendCount);		
		map.put("isBubble", graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH ? true : false);
		
		//NLP
		if(graphInfo.getNlpInfo() != null)
		{
			map.put("sentence", graphInfo.getNlpInfo().getSentence());
			map.put("selectedDataset", graphInfo.getNlpInfo().getDataSetId());
		}
		if (detailedMonitorEndpoint.getActiveRequest(Thread.currentThread().getId()) != null && (detailedMonitorEndpoint.getActiveRequest(Thread.currentThread().getId()).getProcess() !=null && !detailedMonitorEndpoint.getActiveRequest(Thread.currentThread().getId()).getProcess().isEmpty())) {
    		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),detailedMonitorEndpoint.getActiveRequest(Thread.currentThread().getId()).getProcess().equalsIgnoreCase("refreshObject")?"Refresh SmartenView":null,"Refresh SmartenView",Thread.currentThread(),userInfo,null);
		} else {
			detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Open SmartenView","Initialize SmartenView",Thread.currentThread(),userInfo,null);
		}
		//marker size when msr in size
		
		long currentTimeStamp = System.currentTimeMillis();
		GraphProperties graphProperties = null;
		try {
			if(userInfo.isFromAPI() && graphInfo.getDashboardInfo()!=null) {
				String dbId = graphInfo.getDashboardInfo().getDashboardId();
				if(graphInfo.getDashboardInfo().getParentDashboardInfo()!=null) {
					dbId = graphInfo.getDashboardInfo().getParentDashboardInfo().getDashboardId();
				}
				GeneralUtil.setApiParameter(dbId, userInfo, map);
			}
			List<CubeRankDataLabel> cubeRankDataLabels = graphInfo.getRankList();
			//if(smartenService.getObjectMode() == AppConstants.NEW_MODE) {		Commented as discussed on 16 Jan 2018
				//if (smartenService.isFirstTime()) {

					//Below sort func commented because when we delete sort it got refilled from here.[2 may 2018]
					//smartenService.setDefaultSorting(graphInfo,userInfo);		
				//2018 merge (if uncoment this change acc to latest smarten controller)
				/*if (graphInfo.getGraphType() == GraphConstants.PIE_GRAPH && graphInfo.getRankList().size() == 0)
				{
					
					//CubeRankDataLabel cubeRankDataLabel  = getCubeRankLabel(CubeRankDataLabel.ROW_TYPE,cubeRankDataLabels);
					
					//if (cubeRankDataLabel == null) 
					//{
						smartenService.setDefaultRank(CubeRankDataLabel.ROW_TYPE, graphInfo, userInfo);
					//}			
				}*/

				//}
			//}
			CubeRankDataLabel cubeRankDataLabel  = getCubeRankLabel(CubeRankDataLabel.COL_TYPE,cubeRankDataLabels);
			cubeRankDataLabel  = getCubeRankLabel(CubeRankDataLabel.ROW_TYPE,cubeRankDataLabels);
			if(cubeRankDataLabel != null)
			{	
				if(graphInfo.getGraphProperties().getPieGraph().isClustered() && cubeRankDataLabel.getRowLimit() == com.elegantjbi.service.graph.GraphConstants.DEFAULT_RANK)
				{	
					cubeRankDataLabel.setRowLimit(com.elegantjbi.service.graph.GraphConstants.DEFAULT_NESTED_RANK);
					cubeRankDataLabel.setShowOthers(true);
				}
			}
			String preventPivotCall = (String) map.get("preventPivotCall");
			boolean preventCallPivotTableFlag = (preventPivotCall != null && preventPivotCall.equals("true"));
			map.addAttribute("isLegendVisible",graphInfo.getGraphProperties().getLegendProperties().getLegendPanelProperties().isLegendPanelVisible());
			
			if(map != null && map.containsKey("fromShowGraph") && graphInfo.isSmartenMap())//To check if is it from Save
			{
				String strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();
				List<GeographicalColumnInfo> geoColumnInfoList;
				try {
					geoColumnInfoList = smartenService.getGeographicalColumnByCubeID(strCubeId);
					graphInfo.getGraphData().setGeoColumnInfoListForSmartenview(geoColumnInfoList);
				} catch (DatabaseOperationException e) {
					ApplicationLog.error(e);
				}
				smartenService.getSmartenViewMapDetails(graphInfo, graphInfo.getDimensionTitleList().get(0).toString(), graphInfo.getDimensionTitleList().size(), graphInfo.getMeasureTitleList().size(),true);
			}
			
			if(request != null && request.getAttribute("fromSmartenRefresh") != null && request.getAttribute("fromSmartenRefresh").toString().equalsIgnoreCase("true"))
			{
				graphInfo.getGraphProperties().setSamplingSnapShotChanged(true);//To restrict call of subCube (Sampling) after Refresh
				graphInfo.getGraphProperties().setCallCreateSmartenResultSet(false);
			}
			
			boolean isSavegraph = false;
			boolean openObjectFromDashboard = false;
			if(map.get("issaveGraph") != null) {
				isSavegraph = (boolean)map.get("issaveGraph");
			}
			if(map.get("openObjectFromDashboard") != null) {
				openObjectFromDashboard = (boolean)map.get("openObjectFromDashboard");
			}
			else
			{
				graphInfo.setFromDashBoardLink(false);
			}
			if(!isSavegraph && !openObjectFromDashboard) {
				if (preventCallPivotTableFlag) {
					boolean refreshReq = graphInfo.isRefreshReq();
					graphInfo.setRefreshReq(false);
	
					smartenService.initGraphData(graphInfo, params,userInfo);
	
					graphInfo.setRefreshReq(refreshReq);
	
					map.remove("preventPivotCall");
				} else {
					
					smartenService.initGraphData(graphInfo, params,userInfo);
				}
			}
			graphInfo.setFromSetOutliner(false);
						
			if(smartenService.isGraphTypeWizard())
				smartenService.setGraphTypeWizard(false);
			
			
			graphProperties = graphInfo.getGraphProperties();
			smartenService.setFirstTime(false);
			
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_REFRESH_GRAPH",
					new Object[] {userInfo.getUsername(), getObjectDisplayName()}), e);
		} catch (ALSException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_REFRESH_GRAPH",
					new Object[] {userInfo.getUsername(), getObjectDisplayName()}), e);
		}
		ApplicationLog.debug(" Init Graph datat Completed");
		
		params = new HashMap<String, String>();

		params.put("sCmd", AppMainCommandList.NEW_GRAPH.getM_strCommandName());
		params.put("firstTime" , "true");
		params.put("currentTimeStamp", currentTimeStamp + "");
		map.put("isAdaptive", "true");
		if(request == null || request.getHeader("User-Agent") == null || !request.getHeader("User-Agent").contains("Mobile")) {
			boolean isAdaptive = graphProperties.getAdaptiveBehaviour();
			if(GeneralUtil.getNonAdaptiveFixedWidth(isAdaptive) != -1) {
				isAdaptive = false;
			} else {
				isAdaptive = true;
			}
			String strAdaptive = smartenService.getAdaptiveBehaviour();
			if(!strAdaptive.equals(""+isAdaptive)) {
				smartenService.setAdaptiveBehaviour(""+isAdaptive);				
			}
			map.put("isAdaptive", ""+isAdaptive);
		}
		generateRequiredItemsForGraph(map, params);

		if (graphInfo.getDrilldownBreadcrumbMap() != null && graphInfo.getDrilldownBreadcrumbMap().size() > 0) {
			map.put("drilldownBreadCrumb", graphInfo.getDrilldownBreadcrumbMap());

			map.put("drillUpLinkToOneLevel", graphInfo.getDrillUpLinkToOneLevel());
		}
		
		if (graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE) {
            map.put("isGaugeGraph", true);
        } else {
            map.put("isGaugeGraph", false);
        } 
		showAppliedFilter(map, userInfo, null);// Check whether filter is applied or not.
		
		map.put("currentTimeStamp", currentTimeStamp);
		map.put("graphInfo", graphInfo);
		map.put("graphType", graphInfo.getGraphType());
		map.put("graphProperties", graphProperties);
		map.put("isXYChart", graphInfo.isXyChart());
		
		String[] tempData = StringUtil.toArray((String) graphInfo.getDataColLabels3().toString());
		
		if((graphInfo.getGraphData().isSmartenColoumnsEnable() && graphInfo.getGraphData().isSmartenRowsEnable()) && graphInfo.isOutlinerFirstTime())
		{
			graphInfo.getGraphProperties().getPieGraph().setRadius(40);
		}
		
		map.put("allIconsList", smartenService.getAllIconsList());
		long start1 = System.currentTimeMillis();
		graphInfo.setHideIconsList(smartenService.getIconsList(graphInfo.getDimensionTitleList(), graphInfo.getMeasureTitleList(), graphInfo));
		ApplicationLog.info("SmartenView getIconsList   === >>> "+(System.currentTimeMillis()-start1));
		map.put("hideIconsList", graphInfo.getHideIconsList());
		
		boolean isD3PieRadar = false;
		boolean isTabular = false;
		boolean isAmchart = false;
		long mainResultSetCount = graphInfo.getOriginalResultSetSize();
	    if (graphInfo.getGraphType() != SmartenConstants.SMARTENVIEW_MAP)
			graphInfo.setSmartenMap(false);
		else {
			graphInfo.setSmartenMap(true);
		}
		if(graphInfo.isSmartenTabular())
			isTabular = true;
		else if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
				graphInfo.getGraphType() == GraphConstants.D3_TREEMAP || graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP)
		{
			isD3PieRadar = true;
		}
		else
			isAmchart = true;

		map.put("isD3PieRadar",isD3PieRadar);
		map.put("isTabular",isTabular);
		map.put("isAmchart",isAmchart);
		
		map.put("paginationCB",graphInfo.getGraphProperties().isPaginationCB());
		map.put("samplingCB",graphInfo.getGraphProperties().isSamplingCB());
		map.put("snapShotCB",graphInfo.getGraphProperties().isSnapShotSamplingCB());
		map.put("mainResultSetCount",mainResultSetCount);
		
		List<GeographicalColumnInfo> geoColumnInfoList = graphInfo.getGraphData().getGeoColumnInfoListForSmartenview();
		List<String> mapDimensionTitleList = new ArrayList<String>();
		
		String strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();
		if (null == geoColumnInfoList || (null != geoColumnInfoList && geoColumnInfoList.isEmpty())) {
			try {
				geoColumnInfoList = smartenService.getGeographicalColumnByCubeID(graphInfo.getCubeInfo().getDataObjectId());
			} catch (DatabaseOperationException e1) {
				ApplicationLog.error(e1);
			}
		}
		for(int i = 0; i < geoColumnInfoList.size(); i++)
		{
			if(strCubeId.endsWith(MashUpConstants.DATASETS_FILE_EXT))
				mapDimensionTitleList.add(geoColumnInfoList.get(i).getGeoGraphicalColumnName());
			else
				mapDimensionTitleList.add(geoColumnInfoList.get(i).getGeographicRole());
		}
		
		map.put("mapDimensionTitleList", StringUtils.join(mapDimensionTitleList, ',') );
		
		map.put("isFromCancelAction",false);
		if(graphInfo.isCancelAction())
		{
			String strErrorMsg = ResourceManager.getString("INFO_MSG_NO_DATA_FOUND");
			map.put("errorMessage",strErrorMsg);
			map.put("isFromCancelAction",true);
		}
		
        List dList = graphInfo.getDimensionTitleList();
        List mList = graphInfo.getMeasureTitleList();
        if(dList != null && dList.size() >0)
            ApplicationLog.debug("Plotting Dimesnions: "+StringUtils.join(dList, ','));
        if(mList != null && mList.size() >0)
            ApplicationLog.debug("Plotting Measures: "+StringUtils.join(mList, ','));
        ApplicationLog.debug("Plotting graphType: "+graphInfo.getGraphType());
      
		
		if(map.get("errorMessage").equals("") && !graphInfo.isCancelAction())
		{
		
		List<Map<String, Object>> dpList =  new ArrayList<Map<String,Object>>();
		String[] jsonArr = new String[2];
		
		
		map.put("isSmartenTabular",false);
		map.put("objectName", "graph");
		
		if(graphInfo.isSmartenTabular() && graphInfo.isSmartenMode())
			graphInfo.setIgnoreDimAndMeasureList(new ArrayList());
		
		if(graphInfo.getIgnoreDimAndMeasureList() != null && graphInfo.getIgnoreDimAndMeasureList().size() > 0)
			map.put("ignoreMainOutlinerList", StringUtils.join(graphInfo.getIgnoreDimAndMeasureList(), ','));
		else
			map.put("ignoreMainOutlinerList", "");
		
		
		int smartenDivCount = 1;
		boolean isPieWithRow = false;
		if(graphInfo.getGraphProperties().getSmartenProperties().isSplitHorizontal() || graphInfo.getGraphProperties().getSmartenProperties().isSplitVertical())			
			smartenDivCount=2;
		if(graphInfo.getGraphData().isSmartenRowsEnable() || graphInfo.getGraphData().isSmartenColoumnsEnable())
			smartenDivCount = 2;
		if(graphInfo.getGraphData().isSmartenRowsEnable() && graphInfo.getGraphData().isSmartenColoumnsEnable()) {
			
			if(graphInfo.getGraphData().getRowsList()!= null && graphInfo.getGraphData().getRowsList().size() < 2 && graphInfo.getGraphData().getColsList()!=null && graphInfo.getGraphData().getColsList().size() < 2)
				smartenDivCount = 1;
			else
				smartenDivCount = 4;	
			
		}
		if(graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			smartenDivCount = 4;
			if(graphInfo.getGraphData().getColList()!=null && graphInfo.getGraphData().getColList().size() > 0)
				map.put("gaugeCount",graphInfo.getGraphData().getColList().size());//useful when isPieWithRow
			if(graphInfo.getGraphData().getRowList()!=null && graphInfo.getGraphData().getRowList().size() > 0)
				map.put("gaugeCount",graphInfo.getGraphData().getRowList().size());
			
			if(graphInfo.getGraphData().isSmartenRowsEnable())
				smartenDivCount = 8;
		}
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			
			if(graphInfo.getGraphData().isSmartenColoumnsEnable() && !graphInfo.getGraphData().isSmartenRowsEnable())
			{
				smartenDivCount = 4;
				if(graphInfo.getGraphData().getRowList() != null && graphInfo.getGraphData().getRowList().size() > 0)
				{
					smartenDivCount = 4;
					isPieWithRow = true;
				}				
			}
			if(graphInfo.getGraphData().isSmartenRowsEnable() && !graphInfo.getGraphData().isSmartenColoumnsEnable())
			{
				if(graphInfo.getGraphData().getRowsList() != null && graphInfo.getGraphData().getRowsList().size() > 1)
				smartenDivCount = 2;
				else
					smartenDivCount = 1;	
				if(graphInfo.getGraphData().getRowList() != null && graphInfo.getGraphData().getRowList().size() > 0)
				{
					smartenDivCount = 8;
					isPieWithRow = true;
				}
				if(graphInfo.getDataColLabels3().size() > 1){
					smartenDivCount = 4;
				}
				if(graphInfo.getGraphData().isRowMeasure() == true)
				{
					smartenDivCount = 2;
					isPieWithRow = false;
				}
				if(graphInfo.isRowsMeasure())
				{
					smartenDivCount = 2;
					isPieWithRow = false;
				}
			}
			if(graphInfo.getGraphData().isSmartenColoumnsEnable() && graphInfo.getGraphData().isSmartenRowsEnable())
			{
				smartenDivCount = 8;
				if(graphInfo.getGraphData().getRowList() != null && graphInfo.getGraphData().getRowList().size() > 0){
					isPieWithRow = true;
					smartenDivCount = 8;
					}
			}
			if(!graphInfo.getGraphData().isSmartenColoumnsEnable() 
					&& !graphInfo.getGraphData().isSmartenRowsEnable() 
					&& graphInfo.getGraphData().getRowList() != null 
					&& graphInfo.getDataColLabels3().size() ==1
					&& graphInfo.getGraphData().getRowList().size() > 0
			  )
			{
				smartenDivCount = 4;
			isPieWithRow = true;
			}
			if(!graphInfo.getGraphData().isSmartenColoumnsEnable() 
					&& !graphInfo.getGraphData().isSmartenRowsEnable() 
					&& graphInfo.getGraphData().getRowList() != null 
					&& graphInfo.getDataColLabels3().size() > 1
					&& graphInfo.getGraphData().getRowList().size() > 0
			  )
			{
				smartenDivCount = 2;
			isPieWithRow = true;
			}
		}
		if((graphInfo.getGraphData().isSizeMeasure() || graphInfo.getGraphData().isRowMeasure() || graphInfo.isRowsMeasure() || graphInfo.isColsMeasure() || graphInfo.getGraphData().isShapeMeasure()) 
			&& graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			map.put("isPieWithMultipleMeasureCount","1");
		}else{
			map.put("isPieWithMultipleMeasureCount",graphInfo.getDataColLabels3().size());
		}
		if(graphInfo.getGraphData().isSmartenRowsEnable() && graphInfo.getGraphData().getRowsList().size() == 1 && !graphInfo.getGraphData().isSmartenColoumnsEnable())
			smartenDivCount = 1;
		if(graphInfo.getGraphData().isSmartenColoumnsEnable() && graphInfo.getGraphData().getColsList().size() == 1  && !graphInfo.getGraphData().isSmartenRowsEnable())
			smartenDivCount = 1;
		
			
		map.put("isPieWithRow",isPieWithRow);
		map.put("pieColCount",graphInfo.getGraphData().getColList().size());
		map.put("smartenDivCount",smartenDivCount);
		
		boolean showSamplingInBreadcrum = false;
		boolean showSnapShotInBreadcrum = false;
		String samplingMessage = "";
		String snapshotMessage = "";
		int method = 3;
		if(graphInfo.getGraphProperties().isSampling())
		{
			SmartenSampling sample = graphInfo.getSmartenSampling();
			showSamplingInBreadcrum = true;
			if(sample.getSamplingMode() == (short)SmartenConstants.SAMPLE_AUTO)
				method = sample.getSampalingMethodAuto();
			if(sample.getSamplingMode() == (short)SmartenConstants.SAMPLE_MANUAL)
				method = sample.getSampalingMethodManual();
				
			if(method == SmartenConstants.SAMPLING_RANDOM_SAMPLING)
				samplingMessage = "Sampling method: Random";
			if(method == SmartenConstants.SAMPLING_STRATIFIED_SAMPLING)
				samplingMessage = "Sampling method: Stratified";
		}
		if(graphInfo.getGraphProperties().isSnapShotSampling())
		{
			
			showSnapShotInBreadcrum = true;
			if(graphInfo.getDimensionValueCountMap().size() == 1)
				samplingMessage = "Sampling method: Random";
			else
				samplingMessage = "Sampling method: Stratified";
			
			snapshotMessage = "Snapshot view: Enabled";
		}
		Vector<String> dateDementionVector = null;
		try {
			dateDementionVector = cubeMetadataServiceUtil.getCubeDateColumn(graphInfo.getCubeInfo(), userInfo, false);
		} catch (Exception e1) {
			ApplicationLog.error(e1);
		}
		Map<String,Integer> dateDemention = new HashMap();
		for (String object : dateDementionVector) {
			dateDemention.put(object,CubeUtil.getColumnType(object, graphInfo.getCubeInfo()));
		}
		map.put("dateDemention", dateDemention);
		String value = StringUtils.join(graphInfo.getDateFrequencyMap().entrySet().stream().map(entry -> entry.getKey() + "||" + entry.getValue()).collect(Collectors.toList()), ',');
		map.put("dateDementionList", value);
		map.put("dateFrequencyMap",graphInfo.getDateFrequencyMap());
		map.put("showSamplingInBreadcrum",showSamplingInBreadcrum);
		map.put("showSnapShotInBreadcrum",showSnapShotInBreadcrum);
		map.put("samplingMessage",samplingMessage);
		map.put("snapshotMessage",snapshotMessage);
		//for bug 14670
		if((graphInfo.getGraphData().isSizeMeasure() || graphInfo.getGraphData().isShapeMeasure() || graphInfo.getGraphData().isRowMeasure())
				&& graphInfo.getGraphType() != GraphConstants.PIE_GRAPH)
			map.put("measureLegend", true);
		else
			map.put("measureLegend", false);
		
		if(graphInfo.isSmartenTabular())
		{
			map.put("objectName", "tabular");
			List tempDataTitleListForSmartenview = graphInfo.getGraphData().getColListForSmartenview();
			if(graphInfo.getGraphProperties().isPaginationCB() && tempDataTitleListForSmartenview.size() > 50)
			{
				List tempListClone = ((List) ((ArrayList) tempDataTitleListForSmartenview).clone());
				tempDataTitleListForSmartenview = tempListClone.subList(0, 50);
			}
			graphInfo.setDataTitleList(tempDataTitleListForSmartenview);
			List dimensionList = graphInfo.getGraphData().getDimensionListForSmartenview();
			List measureList = graphInfo.getGraphData().getMeasureListForSmartenview();
			
			List ignoredDimensionMeasureList = graphInfo.getIgnoreDimAndMeasureList();
			if(null != ignoredDimensionMeasureList && null != dimensionList && !dimensionList.isEmpty())
			{
				for(int j = 0; j < ignoredDimensionMeasureList.size(); j++) {
					if(dimensionList.contains(ignoredDimensionMeasureList.get(j).toString()))
						dimensionList.remove(ignoredDimensionMeasureList.get(j).toString());
				}
			}
			
			graphInfo.setDimensionTitleList(dimensionList);
			graphInfo.setMeasureTitleList(measureList);
			smartenService.setDisplayNameForSmartenTabular(graphInfo, (List<String>) tempDataTitleListForSmartenview.get(0),"");
			map.put("dataTitleList", tempDataTitleListForSmartenview);
			map.put("dataTitleListSize", tempDataTitleListForSmartenview.size());
			map.put("smartenTotalPage", smartenService.getTotalPages(graphInfo));
			map.put("pageNo", 1);
			map.put("isSmartenTabular",true);
			
				map.put("selectedRecommendedGraphType", SmartenConstants.SMARTENVIEW_TABULAR);
			map.put("recommendedGraphType", graphInfo.getRecommendGraphType());
			map.put("isNoDataFound",false);
			map.put("tabSizeList", graphInfo.getGraphData().getSizeListForTab());
			map.put("tabColorList", graphInfo.getGraphData().getColorListForSmartenview());
			map.put("dimensionListSize", graphInfo.getDimensionTitleList().size());
			map.put("measureListSize", graphInfo.getMeasureTitleList().size());
			map.put("tabularLegendInfo", graphInfo.getGraphData().getTabularLegendList());
			map.put("tabularSizeLegendInfo", graphInfo.getGraphData().getTabularSizeLegendList());
			map.put("graphType", SmartenConstants.SMARTENVIEW_TABULAR);
			jsonArr[0] = "0";
			jsonArr[1] = "1";
			map.put("startRowIndex", 1);
			map.put("endRowIndex", 50);
			map.put("totalRecord", (graphInfo.getGraphData().getColListForSmartenview().size() - 1));
			boolean isFromExport=false;
			if(request!=null && request.getSession()!=null){
			isFromExport = request.getSession().getAttribute("FROM_SE")!= null && ((boolean)request.getSession().getAttribute("FROM_SE"));
			}
				map.addAttribute("isFromExport",isFromExport);
			
		}
		else if(graphInfo.isSmartenMap())
		{
			map.put("objectName", "map");
			map.put("isSmartenMode",graphInfo.isSmartenMode());
			
			
			try {
				long mapJsonstartTime = System.currentTimeMillis();
				HashtableEx ddvmList = smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());
				smartenService.updateColLabelsMap(graphInfo, graphInfo.getGraphProperties().getColLabelsDisplayMap()!=null?graphInfo.getGraphProperties().getColLabelsDisplayMap():graphInfo.getGraphProperties().getColLabelsMap());
				jsonString = smartenService.generateSmartenMap(graphInfo.getGraphData().getMapdataValueList(),graphInfo.getGraphData().getDrillLinkList(),graphInfo,"",ddvmList);
				long mapJsonendTime = System.currentTimeMillis();
				ApplicationLog.debug("Map JSON Time "+(mapJsonendTime - mapJsonstartTime));
			} catch (CubeException e) {
				ApplicationLog.error(e);
			}
			graphInfo.setDimensionTitleList(graphInfo.getGraphData().getDimensionListForSmartenview());
			graphInfo.setMeasureTitleList(graphInfo.getGraphData().getMeasureListForSmartenview());
			map.put("jsonString", jsonString);
			map.put("isDrillDown", false);
			map.put("projection","mercator");
			map.put("mouseOverfontColor",graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getDataValueMouseOverFont().getFontColor());
			map.put("mouseOverfontSize", graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getDataValueMouseOverFont().getFontSize());
			map.put("areasSettings", smartenService.getAreaSettingsJSON(graphInfo));
			if(!graphInfo.getGraphData().getMapdataValueList().isEmpty())
				map.put("highlightArea",graphInfo.getGraphData().getMapHighLightArea());
			map.put("mapLegend", graphInfo.getGraphData().getMapLegend());
			map.put("zoomControl", smartenService.getZoomControlJSON(graphInfo));
			if(graphInfo.getGraphProperties().getSmartenProperties().isEnableGoogleMap())
				map.put("mapAreaType", "googleMap");
			else
				map.put("mapAreaType",graphInfo.getGraphData().getMapAreaType());			
			map.put("googleKey", googleMapKey);
			map.put("isSmartenMap",true);
			map.put("selectedRecommendedGraphType", GraphConstants.SMARTENVIEW_MAP);
			map.put("recommendedGraphType", graphInfo.getRecommendGraphType());
			map.put("isNoDataFound",true);
			
			if(graphInfo.getGraphData().isRowMeasure() || graphInfo.getGraphData().isSizeMeasure() || graphInfo.getGraphData().isShapeMeasure()
					|| graphInfo.isRowsMeasure() || graphInfo.isColsMeasure() || graphInfo.isCategoryMeasure())
				map.put("measureBucketEnable",true);
			map.put("dataTitleList", new ArrayList());
			map.put("dataTitleListSize", 1);
			if(graphInfo.getMeasureTitleList()!=null)
			map.put("measureListSize", graphInfo.getMeasureTitleList().size());
			jsonArr[0] = "0";
			jsonArr[1] = "1";
			map.put("jsonData",jsonArr[0]);
			map.put("chartSize", jsonArr[1]);
			
			map.put("rowsOutlinerList", StringUtils.join(graphInfo.getOutlinerRows(), ','));
			map.put("colsOutlinerList",  StringUtils.join(graphInfo.getOutlinerCols(), ','));
			map.put("categoryOutlinerList", StringUtils.join(graphInfo.getOutlinerCol(), ','));
			/*String value = StringUtils.join(graphInfo.getDateFrequencyMap().entrySet().stream().map(entry -> entry.getKey() + "||" + entry.getValue()).collect(Collectors.toList()), ',');
			map.put("dateDementionList", value);*/
			map.put("valueOutlinerList", StringUtils.join(graphInfo.getOutlinerDataColumns(), ','));
			map.put("colorOutlinerList", StringUtils.join(graphInfo.getOutlinerRow(), ','));			
			map.put("sizeOutlinerList",  StringUtils.join(graphInfo.getOutlinerSizesColumns(), ','));
			map.put("shapeOutlinerList", StringUtils.join(graphInfo.getOutlinerShapesColumns(), ','));			
			map.put("mainOutlinerList", StringUtils.join(graphInfo.getMainOutlinerMeasureAndDimension(), ','));
			
			Vector svDimVector = new Vector();
			svDimVector.addAll(graphInfo.getOutlinerRows());
			svDimVector.addAll(graphInfo.getOutlinerCols());
			svDimVector.addAll(graphInfo.getOutlinerCol());
			svDimVector.addAll(graphInfo.getOutlinerRow());
			svDimVector.addAll(graphInfo.getOutlinerSizesColumns());
			svDimVector.addAll(graphInfo.getOutlinerShapesColumns());
			svDimVector.addAll(graphInfo.getOutlinerDataColumns());
			graphInfo.setSvDimVector(svDimVector);
			
			map.put("fromRefreshObjectData",true);
			map.put("graphType", GraphConstants.SMARTENVIEW_MAP);
			if(graphInfo.getIgnoreDimAndMeasureList() != null && graphInfo.getIgnoreDimAndMeasureList().size() > 0)
				map.put("ignoreMainOutlinerList", StringUtils.join(graphInfo.getIgnoreDimAndMeasureList(), ','));
			boolean isFromExport=false;
			if(request!=null && request.getSession()!=null){
			isFromExport = request.getSession().getAttribute("FROM_SE")!= null && ((boolean)request.getSession().getAttribute("FROM_SE"));
			}
				map.addAttribute("isFromExport",isFromExport);
			
			if(graphInfo.getGraphMode() == AppConstants.NEW_MODE && !graphInfo.isGraphFromDashBoard())
			{
				map.put("fromSave",false);
				return new ModelAndView("smartview/smarten");
			}
			
		}
		else
		{
			map.put("dataTitleListSize", 0);
			map.put("dataTitleList", new ArrayList());
			map.put("isNoDataFound",true);
			map.put("isSmartenMap",false);
			
			boolean d3Graph = false;
				
							
			
			ApplicationLog.debug(" Generate Graph");
				try {
					jsonArr =  smartenService.generateGraph(graphInfo,"",false,userInfo, 0);
					
				} catch (Exception e) {
					ApplicationLog.error(e);
					jsonArr[0] = null;
					map.put("errorMessage", e.getMessage());
				}
				if(jsonArr.length > 0 && jsonArr[0]==null)
					jsonArr[0]="1";
				map.put("jsonData",jsonArr[0]);		
				map.put("chartSize", jsonArr[1]); 
				ApplicationLog.debug(" Generate Graph Endn");
				if(graphInfo.getGraphData().getRowList()!=null)
				map.addAttribute("legendLength",graphInfo.getGraphData().getRowList().size());
				else
					map.addAttribute("legendLength",0);
				boolean isFromExport=false;
				if(request!=null && request.getSession()!=null){
				isFromExport = request.getSession().getAttribute("FROM_SE")!= null && ((boolean)request.getSession().getAttribute("FROM_SE"));
				}
				map.addAttribute("isFromExport",isFromExport);
			
		}
		
		map.put("completeGraphData",graphInfo.getGraphData().isCompleteGraphData());
		
		if(graphInfo.getGraphData().isFromAnalysis())
		{
			map.put("fromAnalysis",true);		
			map.put("analysisGraphInfo", graphInfo);
		}
		else
		{	
			map.put("fromAnalysis",false);		
		}
						
						
		map.put("isRowsMeasure", graphInfo.isRowsMeasure());
		
		map.put("jsonData",jsonArr[0]);
		map.put("isSmartenRowsEnabled", false);
		map.put("isSmartenColsEnabled", false);
		map.put("splitHorizontal", graphInfo.getGraphProperties().getSmartenProperties().isSplitHorizontal());
		map.put("splitVertical", graphInfo.getGraphProperties().getSmartenProperties().isSplitVertical());
		map.put("sameScale", graphInfo.getGraphProperties().getSmartenProperties().isSameScale());
		map.put("hideNullOnCategoryAxis", graphInfo.getGraphProperties().getSmartenProperties().isHideNullOnCategoryAxis());
		map.put("enableLogarithmic", graphInfo.getGraphProperties().getSmartenProperties().isEnableLogarithmic());
		map.put("enableQuartile", graphInfo.getGraphProperties().getSmartenProperties().isEnableQuartileScale());
		map.put("googleMapSmarten", graphInfo.getGraphProperties().getSmartenProperties().isEnableGoogleMap());
		if(null != graphInfo.getGraphData().getValueAxisPositionList()
				&& (graphInfo.getGraphType() == GraphConstants.VBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.HBAR_GRAPH
					|| graphInfo.getGraphType() == GraphConstants.LINE_GRAPH || graphInfo.getGraphType() == GraphConstants.AREA_DEPTH_GRAPH))
			map.put("multipleYAxisEnable", !graphInfo.getGraphData().getValueAxisPositionList().isEmpty());
		else
			map.put("multipleYAxisEnable", false);
		map.put("isSmartenColsEnabled", false);
		ApplicationLog.debug(" getGraph_position");
		if(graphInfo.getGraphData().getGraph_position().size() > 0)
		{
			List graph_position = new ArrayList();
			map.put("chartSize", graphInfo.getDataColLabels3().size());
			for(int i=0;i < graphInfo.getDataColLabels3().size();i++)
			{
				graph_position.add(i);
			}
			if(graphInfo.getGraphData().isSmartenRowsEnable())
			{
				int positionSize = graphInfo.getDataColLabels3().size() * graphInfo.getGraphData().getRowsList().size(); 
				map.put("chartSize", positionSize);
				graph_position = new ArrayList();
				
				for(int i=0;i < graphInfo.getGraphData().getRowsList().size();i++)
				{
					for(int j=0;j<graphInfo.getDataColLabels3().size();j++)
					{
						graph_position.add(j);
					}
				}
			}
			if(graphInfo.getGraphData().isSmartenColoumnsEnable())
			{
				int positionSize = graphInfo.getDataColLabels3().size() * graphInfo.getGraphData().getColsList().size(); 
				map.put("chartSize", positionSize);
				graph_position = new ArrayList();
				
				map.put("noOfChartsInRow", positionSize);
				int colsWidth = graphInfo.getDataColLabels3().size() * graphInfo.getGraphData().getColsList().size();
				map.put("graph_position_both", true);
				map.put("graph_position_colswidth", colsWidth);
				for(int i=0;i < graphInfo.getGraphData().getColsList().size();i++)
				{
					for(int j=0;j<graphInfo.getDataColLabels3().size();j++)
					{
						graph_position.add(j);
					}
				}
			}
			if(graphInfo.getGraphData().isSmartenColoumnsEnable() && graphInfo.getGraphData().isSmartenRowsEnable())
			{
				int positionSize = graphInfo.getDataColLabels3().size() * graphInfo.getGraphData().getColsList().size() * graphInfo.getGraphData().getRowsList().size();
				map.put("chartSize", positionSize);
				graph_position = new ArrayList();
				
				for(int i=0;i < (graphInfo.getGraphData().getColsList().size() * graphInfo.getGraphData().getRowsList().size());i++)
				{
					for(int j=0;j<graphInfo.getDataColLabels3().size();j++)
					{
						graph_position.add(j);
					}
				}
			}
			
			graphInfo.getGraphData().setGraph_position(graph_position);
		}
		
		
		else
		{
		map.put("chartSize", jsonArr[1]);
		}
		ApplicationLog.debug(" getGraph_position End");
		map.put("noOfRows", 0);
		
		map.put("gaugeLegendInfo", graphInfo.getGaugeData());
		
		int noOfChartsInRow = 0;
		int noOfGraphsInRows = 1;
		
		int noOfRows = 0;
		int noOfCols = 0;
		if(graphInfo.getGraphType() == GraphConstants.D3_CHORD
				|| graphInfo.getGraphType() == GraphConstants.D3_TREEMAP
				|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST
				|| graphInfo.getGraphType() == GraphConstants.D3_BUBBLE
				|| graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT
				|| graphInfo.isSmartenTabular() || graphInfo.isSmartenMap()){
			map.put("objectName", "d3");
			graphInfo.getGraphData().setSmartenRowsEnable(false);
			graphInfo.getGraphData().setSmartenColoumnsEnable(false);
			boolean isFromExport=false;
			if(request!=null && request.getSession()!=null){
			isFromExport = request.getSession().getAttribute("FROM_SE")!= null && ((boolean)request.getSession().getAttribute("FROM_SE"));
			}
				map.addAttribute("isFromExport",isFromExport);
			
		}
		if(graphInfo.getGraphData().isSmartenRowsEnable())
			noOfRows = graphInfo.getGraphData().getSmartenRowList().size();
		if(graphInfo.getGraphData().isSmartenColoumnsEnable())
			noOfCols = graphInfo.getGraphData().getSmartenColList().size();
		map.put("pieLegend", false);
		if(graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			noOfChartsInRow =  graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getNoOfGauge();
			map.put("titledist", graphInfo.getGraphProperties().getGaugeTitleProperties().getDistanceFromCenter());
		}
		else if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			if(graphInfo.getGraphProperties().getPieGraph().isClustered())
			{
				noOfChartsInRow = Integer.parseInt(jsonArr[1]);
			}
			else
			{	
				noOfChartsInRow =  graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getNumberofpie();
			}
			map.put("isLegendPieVisible", false);
			int rowSize = graphInfo.getGraphData().getRowList().size();
			if(rowSize > 0)
				map.put("isLegendPieVisible", true);
			map.put("pietitle", StringUtils.join(graphInfo.getTitleData(),','));
			map.put("nestedgraph", graphInfo.getGraphProperties().getPieGraph().isClustered());
			
		}
		
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ||	graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			if(jsonArr[1] == "1")
			{
				map.put("noOfChartsInRow", jsonArr[1]);
			}
			else
			{	
				map.put("noOfChartsInRow", noOfChartsInRow);
			}	
		}
		else
		{
			map.put("nestedgraph", "false");
			map.put("noOfChartsInRow", jsonArr[1]);
		}
		if(graphInfo.getGraphData().getGraph_position().size() > 0)
		{	
			map.put("noOfChartsInRow", graphInfo.getDataColLabels3().size());
			if(graphInfo.getGraphData().isSmartenColoumnsEnable() && !graphInfo.getGraphData().isSmartenRowsEnable())
			{
				int positionSize = graphInfo.getDataColLabels3().size() * graphInfo.getGraphData().getColsList().size(); 
				map.put("chartSize", positionSize);
				map.put("noOfChartsInRow", positionSize);
				map.put("graph_position_both", true);
				map.put("graph_position_colswidth", positionSize);
			}
			if(graphInfo.getGraphData().isSmartenRowsEnable() && graphInfo.getGraphData().isSmartenColoumnsEnable())
			{
				int positionSize = graphInfo.getDataColLabels3().size() * graphInfo.getGraphData().getColsList().size() * graphInfo.getGraphData().getRowsList().size();
				map.put("noOfChartsInRow", positionSize);
				int colsWidth = graphInfo.getDataColLabels3().size() * graphInfo.getGraphData().getColsList().size();
				map.put("graph_position_both", true);
				map.put("graph_position_colswidth", colsWidth);
			}
			
			
		}
		else
		{
		map.put("noOfChartsInRow", 1);	

		if(graphInfo.getGraphData().isSmartenRowsEnable() && graphInfo.getGraphData().isSmartenColoumnsEnable())
		{
			map.put("chartSize", (graphInfo.getGraphData().getTotalRows()*graphInfo.getGraphData().getTotalCols()));
			map.put("noOfChartsInRow", (graphInfo.getGraphData().getRowsList().size()*graphInfo.getGraphData().getColsList().size()));
			noOfGraphsInRows = graphInfo.getGraphData().getTotalRows();
		}
		else 
		{
			if(graphInfo.getGraphData().isSmartenRowsEnable())
			{
				Map tempRowMap = graphInfo.getGraphData().getRowsMap();
				List rowsNameList = graphInfo.getGraphData().getSmartenRowList();//Keys
				List rowsValueList = new ArrayList();//List values
				map.put("rowsItemSize",graphInfo.getGraphData().getSmartenRowList().size());
				if(noOfRows > 1)
				{
					noOfGraphsInRows = graphInfo.getGraphData().getTotalRows();
					map.put("chartSize",noOfGraphsInRows);
					map.put("noOfChartsInRow", noOfGraphsInRows);
				}
				else
				{
					map.put("chartSize", graphInfo.getGraphData().getRowsList().size());
					map.put("noOfChartsInRow", graphInfo.getGraphData().getRowsList().size());
				}
			}
			if(graphInfo.getGraphData().isSmartenColoumnsEnable())
			{
				map.put("chartSize", graphInfo.getGraphData().getTotalCols());
				map.put("noOfChartsInRow", graphInfo.getGraphData().getColsList().size());
			}
		}
		
		
		}
		ApplicationLog.debug(" isSmartenRowsEnable Start");
		
		map.put("innerRowsSize", 0);
		map.put("secondRowsSize",0);
		if(graphInfo.getGraphData().isSmartenRowsEnable())
		{
			map.put("rowsItemSize",graphInfo.getGraphData().getSmartenRowList().size());
			if(noOfRows>1 && !graphInfo.isRowsMeasure())
			{
				map.put("rowsSize",noOfGraphsInRows);
				Map tmap = graphInfo.getGraphData().getRowsMap();
				String mapindex = graphInfo.getGraphData().getSmartenRowList().get(noOfRows-1).toString();
				List tlist = (List)tmap.get(mapindex);
				map.put("innerRowsSize", tlist.size());
				List tlist1 =(List)tmap.get(graphInfo.getGraphData().getSmartenRowList().get(noOfRows-2).toString());
				map.put("rowsList",tlist1);
				int secondRowsSize = 1;
				int thirdRowsSize = 1;
				if(noOfRows>2)
				{
					mapindex = graphInfo.getGraphData().getSmartenRowList().get(noOfRows-2).toString();
					tlist = (List)tmap.get(mapindex);
					secondRowsSize = tlist.size();
					
					String mapindex1 = graphInfo.getGraphData().getSmartenRowList().get(noOfRows-3).toString();
					List tlist3 = (List)tmap.get(mapindex1);
					 thirdRowsSize = tlist3.size();
					 
					List rowsChildList = new ArrayList();
					boolean rowsFlag = false;
					 for (int i = 0; i < tlist3.size(); i++) {
						rowsChildList.add(tlist3.get(i).toString() +"-"+ tlist.get(0).toString());
					}
					map.put("rowsChildList",rowsChildList);
				}
				if(noOfRows>3)
				{
					mapindex = graphInfo.getGraphData().getSmartenRowList().get(noOfRows-3).toString();
					tlist = (List)tmap.get(mapindex);
					thirdRowsSize = tlist.size();
				}
				map.put("rowsList",tlist1);
				map.put("secondRowsSize", secondRowsSize);
				map.put("thirdRowsSize", thirdRowsSize);
			}
			else
			{
				map.put("rowsSize",graphInfo.getGraphData().getRowsList().size());
			}
			map.put("isSmartenRowsEnabled",true);
		}
		map.put("noOfRows", noOfRows);
		if(graphInfo.getGraphData().isSmartenColoumnsEnable())
		{
			map.put("innerColsSize", 0);
			map.put("secondColsSize", 0);
			if(noOfCols>1 && !graphInfo.isColsMeasure())
			{	
				Map tmap = graphInfo.getGraphData().getColsMap();
				String mapindex = graphInfo.getGraphData().getSmartenColList().get(noOfCols-1).toString();
				List tlist = (List)tmap.get(mapindex);
				map.put("innerColsSize", tlist.size());
				
				if(noOfRows>2)
				{
					mapindex = graphInfo.getGraphData().getSmartenColList().get(noOfCols-2).toString();
					tlist = (List)tmap.get(mapindex);
					map.put("secondColsSize", tlist.size());
				}
			}
			map.put("noOfCols", noOfCols);
			if(noOfCols == 2 && !graphInfo.isColsMeasure())
			{
				List tempList1 = (List)graphInfo.getGraphData().getColsMap().get(graphInfo.getGraphData().getSmartenColList().get(0).toString());
				List tempList2 = (List)graphInfo.getGraphData().getColsMap().get(graphInfo.getGraphData().getSmartenColList().get(1).toString());
				map.put("colsSize",tempList1.size() * tempList2.size());
			}
			else	
				map.put("colsSize",graphInfo.getGraphData().getColsList().size());
			map.put("isSmartenColsEnabled",true);
		}
		if((graphInfo.getGraphData().getRowLabel() != null && graphInfo.getGraphData().getRowList().size()>0
				|| graphInfo.getGraphData().getCmbBarrowLabel() != null && graphInfo.getGraphData().getCmbBarrowList().size()>0
				|| graphInfo.getGraphData().getCmbLinerowLabel() != null && graphInfo.getGraphData().getCmbLinerowList().size()>0
				&& graphInfo.getGraphType() != GraphConstants.CANDLE_STICK_GRAPH && graphInfo.getGraphType() != GraphConstants.HIGH_LOW_OPEN_CLOSE_GRAPH)
				|| graphInfo.getGraphType() == SmartenConstants.HEAT_MAP_GRAPH)
		{
			map.put("isSmartenLegendVisible",true);
		}
		else
		{
			map.put("isSmartenLegendVisible",false);
		}
		
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ||
		   graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			int rowsListSize = 0;
			if(null != graphInfo.getGraphData().getRowsList())//Added to handle when come from graph type change
				rowsListSize = graphInfo.getGraphData().getRowsList().size();
			
			int rowListSize = graphInfo.getGraphData().getRowList().size();
			
			int totalRows = graphInfo.getGraphData().getTotalRows();
			int totalCols = graphInfo.getGraphData().getTotalCols();
			
			if(totalRows == 0)
				totalRows =1;
			if(totalCols == 0)
				totalCols =1;
			if(rowsListSize == 0 || graphInfo.getGraphData().getRowList().size() == 0)//when no legend in PIE
			{
				rowsListSize = 1;
			}
			if(rowListSize>0)
			{
				
				map.put("chartSize", graphInfo.getGraphData().getColList().size()* totalRows);
				map.put("noOfChartsInRow", graphInfo.getGraphData().getColList().size());
				if(graphInfo.getGraphData().isSmartenColoumnsEnable())
					map.put("chartSize", graphInfo.getGraphData().getColList().size()* totalCols);
			}
			if(graphInfo.getGraphData().isSmartenColoumnsEnable() && !graphInfo.getGraphData().isSmartenRowsEnable())
			{
				if(rowListSize>0)
				{
					int chartSize = 1;
					if(map.get("chartSize").toString() != null)
						chartSize = Integer.parseInt(map.get("chartSize").toString());
					map.put("noOfChartsInRow", chartSize);
					map.put("pieLegend", true);
				}
			}
			if(graphInfo.getGraphData().isSmartenRowsEnable() && graphInfo.getGraphData().isSmartenColoumnsEnable())
			{
				map.put("chartSize", totalCols* totalRows);
				map.put("noOfChartsInRow", totalCols);
				if(rowListSize>0)
				{
					map.put("noOfChartsInRow", totalRows*graphInfo.getGraphData().getColList().size());
					map.put("chartSize", totalCols* totalRows*graphInfo.getGraphData().getColList().size());
					map.put("pieLegend", true);
				}
			}
			if(rowListSize>0)
				map.put("isSmartenLegendVisible",true);
			
			if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH && (graphInfo.getGraphData().isSmartenColoumnsEnable() || graphInfo.getGraphData().isSmartenRowsEnable() || graphInfo.getGraphData().isRowMeasure()))
			{
				
				map.put("isSmartenLegendVisible",true);
				map.put("smartenLegendValueCount", graphInfo.getGraphData().getColList().size());
			}
			
			if(graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
			{
				map.put("noOfChartsInRow", graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getNoOfGauge());
				
				if(!graphInfo.getGraphData().isSmartenRowsEnable() && (graphInfo.getGraphData().isSmartenColoumnsEnable() && rowListSize == 0))
					map.put("chartSize", graphInfo.getGraphData().getColList().size()* totalCols);
				
				if(graphInfo.getGraphData().isSmartenRowsEnable() && graphInfo.getGraphData().isSmartenColoumnsEnable())
				{
					if(rowListSize > 0)
					{
						map.put("chartSize", totalRows*rowListSize* totalCols);
					}
					else
						map.put("chartSize", totalRows*graphInfo.getGraphData().getColList().size()* totalCols);
				}
					
				
				if(graphInfo.getGraphData().isSmartenRowsEnable() && !graphInfo.getGraphData().isSmartenColoumnsEnable())
					map.put("chartSize", totalRows*graphInfo.getGraphData().getColList().size());
				
				if(!(graphInfo.getGraphData().isSmartenColoumnsEnable()) && (rowListSize > 0))
					map.put("chartSize", rowListSize);
				
				if((graphInfo.getGraphData().isSmartenRowsEnable() && !graphInfo.getGraphData().isSmartenColoumnsEnable()) && rowListSize > 0)
					map.put("chartSize", rowListSize * totalRows);
					
				if(!graphInfo.getGraphData().isSmartenRowsEnable() && (graphInfo.getGraphData().isSmartenColoumnsEnable() && rowListSize > 0))
					map.put("chartSize", rowListSize* totalCols);
			}
			map.put("categorySize", graphInfo.getGraphData().getColList().size());
			map.put("noOfMeasure",graphInfo.getDataColLabels3().size());
		}
		map.put("totColsSize", graphInfo.getGraphData().getTotalCols());
		map.put("chartsToDisplay", graphInfo.getGraphData().getTotalCols() * graphInfo.getGraphData().getColList().size());
		String chartLoadedArr = "";
		map.put("chartLoadedArr", chartLoadedArr);
		
		
		if(graphInfo.getGraphProperties().getSmartenProperties().isSplitVertical())
			map.put("rowsSize",graphInfo.getDataColLabels3().size());
	
		ApplicationLog.debug(" isSmartenRowsEnable End");
		if(isFromSmarten())
			map.put("isFromSmarten", true);
		
		auditUserActionLog(ResourceManager.getString("LBL_REFRESH_GRAPH_OBJECT_DATA"), AppConstants.DETAIL, userInfo);
		long endTime = System.currentTimeMillis();
		}else{
			
			map.put("jsonData","1");
		}
		
		boolean fromShowGraph = false;
		if(map != null && map.containsKey("fromShowGraph"))
			fromShowGraph = true;
		map.put("includeSmartenJSP",false);

		
		
				if(graphInfo.isSmartenTabular() && graphInfo.isSmartenMode())
				{
					if(graphInfo.getGraphData().getDimensionListForSmartenview() != null && graphInfo.getGraphData().getDimensionListForSmartenview().size() > 0)
						graphInfo.setOutlinerCol(new Vector(graphInfo.getGraphData().getDimensionListForSmartenview()));
					if(graphInfo.getGraphData().getMeasureListForSmartenview() != null && graphInfo.getGraphData().getMeasureListForSmartenview().size() > 0)
						graphInfo.setOutlinerDataColumns(new Vector(graphInfo.getGraphData().getMeasureListForSmartenview()));
					if(!graphInfo.getGraphData().isRowMeasure())
						graphInfo.setOutlinerRow(new Vector());
					if(!graphInfo.isPerformAggregation())
					{
						Vector colVec = new Vector();
						Vector measureVec = new Vector();
						for(int i=0;i<graphInfo.getGraphData().getMeasureListForSmartenview().size();i++)
						{
							if(i == 0)
								colVec.add(graphInfo.getGraphData().getMeasureListForSmartenview().get(i));
							else
								measureVec.add(graphInfo.getGraphData().getMeasureListForSmartenview().get(i));
						}
						graphInfo.setOutlinerDataColumns(measureVec);
						graphInfo.setOutlinerCol(colVec);
					}
				}

		Vector svDimVector = new Vector();
		if(graphInfo.getGraphData().isSmartenRowsEnable())
		{
			map.put("rowsOutlinerList", StringUtils.join(graphInfo.getOutlinerRows(), ','));
			svDimVector.addAll(graphInfo.getOutlinerRows());
		}
		if(graphInfo.getGraphData().isSmartenColoumnsEnable())
		{
			map.put("colsOutlinerList",  StringUtils.join(graphInfo.getOutlinerCols(), ','));
			svDimVector.addAll(graphInfo.getOutlinerCols());
		}
		
		map.put("rowsOutlinerList", StringUtils.join(graphInfo.getOutlinerRows(), ','));
		svDimVector.addAll(graphInfo.getOutlinerRows());
		map.put("colsOutlinerList",  StringUtils.join(graphInfo.getOutlinerCols(), ','));
		svDimVector.addAll(graphInfo.getOutlinerCols());
		
		map.put("categoryOutlinerList", StringUtils.join(graphInfo.getOutlinerCol(), ','));
		//map.put("dateDemention",graphInfo.getDateFrequencyMap());
		Vector<String> dateDementionVector = null;
		try {
			dateDementionVector = cubeMetadataServiceUtil.getCubeDateColumn(graphInfo.getCubeInfo(), userInfo, false);
		} catch (Exception e1) {
			ApplicationLog.error(e1);
		}
		Map<String,Integer> dateDemention = new HashMap();
		for (String object : dateDementionVector) {
			dateDemention.put(object,CubeUtil.getColumnType(object, graphInfo.getCubeInfo()));
		}
		map.put("dateDemention", dateDemention);
		String value = StringUtils.join(graphInfo.getDateFrequencyMap().entrySet().stream().map(entry -> entry.getKey() + "||" + entry.getValue()).collect(Collectors.toList()), ',');
		map.put("dateDementionList", value);
		map.put("colorOutlinerList", StringUtils.join(graphInfo.getOutlinerRow(), ','));
		map.put("valueOutlinerList", StringUtils.join(graphInfo.getOutlinerDataColumns(), ','));
		map.put("sizeOutlinerList",  StringUtils.join(graphInfo.getOutlinerSizesColumns(), ','));
		map.put("shapeOutlinerList", StringUtils.join(graphInfo.getOutlinerShapesColumns(), ','));
		svDimVector.addAll(graphInfo.getOutlinerCol());
		svDimVector.addAll(graphInfo.getOutlinerRow());
		svDimVector.addAll(graphInfo.getOutlinerSizesColumns());
		svDimVector.addAll(graphInfo.getOutlinerShapesColumns());
		svDimVector.addAll(graphInfo.getOutlinerDataColumns());
		graphInfo.setSvDimVector(svDimVector);
		
		map.put("mainOutlinerList", StringUtils.join(graphInfo.getMainOutlinerMeasureAndDimension(), ','));
		map.put("fromSave",false);
		map.put("fromRefreshObjectData",true);
		map.put("d3Graph",false);
		map.put("selectedMeasureIndex",graphInfo.getSelectedMeasureIndex());
		
		
		map.put("measureBucketEnable",false);
		if(graphInfo.getGraphData().isRowMeasure() || graphInfo.getGraphData().isSizeMeasure() || graphInfo.getGraphData().isShapeMeasure()
			|| graphInfo.isRowsMeasure() || graphInfo.isColsMeasure() || graphInfo.isCategoryMeasure()
			|| graphInfo.getGraphType() == SmartenConstants.HEAT_MAP_GRAPH)
			map.put("measureBucketEnable",true);
		

		if(graphInfo.isSmartenTabular())
		{
			List<List<String>> temDataList = new ArrayList<>();
			// Deep copy each inner list
			for (Object row : graphInfo.getDataTitleList()) {
			    List<String> originalRow = (List<String>) row;
			    temDataList.add(new ArrayList<>(originalRow));  // new copy of each row
			}			
			smartenService.setDisplayNameForSmartenTabular(graphInfo, temDataList.get(0),"");
			map.put("dataTitleList", temDataList);
			map.put("isNoDataFound",false);
			if(null != graphInfo.getDataTitleList() && graphInfo.getDataTitleList().size() > 0)
				map.put("dataTitleListSize", graphInfo.getDataTitleList().size());
			
			
			map.put("tabColorList", graphInfo.getGraphData().getColorListForSmartenview());
			map.put("tabSizeList", graphInfo.getGraphData().getSizeListForTab());
			map.put("dimensionListSize", graphInfo.getDimensionTitleList().size());
			map.put("measureListSize", graphInfo.getMeasureTitleList().size());
			map.put("tabularLegendInfo", graphInfo.getGraphData().getTabularLegendList());
			map.put("tabularSizeLegendInfo", graphInfo.getGraphData().getTabularSizeLegendList());
			map.put("isSmartenTabular",true);
			map.put("startRowIndex", 1);
			map.put("endRowIndex", 50);
			if(graphInfo.getGraphData().getColListForSmartenview() != null)
				map.put("totalRecord", (graphInfo.getGraphData().getColListForSmartenview().size() - 1));
			graphInfo.setGraphType(SmartenConstants.SMARTENVIEW_TABULAR);
		}
		else
		{
			map.put("dataTitleListSize", 0);
			map.put("isNoDataFound",true);
		}
		map.put("smartenTotalPage", smartenService.getTotalPages(graphInfo));
		map.put("pageNo", 1);
		
		if(graphInfo.isSmartenMap() && "".equals(jsonString))
		{
			
			try {
				HashtableEx ddvmList = smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());
				jsonString = smartenService.generateSmartenMap(graphInfo.getGraphData().getMapdataValueList(),graphInfo.getGraphData().getDrillLinkList(),graphInfo,"",ddvmList);
			} catch (CubeException e) {
				ApplicationLog.error(e);
			}
			String areasSettingJSON = smartenService.getAreaSettingsJSON(graphInfo);
			String zoomControlJSON = smartenService.getZoomControlJSON(graphInfo);
			map.put("jsonString", jsonString);
			map.put("mouseOverfontColor",graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getDataValueMouseOverFont().getFontColor());
			map.put("mouseOverfontSize", graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getDataValueMouseOverFont().getFontSize());
			map.put("areasSettings", areasSettingJSON);
			if(!graphInfo.getGraphData().getMapdataValueList().isEmpty())
				map.put("highlightArea",graphInfo.getGraphData().getMapHighLightArea());
			map.put("zoomControl", zoomControlJSON);
			map.put("isDrillDown", false);
			map.put("projection","mercator");
			if(graphInfo.getGraphProperties().getSmartenProperties().isEnableGoogleMap())
				map.put("mapAreaType", "googleMap");
			else
				map.put("mapAreaType",graphInfo.getGraphData().getMapAreaType());
			map.put("googleKey", googleMapKey);
			map.put("isSmartenMap",true);
			map.put("selectedRecommendedGraphType", GraphConstants.SMARTENVIEW_MAP);
			map.put("recommendedGraphType", graphInfo.getRecommendGraphType());			
			map.put("isNoDataFound",true);
			map.put("dataTitleList", new ArrayList());
			map.put("dataTitleListSize", 1);
			map.put("measureListSize", graphInfo.getMeasureTitleList().size());
			map.put("fromRefreshObjectData",true);
			
				map.put("fromSave",false);
		
		}
		else
		{
			if("".equals(jsonString))
				map.put("isSmartenMap",false);
		}
		
	
		if(graphInfo.getDimensionTitleList()!=null)	
		map.put("d3dimensionsize", graphInfo.getDimensionTitleList().size());
		
		map.put("isSmartenMode",graphInfo.isSmartenMode());
		if(!graphInfo.isSmartenMode())
			map.put("graphPropertiesSubmit", true);
		
		map.put("d3Color","");
		
		if(graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH)
			map.put("isRadar", true);
		else
			map.put("isRadar", false);
		
		if(graphInfo.getGraphProperties().getSmartenProperties().isHideNullOnCategoryAxis())
			map.put("isHideonNullValueAxis", true);
		else
			map.put("isHideonNullValueAxis", false);


			String[] d3Color =new String[]{"#67b7dc","#6794dc","#6771dc","#8067dc","#a367dc","#c767dc","#dc67ce","#dc67ab","#dc6788","#dc6967",
				    "#dc8c67","#dcaf67","#dcd267","#c3dc67","#a0dc67","#7ddc67","#67dc75","#67dc98","#67dcbb","#67dadc",
				    "#80d0f5","#80adf5","#808af5","#9980f5","#bc80f5","#e080f5","#f580e7", "#f7d584", "#b1fb83", "#50407f", 
				    "#64c7cd", "#02adf2", "#828813", "#3ab54a", "#ed008c", "#8daacb", "#fc7362", "#bbd854", "#ffd92f", "#66c296",
				    "#e5b694", "#e78ad2", "#b3b3b3", "#a6d8e3", "#abe9bc", "#1b7d9c", "#ffbfc9", "#4da741", "#c4b2d6", "#b22424",
				    "#00acac", "#be6c2c", "#695496", "#349152", "#c9a16c", "#2d6396", "#fb2600", "#1596ff", "#fc9400", "#36fa92",
				    "#ec8b8b", "#93c2ff", "#f7d584", "#b1fb83", "#50407f", "#64c7cd", "#02adf2", "#828813", "#3ab54a", "#ed008c"};
			switch(graphInfo.getGraphProperties().getColorType())
			{
			case 1:
				if(graphInfo.getGraphProperties().getCustomColors() != null)
				{
					d3Color = new String[graphInfo.getGraphProperties().getCustomColors().size()];//setting the custom color length to the one given by user
					for (int i = 0; i < graphInfo.getGraphProperties().getCustomColors().size(); i++) {
						if(i > (d3Color.length-1))// || i > (bulletColor.length-1))
						{
							d3Color = smartenService.appendValue(d3Color, graphInfo.getGraphProperties().getCustomColors().get(i));
						}
						else
						{	
							d3Color[i] = graphInfo.getGraphProperties().getCustomColors().get(i);
						}
					}
				}
				break;
			case 2:
				d3Color = new String[]{graphInfo.getGraphProperties().getColor()};
				break;
			case 3:
				//Custom color palette when row,col and measure
				if(graphInfo.getGraphProperties().getRangeColorList()!=null) {
				d3Color = new String[graphInfo.getGraphProperties().getRangeColorList().size()];
				graphInfo.getGraphProperties().getRangeColorList().toArray(d3Color);}
				break;
			}
			if(graphInfo.getGraphType() == GraphConstants.D3_CHORD
					|| graphInfo.getGraphType() == GraphConstants.D3_TREEMAP
					|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST
					|| graphInfo.getGraphType() == GraphConstants.D3_BUBBLE)
			{
				if(graphInfo.getGraphProperties().getColorType()==3) 
				{
					String startColor = graphInfo.getGraphProperties().getRangeStartColor();
					String endColor = graphInfo.getGraphProperties().getRangeEndColor();
					int autoRangeDivValue = graphInfo.getGraphProperties().getRangeColorDivValue();
					int colorRange = graphInfo.getGraphProperties().getColorRange();// Auto range color
					List colorList = new ArrayList();
					if (colorRange == 0) 
					{
							colorList = smartenService.createRangeColorSmarten(startColor, endColor, autoRangeDivValue);
							graphInfo.getGraphProperties().setRangeColorList(colorList);
							d3Color = new String[graphInfo.getGraphProperties().getRangeColorList().size()];
							graphInfo.getGraphProperties().getRangeColorList().toArray(d3Color);
					}
				}		
			}
			if(graphInfo.getGraphType() == GraphConstants.D3_CHORD)
			{
				if(graphInfo.getGraphProperties().getColorType()==0)
				d3Color =new String[]{"#00A1DE", "#C4C4C4"};
				map.put("d3Color", StringUtils.join(Arrays.asList(d3Color), ","));
			}
			else
				map.put("d3Color", StringUtils.join(Arrays.asList(d3Color), ","));
		//}
		//map.put("d3Color", d3Color);
		
		//CommaSeprator D3
		String precisionLabelD3 = "";
		int adjustedDigit = graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().getAdjustedDigit();
		int digitsAfterDecimal = graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().getNumberOfDigits();
		boolean isCommaFormatEnable = graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().isCommaSeprator();
		int commaFormat = graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().getCommaFormat();
		if(graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().isShowadAdjustedSuffixed())
		{	
			int prefix = graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().getAdjustedDigit();
			switch(prefix)
			{
			case 0:
				precisionLabelD3="";
				break;
			case 3:
				precisionLabelD3="K";
				break;
			case 5:
				precisionLabelD3="L";
				break;
			case 6:
				precisionLabelD3="M";
				break;
			case 7:
				precisionLabelD3="Cr";
				break;
			case 9:
				precisionLabelD3="Bn";
				break;
			}
		}
		map.put("precisionLabelD3", precisionLabelD3);
		map.put("digitsAfterDecimalD3", digitsAfterDecimal);
		map.put("commaFormatD3", commaFormat);
		map.put("isCommaFormatEnableD3", isCommaFormatEnable);
		map.put("adjustedDigitD3", adjustedDigit);
		//CommaSeprator D3	

		//Added code for Bug 15023 start
		String chordToolTip = "";
		if ( (graphInfo.getGraphType() == SmartenConstants.D3_CHORD || graphInfo.getGraphType() == SmartenConstants.D3_TREEMAP)
			 && graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().isMouseOverTextEnable())
		{
			chordToolTip = graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getDataValueMouseOverFormatText();
			chordToolTip = chordToolTip.replaceAll("(\r\n|\n)", "<br />");
		}
		if ( (graphInfo.getGraphType() == SmartenConstants.D3_BUBBLE || graphInfo.getGraphType() == SmartenConstants.D3_TREEMAP)
				 && graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().isMouseOverTextEnable())
			{
				chordToolTip = graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getDataValueMouseOverFormatTextD3();
				chordToolTip = chordToolTip.replaceAll("(\r\n|\n)", "<br />");
			}
		//Code for Bug 15023 end
		
		map.put("dimensionListSize", graphInfo.getDimensionTitleList().size());//Added for Bug #15068 (Problem 2)
		map.put("measureListSize", graphInfo.getMeasureTitleList().size());//Added for Bug #15068 (Problem 2)
		
		map.put("smartenLegendValueCount", 0);
		if(graphInfo.getGraphData().getRowList()!=null && graphInfo.getGraphData().getRowList().size() > 0)
			map.put("smartenLegendValueCount", graphInfo.getGraphData().getRowList().size());
		
		boolean isOnLoadInfo = false;
		if(graphInfo.getOnLoadObjectInfo() != null && !graphInfo.getOnLoadObjectInfo().isEmpty()) {
			if (graphInfo.getOnLoadObjectInfo() != null
					&& (!graphInfo.getOnLoadObjectInfo().isEmpty() && !graphInfo.getOnLoadObjectInfo().get(0).isEmpty()
							|| graphInfo.getOnLoadObjectInfo().size() > 1 && graphInfo.getOnLoadObjectInfo().get(1) != null
									&& !graphInfo.getOnLoadObjectInfo().get(1).isEmpty())) {
				isOnLoadInfo = true;
			}
		}
		map.put("isOnLoadInfo", isOnLoadInfo);
		
		if(isFromDash) {
				graphInfo.setFromDashBoardLink(true);
			}
		
		ObjectMapper mapper1 = new ObjectMapper();
		String json;
		try {
			json = mapper1.writeValueAsString(graphInfo.getGraphProperties().getColLabelsDisplayMap());
			map.put("objectDisplayValueMap", json);
		} catch (IOException e) {
			ApplicationLog.error("Error while converting object display value map to json", e);
		}
		
		/*
		 * List<String> dimensionList = columnList.stream() .map(col -> { String
		 * objectVal = objectMap.get(col); // If objectMap has value AND it's different
		 * from key ? use objectMap if (objectVal != null && !objectVal.equals(col)) {
		 * return objectVal; } // Otherwise fallback to displayMap return
		 * displayMap.getOrDefault(col, col); // last fallback = original col name })
		 * .collect(Collectors.toList());
		 */
		if(fromShowGraph)// && !graphInfo.isSmartenTabular() && !graphInfo.isSmartenMap())//Added
		{
			try
			{
				IDataObject cubeInfo = graphInfo.getCubeInfo();

				strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();

				Vector<String> vector = new Vector<String>();
				if (strCubeId == null) {
					//cubeId = graphInfo.getCubeInfo().getCubeId();
					//strCubeId = graphInfo.getCubeInfo().getCubeId();
					List<ActiveGlobalVariableInfo> activeGolbalVariableList = graphInfo.getActiveTemplateProperties().getActiveGlobalVariableInfo(userInfo.getUserId());
					if(activeGolbalVariableList != null && activeGolbalVariableList.size()>0){
						for (ActiveGlobalVariableInfo activeGlobalVariableInfo : activeGolbalVariableList) {
							vector.add(activeGlobalVariableInfo.getGlobalVariableInfo().getGlobalVariableName());
						}
					}	
				}

				HashMap<String, Vector<String>> dimensionMap = metadataServiceUtil
						.getColumnsAndMeasuresMap(cubeInfo, userInfo,
								vector,true,true,false,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
				List<String> dimensionList = new ArrayList<String>();
				dimensionList.addAll(dimensionMap.get("1"));
				dimensionList.addAll(dimensionMap.get("2"));
				dimensionList.addAll(dimensionMap.get("3"));
				//modified by harsh on 4 dec
				//dimensionList.addAll(dimensionMap.get("4"));
				dimensionList.addAll(dimensionMap.get("5"));
				dimensionList.addAll(dimensionMap.get("6"));
				List<String> measureList = metadataServiceUtil.getMeasureList(cubeInfo, userInfo,true,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
				OutlinerBean outlinerBean = new OutlinerBean();
				outlinerBean.setPtreeEnable(true);
				try {
					outlinerBean = smartenService.getOutlinerData(outlinerBean, graphInfo, userInfo, dimensionList, cubeInfo, false);
				} catch (CubeException e) {
					ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
				} catch( DatabaseOperationException e){
					ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
				} catch (Exception e) {
					ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
				}
				map.put("dimensionList", dimensionList);			
				map.put("measureList", measureList);
				map.put("outlinerBean", outlinerBean);
				map.put("selectedCubeId", strCubeId);
				map.put("objectType", AppConstants.SMARTEN);
				map.put("selectedGraphType", graphInfo.getGraphType());
				map.put("selectedRecommendedGraphType", graphInfo.getGraphType());//graphInfo.getRecommendGraphType());
				map.put("recommendedGraphType", graphInfo.getRecommendGraphType());//graphInfo.getRecommendGraphType());
				map.put("smartenChartHeight",graphInfo.getGraphProperties().getSmartenChartHeight());
				map.put("fromSave",true);
				map.put("smartenModeAfterSave",graphInfo.isSmartenMode());
				map.put("isRefreshReq", true);//Added
				/*if(graphInfo.isSmartenMap())
					map.put("fromSave",false);*/
				
			}
			catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
			} catch (DatabaseOperationException ex) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), ex);
			}

			map.put("includeSmartenJSP",true);
			if(graphInfo.getGraphType()==GraphConstants.D3_TREELAYOUT && !graphInfo.isSmartenTabular())
			{
				
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String treeGraphJson=null;
		        try {
		        	treeGraphJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getD3Json());
		        } catch (JsonGenerationException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        }
		        map.put("d3Json",treeGraphJson);
		        map.put("d3Graph",true);
		        map.put("selectedRecommendedGraphType", graphInfo.getRecommendGraphType());
		        map.put("recommendedGraphType", graphInfo.getRecommendGraphType());//graphInfo.getRecommendGraphType());
		        
		        //for save D3 issue
		        map.put("dataTitleList", new ArrayList());
				map.put("dataTitleListSize", 1);
				//jsonArr[0] = "0";
				//jsonArr[1] = "1";
				
				//return new ModelAndView("smartview/d3Tree");
			}
			
			/*if(graphInfo.getGraphType()==GraphConstants.D3_TREEMAP && !graphInfo.isSmartenTabular())
			{
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String treeMapGraphJson=null;
		        try {
		        	treeMapGraphJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getTreeMapGraphDataList());
		        } catch (JsonGenerationException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        }
		        map.put("treeMapGraphJson",treeMapGraphJson);
		        map.put("d3Graph",true);
		        map.put("selectedRecommendedGraphType", graphInfo.getRecommendGraphType());
		        map.put("recommendedGraphType", graphInfo.getRecommendGraphType());//graphInfo.getRecommendGraphType());
		      //for save D3 issue
		        map.put("dataTitleList", new ArrayList());
				map.put("dataTitleListSize", 1);
				//return new ModelAndView("smartview/d3ZoomTree");
				
			}*/
			if(graphInfo.getGraphType()==GraphConstants.D3_CHORD && !graphInfo.isSmartenTabular())
			{
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String chordNameJson=null;
		        String martixJson=null;
		        try {
		        	chordNameJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getNames());
		        	martixJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getBiDemArrList());
		        } catch (JsonGenerationException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        }
		        map.put("chordNameJson",chordNameJson);
		        map.put("d3Graph",true);
		        map.put("martixJson",martixJson);
		        map.put("chordToolTip", chordToolTip);
		        map.put("respondents", graphInfo.getGraphData().getRespondents());
		      //for save D3 issue
		        map.put("dataTitleList", new ArrayList());
				map.put("dataTitleListSize", 1);
				//return new ModelAndView("smartview/d3Chord");
			}
			if((graphInfo.getGraphType()==GraphConstants.D3_TREEMAP || graphInfo.getGraphType()==GraphConstants.D3_SUNBURST || graphInfo.getGraphType()==GraphConstants.D3_BUBBLE)&& !graphInfo.isSmartenTabular())
			{
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String treeGraphJson=null;
		        try {
		        	treeGraphJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getD3Json());
		        } catch (JsonGenerationException e1) {
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            ApplicationLog.error(e1);
		        }
		        map.put("selectedRecommendedGraphType", graphInfo.getRecommendGraphType());
		        map.put("recommendedGraphType", graphInfo.getRecommendGraphType());//graphInfo.getRecommendGraphType());
		        map.put("d3Graph",true);
		        map.put("d3BubbleGraphJson",treeGraphJson);
		       // map.put("bubbledataLabel", ${graphInfo.getGraphData().getDataLabel());
		        map.put("d3ToolTip", chordToolTip);//Added code for Bug 15023 (to achieve after migrating from v3 to v4)
		      //for save D3 issue
		        map.put("dataTitleList", new ArrayList());
				map.put("dataTitleListSize", 1);
				if(graphInfo.getGraphType()==GraphConstants.D3_BUBBLE)
					map.put("d3Bubble",true);//return new ModelAndView("smartview/d3Bubble");
				else if(graphInfo.getGraphType()==GraphConstants.D3_TREEMAP)
					map.put("d3ZoomTree",true);//return new ModelAndView("smartview/d3Bubble");
				else
					map.put("d3Sunburst",true);//return new ModelAndView("smartview/d3Sunburst");
			}
			if(null != graphInfo.getGraphProperties().getSunburst()) {
				map.put("d3sunburstRadius", graphInfo.getGraphProperties().getSunburst().getRadiusInPer());	
			}
			else {
				map.put("d3sunburstRadius", 45);
			}
			
			ApplicationLog.info("Smarten View Refresh Time == >> "+(System.currentTimeMillis()-startRefreshTime));
			return new ModelAndView("smartenSave");
		}
		else
		{
			/*if(fromPaginationGraphs)
			{
				graphInfo.setFromPagination(false);
				 return null; 
			}*/
			
			if(!graphInfo.isSmartenTabular())
				map.put("selectedRecommendedGraphType", graphInfo.getGraphType());//graphInfo.getRecommendGraphType());
			map.put("selectedGraphType", graphInfo.getGraphType());
			map.put("recommendedGraphType", graphInfo.getRecommendGraphType());//graphInfo.getRecommendGraphType());
			if(graphInfo.getGraphType()==GraphConstants.D3_TREELAYOUT && !graphInfo.isSmartenTabular())
			{
				
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String treeGraphJson=null;
		        try {
		        	treeGraphJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getD3Json());
		        } catch (JsonGenerationException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        }
		        map.put("d3Json",treeGraphJson);
		        map.put("d3Graph",true);
		        long endTime = System.currentTimeMillis();
		        String timeTake = CalendarUtil.getHMSfromMillSec(endTime - startOutlinetime);
				ApplicationLog.debug("Complete Time "+(timeTake));
				
				return new ModelAndView("smartview/d3Tree");
			}
			/*if(graphInfo.getGraphType()==GraphConstants.D3_TREEMAP && !graphInfo.isSmartenTabular())
			{
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String treeMapGraphJson=null;
		        try {
		        	treeMapGraphJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getTreeMapGraphDataList());
		        } catch (JsonGenerationException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            // TODO Auto-generated catch block
		            ApplicationLog.error(e1);
		        }
		        map.put("treeMapGraphJson",treeMapGraphJson);
		        map.put("d3Graph",true);
		        long endTime = System.currentTimeMillis();
		        ApplicationLog.debug("Complete Time "+(endTime - startOutlinetime));
				return new ModelAndView("smartview/d3ZoomTree");
				
			}*/
			if(graphInfo.getGraphType()==GraphConstants.D3_CHORD && !graphInfo.isSmartenTabular())
			{
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String chordNameJson=null;
		        String martixJson=null;
		        try {
		        	chordNameJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getNames());
		        	martixJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getBiDemArrList());
		        } catch (JsonGenerationException e1) {
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            ApplicationLog.error(e1);
		        }
		        map.put("chordNameJson",chordNameJson);
		        map.put("d3Graph",true);
		        map.put("martixJson",martixJson);
		        map.put("chordToolTip", chordToolTip);
		        map.put("respondents", graphInfo.getGraphData().getRespondents());
		        long endTime = System.currentTimeMillis();
		        ApplicationLog.debug("Complete Time "+(endTime - startOutlinetime));
				return new ModelAndView("smartview/d3Chord");
			}
			if((graphInfo.getGraphType()==GraphConstants.D3_TREEMAP || graphInfo.getGraphType()==GraphConstants.D3_SUNBURST || graphInfo.getGraphType()==GraphConstants.D3_BUBBLE)&& !graphInfo.isSmartenTabular())
			{
				
				//This is for d3
				ObjectMapper objectMapper = new ObjectMapper();
		        String treeGraphJson=null;
		        try {
		        	treeGraphJson = objectMapper.writeValueAsString(graphInfo.getGraphData().getD3Json());
		        	map.put("contextFiltreMapD3",objectMapper.writeValueAsString(graphInfo.getContextFilterMapD3()));
		        } catch (JsonGenerationException e1) {
		            ApplicationLog.error(e1);
		        } catch (JsonMappingException e1) {
		            ApplicationLog.error(e1);
		        } catch (IOException e1) {
		            ApplicationLog.error(e1);
		        }
		        if(null != graphInfo.getGraphProperties().getSunburst()) {
					map.put("d3sunburstRadius", graphInfo.getGraphProperties().getSunburst().getRadiusInPer());	
				}
				else {
					map.put("d3sunburstRadius", 45);
				}
		        map.put("d3Graph",true);
		        map.put("d3BubbleGraphJson",treeGraphJson);//System.out.println(treeGraphJson);
		        map.put("d3ToolTip", chordToolTip);//Added code for Bug 15023 (to achieve after migrating from v3 to v4)
		        long endTime = System.currentTimeMillis();
		        ApplicationLog.debug("Complete Time "+(endTime - startOutlinetime));
				if(graphInfo.getGraphType()==GraphConstants.D3_BUBBLE)
					return new ModelAndView("smartview/d3Bubble");
				else if(graphInfo.getGraphType()==GraphConstants.D3_TREEMAP)
					return new ModelAndView("smartview/d3ZoomTree");
				else
					return new ModelAndView("smartview/d3Sunburst");
			}
			
			
			//map.put("measures", graphInfo.)
			/*if(treeMapGraphJson != null)
			{
				return new ModelAndView("smartview/ZoomTree");
			}
			else*/
			long endTime = System.currentTimeMillis();
			ApplicationLog.debug("Complete Time "+CalendarUtil.getHMSfromMillSec(endTime - startOutlinetime));
			ApplicationLog.info("Smarten View Refresh Time == >> "+(System.currentTimeMillis()-startRefreshTime));
				return new ModelAndView("smartview/smarten");
		}
		
	}

	private void generateRequiredItemsForGraph(ModelMap map, Map<String, String> params) {
		
		try {
			Map graphMap = smartenService.generateRequiredItemsForGraph(graphInfo,params);
			map.put("legendDrilldownLinkMapForLineGrf", graphMap.get("legendDrilldownLinkMapForLineGrf"));
			map.put("lineGraphLegendItemsList", graphMap.get("lineGraphLegendItemsList"));

			map.put("imageMapData", graphMap.get("imageMapData"));
			map.put("graphcss", graphMap.get("graphcss"));
			map.put("legendItemsList", graphMap.get("legendItemsList"));
			map.put("legendDrilldownLinkMap",graphMap.get("legendDrilldownLinkMap"));
			map.put("isCombinedGraph", graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH ? true : false);
			map.put("isPie", graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ? true : false);
			map.put("isScatter", graphInfo.getGraphType() == GraphConstants.SCATTER_LINE_GRAPH ? true : false);
			map.put("isBubble", graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH ? true : false);
			map.put("isCandle", graphInfo.getGraphType() == GraphConstants.CANDLE_STICK_GRAPH ? true : false);
			map.put("isHistogram", graphInfo.getGraphType() == GraphConstants.HISTOGRAM_GRAPH ? true : false);
			map.put("isHeatMap", graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH ? true : false);
			map.put("isHighLowOC", graphInfo.getGraphType() == GraphConstants.HIGH_LOW_OPEN_CLOSE_GRAPH ? true : false);
			map.put("isStackedBar", graphInfo.getGraphType() == GraphConstants.STACKED_VBAR_GRAPH 
					|| graphInfo.getGraphType() == GraphConstants.STACKED_HBAR_GRAPH ? true : false);
			map.put("isHeatmap", graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH? true : false);
			map.put("totalObjects", graphMap.get("totalObjects"));
			if(graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH)
				map.put("isRadar", true);
			map.put("isStackedBar", graphInfo.getGraphType() == GraphConstants.STACKED_VBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.STACKED_HBAR_GRAPH ? true : false);
			if(graphInfo.getGraphData().getRowLabel() != null && !graphInfo.getGraphData().getRowLabel().equalsIgnoreCase("legend") && graphInfo.getGraphData().getColLabel() != null && graphInfo.getDataColLabels3().size() == 1
					||(graphInfo.getGraphData().getRowLabel() != null && graphInfo.getGraphData().getRowLabel().equals("legend") && graphInfo.getDataColLabels3().size()>1))
				map.put("isClustered", true);
			/*if(graphInfo.getGraphData().getRowLabel() != null && !graphInfo.getGraphData().getRowLabel().equalsIgnoreCase("legend") && graphInfo.getGraphData().getColLabel() != null && graphInfo.getDataColLabels3().size() == 1
					||(graphInfo.getGraphData().getRowLabel() != null && graphInfo.getGraphData().getRowLabel().equals("legend") && graphInfo.getDataColLabels3().size()>1))
				map.put("isClustered", true);*/
			//map.put("gaugeLegendInfo", graphMap.get("gaugeLegendInfo"));
			map.put("isYAxisVisible", graphMap.get("isYAxisVisible"));
			map.put("isXAxisVisible", graphMap.get("isXAxisVisible"));
			map.put("isGraphVisible", graphMap.get("isGraphVisible"));
			map.put("graphXAxisTitle", graphMap.get("graphXAxisTitle"));
			map.put("graphYAxisTitle", graphMap.get("graphYAxisTitle"));
			map.put("graphYAxis2Title", graphMap.get("graphYAxis2Title"));
			map.put("graphLegendTitle", graphMap.get("graphLegendTitle"));
			map.put("graphLegend2Title", graphMap.get("graphLegend2Title"));
			map.put("errorMessage", graphMap.get("errorMessage"));
			
		} catch (Exception ex) {
			ApplicationLog.error(ex);
		}
	}

	

	//Not Needed To Set in graph
	/**
	 * Sets the Rank Label
	 * 
	 * @throws CubeException
	 * @throws ALSException
	 */
	@Override
	public void setRank() throws CubeException, ALSException {}

	@Override
	public Map<SelectItem, Integer> prepareCubeAllItemsMap(boolean isAddMeasure,UserInfo userInfo) throws CubeException {
		return smartenService.prepareDimensionItemsMap(getCubeInfo(null).getDataObjectId(), userInfo,graphInfo);
	}

	@Override
	public List<ActiveFilterInfo> getActiveFilterInfo(UserInfo userInfo) {
		String id = "";
		if(userInfo.isAdmin()) {
			id = AppConstants.ADMIN_USERNAME;
		} else {
			id = userInfo.getUserId();
		}
		return graphInfo.getActiveFilterInfo(id);
	}

	@Override
	public CubeDataExpExecutor getDataExpressionExecutor() throws CubeException {

		return smartenService.getDataExpressionExecutor();
	}

	@Override
	public PDFPageSetupInfo getPdfPageSetUpInfo(HttpServletRequest request) {
		PDFPageSetupInfo info;
		if(graphInfo != null){
			info =  graphInfo.getPdfPageSetup();
			if(info == null){
				info = new PDFPageSetupInfo();
			}
		} else {
			info = new PDFPageSetupInfo();
		}
		return info;
	}

	public SmartenInfo getGraphInfo() {
		return graphInfo;
	}

	/**
	 * This method is use to set outliner Data.
	 * @param request
	 * @return Message String
	 */
	@RequestMapping (value = "/setOutliner")
	@ResponseBody
	public Object setOutliner(HttpServletRequest request, ModelMap map, @LoggedInUser UserInfo userInfo, HttpServletResponse response) {
		long setOutlinerStart = System.currentTimeMillis();
		try {
			
			//ApplicationLog.debug("------------>Set Outliner START");
			graphInfo.setFromNlp(false);
			graphInfo.setFromSetOutliner(true);
			graphInfo.setColsMeasure(false);
			graphInfo.setRowsMeasure(false);
			graphInfo.setCancelAction(false);
			HashMap<String, String> requestParamMap = new HashMap<String, String>();
			Enumeration<String> requestEnum = request.getParameterNames();
			String graphId=graphInfo.getGraphId();
			if(graphInfo.getGraphMode() == AppConstants.NEW_MODE) {
				graphId=graphInfo.getNewGraphId()+AppConstants.SMARTEN_FILE_EXT;
			}
			detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphId,"Outliner",null,Thread.currentThread(),userInfo,new Date());
		//	Map<String, Integer> dimensionCountMap =new HashMap();
		//	dimensionCountMap = getDimensionSize(userInfo,graphInfo,request);
			graphInfo.setFromOutlinerSubmit(true);//2018 merge
			//lovlist alternative for color start
			graphInfo.setLovListForColor(new ArrayList());
			//cmb
			graphInfo.setLovListForColorBar(new ArrayList());
			graphInfo.setLovListForColorLine(new ArrayList());
			
			graphInfo.setLovListForColorUnique(new ArrayList());
			//cmb
			graphInfo.setLovListForColorBarUnique(new ArrayList());
			graphInfo.setLovListForColorLineUnique(new ArrayList());
			//lovlist alternative for color end	
			graphInfo.setSizeLabelBubble(null);//25 feb 2019
			
			graphInfo.setSmartenMap(false);
			
			int graphType=0;
			graphInfo.setGraphData(new GraphData());
			
			graphInfo.getGraphData().setSmartenColoumnsEnable(false);
			graphInfo.getGraphData().setSmartenRowsEnable(false);
			
			List crDetailedOutlinerValues = new ArrayList();//Added
			
			boolean isSmartenModeOn = false;
			String smartenMode = request.getParameter("smartenModeEnable");
			String selectedValues = request.getParameter("selectedValues");
	        Map<String, String> selectedValuesMap = new HashMap<>();
	        if (selectedValues != null && !selectedValues.isEmpty()) {
	            for (String pair : selectedValues.split(" && ")) {
	                String[] keyValue = pair.split("\\|\\|");
	                if (!keyValue[1].equals("null") && keyValue.length == 2 && Integer.parseInt(keyValue[1].trim()) != 0) {
	                    selectedValuesMap.put(keyValue[0].trim(), KPIConstants.Frequency.getCorrespondingValue(Integer.parseInt(keyValue[1].trim())).toString());
	                }
	            }
	        }
			graphInfo.setDateFrequencyMap(selectedValuesMap);
			if(smartenMode != null && smartenMode.equalsIgnoreCase("true"))
			{
				isSmartenModeOn = true;
				graphInfo.setSmartenMode(true);//Added on info
			}
			else
			{
				graphInfo.setSmartenMode(false);//Added on info
			}
			
			String sampleMode = request.getParameter("sampleMode");
			if(sampleMode != null && sampleMode.equalsIgnoreCase("true"))
			{
				graphInfo.getGraphProperties().setSamplingSnapShotChanged(true);
				//graphInfo.getGraphProperties().setCallCreateSmartenResultSet(false);
			}
			
			List originalDimensionList = new ArrayList();
			
			String userId = userInfo.getUserId();
			String srtCubeId = graphInfo.getCubeInfo().getDataObjectId();
			if(srtCubeId!=null){
			try
			{
				IDataObject cubeInfo = cubeDataServiceUtil.getCubeForCubeColumnInfoByCubeId(srtCubeId);
				originalDimensionList  = metadataServiceUtil.getDimensionColumns(cubeInfo, userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			}
			catch(Exception exc)
			{
				ApplicationLog.error(exc);
			}
			}
			graphInfo.setOriginalDimensionList(originalDimensionList);
			
			//Drill
			if(graphInfo.getDrillVector() != null && graphInfo.getDrillVector().size() > 0)
				graphInfo.setGraphTypeChangeWithDrill(true);
			smartenService.resetDrillVector(graphInfo);
			//Drill
			
			graphInfo.setColsMeasureList(new ArrayList());
			graphInfo.setRowsMeasureList(new ArrayList());
			graphInfo.setColsDimMeasureList(new ArrayList());
			graphInfo.setRowsDimMeasureList(new ArrayList());
			graphInfo.setCategoryMeasure(false);
			graphInfo.setCategoryMeasureLabel("");
			boolean isFromMainOutliner = false;
			List ignoreDimForRowsAndCols = new ArrayList();
			List rowLabel2 = new ArrayList();
			boolean onlyMeasureInOutliner = true;//when both row and col has measure[or rows,cols,col,row has no dim]
			
			//Added for Bug #14858 start
			List rowsValuesList = new ArrayList();
			List coloumnsValuesList = new ArrayList();
			List rowlabelsValuesList = new ArrayList();
			List sizesValuesList = new ArrayList();
			List shapesValuesList = new ArrayList();
			List collabelsValuesList = new ArrayList();
			List orderedDataLabelsValuesList = new ArrayList();
			List rowlabels2ValuesList = new ArrayList();
			//Added for Bug #14858 end
			
			while (requestEnum.hasMoreElements()) {
				String paramName = requestEnum.nextElement();
				String paramValue = StringUtil.null2String(request.getParameter(paramName));
				//System.out.println("paramName "+paramName+" paramValue"+ paramValue);
				if(paramName.equalsIgnoreCase("sngraphtype") && !paramValue.equalsIgnoreCase("") && !paramValue.equalsIgnoreCase("70"))
					graphType=Integer.parseInt(paramValue);//60
				if(paramName.equalsIgnoreCase("rows"))
				{
					/*List smartenRowList = new ArrayList();
					smartenRowList.add(paramValue);
					if(paramValue != null && !paramValue.equals(""))
					{
						graphInfo.getGraphData().setSmartenRowList(smartenRowList);
						graphInfo.getGraphData().setSmartenRowsEnable(true);
						graphInfo.getGraphData().setRowsLabel(paramValue);
					}*/
					if(paramValue != null && !paramValue.equals(""))
					{
						List smartenRowList = new ArrayList();
						List measure = new ArrayList();
						List rowsDimMeasure = new ArrayList();
						//graphInfo.getGraphData().setSmartenRowsEnable(true);
						String[] commaSepratedArray = paramValue.split(",");
						String rows = "";
						String rowsMeasure = "";
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							rowsDimMeasure.add(commaSepratedArray[i].toString());//This will contain both dimension and measure
							if(originalDimensionList.contains(commaSepratedArray[i].toString()))//allow dim only
							{
								rows = rows + commaSepratedArray[i].toString() + ",";
								smartenRowList.add(commaSepratedArray[i].toString());
								onlyMeasureInOutliner = false;
							}
							else
							{
								rows = rows + commaSepratedArray[i].toString() + ",";//11 sep 2018 [non aggregated data(reArrangeMeasure)]
								graphInfo.setRowsMeasure(true);
								//ignoreDimForRowsAndCols.add(commaSepratedArray[i].toString());
								measure.add(commaSepratedArray[i].toString());
							}
						}
						if(!isSmartenModeOn)
						{
							if(smartenRowList.size() > 0)//as it may contain msr
							{
								graphInfo.getGraphData().setSmartenRowsEnable(true);
								graphInfo.getGraphData().setSmartenRowList(smartenRowList);
							}
						}
						//graphInfo.getGraphData().setOutlinerRowsList(smartenRowList);
						if(graphType == 60)
						{
							rowsValuesList.addAll(smartenRowList);
						}
						else
						{	
							rowsValuesList.addAll(smartenRowList);
							Vector rowsVectorTemp = new Vector(rowsDimMeasure);//msr+dim
							graphInfo.setRowsColumns(rowsVectorTemp);
							graphInfo.setOutlinerRows(rowsVectorTemp);
						}
						paramValue = rows;
						if (paramValue.endsWith(",")) 
							paramValue = paramValue.substring(0, paramValue.length() - 1);
						
						if(graphInfo.isRowsMeasure())
							graphInfo.setRowsMeasureList(measure);
						
						graphInfo.setRowsDimMeasureList(rowsDimMeasure);
					}
					else
					{
						graphInfo.setRowsColumns(new Vector());
						graphInfo.setOutlinerRows(new Vector());
					}
				}
				if(paramName.equalsIgnoreCase("coloumns"))
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						List smartenColList = new ArrayList();
						List measure = new ArrayList();
						String cols = "";
						List colsDimMeasure = new ArrayList();
						//graphInfo.getGraphData().setSmartenColoumnsEnable(true);
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							colsDimMeasure.add(commaSepratedArray[i].toString());//This will contain both dimension and measure
							if(originalDimensionList.contains(commaSepratedArray[i].toString()))
							{
								smartenColList.add(commaSepratedArray[i].toString());
								cols = cols + commaSepratedArray[i].toString() + ",";
								onlyMeasureInOutliner = false;
							}
							else
							{
								cols = cols + commaSepratedArray[i].toString() + ",";//11 sep 2018 [non aggregated data(reArrangeMeasure)]
								graphInfo.setColsMeasure(true);
								measure.add(commaSepratedArray[i].toString());
							}
						}
						if(!isSmartenModeOn)
						{
							if(smartenColList.size() > 0)
							{
								graphInfo.getGraphData().setSmartenColoumnsEnable(true);
								graphInfo.getGraphData().setSmartenColList(smartenColList);
							}
						}
						//graphInfo.getGraphData().setOutlinerColsList(smartenColList);
						
						if(graphType == 60)
						{
							coloumnsValuesList.addAll(smartenColList);
						}
						else
						{	
							coloumnsValuesList.addAll(smartenColList);
							Vector colsVectorTemp = new Vector(colsDimMeasure);//msr+dim
							graphInfo.setColsColumns(colsVectorTemp);
							graphInfo.setOutlinerCols(colsVectorTemp);
						}
						paramValue = cols;
						if (paramValue.endsWith(",")) 
							paramValue = paramValue.substring(0, paramValue.length() - 1);
						
						if(graphInfo.isColsMeasure())
							graphInfo.setColsMeasureList(measure);
						
						graphInfo.setColsDimMeasureList(colsDimMeasure);
					}
					else
					{
						graphInfo.setColsColumns(new Vector());
						graphInfo.setOutlinerCols(new Vector());
					}
				}

				if(paramName.equalsIgnoreCase("main"))
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						List mainOutlinerList = new ArrayList();
						isFromMainOutliner = true;
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							mainOutlinerList.add(commaSepratedArray[i].toString());
						}
						graphInfo.getGraphData().setOrderedOutlinerListForSmartenview(mainOutlinerList);
					}
				}
				if(paramName.equalsIgnoreCase("rowlabels"))//row
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						List rowList = new ArrayList();
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							rowList.add(commaSepratedArray[i].toString());
							if(originalDimensionList.contains(commaSepratedArray[i].toString()))
								onlyMeasureInOutliner = false;
						}
						if(graphType == 60)
						{
							rowlabelsValuesList.addAll(rowList);
							crDetailedOutlinerValues.addAll(rowList);
						}
						else
						{
							graphInfo.setOutlinerRow(new Vector(rowList));
							rowlabelsValuesList.addAll(rowList);
						}
					}
					else
					{
						graphInfo.setOutlinerRow(new Vector());
					}
				}
				if(paramName.equalsIgnoreCase("sizes"))
				{
					if(paramValue != null && !paramValue.equals("") && graphType!=63)
					{

						List sizesList = new ArrayList();
						//graphInfo.getGraphData().setSizeMeasure(true); 
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							sizesList.add(commaSepratedArray[i].toString());
						}
						if(!isSmartenModeOn)
						{
							graphInfo.getGraphData().setSizeList(sizesList);
							sizesValuesList.addAll(sizesList);
						}
						
						if(graphType == 60)
						{
							sizesValuesList.addAll(sizesList);
							crDetailedOutlinerValues.addAll(sizesList);
						}
						else
						{	
							Vector sizeVectorTemp = new Vector(sizesList);
							if(isSmartenModeOn)//ignore size when smarten mode disable
							{
								sizesValuesList.addAll(sizesList);
								graphInfo.setSizeColumns(sizeVectorTemp);
							}else{
								if(graphInfo != null && (graphInfo.getGraphType() == 70 || graphInfo.getGraphType() == 71 || StringUtil.null2String(request.getParameter("sngraphtype")).equals("70") 
										|| StringUtil.null2String(request.getParameter("sngraphtype")).equals("71")))
								{
									sizesValuesList.addAll(sizesList);
									graphInfo.setSizeColumns(sizeVectorTemp);
								}
							}
							graphInfo.setOutlinerSizesColumns(sizeVectorTemp);
						}
						
					}
					else
					{
						graphInfo.setSizeColumns(new Vector());
						graphInfo.setOutlinerSizesColumns(new Vector());
					}
				}
				if(paramName.equalsIgnoreCase("shapes"))//shape
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						List shapeList = new ArrayList();
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							shapeList.add(commaSepratedArray[i].toString());
						}
						if(!isSmartenModeOn)
						{
							graphInfo.getGraphData().setShapeList(shapeList);
							shapesValuesList.addAll(shapeList);
						}
						//graphInfo.getGraphData().setOutlinerShapeList(shapeList);
						
						if(graphType == 60)
						{
							shapesValuesList.addAll(shapeList);
							crDetailedOutlinerValues.addAll(shapeList);
						}
						else
						{	
							
							Vector shapeVectorTemp = new Vector(shapeList);
							if(isSmartenModeOn)//ignore shape when smarten mode disable
							{
								shapesValuesList.addAll(shapeList);
								graphInfo.setShapeColumns(shapeVectorTemp);
							}
							{
								if(graphInfo != null && (graphInfo.getGraphType() == 70 || graphInfo.getGraphType() == 71 || StringUtil.null2String(request.getParameter("sngraphtype")).equals("70")
										|| StringUtil.null2String(request.getParameter("sngraphtype")).equals("71")))//Added Map code for save Point 9 Task #13184
								{
									shapesValuesList.addAll(shapeList);
									graphInfo.setSizeColumns(shapeVectorTemp);
								}
							}
							graphInfo.setOutlinerShapesColumns(shapeVectorTemp);
						}
					}
					else
					{
						graphInfo.setShapeColumns(new Vector());
						graphInfo.setOutlinerShapesColumns(new Vector());
					}
				}
				if(paramName.equalsIgnoreCase("collabels"))//col
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						List colList = new ArrayList();
						List allColLabel = new ArrayList();
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							allColLabel.add(commaSepratedArray[i].toString());
							if(originalDimensionList.contains(commaSepratedArray[i].toString()))
							{
								colList.add(commaSepratedArray[i].toString());
								onlyMeasureInOutliner = false;
							}
							else
							{
								if(i == 0)
								{
									if(!graphInfo.isSmartenMode())
									{
										graphInfo.setCategoryMeasure(true);
										graphInfo.setCategoryMeasureLabel(commaSepratedArray[i].toString());
									}
								}
							}
						}
						
						if(graphType == 60)
						{
							collabelsValuesList.addAll(allColLabel);
							crDetailedOutlinerValues.addAll(allColLabel);
						}
						else
						{
							graphInfo.setOutlinerCol(new Vector(allColLabel));
							collabelsValuesList.addAll(allColLabel);
						}
					}
					else
					{
						graphInfo.setOutlinerCol(new Vector());
					}
				}
				if(paramName.equalsIgnoreCase("orderedDataLabels"))//data
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						List dataList = new ArrayList();
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							dataList.add(commaSepratedArray[i].toString());
						}
						if(graphType == 60)
						{
							orderedDataLabelsValuesList.addAll(dataList);
						}
						else
						{
							graphInfo.setOutlinerDataColumns(new Vector(dataList));
							orderedDataLabelsValuesList.addAll(dataList);
						}
						graphInfo.setOutlinerMeasureColumns(new Vector(dataList));
					}
					else
					{
						graphInfo.setOutlinerMeasureColumns(new Vector());
						graphInfo.setOutlinerDataColumns(new Vector());
					}
				}
				if(paramName.equalsIgnoreCase("rowlabels2"))//data
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						//List rowLabel2 = new ArrayList();
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							rowLabel2.add(commaSepratedArray[i].toString());
						}
						rowlabels2ValuesList.addAll(rowLabel2);
					}
				}

				/**
				 * Added to plot Graphs when not AUTO and 2 Measures
				 */
				if(graphType != 60
						&& (paramName.equalsIgnoreCase("datalabels2"))
						&& graphType != GraphConstants.COMBINED_GRAPH)
				{
					
					String[] useraddeddatalabels = StringUtil.toArray((String)request.getParameter("useraddeddatalabels"));
					if(useraddeddatalabels.length==0)
					{					
					paramValue = StringUtil.null2String(request.getParameter("datalabels"));
					String paramValue2=StringUtil.null2String(request.getParameter(paramName));//datalabels2;
					if(!paramValue2.equals(""))						
					paramValue += ","+StringUtil.null2String(request.getParameter(paramName));//datalabels2
					paramName = "datalabels";
					}
				}

				requestParamMap.put(paramName, paramValue);
			}
			
			//16 spet 2019
			//System.out.println("Ignore from front end is "+requestParamMap.get("ignoreString"));
			
			//Moved here for Bug #14858 start
			List allDetailedOutlinerValues = new ArrayList();
			allDetailedOutlinerValues.addAll(rowsValuesList);//Rows
			allDetailedOutlinerValues.addAll(coloumnsValuesList);//Columns
			allDetailedOutlinerValues.addAll(collabelsValuesList);//Category
			allDetailedOutlinerValues.addAll(orderedDataLabelsValuesList);//Data
			allDetailedOutlinerValues.addAll(rowlabelsValuesList);//Color
			allDetailedOutlinerValues.addAll(sizesValuesList);//Size
			allDetailedOutlinerValues.addAll(shapesValuesList);//Shape
			allDetailedOutlinerValues.addAll(rowlabels2ValuesList);
			//Bug #14858 end
			
			List allDetailedOutlinerValuesTemp= new ArrayList<String>();
			allDetailedOutlinerValuesTemp = new ArrayList<String>(new LinkedHashSet<String>(allDetailedOutlinerValues));
			
			
			allDetailedOutlinerValues=allDetailedOutlinerValuesTemp;
			
			
			//ignore 2nd coloum[d/m] in row when vbar/line etc....and mode off
			if(graphType != 60 && !isSmartenModeOn && rowLabel2.size() > 0 && graphInfo.getOutlinerRow().size() > 0)
			{
				Vector tempVect = new Vector();
				tempVect.addAll(graphInfo.getOutlinerRow());
				tempVect.addAll(rowLabel2);
				graphInfo.setOutlinerRow(tempVect);
			}

			
			//It removes rows when mode is ON(Scenario:when pie to other chart for 2 or more Dim,Reason:Bcz pie chart keep 1D in rows instead of col)
			if(isSmartenModeOn)
			{	
				requestParamMap.put("rows", "");
				requestParamMap.put("rowlabels2", "");//bcz d3 chart keeps all dim in color[which changes ignore dim list after save].
				requestParamMap.put("sizes", "");
				requestParamMap.put("shapes", "");
				requestParamMap.put("rowlabels", "");
				requestParamMap.put("coloumns","");
                requestParamMap.put("collabels",""); 
			}
			
			/**
			 * This code is to enable sampling CB(Eg:change vBar(snapShot=OFF) to d3,enable snapShotView if clicks ok on dialouge box)
			 */
			//------------------sampling checkBox start-------------------------------------------------//
			if(requestParamMap.get("outlinerPaginationCB") != null && requestParamMap.get("outlinerPaginationCB").equalsIgnoreCase("true"))
				graphInfo.getGraphProperties().setPaginationCB(true);
			else
				graphInfo.getGraphProperties().setPaginationCB(false);
			
			if(requestParamMap.get("outlinerSamplingCB") != null && requestParamMap.get("outlinerSamplingCB").equalsIgnoreCase("true"))
				graphInfo.getGraphProperties().setSamplingCB(true);
			else
				graphInfo.getGraphProperties().setSamplingCB(false);
			
			if(requestParamMap.get("outlinerSnapshotCB") != null && requestParamMap.get("outlinerSnapshotCB").equalsIgnoreCase("true"))
				graphInfo.getGraphProperties().setSnapShotSamplingCB(true);
			else
				graphInfo.getGraphProperties().setSnapShotSamplingCB(false);
			
			if(requestParamMap.get("outlinerPaginationCB") != null && requestParamMap.get("outlinerPaginationCB").equalsIgnoreCase("true")
					&& requestParamMap.get("outlinerSamplingCB") != null && requestParamMap.get("outlinerSamplingCB").equalsIgnoreCase("true")
					&& requestParamMap.get("outlinerSnapshotCB") != null && requestParamMap.get("outlinerSnapshotCB").equalsIgnoreCase("true"))
			{
				graphInfo.setSmartSamplingEnable(true);
			}
			else
				graphInfo.setSmartSamplingEnable(false);
			//------------------sampling checkBox end-------------------------------------------------//
			
			//------------------Sorting------------------------------------//
			//checks if sorting of dim is req while creating resultSet
			String isSortReq = "";
			if(requestParamMap.get("isSortReq") != null)
				isSortReq = requestParamMap.get("isSortReq");
			
			if(isSortReq.equals("true"))
				graphInfo.setSmartenSort(true);
			else
				graphInfo.setSmartenSort(false);
			//------------------Sorting------------------------------------//
			
			
			//dimesnion map shifted to pass in ignore dim list
			Map<String, Integer> detailedOutlinerMap =new HashMap();
			
			detailedOutlinerMap = getDimensionSize(userInfo,graphInfo,request,allDetailedOutlinerValues);
			String graphtype = request.getParameter("sngraphtype");
			
			int type = Integer.parseInt(graphtype); 
			graphInfo.setChangedGraphTypeSmarten(type);
			
			/*for new feature 16th Aug to plot chart when there is msr in category as well as in value[ie: no dimension]*/
			//------------------Aggregation--------------------------------//
			if(detailedOutlinerMap.size() > 0)
				onlyMeasureInOutliner = false;
			graphInfo.setPerformAggregation(true);
			/*graphInfo.setNoneDataOperation(false);*/
			if((!isSmartenModeOn && onlyMeasureInOutliner) || (isSmartenModeOn && detailedOutlinerMap.size() == 0 && graphInfo.getGraphData().getMeasureListForSmartenview().size() > 1))
				graphInfo.setPerformAggregation(false);
			
			//------------------Aggregation--------------------------------//
			
			//For sorting dimension value in descending order
			detailedOutlinerMap = smartenService.sortByComparator(detailedOutlinerMap, false);
			graphInfo.setDimensionValueCountMap(detailedOutlinerMap);
			//dimension map shifted to pass in ignore dim list
			
			if(isSmartenModeOn && graphInfo.getMainOutlinerMeasureAndDimension() != null && !graphInfo.getMainOutlinerMeasureAndDimension().isEmpty()
					&& null != graphInfo.getGraphData().getOrderedOutlinerListForSmartenview() && !graphInfo.getGraphData().getOrderedOutlinerListForSmartenview().isEmpty())
			{
				boolean sampleFlag = false;
				int k=0;
				for(int i =0; i < graphInfo.getGraphData().getOrderedOutlinerListForSmartenview().size(); i++)
				{
					for(int j =0; j < graphInfo.getMainOutlinerMeasureAndDimension().size(); j++)
					{
						if(graphInfo.getGraphData().getOrderedOutlinerListForSmartenview().get(i).toString().equalsIgnoreCase(graphInfo.getMainOutlinerMeasureAndDimension().get(j).toString()))
						{
							k++;
						}
						
					}
				}
				if(k==graphInfo.getGraphData().getOrderedOutlinerListForSmartenview().size() && graphInfo.getGraphData().getOrderedOutlinerListForSmartenview().size() == graphInfo.getMainOutlinerMeasureAndDimension().size())
				{
					graphInfo.getGraphProperties().setSamplingSnapShotChanged(true);
					//graphInfo.getGraphProperties().setCallCreateSmartenResultSet(false);//Commented to Sort dimensions as per chart's requirement (Bug #14377 Issue-3)
				}
			}
			
			//isFromGraphTypeChange and changed graphType
			String isGraphTypeChange = request.getParameter("isFromGraphTypeChange");
			String isGraphTypeChangeFromLeftPannel = request.getParameter("isGraphTypeChangeFromLeftPannel");
			/*String graphtype = request.getParameter("sngraphtype");
			
			int type = Integer.parseInt(graphtype); 
			graphInfo.setChangedGraphTypeSmarten(type);*/
			if(isGraphTypeChange.equalsIgnoreCase("true"))
				graphInfo.setFromGraphTypeChangeSmarten(true);
			else
				graphInfo.setFromGraphTypeChangeSmarten(false);
			
			if(isGraphTypeChangeFromLeftPannel.equalsIgnoreCase("true"))
				graphInfo.setGraphTypeChangeFromLeftPannel(true);
			else
				graphInfo.setGraphTypeChangeFromLeftPannel(false);
			
			//isFromGraphTypeChange and changed graphType
			String forceSubCubeCall = request.getParameter("forceSubCubeCall");
			if(forceSubCubeCall != null && forceSubCubeCall.equalsIgnoreCase("true"))
			{
				graphInfo.getGraphProperties().setSamplingSnapShotChanged(false);
				graphInfo.setGraphTypeChangeFromLeftPannel(false);
			}
			
			
			//This is to red from outliner when mode off and set ignore dimList
			String strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();
			List<GeographicalColumnInfo> geoColumnInfoList = smartenService.getGeographicalColumnByCubeID(strCubeId);
			graphInfo.getGraphData().setGeoColumnInfoListForSmartenview(geoColumnInfoList);
			
			List dimensionList = graphInfo.getGraphData().getDimensionListForSmartenview();
			List measureList = graphInfo.getGraphData().getMeasureListForSmartenview();
			
			if(("#8daacb".equals(graphInfo.getGraphProperties().getLinecolor()) && graphType == GraphConstants.COMBINED_GRAPH)
				|| (!measureList.isEmpty() && measureList.size() == 2 && !dimensionList.isEmpty() && dimensionList.size() >= 1 && graphType == 60)) {
				graphInfo.getGraphProperties().setLinecolor("#fc7362");//Temp Finland demo changes 13 Apr 2018
			}
			
			//Added to set geoColumn when parent-child heirarchy start
			if(!geoColumnInfoList.isEmpty())
			{
				List mapDimensionList = new ArrayList();
				for(int i = 0; i < geoColumnInfoList.size(); i++)
				{
					GeographicalColumnInfo geographicalColumnInfo = (GeographicalColumnInfo) geoColumnInfoList.get(i);
					for(int j = 0; j < dimensionList.size(); j++)
					{
						if(strCubeId.endsWith(MashUpConstants.DATASETS_FILE_EXT)) {
							if(dimensionList.get(j).toString().equalsIgnoreCase(geographicalColumnInfo.getGeoGraphicalColumnName()))
								mapDimensionList.add(dimensionList.get(j).toString());
						} else {
							if(geographicalColumnInfo.getGeographicRole().equalsIgnoreCase(dimensionList.get(j).toString()))
								mapDimensionList.add(dimensionList.get(j).toString());
						}
					}
				}
				List sortedDimensionList = smartenService.checkForParentChildHeirarchy(detailedOutlinerMap, graphInfo, userInfo, mapDimensionList);
				if(null != sortedDimensionList && !sortedDimensionList.isEmpty() && !mapDimensionList.isEmpty() && mapDimensionList.contains(sortedDimensionList.get(sortedDimensionList.size()-1).toString()))
					graphInfo.setGeoColumnName(sortedDimensionList.get(sortedDimensionList.size()-1).toString());
				else {
					if(null != mapDimensionList && mapDimensionList.size() > 0)
						graphInfo.setGeoColumnName(mapDimensionList.get(0).toString());
				}
				if(null != sortedDimensionList && !sortedDimensionList.isEmpty()) {
					Collections.reverse(sortedDimensionList);
					graphInfo.setMapDimensionTitleList(sortedDimensionList);
				}
				else {
					if(null != mapDimensionList && mapDimensionList.size() > 0)
						graphInfo.setMapDimensionTitleList(mapDimensionList);
				}
			}
			//Added to set geoColumn when parent-child heirarchy end
			
			graphInfo.setIgnoreDimAndMeasureList(ignoreDimForRowsAndCols);//rows cols measure 9 NOV 2017
			if(requestParamMap.get("smartenModeEnable").toString().equalsIgnoreCase("false"))
			{
				int graphTypeTemp = Integer.parseInt(requestParamMap.get("sngraphtype").toString());
				requestParamMap = ignoreDimensionAndMeasure(requestParamMap,detailedOutlinerMap,graphTypeTemp,false,originalDimensionList);
			}
			//This is to red from outliner when mode off
			
			//For maintaining adjusted digit auto
			//graphInfo.getGraphProperties().setyAxisPropertiesMap(new LinkedHashMap<String, YaxisTrendProperties>());
			//For maintaining adjusted digit auto
			/*//This is to show same color selected when no Legend and auto color selected when Legend
			graphInfo.setSmartenColorAutoCustom(true);*/
			/*if(!isGraphTypeChange.equalsIgnoreCase("true"))//split value axis
				graphInfo.setQuickSettingsNumberFormat(false);*/
			//If andy uddc is provided
			String measureNames = requestParamMap.get("userDataLabels");
			if(!measureNames.equals(""))
				graphInfo.setSmartenMeasureContainsUDDC(true);
			else
				graphInfo.setSmartenMeasureContainsUDDC(false);
			CubeVector<String> orderedColumnInfoList = new CubeVector<String>();
			String[] tempData = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
			String allMeasureString = "";
			for(int k = 0;k<tempData.length;k++)
			{
				orderedColumnInfoList.add(tempData[k]);
				if(k==0)
					allMeasureString = tempData[k];
				else
					allMeasureString = allMeasureString+ "," + tempData[k];
			}
			String ignoreString = requestParamMap.get("ignoreString").toString();
			if (ignoreString.endsWith(",")) 
				ignoreString = ignoreString.substring(0, ignoreString.length() - 1);
			String[] ignoreArr = ignoreString.split(",");
			
			List frontEndIgnoreList = Arrays.asList(ignoreArr);//front end
			/*map.put("allMeasures", allMeasureString);
			smartenService.addRemoveYAxisPropertiesFromMap(graphInfo, userInfo.getUserId(), orderedColumnInfoList);*/
			
			//Map<String, Integer> dimensionCountMap =new HashMap();
			/*Map<String, Integer> detailedOutlinerMap =new HashMap();*/

			//Added crOutlinerMap to ignore dimensions of Rows and Cols when Detail Outliner
			/*Map<String, Integer> crOutlinerMap =new HashMap();
			crOutlinerMap = getDimensionSize(userInfo,graphInfo,request,crDetailedOutlinerValues);
			crOutlinerMap = smartenService.sortByComparator(crOutlinerMap, false);*/
			
			/*if(isFromMainOutliner)
				dimensionCountMap = getDimensionSize(userInfo,graphInfo,request,allDetailedOutlinerValues);*/
			
			//shifted above
			//detailedOutlinerMap = getDimensionSize(userInfo,graphInfo,request,allDetailedOutlinerValues);
			//detailedOutlinerMap = smartenService.sortByComparator(detailedOutlinerMap, false);
			//graphInfo.setDimensionValueCountMap(detailedOutlinerMap);
			//Added to get geoColumns existing in selected cube SmartenView Map start
			/*String strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();
			List<GeographicalColumnInfo> geoColumnInfoList = smartenService.getGeographicalColumnByCubeID(strCubeId);
			graphInfo.getGraphData().setGeoColumnInfoListForSmartenview(geoColumnInfoList);*/
			//Added to get geoColumns existing in selected cube SmartenView Map end
			
			//Check whether smarten mode is enable(If yes then set detailed outliner to main outliner)
//			boolean isSmartenModeOn = false;
//			String smartenMode = request.getParameter("smartenModeEnable");
//			if(smartenMode != null && smartenMode.equalsIgnoreCase("true"))
//				isSmartenModeOn = true;
			
			//main outliner has only dim or only msr(plot forced tabular and/or TREELAYOUT).
			//boolean isOneColumn = false;
			/*	// CODE For plotting of Map only on SIZE/SHAPE
			if(graphInfo.isSmartenMap() && !graphInfo.getMeasureTitleList().isEmpty())//To consider Size/Shape as measure when Map
			{
				for(int i = 0; i < graphInfo.getMeasureTitleList().size(); i++)
  	        	{
  	        		if(!measureList.contains(graphInfo.getMeasureTitleList().get(i).toString()))
  	        			measureList.add(graphInfo.getMeasureTitleList().get(i).toString());
  	        	}
			}*/
			
			graphInfo.setHideIconsList(smartenService.getIconsList(dimensionList, measureList, graphInfo));
			/*String  chartWihtoutValueAxis= requestParamMap.get("chartWihtoutValueAxis");
			if(graphType != SmartenConstants.SMARTENVIEW_MAP)
			{
				if(dimensionList.size() > 1 && measureList.size() == 0 && !isSmartenModeOn)
				{
					isOneColumn = true;
					requestParamMap.put("isFromGraphTypeChange","true");
					request.setAttribute("isFromGraphTypeChange", "true");
					requestParamMap.put("sngraphtype","65" );
					request.setAttribute("sngraphtype", "65");
				}
				if((dimensionList.size() == 0 && measureList.size() > 0) || (dimensionList.size() == 1 && measureList.size() == 0)
						|| (dimensionList.size() == 0 && measureList.size() == 1) || (chartWihtoutValueAxis != null && chartWihtoutValueAxis.equalsIgnoreCase("true")) && !isSmartenModeOn)
				{
					isOneColumn = true;
					requestParamMap.put("isFromGraphTypeChange","true");
					request.setAttribute("isFromGraphTypeChange", "true");
					if(chartWihtoutValueAxis != null && chartWihtoutValueAxis.equalsIgnoreCase("true"))
					{
						requestParamMap.put("sngraphtype",requestParamMap.get("chartWihtoutValueAxisType"));
						request.setAttribute("sngraphtype",requestParamMap.get("chartWihtoutValueAxisType"));
					}
					else
					{
						requestParamMap.put("sngraphtype","70");
						request.setAttribute("sngraphtype","70");
					}
				}
			}
			else
			{
				String[] rowArr = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
				String[] sizeArr = StringUtil.toArray((String) requestParamMap.get("sizes"));
				String[] shapeArr = StringUtil.toArray((String) requestParamMap.get("shapes"));
				int measureListSize = measureList.size() + rowArr.length + sizeArr.length + shapeArr.length;
				
				if((dimensionList.size() == 0 && measureListSize > 0) || (dimensionList.size() > 0 && measureListSize == 0) && !isSmartenModeOn)
				{
					isOneColumn = true;
					requestParamMap.put("isFromGraphTypeChange","true");
					request.setAttribute("isFromGraphTypeChange", "true");
					requestParamMap.put("sngraphtype","70");
					request.setAttribute("sngraphtype","70");
				}
			}*/
			//main outliner has only dim or only msr.
			if(isSmartenModeOn)
			{
				//orderedDataLabels
				/*String[] orderedDataLabels = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
				if(orderedDataLabels.length>0)
					measureList.addAll(Arrays.asList(orderedDataLabels));*/
				String dimension = StringUtils.join(dimensionList, ',');
				String measure = StringUtils.join(measureList, ',');
				
				requestParamMap.put("outlinerName","mainOutliner");
				
				if(dimensionList.size() > 0 && measureList.size() > 0)
				{
					requestParamMap.put("main", dimension+","+measure);
				}
				if(dimensionList.size() == 0 && measureList.size() > 0)
				{
					requestParamMap.put("main",measure);
				}
				if(dimensionList.size() > 0 && measureList.size() == 0)
				{
					requestParamMap.put("main", dimension);
				}
				//requestParamMap.get("orderedDataLabels")
				requestParamMap.put("orderedDataLabels",measure);//for map outliner issue when 4m 1d[mail]5 Jun[ActaulProb = orederaData was not setting to 4 m when we set mode on]
				//When measureList is of size two it requires datalabels2 for combined chart
				
				//Removing rows,col etc as it is all added in main outliner
				
				smartenService.sizeShapeColorDimensionMeasure(requestParamMap,graphInfo,userInfo);//Added after restricting call on Mode Off
				if(!graphInfo.getGraphData().isRowMeasure())
				{
					graphInfo.setRowMeasure(new Vector());
				}
				/*requestParamMap.put("rows","");
				requestParamMap.put("rowlabels","");
				requestParamMap.put("rowlabels2","");
				requestParamMap.put("sizes","");
				requestParamMap.put("shapes","");
				requestParamMap.put("coloumns","");*/
				
			}
			else
			{
				if(graphInfo.isPerformAggregation())
				{
					ignoreParamsFromFrontEnd(requestParamMap,frontEndIgnoreList);//removes ignore from the map before pagerow function
					smartenService.sizeShapeColorDimensionMeasure(requestParamMap,graphInfo,userInfo);
				}
				/*requestParamMap.put("sizes","");
				requestParamMap.put("shapes","");*/
				if(!graphInfo.getGraphData().isRowMeasure())
				{
					graphInfo.setRowMeasure(new Vector());
				}
				
				//Added for Smarten Mode OFF
				measureList = graphInfo.getGraphData().getMeasureListForSmartenview();
				String measure = StringUtils.join(measureList, ',');
				/*if(measureList.size() > 1 && "16".equals(request.getParameter("sngraphtype")))//Check for Combined
				{}*/
					/*}*/
					/*else
					{
						requestParamMap.put("datalabels2","");//Added to remove Measure from Line Combined
					}*/
				}//Added for Smarten Mode OFF end
			/*}*/
			//Added code for Page filter start
			String strFilterval = "";
			String[][] pageFilterColumns = graphInfo.getMultiPageFilterInfo();
			if(pageFilterColumns != null)
			{
				for(int i = 0; i < pageFilterColumns.length; i++)
				{
					if(i==0)
						strFilterval = pageFilterColumns[i][0];
					else
						strFilterval += ","+pageFilterColumns[i][0];
				}
			}
			requestParamMap.put("pagelabels",strFilterval);
			//Added code for Page filter end
			
			
			if (graphInfo.getGraphMode() == AppConstants.NEW_MODE && requestParamMap.get("outlinerMode").toString().equals("1")) {			
				String strRecords =  StringUtil.null2String(request.getParameter("limitedrecord"));
				if(strRecords != null && !strRecords.equals("")) {
					graphInfo.setRecordlimit(Boolean.valueOf(strRecords));
				}
			}
			//rows
			graphInfo.getGraphData().setDpListStartIndex(0);
			String fromGraphTypeChange = request.getParameter("isFromGraphTypeChange");
			graphtype =request.getParameter("sngraphtype");
			String sngraphtype = request.getParameter("sngraphtype");
			/*if(isOneColumn)
			{
				graphType = Integer.parseInt(requestParamMap.get("sngraphtype").toString());
				graphtype = requestParamMap.get("sngraphtype").toString();
				sngraphtype = requestParamMap.get("sngraphtype").toString();
				isGraphTypeChange = requestParamMap.get("isFromGraphTypeChange");
				
				fromGraphTypeChange = requestParamMap.get("isFromGraphTypeChange");
				type = Integer.parseInt(graphtype); 
				graphInfo.setChangedGraphTypeSmarten(type);
				graphInfo.setRecommendGraphType(type);
				if(isGraphTypeChange.equalsIgnoreCase("true"))
					graphInfo.setFromGraphTypeChangeSmarten(true);
				else
					graphInfo.setFromGraphTypeChangeSmarten(false);
			}*/
			
			String windowScreenWidth = request.getParameter("windowScreenWidth");
			String windowScreenHeight = request.getParameter("windowScreenHeight");
			try
			{
				if (!"".equals(windowScreenWidth) && !"".equals(windowScreenHeight))
				{
					graphInfo.setWindowScreenWidth(Integer.parseInt(windowScreenWidth));
					graphInfo.setWindowScreenHeight(Integer.parseInt(windowScreenHeight));
				}
			}
			catch(Exception e)
			{
				graphInfo.setWindowScreenWidth(0);
				graphInfo.setWindowScreenHeight(0);
			}
			
			//When we disable smarten mode after tabular view
			if(graphtype != null && !graphtype.equalsIgnoreCase("") && graphtype.equalsIgnoreCase("70"))
				graphInfo.getGraphData().setSmartenType(GraphConstants.VBAR_GRAPH);
			if(graphtype != null && !graphtype.equalsIgnoreCase("") && !graphtype.equalsIgnoreCase("70"))
				graphInfo.getGraphData().setSmartenType(Integer.parseInt(graphtype));
			if(graphtype != null && !graphtype.equalsIgnoreCase("") && !graphtype.equalsIgnoreCase("70"))
				graphInfo.setSmartenType(Integer.parseInt(graphtype));
			if((graphtype != null && !graphtype.equalsIgnoreCase("") && graphtype.equalsIgnoreCase("70")))//Turn of smarten when analysis
				graphInfo.setSmartenType(GraphConstants.VBAR_GRAPH);
			map.put("allMeasures", allMeasureString);
			//smartenService.addRemoveYAxisPropertiesFromMap(graphInfo, userInfo.getUserId(), orderedColumnInfoList);
			boolean firstTimeSmarten=false;
			if(graphInfo.isFromGraphTypeChangeSmarten() ==false && graphInfo.isGraphTypeChanged() == false && graphInfo.isSmartenMode()==false && graphInfo.getGraphType()==1 ) {
				//graphInfo.getGraphData().setSmartenType(60);
				
				/*if((requestParamMap.get("chartWihtoutValueAxis").equals("true") || !requestParamMap.get("ignoreString").equals("")) ||
						(null  != graphInfo.getGraphData().getColorMeasureLabel() && !graphInfo.getGraphData().getColorMeasureLabel().equals(""))) {
					firstTimeSmarten=false;
				}
				
				else {
				firstTimeSmarten= true;
				}*/
				//if(!requestParamMap.get("datalabels").equals("") &&!graphInfo.getGraphData().getColorMeasureLabel().equals("") &&!graphInfo.getGraphData().getSizeLabel().equals("") ) {
					//firstTimeSmarten= true;
				//}
				if(requestParamMap.get("datalabels").equals("") &&!requestParamMap.get("ignoreString").equals("") ) {
					firstTimeSmarten= false;
				}
				else {
					firstTimeSmarten= true;
				}
				if(requestParamMap.get("chartWihtoutValueAxis").equals("true") && requestParamMap.get("ignoreString").equals("") &&requestParamMap.get("datalabels").equals("")&&graphInfo.getGraphData().getMeasureListForSmartenview().isEmpty()) {
					firstTimeSmarten= false;
				}
				if(requestParamMap.get("collabels").equals("")) {
					firstTimeSmarten= false;
				}
				if(requestParamMap.get("changesMade").equals("true")) {
					firstTimeSmarten= false;
				}
					if(requestParamMap.get("datalabels").equals("") &&  firstTimeSmarten &&!graphInfo.getGraphData().getMeasureListForSmartenview().isEmpty()) {
						requestParamMap.put("datalabels", graphInfo.getGraphData().getMeasureListForSmartenview().get(0));
						requestParamMap.put("orderedDataLabels", graphInfo.getGraphData().getMeasureListForSmartenview().get(0));
					}
					graphInfo.setIgnoreDimAndMeasureList(new ArrayList<>());
					
			}
			//Added to change to any graph from Map start
			if(fromGraphTypeChange.equals("true") && graphInfo.isSmartenMap() && !graphtype.equals("71"))
				graphInfo.setSmartenMap(false);
			//Added to change to any graph from Map end
			
			boolean graphTypeChange = true;
			
			
			
			
			
			
				/*if(graphtype.equals("71") && fromGraphTypeChange.equals("true"))//Added to show Map when fromGraphTypeChange
				{
					graphInfo.setSmartenMap(true);
					graphInfo.setSmartenTabular(false);
					smartenService.getSmartenViewMapDetails(graphInfo, graphInfo.getDimensionTitleList().get(0).toString(), graphInfo.getDimensionTitleList().size(), graphInfo.getMeasureTitleList().size(),true);
				}
				else */if(fromGraphTypeChange.equals("true"))//Added to plot graphs from tabular when user clicks on any graph from View section
				{
					if(graphtype.equals("70"))//70
						graphInfo.setSmartenTabular(true);
					else
					{
						graphInfo.setSmartenTabular(false);
						graphInfo.setSmartenMap(false);
						graphInfo.setGraphType(graphType);
						if(graphtype.equals("71"))//Added to show Map when fromGraphTypeChange
						{
							graphInfo.setSmartenMap(true);
							graphInfo.setSmartenTabular(false);
							smartenService.getSmartenViewMapDetails(graphInfo, graphInfo.getDimensionTitleList().get(0).toString(), graphInfo.getDimensionTitleList().size(), graphInfo.getMeasureTitleList().size(),true);
						}
						if(isSmartenModeOn)//Added to not set according to algorithm when Smarten Mode OFF
							smartenService.setAndValidateMainOutlinerForTabularToGraph(requestParamMap, detailedOutlinerMap, graphInfo, userInfo);//Moved here to pass theRowColLabels for Tabular
					}
					//smartenService.setAndValidateMainOutlinerForTabularToGraph(requestParamMap, detailedOutlinerMap, graphInfo, userInfo);
				}
				else if(isSmartenModeOn)
				{
					if(fromGraphTypeChange.equals("true"))//Added to plot graphs from tabular when user clicks on any graph from View section
					{
						graphInfo.setGraphType(graphType);
						if(graphtype.equals("70"))//Turn of smarten when analysis
						{
							graphType = GraphConstants.VBAR_GRAPH;
							graphInfo.setSmartenTabular(true);
							graphInfo.getGraphData().setSmartenType(GraphConstants.VBAR_GRAPH);
							graphInfo.setSmartenType(GraphConstants.VBAR_GRAPH);
							smartenService.setAndValidateMainOutlinerForTabularToGraph(requestParamMap, detailedOutlinerMap, graphInfo, userInfo);
						}
						/*else
							graphInfo.setGraphType(graphType);*/
						
					}
					
					
					int graphtypetemp=0;
					if(null != graphInfo.getRecommendGraphType() && graphType == graphInfo.getRecommendGraphType())//Added
					{
						graphType = SmartenConstants.AUTO_GRAPH;//60
						graphtype = "60";
						requestParamMap.put("sngraphtype","60");//graphtype = "60";
						graphInfo.getGraphData().setSmartenType(SmartenConstants.AUTO_GRAPH);
					}
					
					if(graphType != SmartenConstants.AUTO_GRAPH)
					{
					//HashMap<String, String> temprequestParamMap = new HashMap<String, String>(requestParamMap);					
					graphtypetemp=graphInfo.getGraphType();
					//HashMap<String, String> requestParamMapOriginal = new HashMap<String, String>(requestParamMap);
					graphInfo.getGraphData().setSmartenType(SmartenConstants.AUTO_GRAPH);
					smartenService.setSmarten(requestParamMap,graphInfo,userInfo.getUserId(),detailedOutlinerMap,userInfo,sngraphtype,false);
					HashMap<String, String> requestParamMapOriginal = new HashMap<String, String>(requestParamMap);
					//requestParamMap=temprequestParamMap;
						graphInfo.setGraphType(graphtypetemp);
						if(graphtypetemp != SmartenConstants.SMARTENVIEW_MAP)
							graphInfo.setSmartenMap(false);
						
						//(PN,City,month,Cog)Tab to Vbar(or any chart),open main outliner and apply,it changes to TAB bcz setTabular becomes true in setSmarten
						if(Integer.parseInt(sngraphtype) != 70)//current selected graph not tab
						{
							graphInfo.setSmartenTabular(false);
						}
						String outlinerName = requestParamMap.get("outlinerName");
						if(outlinerName.equalsIgnoreCase("mainOutliner") && Integer.parseInt(sngraphtype) != 70)
						{
							//Mode on,graph type changed,open main outliner apply.(re setting the Dim without any smarten size constrain as grph is selected by user)
							smartenService.setAndValidateMainOutlinerForTabularToGraph(requestParamMapOriginal, detailedOutlinerMap, graphInfo, userInfo);
							requestParamMap = new HashMap<String, String>(requestParamMapOriginal);
						}
						
					}else{
						smartenService.setSmarten(requestParamMap,graphInfo,userInfo.getUserId(),detailedOutlinerMap,userInfo,sngraphtype,false);
					}
					
						
				}
				else if (firstTimeSmarten) {
					smartenService.setSmarten(requestParamMap,graphInfo,userInfo.getUserId(),detailedOutlinerMap,userInfo,sngraphtype,false);
				}
				else//When smarten mode is disable and current mode is tabular
				{
					if(graphtype.equalsIgnoreCase("70"))
					{
						graphInfo.setSmartenTabular(true);
					}
					/*if(fromGraphTypeChange.equals("true"))//Added to plot graphs when user clicks on any graph from View section while smarten mode is Off
						smartenService.setAndValidateMainOutlinerForTabularToGraph(requestParamMap, detailedOutlinerMap, graphInfo, userInfo);*/
				}
				/*This code is in setAndValidateMainOutlinerForTabularToGraph
				Reason:when we make xy chart in auto[uniqueCarrier,3m] then we try and drag UDDC then it is not added in dataLabels and makes vbar on 3m instead of 4m,
				this same code is in setAndValidateMainOutlinerForTabularToGraph,when user clicks on vbar the below code executes and works fine*/ 
				String[] useraddeddatalabels= StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));
				if(graphInfo.getGraphType() != GraphConstants.COMBINED_GRAPH && useraddeddatalabels.length == 0)
				    requestParamMap.put("datalabels",requestParamMap.get("orderedDataLabels"));
				
				//d3 chart validation
				graphtype = requestParamMap.get("sngraphtype");
				graphType = Integer.parseInt(graphtype);
				if(firstTimeSmarten) {
					graphInfo.setSmartenTabular(false );
					graphType = graphInfo.getRecommendGraphType();
					graphInfo.getGraphData().setSmartenType(graphInfo.getRecommendGraphType());
					graphInfo.setGraphType(graphInfo.getRecommendGraphType());
				}
				//shifted from below
				if(!graphtype.equals("60"))
					graphInfo.setGraphType(graphType);
				if(graphInfo.getGraphType()==16)
					measureList.removeAll(frontEndIgnoreList);
				if(measureList.size() > 1 && graphType != 71)//Not for 71 = Map
				{
				
					if(measureList.size() > 0 && graphInfo.getGraphType()==16 && !graphInfo.getGraphData().isRowMeasure() && graphInfo.isPerformAggregation())//Check for Combined
					{
						
						
						String[] userAddedDataItems = StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));
						int uddcPosition1= Arrays.asList(userAddedDataItems).indexOf(measureList.get(0).toString());
						int uddcPosition2= Arrays.asList(userAddedDataItems).indexOf(measureList.get(1).toString());
						//List<ActiveUDDCInfo> activeUDDCList = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
						String[] activeUddcs = StringUtil.toArray((String) requestParamMap
								.get("userDataLabels"));
						
						List<String> actUddcStringList = new ArrayList<String>();
					/*	Vector outlinerDataColumns = graphInfo.getOutlinerDataColumns();//Code for adding new added UDDC in Detail outliner
						Vector finalOutlinerDataColumns = new Vector();*/
						//List<ActiveUDDCInfo> activeUDDCList = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
						
						int activeUDDCIndex1=-1;						
						int activeUDDCIndex2=-1;	
						if(activeUddcs.length > 0) 
						{
							//Object[] activeTemplateIds = new Object[activeUDDCList.size()];
							for (int cnt = 0; cnt < activeUddcs.length; cnt++) 
							{
								/*ActiveUDDCInfo activeuddcInfo = activeUDDCList.get(cnt);
								activeTemplateIds[cnt] = activeuddcInfo.getUddcTemplateInfo();*/
								actUddcStringList.add(activeUddcs[cnt]);
							}
							 activeUDDCIndex1=actUddcStringList.indexOf(measureList.get(0).toString());						
							 activeUDDCIndex2=actUddcStringList.indexOf(measureList.get(1).toString());	
						}
						
						
						if(userAddedDataItems.length > 0)
						{
							if(uddcPosition1 > 0 )
							{
								if(uddcPosition1%2 == 0) 
								{
								requestParamMap.put("datalabels",measureList.get(0).toString());								
								requestParamMap.put("useraddeddatalabels","");
								requestParamMap.put("orderedDataLabels",measureList.get(0).toString());
								}	
								else									
								{
										requestParamMap.put("datalabels","");									
										requestParamMap.put("useraddeddatalabels",userAddedDataItems[uddcPosition1-1]+","+userAddedDataItems[uddcPosition1]);
										requestParamMap.put("orderedDataLabels",userAddedDataItems[uddcPosition1]);
									}
							}else {		
								
								if(activeUDDCIndex1 >= 0)
								{
									requestParamMap.put("datalabels",measureList.get(0).toString());								
									requestParamMap.put("useraddeddatalabels","");
									requestParamMap.put("orderedDataLabels",measureList.get(0).toString()+","+measureList.get(1).toString());
									requestParamMap.put("dataandUserLabelsBar",measureList.get(0).toString());
								}else {
								requestParamMap.put("datalabels",measureList.get(0).toString());								
								requestParamMap.put("useraddeddatalabels","");
								requestParamMap.put("orderedDataLabels",measureList.get(0).toString()+","+measureList.get(1).toString());
								}
							}
							if(uddcPosition2 > 0)
							{
								if(uddcPosition2%2 == 0) 
								{
									requestParamMap.put("datalabels2",measureList.get(1).toString());								
									requestParamMap.put("useraddeddatalabels2","");
									requestParamMap.put("orderedDataLabels",requestParamMap.get("orderedDataLabels")+","+measureList.get(1).toString());
								}	
								else									
								{
									requestParamMap.put("datalabels2","");									
									requestParamMap.put("useraddeddatalabels2",userAddedDataItems[uddcPosition2-1]+","+userAddedDataItems[uddcPosition2]);
									requestParamMap.put("orderedDataLabels",requestParamMap.get("orderedDataLabels")+","+measureList.get(1).toString());
								}
							}
							else {
								if(activeUDDCIndex2 >= 0)
								{
									requestParamMap.put("datalabels2",measureList.get(1).toString());								
									requestParamMap.put("useraddeddatalabels","");
									requestParamMap.put("orderedDataLabels",measureList.get(0).toString()+","+measureList.get(1).toString());
									requestParamMap.put("dataandUserLabelsLine",measureList.get(1).toString());
								}else {
								requestParamMap.put("datalabels2",measureList.get(1).toString());								
								requestParamMap.put("useraddeddatalabels2","");
								requestParamMap.put("orderedDataLabels",requestParamMap.get("orderedDataLabels")+","+measureList.get(1).toString());
								}
							}
							
						}else {
							if(activeUDDCIndex1 >= 0)
							{
								requestParamMap.put("datalabels",measureList.get(0).toString());								
								requestParamMap.put("useraddeddatalabels","");
								requestParamMap.put("orderedDataLabels",measureList.get(0).toString()+","+measureList.get(1).toString());
								requestParamMap.put("dataandUserLabelsBar",measureList.get(0).toString());
							}else {
								requestParamMap.put("datalabels",measureList.get(0).toString());
								//requestParamMap.put("datalabels2",measureList.get(1).toString());
								requestParamMap.put("useraddeddatalabels","");
								//requestParamMap.put("useraddeddatalabels2","");
								requestParamMap.put("orderedDataLabels",measureList.get(0).toString()+","+measureList.get(1).toString());
							}
							if(activeUDDCIndex2 >= 0)
							{
								requestParamMap.put("datalabels2",measureList.get(1).toString());								
								requestParamMap.put("useraddeddatalabels","");
								requestParamMap.put("orderedDataLabels",measureList.get(0).toString()+","+measureList.get(1).toString());
								requestParamMap.put("dataandUserLabelsLine",measureList.get(1).toString());
							}else {
								//requestParamMap.put("datalabels",measureList.get(0).toString());
								requestParamMap.put("datalabels2",measureList.get(1).toString());
								requestParamMap.put("useraddeddatalabels","");
								//requestParamMap.put("useraddeddatalabels2","");
								requestParamMap.put("orderedDataLabels",measureList.get(0).toString()+","+measureList.get(1).toString());
							}
							
							
							
							
						}						
						if(measureList.size() > 2)
						{
							List ignoreDimMeasureList=new ArrayList();							 
							for(int i=2;i<measureList.size();i++)
							{
								ignoreDimMeasureList.add(measureList.get(i).toString());
							}
							graphInfo.setIgnoreDimAndMeasureList(ignoreDimMeasureList);
							
						}
					
						
						
					}
					
					
					
					
				}
				
				
				
				
				//if PIE keep smallest(row) in rows and apply sampling on that dim
				boolean isPieModified = false;
				List newDimensionList = smartenService.checkForParentChildHeirarchy(detailedOutlinerMap, graphInfo, userInfo, new ArrayList());
				if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH && graphInfo.getGraphData().getDimensionListForSmartenview().size() >1 && isSmartenModeOn)
				{
					String colName = "";
					String rows="";
					String row = "";
					
					if(requestParamMap.get("rowlabels") != null)
						row = requestParamMap.get("rowlabels").toString();
					if(requestParamMap.get("collabels") != null)
						colName = requestParamMap.get("collabels").toString();
					if(requestParamMap.get("rows") != null)
						rows = requestParamMap.get("rows").toString();
					
					if(!"".equals(requestParamMap.get("rowlabels").toString()) )//Added for Bug #14756
					{
						requestParamMap.put("collabels",requestParamMap.get("rowlabels").toString());
						requestParamMap.put("rows",colName);
						if(!rows.equals("")) {
							requestParamMap.put("rows",rows+","+colName);
						}
						requestParamMap.put("rowlabels","");
						isPieModified = true;
					}
					//isPieModified = true;
				}
				//ends
				if(isPieModified)//graphtype.equals("71") || !isSmartenModeOn || 
				 {
				String[] rowItemsForLine = StringUtil.toArray((String) requestParamMap.get("rowlabels2"));
				String[] rowItems = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
				String[] sizes = StringUtil.toArray((String) requestParamMap.get("sizes"));
			    String[] shape = StringUtil.toArray((String) requestParamMap.get("shapes"));
			    String[] dataItems = StringUtil.toArray((String) requestParamMap.get("datalabels"));
			    String[] colItems = StringUtil.toArray((String) requestParamMap.get("collabels"));
			    String rowsDimensionName = requestParamMap.get("rows");
				String coloumnsDimensionValue = requestParamMap.get("coloumns");
			    String[] rows = StringUtil.toArray(rowsDimensionName);
			    String[] coloumns = StringUtil.toArray(coloumnsDimensionValue);
			    String[] orderedData = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));			    
			    
			    //Added for Smarten Map to set Detail Outliner start
			    dimensionList = graphInfo.getGraphData().getDimensionListForSmartenview();
			    if(graphtype.equals("71") && null != dimensionList)
			    {
			    	/*if(dimensionList.size() > 0) 
			    		colItems = StringUtil.toArray((String) dimensionList.get(0).toString());
			    	if(dimensionList.size() > 1)
			    		rowItems = StringUtil.toArray((String) dimensionList.get(1).toString());*/
			    	colItems = new String[dimensionList.size()];
			    	dimensionList.toArray(colItems);
			    }
			    //Added for Smarten Map to set Detail Outliner end
			    
			    smartenService.setDetailOutlinerData(graphInfo, rowItems, colItems, rows, rowItemsForLine,coloumns, sizes, shape, orderedData);
				 }
				
				//
				/* if((graphtype.equals("70")) && chartWihtoutValueAxis != null && !chartWihtoutValueAxis.equalsIgnoreCase("true"))
					 graphInfo.setIgnoreDimAndMeasureList(new ArrayList());*/
				 
				dimensionList = graphInfo.getGraphData().getDimensionListForSmartenview();
				measureList = graphInfo.getGraphData().getMeasureListForSmartenview();
				smartenService.setMainOutliner(graphInfo,dimensionList,measureList);
				//
					
				//shifted above [Not for 71 = Map] as discussed bcz when we click combined to tabular the 3rd measure gets ignored[mail task:"Tabular"] 5 jun 2018
				/*if(!graphtype.equals("60"))
					graphInfo.setGraphType(graphType);*/
			
				/*//jun 14 2017
				if(fromGraphTypeChange.equals("true"))
					smartenService.addRemoveYAxisPropertiesFromMap(graphInfo, userInfo.getUserId(), orderedColumnInfoList);*/
				//Ignore size and shape if it's not line chart
				if(!(graphInfo.getGraphType().equals(GraphConstants.LINE_GRAPH) ||  graphInfo.getGraphType().equals(GraphConstants.VBAR_GRAPH) || graphInfo.getGraphType().equals(GraphConstants.HBAR_GRAPH))  && !(graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR))
				{
					graphInfo.setSizeColumns(new Vector());
					graphInfo.setShapeColumns(new Vector());
				}
				
				if((graphInfo.getGraphType().equals(GraphConstants.VBAR_GRAPH) || graphInfo.getGraphType().equals(GraphConstants.HBAR_GRAPH)) &&  !graphInfo.getGraphData().isSizeMeasure())
				{
					graphInfo.setSizeColumns(new Vector());
					
				}
				if((graphInfo.getGraphType().equals(GraphConstants.VBAR_GRAPH) || graphInfo.getGraphType().equals(GraphConstants.HBAR_GRAPH)) &&  !graphInfo.getGraphData().isShapeMeasure())
				{
					graphInfo.setShapeColumns(new Vector());
					
				}
				
			
				String[] orderedData = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
			if(orderedData.length == 2 && graphType==60)//restricting 2measure in Bar and Line Combined
			{
				//requestParamMap.put("datalabels2","");
				String[] rowItems = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
				if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH && rowItems.length > 0)//Smarten View stability //Added to plot Combined on 1M & 1D as fromGraphTypeChange (like 4.3 when from other graph)
					requestParamMap.put("useraddeddatalabels2","");
			}
			
			if(((detailedOutlinerMap.size() > 1 && (graphInfo.getGraphType() == GraphConstants.D3_TREEMAP || graphInfo.getGraphType() == GraphConstants.D3_CHORD
					|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT))//fromGraphTypeChange.equals("true") && 
					||
					(detailedOutlinerMap.size() > 1 && graphInfo.getGraphType() == GraphConstants.D3_BUBBLE)) && isSmartenModeOn)
			{
				Set<Entry<String, Integer>> set = detailedOutlinerMap.entrySet();
				List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);


				Collections.sort( list, new Comparator<Map.Entry<String, Integer>>()
				{
					public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
					{
						return (o1.getValue()).compareTo( o2.getValue() );
					}
				} );

				List<Entry<String, Integer>> list2 = new ArrayList<Entry<String, Integer>>();
				int listSize = list.size();
				List<Entry<String, Integer>> list3 = new ArrayList<Entry<String, Integer>>();
				List ignoreDimMeasureList = graphInfo.getIgnoreDimAndMeasureList();
				if(graphInfo.getGraphType() == GraphConstants.D3_CHORD && listSize >= 2)
				{
					list2.addAll(list.subList(0, 2));//Taking only 2 smallest dimensions
					list3.addAll(list.subList(2, listSize));
				}
				else if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE && listSize >= 5)
				{
					list2.addAll(list.subList(0, 5));//Taking only 5 smallest dimensions
					list3.addAll(list.subList(5, listSize));
				}
				else
				{
					if(!(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE)){
						if(listSize >= 3)
						{
							list2.addAll(list.subList(0, 3));//Taking only 3 smallest dimensions
							list3.addAll(list.subList(3, listSize));
						}
					}
				}


				if(list2==null || list2.size() == 0)
				{
					list2.addAll(list);
				}
				if(graphInfo.getGraphType() == GraphConstants.D3_CHORD)
				Collections.reverse(list2);
				
				List dimensionList2 = new ArrayList();
				if(isSmartenModeOn){
					for(Map.Entry<String, Integer> entry:list2){
						dimensionList2.add(entry.getKey());
					}
				}else{
					if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE){
						List<String> measure = new ArrayList<String>();
						String[] dataItems = StringUtil.toArray((String) requestParamMap.get("datalabels"));
						
						measure.add(dataItems[0].toString());
						if(graphInfo.getGraphData().isRowMeasure())
						{
							measure.add(graphInfo.getGraphData().getColorMeasureLabel().toString());
						}
						graphInfo.getGraphData().setMeasureListForSmartenview(measure);
					String rowName = requestParamMap.get("rowlabels");
					for(Map.Entry<String, Integer> entry:list2){
						if(entry.getKey().toString().equals(rowName))
						{							
								dimensionList2.add(entry.getKey());
						}						
					}
					for(Map.Entry<String, Integer> entry:list2){
						if(!entry.getKey().toString().equals(rowName))
						{						
								dimensionList2.add(entry.getKey());
						}						
					}
					
					}else if(graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREEMAP)
					{
						String rowName = requestParamMap.get("rowlabels");
						for(Map.Entry<String, Integer> entry:list2){
							if(entry.getKey().toString().equals(rowName))
							{							
									dimensionList2.add(entry.getKey());
							}						
						}
						for(Map.Entry<String, Integer> entry:list2){
							if(!entry.getKey().toString().equals(rowName))
							{						
									dimensionList2.add(entry.getKey());
							}						
						}
					}
					else{
						for(Map.Entry<String, Integer> entry:list2){
							dimensionList2.add(entry.getKey());
						}
					}
				}
				for(Map.Entry<String, Integer> entry:list3){
					ignoreDimMeasureList.add(entry.getKey());
				}
				if(dimensionList2.size() == 3 
						&& (graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREEMAP)
						&&	(graphInfo.getGraphData().isParentChild() && !graphInfo.isFromGraphTypeChangeSmarten()))
				{
				
					
						dimensionList2.clear();
						dimensionList2.add(requestParamMap.get("rows"));
						dimensionList2.add(requestParamMap.get("collabels"));
						dimensionList2.add(requestParamMap.get("rowlabels"));
				
					
					
					
				}	
				
				graphInfo.getGraphData().setDimensionListForSmartenview(dimensionList2);
				/*for(int i=1;i<graphInfo.getGraphData().getMeasureListForSmartenview().size();i++){
					
					if(!(graphInfo.getGraphData().getColorMeasureLabel()!=null 
							&& graphInfo.getGraphData().getColorMeasureLabel().toString().equals(graphInfo.getGraphData().getMeasureListForSmartenview().get(i).toString()))
					  ){
						ignoreDimMeasureList.add(graphInfo.getGraphData().getMeasureListForSmartenview().get(i).toString());
						graphInfo.getGraphData().getMeasureListForSmartenview().remove(i);
						}
					
					
				}*/
			
				List measureList2 = new ArrayList();
				if(null != graphInfo.getGraphData().getMeasureListForSmartenview() && !graphInfo.getGraphData().getMeasureListForSmartenview().isEmpty())
				{
					
						for(int i=1;i<graphInfo.getGraphData().getMeasureListForSmartenview().size();i++){
						if(!(graphInfo.getGraphData().getColorMeasureLabel()!=null 
								&& graphInfo.getGraphData().getColorMeasureLabel().toString().equals(graphInfo.getGraphData().getMeasureListForSmartenview().get(i).toString()))
						  ){
							if(ignoreDimMeasureList.indexOf(graphInfo.getGraphData().getMeasureListForSmartenview().get(i).toString()) < 0)
							ignoreDimMeasureList.add(graphInfo.getGraphData().getMeasureListForSmartenview().get(i).toString());
							
							}
						}
						
					/*}else{
						for(int i=0;i<ignoreDimMeasureList.size();i++){
							for(int j=1;j<graphInfo.getGraphData().getMeasureListForSmartenview().size();j++){
								if(!(ignoreDimMeasureList.get(i).toString().equals(graphInfo.getGraphData().getMeasureListForSmartenview().get(j).toString())))
								if(!(graphInfo.getGraphData().getColorMeasureLabel()!=null 
										&& graphInfo.getGraphData().getColorMeasureLabel().toString().equals(graphInfo.getGraphData().getMeasureListForSmartenview().get(j).toString()))
								  ){
									ignoreDimMeasureList.add(graphInfo.getGraphData().getMeasureListForSmartenview().get(j).toString());
									
									}
								}
						}
					}*/
					String[] datalabels = StringUtil.toArray((String) requestParamMap.get("datalabels"));
					String[] orderedDataLabels = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
					if(datalabels.length > 0)//Check to handle "Failed to set outliner"
					{
						requestParamMap.put("datalabels", datalabels[0]);
						requestParamMap.put("orderedDataLabels", datalabels[0]);
					}
					
						measureList2.add(graphInfo.getGraphData().getMeasureListForSmartenview().get(0));
						if(graphInfo.getGraphData().getColorMeasureLabel()!=null && !(graphInfo.getGraphType() == GraphConstants.D3_CHORD))
						{
							measureList2.add(graphInfo.getGraphData().getColorMeasureLabel());
							if(datalabels.length > 0)
							{
								requestParamMap.put("datalabels", datalabels[0]+","+graphInfo.getGraphData().getColorMeasureLabel());
								requestParamMap.put("orderedDataLabels", datalabels[0]+","+graphInfo.getGraphData().getColorMeasureLabel());
							}
							
						}
				}
				//List tempmeasureList2 = new ArrayList(measureList2.get(0));
				
				if(null != graphInfo.getRecommendGraphType() && graphType != graphInfo.getRecommendGraphType() && "65".equals(sngraphtype)
						&& !measureList2.isEmpty())//When user has changed graphType to D3_TREELAYOUT ignore measures 7 Feb 2018
				{
					ignoreDimMeasureList.addAll(measureList2);
					measureList2 = new ArrayList();
				}
				graphInfo.getGraphData().setMeasureListForSmartenview(measureList2);//Only 1M when D3 graphs
				graphInfo.setIgnoreDimAndMeasureList(ignoreDimMeasureList);
			}
			//Added to plot D3 graphs taking smallest dimensions and ignoring other dimensions end
			//when mode OFF no need to take smallest,plot on cat and row
			if((graphInfo.getGraphType() == GraphConstants.D3_TREEMAP || graphInfo.getGraphType() == GraphConstants.D3_CHORD
					|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT || graphInfo.getGraphType() == GraphConstants.D3_BUBBLE) && !isSmartenModeOn)
			{
				List dim = new ArrayList();
				List measure = new ArrayList();
				
				String[] colItems = StringUtil.toArray((String) requestParamMap.get("collabels"));
				String[] rowItems = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
				String[] data = StringUtil.toArray((String) requestParamMap.get("datalabels"));
				
				if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE)
				{
					if(data.length > 0)
						measure.add(data[0].toString());
					if(rowItems.length > 0)
					{
						for(int i=0;i<rowItems.length;i++)
							dim.add(rowItems[i].toString());
					}
					
					if(colItems.length > 0)
					{
						for(int i=0;i<colItems.length;i++)
							dim.add(colItems[i].toString());
					}
				}else {

				
					if(data.length > 0)
						measure.add(data[0].toString());
					if(colItems.length > 0)
					{
						for(int i=0;i<colItems.length;i++)
							dim.add(colItems[i].toString());
					}
					if(rowItems.length > 0)
					{
						for(int i=0;i<rowItems.length;i++)
							dim.add(rowItems[i].toString());
					}
				
				}		
				graphInfo.getGraphData().setDimensionListForSmartenview(dim);//row/col
				graphInfo.getGraphData().setMeasureListForSmartenview(measure);//1M
				
			}
			//when mode OFF no need to take smallest,plot on cat and row
			
				if(graphInfo.getGraphType()==GraphConstants.COMBINED_GRAPH){
					String[] rowItems = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
					if(rowItems.length > 0)
					{
					requestParamMap.put("rows", rowItems[0]);
					requestParamMap.put("rowlabels", "");
					}
				}
			
				//smart color start
				//If range or custom color is selected do not go for smart color visualization,when legend and show same color change to AUTO(10 Oct 2017)
				/*int colorType = graphInfo.getGraphProperties().getColorType();
				String rowName = requestParamMap.get("rowlabels");
				if((colorType != 3 && colorType != 1) || (!rowName.equalsIgnoreCase("") && colorType == 2)) 
					graphInfo.setSmartenColorAutoCustom(true);*/
				//smart color end
				
				
				/*if((graphInfo.getGraphType() == GraphConstants.D3_TREEMAP 
						|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST 
						|| graphInfo.getGraphType() == GraphConstants.D3_BUBBLE) && !isSmartenModeOn)
				{
					rowName = requestParamMap.get("rowlabels");
							for (Map.Entry<String, Integer> entry : detailedOutlinerMap.entrySet())
							{
								if(entry.getKey().equals(rowName.toString()))
							   
							}
				}*/
				
			//
			String rowName = requestParamMap.get("rowlabels");
			String[] colItems = StringUtil.toArray((String) requestParamMap.get("collabels"));
		    String[] rowItems = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
		    if(colItems.length == 0 && rowItems.length > 0)				
			{
		    	String row = requestParamMap.get("rowlabels");
		    	requestParamMap.put("collabels",requestParamMap.get("rowlabels"));
				requestParamMap.put("rowlabels", "");
				
				//non Aggregate[only measure] when[M > 3] set mode off -> open outliner -> apply for PIE
				if(!dimensionList.contains(row))
				{
					graphInfo.setCategoryMeasure(true);
					graphInfo.setCategoryMeasureLabel(row);
				}
				//non Aggregate[only measure] when[M > 3] set mode off -> open outliner -> apply for PIE
				
			}
			//combined can have only 2 msr and no row.(8 Nov 2017 as discussed)
			if(graphInfo.getChangedGraphTypeSmarten() == GraphConstants.COMBINED_GRAPH && !graphInfo.isSmartenMode())
			{
				String dataLabels = requestParamMap.get("datalabels");
				String dataLabels2 = requestParamMap.get("datalabels2");
				List ignoreDimesnionList = graphInfo.getIgnoreDimAndMeasureList();
				rowName = requestParamMap.get("rowlabels");
				String rowsName = requestParamMap.get("rows");
				String[] userAddedDataItems = StringUtil.toArray((String) requestParamMap
						.get("useraddeddatalabels"));
				String[] userAddedDataItems2 = StringUtil.toArray((String) requestParamMap
						.get("useraddeddatalabels2"));
				if(userAddedDataItems.length  ==userAddedDataItems2.length)
				{
					if(Arrays.asList(userAddedDataItems).equals(Arrays.asList(userAddedDataItems2)))
						requestParamMap.put("useraddeddatalabels" , "");

				}
				String[] dataLabel = StringUtil.toArray((String) requestParamMap
						.get("datalabels"));
				
				String[] dataArr=new String[0];
				if(dataLabel.length > 0)					
				dataArr = dataLabels.split(",");
				
				if(dataArr.length > 1 && userAddedDataItems.length > 1)
				{
					ignoreDimesnionList.add(userAddedDataItems[1].toString());
					requestParamMap.put("useraddeddatalabels" , "");					
				}
				else if(graphInfo.getGraphData().isRowMeasure() && dataLabel.length > 1)
				{
					if(userAddedDataItems.length > 0  && userAddedDataItems[1].toString().equalsIgnoreCase(graphInfo.getGraphData().getColorMeasureLabel()))
					{
						
						ignoreDimesnionList.add(userAddedDataItems[1].toString());
						requestParamMap.put("useraddeddatalabels" , "");
						graphInfo.getGraphData().setRowMeasure(false);
						
					}
					else
					{
						String[] rowlabels = StringUtil.toArray((String) requestParamMap
								.get("rowlabels"));
						if(rowlabels.length > 0)
						graphInfo.getGraphData().setRowMeasure(false);
						ignoreDimesnionList.add(requestParamMap.get("rowlabels").toString());
						requestParamMap.put("useraddeddatalabels" , "");
					}
					
					
						
				}
				for(int i=0;i<dataArr.length;i++)
				{
					if(i >= 2)
					{
						ignoreDimesnionList.add(dataArr[i].toString());
					}
				}
				if(!graphInfo.getGraphData().isRowMeasure() && !isSmartenModeOn)
				{
					if(dataArr.length >= 1)
					{
						requestParamMap.put("datalabels", dataArr[0].toString());
						requestParamMap.put("orderedDataLabels", dataArr[0].toString());
					}
					if(dataArr.length >= 2)
					{
						requestParamMap.put("datalabels2", dataArr[1].toString());
						requestParamMap.put("orderedDataLabels", dataArr[0].toString()+"," + dataArr[1].toString());
					}
				}
				if(rowName != null && !rowName.equals(""))//ignore row
				{
					ignoreDimesnionList.add(rowsName);
					rowsName += rowName;
					requestParamMap.put("rowlabels", rowName);
					requestParamMap.put("rows", rowsName);
				}
				/*if(dataArr.length >= 2)
				requestParamMap.put("useraddeddatalabels", "");//duplicate measure is set in line(datalabels2)
*/				graphInfo.setIgnoreDimAndMeasureList(ignoreDimesnionList);
			}
			if(graphInfo.isSmartenMode())//smarten mode ON ignore cols rows if more then 4.(14 NOV 2017)
				requestParamMap = ignoreDimensionAndMeasure(requestParamMap,detailedOutlinerMap,graphInfo.getGraphType(),true,originalDimensionList);
			/*if(graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR && chartWihtoutValueAxis != null && !chartWihtoutValueAxis.equalsIgnoreCase("true"))
				graphInfo.setIgnoreDimAndMeasureList(new ArrayList());*/
			
			/*
			Commenting code to give priority to Color as discussed 14 Mar 2018
			if(graphInfo.isSmartenMap() && !graphInfo.isSmartenMode() && graphInfo.getGraphData().getMeasureListForSmartenview().size() > 1)//Added for Map 4 Dec 2017
			{
				smartenService.ignoreMeasureForSmartenviewMap(graphInfo, graphInfo.getGraphData().getMeasureListForSmartenview());
			}*/
			//For setting Dimension in Legend(Color) when Pie start//show same color pie
			if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH && graphInfo.isSmartenMode() && graphInfo.getOutlinerRow().isEmpty())
			{
				Vector outlinerColVector = graphInfo.getOutlinerCol(); 
				Vector outlinerRowVector = new Vector();
				
				if(null != outlinerColVector && !outlinerColVector.isEmpty() && outlinerColVector.size() > 0) 
					outlinerRowVector.add(outlinerColVector.get(0));
				
				outlinerColVector = new Vector();
				graphInfo.setOutlinerCol(outlinerColVector);
				graphInfo.setOutlinerRow(outlinerRowVector);
			}
			if(graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR && !graphInfo.isSmartenTabular())//[as when grpahType is 70,smarten Tabular should be enabled]
				graphInfo.setSmartenTabular(true);
			
			if(graphInfo.isSmartenTabular())
			{
				if(null != graphInfo.getGraphData().getDimensionListForSmartenview() && !graphInfo.getGraphData().getDimensionListForSmartenview().isEmpty()) {
					String colItemstemp="";
					for(int i=0;i < graphInfo.getGraphData().getDimensionListForSmartenview().size();i++)
					{
						colItemstemp+=graphInfo.getGraphData().getDimensionListForSmartenview().get(i).toString()+",";
					}		
					requestParamMap.put("collabels", colItemstemp.substring(0, colItemstemp.lastIndexOf(",")));
				}
				
				if(null != graphInfo.getGraphData().getMeasureListForSmartenview() && !graphInfo.getGraphData().getMeasureListForSmartenview().isEmpty()) {
					String dataItemstemp="";
					for(int i=0;i < graphInfo.getGraphData().getMeasureListForSmartenview().size();i++)
					{
						if(!graphInfo.getIgnoreDimAndMeasureList().contains(graphInfo.getGraphData().getMeasureListForSmartenview().get(i).toString()))
							dataItemstemp += graphInfo.getGraphData().getMeasureListForSmartenview().get(i).toString()+",";
					}
					if(!"".equals(dataItemstemp))
						requestParamMap.put("datalabels", dataItemstemp.substring(0, dataItemstemp.lastIndexOf(",")));//for dataOperations on multiple measure
				}
			}
			
			colItems = StringUtil.toArray((String) requestParamMap.get("collabels"));
			if(graphType == GraphConstants.HEAT_MAP_GRAPH && !graphInfo.isSmartenMode() && colItems.length > 1 && graphInfo.getGraphData().getMeasureListForSmartenview().size() > 0)//For setting 2nd D in Row when M-off & HeatMap
			{
				for(int i=0;i<colItems.length;i++)
				{
					if(i == 0)
						requestParamMap.put("collabels", colItems[i].toString());
					else if(i == 1)
						requestParamMap.put("rowlabels", colItems[i].toString());
				}
			}
			//check if it is XY chart or not
			graphInfo.setXyChart(false);
			if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
			{
				String[] tempColItems = StringUtil.toArray((String) requestParamMap.get("collabels"));
				String[] tmpMsr = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
				if(tempColItems.length > 0 && !originalDimensionList.contains(tempColItems[0].toString()) && tmpMsr.length > 2)
				{
					graphInfo.setXyChart(true);
					if(graphInfo.isSmartenMode() && graphInfo.getSizeLabelBubble() == null)
						graphInfo.setSizeLabelBubble(tmpMsr[2].toString());
					if(!graphInfo.isPerformAggregation() && tmpMsr.length == 3)
					{
						/*if(graphInfo.isSmartenMode())
							graphInfo.setSizeLabelBubble(tmpMsr[1].toString());
						else*/
							graphInfo.setSizeLabelBubble(tmpMsr[2].toString());
					}
				}
			}
			
			//Below code to save Color/Size/Shape when Measure start
			
			/*graphInfo.setColorMeasureLabel(graphInfo.getGraphData().getColorMeasureLabel());
			graphInfo.setSizeMeasure(graphInfo.getGraphData().isSizeMeasure());
			graphInfo.setSizeLabel(graphInfo.getGraphData().getSizeLabel());
			graphInfo.setShapeMeasure(graphInfo.getGraphData().isShapeMeasure());
			graphInfo.setShapeLabel(graphInfo.getGraphData().getShapeLabel());*/
			//Below code to save Color/Size/Shape when Measure end
			
			//for rowsMeasure
			if(graphInfo.isRowsMeasure() && graphInfo.isPerformAggregation() && graphInfo.getGraphType() != GraphConstants.SMARTENVIEW_TABULAR)
			{
				String rowsMsr = "";
				for(int i=0;i<graphInfo.getRowsMeasureList().size();i++)
					rowsMsr = rowsMsr + graphInfo.getRowsMeasureList().get(i).toString() + ",";
				String orderedDataTemp = requestParamMap.get("orderedDataLabels") +"," + rowsMsr;
				String dataLabelsTemp = requestParamMap.get("datalabels")+","+rowsMsr;
				
				if (orderedDataTemp.endsWith(",")) 
					orderedDataTemp = orderedDataTemp.substring(0, orderedDataTemp.length() - 1);
				
				if (dataLabelsTemp.endsWith(",")) 
					dataLabelsTemp = dataLabelsTemp.substring(0, dataLabelsTemp.length() - 1);
				
				requestParamMap.put("orderedDataLabels", orderedDataTemp);
				requestParamMap.put("datalabels", dataLabelsTemp);
				graphInfo.getGraphData().getMeasureListForSmartenview().addAll(graphInfo.getRowsMeasureList());
			}
			//for rowsMeasure
			
			//for colsMeasure
			if(graphInfo.isColsMeasure() && graphInfo.isPerformAggregation() && graphInfo.getGraphType() != GraphConstants.SMARTENVIEW_TABULAR)
			{
				String colsMsr = "";
				for(int i=0;i<graphInfo.getColsMeasureList().size();i++)
					colsMsr = colsMsr + graphInfo.getColsMeasureList().get(i).toString() + ",";
				String orderedDataTemp = requestParamMap.get("orderedDataLabels") +"," + colsMsr;
				String dataLabelsTemp = requestParamMap.get("datalabels")+","+colsMsr;

				if (orderedDataTemp.endsWith(",")) 
					orderedDataTemp = orderedDataTemp.substring(0, orderedDataTemp.length() - 1);

				if (dataLabelsTemp.endsWith(",")) 
					dataLabelsTemp = dataLabelsTemp.substring(0, dataLabelsTemp.length() - 1);

				requestParamMap.put("orderedDataLabels", orderedDataTemp);
				requestParamMap.put("datalabels", dataLabelsTemp);
				graphInfo.getGraphData().getMeasureListForSmartenview().addAll(graphInfo.getColsMeasureList());
			}
			
			if(graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR && (graphInfo.isRowsMeasure() || graphInfo.isColsMeasure()))
			{
				List ignoreList = graphInfo.getIgnoreDimAndMeasureList();
				ignoreList.addAll(graphInfo.getRowsMeasureList());
				ignoreList.addAll(graphInfo.getColsMeasureList());
				graphInfo.setIgnoreDimAndMeasureList(ignoreList);
				graphInfo.setRowsMeasure(false);
				graphInfo.setColsMeasure(false);
				graphInfo.setRowsMeasureList(new ArrayList());
				graphInfo.setColsMeasureList(new ArrayList());
			}
			//for colsMeasure
			
			//for category measure
			if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
				graphInfo.setCategoryMeasure(false);
			if(graphInfo.isCategoryMeasure())
			{
				/*String orderedDataTemp = requestParamMap.get("orderedDataLabels");
				String dataLabelsTemp = requestParamMap.get("datalabels");
				String[] dataLabelsArr = StringUtil.toArray((String) requestParamMap.get("datalabels"));
				String[] orderData = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
				if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH && dataLabelsArr.length == 1)
				{
					if(!graphInfo.isSmartenMode())
					{
						if(orderData.length == 1)
						{
						dataLabelsTemp = requestParamMap.get("collabels") + "," +orderedDataTemp;
						orderedDataTemp = requestParamMap.get("collabels") + "," +orderedDataTemp;
						}
						else
						{
							orderedDataTemp = orderedDataTemp;
							dataLabelsTemp = orderedDataTemp;
						}
					}
					else
					{
					if(graphInfo.isPerformAggregation())
						dataLabelsTemp = dataLabelsTemp+","+graphInfo.getCategoryMeasureLabel();
					else
						dataLabelsTemp = requestParamMap.get("datalabels") + "," +requestParamMap.get("datalabels2");//requestParamMap.put("datalabels2","")
					
					orderedDataTemp = requestParamMap.get("datalabels") + "," +requestParamMap.get("datalabels2");
					}
				}
				else
				{
					if(dataLabelsArr.length == 1)
					{
						orderedDataTemp = requestParamMap.get("orderedDataLabels") +"," + graphInfo.getCategoryMeasureLabel();
						if(graphInfo.isPerformAggregation())
							dataLabelsTemp = dataLabelsTemp+","+graphInfo.getCategoryMeasureLabel();
						else
							dataLabelsTemp = graphInfo.getCategoryMeasureLabel()+","+dataLabelsTemp;//dataLabelsTemp = dataLabelsTemp+","+graphInfo.getCategoryMeasureLabel();//--aggregation
					}
				}
				if (orderedDataTemp.endsWith(",")) 
					orderedDataTemp = orderedDataTemp.substring(0, orderedDataTemp.length() - 1);

				if (dataLabelsTemp.endsWith(",")) 
					dataLabelsTemp = dataLabelsTemp.substring(0, dataLabelsTemp.length() - 1);
				requestParamMap.put("datalabels2","");
				requestParamMap.put("orderedDataLabels", dataLabelsTemp);
				requestParamMap.put("datalabels", dataLabelsTemp);
				graphInfo.getGraphData().getMeasureListForSmartenview().add(graphInfo.getCategoryMeasureLabel());*/
			}
			//for category measure
			
			
			//check if it is XY chart or not
			if(!graphInfo.isSmartenMode()){
				String rowsDimensionName = graphInfo.getOutlinerRows().toString();
				String coloumnsDimensionValue = graphInfo.getOutlinerCols().toString();
				//Remove trailing comma
				if (rowsDimensionName.endsWith(",")) {
					rowsDimensionName = rowsDimensionName.substring(0, rowsDimensionName.length() - 1);
				}
				if (coloumnsDimensionValue.endsWith(",")) {
					coloumnsDimensionValue = coloumnsDimensionValue.substring(0, coloumnsDimensionValue.length() - 1);
				}
				
				colItems = (String[]) graphInfo.getOutlinerCol().toArray(new String[graphInfo.getOutlinerCol().size()]);
				rowItems = (String[]) graphInfo.getOutlinerRow().toArray(new String[graphInfo.getOutlinerRow().size()]);
				
			   String[]  rowItemsForLine = StringUtil.toArray((String) requestParamMap.get("rowlabels2"));
			    orderedData = (String[]) graphInfo.getOutlinerDataColumns().toArray(new String[graphInfo.getOutlinerDataColumns().size()]);
			    String [] dataItems = (String[]) graphInfo.getOutlinerDataColumns().toArray(new String[graphInfo.getOutlinerDataColumns().size()]);
			    String[] sizes =(String[]) graphInfo.getOutlinerSizesColumns().toArray(new String[graphInfo.getOutlinerSizesColumns().size()]); 
			    String[] shape = (String[]) graphInfo.getOutlinerShapesColumns().toArray(new String[graphInfo.getOutlinerShapesColumns().size()]);  
			    String[] rows = StringUtil.toArray(rowsDimensionName);
			    String[] coloumns = StringUtil.toArray(coloumnsDimensionValue);
			    String[] sDataAndUserDataLine = StringUtil.toArray((String) requestParamMap.get("dataandUserLabelsLine"));
			    String[] dataItemsForLine = StringUtil.toArray((String) requestParamMap.get("datalabels2"));
			    Map<String, Integer> dimensionValueCountMap = graphInfo.getDimensionValueCountMap();
				int gType = Integer.parseInt(sngraphtype);
				String colValue  = requestParamMap.get("collabels");
				String rowValue =  requestParamMap.get("rowlabels");
				String rowsValue = requestParamMap.get("rows");
				int colDimensionSize =0;
				int rowDimensionSize = 0;
				if(dimensionValueCountMap.get(colValue)!=null)
					colDimensionSize= dimensionValueCountMap.get(colValue).intValue();
				if(dimensionValueCountMap.get(rowValue)!=null)
					rowDimensionSize= dimensionValueCountMap.get(rowValue).intValue();
				
				int rowsDimensionSize = 0;
				boolean swapRowAndRows=false;
				int noOfMeasures = 0;
				int noOfDimension = 0;
				if(!graphInfo.getGraphData().getDimensionListForSmartenview().isEmpty())
					noOfDimension = graphInfo.getGraphData().getDimensionListForSmartenview().size();
				if(!graphInfo.getGraphData().getMeasureListForSmartenview().isEmpty())
					noOfMeasures = graphInfo.getGraphData().getMeasureListForSmartenview().size();
				
				/*if((noOfDimension == 0 && noOfMeasures>0)
						||(noOfDimension > 0 && noOfMeasures == 0))//only measure in detailed outliner
				{
					isSmartenGraph = false;
				}*/
				boolean isParentChildHeirarchy = false;
				newDimensionList = smartenService.checkForParentChildHeirarchy(dimensionValueCountMap, graphInfo, userInfo, new ArrayList());
				if(newDimensionList.size() > 0)
				{
					isParentChildHeirarchy = true;
				}
				if(dimensionValueCountMap.get(rowValue) != null)
					rowDimensionSize = dimensionValueCountMap.get(rowValue).intValue();
				if(dimensionValueCountMap.get(rowsValue) != null)
					rowsDimensionSize = dimensionValueCountMap.get(rowsValue).intValue();
				if(colDimensionSize >= 6 && colDimensionSize < 20 && rowDimensionSize < 6 && noOfDimension == 2)
					swapRowAndRows = true;
				smartenService.setSmartenRecommendedGraphtype(graphInfo, requestParamMap, userId, rowItems, colItems, rows, coloumns, sizes, shape, orderedData,rowItemsForLine,sDataAndUserDataLine,dataItems,dataItemsForLine,dimensionValueCountMap,noOfMeasures,isParentChildHeirarchy,"",swapRowAndRows);	
			}
			if(firstTimeSmarten) {
				graphInfo.setSmartenTabular(false );
				graphType = graphInfo.getRecommendGraphType();
				graphInfo.getGraphData().setSmartenType(graphInfo.getRecommendGraphType());
				graphInfo.setGraphType(graphInfo.getRecommendGraphType());
			}
			
			//----------------Aggregation----------------------//
			if(!graphInfo.isPerformAggregation() && !isSmartenModeOn)
			{
				smartenService.reArrangeMeasure(graphInfo,requestParamMap);
				/*
				String[] dataItems = StringUtil.toArray((String) requestParamMap.get("datalabels"));
				String[] dataItemsForLine = StringUtil.toArray((String) requestParamMap.get("datalabels2"));
				String orderedDataLabels = requestParamMap.get("orderedDataLabels");
				
				if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH)
				{
					requestParamMap.put("rowlabels", "");
					if(dataItems.length == 2)
					{
						requestParamMap.put("datalabels", dataItems[0]+","+dataItems[1]);
						requestParamMap.put("datalabels2", "");
						//requestParamMap.put("dataandUserLabelsBar", dataItems[0]+","+dataItems[0]);
						
					}
					else if(dataItems.length == 3) 
					{
						requestParamMap.put("collabels", dataItems[0]);
						requestParamMap.put("datalabels", dataItems[0]+","+dataItems[1]);
						requestParamMap.put("datalabels2", dataItems[2]);
					}
				}
				List ignoreDim =  graphInfo.getIgnoreDimAndMeasureList();
				if(graphInfo.isRowsMeasure())
				{
					graphInfo.setRowsMeasure(false);
					ignoreDim.addAll(graphInfo.getRowsMeasureList());
				}
				if(graphInfo.isColsMeasure())
				{
					graphInfo.setColsMeasure(false);
					ignoreDim.addAll(graphInfo.getColsMeasureList());
				}
				graphInfo.setIgnoreDimAndMeasureList(ignoreDim);
			*/
				
			}
			//----------------Aggregation----------------------//
			
			//16 sept 2019
			if(requestParamMap.get("ignoreString") != null && !requestParamMap.get("ignoreString").equals("") && !isSmartenModeOn)
			{	
				List ignoreDimMeasureList = graphInfo.getIgnoreDimAndMeasureList();//server 
				ignoreDimMeasureList.addAll(frontEndIgnoreList);
				
				//ignoreParamsFromFrontEnd(requestParamMap,frontEndIgnoreList);//removes ignore from the map before pagerow function
				
				graphInfo.setIgnoreDimAndMeasureList(new ArrayList(new HashSet(ignoreDimMeasureList)));
			}
			String[] dataItemstemp = StringUtil.toArray((String) requestParamMap.get("datalabels"));
			if(requestParamMap.get("ignoreString") != null && requestParamMap.get("ignoreString").equals("") && dataItemstemp.length>0)			{
				
				if(graphInfo.getIgnoreDimAndMeasureList().contains(dataItemstemp[0]) && graphInfo.getGraphType()==GraphConstants.D3_CHORD) {
					graphInfo.getIgnoreDimAndMeasureList().remove(dataItemstemp[0]);
				}
			}
			//16 sept 2019
			smartenService.setPageRowColDataItems(requestParamMap, graphInfo, userInfo,graphInfo.getGraphData().getDimensionListForSmartenview(),graphInfo.getGraphData().getMeasureListForSmartenview());
			
			smartenService.doGraphChanged(graphInfo.getGraphType(), false, graphInfo, userInfo);
			if (graphInfo.getGraphMode() == AppConstants.NEW_MODE) {
				smartenService.setDefaultSorting(graphInfo,userInfo);
				graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setDateFormat(userInfo.getDateFormat());
				smartenService.setDefaultTimeFormatForProperties(graphInfo,userInfo);
			}
			if(requestParamMap.get("outlinerMode").toString().equals("1")) {
			if (smartenService.getObjectMode() == AppConstants.NEW_MODE) {
				GeneralConfigurationInfo generalConfigurationInfo = generalConfigurationServiceUtil.getGeneralConfigurationInfo();
				String mapValue = generalConfigurationInfo.getDefaultGraphProperties();
				if(mapValue != null && !mapValue.equalsIgnoreCase("-1")) {
					smartenService.copyTheme(mapValue,graphInfo);
				}
			}
			}
			long setOutlinerEnd = System.currentTimeMillis();
			//ApplicationLog.debug("setOutliner ENDS time take :->"+(setOutlinerEnd - setOutlinerStart));
			
		} catch (MDXException | RealTimeCubeException e) {
			String objectMode = (smartenService.getObjectMode() == AppConstants.NEW_MODE ? "New" : "Open");
			
			String outlinerMode = request.getParameter("outlinerMode");
			if(outlinerMode != null && outlinerMode.equals("1")){
				ApplicationLog.error(ResourceManager.getString(
						"LOG_ERROR_MSG_FAILED_TO_SET_OUTLINER", new Object[] {
								objectTypeName, objectMode, getObjectDisplayName(),
								userInfo.getUsername() }), e);
				return ResourceManager.getString("ERROR_MSG_FAILED_TO_SET_OUTLINER") + " " + e.getMessage();
			} else {
				
				ApplicationLog.error(ResourceManager.getString(
						"LOG_ERROR_MSG_FAILED_TO_SET_OUTLINER", new Object[] {
								objectTypeName, objectMode, getObjectDisplayName(),
								userInfo.getUsername() }), e);
				return ResourceManager.getString("ERROR_MSG_FAILED_TO_SET_OUTLINER_EDIT_MODE") + " " + e.getMessage();
			}
		}catch (ALSException e) {
			return e.getLocalizedMessage();
		} catch (Exception e) {
			String objectMode = (smartenService.getObjectMode() == AppConstants.NEW_MODE ? "New" : "Open");
			
			String outlinerMode = request.getParameter("outlinerMode");
			if(outlinerMode != null && outlinerMode.equals("1")){
				ApplicationLog.error(ResourceManager.getString(
						"LOG_ERROR_MSG_FAILED_TO_SET_OUTLINER", new Object[] {
								objectTypeName, objectMode, getObjectDisplayName(),
								userInfo.getUsername() }), e);
				return ResourceManager.getString("ERROR_MSG_FAILED_TO_SET_OUTLINER");
			} else {
				
				ApplicationLog.error(ResourceManager.getString(
						"LOG_ERROR_MSG_FAILED_TO_SET_OUTLINER", new Object[] {
								objectTypeName, objectMode, getObjectDisplayName(),
								userInfo.getUsername() }), e);
				return ResourceManager.getString("ERROR_MSG_FAILED_TO_SET_OUTLINER_EDIT_MODE");
			}
		}

		try {
			GraphProperties graphProperties = graphInfo.getGraphProperties();

			if (graphProperties.getTitleProperties().isTitleVisible()) {
				HashtableEx ddvmList = smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());

				smartenService.setObjectPageTitle(graphInfo.getGraphId(),graphInfo
						.getActiveFilterInfo(userInfo.getUserId()),
						smartenService.getPageFilterNew(graphInfo),
						smartenService.getActiveVariableMap(), smartenService.getResultSetMetaData(),
						(IDataObject)graphInfo.getCubeInfo(), graphProperties.getTitleProperties(),
						userInfo, ddvmList);
			}
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_SET_VARIABLE_IN_TITLE",
					new Object[] {
							objectTypeName,
							graphInfo.getGraphName(),
									userInfo.getUsername() }), e);

			return ResourceManager.getString("ERROR_MSG_FAILED_TO_SET_VARIABLE_IN_GRAPH_TITLE",new Object[] {e.getMessage()});
		}
		map.put("setOutlinerStart", setOutlinerStart); 
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_GRAPH_OUTLINER"), AppConstants.DETAIL,userInfo);
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		try {
			recentlyUsedServiceUtil.saveRecentlyUsed(graphInfo.getCubeInfo().getId(),graphInfo.getCubeInfo().getDataObjecName(), userInfo, AppConstants.DATASETS,1);
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		}
		changeAppliedFiltersFlags(request, userInfo, response, map);
		map.put("requireRefresh", true);
		/*
		 * graphInfo.getGraphProperties().setColLabelsMap(new HashMap());
		 * graphInfo.getGraphProperties().setColLabelsDisplayMap(new HashMap());
		 */
		return refreshObjectData(null,response, userInfo, map);	
	}
	
	
	private void ignoreParamsFromFrontEnd(HashMap<String, String> requestParamMap, List frontEndIgnoreList) {
		
		String[] colsArr = StringUtil.toArray((String) requestParamMap.get("coloumns"));
		String[] rowsArr = StringUtil.toArray((String) requestParamMap.get("rows"));
		String[] rowArr = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
		String[] sizeArr = StringUtil.toArray((String) requestParamMap.get("sizes"));
		String[] shapeArr = StringUtil.toArray((String) requestParamMap.get("shapes"));
		String[] colItems = StringUtil.toArray((String) requestParamMap.get("collabels"));
		String temp = "";
		
		//category
		List tempList = new ArrayList();
		tempList.addAll(Arrays.asList(colItems));
		tempList.removeAll(frontEndIgnoreList);
		StringUtils.join(tempList);
		requestParamMap.put("collabels", StringUtils.join(tempList, ','));
		
		//cols
		tempList.clear();
		tempList.addAll(Arrays.asList(colsArr));
		tempList.removeAll(frontEndIgnoreList);
		StringUtils.join(tempList);
		requestParamMap.put("coloumns", StringUtils.join(tempList, ','));
		
		//rows
		tempList.clear();
		tempList.addAll(Arrays.asList(rowsArr));
		tempList.removeAll(frontEndIgnoreList);
		StringUtils.join(tempList);
		requestParamMap.put("rows", StringUtils.join(tempList, ','));
		
		//color
		tempList.clear();
		tempList.addAll(Arrays.asList(rowArr));
		tempList.removeAll(frontEndIgnoreList);
		StringUtils.join(tempList);
		requestParamMap.put("rowlabels", StringUtils.join(tempList, ','));
		
		//size
		tempList.clear();		
		tempList.addAll(Arrays.asList(sizeArr));
		if(sizeArr.length > 0 && frontEndIgnoreList!=null && frontEndIgnoreList.size() > 0 && frontEndIgnoreList.contains(sizeArr[0]))
		{
			graphInfo.setSizeColumns(new Vector());
			graphInfo.setOutlinerSizesColumns(new Vector());
			graphInfo.getGraphData().setSizeList(new ArrayList());
			graphInfo.getGraphData().setSizeMeasure(false);
			graphInfo.getGraphData().setSizeLabel("");
		}
		tempList.removeAll(frontEndIgnoreList);
		StringUtils.join(tempList);
		requestParamMap.put("sizes", StringUtils.join(tempList, ','));
		
		//shape
		tempList.clear();
		tempList.addAll(Arrays.asList(shapeArr));
		if(shapeArr.length > 0 && frontEndIgnoreList!=null && frontEndIgnoreList.size() > 0 && frontEndIgnoreList.contains(shapeArr[0]))
		{
			graphInfo.setShapeColumns(new Vector());
			graphInfo.setOutlinerShapesColumns(new Vector());
			graphInfo.getGraphData().setShapeList(new ArrayList());
			graphInfo.getGraphData().setShapeMeasure(false);
			graphInfo.getGraphData().setShapeLabel("");
		}
		
		tempList.removeAll(frontEndIgnoreList);
		StringUtils.join(tempList);
		requestParamMap.put("shapes", StringUtils.join(tempList, ','));
		
	}

	private HashMap<String, String> ignoreDimensionAndMeasure(HashMap<String, String> requestParamMap, Map<String, Integer> detailedOutlinerMap,int graphType, boolean smartenMode, List originalDimensionList) 
	{
		
		HashMap<String, String> map = new HashMap<String, String>(requestParamMap);
		String[] colsArr = StringUtil.toArray((String) requestParamMap.get("coloumns"));
		String[] rowsArr = StringUtil.toArray((String) requestParamMap.get("rows"));
		String[] measureArr = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
		String[] dataArr = StringUtil.toArray((String) requestParamMap.get("datalabels"));
		String[] rowArr = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
		String[] sizeArr = StringUtil.toArray((String) requestParamMap.get("sizes"));
		String[] shapeArr = StringUtil.toArray((String) requestParamMap.get("shapes"));
		String[] rowlabels2 = StringUtil.toArray((String) requestParamMap.get("rowlabels2"));
		String[] colItems = StringUtil.toArray((String) requestParamMap.get("collabels"));
		String isGraphTypeChange = requestParamMap.get("isFromGraphTypeChange");
		List ignoredDimensionMeasureList = graphInfo.getIgnoreDimAndMeasureList();
		
		//int graphType = Integer.parseInt(map.get("sngraphtype").toString());
		int colsSize = colsArr.length;
		int rowsSize = rowsArr.length;
		int noOfMeasure = measureArr.length;
		int rowSize = rowArr.length;
		String finalRows = "";
		String finalCols = "";
		boolean isD3Graph =false;
		boolean isTabular = false;
		boolean isMap = false;//Added for Issues Point 10 Task #13184
		boolean chartWihtoutValueAxis = false;
		boolean colHasMeasureTabular = false;//XY to tabular
		boolean isPieWithRowDimension = false;//14162[ignore column when row]
		
		//172 starts
		/* Commented as because of this code Stacked charts were not plotting (And SDEVAPR20-172 was also not Resolved)
		if (graphType == GraphConstants.PIE_GRAPH
				|| graphType == GraphConstants.STACKED_VBAR_GRAPH
				|| graphType == GraphConstants.PERCENTAGE_VBAR_GRAPH
				|| graphType == GraphConstants.STACKED_HBAR_GRAPH
				|| graphType == GraphConstants.PERCENTAGE_HBAR_GRAPH
				|| graphType == GraphConstants.STACKED_LINE_GRAPH
				|| graphType == GraphConstants.PERCENTAGE_LINE_GRAPH
				|| graphType == GraphConstants.AREA_DEPTH_GRAPH
				|| graphType == GraphConstants.AREA_STACK_GRAPH
				|| graphType == GraphConstants.AREA_PERCENTAGE_GRAPH
				|| graphType == GraphConstants.DRILLED_RADAR_GRAPH
				|| graphType == GraphConstants.DRILLED_STACKED_RADAR_GRAPH
				|| graphType == GraphConstants.HISTOGRAM_GRAPH)
		{
			GraphData g1= graphInfo.getGraphData();
				if( rowArr.length > 0)
				{
					ignoredDimensionMeasureList.add(rowArr[0]);
					map.put("rowlabels","");
					rowArr=StringUtil.toArray((String) map.get("rowlabels"));
					rowSize = rowArr.length;
					g1.setRowMeasure(false);
					g1.setColorMeasureLabel("");
					//String[] sizeArr = StringUtil.toArray((String) requestParamMap.get("sizes"));
					///String[] shapeArr = StringUtil.toArray((String) requestParamMap.get("shapes"));
					
				}			
				graphInfo.setGraphData(g1);
				
		}*/	
		//172 ends
		
		
		
		if(graphType == GraphConstants.D3_CHORD
				|| graphType == GraphConstants.D3_TREEMAP
				|| graphType == GraphConstants.D3_SUNBURST
				|| graphType == GraphConstants.D3_BUBBLE
				|| graphType == GraphConstants.D3_TREELAYOUT)
		{	
			isD3Graph = true;
		}
		if(graphType == GraphConstants.SMARTENVIEW_TABULAR)
			isTabular = true;
		if(graphType == GraphConstants.SMARTENVIEW_MAP)
			isMap = true;
		if(graphType == GraphConstants.PIE_GRAPH && rowArr.length > 0 && originalDimensionList.contains(rowArr[0]))
			isPieWithRowDimension = true;
		
		//check if geoCol
		List dimList = new ArrayList();
		if(graphInfo.getGraphMode() == AppConstants.NEW_MODE)//When new
			dimList = graphInfo.getGraphData().getDimensionListForSmartenview();//graphInfo.getDimensionTitleList();
		else
			dimList = graphInfo.getDimensionTitleList();//When saved
		
		String dimensionName="";
		List geoColumnInfoList = graphInfo.getGraphData().getGeoColumnInfoListForSmartenview();
		boolean allDimGeoColoumn = false;
		boolean histogram_or_candle = (graphType == GraphConstants.HISTOGRAM_GRAPH
										|| graphType == GraphConstants.CANDLE_STICK_GRAPH
										|| graphType == GraphConstants.HIGH_LOW_OPEN_CLOSE_GRAPH
										|| graphType == GraphConstants.NUMERIC_DIAL_GAUGE);
		int count=0;
		for(int j=0;j<dimList.size();j++)
		{
			dimensionName = dimList.get(j).toString();
			for(int i = 0; i < geoColumnInfoList.size(); i++)
			{
				GeographicalColumnInfo geographicalColumnInfo = (GeographicalColumnInfo) geoColumnInfoList.get(i);
				if(geographicalColumnInfo.getGeographicRole().contains(dimensionName))
					count++;
			}
		}
		if(count == dimList.size())
			allDimGeoColoumn = true;
		//check if geoCall
		
		boolean legendContainsMeasure = false;
		boolean aggregatedData = graphInfo.isPerformAggregation();
		boolean rowMeasure = false;
		if(null != originalDimensionList && !originalDimensionList.isEmpty()){
			for(int j=0;j<rowArr.length;j++) {
				if(!originalDimensionList.contains(rowArr[j].toString()))
					rowMeasure = true;
			}
			for(int k=0;k<sizeArr.length;k++) {
				if(!originalDimensionList.contains(sizeArr[k].toString()))
					legendContainsMeasure = true;
			}
			for(int l=0;l<shapeArr.length;l++) {
				if(!originalDimensionList.contains(shapeArr[l].toString()))
					legendContainsMeasure = true;
			}
		}
		
		if(!smartenMode && measureArr.length == 0 && !legendContainsMeasure)//mode off
			chartWihtoutValueAxis = true;//plot tabular
		
		map.put("chartWihtoutValueAxisType", ""+70);//71
		if(chartWihtoutValueAxis && !allDimGeoColoumn)//plot tabular remove size shape
		{
			for(int i=0;i<shapeArr.length;i++)
				ignoredDimensionMeasureList.add(shapeArr[i].toString());
			for(int i=0;i<sizeArr.length;i++)
				ignoredDimensionMeasureList.add(sizeArr[i].toString());
			
			map.put("sizes","");
			map.put("shapes","");
			sizeArr = StringUtil.toArray((String) requestParamMap.get("sizes"));
			shapeArr = StringUtil.toArray((String) requestParamMap.get("shapes"));
			map.put("chartWihtoutValueAxisType", ""+70);
		}
		map.put("chartWihtoutValueAxis", ""+chartWihtoutValueAxis);
		
		//XY to tabular
		if(isMap || isTabular)//Added Map check to ignore Rows/Columns[removed tabular condition on 5 Jun 2018]
		{
			for(int i=0;i<colItems.length;i++)
			{
				if(!originalDimensionList.contains(colItems[i].toString()))
				{
					if(!isTabular)//As dicussed allow eveything in category and valueAxis,XY to tabular 5 Jun 2018
						ignoredDimensionMeasureList.add(colItems[i].toString());
					colHasMeasureTabular = true;
				}
			}
		}
		
		
		
		//max of 2 rows and 2 cols possible OR total should be less then 4.
		if(colsSize == 0 && rowsSize >= 4)
			rowsSize = 4;
		else if(rowsSize == 0 && colsSize >= 4)
			colsSize = 4;
		else if(colsSize > 0 && rowsSize > 0 && colsSize<=4 && rowsSize <=4)
		{
			if(rowsSize == 1 && (colsSize+rowsSize) > 4)
				colsSize = 3;
			if(rowsSize == 2 && (colsSize+rowsSize) > 4)
				colsSize = 2;
			if(rowsSize == 3 && (colsSize+rowsSize) > 4)
			{
				if(colsSize >= 2)
				{
					rowsSize = 2;
					colsSize = 2;
				}
			}
			if(rowsSize == 4 && (colsSize+rowsSize) > 4)
			{
				if(colsSize >= 2)
				{
					rowsSize = 2;
					colsSize = 2;
				}
				if(colsSize == 1)
					rowsSize = 3;
			}
		}
		else if(colsSize > 4 && rowsSize > 4)
		{
			rowsSize = 2;
			colsSize = 2;
		}
		else if(colsSize > 0 && rowsSize > 4)
		{
			
			if(colsSize ==1)
			{
			colsSize = 1;
			rowsSize =3;
			}
			if(colsSize >=2)
			{
			colsSize = 2;
			rowsSize =2;
			}
		}else if(rowsSize > 0 && colsSize  > 4)
		{
			
			if(rowsSize ==1)
			{
				rowsSize = 1;
				colsSize =3;
			}
			if(rowsSize >=2)
			{
				rowsSize = 2;
				colsSize =2;
			}
		}
			
		//14 Nov 2017 ignore larger rows cols.
		Map<String, Integer> colsMap = new HashMap();
		Map<String, Integer> rowsMap = new HashMap();
		String colsName = "";
		String rowsName = "";
		
		if(colsArr.length > 0)
		{
			for(int i=0;i<colsArr.length;i++)
			{
				colsName = colsArr[i].toString();
				if(detailedOutlinerMap.get(colsName) != null)
					colsMap.put(colsName, detailedOutlinerMap.get(colsName));
			}
			if(graphInfo.isSmartenMode())
				colsMap = smartenService.sortByComparator(colsMap, true);
		}
		
		if(rowsArr.length > 0)
		{
			for(int i=0;i<rowsArr.length;i++)
			{
				rowsName = rowsArr[i].toString();
				if(detailedOutlinerMap.get(rowsName) != null)
					rowsMap.put(rowsName, detailedOutlinerMap.get(rowsName));
			}
			if(graphInfo.isSmartenMode())
				rowsMap = smartenService.sortByComparator(rowsMap, true);
		}
		//14 Nov 2017
		//ignore rows and cols when mode oFF and d3
		if(((isD3Graph || isTabular || isMap) && !graphInfo.isSmartenMode()) || (!aggregatedData && graphType != GraphConstants.BUBBLE_GRAPH && !graphInfo.isSmartenMode()))
		{
			colsSize = 0;
			rowsSize = 0;
		}
		
		//max of 2 rows and 2 cols possible OR total should be less then 4.
		if((!graphInfo.isRowsMeasure() && !graphInfo.isColsMeasure()) || (isMap || isTabular))//14672[issue 2]
		{
			for(int i=0;i<colsArr.length;i++)
			{
				if(i<colsSize)
					finalCols += colsArr[i].toString()+",";
				else
					ignoredDimensionMeasureList.add(colsArr[i].toString());
			}
			for(int i=0;i<rowsArr.length;i++)
			{
				if(i<rowsSize)
					finalRows += rowsArr[i].toString()+",";
				else
					ignoredDimensionMeasureList.add(rowsArr[i].toString());
			}
		}
		else
		{
			List allRows = new ArrayList();//graphInfo.getRowsDimMeasureList();
			List rowsMeasure = new ArrayList();
			List allCols = new ArrayList();//graphInfo.getColsDimMeasureList();
			List colsMeasure = new ArrayList();
			
			allRows.addAll(graphInfo.getRowsDimMeasureList());
			allCols.addAll(graphInfo.getColsDimMeasureList());
			rowsMeasure.addAll(graphInfo.getRowsMeasureList());
			colsMeasure.addAll(graphInfo.getColsMeasureList());
			
			int rowsCount = 3;
			int colsCount = 3;
			if(graphInfo.isRowsMeasure() && graphInfo.isColsMeasure())
			{
				rowsCount = 2;
				colsCount = 2;
			}
		
			for(int i=0;i<allCols.size();i++)
			{
				if(i<colsCount)
				{
					if(originalDimensionList.contains(allCols.get(i).toString()))
						finalCols += allCols.get(i).toString()+",";
				}
				else
					ignoredDimensionMeasureList.add(allCols.get(i).toString());
			}
			for(int i=0;i<allRows.size();i++)
			{
				if(i<rowsCount)
				{
					if(originalDimensionList.contains(allRows.get(i).toString()))
						finalRows += allRows.get(i).toString()+",";
				}
				else
					ignoredDimensionMeasureList.add(allRows.get(i).toString());
			}
			
			allRows.removeAll(ignoredDimensionMeasureList);
			rowsMeasure.removeAll(ignoredDimensionMeasureList);
			allCols.removeAll(ignoredDimensionMeasureList);
			colsMeasure.removeAll(ignoredDimensionMeasureList);
			
			graphInfo.setRowsMeasureList(rowsMeasure);
			graphInfo.setColsMeasureList(colsMeasure);
			graphInfo.setRowsDimMeasureList(allRows);
			graphInfo.setColsDimMeasureList(allCols);
			
			
		}
		
		if(graphInfo.isSmartenMode())
		{
			int noOfCols = 0;
			int noOfRows = 0;
			finalCols = "";
			finalRows = "";
			Iterator iterator = (Iterator) colsMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry pair = (Map.Entry)iterator.next();
				if(noOfCols < colsSize)
					finalCols += pair.getKey().toString()+",";
				else
					ignoredDimensionMeasureList.add(pair.getKey().toString());

				noOfCols++;
			}

			iterator = (Iterator) rowsMap.entrySet().iterator();
			int i=0;
			while (iterator.hasNext()) {
				Map.Entry pair = (Map.Entry)iterator.next();
				if(noOfRows < rowsSize)
					finalRows += rowsArr[i].toString()+",";
				else
					ignoredDimensionMeasureList.add(pair.getKey().toString());
				i++;
				noOfRows++;
			}
		}
		
		
		for(int i=0;i<sizeArr.length;i++)
		{
			if((chartWihtoutValueAxis && !map.get("chartWihtoutValueAxisType").equals("71")) ||(isD3Graph && !graphInfo.isSmartenMode())  || (isTabular && !smartenMode && measureArr.length == 0) || colHasMeasureTabular || (!aggregatedData && graphType != GraphConstants.BUBBLE_GRAPH && !graphInfo.isSmartenMode())
					 || (isTabular && !smartenMode && !graphInfo.getMeasureTitleList().contains(sizeArr[i].toString())) )//allow measure only,In tabular allow measure when 1M in value
			{
				ignoredDimensionMeasureList.add(sizeArr[i].toString());
				map.put("sizes","");
			}
			else
			{
				if(i == 0)
					map.put("sizes",sizeArr[i].toString());
				else
					ignoredDimensionMeasureList.add(sizeArr[i].toString());
			}
		}
		for(int i=0;i<shapeArr.length;i++)
		{
			if((chartWihtoutValueAxis && !map.get("chartWihtoutValueAxisType").equals("71")) || isTabular || (isMap && originalDimensionList.contains(shapeArr[i].toString())) ||(isD3Graph && !graphInfo.isSmartenMode()) || colHasMeasureTabular || (!aggregatedData && graphType != GraphConstants.BUBBLE_GRAPH && !graphInfo.isSmartenMode()))
			{
				ignoredDimensionMeasureList.add(shapeArr[i].toString());
				map.put("shapes","");
			}
			else
			{
				if(i == 0)
					map.put("shapes",shapeArr[i].toString());
				else
					ignoredDimensionMeasureList.add(shapeArr[i].toString());
			}
		}
		for(int i=0;i<rowArr.length;i++)
		{
			/*if(chartWihtoutValueAxis && !allDimGeoColoumn)
			{
				ignoredDimensionMeasureList.add(rowArr[i].toString());
				map.put("rowlabels","");
			}
			else
			{*/
				if((graphType != GraphConstants.COMBINED_GRAPH && !isTabular && !isD3Graph && aggregatedData) || (isD3Graph && graphInfo.isSmartenMode() && aggregatedData) || (!aggregatedData && graphType == GraphConstants.BUBBLE_GRAPH && !graphInfo.isSmartenMode()))
				{
					if(i == 0)
						map.put("rowlabels",rowArr[i].toString());
					else
						ignoredDimensionMeasureList.add(rowArr[i].toString());
				}
				else
				{
					if((originalDimensionList.contains(rowArr[i].toString()) && !isD3Graph && !isTabular) || (isTabular && !smartenMode && measureArr.length == 0)
						 || (isTabular && !smartenMode && !graphInfo.getMeasureTitleList().contains(rowArr[i].toString())) )//not measure in color,tab= allow measure only,In tabular allow measure when 1M in value (!aggregatedData && graphType != GraphConstants.BUBBLE_GRAPH && !graphInfo.isSmartenMode())
					{
						ignoredDimensionMeasureList.add(rowArr[i].toString());
						map.put("rowlabels","");
					}
				}
			//}
		}
		if(isMap)//Ignoring More than 3 Measures when Map (Introduced to handle duplicate measure issue when graphs other map)
		{
			if(graphInfo.isSmartenMode())
			{
				if(null != graphInfo.getMeasureTitleList() && graphInfo.getMeasureTitleList().size() > 3) {
					ignoredDimensionMeasureList.addAll(graphInfo.getMeasureTitleList().subList(3, graphInfo.getMeasureTitleList().size()));
					graphInfo.getMeasureTitleList().removeAll(ignoredDimensionMeasureList);
				}
			} else {
				if(null != graphInfo.getOutlinerDataColumns() && graphInfo.getOutlinerDataColumns().size() > 0 && null != graphInfo.getMeasureTitleList() && !graphInfo.getMeasureTitleList().isEmpty()
					&& null != graphInfo.getOutlinerRow() && graphInfo.getOutlinerRow().size() > 0 && graphInfo.getMeasureTitleList().contains(graphInfo.getOutlinerRow().get(0).toString())) {
					ignoredDimensionMeasureList.addAll(graphInfo.getOutlinerDataColumns());
					graphInfo.getMeasureTitleList().removeAll(graphInfo.getOutlinerDataColumns());//ignoredDimensionMeasureList
				}
			}
		}
		if(graphType == GraphConstants.HEAT_MAP_GRAPH)// && measureArr.length > 0)//Added for HeatMap to ignore measure in Value
		{
			for(int i=1;i<measureArr.length;i++)//i=0
			{
				if(measureArr.length > 0 && (rowArr.length > 0 && !originalDimensionList.contains(rowArr[0].toString())))
				{
					ignoredDimensionMeasureList.add(measureArr[i].toString());
					map.put("orderedDataLabels","");
				}
			}
		}
		if(graphType != GraphConstants.COMBINED_GRAPH && rowlabels2.length > 0 && colItems.length > 0 && (!Arrays.asList(colItems).contains(rowlabels2[0].toString())))
		{
			for(int i=0;i<rowlabels2.length;i++)
			{
					ignoredDimensionMeasureList.add(rowlabels2[i].toString());
			}
		}
		if(!isD3Graph && !(graphType==GraphConstants.SMARTENVIEW_MAP) && !(graphType==GraphConstants.SMARTENVIEW_TABULAR))
		{
			if(graphType == GraphConstants.HEAT_MAP_GRAPH && !graphInfo.isSmartenMode() && colItems.length > 1)
			{//For setting 2nd D in Row when M-off & HeatMap
				String heatMapColLabels = "";
				for(int i=0;i<colItems.length;i++)
				{
					if(i == 0)
						heatMapColLabels = colItems[i].toString();
					else if(i == 1 && (measureArr.length == 1 || (rowArr.length == 1 && !originalDimensionList.contains(rowArr[0].toString()))))
						heatMapColLabels += "," + colItems[i].toString();
					else
						ignoredDimensionMeasureList.add(colItems[i].toString());
				}
				map.put("collabels", heatMapColLabels);
			}
			else
			{
				for(int i=0;i<colItems.length;i++)
				{
					if(i == 0 && !isPieWithRowDimension)
						map.put("collabels",colItems[i].toString());
					else
						ignoredDimensionMeasureList.add(colItems[i].toString());
				}
			}
		}
		if(isPieWithRowDimension)
			map.put("collabels","");
			
		//max 3 dim from col only no row.[3 apr 2018].
		if((graphType == GraphConstants.D3_TREEMAP || graphType == GraphConstants.D3_SUNBURST || graphType == GraphConstants.D3_BUBBLE) && !graphInfo.isSmartenMode())
		{
			for(int i=0;i<colItems.length;i++)
			{
				if(i >2)
					ignoredDimensionMeasureList.add(colItems[i].toString());
			}
		}
		//max 2 dim from col only no row.[3 apr 2018].
		if(graphType == GraphConstants.D3_CHORD &&  !graphInfo.isSmartenMode())
		{
			for(int i=0;i<colItems.length;i++)
			{
				if(i > 1)
					ignoredDimensionMeasureList.add(colItems[i].toString());
			}
		}
		
		if (finalCols.endsWith(",")) {
			finalCols = finalCols.substring(0, finalCols.length() - 1);
			map.put("coloumns",finalCols);
		}
		if (finalRows.endsWith(",")) {
			finalRows = finalRows.substring(0, finalRows.length() - 1);
			map.put("rows",finalRows);
		}
		
		
		/**
		 * Depending on the graph type it will ignore the columns in LEGEND.
		 */
		/*if(!requestParamMap.get("rowlabels").equals(""))*/
			//ignoreForSizeShapeColor(ignoredDimensionMeasureList,map,originalDimensionList);
		
		if(!smartenMode)
			ignoreForSizeShapeColor(ignoredDimensionMeasureList,map,originalDimensionList);
		
		
		boolean measureInLegend = graphInfo.getGraphData().isRowMeasure() || graphInfo.getGraphData().isSizeMeasure() || graphInfo.getGraphData().isShapeMeasure();
		boolean multipleMeasureWhenLegend = (graphType == GraphConstants.STACKED_HBAR_GRAPH || graphType == GraphConstants.STACKED_VBAR_GRAPH
										  || graphType == GraphConstants.SMARTENVIEW_TABULAR || graphType == GraphConstants.COMBINED_GRAPH || graphType == GraphConstants.PIE_GRAPH
										  || (graphType == GraphConstants.SMARTENVIEW_MAP && graphInfo.isSmartenMode()));//Added Map check to restrict ignoring multiple measures
		//when none in category and one dim in row with multiple measure
		if(colItems.length == 0 && rowArr.length > 0 && measureArr.length >=1)
			multipleMeasureWhenLegend = true;
		
		if(rowMeasure && graphType != GraphConstants.PIE_GRAPH )//stacked with multiple measure and msr in row,same with combined[consider the first msr only]
			multipleMeasureWhenLegend = false;
		if(graphType == GraphConstants.BUBBLE_GRAPH)//bcz the other msr should be in size 
			multipleMeasureWhenLegend = false;
		//pie
		if(rowMeasure && measureArr.length > 1 && graphType == GraphConstants.PIE_GRAPH && !graphInfo.isSmartenMode())
			multipleMeasureWhenLegend = false;
		
		if(graphType == GraphConstants.SMARTENVIEW_TABULAR)
		{
			multipleMeasureWhenLegend = true;
			if(rowArr.length > 0 && !originalDimensionList.contains(rowArr[0])
					|| sizeArr.length > 0 && !originalDimensionList.contains(sizeArr[0]))//14689 point 6
				multipleMeasureWhenLegend = false;
		}
		
		if(graphType == GraphConstants.HISTOGRAM_GRAPH || graphType == GraphConstants.CANDLE_STICK_GRAPH || graphType == GraphConstants.HIGH_LOW_OPEN_CLOSE_GRAPH)
			multipleMeasureWhenLegend = false;
		//if size dim for vbar then allow multiple measure
		sizeArr = StringUtil.toArray((String) map.get("sizes"));
		shapeArr = StringUtil.toArray((String) map.get("shapes"));
		/*
		 * We remove the measure from here and add unwanted measure in the ignore list which in turn will apply red color to the measure in the outliner
		 */
		boolean isAggregation = graphInfo.isPerformAggregation();
		if(!multipleMeasureWhenLegend && isAggregation)
		{
			if(graphType != GraphConstants.BUBBLE_GRAPH && ((measureArr.length > 1 && rowArr.length > 0) || (measureInLegend) || histogram_or_candle || isD3Graph))
			{
				
				for(int i=0;i<measureArr.length;i++)
				{
					if(i != 0)
					{
						ignoredDimensionMeasureList.add(measureArr[i].toString());
					}
					else
					{
						if(dataArr.length > 1)
						{
							map.put("orderedDataLabels",measureArr[i].toString());
							map.put("datalabels",measureArr[i].toString());
						}
					}
				}
				
				for(int i=0;i<measureArr.length;i++)
				{
					if(i != 0)
					{
						ignoredDimensionMeasureList.add(measureArr[i].toString());
					}
					else
					{
						if(rowArr.length > 0)
						{
							map.put("orderedDataLabels",measureArr[i].toString());
							map.put("datalabels",measureArr[i].toString());
						}
					}
				}
			}
			//Added to ignore more than 1 M when Size or Shape start
			if(graphType != GraphConstants.BUBBLE_GRAPH && ((measureArr.length > 1 && sizeArr.length > 0) || measureInLegend || histogram_or_candle || isD3Graph))
			{
				for(int i=0;i<measureArr.length;i++)
				{
					if(i != 0)
						ignoredDimensionMeasureList.add(measureArr[i].toString());
					else
					{
						if(sizeArr.length > 0)
						{
							map.put("orderedDataLabels",measureArr[i].toString());
							map.put("datalabels",measureArr[i].toString());
						}
					}
				}
			}
			if(graphType != GraphConstants.BUBBLE_GRAPH && ((measureArr.length > 1 && shapeArr.length > 0) || measureInLegend || histogram_or_candle || isD3Graph))
			{
				for(int i=0;i<measureArr.length;i++)
				{
					if(i != 0)
						ignoredDimensionMeasureList.add(measureArr[i].toString());
					else
					{
						if(shapeArr.length > 0)
						{
							map.put("orderedDataLabels",measureArr[i].toString());
							map.put("datalabels",measureArr[i].toString());
						}
					}
				}
			}
			//Added to ignore more than 1 M when Size or Shape end
			if(graphType == GraphConstants.BUBBLE_GRAPH && measureArr.length  >= 2)
			{
				if(!smartenMode)
				{
					for(int i=0;i<measureArr.length;i++)
					{
						if(i != 0 && !ignoredDimensionMeasureList.contains(measureArr[i]))
							ignoredDimensionMeasureList.add(measureArr[i].toString());
						else
						{
							map.put("orderedDataLabels",measureArr[i].toString());
							map.put("datalabels",measureArr[i].toString());
						}

					}
				}
				else
				{
					if(!graphInfo.isXyChart())
					{
						int startIndex = 2;
						if(sizeArr.length > 1)
							startIndex = 1;
						for(int i=startIndex;i<measureArr.length;i++)
						{
							ignoredDimensionMeasureList.add(measureArr[i].toString());
						}
					}
					else
					{	
						int startIndex = 3;
						for(int i=startIndex;i<measureArr.length;i++)
						{
							ignoredDimensionMeasureList.add(measureArr[i].toString());
						}
					}
				
				}
			}
			
		}
		else
		{
			if(!isAggregation && smartenMode)
			{
				ignoredDimensionMeasureList = new ArrayList();
				int maxPossibleMeasure = 5;
				if(isD3Graph)
					maxPossibleMeasure = 4;
				if(graphType == GraphConstants.D3_CHORD)
					maxPossibleMeasure = 3;
				String measure = "";
				if(smartenMode)
				{
					if(graphType == GraphConstants.BUBBLE_GRAPH)
						maxPossibleMeasure = 4;
					for(int i=0;i<measureArr.length;i++)
					{
						if(i >= maxPossibleMeasure)
							ignoredDimensionMeasureList.add(measureArr[i].toString());
						else
						{
							measure = measure + measureArr[i].toString() + ",";
						}
					}
				}
				else
				{
					if(graphInfo.isCategoryMeasure() && graphType != GraphConstants.BUBBLE_GRAPH)
					{
						measure =colItems[0].toString()+","+measureArr[0].toString();
						for(int i=1;i<measureArr.length;i++)
							ignoredDimensionMeasureList.add(measureArr[i].toString());
					}
					if(graphType == GraphConstants.BUBBLE_GRAPH)
					{
						measure =measureArr[0].toString();
						for(int i=1;i<measureArr.length;i++)
								ignoredDimensionMeasureList.add(measureArr[i].toString());
					}
				}
				if(measure.endsWith(","))
					measure = measure.substring(0, measure.length() - 1);
				map.put("orderedDataLabels",measure);
				map.put("datalabels",measure);
			}
		}
		
		if(graphType == GraphConstants.PIE_GRAPH && measureArr.length  > 1 && (rowsSize >= 1 || colsSize >= 1))
		{
			for(int i=1;i<measureArr.length;i++)
			{
				ignoredDimensionMeasureList.add(measureArr[i].toString());
				
			}
			map.put("orderedDataLabels",measureArr[0]);
			map.put("datalabels",measureArr[0]);
		}
		//ignore line msr when we hv msr in color
		if(graphType == GraphConstants.COMBINED_GRAPH && !graphInfo.isSmartenMode() && rowMeasure)
		{
			map.put("useraddeddatalabels2","");
			map.put("dataandUserLabelsLine","");
		}
		/*if(graphType == GraphConstants.PIE_GRAPH && measureArr.length > 1 && (colsArr.length > 0  || rowsArr.length > 0))
		{
			for (int i = 0; i < colsArr.length; i++) {
				ignoredDimensionMeasureList.add(colsArr[i]);
			}
			for (int i = 0; i < rowsArr.length; i++) {
				ignoredDimensionMeasureList.add(rowsArr[i]);
			}			
		}*/
		String[] userAddedDataItems = StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));
		if(userAddedDataItems.length > 0 && ignoredDimensionMeasureList.size()> 0)
		{
			for(int j =0 ;j < ignoredDimensionMeasureList.size(); j ++)
			{
	    		int indexUddc=Arrays.asList(userAddedDataItems).indexOf(ignoredDimensionMeasureList.get(j).toString());
	    		
				if(indexUddc > 0)
				{
					userAddedDataItems = ArrayUtils.removeElement(userAddedDataItems, ignoredDimensionMeasureList.get(j).toString());							
					
					String uddc="";
					for (int i = 0; i < userAddedDataItems.length; i++) {
						if(i!=indexUddc-1)
						{
							if(userAddedDataItems.length-1 == i)
							{
								uddc=uddc + userAddedDataItems[i];	
							}
							else
							{
								uddc=uddc + userAddedDataItems[i]+",";
							}
						}else{
							if(indexUddc-1 == userAddedDataItems.length-1)
								if(uddc.length() > 1)
									uddc = uddc.substring(0,uddc.length()-1);
								else
									uddc="";
							
						}
					}
					map.put("useraddeddatalabels", uddc);
					userAddedDataItems = StringUtil.toArray((String) map.get("useraddeddatalabels"));
				}
			}
		}
		
		
		graphInfo.setIgnoreDimAndMeasureList(ignoredDimensionMeasureList);
		return map;
		
	}
	private void ignoreForSizeShapeColor(List ignoredDimensionMeasureList, HashMap<String, String> requestParamMap,
			List originalDimensionList) {
			
			String[] colItems = StringUtil.toArray((String) requestParamMap.get("collabels"));	
			String[] rowItems = StringUtil.toArray((String) requestParamMap.get("rowlabels"));
			String[] sizes = StringUtil.toArray((String) requestParamMap.get("sizes"));
		    String[] shape = StringUtil.toArray((String) requestParamMap.get("shapes"));
		    String[] graphtype = StringUtil.toArray((String) requestParamMap.get("sngraphtype"));
		    String[] orderedDataLabels = StringUtil.toArray((String) requestParamMap.get("orderedDataLabels"));
		    String[] datalabels = StringUtil.toArray((String) requestParamMap.get("datalabels"));
		    String[] colsArr = StringUtil.toArray((String) requestParamMap.get("coloumns"));
			String[] rowsArr = StringUtil.toArray((String) requestParamMap.get("rows"));
			String[] userAddedDataItems = StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));

			List<String> userAddedDataItemsList = Arrays.asList(userAddedDataItems); 
		    
		    if(Integer.parseInt(graphtype[0])==GraphConstants.AREA_DEPTH_GRAPH ||
		       Integer.parseInt(graphtype[0])==GraphConstants.AREA_STACK_GRAPH ||
		       Integer.parseInt(graphtype[0])==GraphConstants.AREA_PERCENTAGE_GRAPH ||
		       Integer.parseInt(graphtype[0])==GraphConstants.DRILLED_RADAR_GRAPH ||
			   Integer.parseInt(graphtype[0])==GraphConstants.DRILLED_STACKED_RADAR_GRAPH ||
			   Integer.parseInt(graphtype[0])==GraphConstants.HISTOGRAM_GRAPH || 
			   Integer.parseInt(graphtype[0])==GraphConstants.CANDLE_STICK_GRAPH || 
			   Integer.parseInt(graphtype[0])==GraphConstants.HIGH_LOW_OPEN_CLOSE_GRAPH)
		    	{
		    		if(originalDimensionList.size() > 0 && rowItems.length > 0  && !originalDimensionList.contains(rowItems[0].toString()))
		    		{
		    			ignoredDimensionMeasureList.add(rowItems[0].toString());	
		    			requestParamMap.put("rowlabels","");
		    		}
		    		if(userAddedDataItems.length > 0 && rowItems.length > 0)
		    		{
			    		int indexUddc=Arrays.asList(userAddedDataItems).indexOf(rowItems[0].toString());
			    		
						if(indexUddc > 0)
						{
							userAddedDataItems = ArrayUtils.removeElement(userAddedDataItems, rowItems[0].toString());							
							
							String uddc="";
							for (int i = 0; i < userAddedDataItems.length; i++) {
								if(i!=indexUddc-1)
								{
									if(userAddedDataItems.length-1 == i)
									{
										uddc=uddc + userAddedDataItems[i];	
									}
									else
									{
										uddc=uddc + userAddedDataItems[i]+",";
									}
								}else{
									if(indexUddc-1 == userAddedDataItems.length-1)
										if(uddc.length() > 1)
											uddc = uddc.substring(0,uddc.length()-1);
										else
											uddc="";
									
								}
							}
							requestParamMap.put("useraddeddatalabels", uddc);
							userAddedDataItems = StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));
						}
		    		}
		    	}
		    
		    //Sizes are ignored for all except line graph(When there is minimum one measure in value axis)
		    if(Integer.parseInt(graphtype[0])!=GraphConstants.LINE_GRAPH 
		    		&&  Integer.parseInt(graphtype[0])!=GraphConstants.VBAR_GRAPH 
		    		&& Integer.parseInt(graphtype[0])!=GraphConstants.HBAR_GRAPH  
		    		&& Integer.parseInt(graphtype[0])!=GraphConstants.BUBBLE_GRAPH
		    		&& Integer.parseInt(graphtype[0])!=GraphConstants.STACKED_LINE_GRAPH 
		    		&& Integer.parseInt(graphtype[0])!=GraphConstants.PERCENTAGE_LINE_GRAPH
		    		&& Integer.parseInt(graphtype[0])!=SmartenConstants.SMARTENVIEW_MAP
		    		&& Integer.parseInt(graphtype[0])!=SmartenConstants.SMARTENVIEW_TABULAR
		    		&& Integer.parseInt(graphtype[0])!=SmartenConstants.NUMERIC_DIAL_GAUGE)
		    {
		    	if(datalabels.length > 0 && sizes.length > 0)
		    	{
		    		ignoredDimensionMeasureList.add(sizes[0].toString());
		    		requestParamMap.put("sizes","");
		    	}
		    	if(userAddedDataItems.length > 0 && sizes.length > 0 )
	    		{
		    		int indexUddc=Arrays.asList(userAddedDataItems).indexOf(sizes[0].toString());
					if(indexUddc > 0)
					{
						userAddedDataItems = ArrayUtils.removeElement(userAddedDataItems, sizes[0].toString());
						String uddc="";
						for (int i = 0; i < userAddedDataItems.length; i++) {
							if(i!=indexUddc-1)
							{
								if(userAddedDataItems.length-1 == i)
								{
									uddc=uddc + userAddedDataItems[i];	
								}
								else
								{
									uddc=uddc + userAddedDataItems[i]+",";
								}
							}else{
								if(indexUddc-1 == userAddedDataItems.length-1)
									if(uddc.length() > 1)
										uddc = uddc.substring(0,uddc.length()-1);
									else
										uddc="";
								
							}
						}
						requestParamMap.put("useraddeddatalabels", uddc);
						userAddedDataItems = StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));
					}
	    		}
		    }
		    
		    if(Integer.parseInt(graphtype[0])==GraphConstants.PIE_GRAPH)
		    {		    	
		    	if(rowItems.length > 0 && originalDimensionList.contains(rowItems[0].toString()) && colItems.length > 0)
		    	{
		    		ignoredDimensionMeasureList.add(colItems[0].toString());
		    		requestParamMap.put("collabels","");
		    	}
		    	if(rowItems.length > 0 && !originalDimensionList.contains(rowItems[0].toString())
		    			&& colItems.length > 0 && originalDimensionList.contains(colItems[0].toString()) )//Added for Bug #14728 (Ignore measure in color, if dim in x-axis	 when Pie)
		    	{
		    		ignoredDimensionMeasureList.add(rowItems[0].toString());
		    		requestParamMap.put("rowlabels","");
		    	}
		    	/*if((colsArr.length > 0 || rowsArr.length > 0) && datalabels.length > 1)
		    	{
		    		for (int i = 0; i < colsArr.length; i++) {
						ignoredDimensionMeasureList.add(colsArr[i]);
					}
					for (int i = 0; i < rowsArr.length; i++) {
						ignoredDimensionMeasureList.add(rowsArr[i]);
					}
					requestParamMap.put("coloumns","");
					requestParamMap.put("rows","");
					if(colsArr.length  > 0)
					ignoredDimensionMeasureList.add(colsArr[0].toString());
					if(rowsArr.length  > 0)
						ignoredDimensionMeasureList.add(rowsArr[0].toString());
		    		ignoredDimensionMeasureList.add(datalabels[datalabels.length - 1].toString());
		    		String dataLabelsTemp="";
		    		for(int i = 0; i < datalabels.length-1; i++)
					{
						if(i==0)
							dataLabelsTemp = datalabels[i];
						else
							dataLabelsTemp += ","+datalabels[i];
					}
		    		
		    		requestParamMap.put("datalabels",dataLabelsTemp);
		    		requestParamMap.put("orderedDataLabels",dataLabelsTemp);
		    	}*/
		    }
		    
		    if(Integer.parseInt(graphtype[0])==GraphConstants.HBAR_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.VBAR_GRAPH)
		    {
		    	//if(rowItems.length>0)//has row ignore size,shape
		    	//{
		    		if(sizes.length > 0)
		    		{
		    			if(originalDimensionList.size() > 0 && originalDimensionList.contains(sizes[0].toString()))//if it is dimennsion ignore it.
		    			{
		    				ignoredDimensionMeasureList.add(sizes[0].toString());
		    				requestParamMap.put("sizes","");
		    			}
		    			if(rowItems.length > 0 && originalDimensionList.contains(rowItems[0].toString()))//row is dimension
		    			{
		    				ignoredDimensionMeasureList.add(sizes[0].toString());
		    				requestParamMap.put("sizes","");
		    			}
		    		}
		    		if(shape.length > 0)
		    			ignoredDimensionMeasureList.add(shape[0].toString());
		    		if(userAddedDataItems.length > 0 && shape.length > 0)
		    		{
				    		int indexUddc=Arrays.asList(userAddedDataItems).indexOf(shape[0].toString());
							if(indexUddc > 0)
							{
								userAddedDataItems = ArrayUtils.removeElement(userAddedDataItems, shape[0].toString());
								
								String uddc="";
								for (int i = 0; i < userAddedDataItems.length; i++) {
									if(i!=indexUddc-1)
									{
										if(userAddedDataItems.length-1 == i)
										{
											uddc=uddc + userAddedDataItems[i];	
										}
										else
										{
											uddc=uddc + userAddedDataItems[i]+",";
										}
									}else{
										if(indexUddc-1 == userAddedDataItems.length-1)
											if(uddc.length() > 1)
												uddc = uddc.substring(0,uddc.length()-1);
											else
												uddc="";
										
									}
								}
								requestParamMap.put("useraddeddatalabels", uddc);
								userAddedDataItems = StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));
							}
					}
		    		
		    		requestParamMap.put("shapes", "");
		    	//}
		    }
		    if(Integer.parseInt(graphtype[0])==GraphConstants.PERCENTAGE_VBAR_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.STACKED_VBAR_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.PERCENTAGE_HBAR_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.STACKED_HBAR_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.PERCENTAGE_LINE_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.STACKED_LINE_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.AREA_DEPTH_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.AREA_STACK_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.AREA_PERCENTAGE_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.PIE_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.COMBINED_GRAPH||
		    		Integer.parseInt(graphtype[0])==GraphConstants.HEAT_MAP_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.DRILLED_RADAR_GRAPH ||
		    		Integer.parseInt(graphtype[0])==GraphConstants.DRILLED_STACKED_RADAR_GRAPH || 
		    		Integer.parseInt(graphtype[0]) == GraphConstants.D3_CHORD ||
		    		Integer.parseInt(graphtype[0]) == GraphConstants.D3_TREEMAP || 
					Integer.parseInt(graphtype[0]) == GraphConstants.D3_SUNBURST || 
					Integer.parseInt(graphtype[0]) == GraphConstants.D3_BUBBLE || 
					Integer.parseInt(graphtype[0]) == GraphConstants.D3_TREELAYOUT ||
					Integer.parseInt(graphtype[0]) == GraphConstants.HISTOGRAM_GRAPH ||
					Integer.parseInt(graphtype[0]) == GraphConstants.NUMERIC_DIAL_GAUGE)
		    {
		    	if(shape.length >0)
		    		ignoredDimensionMeasureList.add(shape[0].toString());
		    	if(sizes.length >0)
		    		ignoredDimensionMeasureList.add(sizes[0].toString());
		    	if(userAddedDataItems.length > 0 && shape.length > 0)
	    		{
			    	if(shape.length >0)
			    	{
				    	int indexUddc=Arrays.asList(userAddedDataItems).indexOf(shape[0].toString());
						if(indexUddc > 0)
						{
							userAddedDataItems = ArrayUtils.removeElement(userAddedDataItems, shape[0].toString());
							String uddc="";
							for (int i = 0; i < userAddedDataItems.length; i++) {
								if(i!=indexUddc-1)
								{
									if(userAddedDataItems.length-1 == i)
									{
										uddc=uddc + userAddedDataItems[i];	
									}
									else
									{
										uddc=uddc + userAddedDataItems[i]+",";
									}
								}else{
									if(indexUddc-1 == userAddedDataItems.length-1)
									{
										if(uddc.length() > 1)
											uddc = uddc.substring(0,uddc.length()-1);
										else
											uddc="";	
									}
									
								}
							}
							requestParamMap.put("useraddeddatalabels", uddc);
							userAddedDataItems = StringUtil.toArray((String) requestParamMap
									.get("useraddeddatalabels"));
							
						}
			    	}
			    	if(sizes.length >0)
			    	{
				    	int indexUddc=Arrays.asList(userAddedDataItems).indexOf(sizes[0].toString());
						if(indexUddc > 0)
						{
							userAddedDataItems = ArrayUtils.removeElement(userAddedDataItems, sizes[0].toString());
							String uddc="";
							for (int i = 0; i < userAddedDataItems.length; i++) {
								if(i!=indexUddc-1)
								{
									if(userAddedDataItems.length-1 == i)
									{
										uddc=uddc + userAddedDataItems[i];	
									}
									else
									{
										uddc=uddc + userAddedDataItems[i]+",";
									}
								}else{
									if(indexUddc-1 == userAddedDataItems.length-1)
										if(uddc.length() > 1)
											uddc = uddc.substring(0,uddc.length()-1);
										else
											uddc="";
									
								}
							}
							requestParamMap.put("useraddeddatalabels", uddc);
							userAddedDataItems = StringUtil.toArray((String) requestParamMap
									.get("useraddeddatalabels"));
							
						}
			    	}
	    		}
	    		
	    		requestParamMap.put("shapes", "");
	    		requestParamMap.put("sizes", "");
		    }
		    if(Integer.parseInt(graphtype[0])==GraphConstants.BUBBLE_GRAPH)
		    {
		    	if(sizes.length > 0 && originalDimensionList.size() > 0 && originalDimensionList.contains(sizes[0].toString()))//if it is dimennsion ignore it.
	    		{
		    		ignoredDimensionMeasureList.add(sizes[0].toString());
	    			requestParamMap.put("sizes","");
	    		}
	    		if(shape.length > 0)
	    				ignoredDimensionMeasureList.add(shape[0].toString());
	    		if(userAddedDataItems.length > 0 && shape.length > 0)
	    		{
	    			int indexUddc=Arrays.asList(userAddedDataItems).indexOf(shape[0].toString());
					if(indexUddc > 0)
					{
						userAddedDataItems = ArrayUtils.removeElement(userAddedDataItems, shape[0].toString());
						String uddc="";
						for (int i = 0; i < userAddedDataItems.length; i++) {
							if(i!=indexUddc-1)
							{
								if(userAddedDataItems.length-1 == i)
								{
									uddc=uddc + userAddedDataItems[i];	
								}
								else
								{
									uddc=uddc + userAddedDataItems[i]+",";
								}
							}else{
								if(indexUddc-1 == userAddedDataItems.length-1)
									if(uddc.length() > 1)
										uddc = uddc.substring(0,uddc.length()-1);
									else
										uddc="";
								
							}
						}
						requestParamMap.put("useraddeddatalabels", uddc);
						userAddedDataItems = StringUtil.toArray((String) requestParamMap.get("useraddeddatalabels"));
					}
	    		}
	    		requestParamMap.put("shapes", "");
		    }
		    //MAP- ignore color measure when there is measure in value axis
		    /*if(Integer.parseInt(graphtype[0])==GraphConstants.SMARTENVIEW_MAP)
		    {
		    	if(orderedDataLabels.length > 0 && rowItems.length>0 && !originalDimensionList.contains(rowItems[0].toString()))//value axis is measure
    			{
    				ignoredDimensionMeasureList.add(rowItems[0].toString());
    				requestParamMap.put("rowlabels","");
    			}
		    }*/
			
	}

	public Map<String, Integer> getDimensionSize(UserInfo userInfo,SmartenInfo graphInfo,HttpServletRequest request,List allDetailedOutlinerValues)
	{
		long getDimensionSizeStart = System.currentTimeMillis();
		//ApplicationLog.debug(" getDimensionSize START");
		Map<String, String> dimensionMap = new HashMap<String, String>();
		Vector uniqueVect = new Vector();
		Hashtable hData = null;
		Vector vDimensions = new Vector();
		FileEx m_fileExDir = null;
		String unitText = "";
		boolean isUnitPosPrefix = false;
		String srtCubeId = graphInfo.getCubeInfo().getDataObjectId();
		 Map<String, Integer> dimensionCountMap = new HashMap();
		if(graphInfo.getDimensionValueCountMap() != null && graphInfo.getDimensionValueCountMap().size() > 0)//prev map
			dimensionCountMap = graphInfo.getDimensionValueCountMap();
		 
		String userId = userInfo.getUserId();
		
		try
		{
			IDataObject cubeInfo = graphInfo.getCubeInfo();
					//cubeDataServiceUtil.getCubeForCubeColumnInfoByCubeId(srtCubeId);
			Vector vecData = XMLDimensionColumn.loadDimensionColumn(cubeInfo);
			/*if(vecData.size() > 0) {
				hData = (Hashtable)vecData.elementAt(0);
				vDimensions = (Vector)vecData.elementAt(1);
			}*/
			
			//get dimension from main
			HashMap<String, String> requestParamMap = new HashMap<String, String>();
			Enumeration<String> requestEnum = request.getParameterNames();
			List mainOutlinerList = new ArrayList();
			while (requestEnum.hasMoreElements()) {
				String paramName = requestEnum.nextElement();
				String paramValue = StringUtil.null2String(request.getParameter(paramName));
				if(paramName.equalsIgnoreCase("main"))
				{
					if(paramValue != null && !paramValue.equals(""))
					{
						//dimensionCountMap.put("fromMainOutliner", 1);
						String[] commaSepratedArray = paramValue.split(",");
						for(int i=0;i<commaSepratedArray.length;i++)
						{
							mainOutlinerList.add(commaSepratedArray[i].toString());
						}
					}
					else
					{
						//dimensionCountMap.put("fromMainOutliner", 0);
					}
				}
			}
			
			/*HashMap<String, Vector<String>> dimensionMapTemp = metadataServiceUtil.getColumnsAndMeasuresMap(cubeInfo, userManagementServiceUtil.getUserInfoById(userId),
							new Vector(),true,true,false);*/
			
/*			List<String> measureList = metadataServiceUtil.getMeasureList(
					cubeInfo, userManagementServiceUtil.getUserInfoById(userId),false);*/
			List originalDimensionList  = metadataServiceUtil.getDimensionColumns(cubeInfo, userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			List dimensionList = new ArrayList();//only dimension which are seprated from main
			List measureListForSmarten = new ArrayList();//measure
			
			//Dim and measure list from  main outliner
			for(int i=0;i<mainOutlinerList.size();i++)
			{
				String value = mainOutlinerList.get(i).toString();
				if(originalDimensionList.contains(value))
				{
					dimensionList.add(value);
				}
				else
				{
					measureListForSmarten.add(value);
				}
			}
			//This is to get the detailed outliner dim and measure list
			if(mainOutlinerList.size() == 0)
			{
				for(int i=0;i<allDetailedOutlinerValues.size();i++)
				{
					String value = allDetailedOutlinerValues.get(i).toString();
					if(originalDimensionList.contains(value))
					{
						dimensionList.add(value);
					}
					else
					{
						measureListForSmarten.add(value);
					}
				}
			}
			graphInfo.getGraphData().setDimensionListForSmartenview(dimensionList);
			//graphInfo.setDimensionTitleList(dimensionList);
			graphInfo.getGraphData().setMeasureListForSmartenview(measureListForSmarten);
			//graphInfo.setMeasureTitleList(measureListForSmarten);

			/*if(null != graphInfo.getGraphData().getDataTitleListForSmartenview() && !graphInfo.getGraphData().getDataTitleListForSmartenview().isEmpty() 
					&& null != graphInfo.getGraphData().getColListForSmartenview() && graphInfo.getGraphData().getColListForSmartenview().isEmpty())
			{
			* Code commented to set explicitly
			*/
				graphInfo.setDimensionTitleList(dimensionList);
			/*}
			else if(null != graphInfo.getGraphData().getColListForSmartenview() && !graphInfo.getGraphData().getColListForSmartenview().isEmpty()
					&& null != graphInfo.getGraphData().getDataTitleListForSmartenview() && graphInfo.getGraphData().getDataTitleListForSmartenview().isEmpty())
			{
			* Code commented to set explicitly
			*/
				graphInfo.setMeasureTitleList(measureListForSmarten);
			//}
			
			if(dimensionList.size()>0)
			{
				vDimensions.clear();
				vDimensions.addAll(dimensionList);
			}
			/*if(allDetailedOutlinerValues.size() > 0 && mainOutlinerList.size() == 0)
			{
				vDimensions.clear();
				vDimensions.addAll(allDetailedOutlinerValues);
			}*/
			//vDimensions = new Vector()
			//get main end
			
			
			//Adding count of only those dim which were not present in the map previously
			boolean calculateSize = true;
			if(dimensionCountMap.size() > 0)
			{
				List keys = new ArrayList(dimensionCountMap.keySet());
				vDimensions.removeAll(keys);
				//now vDimensions will have newlyadded only
				if(vDimensions.size() == 0)//no new dimension is added,it will be 0 when only msr is added
					calculateSize = false;
			}
			//System.out.println("CALCULATE DIMENSION SIZE ------> "+calculateSize);
			//Adding count of only those dim which were not present in the map previously
			if(calculateSize)
			{
				if(cubeInfo instanceof CubeInfo) {
					m_fileExDir = new FileEx(AppConstants.CUBES_DIR, srtCubeId);
					for(int i = 0; vDimensions != null && i < vDimensions.size(); i++){

						String cubePath = AppContextUtil.getAppPath() + AppConstants.CUBES_DIR + "/"+ cubeInfo.getDataObjectId() + "/" + IDAIFQTDOperator.FILE_DISTINCT_PARQUEET+"/"+(String)vDimensions.get(i)+".parquet";
						BIDataset<Row> dataset = sparkServiceUtil.biDataSetFromParquet(cubePath,cubeInfo.getDataObjectId());
						dimensionCountMap.put((String)vDimensions.get(i),(int) dataset.count());
						/*if(cubeInfo.getDatabaseCubeType() == AppConstants.DATABASE_AGGREGATION_APPLICATION) {
					tocReader.Open((String)vDimensions.get(i), new CubeVector(),true,cubeInfo);
					dimensionCountMap.put((String)vDimensions.get(i), tocReader.getRowCount());
				} else if(cubeInfo.getDatabaseCubeType() == AppConstants.DATABASE_AGGREGATION_SERVER) {
					tocReader.Open((String)vDimensions.get(i), new CubeVector(),false,cubeInfo);
					dimensionCountMap.put((String)vDimensions.get(i), tocReader.getRowCount());
				}*/
						//dimensionMap.put((String)vDimensions.get(i), StringUtil.modifyDataForIndianStyleCommaPos(""+tocReader.getRowCount(), unitText, isUnitPosPrefix));
					}
				}else
				{
					String distinctFile = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/" + cubeInfo.getDataObjectId() +"/" + IDAIFQTDOperator.FILE_DISTINCT_PARQUEET;
					File file = new File(distinctFile);
					if(!file.exists()) {
						distinctFile = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/" + cubeInfo.getDataObjectId() + DataSetConstant.PUBLISH_DATASET+"/"+IDAIFQTDOperator.FILE_DISTINCT_PARQUEET;
					}
					for(int i = 0; vDimensions != null && i < vDimensions.size(); i++){

						BIDataset<Row> dataset = null;
						String cubePath = distinctFile+"/"+(String)vDimensions.get(i)+".parquet";
						
						 file = new File(cubePath);
						if(!file.exists()) {
							cubePath = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/"
									+ cubeInfo.getDataObjectId()+ DataSetConstant.PUBLISH_DATASET;
							File publishFile = new File(cubePath);
							if(!publishFile.exists()) {
								cubePath = AppContextUtil.getAppPath() + "/" +MashupsConstants.MASHUPS_MODULE_DATASETS+"/"
										+ cubeInfo.getDataObjectId()+ DataSetConstant.DEVELOPMENT;
							}
							dataset = sparkServiceUtil.biDataSetFromParquet(cubePath,cubeInfo.getDataObjectId()).select((String)vDimensions.get(i)).distinct();
						} else {
							dataset = sparkServiceUtil.biDataSetFromParquet(cubePath,cubeInfo.getDataObjectId());
						}

						//tocReader.Open((String)vDimensions.get(i), new CubeVector(),true,cubeInfo);
						dimensionCountMap.put((String)vDimensions.get(i),(int) dataset.count());

						//dimensionMap.put((String)vDimensions.get(i), StringUtil.modifyDataForIndianStyleCommaPos(""+tocReader.getRowCount(), unitText, isUnitPosPrefix));
						//tocReader.Close();
					}	

				}
			}
			
			List keys = new ArrayList(dimensionCountMap.keySet());
			for(int i = 0; i < keys.size(); i++)//Added for removing removed dimensions from old Map
			{
				if(!dimensionList.contains(keys.get(i).toString()))
					dimensionCountMap.remove(keys.get(i).toString());
			}
			
			
			//Restrict count in subCube when 1D only
			if(dimensionList.size() == 1)
			{
				String firstDim = dimensionList.get(0).toString();
				if(dimensionCountMap.get(firstDim) != null)
					graphInfo.setOriginalResultSetSize(dimensionCountMap.get(firstDim));
			}
				
		} catch(Exception exc)
		{
			ApplicationLog.error(exc);
		}
		long dimensionCountMapEnd = System.currentTimeMillis();
		String createResultTimeTake = CalendarUtil.getHMSfromMillSec(dimensionCountMapEnd - getDimensionSizeStart);
	   // ApplicationLog.debug("GetSubCubeTIme Time "+createResultTimeTake);
		ApplicationLog.debug("Unique Value identify=== -- -- >>>"+createResultTimeTake);
		return dimensionCountMap;
	}

	@ResponseBody
	@RequestMapping (value = "/generateGraphImage", produces = {"image/jpg", "image/png", "image/svg+xml"}, headers = "Accept=*/*")
	public byte[] generateGraphImage(@RequestParam(value="imageId") String imageId, @LoggedInUser UserInfo userInfo) {
		byte[] imageData = null;
    	/*try {
    		if (smartenService.getGraphImages() == null || smartenService.getGraphImages().isEmpty()) {
    			smartenService.setGraphImages(smartenService.pushGraphImage(graphInfo, ""));
    		} 
    		imageData = smartenService.getGraphImages().get(imageId);
		} catch (IOException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_GRAPH_GENERATE_IMAGE",
					new Object[] { userInfo.getUsername(), getObjectDisplayName() }), e);
		}*/
		auditUserActionLog(ResourceManager.getString("LBL_GENERATE_GRAPH_IMAGES"), AppConstants.DETAIL,userInfo);
    	return imageData;
	}
	
	@RequestMapping(value = "/cancelTask")
	@ResponseBody
	public String cancelTask(@LoggedInUser UserInfo userInfo) {
		smartenService.cancelTask(graphInfo.getCubeInfo().getDataObjectId(),graphInfo);
		return AppConstants.SUCCESS_STATUS;
	}
	
	/**
	 * Close and Apply UDDC in Graph
	 * 
	 * @return 'Success' if oparation is successfull otherwise error message.
	 */
	@RequestMapping (value = "/closeUDDC")
	@ResponseBody
	public Object closeUDDC(HttpSession session, @LoggedInUser UserInfo userInfo, HttpServletResponse response, ModelMap map,
			@RequestParam(value = "smartenMEnable", required = false) boolean smartenMEnable) {
		
		Object status = "";
		List dimensionList = graphInfo.getDimensionTitleList();
		List measureList = graphInfo.getMeasureTitleList();
		List finalMeasureList = new ArrayList();
		List<String> actUddcStringList = new ArrayList<String>();
		Vector outlinerDataColumns = graphInfo.getOutlinerDataColumns();//Code for adding new added UDDC in Detail outliner
		Vector finalOutlinerDataColumns = new Vector();
		List<ActiveUDDCInfo> activeUDDCList = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
		Object[] activeTemplateIds = new Object[activeUDDCList.size()];
		for (int cnt = 0; cnt < activeUDDCList.size(); cnt++) {
			ActiveUDDCInfo activeuddcInfo = activeUDDCList.get(cnt);
			activeTemplateIds[cnt] = activeuddcInfo.getUddcTemplateInfo();
			actUddcStringList.add(activeTemplateIds[cnt].toString());
			/*if(!measureList.contains(activeTemplateIds[cnt].toString()))
				measureList.add(activeTemplateIds[cnt].toString());
			if(!outlinerDataColumns.contains(activeTemplateIds[cnt].toString()))
				outlinerDataColumns.add(activeTemplateIds[cnt].toString());*/
		}
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"UDDC","Set UDDC",Thread.currentThread(),userInfo,null);
		//
		List<String> allUddcStringList = new ArrayList<String>();
		try {
			@SuppressWarnings("unchecked")
			Hashtable<Long, UddcTemplateInfo> htUDDCTemplate = (Hashtable<Long, UddcTemplateInfo>) session.getAttribute("uddcTemplateTable");
			List sessionUDDCInfoList = new ArrayList(htUDDCTemplate.values());
			
			for(int j = 0; j < sessionUDDCInfoList.size(); j++) {
				allUddcStringList.add(sessionUDDCInfoList.get(j).toString());
			}
		} catch (Exception ex) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_APPLY_UDDC", new Object[]{userInfo.getUsername(), getObjectDisplayName()}), ex);
		}
		if(!measureList.isEmpty())
		{
			for(int i = 0; i < measureList.size(); i++) {
				if(!allUddcStringList.contains(measureList.get(i).toString()))// && actUddcStringList.contains(outlinerDataColumns.get(i).toString()))
					finalMeasureList.add(measureList.get(i).toString());
			}
		}
		if(!outlinerDataColumns.isEmpty())
		{
			for(int i = 0; i < outlinerDataColumns.size(); i++) {
				if(!allUddcStringList.contains(outlinerDataColumns.get(i).toString()))// && actUddcStringList.contains(outlinerDataColumns.get(i).toString()))
					finalOutlinerDataColumns.add(outlinerDataColumns.get(i).toString());
			}
		}
		if(!actUddcStringList.isEmpty() && actUddcStringList.size() > 0) {
			finalMeasureList.addAll(actUddcStringList);
			finalOutlinerDataColumns.addAll(actUddcStringList);
		}
		//
		
		try {
			smartenService.setActiveTemplates(TemplateUtil.ANA_USER_UDDC,
					activeTemplateIds, graphInfo, userInfo.getUserId(), false);
			smartenService.reArrengeDataItems(activeUDDCList,graphInfo,userInfo);
			smartenService.setMainOutliner(graphInfo,dimensionList,finalMeasureList);//Code for adding new added UDDC in Main outliner
			if(graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP)
			{
				List tempdataList = new ArrayList<String>();
				List outlinertempDataList=new ArrayList<String>(finalMeasureList);
				if(finalMeasureList.size() == 1) {
					tempdataList.clear();
					tempdataList.add(outlinertempDataList.get(0).toString());									
					graphInfo.setOutlinerRow(new Vector(tempdataList));	
				}
				if(finalMeasureList.size() == 2) {
					tempdataList.clear();
					tempdataList.add(outlinertempDataList.get(1).toString());									
					graphInfo.setOutlinerSizesColumns(new Vector(tempdataList));
				}
				if(finalMeasureList.size() == 3) {
					tempdataList.clear();
					tempdataList.add(outlinertempDataList.get(2).toString());									
					graphInfo.setOutlinerShapesColumns(new Vector(tempdataList));
				}
				if(finalMeasureList.size() > 2) {
					tempdataList.clear();
					tempdataList.addAll(outlinertempDataList.subList(3, outlinertempDataList.size()));									
					graphInfo.setOutlinerDataColumns(new Vector(tempdataList));
				}
				if(finalMeasureList.size() <= 2)
					graphInfo.setOutlinerDataColumns(new Vector());
			}
			/*else
				graphInfo.setOutlinerDataColumns(finalOutlinerDataColumns);//Code for adding new added UDDC in Detail outliner
*/			graphInfo.setSmartenMode(smartenMEnable);//Added code to maintain Smarten Mode after Rank apply
			graphInfo.setMeasureTitleList(finalMeasureList);//Added to refill measureList adding activated Uddc for checking no. of Measures in getIconsList()
			//graphInfo.setDataColumns(finalOutlinerDataColumns);		//For setting DataColumns after graph type change (handle getSubCube)
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			map.put("graphPropertiesSubmit", true);//change accordian to View (Left Panel)
			status = refreshObjectData(null,response, userInfo, map);
		} catch (CubeException | RScriptException e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_APPLY_UDDC", new Object[] {
							userInfo.getUsername(), getObjectDisplayName() }),
					e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_APPLY");
		} 
		session.removeAttribute("uddcTemplateTable");
		session.removeAttribute("activeUddcTemplate");
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_GRAPH_UDDC"), AppConstants.DETAIL,userInfo);
		return status;
	}
	
	/**
	 * Close and Apply UDHC in Graph
	 * 
	 * @return 'Success' if operation is successfully otherwise error message.
	 */
	@RequestMapping (value = "/closeUDHC")
	@ResponseBody
	public Object closeUDHC(HttpSession session, @LoggedInUser UserInfo userInfo, HttpServletResponse response, ModelMap map) {
		
		Object status = "";
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"UDHC","Set UDHC",Thread.currentThread(),userInfo,null);
		List<ActiveUDHCInfo> activeUDHCList = graphInfo.getActiveUDHCInfo(userInfo.getUserId());
		Object[] activeTemplateIds = new Object[activeUDHCList.size()];
		for (int cnt = 0; cnt < activeUDHCList.size(); cnt++) {
			ActiveUDHCInfo udhcInfo = activeUDHCList.get(cnt);
			activeTemplateIds[cnt] = TemplateUtil.getDataLabelVector(udhcInfo, null);
		}
		try {
			smartenService.setActiveTemplates(TemplateUtil.ANA_USER_UDHC, activeTemplateIds, graphInfo, userInfo.getUserId(), false);
			
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			status = refreshObjectData(null,response, userInfo, map);
		} catch (CubeException | RScriptException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_APPLY_UDHC", new Object[]{userInfo.getUsername(), getObjectDisplayName()}), e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_APPLY");
		}
		session.removeAttribute("udhcTemplateTable");
		session.removeAttribute("activeUdhcTemplate");
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_GRAPH_UDHC"), AppConstants.DETAIL,userInfo);
		return status;
	}
	
	/**
	 * Close and Apply Data Display Value in Graph
	 * 
	 * @return 'Success' if oparation is successfull otherwise error message.
	 */
	@RequestMapping (value = "/closeDDVM")
	@ResponseBody
	public Object closeDDVM(HttpSession session, @LoggedInUser UserInfo userInfo, HttpServletResponse response, ModelMap map) {
		Object status = "";
		try {
			detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Data display value","Apply data display value",Thread.currentThread(),userInfo,null);
			smartenService.setActiveTemplates(TemplateUtil.ANALYSIS_VALUEMAP, null, graphInfo, userInfo.getUserId(), false);
			
			if (graphInfo.getGraphProperties().getTitleProperties().isTitleVisible()) {
				HashtableEx ddvmList = smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());

				smartenService.setObjectPageTitle(graphInfo.getGraphId(),graphInfo
						.getActiveFilterInfo(userInfo.getUserId()),
						smartenService.getPageFilterNew(graphInfo),
						smartenService.getActiveVariableMap(), smartenService.getResultSetMetaData(),
						(IDataObject)graphInfo.getCubeInfo(), graphInfo.getGraphProperties().getTitleProperties(),
						userInfo, ddvmList);
			}
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			graphInfo.getGraphProperties().setSamplingSnapShotChanged(true);//To restrict call of subCube (Sampling) after DDVM
			graphInfo.getGraphProperties().setCallCreateSmartenResultSet(false);
			status = refreshObjectData(null,response, userInfo, map);
		} catch (CubeException | RScriptException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_APPLY_DDVM", new Object[] {getObjectDisplayName(),
					userInfo.getUsername() }), e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_APPLY");
		}
		session.removeAttribute("ddvmTemplateTable");
		session.removeAttribute("activeDDVMTemplate");
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_GRAPH_DDVM"), AppConstants.DETAIL,userInfo);
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),null,"Create Object view",Thread.currentThread(),userInfo,new Date());
		return status;
	}
	
	@RequestMapping (value = "/viewGraphProperties")	
	public ModelAndView showGraphProperties(ModelMap modelMap, @LoggedInUser UserInfo userInfo)	{	

		//For Smarten Pagination start
		graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setCategoryAxisPagination
		(graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getSmartenCategoryAxisPagination());
		graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setLegendPagination
		(graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getSmartenLegendPagination());
		//For Smarten Pagination end
		if(graphInfo.getGraphProperties().getColLabelsDisplayMap() != null && !graphInfo.getGraphProperties().getColLabelsDisplayMap().isEmpty()) {
			graphInfo.getGraphProperties().setColLabelsMap(graphInfo.getGraphProperties().getColLabelsDisplayMap());
		}
		Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
		if(propertyMap != null) {
			@SuppressWarnings("rawtypes")
			Iterator itr = propertyMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				modelMap.put(key, propertyMap.get(key));
			}
		}
		modelMap.put("gpdCurrentTabName", com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GENERAL);
		/*if(propertyMap!=null && "true".equalsIgnoreCase(propertyMap.get("isSmartenD3chart").toString()))
			modelMap.put("gpdCurrentTabName", com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_TITLE);*/
		modelMap.put("measureCurrentTabName", "M"+0);
		boolean d3Graph = false;
		/*if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
			d3Graph = true;*/
		modelMap.put("d3Graph", d3Graph);
		if(isFromSmarten())
			modelMap.put("isFromSmarten", true);
		List<String> partitionCols =new ArrayList<>();
		List<String> metaDataLst=new ArrayList<>();
		try {
			if(smartenService.getObjectusedcolumnList()!=null && !smartenService.getObjectusedcolumnList().isEmpty()) {
				metaDataLst = smartenService.getObjectusedcolumnList();
			}else {
				metaDataLst = smartenService.getSubCubeMetadataList();
			}
		} catch (CubeException e) {
			ApplicationLog.error(e);
		}
		setPartitionColumns(modelMap, graphInfo.getCubeInfo().getDataObjectColumnInfoList(), metaDataLst, graphInfo.getPartitionBy());
		return new ModelAndView("smartenGraphProperties");
	}
	
	@RequestMapping(value = "/saveGraphProperties", method=RequestMethod.POST)
	@ResponseBody
	public Object saveGraphProperties(@ModelAttribute GraphProperties graphProperties,
			@RequestParam(required = false, value="gpdSelectedTabNames") String selectedTabNames,
			@RequestParam(required = false, value="gpdNextTabName") String nextTabName,
			@RequestParam(required = false, value="editByCreator") String editByCreator,
			@RequestParam(required = false, value="measureSelectedTabNames") String measureSelectedTabNames,
			@RequestParam(required = false, value="measureNextTabName") String measureNextTabName,
			@RequestParam(required = false, value="rangeColorList") String rangeColorList,
			@RequestParam(value = "partitionBy", required= false, defaultValue = "") String partitionBy,
			@RequestParam(value="valueLegendList",required=false,defaultValue="") String strValues,
			ModelMap map, HttpServletResponse response, @LoggedInUser UserInfo userInfo) {
		
		graphInfo.setPartitionBy(partitionBy);
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Graph Properties","Apply properties",Thread.currentThread(),userInfo,null);
		
		if(isFromSmarten())
			map.put("isFromSmarten", true);
		
		boolean isD3Chart = false;
		if(graphInfo.getGraphType() == GraphConstants.D3_CHORD
				|| graphInfo.getGraphType() == GraphConstants.D3_TREEMAP
				|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST
				|| graphInfo.getGraphType() == GraphConstants.D3_BUBBLE
				|| graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT)
		{	
			isD3Chart = true;
		}
		map.put("graphPropertiesSubmit", false);
		
		if (nextTabName.equals("submit")) 
		{
			/*	below checks are used to solve a problem :
			 * Problem : modelAttribute of Graph properties will apply default properties, if user doesn't click on the property tab.
			 */
			GraphProperties serverGraphProperties = graphInfo.getGraphProperties();
			//11939
			strValues = StringEscapeUtils.unescapeHtml4(strValues);
			String[] strValueList = null;
			List<String> rowList = new ArrayList<>(graphInfo.getGraphData().getRowList());
			if( null == rowList || rowList.isEmpty()) {
				rowList = new ArrayList<>(graphInfo.getGraphData().getColList());
			}
			if((null == graphInfo.getGraphData().getRowList() || graphInfo.getGraphData().getRowList().isEmpty() )&& graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ) {
				Collections.sort(rowList);
			}
			if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH && graphInfo.getGraphData().getCmbBarrowList() != null && !graphInfo.getGraphData().getCmbBarrowList().isEmpty()) {
				rowList = new ArrayList<>(graphInfo.getGraphData().getCmbBarrowList());
			}
			if(strValues.contains(AppConstants.CUBE_CONDITION_FILTER_VALUE_SEPERATOR)) {
				if(!strValues.equals("")) {
				strValues = strValues.substring(0, strValues.length()-1);}
				strValueList = StringUtil.tokenize(strValues, AppConstants.CUBE_CONDITION_FILTER_VALUE_SEPERATOR,false);
			}else{
				if(!strValues.equals("")) {
				strValues = strValues.substring(0, strValues.length()-1);}
				strValueList = (new String[] {strValues});
			}
			if(null != strValueList[0] && (strValueList[0].equals(",")||strValueList[0].equals(""))) {
				strValueList = null;	
			}
			if (strValueList != null && strValueList.length > 0) {
				for (int i = 0; i < strValueList.length; i++) {
					strValueList[i] = StringUtil.decodeURL(strValueList[i]);
				}
			}
			 /*if (strValueList.length > 0 && strValueList[strValueList.length - 1].endsWith(",")) {
				 strValueList[strValueList.length - 1] = strValueList[strValueList.length - 1].substring(0, strValueList[strValueList.length - 1].length() - 1);
		        }*/
			 LinkedHashSet<String> resultSet = new LinkedHashSet<>();

		        if(null!= strValueList && !strValueList[0].equals("")) {
			        for (String value : strValueList) {
			            resultSet.add(value);
			        }
		        }
		        
		        resultSet.addAll(rowList);

		        
		        List<String> resultList = new ArrayList<>(resultSet);
		        if(resultList.equals(rowList) ) {
		        	resultList = new ArrayList<>();
                }
				if (serverGraphProperties.getLegendCustomValueList() != null
						&& !serverGraphProperties.getLegendCustomValueList().isEmpty()
						&& !rowList.equals(serverGraphProperties.getLegendCustomValueList()) && strValueList==null) {
					graphProperties
							.setLegendCustomValueList(serverGraphProperties.getLegendCustomValueList());
					graphProperties.setCustomLegendSelectedValueList(serverGraphProperties.getCustomLegendSelectedValueList());
				} else {
					if (null != strValueList)
						graphProperties.setCustomLegendSelectedValueList(new ArrayList<>(Arrays.asList(strValueList)));
					else
						graphProperties.setCustomLegendSelectedValueList(new ArrayList<>());
					// selectedValues = Arrays.asList(strValueList);
					graphProperties.setLegendCustomValueList(resultList);
					if (null != graphProperties.getLegendProperties().getLegendValuesProperties())
						graphProperties.getLegendProperties().getLegendValuesProperties()
								.setLegendCustomValueList(resultList);
				}			//changes made by harsh on 17 dec

			//11939
			if(graphInfo.getGraphType() == GraphConstants.LINE_GRAPH
					|| graphInfo.getGraphType() == GraphConstants.STACKED_LINE_GRAPH
					|| graphInfo.getGraphType() == GraphConstants.PERCENTAGE_LINE_GRAPH)
			{	
				graphInfo.getGraphProperties().setColorType(graphProperties.getLineColorType());
				graphInfo.getGraphProperties().setCustomColors(graphProperties.getLineCustomColors());
				graphInfo.getGraphProperties().setColor(graphProperties.getLinecolor());
			}
			//This changes are made to set range color properties
			map.put("graphPropertiesSubmit", true);//change accordian
			//This is to show same color selected when no Legend and auto color selected when Legend
			if(graphInfo.getGraphType() != GraphConstants.SMARTENVIEW_TABULAR && graphInfo.getGraphType() != GraphConstants.SMARTENVIEW_MAP)
			{
				if(graphProperties.getColorType() != graphInfo.getGraphProperties().getColorType())
					graphInfo.setSmartenColorAutoCustom(false);
			}
			
			graphProperties.setSmartenProperties(serverGraphProperties.getSmartenProperties());
			if(rangeColorList != null)
				graphProperties.setRangeColorList(Arrays.asList(rangeColorList.split(",")));
			graphInfo.getGraphData().setAllMedianValueZero(false);
			
			//For Smarten Pagination start
			graphProperties.getGraphAreaProperties().getGeneralGraphArea().setSmartenCategoryAxisPagination
			(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getCategoryAxisPagination());
			graphProperties.getGraphAreaProperties().getGeneralGraphArea().setSmartenLegendPagination
			(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getLegendPagination());
			//For Smarten Pagination end
			
			//Sampling issue on property wizard(graphProperties shows false for sampling)
			graphProperties.setSampling(serverGraphProperties.isSampling());
			graphProperties.setSamplingSnapShotChanged(true);
			graphProperties.setCallCreateSmartenResultSet(false);//Added to restrict createSmartenResultSet call when properties apply
			graphProperties.setCombinedRotateType(serverGraphProperties.getCombinedRotateType());//for bug 14077
			
			//Range color
			/*graphProperties.setColorRange(graphInfo.getGraphProperties().getColorRange());//auto/custom range color
			graphProperties.setRangeColorDivValue(graphInfo.getGraphProperties().getRangeColorDivValue());
			graphProperties.setRangeColorList(graphInfo.getGraphProperties().getRangeColorList());*/
			
			if(!graphInfo.isBubbleSizeChanged() && graphInfo.getGraphProperties().getBarProperties().getBubbleSize() != graphProperties.getBarProperties().getBubbleSize())
				graphInfo.setBubbleSizeChanged(true);
			
			
			/*graphProperties.getSmartenColorProperties().setRangeColorList(Arrays.asList(rangeColorList.split(",")));
			graphProperties.getSmartenColorProperties().setColorType(graphProperties.getColorType());
			graphProperties.getSmartenColorProperties().setColor(graphProperties.getColor());
			graphProperties.getSmartenColorProperties().setCustomColors(graphProperties.getCustomColors());
			graphProperties.getSmartenColorProperties().setRangeColorDivValue(graphProperties.getRangeColorDivValue());
			graphProperties.getSmartenColorProperties().setColorRange(graphProperties.getColorRange());
			graphProperties.getSmartenColorProperties().setRangeStartColor(graphProperties.getRangeStartColor());
			graphProperties.getSmartenColorProperties().setRangeEndColor(graphProperties.getRangeEndColor());
			graphProperties.getSmartenColorProperties().setTranceperancy(graphProperties.getTranceperancy());*/
			
			//Bug #14401 start
			//graphProperties.setRangeColorDivValue(serverGraphProperties.getRangeColorDivValue());
			//graphProperties.setRangeStartColor(serverGraphProperties.getRangeStartColor());
			//graphProperties.setRangeEndColor(serverGraphProperties.getRangeEndColor());
			//Bug #14401 end
			
			//for bug 13900[when user changes the tool tip it should be set as the toolTip provided by the user.]
			if(selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DATA_VALUE))
				graphInfo.setDefaultTooltip(false);
			
			/*
			 * This quickSetting no format sets the valueAxisPosition list to new array list hence splitting all then axis and 
			 * it should work same as graph[22 nov 2018] 14031
			 */
			if(selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_Y_AXIS))
				graphInfo.setQuickSettingsNumberFormat(true);
			
			if (selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DATA_VALUE) && isD3Chart)
				graphInfo.setQuickSettingsNumberFormat(true);
			
			//for bug [13989][when user changes color from prop wizard]
			if(graphProperties.getColorType() != graphInfo.getGraphProperties().getColorType()
					|| graphProperties.getLineColorType() != graphInfo.getGraphProperties().getLineColorType())
				graphInfo.setSmartenColorAutoCustom(false);
				
				
			graphProperties.setEditByCreator(Boolean.parseBoolean(editByCreator));

			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GENERAL))
			{
				graphProperties.setGeneralProperties(serverGraphProperties.getGeneralProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_TITLE))
			{
				graphProperties.setTitleProperties(serverGraphProperties.getTitleProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_AREA))
			{
				graphProperties.setGraphAreaProperties(serverGraphProperties.getGraphAreaProperties());
				graphProperties.setZoomType(serverGraphProperties.getZoomType());//Added to solve Bug #12119
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_PIE_TITLE))
			{
				graphProperties.setPieTitle(serverGraphProperties.getPieTitle());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DOUGHNUT_TITLE))
			{
				graphProperties.setDoughnutTitleProperties(serverGraphProperties.getDoughnutTitleProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_X_AXIS)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_RADAR_AXIS))
			{
				graphProperties.setxAxisProperties(serverGraphProperties.getxAxisProperties());
			}
			/*if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_Y_AXIS)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_RADAR_SCALE))
			{*/
				//graphProperties.setyAxisProperties(serverGraphProperties.getyAxisProperties());
			
				Map<String, YaxisTrendProperties> yAxisMap = graphProperties.getyAxisPropertiesMap();
				Map<String, YaxisTrendProperties> yAxisServerMap = serverGraphProperties.getyAxisPropertiesMap();
				if (null != yAxisServerMap) {
					Object[] yAxisTrendServerProperties = yAxisServerMap.values().toArray();
					// This is to maintain default properties while we click on any other tabs except y-axis tab (Bug #10857)
					if (yAxisMap.size() != 0 && (selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_Y_AXIS))) {
						for (int i=0; i<yAxisServerMap.size(); i++) {
							if (i == 0) {
								 yAxisMap.remove("M"+i);
								 graphProperties.getyAxisProperties().setTabDisplayColumnName(((YaxisTrendProperties)yAxisTrendServerProperties[i]).getTabDisplayColumnName());
								 graphProperties.getyAxisProperties().setTabMesureName(((YaxisTrendProperties)yAxisTrendServerProperties[i]).getTabMesureName());
								// if(graphProperties.getyAxisPropertiesMap().get("M"+i).getLabelProperties().getAdjustedDigit()==999)
								 yAxisMap.put("M"+i, graphProperties.getyAxisProperties());
								 if(yAxisMap.get("M"+i).getLabelProperties().getAdjustedDigit()==999) {
							    		yAxisMap.get("M"+i).getLabelProperties().setAutoValue(true);	
							     }else {
							    		yAxisMap.get("M"+i).getLabelProperties().setAutoValue(false);
							    }
						    } else {
						    	if (null != yAxisMap.get("M" + i)) {
						    	if(yAxisMap.get("M"+i).getLabelProperties().getAdjustedDigit()==999) {
						    		yAxisMap.get("M"+i).getLabelProperties().setAutoValue(true);	
						    	}else {
						    		yAxisMap.get("M"+i).getLabelProperties().setAutoValue(false);
						    	}
						    	}
						    	if (measureSelectedTabNames != null && !measureSelectedTabNames.contains("M"+i)) {
							    	 yAxisMap.put("M"+i, (YaxisTrendProperties)yAxisTrendServerProperties[i]);
								} else {
									yAxisMap.get("M"+i).setTabDisplayColumnName(((YaxisTrendProperties)yAxisTrendServerProperties[i]).getTabDisplayColumnName());
									yAxisMap.get("M"+i).setTabMesureName(((YaxisTrendProperties)yAxisTrendServerProperties[i]).getTabMesureName());
								}
						    	
						    }
						}
					} else {
						if (null != yAxisServerMap && yAxisServerMap.size() > 0) {
							yAxisServerMap.remove("M0");
							graphProperties.getyAxisProperties().setTabDisplayColumnName(((YaxisTrendProperties)yAxisTrendServerProperties[0]).getTabDisplayColumnName());
							graphProperties.getyAxisProperties().setTabMesureName(((YaxisTrendProperties)yAxisTrendServerProperties[0]).getTabMesureName());
							if (selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_RADAR_SCALE)) {
								yAxisServerMap.put("M0", graphProperties.getyAxisProperties());
							} else if (measureNextTabName.isEmpty()) {
								yAxisServerMap.put("M0", serverGraphProperties.getyAxisProperties());
							} else {
								yAxisServerMap.put("M0", graphProperties.getyAxisProperties());
								if(yAxisServerMap.get("M0").getLabelProperties().getAdjustedDigit()==999) {
									yAxisServerMap.get("M0").getLabelProperties().setAutoValue(true);
								}
								else {
									yAxisServerMap.get("M0").getLabelProperties().setAutoValue(false);
								}
							}
						}
						
						graphProperties.setyAxisPropertiesMap(yAxisServerMap);
					}
				}
				graphInfo.setYaxisPropertiesFirstTime(true);
			/*}*/
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_Y_LINE_BAR_AXIS))
			{
				graphProperties.setCombinedYaxisProperties(serverGraphProperties.getCombinedYaxisProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_STOCK_CONFIG)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_CANDAL_STICK))
			{
				graphProperties.setCandleStick(serverGraphProperties.getCandleStick());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GAUGE_TITLE_PROP))
			{
				graphProperties.setGaugeTitleProperties(serverGraphProperties.getGaugeTitleProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GAUGE_SCALE_PROP))
			{
				graphProperties.setGaugeScaleProperties(serverGraphProperties.getGaugeScaleProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GAUGE_THERMOMETER_PROP))
			{
				graphProperties.setThermometerGauge(serverGraphProperties.getThermometerGauge());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GAUGE_NEEDLE_PROP))
			{
				graphProperties.setGaugeNeedleProperties(serverGraphProperties.getGaugeNeedleProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GAUGE_DIAL_PROP))
			{
				graphProperties.setGaugeDialProperties(serverGraphProperties.getGaugeDialProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_GAUGE_LEGEND_PROP) 
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_LEGEND))
			{
				graphProperties.setLegendProperties(serverGraphProperties.getLegendProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_LEVEL_PROP))
			{
				graphProperties.setGaugeLevel(serverGraphProperties.getGaugeLevel());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DATAVALUE_PROP))
			{
				graphProperties.setGaugeDataValueScale(serverGraphProperties.getGaugeDataValueScale());
				graphProperties.setGaugeDataValueActual(serverGraphProperties.getGaugeDataValueActual());
				graphProperties.setGaugeDataValueTarget(serverGraphProperties.getGaugeDataValueTarget());
				graphProperties.setGaugeDataValueZone(serverGraphProperties.getGaugeDataValueZone());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_BUBBLE)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_COMB_CONFIG)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_VERTICAL_BAR))
			{
				graphProperties.setBarProperties(serverGraphProperties.getBarProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_COMB_CONFIG)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_LINE_PROP))
			{
				graphProperties.setGraphLineProperties(serverGraphProperties.getGraphLineProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_COMB_LEGEND))
			{
				graphProperties.getCombinedGraph().setBarLegendProperties(serverGraphProperties.getCombinedGraph().getBarLegendProperties());
				graphProperties.getCombinedGraph().setLineLegendProperties(serverGraphProperties.getCombinedGraph().getLineLegendProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_RADAR))
			{
				graphProperties.setRadar(serverGraphProperties.getRadar());
				
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DOUGHNUT))
			{
				graphProperties.setDoughNutGraph(serverGraphProperties.getDoughNutGraph());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_PIE))
			{
				graphProperties.setPieGraph(serverGraphProperties.getPieGraph());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_AREA_PROP))
			{
				graphProperties.setGraphArea(serverGraphProperties.getGraphArea());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_HISTOGRAM))
			{
				graphProperties.setHistogram(serverGraphProperties.getHistogram());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_HEAT_MAP))
			{
				graphProperties.setHeatmap(serverGraphProperties.getHeatmap());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_SMARTENVIEW_MAP))
			{
				graphProperties.setMapAreaProperties(serverGraphProperties.getMapAreaProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_SMARTENVIEW_MAP_MARKER))
			{
				graphProperties.setSmartenMapShape(serverGraphProperties.getSmartenMapShape());
				graphProperties.setSmartenMapSize(serverGraphProperties.getSmartenMapSize());
			}
			graphProperties.setSamplingCB(serverGraphProperties.isSamplingCB());
			graphProperties.setSnapShotSamplingCB(serverGraphProperties.isSnapShotSamplingCB());//Added to solve Bug #13901
			graphProperties.getSmartenProperties().setShowAllMarker(serverGraphProperties.getSmartenProperties().isShowAllMarker());
			graphProperties.getSmartenProperties().setEnableGoogleMap(serverGraphProperties.getSmartenProperties().isEnableGoogleMap());
			graphProperties.setMapColorType(serverGraphProperties.getMapColorType());
			//graphProperties.setPaginationCB(serverGraphProperties.isPaginationCB());
		/*	if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DATA_VALUE))
			{
				graphProperties.setDataValueProperties(serverGraphProperties.getDataValueProperties());
			}*/
			if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
			{
				Map<String, TrendDataValueProperties> dataValuePropertiesMap = graphProperties.getDataValuePropertiesMap();
				Map<String, TrendDataValueProperties> dataValueServerMap = serverGraphProperties.getDataValuePropertiesMap();
				if (selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DATA_VALUE))
				{
					if (null != dataValuePropertiesMap) {
						Object[] dataValuesServerProperties = dataValueServerMap.values().toArray();
						for (int i=0; i<dataValueServerMap.size(); i++) {
							if (i == 0) {
								dataValuePropertiesMap.remove("M"+i);
								graphProperties.getDataValueProperties().setTabDisplayColumnName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabDisplayColumnName());
								graphProperties.getDataValueProperties().setTabMesureName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabMesureName());
								dataValuePropertiesMap.put("M"+i, graphProperties.getDataValueProperties());
								 if(dataValuePropertiesMap.get("M"+i).getNumberFormat().getAdjustedDigit()==999) {
									 dataValuePropertiesMap.get("M"+i).getNumberFormat().setAutovalue(true);	
							     }else {
							    	 dataValuePropertiesMap.get("M"+i).getNumberFormat().setAutovalue(false);
							    }
						    } else {
						    	 if(dataValuePropertiesMap.get("M"+i).getNumberFormat().getAdjustedDigit()==999) {
									 dataValuePropertiesMap.get("M"+i).getNumberFormat().setAutovalue(true);	
							     }else {
							    	 dataValuePropertiesMap.get("M"+i).getNumberFormat().setAutovalue(false);
							    }
						    	if (measureSelectedTabNames != null && !measureSelectedTabNames.contains("M"+i)) {
						    		dataValuePropertiesMap.put("M"+i, (TrendDataValueProperties)dataValuesServerProperties[i]);
								} else {
									dataValuePropertiesMap.get("M"+i).setTabDisplayColumnName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabDisplayColumnName());
									dataValuePropertiesMap.get("M"+i).setTabMesureName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabMesureName());
								}
						    }
						}
						graphProperties.setDataValuePropertiesMap(dataValuePropertiesMap);
					}
					
				}
				else
				{
					Object[] dataValuesServerProperties = dataValueServerMap.values().toArray();
					for (int i=0; i<dataValueServerMap.size(); i++) {
						if (i == 0) {
							dataValuePropertiesMap.remove("M"+i);
							graphProperties.getDataValueProperties().setTabDisplayColumnName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabDisplayColumnName());
							graphProperties.getDataValueProperties().setTabMesureName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabMesureName());
							dataValuePropertiesMap.put("M"+i, dataValueServerMap.get("M"+i));
					    } else {
					    	if (measureSelectedTabNames != null && !measureSelectedTabNames.contains("M"+i)) {
					    		dataValuePropertiesMap.put("M"+i, (TrendDataValueProperties)dataValuesServerProperties[i]);
							} else {
								dataValuePropertiesMap.get("M"+i).setTabDisplayColumnName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabDisplayColumnName());
								dataValuePropertiesMap.get("M"+i).setTabMesureName(((TrendDataValueProperties)dataValuesServerProperties[i]).getTabMesureName());
								if(dataValueServerMap.get("M"+i).getNumberFormat().getAdjustedDigit()==999) {
									dataValueServerMap.get("M"+i).getNumberFormat().setAutovalue(true);
								}
								else {
									dataValueServerMap.get("M"+i).getNumberFormat().setAutovalue(false);
								}
							}
					    }
					}
					graphProperties.setDataValuePropertiesMap(dataValuePropertiesMap);
				}
				graphProperties.setDataValueProperties(serverGraphProperties.getDataValueProperties());
			}
			else
			{
				if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DATA_VALUE))
				{
					graphProperties.setDataValueProperties(serverGraphProperties.getDataValueProperties());
				}
				else if(graphInfo.getGraphType() == GraphConstants.D3_CHORD) {
					if(graphProperties.getDataValueProperties().getNumberFormat().getAdjustedDigit()==999) {
						graphProperties.getDataValueProperties().getNumberFormat().setAutovalue(true);
					}
					else {
						graphProperties.getDataValueProperties().getNumberFormat().setAutovalue(false);
					}
				}
				
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DATA_BAR_LINE_VALUE))
			{
				graphProperties.setCombinedDataValueProperties(serverGraphProperties.getCombinedDataValueProperties());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_COMBINED_REFERENCE_LINE))
			{
				graphProperties.setBarReferencelinePropertiesMap(serverGraphProperties.getBarReferencelinePropertiesMap());
				graphProperties.setLineReferencelinePropertiesMap(serverGraphProperties.getLineReferencelinePropertiesMap());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_COMBINED_TREND_LINE))
			{
				graphProperties.setBartrendlinePropertiesMap(serverGraphProperties.getBartrendlinePropertiesMap());
				graphProperties.setLinetrendlinePropertiesMap(serverGraphProperties.getLinetrendlinePropertiesMap());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_REFERENCE_LINE))
			{
				graphProperties.setReferencelinePropertiesMap(serverGraphProperties.getReferencelinePropertiesMap());
			}
			if (!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_TREND_LINE))
			{
				graphProperties.setTrendlinePropertiesMap(serverGraphProperties.getTrendlinePropertiesMap());
			}
			
			if(!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_BUBBLE)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_COMB_CONFIG)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_VERTICAL_BAR)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_DOUGHNUT)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_PIE)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_AREA_PROP)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_HISTOGRAM)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_RADAR)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_HEAT_MAP))	
			{
				graphProperties.setCustomColors(serverGraphProperties.getCustomColors());
				graphProperties.setColorType(serverGraphProperties.getColorType());
				graphProperties.setSameColor(serverGraphProperties.isSameColor());
				graphProperties.setColor(serverGraphProperties.getColor());
				graphProperties.setTotalBarColor(serverGraphProperties.getTotalBarColor());
				graphProperties.setOtherBarColor(serverGraphProperties.getOtherBarColor());
				graphProperties.setTranceperancy(serverGraphProperties.getTranceperancy());
				graphProperties.setRangeStartColor(serverGraphProperties.getRangeStartColor());
				graphProperties.setRangeEndColor(serverGraphProperties.getRangeEndColor());
			}
			if(!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_LINE_PROP)
					&& !selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_COMB_CONFIG)
					&&!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_AREA_PROP))
				{
					graphProperties.setLineCustomColors(serverGraphProperties.getLineCustomColors());
					graphProperties.setPointCustomColors(serverGraphProperties.getPointCustomColors());
					
					graphProperties.setLineType(serverGraphProperties.getLineType());
					graphProperties.setPointType(serverGraphProperties.getPointType());
					
					graphProperties.setLineColorType(serverGraphProperties.getLineColorType());
					graphProperties.setPointColorType(serverGraphProperties.getPointColorType());
					
					graphProperties.setLinecolor(serverGraphProperties.getLinecolor());
					graphProperties.setPointcolor(serverGraphProperties.getPointcolor());
					
				}
			if(!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_BREADCRUM))
			{
				graphProperties.setBreadCrumProperties(serverGraphProperties.getBreadCrumProperties());
			}
			if(!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_COLUMN_LABELS)) {
				graphProperties.setColLabelsMap(serverGraphProperties.getColLabelsMap());
				graphProperties.setColLabelsDisplayMap(serverGraphProperties.getColLabelsMap());
			}
			if(!selectedTabNames.contains(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_CHART_SELECTION)) {
				graphProperties.setSelectionFontProp(serverGraphProperties.getSelectionFontProp());
			}
			if(graphInfo.getGraphType().equals(GraphConstants.LINE_GRAPH) ||
					graphInfo.getGraphType().equals(GraphConstants.STACKED_LINE_GRAPH) ||
					graphInfo.getGraphType().equals(GraphConstants.PERCENTAGE_LINE_GRAPH) ||
					graphInfo.getGraphType().equals(GraphConstants.COMBINED_GRAPH))
			{
				if(graphProperties.getGraphLineProperties().getGraphlinepointPropertiesList() != null && graphProperties.getGraphLineProperties().getGraphlinepointPropertiesList().isEmpty())
					graphProperties.getGraphLineProperties().setAllLineCompatibility(true);	
			}
			
			graphProperties.setClickFromSave(true);
			graphProperties.getxAxisProperties().getLabelProperties().setDateFormat(StringUtil.unescapeHtmlUtil(graphProperties.getxAxisProperties().getLabelProperties().getDateFormat()));
			
			auditUserActionLog(ResourceManager.getString("LBL_SAVE_AND_APPLY_GRAPH_PROPERTIES"), AppConstants.DETAIL,userInfo);
			try {
				if (graphProperties.getTitleProperties().isTitleVisible()) {
					HashtableEx ddvmList = smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());

					smartenService.setObjectPageTitle(graphInfo.getGraphId(),graphInfo
							.getActiveFilterInfo(userInfo.getUserId()),
							smartenService.getPageFilterNew(graphInfo),
							smartenService.getActiveVariableMap(), smartenService.getResultSetMetaData(),
							(IDataObject)graphInfo.getCubeInfo(), graphInfo.getGraphProperties().getTitleProperties(),
							userInfo, ddvmList);
				}
			} catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString(
						"LOG_ERROR_MSG_FAILED_SET_VARIABLE_IN_TITLE",
						new Object[] {
								objectTypeName,
								graphInfo.getGraphName(),
										userInfo.getUsername() }), e);

				return ResourceManager.getString("ERROR_MSG_FAILED_TO_SET_VARIABLE_IN_GRAPH_TITLE",new Object[] {e.getMessage()});
			}
			/*if(graphInfo.isOutlinerFirstTime())
				graphInfo.setQuickSettingsNumberFormat(true);*/
			graphInfo.setOutlinerFirstTime(false);
			graphInfo.setSelectedMeasureIndex(0);
			map.put("smartenMeasureCurrentTabName", "M"+0);
			map.put("smartenMeasureSelectedTabNames", "M"+0);
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			
			//Set dataValue properties to y-axis which will be shown on left  pannel of smarten
			if(graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH
					|| graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH
					|| graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH
					|| graphInfo.getGraphType() == GraphConstants.HISTOGRAM_GRAPH
					|| graphInfo.getGraphType() == GraphConstants.CANDLE_STICK_GRAPH)
			{
				Map<String, YaxisTrendProperties> yPropertyMap = graphProperties.getyAxisPropertiesMap();
				YaxisTrendProperties yProperties = yPropertyMap.get("M0");
				
				yProperties.getLabelProperties().setCommaSeprator(graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().isCommaSeprator());
				yProperties.getLabelProperties().setCommaFormat(graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().getCommaFormat());
				yProperties.getLabelProperties().setAdjustedDigit(graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().getAdjustedDigit());
				yProperties.getLabelProperties().setNumberOfDigits(graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().getNumberOfDigits());
				yProperties.getLabelProperties().setShowadAdjustedSuffixed(graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().isShowadAdjustedSuffixed());
				yPropertyMap.put("M0",yProperties);
				graphProperties.setyAxisPropertiesMap(yPropertyMap);
				
			}			
			graphInfo.setGraphProperties(graphProperties);
			return refreshObjectData(null,response, userInfo, map);
		}
		else
		{
			Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
			if(propertyMap != null) {
				@SuppressWarnings("rawtypes")
				Iterator itr = propertyMap.keySet().iterator();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					map.put(key, propertyMap.get(key));
				}
			}
			getSmartenProperties(map, userInfo);
			map.put("currentTabName", nextTabName);
			if (nextTabName.equals(com.elegantjbi.service.graph.GraphConstants.GPD_GRAPH_Y_AXIS)) {
				if (null == measureNextTabName || measureNextTabName.isEmpty()) {
					GraphProperties gProp = (GraphProperties)map.get("graphProperties");
					gProp.setyAxisProperties(gProp.getyAxisPropertiesMap().get("M"+0));
					map.put("measureCurrentTabName", "M"+0);
				} 
			}
			boolean d3Graph = false;
			/*if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
				d3Graph = true;*/
			map.put("d3Graph", d3Graph);
			map.put("isSmartenLeftPannel",false);
			map.put("legendLabel", graphInfo.getGraphData().getRowLabel());
			if(null !=graphInfo.getGraphProperties().getCustomLegendSelectedValueList()){
			map.put("selectedValues",graphInfo.getGraphProperties().getCustomLegendSelectedValueList());}
			if(graphInfo.getGraphData().getRowLabel() == null){
				map.put("legendLabel", graphInfo.getGraphData().getColLabel());					
			}
			
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			return new ModelAndView("graph/graphPropertiesTabContent");
		}
	}
	
	
	
	@RequestMapping(value = "/loadYaxisMeasureProperties", method=RequestMethod.POST)
	@ResponseBody
	public Object loadYaxisMeasureProperties(@ModelAttribute GraphProperties graphProperties,
			@RequestParam(required = false, value="measureSelectedTabNames") String selectedTabNames,
			@RequestParam(required = false, value="measureNextTabName") String nextTabName,
			ModelMap map, HttpServletResponse response, @LoggedInUser UserInfo userInfo) {
		//System.out.println("GraphController.loadYaxisMeasureProperties() ::: "+nextTabName);
		Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
		if(propertyMap != null) {
			@SuppressWarnings("rawtypes")
			Iterator itr = propertyMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				map.put(key, propertyMap.get(key));
			}
		}
		if (null == nextTabName) {
			map.put("measureCurrentTabName", "M"+0);
		} else {
			map.put("measureCurrentTabName", nextTabName);
		}
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return new ModelAndView("graph/graphYaxisPropertiesMultiMeasureProperties");
	}
	
	/**
	 * Perform Save Rank Operation
	 * 
	 * @param rankVO
	 *            rank value objec
	 * @param srtAddRank
	 *            flag for add or edit rank
	 * @param userInfo
	 *            user detail info
	 * @return 'Success' if operation is successful otherwise error message.
	 */
    @SuppressWarnings("unchecked")
	@RequestMapping (value = "/saveSmartenRank")
	@ResponseBody
	public Object saveSmartenRank( @RequestParam("rnkDimension") String rnkDimension,
			   @RequestParam("rnkMeasure") String rnkMeasure,
			   @RequestParam("rnkType") int rnkType,
			   @RequestParam(value="rnkLimit",required=false,defaultValue="") int rnkLimit,
			   @RequestParam("addEditRankFlag") String srtAddRank,
			   @LoggedInUser UserInfo userInfo)
    {
    	RankVO rankVO=new RankVO();
    	rankVO.setRankName(rnkDimension+"_Rank");
    	rankVO.setSelectedDimension(rnkDimension);
    	rankVO.setSelectedMeasure(rnkMeasure);
    	rankVO.setRankLimit(rnkLimit);
    	rankVO.setRankType(rnkType);
    	
    	
    	CubeVector  rowLabelNames = (CubeVector) getRowLabelNameVector(null).clone();
    	CubeVector  columnLabelNames = (CubeVector) getColLabelNameVector(null).clone();

    	int cubeRankIndex = -1;
    	List<CubeRankDataLabel> cubeRankDataLabels = getActiveTemplateProperties(null).getRankList();
    	if(cubeRankDataLabels!=null && cubeRankDataLabels.size() > 0)
    	{
    		for(int i=0;i < cubeRankDataLabels.size();i++)
    		{
    			if(cubeRankDataLabels.get(i).getRankName().equals(rnkDimension+"_Rank")){
    				cubeRankIndex = i;
    				srtAddRank="false";
    				break;
    			}
    		
    		}
    	}
    	if(srtAddRank.equals("false")) {
    		/*String selectedDimesion = rankVO.getSelectedDimension();
    		if(rowLabelNames.contains(selectedDimesion)){
    			cubeRankIndex = getRankDataLabelIndex(CubeRankDataLabel.COL_TYPE,cubeRankDataLabels);
    		}else{
    			cubeRankIndex = getRankDataLabelIndex(CubeRankDataLabel.ROW_TYPE,cubeRankDataLabels);
    		}*/
    	} else {
    		if(cubeRankDataLabels != null && cubeRankDataLabels.size()>0) {
    			for(CubeRankDataLabel cubeRankDataLabel:cubeRankDataLabels) {
    				if(cubeRankDataLabel.getRankName().equals(rankVO.getRankName())) {
    					return ResourceManager.getString("MSG_SORT_NAME_ALREADY_EXIST");
    				}
    			}
    		}
    		String selectedDimesion = rankVO.getSelectedDimension();
    		//CubeRankDataLabel cubeRankDataLabel  = 	null;
    		for (int i = 0; i < cubeRankDataLabels.size(); i++) {
    			CubeRankDataLabel cubeRankDataLabel  = cubeRankDataLabels.get(i);
    			if(cubeRankDataLabel.getColumnName().equals(selectedDimesion)) {
    				return ResourceManager.getString("ERROR_RANK_LABELS_MSG_INSERT_NAME");
    			}
			}
    		/*if(rowLabelNames.contains(selectedDimesion)){       //commented for Smarten Integration
    			cubeRankDataLabel  = 	getCubeRankLabel(CubeRankDataLabel.COL_TYPE,cubeRankDataLabels);
    		}else{
    			cubeRankDataLabel  = 	getCubeRankLabel(CubeRankDataLabel.ROW_TYPE,cubeRankDataLabels);
    		}		
    		if(cubeRankDataLabel != null)
    			return ResourceManager.getString("ERROR_RANK_LABELS_MSG_INSERT_NAME");*/
    	}
    	boolean columnRankVisible = true;
    	CubeRankDataLabel cubeRankLabel = null;
    	if(rankVO.getSelectedMeasure().equals(ResourceManager.getString("LBL_NONE"))) {
    		columnRankVisible = false;
    	}
    	String rankName = "";
    	boolean rankLabelVisible = false;
    	int rankLimit = 0;
    	boolean rankSortOrder = false;
    	boolean rankSummaryLabel = false;
    	int isRow = 0;
    	try {
    		if (columnRankVisible) {
    			cubeRankLabel = new CubeRankDataLabel();
    			String rankColumnName = rankVO.getSelectedDimension();
    			CubeVector targetColumns = null;
    			CubeVector rankTargetColumns = new CubeVector();
    			CubeVector rankSourceColumns = new CubeVector();
    			String rankLabelSuffix = ResourceManager.getString("RANK_LABEL_SUFFIX");
    			CubeVector dimensionList = null;
    			// Col Rank Code
    			rankName = rankVO.getSelectedDimension();// Rank Label Name
    			if(rowLabelNames.contains(rankName)) {
    				dimensionList = (CubeVector) columnLabelNames.clone();
    				isRow =CubeRankDataLabel.COL_TYPE;
    			}else{
    				isRow = CubeRankDataLabel.ROW_TYPE;
    				dimensionList = (CubeVector) rowLabelNames.clone();
    			}

    			// Always Change
    			rankLabelVisible = rankVO.isShowRankLabel();
    			try {
    				rankLimit = rankVO.getRankLimit();// Limit
    				if(rankVO.getRankLimit() <= 0){
    					rankLimit = 10000;//5
    				}
    			} catch (Exception e) {
    			}
    			if (rankVO.getRankType() == 0) {
    				rankSortOrder = false;
    			} else {
    				rankSortOrder = true;
    			}
    			rankSummaryLabel = true; //Summary Label
    			String sourceString = rankVO.getFilterString();
    			String tmpRankName = "";
    			String tmpColumnName = rankColumnName;
    			if (!sourceString.equals("")) {
    				String[] conditions = StringUtil.tokenize(sourceString, "|");
    				for (int cond = conditions.length - 1; cond < conditions.length; cond++) {
    					String[] condition = StringUtil.tokenize(conditions[cond], "@");
    					rankSourceColumns.addElement(new CubeDataLabelSource(condition[0], condition[1].equals("") ? null : condition[1]));
    					rankColumnName = condition[0];
    					tmpColumnName = condition[0];
    					tmpRankName = condition[1];
    				}
    			}
    			// If No Condition Defined Then ColumnName Must be
    			// Last Column
    			/*if (rankColumnName.equals("") && dimensionList.size() != 0) {      //commented for Smarten Integration
    				if (getObjectTypeName().equals(ResourceManager.getString("LBL_GRAPH_LINK"))) {
    					rankColumnName = (String) dimensionList.firstElement();
    				} else {
    					rankColumnName = (String) dimensionList.lastElement();
    				}
    			}*/

    			targetColumns = null;

    			String targetString = rankVO.getFilterString();

    			if (!targetString.equals("")) {
    				String[] conditions = StringUtil.tokenize(
    						targetString, "|");
    				for (int cond = 0; cond < conditions.length - 1; cond++) {
    					String[] condition = StringUtil.tokenize(
    							conditions[cond], "@");
    					if (targetColumns == null)
    						targetColumns = new CubeVector();
    					targetColumns
    					.addElement(new CubeDataLabelTarget(
    							condition[0], condition[1]
    									.equals("") ? null
    											: condition[1]));
    				}

    			}
    			if (targetColumns != null)
    				rankTargetColumns.addElement(targetColumns);
    			// For Rank On Summary Code
    			boolean isSummary = true;
    			if (!targetString.equals("")) {
    				String[] conditions = StringUtil.tokenize(targetString, "|");

    				for (int cond = 0; cond < conditions.length; cond++) {
    					String[] condition = StringUtil.tokenize(conditions[cond], "@");
    					if (condition[0].equals((String) dimensionList.lastElement())) {
    						isSummary = false;
    						break;
    					}
    				}
    			}
    			if (isSummary) {
    				rankSummaryLabel = true;
    				rankName = ResourceManager.getString("LBL_VIEW_NAME_SUMMARY")+ rankLabelSuffix;
    			} else {
    				rankSummaryLabel = false;
    				rankName = tmpRankName + rankLabelSuffix;
    			}
    			rankName = tmpColumnName +"_"+ rankName ;//rankColumnName
    			List rankBasicInfo = new ArrayList();
    			rankBasicInfo.add(userInfo.getPersonName());
    			rankBasicInfo.add(userInfo.getPersonName());
    			rankBasicInfo.add(StringUtil.unescapeHtmlUtil(rankVO.getRankName()));
    			rankBasicInfo.add(new Date());
    			rankBasicInfo.add(new Date());
    			rankBasicInfo.add(true);
    			if(cubeRankDataLabels != null && cubeRankDataLabels.size() >0) {
    				for(CubeRankDataLabel cubeRankDataLabel :cubeRankDataLabels) {
    					if(cubeRankDataLabel.getRankName().equals(rankVO.getRankName())) {
    						rankBasicInfo.remove(5);
    						rankBasicInfo.add(cubeRankDataLabel.isStatus());
    					}
    				}
    			}

    			cubeRankLabel = new CubeRankDataLabel(
    					isRow,
    					rankColumnName, rankName,
    					rankSourceColumns, rankTargetColumns,
    					rankSortOrder, rankLimit,
    					rankLabelVisible, rankSummaryLabel,
    					rankVO.getRankType(), rankVO.getSelectedMeasure(),rankVO.isBandRankValue(),rankBasicInfo,rankVO.isShowOthers());

    			if(srtAddRank.equals("false"))
    			{
    				/*List<CubeRankDataLabel> tempRankList = getActiveTemplateProperties().getRankList();
    				if(null == tempRankList)
    					tempRankList = new ArrayList<CubeRankDataLabel>();
    				for(int i = 0; i < tempRankList.size(); i++) {
	    				if(tempRankList.contains(cubeRankLabel)) {
	    					tempRankList.remove(tempRankList.indexOf(cubeRankLabel));//Remove rank if already present
	    				}
    				}*/
    				if(getActiveTemplateProperties(null).getRankList().size() > 0)
    					getActiveTemplateProperties(null).getRankList().set(cubeRankIndex, cubeRankLabel);
    				else
    					getActiveTemplateProperties(null).getRankList().add(cubeRankLabel);
    			}
    			else
    				getActiveTemplateProperties(null).getRankList().add(cubeRankLabel);

    		} else {
    			if(srtAddRank.equals("false")){
    				getActiveTemplateProperties(null).getRankList().remove(cubeRankIndex);
    			}
    		}
    	} catch(Exception ex) {
    		ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_SAVE_RANK"),ex);
    		return ResourceManager.getString("ERROR_MSG_FAILED_TO_SAVE_RANK",new Object[] {ex.getMessage()});
    	}
    	auditUserActionLog(ResourceManager.getString("LBL_ADD_SMARTEN_RANK")+rankName, AppConstants.DETAIL,userInfo);
		
    	return AppConstants.SUCCESS_STATUS;
    }
    
  	
	@RequestMapping (value = "/rankSmarten")
	@ResponseBody
	public ModelAndView showManageRankDialog(ModelMap modelMap,@RequestParam("objectType") String strObjType,
			@LoggedInUser UserInfo userInfo) {
  
    	List<CubeRankDataLabel> orgRankList  = getActiveTemplateProperties(null).getRankList();
    	List<CubeRankDataLabel> rankList  = new ArrayList<CubeRankDataLabel>();
    	String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
    	Vector rankDimension = new Vector();
    	CubeVector rowLabelNames = getRowLabelNameVector(null);
    	CubeVector columnLabelNames = getColLabelNameVector(null);
    	int iObjectType = AppConstants.ANALYSIS;
		if (strObjType.trim().equalsIgnoreCase(AppConstants.GRAPH+"")) {
			iObjectType = AppConstants.GRAPH;
		}
		
		//as discussed with Dhaval
		
		rankList=orgRankList;
    	
    	int iSortColumn = 0;
		int totalPage  = 0;
		short sRecordSize = AppConstants.DEFAULT_RECORDS_SIZE;
		int totalRecord = rankList.size();
		
		int endIndex = 1 * sRecordSize;
		int startIndex = endIndex - sRecordSize;
		if (endIndex > totalRecord) {
			endIndex = totalRecord;
		}
		double dValue = ((double) totalRecord/sRecordSize );
		totalPage = (int) (Math.ceil(dValue) * 1);
		rankList = cubeDataServiceUtil.orderByForCubeRankDataLabel(iSortColumn, IApplicationConfigurationService.SORT_ASCENDING, rankList);
		rankList = (List<CubeRankDataLabel>) rankList.subList(startIndex, endIndex);
		//session.setAttribute("rankList", rankList);
		modelMap.put("orderByInfo", rankList);
		
		for (int i = 0; i < rankList.size(); i++) {
			
			rankDimension.add(rankList.get(i).getColumnName());
    		}
		
    	modelMap.put("objectType", AppConstants.SMARTEN);
    	modelMap.put("rankList", rankList);
    	modelMap.put("sortInfo", rankDimension);
    	modelMap.put("dateFormat", strDateFormat);			
    	modelMap.addAttribute("sortOption", iSortColumn +":0");
    	modelMap.addAttribute("sortType", iSortColumn);
    	modelMap.addAttribute("totalPage", totalPage);
    	modelMap.addAttribute("pageNo", 1);
    	modelMap.put("paginationCB",graphInfo.getGraphProperties().isPaginationCB());
		modelMap.put("samplingCB",graphInfo.getGraphProperties().isSamplingCB());
		modelMap.put("snapShotCB",graphInfo.getGraphProperties().isSnapShotSamplingCB());
		modelMap.put("mainResultSetCount",graphInfo.getOriginalResultSetSize());
    	
    	if(isFromSmarten())
    		modelMap.put("isFromSmarten", true);
    	
    	// Fetch alias names for columns to display in the Top-Bottom (Rank) UI
    	Map<String, String> colLabelsMap = new HashMap<String, String>();
    	if (graphInfo != null && graphInfo.getGraphProperties() != null && graphInfo.getGraphProperties().getColLabelsMap() != null) {
    	colLabelsMap = graphInfo.getGraphProperties().getColLabelsMap();
    	}
    			
    	// Passing the same map with two keys: one for JavaScript and one for JSP table rendering
    	modelMap.put("colLabelsAliasMap", colLabelsMap);
    	modelMap.put("colLabelsMap", colLabelsMap);
    	
    	return new ModelAndView("smartview/smartenRankObject");
    }
    
    @RequestMapping (value = "/applySmartenRank")
	@ResponseBody
	public Object applySmartenRank(HttpServletResponse response, ModelMap map,
			@LoggedInUser UserInfo userInfo,
			@RequestParam("paginationCB") String paginationCB,
			@RequestParam("samplingCB") String samplingCB,
			@RequestParam("snapShotCB") String snapShotCB,
			@RequestParam("smartenMEnable") boolean smartenMEnable) {
    	graphInfo.setSmartenMode(smartenMEnable);//Added code to maintain Smarten Mode after Rank apply
    	detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),null,"Set smarten rank",Thread.currentThread(),userInfo,null);
    	//------------------sampling checkBox start-------------------------------------------------//
		
    	/*
		 * Commenting code as due to it applymanualSampling is called
		 * 
    	if(!"".equals(paginationCB) && paginationCB.equalsIgnoreCase("true"))
			graphInfo.getGraphProperties().setPaginationCB(true);
		else
			graphInfo.getGraphProperties().setPaginationCB(false);
		
		if(!"".equals(samplingCB) && samplingCB.equalsIgnoreCase("true"))
			graphInfo.getGraphProperties().setSamplingCB(true);
		else
			graphInfo.getGraphProperties().setSamplingCB(false);
		
		if(!"".equals(snapShotCB) && snapShotCB.equalsIgnoreCase("true"))
			graphInfo.getGraphProperties().setSnapShotSamplingCB(true);
		else
			graphInfo.getGraphProperties().setSnapShotSamplingCB(false);
		
		graphInfo.setSmartSamplingEnable(false);*/
		//------------------sampling checkBox end-------------------------------------------------//
    	
		List<CubeRankDataLabel> rankList = graphInfo.getRankList();
    	graphInfo.getGraphData().setGraphFromDashboard(false);//Smarten Integration
    	List<CubeRankDataLabel> activeRankList = new ArrayList<CubeRankDataLabel>();//Smarten Integration
		Object status = "";
		try {
			/*if(rankList.size() == 0) {	//commented for Smarten Integration
				smartenService.setRowRankDataLabel(null);
				smartenService.setAutoRefresh(true);
				smartenService.setRankDataLabel(null);
			}*/
			for (int cnt = 0; cnt < rankList.size(); cnt++) {
				
				CubeRankDataLabel rankDaraLabel = rankList.get(cnt);
				smartenService.setAutoRefresh(false);
				if(rankDaraLabel.isStatus()) {
					activeRankList.add(rankDaraLabel);
				}
					/*if(rankDaraLabel.getLabelType() == CubeRankDataLabel.ROW_TYPE)   //commented for Smarten Integration
						smartenService.setRowRankDataLabel(rankDaraLabel);			
					else
						smartenService.setRankDataLabel(rankDaraLabel);
				} else {
						smartenService.setRowRankDataLabel(null);
						smartenService.setRankDataLabel(null);
				}*/
			}
			//session.removeAttribute("activeRankList");
			graphInfo.setFromRank(true);
			map.put("graphPropertiesSubmit", true);//change accordian
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			status = refreshObjectData(null,response, userInfo, map);
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_APPLIED_SORT", new Object[] {getObjectDisplayName(), userInfo.getUsername() }), e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_APPLY_RANK", new Object[] {e.getMessage()});
		} 
		
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_SMARTEN_RANK"), AppConstants.DETAIL,userInfo);
		return status;
	}
    
    /**
     * Below function will delete Rank from Left Panel for Smarten
     */
    @RequestMapping (value = "/deleteSmartenRank")
	@ResponseBody
	public String deleteRank( @RequestParam(value="templateId", required=false, defaultValue="") String templateId,
			@RequestParam(value="smartenMEnable", required=false) boolean smartenMEnable,
			@LoggedInUser UserInfo userInfo) {
    	graphInfo.setSmartenMode(smartenMEnable);//Added code to maintain Smarten Mode after Rank apply
    	CubeVector  rowLabelNames = getRowLabelNameVector(null);
    	graphInfo.setFromRank(true);
    	try {
    		List<CubeRankDataLabel> rankList = getActiveTemplateProperties(null).getRankList();
    		//for (String srtRankDimension : objectIds) {
    			int cubeRankIndex = -1;
    			if(rankList.size() > 0) {
    				for (int i = 0; i < rankList.size(); i++) {
    					CubeRankDataLabel cubeRankDataLabel = rankList.get(i);
    					if(cubeRankDataLabel.getColumnName().equals(templateId.trim())) {
    						cubeRankIndex = i;
    					}
					}
    			}
    			if(rankList != null && rankList.size() > 0 && cubeRankIndex > -1)
    				rankList.remove(cubeRankIndex);		
    		//}
    		getActiveTemplateProperties(null).setRankList(rankList);
    		setRank();
    	} catch (Exception e) {
    		ApplicationLog.error(e);
    		return ResourceManager.getString("ERROR_MSG_FAILED_TO_DELETE_RANK");
    	}
    	auditUserActionLog(ResourceManager.getString("LBL_DELETE_SMARTEN_RANK"), AppConstants.DETAIL,userInfo);
    	return AppConstants.SUCCESS_STATUS;
    }
    
	/**
	 * Get Rank Data Label Index Number
	 * 
	 * @param rankType
	 *            rank type
	 * @param cubeRankDataLabels
	 *            list of cube rank data labels objecs
	 * @return indx number
	 */
	/*private int getRankDataLabelIndex(int rankType,List<CubeRankDataLabel> cubeRankDataLabels) {
		
		for (int i = 0; i < cubeRankDataLabels.size(); i++) {
			CubeRankDataLabel cubeRankDataLabel = cubeRankDataLabels.get(i);
			if (rankType == cubeRankDataLabel.getLabelType()) {
				return i;
			}
		} 
		return -1;
	}*/
	
	/**
	 * Perform Save Sort Operation
	 * 
	 * @param cubeLabelInfo
	 *            cube label info object
	 * @param strValueList
	 *            values
	 * @param srtAddSort
	 *            flag for add or edit
	 * @return 'Success' if operation is successful otherwise error message.
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping (value="/saveSmartenSort")
	@ResponseBody
	public Object saveSmartenSort(@RequestParam("srtDimension") String srtDimension,
						   @RequestParam("srtMeasure") String srtMeasure,
						   @RequestParam("sortDirection") int sortDirection,
						   @RequestParam(value="valueList",required=false,defaultValue="") String[] strValueList, 
						   @RequestParam("addEditSortFlag") String srtAddSort,@LoggedInUser UserInfo userInfo) 
	{
		CubeLabelInfo cubeLabelInfo=new CubeLabelInfo();
		cubeLabelInfo.setSortDirection(sortDirection);
		cubeLabelInfo.setName(srtDimension);		
		cubeLabelInfo.setTargetName(srtMeasure);
		//Added for proper UI start
		if(sortDirection == 0)//Ascending
			cubeLabelInfo.setSortName(srtDimension+"_Asc");
		else
			cubeLabelInfo.setSortName(srtDimension+"_Desc");
		cubeLabelInfo.setCreatedBy(userInfo.getPersonName());
		cubeLabelInfo.setUpdateBy(userInfo.getPersonName());
		//Added for proper UI end
        Date date = new Date();
		cubeLabelInfo.setUpdatedDate(date);
		int index = getSortInfoIndex(srtDimension);
		
		List<CubeLabelInfo> sortList = getActiveTemplateProperties(null).getSortList();
		
		boolean isDimensionSorted = false;
		if(srtAddSort.equals("true")){
			isDimensionSorted = isSortExistSmarten(cubeLabelInfo,sortList);
			cubeLabelInfo.setCreatedBy(userInfo.getPersonName());
			cubeLabelInfo.setUpdateBy(userInfo.getPersonName());
			cubeLabelInfo.setStatus(true);
			if(isDimensionSorted) {
				return ResourceManager.getString("ERROR_SORT_LABELS_MSG_INSERT_NAME");
			}
			for(CubeLabelInfo cuLabelInfo : sortList) {
				if(cuLabelInfo.getSortName().equals(cubeLabelInfo.getSortName())) {
					return ResourceManager.getString("MSG_SORT_NAME_ALREADY_EXIST");
				}
			}

		}else{
			for (CubeLabelInfo sortInfo : sortList) {
				if(sortInfo.getName().equals(srtDimension)){
					cubeLabelInfo.setCreatedBy(sortInfo.getCreatedBy());
					cubeLabelInfo.setUpdateBy(userInfo.getPersonName());					
					cubeLabelInfo.setCreatedDate(sortInfo.getCreatedDate());
					cubeLabelInfo.setStatus(sortInfo.isStatus());
					cubeLabelInfo.setSortName(sortInfo.getSortName());
					//cubeLabelInfo.setTargetName(srtMeasure);
					break;
				}
			}
		}
		//for advance sort when measure selected
		if(!srtMeasure.equals("none"))
			cubeLabelInfo.setSortingType(1);
		//for advance sort when measure selected	
		
		if(cubeLabelInfo.getSortingType() == 1) {
			String filterStr = cubeLabelInfo.getFilterString();
			CubeConditionList sortConditions = null;		

			if (cubeLabelInfo.getTargetName().equals(ResourceManager.getString("LBL_NONE"))) {
				cubeLabelInfo.setSortType(ICubeResultSetSupport.sortTypeByName);
			} else {
				cubeLabelInfo.setSortType(ICubeResultSetSupport.sortTypeByValue);
			}

			if (cubeLabelInfo.getTargetName().equals(ResourceManager.getString( "LBL_NONE"))
					|| filterStr  == null || filterStr.equals(ResourceManager.getString( "LBL_NONE"))) {
				sortConditions = null;
			} else {		
				if(!filterStr.equals("")) {
					String[] fVal = StringUtil.tokenize(filterStr, "|");
					sortConditions = new CubeConditionList();
					CubeCondition sortCondition = null;
					for (int i = 0; i < fVal.length; i++) {
						String[] tmp = fVal[i].split("@");
						sortCondition = new CubeCondition(
								tmp[0], true);					
						if (tmp[1].equals("")) {
							continue;
						} else {
							sortCondition.addElement(StringUtil.unescapeHtmlUtil(tmp[1]));
						}
						sortConditions.addElement(sortCondition);
					}
				}
			}
			cubeLabelInfo.setSortCondition(sortConditions);
			if(cubeLabelInfo.getSortDirection() == 0)
				cubeLabelInfo.setDescOrder(false);
			else
				cubeLabelInfo.setDescOrder(true);
			cubeLabelInfo.setCustomSortMap(null);

			if(index != -1)
				sortList.set(index,cubeLabelInfo);
			else
				sortList.add(cubeLabelInfo);
		} else if(cubeLabelInfo.getSortingType() == 2) {
			try {
				cubeLabelInfo.setSortType(ICubeResultSetSupport.sortTypeCustome);
				HashtableEx ddvmValues = new HashtableEx();
				Vector ddvmVector = new Vector ();
                 if(getActiveDDVMs(userInfo.getUserId()) != null) {
                	 ddvmValues = getActiveDDVMs(userInfo.getUserId());
                	 ddvmVector = (Vector) ddvmValues.get(cubeLabelInfo.getName());
                 }
                 
				CubeVector seqVector = new CubeVector();	
				for (int i = 0; i < strValueList.length; i++) {
					if(ddvmVector != null && ddvmVector.size() > 0) {
						boolean flag = true;
						for (Iterator iterator = ddvmVector.iterator(); iterator.hasNext();) {
							String[] values = (String[]) iterator.next();
							String tmpStr = values[0] + " [ " + values[1] + " ]";
								if (tmpStr.equalsIgnoreCase(StringUtil.unescapeHtmlUtil(strValueList[i]).toString()))  {
									seqVector.add(values[0]);
									flag = false;
								} 
							}
							if(flag) {
								seqVector.add(StringUtil.unescapeHtmlUtil(strValueList[i]));
							}
					} else {
						seqVector.add(StringUtil.unescapeHtmlUtil(strValueList[i]));
					}
				}
				if(cubeLabelInfo.getCustomSortMap() == null)
					cubeLabelInfo.setCustomSortMap(new HashMap<String, CubeVector>());
				cubeLabelInfo.getCustomSortMap().put(cubeLabelInfo.getName(), seqVector);

				cubeLabelInfo.setFilterString("");
				cubeLabelInfo.setSortCondition(null);
				cubeLabelInfo.setTargetName("");
				cubeLabelInfo.setTargetTotalType(0);

				if(index != -1)
					sortList.set(index,cubeLabelInfo);
				else
					sortList.add(cubeLabelInfo);

			} catch (Exception e){
				ApplicationLog.error(e);
			}
		}
		else{
			//cubeLabelInfo.setTargetName(ResourceManager.getString("LBL_NONE"));
			cubeLabelInfo.setTargetTotalType(0);
			cubeLabelInfo.setSortType(ICubeResultSetSupport.sortTypeByName);

			if(cubeLabelInfo.getSortDirection() == 0)
				cubeLabelInfo.setDescOrder(false);
			else
				cubeLabelInfo.setDescOrder(true);

			cubeLabelInfo.setCustomSortMap(null);
			cubeLabelInfo.setFilterString("");
			cubeLabelInfo.setCustomSortMap(null);
			//cubeLabelInfo.setTargetName("");
			cubeLabelInfo.setTargetTotalType(0);

			if(index != -1)
				sortList.set(index,cubeLabelInfo);
			else
				sortList.add(cubeLabelInfo);
		}
		auditUserActionLog(ResourceManager.getString("LBL_SAVE_SMARTEN_SORT"), AppConstants.DETAIL,userInfo);
		getActiveTemplateProperties(null).setSortList(sortList);
		return AppConstants.SUCCESS_STATUS;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping (value = "/sortSmarten")
	public ModelAndView showManageSortDialog(ModelMap map, @LoggedInUser UserInfo userInfo) {
		try {
			String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
			List<CubeLabelInfo>  sortList = getActiveTemplateProperties(null).getSortList(); 
			
			for (int i = 0; i < sortList.size(); i++) {
				CubeLabelInfo cubeLabelInfo = sortList.get(i);
				if("".equals(cubeLabelInfo.getName())) {
					sortList.remove(i--);
				}
			}
			
			int iSortColumn = 0;
			int totalPage  = 0;
			short sRecordSize = AppConstants.DEFAULT_RECORDS_SIZE;
			int totalRecord = 0;//sortList.size();
			for(CubeLabelInfo labelInfo: sortList) {
				if(labelInfo.getSortType() != ICubeResultSetSupport.sortTypeByNone) {
					totalRecord++;
				}
			}
			int endIndex = 1 * sRecordSize;
			int startIndex = endIndex - sRecordSize;
			if (endIndex > totalRecord) {
				endIndex = totalRecord;
			}
			double dValue = ((double) totalRecord/sRecordSize );
			totalPage = (int) (Math.ceil(dValue) * 1);
			sortList = cubeDataServiceUtil.orderByForCubeLabelInfo(iSortColumn, IApplicationConfigurationService.SORT_ASCENDING, sortList);
			sortList = (List<CubeLabelInfo>) sortList.subList(startIndex, endIndex);
			/*List<CubeLabelInfo> savedActiveSortInfo = (List<CubeLabelInfo>) session.getAttribute("activeSortList");
			if (savedActiveSortInfo == null) {
				List<CubeLabelInfo> sActiveSortList = new ArrayList<CubeLabelInfo>(sortList.size());
				for(CubeLabelInfo item: sortList) {
					sActiveSortList.add((CubeLabelInfo) item.clone());
				}
				session.setAttribute("activeSortList", sActiveSortList);
			}*/
			map.put("orderByInfo", sortList);
			//new
			Map<String, String> colLabels = graphInfo.getGraphProperties().getColLabelsMap();
			map.put("colLabelsMap", colLabels);
			map.put("dateFormat", strDateFormat);			
			map.addAttribute("sortOption", iSortColumn +":0");
			map.addAttribute("sortType", iSortColumn);
			map.addAttribute("totalPage", totalPage);
			map.addAttribute("pageNo", 1);
			if(isFromSmarten())
				map.put("isFromSmarten", true);
		} catch (Exception e) {
			ApplicationLog.error(e);
		}		
		return new ModelAndView("smartview/smartenSortObject");
	}
	
	/**
	 * Below function will apply Sort from Left Panel for Smarten
	 * @param response
	 * @param map
	 * @param userInfo
	 * @param session
	 * @return
	 */
	@RequestMapping (value = "/applySmartenSort")
	@ResponseBody
	public Object applySmartenSort(HttpServletResponse response, ModelMap map, @LoggedInUser UserInfo userInfo) {
		List<CubeLabelInfo> sortInfo = graphInfo.getSortList();
		Object status = "";
		String dimensionName = "";
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),null,"Set smarten rank",Thread.currentThread(),userInfo,null);
		try {
			for (int i = 0; i < sortInfo.size(); i++) {
				CubeLabelInfo cubeLabelInfo = sortInfo.get(i);
				dimensionName = cubeLabelInfo.getName();
				if (cubeLabelInfo.getSortType() == ICubeResultSetSupport.sortTypeByNone) {
					sortInfo.remove(i--);
				}
			}
			//session.removeAttribute("activeSortList");
			graphInfo.setFromSort(true);
			map.put("graphPropertiesSubmit", true);//change accordian
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			status = refreshObjectData(null,response, userInfo, map);

		}catch (Exception e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_APPLIED_SORT", new Object[] { dimensionName, getObjectDisplayName(), getObjectTypeName(), userInfo.getUsername() }), e);

			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_APPLY_SORT", new Object[] { dimensionName,e.getMessage() });
		} 
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_SMARTEN_SORT"), AppConstants.DETAIL,userInfo);
		return status;
	}
	
	/**
	 * Below function will delete Sort from Left Panel for Smarten
	 * @param templateId
	 * @param strMode
	 * @param strCellRef
	 * @param userInfo
	 * @return
	 */
	@RequestMapping (value = "/deleteSmartenSort")
	@ResponseBody
	public String deleteSmartenSort( @RequestParam(value="templateId", required=false, defaultValue="") String templateId,
			@RequestParam(value = "mode", required = false) String strMode,
			@RequestParam(value = "cellRef", required = false) String strCellRef, @LoggedInUser UserInfo userInfo) {

		List<CubeLabelInfo> cubeLabelInfos = getActiveTemplateProperties(null).getSortList();		
		try {
			List<String> objectIds = new ArrayList<String>();
			if(strMode!= null && strMode.equalsIgnoreCase("None")) {
				String dimensionName = getColumnNameFmCellReference(strCellRef.trim());
				objectIds.add(dimensionName);
			} else {
				objectIds = StringUtil.toList(templateId, ",");
			}
			//for (String srtSortDimension : objectIds) {
				for (CubeLabelInfo cubeLabelInfo : cubeLabelInfos) {
					if(cubeLabelInfo.getName().equals(templateId.trim())){
						cubeLabelInfos.remove(cubeLabelInfo);
						break;
					}
				}					
			//}
			getActiveTemplateProperties(null).setSortList(cubeLabelInfos);
		} catch (Exception e) {
			ApplicationLog.error(e);
			return ResourceManager.getString("ERROR_MSG_FAILED_TO_DELETE_SORT");
		}
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_SMARTEN_SORT"), AppConstants.DETAIL,userInfo);
		return AppConstants.SUCCESS_STATUS;
	}
	
	private int getSortInfoIndex(String strSortDimension) {

		List<CubeLabelInfo> cubeOrderByInfos = getActiveTemplateProperties(null).getSortList();
		int index = 0;
		for(CubeOrderByInfo orderInfo : cubeOrderByInfos){
			if(orderInfo.getName().equals(strSortDimension)) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
	private boolean isSortExistSmarten(CubeLabelInfo cubeLabelInfo,List<CubeLabelInfo> cubeLabelInfos) {

		for(CubeOrderByInfo orderInfo : cubeLabelInfos){
			if(orderInfo.getName().equals(cubeLabelInfo.getName())){
				if(orderInfo.getSortType() == ICubeResultSetSupport.sortTypeByNone){
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@RequestMapping(value = "/loadYaxisMeasurePropertiesSmarten", method=RequestMethod.POST)
	@ResponseBody
	public Object loadYaxisMeasurePropertiesSmarten(
			@ModelAttribute GraphProperties smartenLabelProperties,
			@RequestParam(required = false, value="smartenMeasureCurrentTabName") String currentTabName,
			@RequestParam(required = false, value="measureIndex") int measureIndex,
			@RequestParam(required = false, value="isFromRangeBucket") boolean isFromRangeBucket,
			ModelMap map, HttpServletResponse response, @LoggedInUser UserInfo userInfo) {
		//System.out.println("GraphController.loadYaxisMeasureProperties() ::: "+nextTabName);
		Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
		graphInfo.setQuickSettingsNumberFormat(true);
		if(propertyMap != null) {
			@SuppressWarnings("rawtypes")
			Iterator itr = propertyMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				map.put(key, propertyMap.get(key));
			}
		}
		Iterator entries;
		if(graphInfo.getGraphType()==GraphConstants.PIE_GRAPH)
		{
			entries = smartenLabelProperties.getDataValuePropertiesMap().entrySet().iterator();
		}else{
		entries = smartenLabelProperties.getyAxisPropertiesMap().entrySet().iterator();
		}
		if(graphInfo.getGraphType() == SmartenConstants.HEAT_MAP_GRAPH)//Added for Bug #15415
		{
			currentTabName = "0";
			measureIndex = 0;
		}
		String key="";
		while (entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		     key = entry.getKey().toString();
		}
		//String index = key.split("M")[1];
		/*smartenLabelProperties.getyAxisPropertiesMap().put(key, smartenLabelProperties.getyAxisPropertiesMap().get(key));
		smartenLabelProperties.setyAxisPropertiesMap(smartenLabelProperties.getyAxisPropertiesMap());
		smartenLabelProperties.getyAxisPropertiesMap().get(key).setTabDisplayColumnName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());
		smartenLabelProperties.getyAxisPropertiesMap().get(key).setTabMesureName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());*/
		//graphInfo.getGraphProperties().setSmartenLabelProperties(smartenLabelProperties);
		
		/*YaxisTrendProperties tmpProp = smartenLabelProperties.getyAxisPropertiesMap().get(key);
		tmpProp.setTabDisplayColumnName(tabDisplayColumnName);*/
		//boolean radarChart = (graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH);
		boolean dataValuePropertyChart = (graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH || graphInfo.getGraphType() == GraphConstants.HISTOGRAM_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.CANDLE_STICK_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH);
				
		boolean xyChart = graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH || graphInfo.isXyChart();
		map.put("isCombinedChart", false);
		map.put("isPieChart", false);
		map.put("isRadarChart", false);

		/*Using graphInfo.getDataColumns() in setTabDisplayColumnName and setTabMesureName instead of DataColLabels3 for when columnlabels[prop wizard] are given y-axis prop was not working in stacked chart
		 * as changed name is set to setTabDisplayColumnName
		Bug:14462*/
		
		if(graphInfo.getGraphType() != GraphConstants.COMBINED_GRAPH && graphInfo.getGraphType() != GraphConstants.PIE_GRAPH && !dataValuePropertyChart && !xyChart)
		{
			String index = key.split("M")[1];
			if(graphInfo.isYaxisPropertiesFirstTime())
			{
				graphInfo.setYaxisPropertiesFirstTime(false);
				graphInfo.getGraphProperties().setyAxisPropertiesMap(graphInfo.getGraphProperties().getyAxisPropertiesMap());
			}
			else
			{
				if(smartenLabelProperties.getyAxisPropertiesMap().get(key).getLineProperties().isVisible())
				{
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getyAxisTitleTrendProperties().setVisible(true);
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getLabelProperties().setVisible(true);
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getLineProperties().setVisible(true);
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
				}
				else
				{	
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getyAxisTitleTrendProperties().setVisible(false);
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getLabelProperties().setVisible(false);
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getLineProperties().setVisible(false);
					smartenLabelProperties.getyAxisPropertiesMap().get(key).getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(false);
				}
				smartenLabelProperties.getyAxisPropertiesMap().get(key).setTabDisplayColumnName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());
				smartenLabelProperties.getyAxisPropertiesMap().get(key).setTabMesureName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());
				if(graphInfo.getGraphType() != SmartenConstants.SMARTENVIEW_MAP)//Added for Jira Bug SDEVAPR20-355
				{
					graphInfo.getGraphProperties().getyAxisPropertiesMap().put(key, smartenLabelProperties.getyAxisPropertiesMap().get(key));
					graphInfo.getGraphProperties().setyAxisPropertiesMap(graphInfo.getGraphProperties().getyAxisPropertiesMap());
				}
			}
			//	graphInfo.getGraphProperties().setyAxisProperties(graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M0"));
			//	graphInfo.getGraphProperties().getyAxisProperties().setTabMesureName(graphInfo.getDataColLabels3().get(0).toString());
			//	graphInfo.getGraphProperties().getyAxisProperties().setTabDisplayColumnName(graphInfo.getDataColLabels3().get(0).toString());

			graphInfo.setSelectedMeasureIndex(measureIndex);
			if(currentTabName.indexOf("M") == -1)//does not contains
				map.put("smartenMeasureCurrentTabName", "M"+currentTabName);
			else
				map.put("smartenMeasureCurrentTabName", currentTabName);
			
			map.put("isCombinedChart", false);
		}
		else
		{			
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			map.put("isPieChart", true);
			entries = smartenLabelProperties.getDataValuePropertiesMap().entrySet().iterator();
			key="";
			while (entries.hasNext()) {
			    Map.Entry entry = (Map.Entry) entries.next();
			     key = entry.getKey().toString();
					/*graphInfo.getGraphProperties().getDataValuePropertiesMap().put(key, smartenLabelProperties.getDataValuePropertiesMap().get(key));
					graphInfo.getGraphProperties().setDataValuePropertiesMap(graphInfo.getGraphProperties().getDataValuePropertiesMap());*/
			}
			String index = key.split("M")[1];
			
			TrendDataValueProperties dataNumberFormat= smartenLabelProperties.getDataValuePropertiesMap().get(key);
			
			TrendDataValueProperties dataNumberFormatServer = graphInfo.getGraphProperties().getDataValuePropertiesMap().get(key);
			if(dataNumberFormat.getNumberFormat()!=null){
			dataNumberFormatServer.getNumberFormat().setCommaSeprator(dataNumberFormat.getNumberFormat().isCommaSeprator());
			dataNumberFormatServer.getNumberFormat().setCommaFormat(dataNumberFormat.getNumberFormat().getCommaFormat());
			dataNumberFormatServer.getNumberFormat().setAdjustedDigit(dataNumberFormat.getNumberFormat().getAdjustedDigit());
			dataNumberFormatServer.getNumberFormat().setNumberOfDigits(dataNumberFormat.getNumberFormat().getNumberOfDigits());
			}
			
			//graphInfo.getGraphProperties().getDataValuePropertiesMap().put(key, smartenLabelProperties.getDataValuePropertiesMap().get(key));
			graphInfo.getGraphProperties().getDataValuePropertiesMap().put(key, dataNumberFormatServer);
			graphInfo.getGraphProperties().setDataValuePropertiesMap(graphInfo.getGraphProperties().getDataValuePropertiesMap());
			smartenLabelProperties.getDataValuePropertiesMap().get(key).setTabDisplayColumnName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());
			smartenLabelProperties.getDataValuePropertiesMap().get(key).setTabMesureName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());
			graphInfo.setSelectedMeasureIndex(measureIndex);
			if(currentTabName.indexOf("M") == -1)//does not contains
				map.put("smartenMeasureCurrentTabName", "M"+currentTabName);
			else
				map.put("smartenMeasureCurrentTabName", currentTabName);
		}
		else if(xyChart)
		{
			String index = key.split("M")[1];
			YaxisTrendProperties prop = smartenLabelProperties.getyAxisPropertiesMap().get(key);
			if(index.equals("1"))//x-axis
			{
				graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setCommaFormat(prop.getLabelProperties().getCommaFormat());
				graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setCommaSeprator(prop.getLabelProperties().isCommaSeprator());
				graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setAdjustedDigit(prop.getLabelProperties().getAdjustedDigit());
				graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setNumberOfDigits(prop.getLabelProperties().getNumberOfDigits());
				graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setShowadAdjustedSuffixed(prop.getLabelProperties().isShowadAdjustedSuffixed());
				
				graphInfo.getGraphProperties().getxAxisProperties().getLineProperties().setVisible(prop.getLineProperties().isVisible());
				graphInfo.getGraphProperties().getxAxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(prop.getLineProperties().isVisible());
				Map<String, YaxisTrendProperties> yMap = graphInfo.getGraphProperties().getyAxisPropertiesMap();
				yMap.put("M1", prop);
				graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
			}
			if(index.equals("0"))
			{
				Map<String, YaxisTrendProperties> yMap =  graphInfo.getGraphProperties().getyAxisPropertiesMap();
				yMap.put("M0", prop);
				graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
			}
			if(index.equals("2"))//x-axis
			{
				graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaFormat(prop.getLabelProperties().getCommaFormat());
				graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaSeprator(prop.getLabelProperties().isCommaSeprator());
				graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setAdjustedDigit(prop.getLabelProperties().getAdjustedDigit());
				graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setNumberOfDigits(prop.getLabelProperties().getNumberOfDigits());
				graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setShowadAdjustedSuffixed(prop.getLabelProperties().isShowadAdjustedSuffixed());
				Map<String, YaxisTrendProperties> yMap = graphInfo.getGraphProperties().getyAxisPropertiesMap();
				yMap.put("M2", prop);
				graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
			}
			smartenLabelProperties.getyAxisPropertiesMap().get(key).setTabDisplayColumnName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());
			smartenLabelProperties.getyAxisPropertiesMap().get(key).setTabMesureName(graphInfo.getDataColLabels3().get(Integer.parseInt(index)).toString());
			graphInfo.getGraphProperties().getyAxisPropertiesMap().put(key, smartenLabelProperties.getyAxisPropertiesMap().get(key));
			graphInfo.getGraphProperties().setyAxisPropertiesMap(graphInfo.getGraphProperties().getyAxisPropertiesMap());
			graphInfo.setSelectedMeasureIndex(measureIndex);
			if(currentTabName.indexOf("M") == -1)//does not contains
				map.put("smartenMeasureCurrentTabName", "M"+currentTabName);
			else
				map.put("smartenMeasureCurrentTabName", currentTabName);
			
			map.put("isCombinedChart", false);
		}
		else if(graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH)//Added for Bug #15415
		{
			graphInfo.setSelectedMeasureIndex(measureIndex);
			if(currentTabName.indexOf("M") == -1) {//does not contains
				map.put("smartenMeasureCurrentTabName", "M"+currentTabName);
			} else {
				map.put("smartenMeasureCurrentTabName", currentTabName);
			}
			map.put("isCombinedChart", false);
			
			Map<String,YaxisTrendProperties> yMap = graphInfo.getGraphProperties().getyAxisPropertiesMap();
			YaxisTrendProperties yAxisProp = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M0");
			TrendDataValueProperties dataNumberFormat = graphInfo.getGraphProperties().getDataValueProperties();
			yAxisProp.getLabelProperties().setCommaFormat(dataNumberFormat.getNumberFormat().getCommaFormat());
			yAxisProp.getLabelProperties().setCommaSeprator(dataNumberFormat.getNumberFormat().isCommaSeprator());
			yAxisProp.getLabelProperties().setAdjustedDigit(dataNumberFormat.getNumberFormat().getAdjustedDigit());
			yAxisProp.getLabelProperties().setNumberOfDigits(dataNumberFormat.getNumberFormat().getNumberOfDigits());
			yMap.put("M0", yAxisProp);
			graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
			graphInfo.setSelectedMeasureIndex(measureIndex);
			map.put("smartenMeasureCurrentTabName", "M"+currentTabName);
		}
		else if(dataValuePropertyChart)//radarChart
		{
			Map<String,YaxisTrendProperties> yMap = graphInfo.getGraphProperties().getyAxisPropertiesMap();
			YaxisTrendProperties yAxisProp = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M0");
			TrendDataValueProperties dataNumberFormat = graphInfo.getGraphProperties().getDataValueProperties();
			yAxisProp.getLabelProperties().setCommaFormat(dataNumberFormat.getNumberFormat().getCommaFormat());
			yAxisProp.getLabelProperties().setCommaSeprator(dataNumberFormat.getNumberFormat().isCommaSeprator());
			yAxisProp.getLabelProperties().setAdjustedDigit(dataNumberFormat.getNumberFormat().getAdjustedDigit());
			yAxisProp.getLabelProperties().setNumberOfDigits(dataNumberFormat.getNumberFormat().getNumberOfDigits());
			yMap.put("M0", yAxisProp);
			graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
			graphInfo.setSelectedMeasureIndex(measureIndex);
			map.put("smartenMeasureCurrentTabName", "M"+currentTabName);
			
		}
		else
		{
			if(currentTabName.equals("0"))
			{
				//smartenLabelProperties.getCombinedDataValueProperties().setBarnumberFormat(graphInfo.getGraphProperties().getCombinedDataValueProperties().getBarnumberFormat());
				graphInfo.getGraphProperties().getCombinedDataValueProperties().setLinenumberFormat(smartenLabelProperties.getCombinedDataValueProperties().getLinenumberFormat());
				//graphInfo.getGraphProperties().getCombinedDataValueProperties().setBarnumberFormat(smartenLabelProperties.getCombinedDataValueProperties().getBarnumberFormat());
			}
			if(currentTabName.equals("1"))
			{
				//smartenLabelProperties.getCombinedDataValueProperties().setLinenumberFormat(graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinenumberFormat());
				graphInfo.getGraphProperties().getCombinedDataValueProperties().setBarnumberFormat(smartenLabelProperties.getCombinedDataValueProperties().getBarnumberFormat());
				graphInfo.getGraphProperties().getCombinedDataValueProperties().setRangeBucket(smartenLabelProperties.getCombinedDataValueProperties().getRangeBucket());
				//graphInfo.getGraphProperties().getCombinedDataValueProperties().setLinenumberFormat(smartenLabelProperties.getCombinedDataValueProperties().getLinenumberFormat());
			}

			//graphInfo.setGraphProperties(smartenLabelProperties);
			map.put("isCombinedChart", true);
			int tabName = Integer.parseInt(currentTabName);
			map.put("currentTabName", tabName);
		}
		}
		map.put("getSmartenLabelProperties", false);
		map.put("smartenLabelProperties", graphInfo.getGraphProperties().getSmartenProperties());
		map.put("graphProperties", graphInfo.getGraphProperties());
	//	map.put("graphProperties", graphInfo.getGraphProperties());
		
		//for bucket
		
		//for bucket
				if(isFromRangeBucket && !(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH))
				{
					if(graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH)//Added for Bug #15415
					{
						map.put("dataRange",graphInfo.getGraphProperties().getHeatmap().getDataRange());
						map.put("noOfRanges",graphInfo.getGraphProperties().getHeatmap().getNoOfRanges());
						map.put("customValues",graphInfo.getGraphProperties().getHeatmap().getCustomValues());
						map.put("dataRangeFrom",graphInfo.getGraphProperties().getHeatmap().isDataRangeFrom());
						map.put("dataRangeTo",graphInfo.getGraphProperties().getHeatmap().isDataRangeTo());
					} else {
						map.put("dataRange",graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+currentTabName).getRangeBucket().getDataRange());
						map.put("noOfRanges",graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+currentTabName).getRangeBucket().getNoOfRanges());
						map.put("customValues",graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+currentTabName).getRangeBucket().getCustomValues());
						map.put("dataRangeFrom",graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+currentTabName).getRangeBucket().isDataRangeFrom());
						map.put("dataRangeTo",graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+currentTabName).getRangeBucket().isDataRangeTo());
					}
				}else
				{
					if(isFromRangeBucket){
					map.put("dataRange",graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+currentTabName).getRangeBucket().getDataRange());
					map.put("noOfRanges",graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+currentTabName).getRangeBucket().getNoOfRanges());
					map.put("customValues",graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+currentTabName).getRangeBucket().getCustomValues());
					map.put("dataRangeFrom",graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+currentTabName).getRangeBucket().isDataRangeFrom());
					map.put("dataRangeTo",graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+currentTabName).getRangeBucket().isDataRangeTo());
					}
				}
				map.put("rangeBucketTabName","M"+currentTabName);
		if((currentTabName.equals("0") && graphInfo.getGraphType() != SmartenConstants.HEAT_MAP_GRAPH)//Added HeatMap check for Bug #15415
				|| currentTabName.equals("-1"))
		{
			map.put("showMeasureRange", false); 
		}
		else
		{
			map.put("showMeasureRange", true);
		}
		//response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		if(isFromRangeBucket && (graphInfo.getGraphType() == GraphConstants.PIE_GRAPH))
			return new ModelAndView("smartview/rangeBucketPie");
		if(isFromRangeBucket && (graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH))
			return new ModelAndView("smartview/rangeBucketCombine");
		else if(isFromRangeBucket && (graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH))
			return new ModelAndView("smartview/rangeBucketHeatmap");
		else if(isFromRangeBucket)
			return new ModelAndView("smartview/rangeBucket");		
		else
			return new ModelAndView("smartview/smartenNumberFormat");	
	}
	
	@RequestMapping(value = "/loadYaxisMeasurePropertiesSmartenFromGraphTab", method=RequestMethod.POST)
	@ResponseBody
	public Object loadYaxisMeasurePropertiesSmartenFromGraphTab(
			@ModelAttribute SmartenProperties smartenLabelProperties,
			@RequestParam(required = false, value="smartenMeasureCurrentTabName") String currentTabName,
			@RequestParam(required = false, value="measureIndex") int measureIndex,
			ModelMap map, HttpServletResponse response, @LoggedInUser UserInfo userInfo) {
		
		Map<String, YaxisTrendProperties> newPropMap = new LinkedHashMap<String, YaxisTrendProperties>();
		YaxisTrendProperties firstMeasureYPorp = graphInfo.getGraphProperties().getyAxisProperties();
		newPropMap.put("M0", firstMeasureYPorp);
		newPropMap.putAll(graphInfo.getGraphProperties().getyAxisPropertiesMap());
		smartenLabelProperties.setyAxisPropertiesMap(newPropMap);
		graphInfo.getGraphProperties().setyAxisPropertiesMap(newPropMap);
		//graphInfo.getGraphProperties().setSmartenLabelProperties(smartenLabelProperties);
		if(currentTabName.indexOf("M") == -1)//does not contains
			map.put("smartenMeasureCurrentTabName", "M"+currentTabName);
		else
			map.put("smartenMeasureCurrentTabName", currentTabName);
		
		map.put("smartenLabelProperties", graphInfo.getGraphProperties().getSmartenProperties());
		map.put("graphProperties", graphInfo.getGraphProperties());
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return new ModelAndView("smartview/smartenNumberFormat");	
		
	}
	
	
	/**
	 * Close and Apply Rank in Graph
	 * 
	 * @return 'Success' if oparation is successfull otherwise error message.
	 */
	@RequestMapping (value = "/closeRank")
	@ResponseBody
	public Object closeRankDialog(HttpServletRequest request, HttpServletResponse response, ModelMap map,
			@LoggedInUser UserInfo userInfo, HttpSession session) {
		
		List<CubeRankDataLabel> rankList = graphInfo.getRankList();
		graphInfo.getGraphData().setGraphFromDashboard(false);
		List<CubeRankDataLabel> activeRankList = new ArrayList<CubeRankDataLabel>();
		Object status = "";
		try {
			/*if(rankList.size() == 0) {
				graphService.setRowRankDataLabel(null);
				graphService.setAutoRefresh(true);
				graphService.setRankDataLabel(null);
			}*/
			for (int cnt = 0; cnt < rankList.size(); cnt++) {
				
				CubeRankDataLabel rankDaraLabel = rankList.get(cnt);
				smartenService.setAutoRefresh(false);
				if(rankDaraLabel.isStatus()) {
						activeRankList.add(rankDaraLabel);
				}
					/*if(rankDaraLabel.getLabelType() == CubeRankDataLabel.ROW_TYPE)
						graphService.setRowRankDataLabel(rankDaraLabel);			
					else
						graphService.setRankDataLabel(rankDaraLabel);
				} else {
						graphService.setRowRankDataLabel(null);
						graphService.setRankDataLabel(null);
				}*/
			}
			try {
				smartenService.setCubeRankDataLabelList(activeRankList);
			} catch (RScriptException e) {
				// TODO Auto-generated catch block
				ApplicationLog.error(e);
			}
			session.removeAttribute("activeRankList");
			graphInfo.setFromRank(true);
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			
			if(map != null && map.get("requireRefresh") == null) {
				request.setAttribute("forceRefresh", true);
				map.put("requireRefresh", true);
				map.put("forceRefresh", true);

				status = refreshObjectData(request, response, userInfo, map);
			}else {
				status = AppConstants.SUCCESS_STATUS;
			}
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_APPLIED_SORT", new Object[] {getObjectDisplayName(),
							userInfo.getUsername() }), e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_APPLY_RANK", new Object[] {e.getMessage()});
		} 
		auditUserActionLog( ResourceManager.getString("LBL_APPLY_GRAPH_RANK"), AppConstants.DETAIL,userInfo);
		return status;
	}

	/**
	 * Close and Apply SORT in Analysis
	 * 
	 * @return 'Success' if operation is sucessfull otherwise error message.
	 */
	@RequestMapping (value = "/closeSort")
	@ResponseBody
	public Object closeSortDialog(HttpServletRequest request, HttpServletResponse response, ModelMap map,
			@LoggedInUser UserInfo userInfo, HttpSession session) {
		List<CubeLabelInfo> sortInfo = graphInfo.getSortList();
		Object status = "";
		String dimensionName = "";
		try {
			for (int i = 0; i < sortInfo.size(); i++) {
				CubeLabelInfo cubeLabelInfo = sortInfo.get(i);
				dimensionName = cubeLabelInfo.getName();
				if (cubeLabelInfo.getSortType() == ICubeResultSetSupport.sortTypeByNone) {
					sortInfo.remove(i--);
				}
			};
			
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
			session.removeAttribute("activeSortList");
			
			if(map != null && map.get("requireRefresh") == null) {
				request.setAttribute("forceRefresh", true);
				map.put("requireRefresh", true);
				map.put("forceRefresh", true);
				status = refreshObjectData(request, response, userInfo, map);
			}else {
				status = AppConstants.SUCCESS_STATUS;
			}

		}catch (Exception e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_APPLIED_SORT", new Object[] {
							dimensionName, getObjectDisplayName(),
							getObjectTypeName(),
							userInfo.getUsername() }), e);

			status = ResourceManager.getString(
					"ERROR_MSG_FAILED_TO_APPLY_SORT",
					new Object[] { dimensionName,e.getMessage() });
		} 
		auditUserActionLog(ResourceManager.getString("LBL_APPLY_GRAPH_SORT"), AppConstants.DETAIL,userInfo);
		return status;
	}

	@Override
	public Map<SelectItem, Integer> prepareAllItemsMap(boolean includeMeasure,UserInfo userInfo) throws CubeException {
		
		return smartenService.prepareAllItemsMap(graphInfo, includeMeasure,userInfo);
	}

	@Override
	public List<SelectItem> getAdvList(UserInfo uInfo,boolean isGlobalAdd, String objectId) throws ALSException, CubeException {
		List<SelectItem> advList = new ArrayList<SelectItem>();
		Map<String, String> rowLabelNames = smartenService.getRowColumnDisplayNameMap(graphInfo);
		Map<String, String> dataLabelNames = smartenService.getMeasureDisplayNameMap(graphInfo, uInfo.getUserId());
		
		for (String key : rowLabelNames.keySet()) {
			SelectItem selectItem = new SelectItem();
			selectItem.setLabel(rowLabelNames.get(key));
			selectItem.setValue(key);
			advList.add(selectItem);
		}
		for (String key : dataLabelNames.keySet()) {
			SelectItem selectItem = new SelectItem();
			selectItem.setLabel(dataLabelNames.get(key));
			selectItem.setValue(key);
			advList.add(selectItem);
		}
		if(isGlobalAdd) {
			List<ActiveGlobalVariableInfo> activeGolbalVariableList = graphInfo.getActiveTemplateProperties().getActiveGlobalVariableInfo(uInfo.getUserId());
			if(activeGolbalVariableList != null && activeGolbalVariableList.size()>0){
				for (ActiveGlobalVariableInfo activeGlobalVariableInfo : activeGolbalVariableList) {
					String globalVariableName = activeGlobalVariableInfo.getGlobalVariableInfo().getGlobalVariableName().substring(1,  activeGlobalVariableInfo.getGlobalVariableInfo().getGlobalVariableName().lastIndexOf("$"));
					SelectItem item = new SelectItem();
					item.setLabel(globalVariableName);
					item.setValue(activeGlobalVariableInfo.getGlobalVariableInfo().getGlobalVariableName());
					advList.add(item);
				}
			}
			}
		return advList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SelectItem> getDisplayLabel(boolean pageFilter, String pageFilterColumnName,UserInfo userInfo) throws CubeException {
		
		Vector<String> dataItemList = new Vector<String>();
		List<SelectItem> dataLabels =  new ArrayList<SelectItem>();
		
		if(graphInfo.getGraphType()== GraphConstants.SMARTENVIEW_TABULAR)
			dataItemList.addAll(graphInfo.getMeasureTitleList());
		else
			dataItemList.addAll(graphInfo.getDataColumns());
		if (graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH) {
			dataItemList.addAll(graphInfo.getLineGraphDataLabelsForCombinedGraph());
		}
		if (pageFilter) {
			SelectItem selectItem = new SelectItem();
			selectItem.setLabel(pageFilterColumnName);
			selectItem.setValue(pageFilterColumnName);
			dataLabels.add(selectItem);
		} else {	
			for (String dataLabel : dataItemList) {
				SelectItem selectItem = new SelectItem();
				selectItem.setLabel(smartenService.getAxisDisplayName(dataLabel, graphInfo));
				selectItem.setValue(dataLabel);
				dataLabels.add(selectItem);
			}
			List<ActiveUDDCInfo> activeUDDCList = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
			for (int cnt = 0; cnt < activeUDDCList.size(); cnt++) {
				SelectItem selectItem = new SelectItem();
				ActiveUDDCInfo activeuddcInfo = activeUDDCList.get(cnt);
				selectItem.setLabel(activeuddcInfo.getUddcTemplateInfo().getColumnName());
				selectItem.setValue(activeuddcInfo.getUddcTemplateInfo().getColumnName());
				if(!dataItemList.contains(activeuddcInfo.getUddcTemplateInfo().getColumnName().toString()))
				dataLabels.add(selectItem);
			}
		}
			return dataLabels;
	}
	
	@RequestMapping (value = "/addTrendLineProperties")
	@ResponseBody
	public ModelAndView addTrendLineProperties(ModelMap modelMap,@RequestParam("trendLineName") String strTrendLineName
			,@RequestParam("trendLineColumn") String strTrendLineColumn			
			,@RequestParam("trendLineType") String strTrendLineType
			,@RequestParam("trendLineStyle") String strTrendLineStyle
			,@RequestParam("trendLineThickness") String strtrendLineThickness
			,@RequestParam("trendLineColor") String strtrendLineColor,@LoggedInUser UserInfo userInfo) {
		
		TrendLineProperties trendlineProperties = new TrendLineProperties();
		if(strTrendLineName != null && !strTrendLineName.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineName(strTrendLineName);
		}
		if(strTrendLineColumn != null && !strTrendLineColumn.equalsIgnoreCase(""))
		{
			//trendlineProperties.setTrendLineColumn(strTrendLineColumn);
			//check with ddvm
			//DDVM
			HashtableEx ddvmMap = new HashtableEx();
			ddvmMap = (HashtableEx) getActiveDDVMs(userInfo.getUserId());
			Vector packDisplayList=null;
			if(ddvmMap != null && ddvmMap.size() > 0)
			{
				if(ddvmMap.containsKey(graphInfo.getGraphData().getRowLabel())) 
				{
					packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getRowLabel());
				}
				else if(ddvmMap.containsKey(graphInfo.getGraphData().getColLabel()))
				{
					packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getColLabel());
				}
			}
			String[] splitString = (strTrendLineColumn).split(",");
			if (packDisplayList != null && packDisplayList.size() > 0) 
			{

					for (Iterator iterator = packDisplayList.iterator(); iterator.hasNext();) 
					{
						String[] values = (String[]) iterator.next();															
						if (values[1].equalsIgnoreCase(splitString[0]))
						{
							strTrendLineColumn =  values[0]+","+values[0];
							break;
						}
					}
			}
			trendlineProperties.setTrendLineColumn(strTrendLineColumn);
			
		}
		if(strTrendLineType != null && !strTrendLineType.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineType(strTrendLineType);
		}
		if(strTrendLineStyle != null && !strTrendLineStyle.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineStyle(strTrendLineStyle);
		}
		if(strtrendLineThickness != null && !strtrendLineThickness.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineThickness(Integer.parseInt(strtrendLineThickness));
		}
		if(strtrendLineColor != null && !strtrendLineColor.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineColor(strtrendLineColor);
		}
		Map<Integer,TrendLineProperties> trendLinePropertiesMap  = null;
		if(graphInfo.getGraphProperties().getTrendlinePropertiesMap()!= null && !graphInfo.getGraphProperties().getTrendlinePropertiesMap().isEmpty())
		{
			trendLinePropertiesMap = graphInfo.getGraphProperties().getTrendlinePropertiesMap();
		}
		else
		{
			trendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
		}
		if(trendLinePropertiesMap!= null)
		{
			trendLinePropertiesMap.put(trendLinePropertiesMap.size()+1,trendlineProperties);
		}
		
		
		graphInfo.getGraphProperties().setTrendlinePropertiesMap(trendLinePropertiesMap);	
		modelMap.put("trendlinePropertiesMap",trendLinePropertiesMap);	
		modelMap.put("isGraphProperties",true);
		modelMap.put("isSmarten",true);
		auditUserActionLog(ResourceManager.getString("LBL_ADD_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("kpi/trendlinePropobjects");
	}	
	
	@RequestMapping (value = "/removeTrendLinPropObj")
	@ResponseBody
	public ModelAndView removeTrendLinPropObj(ModelMap modelMap,@RequestParam("trendlineobjkey") String strTrendlineobjkey,@LoggedInUser UserInfo userInfo)	{
		Map<Integer,TrendLineProperties> trendLinePropertiesMap = null;
		Map<Integer,TrendLineProperties>  tempTrendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
		if(strTrendlineobjkey != null && !strTrendlineobjkey.equalsIgnoreCase("")){
			if(graphInfo.getGraphProperties().getTrendlinePropertiesMap() != null && !graphInfo.getGraphProperties().getTrendlinePropertiesMap().isEmpty())
			{
				trendLinePropertiesMap = graphInfo.getGraphProperties().getTrendlinePropertiesMap();
				trendLinePropertiesMap.remove(Integer.parseInt(strTrendlineobjkey));				
				
				int i = 0;
			//trendLinePropertiesMap = graphInfo.getGraphProperties().getTrendlinePropertiesMap();
			//Map<Integer,TrendLineProperties>  tempTrendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
			
			Iterator iterator = graphInfo.getGraphProperties().getTrendlinePropertiesMap().keySet().iterator();
			 while (iterator.hasNext()) {
			     int key = (int) iterator.next();
				tempTrendLinePropertiesMap.put(i, graphInfo.getGraphProperties().getTrendlinePropertiesMap().get(key));
				i++;
			}
				
				graphInfo.getGraphProperties().setTrendlinePropertiesMap(tempTrendLinePropertiesMap);
			}
		}
		modelMap.put("trendlinePropertiesMap",tempTrendLinePropertiesMap);
		modelMap.put("isGraphProperties",true);
		modelMap.put("isSmarten",true);
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("kpi/trendlinePropobjects");
	}
	
	@RequestMapping (value = "/editTrendTrendLinePropObj")
	@ResponseBody
	public ModelAndView editTrendTrendLinePropObj(ModelMap modelMap,@RequestParam("trendlineobjkey") String strTrendlineobjkey,
			@LoggedInUser UserInfo userInfo)	{
		Map<Integer,TrendLineProperties> trendLinePropertiesMap = null;	
		if(strTrendlineobjkey != null && !strTrendlineobjkey.equalsIgnoreCase("")){

			if(graphInfo.getGraphProperties().getTrendlinePropertiesMap()!= null && !graphInfo.getGraphProperties().getTrendlinePropertiesMap().isEmpty()){
				trendLinePropertiesMap = graphInfo.getGraphProperties().getTrendlinePropertiesMap();

				String trendlineobjName = "";
				String trendlineobjColumn = "";
				String trendlineobjType = "";
				String trendlineobjStyle = "";
				int trendlineobjThickness = 0;
				String trendlineobjColor = "";
				int trendlineobjKey = 0;

				if(trendLinePropertiesMap!=null && !trendLinePropertiesMap.isEmpty()){
					trendlineobjName = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineName();
					trendlineobjColumn = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineColumn();
					trendlineobjType = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineType();
					trendlineobjStyle = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineStyle();
					trendlineobjThickness = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineThickness();
					trendlineobjColor = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineColor();
					trendlineobjKey = Integer.parseInt(strTrendlineobjkey);
				}
				
				if(trendlineobjColumn != null)
				{
					String[] splitString = trendlineobjColumn.split(",");
					trendlineobjColumn = splitString[0];
				}
				
				Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
				if(propertyMap != null) {
					/*@SuppressWarnings("rawtypes")
					Iterator itr = propertyMap.keySet().iterator();
					while (itr.hasNext()) {*/
						String key = "trendColumnsList";
						modelMap.put(key, propertyMap.get(key));
					//}
				}

				modelMap.put("trendlineobjName",trendlineobjName);
				modelMap.put("trendlineobjColumn",trendlineobjColumn);
				modelMap.put("trendlineobjType",trendlineobjType);
				modelMap.put("trendlineobjStyle",trendlineobjStyle);
				modelMap.put("trendlineobjThickness",trendlineobjThickness);
				modelMap.put("trendlineobjColor",trendlineobjColor);
				modelMap.put("trendlineobjKey",trendlineobjKey);
			}
		}
		modelMap.put("isSmarten",true);
		auditUserActionLog(ResourceManager.getString("LBL_EDIT_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("kpi/editTrendTrendLine");
	}
	
	@RequestMapping (value = "/addTrendLinePropertiesForCombinedGraph")
	@ResponseBody
	public ModelAndView addTrendLinePropertiesForCombinedGraph(ModelMap modelMap,@RequestParam("trendLineName") String strTrendLineName
			,@RequestParam("trendLineColumn") String strTrendLineColumn			
			,@RequestParam("trendLineType") String strTrendLineType
			,@RequestParam("trendLineStyle") String strTrendLineStyle
			,@RequestParam("trendLineThickness") String strtrendLineThickness
			,@RequestParam("trendLineColor") String strtrendLineColor
			,@RequestParam("fromGraph") String strfromGraph,@LoggedInUser UserInfo userInfo) {
		String objectview = "";
		if(strfromGraph!= null && !strfromGraph.equalsIgnoreCase("")) {
			TrendLineProperties trendlineProperties = new TrendLineProperties();
			if(strTrendLineName != null && !strTrendLineName.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineName(strTrendLineName);
			}
			if(strTrendLineColumn != null && !strTrendLineColumn.equalsIgnoreCase(""))
			{
				//DDVM
				HashtableEx ddvmMap = new HashtableEx();
				ddvmMap = (HashtableEx) getActiveDDVMs(userInfo.getUserId());
				Vector packDisplayList=null;
				if(ddvmMap != null && ddvmMap.size() > 0)
				{
					
					if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbBarrowLabel()))
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbBarrowLabel());
					}
					else if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbLinerowLabel()))
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbLinerowLabel());
					}
					else if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbLinecolLabel())) 
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbLinecolLabel());
					}
					else if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbBarcolLabel())) 
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbBarcolLabel());
					}
				}
				String[] splitString = (strTrendLineColumn).split(",");
				if (packDisplayList != null && packDisplayList.size() > 0) 
				{

						for (Iterator iterator = packDisplayList.iterator(); iterator.hasNext();) 
						{
							String[] values = (String[]) iterator.next();															
							if (values[1].equalsIgnoreCase(splitString[0]))
							{
								strTrendLineColumn =  values[0]+","+values[0];
								break;
							}
						}
				}
				//trendlineProperties.setTrendLineColumn(strTrendLineColumn);
				trendlineProperties.setTrendLineColumn(strTrendLineColumn);
			}
			if(strTrendLineType != null && !strTrendLineType.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineType(strTrendLineType);
			}
			if(strTrendLineStyle != null && !strTrendLineStyle.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineStyle(strTrendLineStyle);
			}
			if(strtrendLineThickness != null && !strtrendLineThickness.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineThickness(Integer.parseInt(strtrendLineThickness));
			}
			if(strtrendLineColor != null && !strtrendLineColor.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineColor(strtrendLineColor);
			}
			Map<Integer,TrendLineProperties> trendLinePropertiesMap  = null;			
			if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
				if(graphInfo.getGraphProperties().getBartrendlinePropertiesMap()!= null && !graphInfo.getGraphProperties().getBartrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getBartrendlinePropertiesMap();
				}
				else
				{
					trendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
				}
				graphInfo.getGraphProperties().setBartrendlinePropertiesMap(trendLinePropertiesMap);	
				modelMap.put("bartrendlinePropertiesMap",trendLinePropertiesMap);
				objectview = "/barTrendLineObjects";
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
				if(graphInfo.getGraphProperties().getLinetrendlinePropertiesMap()!= null && !graphInfo.getGraphProperties().getLinetrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getLinetrendlinePropertiesMap();
				}
				else
				{
					trendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
				}
				graphInfo.getGraphProperties().setLinetrendlinePropertiesMap(trendLinePropertiesMap);	
				modelMap.put("linetrendlinePropertiesMap",trendLinePropertiesMap);
				objectview = "/lineTrendLineObjects";
			}				
			if(trendLinePropertiesMap!= null)
			{
				trendLinePropertiesMap.put(trendLinePropertiesMap.size()+1,trendlineProperties);
			}						
		}
		auditUserActionLog(ResourceManager.getString("LBL_ADD_COMBINE_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		modelMap.put("isGraphProperties",true);		
		return new ModelAndView("graph"+objectview);
	}	
	
	@RequestMapping (value = "/removeTrendLinPropObjForCombinedGraph")
	@ResponseBody
	public ModelAndView removeTrendLinPropObjForCombinedGraph(ModelMap modelMap,@RequestParam("trendlineobjkey") String strTrendlineobjkey,
			@RequestParam("fromGraph") String strfromGraph,@LoggedInUser UserInfo userInfo)	{
		Map<Integer,TrendLineProperties> trendLinePropertiesMap = null;	
		String objectview = "";
		if(strTrendlineobjkey != null && !strTrendlineobjkey.equalsIgnoreCase("") && strfromGraph != null && !strfromGraph.equalsIgnoreCase("")){
			if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
				if(graphInfo.getGraphProperties().getBartrendlinePropertiesMap() != null && !graphInfo.getGraphProperties().getBartrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getBartrendlinePropertiesMap();
					trendLinePropertiesMap.remove(Integer.parseInt(strTrendlineobjkey));				
					
					int i = 0;
					Map<Integer,TrendLineProperties>  tempTrendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
					
					Iterator iterator = graphInfo.getGraphProperties().getBartrendlinePropertiesMap().keySet().iterator();
					 while (iterator.hasNext()) {
					     int key = (int) iterator.next();
						tempTrendLinePropertiesMap.put(i, graphInfo.getGraphProperties().getBartrendlinePropertiesMap().get(key));
						i++;
					}
					 modelMap.put("bartrendlinePropertiesMap",tempTrendLinePropertiesMap);
						objectview = "/barTrendLineObjects";
					
					graphInfo.getGraphProperties().setBartrendlinePropertiesMap(tempTrendLinePropertiesMap);
				}
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
				if(graphInfo.getGraphProperties().getLinetrendlinePropertiesMap() != null && !graphInfo.getGraphProperties().getLinetrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getLinetrendlinePropertiesMap();
					trendLinePropertiesMap.remove(Integer.parseInt(strTrendlineobjkey));		
					
					int i = 0;
					Map<Integer,TrendLineProperties>  tempTrendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
					
					Iterator iterator = graphInfo.getGraphProperties().getLinetrendlinePropertiesMap().keySet().iterator();
					 while (iterator.hasNext()) {
					     int key = (int) iterator.next();
						tempTrendLinePropertiesMap.put(i, graphInfo.getGraphProperties().getLinetrendlinePropertiesMap().get(key));
						i++;
					}
					 graphInfo.getGraphProperties().setLinetrendlinePropertiesMap(tempTrendLinePropertiesMap);
					modelMap.put("linetrendlinePropertiesMap",tempTrendLinePropertiesMap);
						
					objectview = "/lineTrendLineObjects";
				}
			}
		}
		modelMap.put("trendlinePropertiesMap",trendLinePropertiesMap);
		modelMap.put("isGraphProperties",true);
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_COMBINE_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph"+objectview);
		//return new ModelAndView("kpi/trendlinePropobjects");
	}

	@RequestMapping (value = "/editTrendLinePropObjForCombinedGraph")
	@ResponseBody
	public ModelAndView editTrendLinePropObjForCombinedGraph(ModelMap modelMap,@RequestParam("trendlineobjkey") String strTrendlineobjkey,
			@RequestParam("fromGraph") String strfromGraph,
			@LoggedInUser UserInfo userInfo)	{
		Map<Integer,TrendLineProperties> trendLinePropertiesMap = null;	
		String objectview = "";
		if(strTrendlineobjkey != null && !strTrendlineobjkey.equalsIgnoreCase("") && strfromGraph != null && !strfromGraph.equalsIgnoreCase("")){
			if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
				if(graphInfo.getGraphProperties().getBartrendlinePropertiesMap() != null && !graphInfo.getGraphProperties().getBartrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getBartrendlinePropertiesMap();
					
					String trendlineobjName = "";
					String trendlineobjColumn = "";
					String trendlineobjType = "";
					String trendlineobjStyle = "";
					int trendlineobjThickness = 0;
					String trendlineobjColor = "";
					int trendlineobjKey = 0;

					if(trendLinePropertiesMap!=null && !trendLinePropertiesMap.isEmpty()){
						trendlineobjName = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineName();
						trendlineobjColumn = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineColumn();
						trendlineobjType = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineType();
						trendlineobjStyle = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineStyle();
						trendlineobjThickness = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineThickness();
						trendlineobjColor = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineColor();
						trendlineobjKey = Integer.parseInt(strTrendlineobjkey);
					}
					
					if(trendlineobjColumn != null)
					{
						String[] splitString = trendlineobjColumn.split(",");
						trendlineobjColumn = splitString[0];
					}
					
					Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
					if(propertyMap != null) {
						/*@SuppressWarnings("rawtypes")
						Iterator itr = propertyMap.keySet().iterator();
						while (itr.hasNext()) {*/
							String key = "trendColumnsList";
							modelMap.put(key, propertyMap.get(key));
						//}
					}

					modelMap.put("trendlineobjName",trendlineobjName);
					modelMap.put("trendlineobjColumn",trendlineobjColumn);
					modelMap.put("trendlineobjType",trendlineobjType);
					modelMap.put("trendlineobjStyle",trendlineobjStyle);
					modelMap.put("trendlineobjThickness",trendlineobjThickness);
					modelMap.put("trendlineobjColor",trendlineobjColor);
					modelMap.put("trendlineobjKey",trendlineobjKey);
					
					objectview = "/editBarTrendLine";
				}
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
				if(graphInfo.getGraphProperties().getLinetrendlinePropertiesMap() != null && !graphInfo.getGraphProperties().getLinetrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getLinetrendlinePropertiesMap();

					String trendlineobjName = "";
					String trendlineobjColumn = "";
					String trendlineobjType = "";
					String trendlineobjStyle = "";
					int trendlineobjThickness = 0;
					String trendlineobjColor = "";
					int trendlineobjKey = 0;

					if(trendLinePropertiesMap!=null && !trendLinePropertiesMap.isEmpty()){
						trendlineobjName = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineName();
						trendlineobjColumn = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineColumn();
						trendlineobjType = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineType();
						trendlineobjStyle = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineStyle();
						trendlineobjThickness = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineThickness();
						trendlineobjColor = trendLinePropertiesMap.get(Integer.parseInt(strTrendlineobjkey)).getTrendLineColor();
						trendlineobjKey = Integer.parseInt(strTrendlineobjkey);
					}
					
					if(trendlineobjColumn != null)
					{
						String[] splitString = trendlineobjColumn.split(",");
						trendlineobjColumn = splitString[0];
					}
					
					Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
					if(propertyMap != null) {
						/*@SuppressWarnings("rawtypes")
						Iterator itr = propertyMap.keySet().iterator();
						while (itr.hasNext()) {*/
							String key = "trendColumnsLineList";
							modelMap.put(key, propertyMap.get(key));
						//}
					}

					modelMap.put("trendlineobjName",trendlineobjName);
					modelMap.put("trendlineobjColumn",trendlineobjColumn);
					modelMap.put("trendlineobjType",trendlineobjType);
					modelMap.put("trendlineobjStyle",trendlineobjStyle);
					modelMap.put("trendlineobjThickness",trendlineobjThickness);
					modelMap.put("trendlineobjColor",trendlineobjColor);
					modelMap.put("trendlineobjKey",trendlineobjKey);
					
					objectview = "/editLineTrendLine";
				}
			}
		}
		auditUserActionLog(ResourceManager.getString("LBL_EDIT_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph"+objectview);
	}
	
	@RequestMapping (value = "/updateTrendLinePropObjForCombined")
	@ResponseBody
	public ModelAndView updateTrendLinePropObjForCombined(ModelMap modelMap,@RequestParam("trendLineKey") String strTrendLineKey
			,@RequestParam("trendLineName") String strTrendLineName
			,@RequestParam("trendLineColumn") String strTrendLineColumn			
			,@RequestParam("trendLineType") String strTrendLineType
			,@RequestParam("trendLineStyle") String strTrendLineStyle
			,@RequestParam("trendLineThickness") String strtrendLineThickness
			,@RequestParam("trendLineColor") String strtrendLineColor
			,@RequestParam("fromGraph") String strfromGraph,@LoggedInUser UserInfo userInfo) {
		String objectview = "";
		if(strfromGraph!= null && !strfromGraph.equalsIgnoreCase("")) {
			TrendLineProperties trendlineProperties = new TrendLineProperties();
			if(strTrendLineName != null && !strTrendLineName.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineName(strTrendLineName);
			}
			if(strTrendLineColumn != null && !strTrendLineColumn.equalsIgnoreCase(""))
			{
				//DDVM
				HashtableEx ddvmMap = new HashtableEx();
				ddvmMap = (HashtableEx) getActiveDDVMs(userInfo.getUserId());
				Vector packDisplayList=null;
				if(ddvmMap != null && ddvmMap.size() > 0)
				{
					
					if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbBarrowLabel()))
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbBarrowLabel());
					}
					else if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbLinerowLabel()))
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbLinerowLabel());
					}
					else if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbLinecolLabel())) 
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbLinecolLabel());
					}
					else if(ddvmMap.containsKey(graphInfo.getGraphData().getCmbBarcolLabel())) 
					{
						packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getCmbBarcolLabel());
					}
				}
				String[] splitString = (strTrendLineColumn).split(",");
				if (packDisplayList != null && packDisplayList.size() > 0) 
				{

						for (Iterator iterator = packDisplayList.iterator(); iterator.hasNext();) 
						{
							String[] values = (String[]) iterator.next();															
							if (values[1].equalsIgnoreCase(splitString[0]))
							{
								strTrendLineColumn =  values[0]+","+values[0];
								break;
							}
						}
				}
				//trendlineProperties.setTrendLineColumn(strTrendLineColumn);
				trendlineProperties.setTrendLineColumn(strTrendLineColumn);
			}
			if(strTrendLineType != null && !strTrendLineType.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineType(strTrendLineType);
			}
			if(strTrendLineStyle != null && !strTrendLineStyle.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineStyle(strTrendLineStyle);
			}
			if(strtrendLineThickness != null && !strtrendLineThickness.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineThickness(Integer.parseInt(strtrendLineThickness));
			}
			if(strtrendLineColor != null && !strtrendLineColor.equalsIgnoreCase(""))
			{
				trendlineProperties.setTrendLineColor(strtrendLineColor);
			}
			Map<Integer,TrendLineProperties> trendLinePropertiesMap  = null;			
			if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
				if(graphInfo.getGraphProperties().getBartrendlinePropertiesMap()!= null && !graphInfo.getGraphProperties().getBartrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getBartrendlinePropertiesMap();
				}
				else
				{
					trendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
				}
				graphInfo.getGraphProperties().setBartrendlinePropertiesMap(trendLinePropertiesMap);	
				modelMap.put("bartrendlinePropertiesMap",trendLinePropertiesMap);
				objectview = "/barTrendLineObjects";
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
				if(graphInfo.getGraphProperties().getLinetrendlinePropertiesMap()!= null && !graphInfo.getGraphProperties().getLinetrendlinePropertiesMap().isEmpty())
				{
					trendLinePropertiesMap = graphInfo.getGraphProperties().getLinetrendlinePropertiesMap();
				}
				else
				{
					trendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
				}
				graphInfo.getGraphProperties().setLinetrendlinePropertiesMap(trendLinePropertiesMap);	
				modelMap.put("linetrendlinePropertiesMap",trendLinePropertiesMap);
				objectview = "/lineTrendLineObjects";
			}				
			if(trendLinePropertiesMap!= null)
			{
				if(strTrendLineKey != null && !strTrendLineKey.equalsIgnoreCase(""))
				{
					trendLinePropertiesMap.put(Integer.parseInt(strTrendLineKey),trendlineProperties);
				}
			}						
		}
		auditUserActionLog(ResourceManager.getString("LBL_ADD_COMBINE_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		modelMap.put("isGraphProperties",true);		
		return new ModelAndView("graph"+objectview);
	}
	
	@RequestMapping (value = "/updateTrendLineProperties")
	@ResponseBody
	public ModelAndView updateTrendLineProperties(ModelMap modelMap,@RequestParam("trendLineKey") String strTrendLineKey
			,@RequestParam("trendLineName") String strTrendLineName
			,@RequestParam("trendLineColumn") String strTrendLineColumn			
			,@RequestParam("trendLineType") String strTrendLineType
			,@RequestParam("trendLineStyle") String strTrendLineStyle
			,@RequestParam("trendLineThickness") String strtrendLineThickness
			,@RequestParam("trendLineColor") String strtrendLineColor,@LoggedInUser UserInfo userInfo) {
		
		TrendLineProperties trendlineProperties = new TrendLineProperties();
		if(strTrendLineName != null && !strTrendLineName.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineName(strTrendLineName);
		}
		if(strTrendLineColumn != null && !strTrendLineColumn.equalsIgnoreCase(""))
		{
			//trendlineProperties.setTrendLineColumn(strTrendLineColumn);
			//check with ddvm
			//DDVM
			HashtableEx ddvmMap = new HashtableEx();
			ddvmMap = (HashtableEx) getActiveDDVMs(userInfo.getUserId());
			Vector packDisplayList=null;
			if(ddvmMap != null && ddvmMap.size() > 0)
			{
				if(ddvmMap.containsKey(graphInfo.getGraphData().getRowLabel())) 
				{
					packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getRowLabel());
				}
				else if(ddvmMap.containsKey(graphInfo.getGraphData().getColLabel()))
				{
					packDisplayList = (Vector) ddvmMap.get(graphInfo.getGraphData().getColLabel());
				}
			}
			String[] splitString = (strTrendLineColumn).split(",");
			if (packDisplayList != null && packDisplayList.size() > 0) 
			{

					for (Iterator iterator = packDisplayList.iterator(); iterator.hasNext();) 
					{
						String[] values = (String[]) iterator.next();															
						if (values[1].equalsIgnoreCase(splitString[0]))
						{
							strTrendLineColumn =  values[0]+","+values[0];
							break;
						}
					}
			}
			trendlineProperties.setTrendLineColumn(strTrendLineColumn);
			
		}
		if(strTrendLineType != null && !strTrendLineType.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineType(strTrendLineType);
		}
		if(strTrendLineStyle != null && !strTrendLineStyle.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineStyle(strTrendLineStyle);
		}
		if(strtrendLineThickness != null && !strtrendLineThickness.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineThickness(Integer.parseInt(strtrendLineThickness));
		}
		if(strtrendLineColor != null && !strtrendLineColor.equalsIgnoreCase(""))
		{
			trendlineProperties.setTrendLineColor(strtrendLineColor);
		}
		Map<Integer,TrendLineProperties> trendLinePropertiesMap  = null;
		if(graphInfo.getGraphProperties().getTrendlinePropertiesMap()!= null && !graphInfo.getGraphProperties().getTrendlinePropertiesMap().isEmpty())
		{
			trendLinePropertiesMap = graphInfo.getGraphProperties().getTrendlinePropertiesMap();
		}
		else
		{
			trendLinePropertiesMap = new HashMap<Integer,TrendLineProperties>();
		}
		if(trendLinePropertiesMap!= null)
		{
			if(strTrendLineKey != null && !strTrendLineKey.equalsIgnoreCase(""))
			{
				trendLinePropertiesMap.put(Integer.parseInt(strTrendLineKey),trendlineProperties);
			}
		}
		
		graphInfo.getGraphProperties().setTrendlinePropertiesMap(trendLinePropertiesMap);	
		modelMap.put("trendlinePropertiesMap",trendLinePropertiesMap);	
		modelMap.put("isGraphProperties",true);
		modelMap.put("isSmarten",true);
		auditUserActionLog(ResourceManager.getString("LBL_UPDATE_GRAPH_TREND_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("kpi/trendlinePropobjects");
	}
	
	@RequestMapping (value = "/addReferenceLineProperties")
	@ResponseBody
	public ModelAndView addReferenceLineProperties(ModelMap modelMap,@RequestParam("referencelinename") String strReferencelinename
			,@RequestParam("referencelinevalue") String strReferencelinevalue	
			,@RequestParam("referencelinestyle") String strReferencelinestyle
			,@RequestParam("referencelinewidth") String strReferencelinewidth
			,@RequestParam("referencelinecolor") String strReferencelinecolor
			,@RequestParam("fromGraph") String strfromGraph,@LoggedInUser UserInfo userInfo) {
		if(strfromGraph != null && !strfromGraph.equalsIgnoreCase("")) {
			ReferenceLine referencelineProperties = new ReferenceLine();
			String objectsview = "";
			if(strReferencelinename != null && !strReferencelinename.equalsIgnoreCase(""))
			{
				referencelineProperties.setLabel(strReferencelinename);
			}
			if(strReferencelinevalue != null && !strReferencelinevalue.equalsIgnoreCase(""))
			{
				referencelineProperties.setValue(strReferencelinevalue);
			}
			if(strReferencelinestyle != null && !strReferencelinestyle.equalsIgnoreCase(""))
			{
				referencelineProperties.setStyle(strReferencelinestyle);
			}
			if(strReferencelinewidth != null && !strReferencelinewidth.equalsIgnoreCase(""))
			{
				referencelineProperties.setWidth(Integer.parseInt(strReferencelinewidth));
			}
			if(strReferencelinecolor != null && !strReferencelinecolor.equalsIgnoreCase(""))
			{
				referencelineProperties.setColor(strReferencelinecolor);
			}		
			Map<Integer,ReferenceLine> referencelinePropertiesMap  = null;
			
				if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_GENERAL)) {
					if(graphInfo.getGraphProperties().getReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getReferencelinePropertiesMap().isEmpty()) {
						referencelinePropertiesMap = graphInfo.getGraphProperties().getReferencelinePropertiesMap();
					}
					else
					{
						referencelinePropertiesMap = new HashMap<Integer, ReferenceLine>();
					}
				}
				else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
					if(graphInfo.getGraphProperties().getBarReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getBarReferencelinePropertiesMap().isEmpty()) {
						referencelinePropertiesMap = graphInfo.getGraphProperties().getBarReferencelinePropertiesMap();
					}
					else
					{
						referencelinePropertiesMap = new HashMap<Integer, ReferenceLine>();
					}
				}
				else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
					if(graphInfo.getGraphProperties().getLineReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getLineReferencelinePropertiesMap().isEmpty()){
						referencelinePropertiesMap = graphInfo.getGraphProperties().getLineReferencelinePropertiesMap();
					}
					else
					{
						referencelinePropertiesMap = new HashMap<Integer, ReferenceLine>();
					}
				}			
			if(referencelinePropertiesMap!= null)
			{
				referencelinePropertiesMap.put(referencelinePropertiesMap.size()+1,referencelineProperties);
			}
			if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_GENERAL)) {						
				modelMap.put("referencelinePropertiesMap",referencelinePropertiesMap);					
				graphInfo.getGraphProperties().setReferencelinePropertiesMap(referencelinePropertiesMap);
				objectsview = "/referencelineobjects";
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR))
			{
				modelMap.put("barreferencelinePropertiesMap",referencelinePropertiesMap);				
				graphInfo.getGraphProperties().setBarReferencelinePropertiesMap(referencelinePropertiesMap);	
				objectsview = "/barReferencelineobjects";
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE))
			{
				modelMap.put("linereferencelinePropertiesMap",referencelinePropertiesMap);				
				graphInfo.getGraphProperties().setLineReferencelinePropertiesMap(referencelinePropertiesMap);
				objectsview = "/lineReferencelineobjects";
			}	
			auditUserActionLog(ResourceManager.getString("LBL_ADD_GRAPH_REFERENCE_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
			return new ModelAndView("graph"+objectsview);
		}
		else
		{
			Map<Integer,ReferenceLine> referencelinePropertiesMap  = new HashMap<Integer, ReferenceLine>();
			modelMap.put("referencelinePropertiesMap",referencelinePropertiesMap);
			auditUserActionLog(ResourceManager.getString("LBL_ADD_GRAPH_REFERENCE_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
			return new ModelAndView("graph/referencelineobjects");
		}
	}
	
	@RequestMapping (value = "/removeReferenceLinePropObj")
	@ResponseBody
	public ModelAndView removeReferenceLinePropObj(ModelMap modelMap,@RequestParam("referencelineobjkey") String strReferencelineobjkey,@RequestParam("fromGraph") String strfromGraph,@LoggedInUser UserInfo userInfo)	{
		Map<Integer,ReferenceLine> referencelinePropertiesMap = null;	
		String objectsview = "";
		if(strReferencelineobjkey != null && !strReferencelineobjkey.equalsIgnoreCase("")){		
				if(strfromGraph != null && !strfromGraph.equalsIgnoreCase(""))
				{
					if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_GENERAL)) {
						if(graphInfo.getGraphProperties().getReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getReferencelinePropertiesMap().isEmpty()){
							referencelinePropertiesMap = graphInfo.getGraphProperties().getReferencelinePropertiesMap();
							referencelinePropertiesMap.remove(Integer.parseInt(strReferencelineobjkey));		
							
							int i = 0;
							Map<Integer,ReferenceLine>  tempRefrenceLinePropertiesMap = new HashMap<Integer,ReferenceLine>();
							Iterator iterator =graphInfo.getGraphProperties().getReferencelinePropertiesMap().keySet().iterator();
							 while (iterator.hasNext()) {
								 int key = (int) iterator.next();
							     tempRefrenceLinePropertiesMap.put(i, graphInfo.getGraphProperties().getReferencelinePropertiesMap().get(key));
								i++;
							}
							
							graphInfo.getGraphProperties().setReferencelinePropertiesMap(tempRefrenceLinePropertiesMap);
							modelMap.put("referencelinePropertiesMap",referencelinePropertiesMap);
							objectsview = "/referencelineobjects";
						}
					}
					else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
						if(graphInfo.getGraphProperties().getBarReferencelinePropertiesMap()!=null && !graphInfo.getGraphProperties().getBarReferencelinePropertiesMap().isEmpty()){
							referencelinePropertiesMap = graphInfo.getGraphProperties().getBarReferencelinePropertiesMap();
							referencelinePropertiesMap.remove(Integer.parseInt(strReferencelineobjkey));		
							
							int i = 0;
							Map<Integer,ReferenceLine>  tempRefrenceLinePropertiesMap = new HashMap<Integer,ReferenceLine>();
							Iterator iterator = graphInfo.getGraphProperties().getBarReferencelinePropertiesMap().keySet().iterator();
							 while (iterator.hasNext()) {
								 int key = (int) iterator.next();
							     tempRefrenceLinePropertiesMap.put(i, graphInfo.getGraphProperties().getBarReferencelinePropertiesMap().get(key));
								i++;
							}
							
							graphInfo.getGraphProperties().setBarReferencelinePropertiesMap(tempRefrenceLinePropertiesMap);
							modelMap.put("barreferencelinePropertiesMap",tempRefrenceLinePropertiesMap);
							objectsview = "/barReferencelineobjects";
						}
					}
					else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
						if(graphInfo.getGraphProperties().getLineReferencelinePropertiesMap()!=null && !graphInfo.getGraphProperties().getLineReferencelinePropertiesMap().isEmpty())
						{
							referencelinePropertiesMap = graphInfo.getGraphProperties().getLineReferencelinePropertiesMap();
							referencelinePropertiesMap.remove(Integer.parseInt(strReferencelineobjkey));
							
							int i = 0;
							Map<Integer,ReferenceLine>  tempRefrenceLinePropertiesMap = new HashMap<Integer,ReferenceLine>();
							Iterator iterator = graphInfo.getGraphProperties().getLineReferencelinePropertiesMap().keySet().iterator();
							 while (iterator.hasNext()) {
								 int key = (int) iterator.next();
							     tempRefrenceLinePropertiesMap.put(i, graphInfo.getGraphProperties().getLineReferencelinePropertiesMap().get(key));
								i++;
							}
							 
							graphInfo.getGraphProperties().setLineReferencelinePropertiesMap(tempRefrenceLinePropertiesMap);
							modelMap.put("linereferencelinePropertiesMap",tempRefrenceLinePropertiesMap);
							objectsview = "/lineReferencelineobjects";
						}
					}										
				}							
		}	
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_GRAPH_REFERENCE_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph"+objectsview);
	}
	
	/*This code block is for the smartenLabelProperties on the left pannel*/
	@RequestMapping (value = "/saveSmartenLabelProperties")
	@ResponseBody
	public Object saveSmartenLabelProperties(@ModelAttribute GraphProperties graphProperties
			,@RequestParam(value = "measureIndex", required = false) int measureIndex
			,@RequestParam(value = "measureName", required = false) String measureName
			,@RequestParam(required = false, value="rangeColorList") String rangeColorList
			,ModelMap map, @RequestParam(value = "isFromFilter", required = false) boolean isFromFilter, HttpServletResponse response, HttpServletRequest request,@LoggedInUser UserInfo userInfo)
	{
		long setOutlinerStart = System.currentTimeMillis();
		map.put("setOutlinerStart", setOutlinerStart);
		LinkedHashMap<String, YaxisTrendProperties> yAxisPropMap  = new LinkedHashMap(graphProperties.getyAxisPropertiesMap());
		Iterator<Entry<String, YaxisTrendProperties>> entries = yAxisPropMap.entrySet().iterator();
		String key="";
		graphInfo.setQuickSettingsNumberFormat(true);
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			key = entry.getKey().toString();
		}
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),null,"save SmartenLabel Properties",Thread.currentThread(),userInfo,null);
		YaxisTrendProperties yProperties = graphProperties.getyAxisPropertiesMap().get(key) ;

		//24thFeb 2017
		Map<String, YaxisTrendProperties> yaxisMap = graphInfo.getGraphProperties().getyAxisPropertiesMap();

		LinkedHashMap<String, YaxisTrendProperties> finYAxisPropertyMap = new LinkedHashMap<String, YaxisTrendProperties>();
		//graphInfo.getGraphData().setValueAxisPositionList(new ArrayList());
		
		//When user changes color type the smart color selection sets to false
		if(graphInfo.getGraphType() != GraphConstants.SMARTENVIEW_TABULAR)
		{
			if(graphProperties.getColorType() != graphInfo.getGraphProperties().getColorType())
				graphInfo.setSmartenColorAutoCustom(false);
			if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH
				&& graphProperties.getLineColorType() != graphInfo.getGraphProperties().getLineColorType())//Added for Cmb line Auto (Bug 15094)
			{
				graphInfo.setSmartenColorAutoCustom(false);
			}
		}
		 String data = request.getParameter("quickSettingsParamMeasure");
		 ArrayList<String> list = new ArrayList<>();
	      
		 
		 if(null != data && !data.equals("")) {
	        String[] elements = data.split(",");

	      
	        for (String element : elements) {
	            
	            String cleaned = element.trim().replaceFirst("(?i)measure", "");
	            list.add(cleaned);
	        }
		 }
		 else {
	        if(measureIndex == -1) {
	        	list = new ArrayList<>(graphInfo.getDataColLabels3());
	        	
	        } 
	        else {
	        	if(graphInfo.getDataColLabels3().size() > measureIndex && null != graphInfo.getDataColLabels3().get(measureIndex)) {
	        	String temp = graphInfo.getDataColLabels3().get(measureIndex).toString();
	        	list.add(temp);
	        	}
	        	//list = new ArrayList<>(graphInfo.getDataColLabels3().get(measureteIndex));
	        }
	       }
	        
if(list != null ) {
		for (int i = 0; i < list.size(); i++) {

			YaxisTrendProperties yAxisTrendProperty = yaxisMap.get("M" + i);

			if (yAxisTrendProperty != null) {
				yAxisTrendProperty.setTabDisplayColumnName(graphInfo.getDataColLabels3().get(i).toString());
				yAxisTrendProperty.setTabMesureName(graphInfo.getDataColLabels3().get(i).toString());

				if ( measureIndex==-1 &&/*graphInfo.getDataColLabels3().get(i).toString().equals(list.get(i))&&*/ !(key.isEmpty())) {
					yAxisTrendProperty
							.setLabelProperties(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties());
					yAxisTrendProperty
							.setLineProperties(graphProperties.getyAxisPropertiesMap().get(key).getLineProperties());

					// line visible off along with ticklength and values
					if (graphProperties.getyAxisPropertiesMap().get(key).getLineProperties().isVisible()) {
						yAxisTrendProperty.getyAxisTitleTrendProperties().setVisible(true);
						yAxisTrendProperty.getLabelProperties().setVisible(true);
						yAxisTrendProperty.getLineProperties().setVisible(true);
						yAxisTrendProperty.getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
					} else {
						//yAxisTrendProperty.getyAxisTitleTrendProperties().setVisible(false);
						//yAxisTrendProperty.getLabelProperties().setVisible(false);
						yAxisTrendProperty.getLineProperties().setVisible(false);
						yAxisTrendProperty.getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(false);
					}
					// line visible off along with ticklength and values

				}
				else if (graphInfo.getDataColLabels3().get(i).toString().equals(list.get(i)) && !(key.isEmpty())) {
					yAxisTrendProperty
							.setLabelProperties(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties());
					yAxisTrendProperty
							.setLineProperties(graphProperties.getyAxisPropertiesMap().get(key).getLineProperties());

					// line visible off along with ticklength and values
					if (graphProperties.getyAxisPropertiesMap().get(key).getLineProperties().isVisible()) {
						yAxisTrendProperty.getyAxisTitleTrendProperties().setVisible(true);
						yAxisTrendProperty.getLabelProperties().setVisible(true);
						yAxisTrendProperty.getLineProperties().setVisible(true);
						yAxisTrendProperty.getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
					} else {
						yAxisTrendProperty.getyAxisTitleTrendProperties().setVisible(false);
						yAxisTrendProperty.getLabelProperties().setVisible(false);
						yAxisTrendProperty.getLineProperties().setVisible(false);
						yAxisTrendProperty.getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(false);
					}
				}
				finYAxisPropertyMap.put("M" + i, yAxisTrendProperty);
			}
		}
	}
		//Check for  measureIndex != -1 added to solve bug [13979] issue was without selecting the measure from dropdown when user changes any property it was getting reflected
		
		if(graphInfo.getGraphType() != GraphConstants.COMBINED_GRAPH && yaxisMap!=null && graphInfo.getGraphType() != GraphConstants.PIE_GRAPH && measureIndex != -1 && null==list)
		{
			int noOfMeasure = graphInfo.getDataColLabels3().size();
			/*if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
				noOfMeasure = 1;//As it will have only one value axis irrespective of no of dim set to 2 or 3
*/
			for (int i=0; i<noOfMeasure; i++) {
				YaxisTrendProperties yAxisTrendProperty = yaxisMap.get("M"+i);
				if(yAxisTrendProperty!=null)
				{
				yAxisTrendProperty.setTabDisplayColumnName(graphInfo.getDataColLabels3().get(i).toString());
				yAxisTrendProperty.setTabMesureName(graphInfo.getDataColLabels3().get(i).toString());
				if(graphInfo.isOutlinerFirstTime())
				{
					/*yAxisTrendProperty.getyAxisTitleTrendProperties().getFontProperties().setFontWeight("bold");
					yAxisTrendProperty.getyAxisTitleTrendProperties().getFontProperties().setFontSize(14);*/
					/*yAxisTrendProperty.getLineProperties().setVisible(true);
					yAxisTrendProperty.getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
					yAxisTrendProperty.getyAxisTitleTrendProperties().setVisible(true);
					yAxisTrendProperty.getLabelProperties().setVisible(true);*/
				}
				if(measureIndex == i && !(key.isEmpty()))
				{
					yAxisTrendProperty.setLabelProperties(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties());
					yAxisTrendProperty.setLineProperties(graphProperties.getyAxisPropertiesMap().get(key).getLineProperties());
					
					//line visible off along with ticklength and values
					if(graphProperties.getyAxisPropertiesMap().get(key).getLineProperties().isVisible())
					{
						yAxisTrendProperty.getyAxisTitleTrendProperties().setVisible(true);
						yAxisTrendProperty.getLabelProperties().setVisible(true);
						yAxisTrendProperty.getLineProperties().setVisible(true);
						yAxisTrendProperty.getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
					}
					else
					{	
						yAxisTrendProperty.getyAxisTitleTrendProperties().setVisible(false);
						yAxisTrendProperty.getLabelProperties().setVisible(false);
						yAxisTrendProperty.getLineProperties().setVisible(false);
						yAxisTrendProperty.getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(false);
					}
					//line visible off along with ticklength and values
					
				}
				finYAxisPropertyMap.put("M"+i, yAxisTrendProperty);
			}
			}
		}
		
		//for pie
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH && measureIndex != -1)
		{
			int noOfMeasure = graphInfo.getDataColLabels3().size();
			LinkedHashMap<String, TrendDataValueProperties> dataMap  = new LinkedHashMap(graphProperties.getDataValuePropertiesMap());
			Iterator<Entry<String, TrendDataValueProperties>> entriesData = dataMap.entrySet().iterator();
			key="";
			while (entriesData.hasNext()) {
				Map.Entry entry = (Map.Entry) entriesData.next();
				key = entry.getKey().toString();
			}
			for (int i=0; i<noOfMeasure; i++) {
				if(measureIndex == i && !(key.isEmpty()))
				{
					Map<String, TrendDataValueProperties> trendMap = graphInfo.getGraphProperties().getDataValuePropertiesMap();
					TrendDataValueProperties trendProp = graphInfo.getGraphProperties().getDataValuePropertiesMap().get(key);
					trendProp.getNumberFormat().setAdjustedDigit(graphProperties.getDataValuePropertiesMap().get(key).getNumberFormat().getAdjustedDigit());
					trendProp.getNumberFormat().setCommaFormat(graphProperties.getDataValuePropertiesMap().get(key).getNumberFormat().getCommaFormat());
					trendProp.getNumberFormat().setCommaSeprator(graphProperties.getDataValuePropertiesMap().get(key).getNumberFormat().isCommaSeprator());
					trendProp.getNumberFormat().setNumberOfDigits(graphProperties.getDataValuePropertiesMap().get(key).getNumberFormat().getNumberOfDigits());
					trendProp.getNumberFormat().setShowadAdjustedSuffixed(graphProperties.getDataValuePropertiesMap().get(key).getNumberFormat().isShowadAdjustedSuffixed());
					trendMap.put(key, trendProp);
					graphInfo.getGraphProperties().setDataValuePropertiesMap(trendMap);
				}
			}
		}
		

		//For radar,heatMap etc chart with no value axis
		if((graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.HISTOGRAM_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.CANDLE_STICK_GRAPH) && measureIndex != -1)
		{
			//same prop on M0 and DataValuePoint for graphType change
			//graphInfo.getGraphProperties().setyAxisPropertiesMap(graphInfo.getGraphProperties().getyAxisPropertiesMap());
			graphInfo.getGraphProperties().getyAxisPropertiesMap().put("M0", graphInfo.getGraphProperties().getyAxisProperties());
			
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaSeprator(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties().isCommaSeprator());
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaFormat(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties().getCommaFormat());
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setAdjustedDigit(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties().getAdjustedDigit());
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setNumberOfDigits(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties().getNumberOfDigits());
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setShowadAdjustedSuffixed(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties().isShowadAdjustedSuffixed());
		}
		
		if(graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP)
		{
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaSeprator(graphProperties.getDataValueProperties().getNumberFormat().isCommaSeprator());
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaFormat(graphProperties.getDataValueProperties().getNumberFormat().getCommaFormat());
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setAdjustedDigit(graphProperties.getDataValueProperties().getNumberFormat().getAdjustedDigit());
			if(graphProperties.getDataValueProperties().getNumberFormat().getAdjustedDigit() ==999) {
				graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setAutovalue(true);		
			}else {
				graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setAutovalue(false);
			}
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setNumberOfDigits(graphProperties.getDataValueProperties().getNumberFormat().getNumberOfDigits());
			graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setShowadAdjustedSuffixed(graphProperties.getDataValueProperties().getNumberFormat().isShowadAdjustedSuffixed());
			
			graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().setCommaSeprator(graphProperties.getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().isCommaSeprator());
			graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().setCommaFormat(graphProperties.getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().getCommaFormat());
			if(graphProperties.getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().getAdjustedDigit()==999) {
				graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().setAutovalue(true);
			}else {
				graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().setAutovalue(false);
					
			}
			
			graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().setAdjustedDigit(graphProperties.getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().getAdjustedDigit());
			graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().setNumberOfDigits(graphProperties.getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().getNumberOfDigits());
			graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().setShowadAdjustedSuffixed(graphProperties.getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat().isShowadAdjustedSuffixed());
		}

		//For combined chart
		if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH && measureIndex != -1)
		{
			boolean barLineVisible = graphProperties.getCombinedYaxisProperties().getBarYaxisProperties().getLineProperties().isVisible();
			boolean lineVisible = graphProperties.getCombinedYaxisProperties().getLineYaxisProperties().getLineProperties().isVisible();
			
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBarnumberFormat().setAdjustedDigit(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().getAdjustedDigit());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBarnumberFormat().setCommaSeprator(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().isCommaSeprator());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBarnumberFormat().setCommaFormat(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().getCommaFormat());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBarnumberFormat().setNumberOfDigits(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().getNumberOfDigits());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBarnumberFormat().setShowadAdjustedSuffixed(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().isShowadAdjustedSuffixed());
			
			//Bar Yaxis visible
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getyAxisTitleTrendProperties().setVisible(barLineVisible);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setVisible(barLineVisible);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLineProperties().setVisible(barLineVisible);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(barLineVisible);
			//Bar Yaxis number
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setCommaSeprator(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().isCommaSeprator());
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setCommaFormat(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().getCommaFormat());
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setNumberOfDigits(graphProperties.getCombinedDataValueProperties().getBarnumberFormat().getNumberOfDigits());
			
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinenumberFormat().setAdjustedDigit(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().getAdjustedDigit());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinenumberFormat().setCommaSeprator(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().isCommaSeprator());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinenumberFormat().setCommaFormat(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().getCommaFormat());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinenumberFormat().setNumberOfDigits(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().getNumberOfDigits());
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinenumberFormat().setShowadAdjustedSuffixed(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().isShowadAdjustedSuffixed());
			
			//Line  Yaxis visible also  
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getyAxisTitleTrendProperties().setVisible(lineVisible);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setVisible(lineVisible);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLineProperties().setVisible(lineVisible);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(lineVisible);
			//line yaxis comma format
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setCommaSeprator(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().isCommaSeprator());
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setCommaFormat(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().getCommaFormat());
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setNumberOfDigits(graphProperties.getCombinedDataValueProperties().getLinenumberFormat().getNumberOfDigits());
		}
		
		//for XY
		if(!graphInfo.isBubbleSizeChanged() && graphInfo.getGraphProperties().getBarProperties().getBubbleSize() != graphProperties.getBarProperties().getBubbleSize())
			graphInfo.setBubbleSizeChanged(true);
		graphInfo.getGraphProperties().getBarProperties().setBubbleSize(graphProperties.getBarProperties().getBubbleSize());
		if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH && measureIndex != -1)
		{
			int noOfMeasure = graphInfo.getDataColLabels3().size();
			
			//graphInfo.getGraphProperties().getBarProperties().setBubbleSize(graphProperties.getBarProperties().getBubbleSize());
			for (int i=0; i<noOfMeasure; i++) {
				YaxisTrendProperties yAxisTrendProperty = yaxisMap.get("M"+i);
				if(yAxisTrendProperty!=null)
				{
				yAxisTrendProperty.setTabDisplayColumnName(graphInfo.getDataColLabels3().get(i).toString());
				yAxisTrendProperty.setTabMesureName(graphInfo.getDataColLabels3().get(i).toString());
				if(measureIndex == i && !(key.isEmpty()) && noOfMeasure == 3)
				{
					if(measureIndex == 0)
					{
						Map<String, YaxisTrendProperties> yMap = new HashMap();
						yMap.put("M0", yAxisTrendProperty);
						graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
					}
					if(measureIndex == 1)
					{
						graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setCommaFormat(yAxisTrendProperty.getLabelProperties().getCommaFormat());
						graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setCommaSeprator(yAxisTrendProperty.getLabelProperties().isCommaSeprator());
						graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setAdjustedDigit(yAxisTrendProperty.getLabelProperties().getAdjustedDigit());
						graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setNumberOfDigits(yAxisTrendProperty.getLabelProperties().getNumberOfDigits());
						graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setShowadAdjustedSuffixed(yAxisTrendProperty.getLabelProperties().isShowadAdjustedSuffixed());
						
						graphInfo.getGraphProperties().getxAxisProperties().getLineProperties().setVisible(yAxisTrendProperty.getLineProperties().isVisible());
						graphInfo.getGraphProperties().getxAxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(yAxisTrendProperty.getLineProperties().isVisible());
						
					}
					if(measureIndex == 2)
					{
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaFormat(yAxisTrendProperty.getLabelProperties().getCommaFormat());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaSeprator(yAxisTrendProperty.getLabelProperties().isCommaSeprator());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setAdjustedDigit(yAxisTrendProperty.getLabelProperties().getAdjustedDigit());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setNumberOfDigits(yAxisTrendProperty.getLabelProperties().getNumberOfDigits());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setShowadAdjustedSuffixed(yAxisTrendProperty.getLabelProperties().isShowadAdjustedSuffixed());
					}
					yAxisTrendProperty.setLabelProperties(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties());
					yAxisTrendProperty.setLineProperties(graphProperties.getyAxisPropertiesMap().get(key).getLineProperties());
				}
				if(measureIndex == i && !(key.isEmpty()) && noOfMeasure == 2)
				{
					if(measureIndex == 0)//y
					{
						Map<String, YaxisTrendProperties> yMap = new HashMap();
						yMap.put("M0", yAxisTrendProperty);
						graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
					}
					if(measureIndex == 1)//size
					{
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaFormat(yAxisTrendProperty.getLabelProperties().getCommaFormat());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setCommaSeprator(yAxisTrendProperty.getLabelProperties().isCommaSeprator());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setAdjustedDigit(yAxisTrendProperty.getLabelProperties().getAdjustedDigit());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setNumberOfDigits(yAxisTrendProperty.getLabelProperties().getNumberOfDigits());
						graphInfo.getGraphProperties().getDataValueProperties().getNumberFormat().setShowadAdjustedSuffixed(yAxisTrendProperty.getLabelProperties().isShowadAdjustedSuffixed());
						if(graphInfo.isXyChart())
						{
							graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setCommaFormat(yAxisTrendProperty.getLabelProperties().getCommaFormat());
							graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setCommaSeprator(yAxisTrendProperty.getLabelProperties().isCommaSeprator());
							graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setAdjustedDigit(yAxisTrendProperty.getLabelProperties().getAdjustedDigit());
							graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setNumberOfDigits(yAxisTrendProperty.getLabelProperties().getNumberOfDigits());
							graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setShowadAdjustedSuffixed(yAxisTrendProperty.getLabelProperties().isShowadAdjustedSuffixed());
							
							graphInfo.getGraphProperties().getxAxisProperties().getLineProperties().setVisible(yAxisTrendProperty.getLineProperties().isVisible());
							graphInfo.getGraphProperties().getxAxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(yAxisTrendProperty.getLineProperties().isVisible());
						}
					}
					yAxisTrendProperty.setLabelProperties(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties());
					yAxisTrendProperty.setLineProperties(graphProperties.getyAxisPropertiesMap().get(key).getLineProperties());
				}
				if(measureIndex == i && !(key.isEmpty()) && noOfMeasure == 1)
				{
					if(measureIndex == 0)//y
					{
						Map<String, YaxisTrendProperties> yMap = new HashMap();
						yMap.put("M0", yAxisTrendProperty);
						graphInfo.getGraphProperties().setyAxisPropertiesMap(yMap);
					}
					yAxisTrendProperty.setLabelProperties(graphProperties.getyAxisPropertiesMap().get(key).getLabelProperties());
					yAxisTrendProperty.setLineProperties(graphProperties.getyAxisPropertiesMap().get(key).getLineProperties());
				}
				finYAxisPropertyMap.put("M"+i, yAxisTrendProperty);
			}
			}
		}

		if(graphInfo.getGraphType() != GraphConstants.DRILLED_RADAR_GRAPH
				&& graphInfo.getGraphType() != GraphConstants.DRILLED_STACKED_RADAR_GRAPH
				&& graphInfo.getGraphType() != GraphConstants.HEAT_MAP_GRAPH
				&& graphInfo.getGraphType() != GraphConstants.HISTOGRAM_GRAPH
				&& graphInfo.getGraphType() != GraphConstants.CANDLE_STICK_GRAPH
				&& measureIndex != -1)//Added to restrict quick settings to set as default fro this graphs
			graphInfo.getGraphProperties().setyAxisPropertiesMap(finYAxisPropertyMap);
		//graphInfo.setQuickSettingsNumberFormat(true);
		graphInfo.setSelectedMeasureIndex(measureIndex);


		String quickparamtitle = request.getParameter("quickSettingsParamTitle");
		//String quickparammeasure =request.getParameter("quickSettingsParamMeasure");
		String[] quickParamtitle = quickparamtitle != null ? quickparamtitle.split(",") : null;

		
		boolean Categoryaxistitle =  false;
		boolean valueaxistitle = false;
		boolean CategoryaxislabelCB =  false;
		boolean valueaxislabelCB = false;
		boolean rowlabelCB =  false;
		boolean colsaxislabelCB = false;
		boolean legendaxislabelCB =  false;
		
		if(quickParamtitle != null) {
			Categoryaxistitle = Boolean.parseBoolean(quickParamtitle[0]);
			valueaxistitle = Boolean.parseBoolean(quickParamtitle[1]);
			CategoryaxislabelCB = Boolean.parseBoolean(quickParamtitle[2]);
			valueaxislabelCB = Boolean.parseBoolean(quickParamtitle[3]);
			rowlabelCB = Boolean.parseBoolean(quickParamtitle[4]);
			colsaxislabelCB = Boolean.parseBoolean(quickParamtitle[5]);
			legendaxislabelCB = Boolean.parseBoolean(quickParamtitle[6]);
		}
		
		
		//Title properties
		int selectedTitleDropDown = graphProperties.getSmartenProperties().getTitlesDropdown() - 1;
		int fontSizeTitle= graphProperties.getSmartenProperties().getSmartenTitleProperties().getFontProperties().getFontSize();
		String fontNameTitle = graphProperties.getSmartenProperties().getSmartenTitleProperties().getFontProperties().getFontName();
		String fontColorTitle = graphProperties.getSmartenProperties().getSmartenTitleProperties().getFontProperties().getFontColor();
		String textTransformTitle = graphProperties.getSmartenProperties().getSmartenTitleProperties().getFontProperties().getTextTransform();
		String textDecorationTitle = graphProperties.getSmartenProperties().getSmartenTitleProperties().getFontProperties().getTextDecoration();//"no-line";
		String fontWeightTitle = graphProperties.getSmartenProperties().getSmartenTitleProperties().getFontProperties().getFontWeight();
		String fontStyleTitle = graphProperties.getSmartenProperties().getSmartenTitleProperties().getFontProperties().getFontStyle();
		FontProperties fontPropertiesTitle = new FontProperties(fontSizeTitle,fontWeightTitle,fontStyleTitle,textDecorationTitle,textTransformTitle,fontColorTitle,fontNameTitle);		
		switch(selectedTitleDropDown)
		{
		case 0:
			graphInfo.getGraphProperties().getTitleProperties().setTitleFont(fontPropertiesTitle);
			//category
			graphInfo.getGraphProperties().getxAxisProperties().getxAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			//value axis
			graphInfo.getGraphProperties().getyAxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			//value axis map
			for(int i=0;i<graphInfo.getGraphProperties().getyAxisPropertiesMap().size();i++)
			{
				YaxisTrendProperties tempProperties = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i);
				fontPropertiesTitle.setTextAlignment("center");
				tempProperties.getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
				graphInfo.getGraphProperties().getyAxisPropertiesMap().put("M"+i, tempProperties);
			}

			//For combined
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);



			break;
		case 1:
			graphInfo.getGraphProperties().getTitleProperties().setTitleFont(fontPropertiesTitle);
			break;
		case 2:
			graphInfo.getGraphProperties().getxAxisProperties().getxAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			break;
		case 3:
			graphInfo.getGraphProperties().getyAxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			for(int i=0;i<graphInfo.getGraphProperties().getyAxisPropertiesMap().size();i++)
			{
				YaxisTrendProperties tempProperties = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i);
				tempProperties.getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
				graphInfo.getGraphProperties().getyAxisPropertiesMap().put("M"+i, tempProperties);
			}
			break;
		}

		if(Categoryaxistitle) {
			graphInfo.getGraphProperties().getxAxisProperties().getxAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
		}
		if(valueaxistitle) {
			graphInfo.getGraphProperties().getyAxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
			for(int i=0;i<graphInfo.getGraphProperties().getyAxisPropertiesMap().size();i++)
			{
				YaxisTrendProperties tempProperties = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i);
				tempProperties.getyAxisTitleTrendProperties().setFontProperties(fontPropertiesTitle);
				graphInfo.getGraphProperties().getyAxisPropertiesMap().put("M"+i, tempProperties);
			}
		}

		//Label Properties
		int selectedLabelDropdown = graphProperties.getSmartenProperties().getLabelsDropdown() - 1;
		int fontSizeLabel= graphProperties.getSmartenProperties().getSmartenLabelProperties().getFontProperties().getFontSize();
		String fontNameLabel = graphProperties.getSmartenProperties().getSmartenLabelProperties().getFontProperties().getFontName();
		String fontColorLabel = graphProperties.getSmartenProperties().getSmartenLabelProperties().getFontProperties().getFontColor();
		String textTransformLabel = graphProperties.getSmartenProperties().getSmartenLabelProperties().getFontProperties().getTextTransform();
		String textDecorationLabel = graphProperties.getSmartenProperties().getSmartenLabelProperties().getFontProperties().getTextDecoration();//"no-line";
		String fontWeightLabel = graphProperties.getSmartenProperties().getSmartenLabelProperties().getFontProperties().getFontWeight();
		String fontStyleLabel = graphProperties.getSmartenProperties().getSmartenLabelProperties().getFontProperties().getFontStyle();
		FontProperties fontPropertiesLabel = new FontProperties(fontSizeLabel,fontWeightLabel,fontStyleLabel,textDecorationLabel,textTransformLabel,fontColorLabel,fontNameLabel);
		switch(selectedLabelDropdown)
		{
		case 0:
			//Category
			graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			//Value
			graphInfo.getGraphProperties().getyAxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			for(int i=0;i<graphInfo.getGraphProperties().getyAxisPropertiesMap().size();i++)
			{
				YaxisTrendProperties tempProperties = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i);
				tempProperties.getLabelProperties().setFontProperties(fontPropertiesLabel);
				graphInfo.getGraphProperties().getyAxisPropertiesMap().put("M"+i, tempProperties);
			}
			//Rows div[id^=rowsTitle] in graphProperties
			graphInfo.getGraphProperties().getAllLabelsProperties().setSize(fontSizeLabel);//allLabels fontName via CSS
			graphInfo.getGraphProperties().getSmartenProperties().getSmartenTitleProperties().setFontProperties(fontPropertiesLabel);
			//Cols
			graphInfo.getGraphProperties().getSmartenProperties().getSmartenLabelProperties().setFontProperties(fontPropertiesLabel);
			//Legend 
			graphInfo.getGraphProperties().getLegendProperties().getLegendValuesProperties().setLegendValuesFontProperties(fontPropertiesLabel);
			//PIE
			graphInfo.getGraphProperties().getPieTitle().setFontProp(fontPropertiesLabel);
			
			//cmb
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);

			break;
		case 1:
			//Category
			graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			//Pie
			graphInfo.getGraphProperties().getPieTitle().setFontProp(fontPropertiesLabel);
			break;
		case 2:
			//Value
			graphInfo.getGraphProperties().getyAxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			//cmb
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			
			
			for(int i=0;i<graphInfo.getGraphProperties().getyAxisPropertiesMap().size();i++)
			{
				YaxisTrendProperties tempProperties = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i);
				tempProperties.getLabelProperties().setFontProperties(fontPropertiesLabel);
				graphInfo.getGraphProperties().getyAxisPropertiesMap().put("M"+i, tempProperties);
			}
			break;
		case 3:
			//Rows div[id^=rowsTitle] in graphProperties
			graphInfo.getGraphProperties().getAllLabelsProperties().setSize(fontSizeLabel);//allLabels fontName via CSS
			graphInfo.getGraphProperties().getSmartenProperties().getSmartenTitleProperties().setFontProperties(fontPropertiesLabel);
			break;
		case 4:
			//Cols
			graphInfo.getGraphProperties().getSmartenProperties().getSmartenLabelProperties().setFontProperties(fontPropertiesLabel);
			break;
		case 5:
			//Legend 
			graphInfo.getGraphProperties().getLegendProperties().getLegendValuesProperties().setLegendValuesFontProperties(fontPropertiesLabel);
			break;
		}

		if(CategoryaxislabelCB) {
			graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			
			graphInfo.getGraphProperties().getPieTitle().setFontProp(fontPropertiesLabel);
		}
		if(valueaxislabelCB) {
			graphInfo.getGraphProperties().getyAxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			//cmb
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setFontProperties(fontPropertiesLabel);
			
			
			for(int i=0;i<graphInfo.getGraphProperties().getyAxisPropertiesMap().size();i++)
			{
				YaxisTrendProperties tempProperties = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i);
				tempProperties.getLabelProperties().setFontProperties(fontPropertiesLabel);
				graphInfo.getGraphProperties().getyAxisPropertiesMap().put("M"+i, tempProperties);
			}
		}
		
		if(rowlabelCB) {
			graphInfo.getGraphProperties().getAllLabelsProperties().setSize(fontSizeLabel);//allLabels fontName via CSS
			graphInfo.getGraphProperties().getSmartenProperties().getSmartenTitleProperties().setFontProperties(fontPropertiesLabel);
		}
		if(colsaxislabelCB) {
			graphInfo.getGraphProperties().getSmartenProperties().getSmartenLabelProperties().setFontProperties(fontPropertiesLabel);	
		}
		if(legendaxislabelCB) {
			graphInfo.getGraphProperties().getLegendProperties().getLegendValuesProperties().setLegendValuesFontProperties(fontPropertiesLabel);
		}
		//Samrten Qucik settings
		String quickparam = request.getParameter("quickSettingsParam");
		String quartileValueChange =request.getParameter("enableQuartileValue");
		
		graphInfo.getGraphProperties().getSmartenProperties().setQuartileScaleValue(quartileValueChange);//enableQuartileScale
		
		String[] quickParam = quickparam.split(",");

		/*"dataValue", "mouseOver", "drillOnLegend", "legend",
		"zoom", "cmbLegend","barDataValue","drillOnLegendCmb","mouseOverCmb",
		"lineDataValue"*/
		boolean dataValue =  false;
		boolean mouseOver = false;
		boolean drillOnLegend =  false;
		boolean legend =  false;
		boolean zoom  =  false;
		boolean cmbLegend = false;
		boolean barDataValue  =  false;
		boolean drillOnLegendCmb =  false;
		boolean mouseOverCmb =  false;
		boolean lineDataValue =  false;
		boolean enablePagination = true;
		boolean showValueAxes = false;
		boolean split = false;
		/*boolean splitGraphHorizontal = false;
		boolean splitGraphVertical = false;*/
		boolean sameScale = false;
		boolean hideNullOnCategoryAxis = false;
		boolean sampling = false;
		boolean snapShotSampling = false;
		boolean isSamplingSnapShotChanged = false;//Added to know if user has changed state of sampling/snapShotSampling
		boolean isSnapShotChanged = true;//false;//Added to know if user has changed state of snapShot
		boolean enableLogarithmic = false;
		boolean enableQuartileScale = false;
		boolean isGoogleMap = false;
		boolean isShowAllMarker = false;
		int combinedRotateType = 0;
		
		boolean isD3Chart = false;
		if(graphInfo.getGraphType() == GraphConstants.D3_CHORD
				|| graphInfo.getGraphType() == GraphConstants.D3_TREEMAP
				|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST
				|| graphInfo.getGraphType() == GraphConstants.D3_BUBBLE
				|| graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT)
		{	
			isD3Chart = true;
		}
		
		if(quickParam.length > 1)
		{
		 dataValue =  Boolean.parseBoolean(quickParam[0]);
		 mouseOver =  Boolean.parseBoolean(quickParam[1]);
		 drillOnLegend =  Boolean.parseBoolean(quickParam[2]);
		 legend =  Boolean.parseBoolean(quickParam[3]);
		 zoom  =  Boolean.parseBoolean(quickParam[4]);
		 cmbLegend = Boolean.parseBoolean(quickParam[5]);
		 barDataValue  =  Boolean.parseBoolean(quickParam[6]);
		 drillOnLegendCmb =  Boolean.parseBoolean(quickParam[7]);
		 mouseOverCmb =  Boolean.parseBoolean(quickParam[8]);
		 lineDataValue =  Boolean.parseBoolean(quickParam[9]);
		 enablePagination =  Boolean.parseBoolean(quickParam[10]);
		 showValueAxes =  Boolean.parseBoolean(quickParam[11]);//added
		 split = Boolean.parseBoolean(quickParam[12]);
		 sameScale = Boolean.parseBoolean(quickParam[13]);
		 hideNullOnCategoryAxis = Boolean.parseBoolean(quickParam[14]);
		 sampling = Boolean.parseBoolean(quickParam[15]);
		 snapShotSampling = Boolean.parseBoolean(quickParam[16]);
		 isSamplingSnapShotChanged = Boolean.parseBoolean(quickParam[17]);
		 isSnapShotChanged = Boolean.parseBoolean(quickParam[18]);
		 /*splitGraph =  Boolean.parseBoolean(quickParam[11]);*/
		 enableLogarithmic = Boolean.parseBoolean(quickParam[19]);
		 enableQuartileScale = Boolean.parseBoolean(quickParam[20]);
		 isGoogleMap = Boolean.parseBoolean(quickParam[21]);
		 isShowAllMarker = Boolean.parseBoolean(quickParam[22]);
		 combinedRotateType = Integer.parseInt(quickParam[23]);
		}
		//defaultServer properties
		
		//usr has changed pagination cb[2 jan 2019] BUG:14316
		if(enablePagination != graphInfo.getGraphProperties().isPaginationCB())
		{
			if(enablePagination)
			{
				graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenCategoryAxisPagination(30);
				graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenLegendPagination(30);
			}
			else
			{
				graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenCategoryAxisPagination(-1);
				graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenLegendPagination(-1);
			}
		}
		//usr has changed pagination cb[2 jan 2019] BUG:14316
		
		
		if(enablePagination == true && sampling == true && snapShotSampling == true)
		{
			graphInfo.getGraphProperties().setPaginationCB(!enablePagination);
			graphInfo.getGraphProperties().setSamplingCB(sampling);
			graphInfo.getGraphProperties().setSnapShotSamplingCB(snapShotSampling);
			graphInfo.setSmartSamplingEnable(true);
		}
		else
		{
			graphInfo.getGraphProperties().setPaginationCB(!enablePagination);
			graphInfo.getGraphProperties().setSamplingCB(sampling);
			graphInfo.getGraphProperties().setSnapShotSamplingCB(snapShotSampling);
			graphInfo.setSmartSamplingEnable(false);
		}
		/*if(!graphInfo.isSmartSamplingEnable())//user malually checks unchecks check boxes
			smartenService.applyManualSampling(graphInfo);*/
		
		
		if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH)
		{
			graphInfo.getGraphProperties().setCombinedRotateType(combinedRotateType);
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBardataValuePoint().setDataValuePointVisible(barDataValue);
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinedataValuePoint().setDataValuePointVisible(lineDataValue);
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBardataValueMouseOver().setMouseOverTextEnable(mouseOverCmb);
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinedataValueMouseOver().setMouseOverTextEnable(mouseOverCmb);;
			graphInfo.getGraphProperties().getCombinedGraph().getBarLegendProperties().getLegendPanelProperties().setDrillDown(drillOnLegendCmb);
		}
		//For gauges(as discussed with chintan sir for quick settings)
		if(!isD3Chart)
			graphInfo.getGraphProperties().getGaugeDataValueZone().getDataValueConfiguration().setVisible(dataValue);
		//MouseOver
		if(!isD3Chart)
		{
			graphInfo.getGraphProperties().getDataValueProperties().getDataValuePoint().setDataValuePointVisible(dataValue);
			graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().setMouseOverTextEnable(mouseOver);
		}
		//Drill on legend
		if(!isD3Chart)
			graphInfo.getGraphProperties().getLegendProperties().getLegendPanelProperties().setDrillDown(drillOnLegend);
		//Legend
		if(graphInfo.getGraphType() != GraphConstants.COMBINED_GRAPH)
		{
			if(!isD3Chart)
			{
				graphInfo.getGraphProperties().getLegendProperties().getLegendPanelProperties().setLegendPanelVisible(legend);
				graphInfo.getGraphProperties().getCombinedGraph().getBarLegendProperties().getLegendPanelProperties().setLegendPanelVisible(legend);
			}
		}
		else
		{
			graphInfo.getGraphProperties().getLegendProperties().getLegendPanelProperties().setLegendPanelVisible(cmbLegend);
			graphInfo.getGraphProperties().getCombinedGraph().getBarLegendProperties().getLegendPanelProperties().setLegendPanelVisible(cmbLegend);
		}
		//Zoom
		if(!isD3Chart)
			graphInfo.getGraphProperties().getGraphAreaProperties().getGraphChartCursor().setEnable(zoom);
		
		//noPagination
		if(graphInfo.isSmartSamplingEnable() == false)
		{
			if(enablePagination)
			{
				graphInfo.getGraphProperties().setPaginationChange(true);
			}
			else
			{
				graphInfo.getGraphProperties().setPaginationChange(false);

			}
		}
		//showValueAxes start
		//data value for PIE when multiple measure
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			Map<String,TrendDataValueProperties> pieDataValueMap = graphInfo.getGraphProperties().getDataValuePropertiesMap();
			for(int i =0;i<graphInfo.getDataColLabels3().size();i++)
			{
				TrendDataValueProperties prop= graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+i);
				prop.getDataValuePoint().setDataValuePointVisible(dataValue);
				prop.getDataValueMouseOver().setClusteredMouseOverTextEnable(mouseOver);
				pieDataValueMap.put("M"+i, prop);
			}
			graphInfo.getGraphProperties().setDataValuePropertiesMap(pieDataValueMap);
		}
		
		
		
		showValueAxes = false;
		//showValueAxes start
		/*graphInfo.getGraphProperties().getSmartenProperties().setShowValueAxes(showValueAxes);
	
		graphInfo.getGraphProperties().getSmartenProperties().setShowValueAxes(showValueAxes);
		
		if(graphInfo.getGraphType() != GraphConstants.HISTOGRAM_GRAPH && graphInfo.getGraphType() != GraphConstants.HEAT_MAP_GRAPH)
		{
			if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH)
			{
				if(showValueAxes)
				{
					//Bar
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getyAxisTitleTrendProperties().setVisible(true);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setVisible(true);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLineProperties().setVisible(true);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
					
					//Line
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getyAxisTitleTrendProperties().setVisible(true);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setVisible(true);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLineProperties().setVisible(true);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
				}
				else
				{
					//Bar
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getyAxisTitleTrendProperties().setVisible(false);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLabelProperties().setVisible(false);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLineProperties().setVisible(false);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getBarYaxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(false);
					
					//Line
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getyAxisTitleTrendProperties().setVisible(false);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLabelProperties().setVisible(false);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLineProperties().setVisible(false);
					graphInfo.getGraphProperties().getCombinedYaxisProperties().getLineYaxisProperties().getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(false);
				}
			}
			else
			{
				int valueAxesCounter = 1;
				if(null != graphInfo.getDataColLabels3())
					valueAxesCounter = graphInfo.getDataColLabels3().size();
				
				if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
					valueAxesCounter = 1;

				for(int i = 0 ; i < valueAxesCounter; i++){
					if(showValueAxes && null != graphInfo.getGraphProperties().getyAxisPropertiesMap() && graphInfo.getGraphProperties().getyAxisPropertiesMap().size() >= i)
					{
						graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getyAxisTitleTrendProperties().setVisible(true);
						graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getLabelProperties().setVisible(true);
						graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getLineProperties().setVisible(true);
						graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(true);
					}
					else
					{
						if(null != graphInfo.getGraphProperties().getyAxisPropertiesMap() && graphInfo.getGraphProperties().getyAxisPropertiesMap().size() > i)
						{
							graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getyAxisTitleTrendProperties().setVisible(false);
							graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getLabelProperties().setVisible(false);
							graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getLineProperties().setVisible(false);
							graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+i).getLineProperties().getAxisMajorLineTickTrendProperties().setVisible(false);
						}
					}
				}
			}
		}*/
		//showValueAxes end
		
		
		//hideNullOnCategoryAxis start
		if(!isD3Chart)
			graphInfo.getGraphProperties().getSmartenProperties().setHideNullOnCategoryAxis(hideNullOnCategoryAxis);
		//hideNullOnCategoryAxis end
		if(!isD3Chart)
			graphInfo.getGraphProperties().getSmartenProperties().setEnableLogarithmic(enableLogarithmic);//enableLogarithmic
		if(!isD3Chart)
			graphInfo.getGraphProperties().getSmartenProperties().setEnableQuartileScale(enableQuartileScale);//enableQuartileScale
		if(!isD3Chart)
		{	
			graphInfo.getGraphProperties().getSmartenProperties().setEnableGoogleMap(isGoogleMap);//Added for swapping to Google Map from ammap
			graphInfo.getGraphProperties().getSmartenProperties().setShowAllMarker(isShowAllMarker);//Added for displaying all Markers
		}
		
		//sampling
		if(graphInfo.isSmartSamplingEnable() == false)
		{
			//when user applies sampling an currently there is no sampling(1st condition)
			//there was default sampling,user removes it to view completeData(EG:- sr_no(It will have default sampling) => remove sampling)[2nd condition]
			if( (graphInfo.getGraphProperties().isSampling() == false && sampling == true)
					|| (graphInfo.getGraphProperties().isSampling() == true && sampling == false) )
				graphInfo.setSamplingActivity(true);
			
			if( (graphInfo.getGraphProperties().isSnapShotSampling() == false && snapShotSampling == true) 
					|| (graphInfo.getGraphProperties().isSnapShotSampling() == true && snapShotSampling == false) )
				graphInfo.setSamplingActivity(true);

			graphInfo.getGraphProperties().setSampling(sampling);
			if(!sampling)
				graphInfo.setDefaultSampling(false);
			//sampling off,change mode to auto
			if(!graphInfo.getGraphProperties().isSampling())
			{
				SmartenSampling sample = graphInfo.getSmartenSampling();
				sample.setSamplingMode((short)SmartenConstants.SAMPLE_AUTO);
				graphInfo.setSmartenSampling(sample);
			}

			graphInfo.getGraphProperties().setSnapShotSampling(snapShotSampling);//Snap Shot Sampling
		}
		
		
		//Now smartSampling auto will manage this with astriek
		/*if(snapShotSampling) {//Disabling Pagination when Snap Shot view
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenCategoryAxisPagination(-1);
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenLegendPagination(-1);
			
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setCategoryAxisPagination(-1);
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setLegendPagination(-1);
		}*/
		
		
		
		/* else {
			if(enablePagination)
			{
				if(graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getSmartenCategoryAxisPagination() == -1)
				{
					graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenCategoryAxisPagination(30);
					graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setCategoryAxisPagination(30);
				}
				
				graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenCategoryAxisPagination
				(graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getSmartenCategoryAxisPagination());
				graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setSmartenLegendPagination
				(graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getSmartenLegendPagination());
			}
		}*/
		
		//graphInfo.getGraphProperties().setSamplingSnapShotChanged(isSamplingSnapShotChanged);//To know if user has changed state of sampling/snapShotSampling
		graphInfo.getGraphProperties().setSamplingSnapShotChanged(true);//To send in createSmartenRS (2018)
		
		//graphInfo.getGraphProperties().setCallCreateSmartenResultSet(false);//Added to restrict createSmartenResultSet call when Settings apply
		
		//Added to plot as Split Graph start
		
		graphInfo.getGraphProperties().setSnapShotChanged(isSnapShotChanged);
		
		//split
		if(!isD3Chart)
		{
			graphInfo.getGraphProperties().getSmartenProperties().setSplit(split);
			if(!split)
			{
				graphInfo.getGraphProperties().getSmartenProperties().setSplitHorizontal(false);
				graphInfo.getGraphProperties().getSmartenProperties().setSplitVertical(false);
			}
			else
			{
				graphInfo.getGraphProperties().getSmartenProperties().setSplitType(graphProperties.getSmartenProperties().getSplitType());
				if(graphProperties.getSmartenProperties().getSplitType() == 0)
				{
					graphInfo.getGraphProperties().getSmartenProperties().setSplitHorizontal(true);
					graphInfo.getGraphProperties().getSmartenProperties().setSplitVertical(false);
				}
				else
				{
					graphInfo.getGraphProperties().getSmartenProperties().setSplitVertical(true);
					graphInfo.getGraphProperties().getSmartenProperties().setSplitHorizontal(false);
				}
			}
			graphInfo.getGraphProperties().getSmartenProperties().setSameScale(sameScale);
		}
		/*if(splitGraph)
			graphInfo.getGraphProperties().setSplitGraph(true);
		else
			graphInfo.getGraphProperties().setSplitGraph(false);*/
		//Added to plot as Split Graph end
		
		//Color properties smartenLabelProperties
		//graphInfo.getGraphProperties().setRangeColorList(Arrays.asList(rangeColorList.split(",")));
		//graphInfo.getGraphProperties().setColorType(smartenLabelProperties.getColorType());
		//graphInfo.getGraphProperties().setCustomColors(graphProperties.getCustomColors());
		//graphInfo.getGraphProperties().setColor(graphProperties.getColor());
		
		
		//Color properties
		//graphInfo.getGraphProperties().setSmartenColorProperties(graphProperties.getSmartenColorProperties());
		
			//Removing null from custom colors list start
			List<String> filledCustomColorsList = new ArrayList<String>();
			if(graphProperties.getCustomColors() != null)
			{
				for (int l = 0; l < graphProperties.getCustomColors().size(); l++) {
					if(graphProperties.getCustomColors() != null)
					{
						if(graphProperties.getCustomColors().get(l)!=null)
						{
							filledCustomColorsList.add(graphProperties.getCustomColors().get(l));
						}
					}
				}
			}
			graphProperties.setCustomColors(filledCustomColorsList);
			//Removing null from custom colors list end
		graphInfo.getGraphProperties().setColorType(graphProperties.getColorType());
		graphInfo.getGraphProperties().setFromSaveSmartenLabelProp(true);//Added for Map 4 Dec 2017
		graphInfo.getGraphProperties().setColor(graphProperties.getColor());
		if(graphInfo.getGraphProperties().getColorType() == 1)//If custom type is selected then only fill custom colr array.
			graphInfo.getGraphProperties().setCustomColors(graphProperties.getCustomColors());
		
		//
		graphInfo.getGraphProperties().setLinecolor(graphProperties.getLinecolor());
		graphInfo.getGraphProperties().setLineColorType(graphProperties.getLineColorType());
		graphInfo.getGraphProperties().setLineCustomColors(graphProperties.getLineCustomColors());
		//
			
		graphInfo.getGraphProperties().setTranceperancy(graphProperties.getTranceperancy());
		graphInfo.getGraphProperties().setRangeColorDivValue(graphProperties.getRangeColorDivValue());
		//graphInfo.getGraphProperties().setRangeColorList(smartenLabelProperties.getRangeColorList());
		graphInfo.getGraphProperties().setRangeColorList(Arrays.asList(rangeColorList.split(",")));
		graphInfo.getGraphProperties().setRangeEndColor(graphProperties.getRangeEndColor());
		graphInfo.getGraphProperties().setRangeStartColor(graphProperties.getRangeStartColor());
		graphInfo.getGraphProperties().setColorRange(graphProperties.getColorRange());//setAutoRangeColor(smartenLabelProperties.getSmartenColorProperties().isAutoRangeColor());
		
		//Added for Gauge color in settings
		if(graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			//For Legend Quick settings(Color) start
			String[] splitString = null;
			
			if(graphProperties.getColorType() == 3 && graphProperties.getColorRange() == 0)//Range (Auto Color)
			{
				splitString = rangeColorList.split(",");
			}
			
			if(graphProperties.getColorType() == 1 && graphProperties.getCustomColorType() == 0)//Custom (Auto)
			{
				rangeColorList = "#D62800,#FFE700,#84AE31";
				splitString = rangeColorList.split(",");
			}
			
			if(graphProperties.getColorType() == 1 && graphProperties.getCustomColorType() == 4)//Custom (Add Color)
			{
				List customColorList = graphProperties.getCustomColors();
				if(null != customColorList)
				{
					rangeColorList = "";
					for(int i = 0; i < customColorList.size(); i++)
					{
						rangeColorList += customColorList.get(i).toString()+",";
					}
					splitString = rangeColorList.split(",");
				}
			}
			
			if(null != splitString && splitString.length > 0)
			{
				graphInfo.getGraphProperties().getGaugeDialProperties().getZoneProperties().getDialColorProperties().setAlertColor(splitString[0]);
				if(splitString.length > 1)
					graphInfo.getGraphProperties().getGaugeDialProperties().getZoneProperties().getDialColorProperties().setWarningColor(splitString[1]);
				if(splitString.length > 2)
					graphInfo.getGraphProperties().getGaugeDialProperties().getZoneProperties().getDialColorProperties().setNormalColor(splitString[2]);
			}
			////For Legend Quick settings(Color) end
			
			//Gauge area start
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setHeight(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getHeight());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setNoOfGauge(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getNoOfGauge());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setPolarity(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getPolarity());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setMaxValType(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getMaxValType());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setMaxCustomVal(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getMaxCustomVal());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setMinValType(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getMinValType());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setMinCustomVal(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getMinCustomVal());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setWarningType(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getWarningType());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setWarningCustomVal(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getWarningCustomVal());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setAlertType(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getAlertType());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setAlertCustomVal(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getAlertCustomVal());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setTargetType(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getTargetType());
			graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().setTargetCustomVal(graphProperties.getGraphAreaProperties().getGeneralGraphArea().getTargetCustomVal());
			//Gauge area end
			
			//Scale start
			graphInfo.getGraphProperties().getGaugeScaleProperties().getMajorTickProperties().setVisible(graphProperties.getGaugeScaleProperties().getMajorTickProperties().isVisible());
			graphInfo.getGraphProperties().getGaugeScaleProperties().getMajorTickProperties().setColor(graphProperties.getGaugeScaleProperties().getMajorTickProperties().getColor());
			graphInfo.getGraphProperties().getGaugeScaleProperties().getMajorTickProperties().setLength(graphProperties.getGaugeScaleProperties().getMajorTickProperties().getLength());
			graphInfo.getGraphProperties().getGaugeScaleProperties().getMajorTickProperties().setThickness(graphProperties.getGaugeScaleProperties().getMajorTickProperties().getThickness());
			//Scale end
			
			//Needle start
			graphInfo.getGraphProperties().getGaugeNeedleProperties().getNeedle().setColor(graphProperties.getGaugeNeedleProperties().getNeedle().getColor());
			graphInfo.getGraphProperties().getGaugeNeedleProperties().getNeedle().setLength(graphProperties.getGaugeNeedleProperties().getNeedle().getLength());
			graphInfo.getGraphProperties().getGaugeNeedleProperties().getNeedle().setThickness(graphProperties.getGaugeNeedleProperties().getNeedle().getThickness());
			//Needle end
			//Needle Cap start
			graphInfo.getGraphProperties().getGaugeNeedleProperties().getNeedleCap().setVisible(graphProperties.getGaugeNeedleProperties().getNeedleCap().isVisible());
			graphInfo.getGraphProperties().getGaugeNeedleProperties().getNeedleCap().setRadious(graphProperties.getGaugeNeedleProperties().getNeedleCap().getRadious());
			graphInfo.getGraphProperties().getGaugeNeedleProperties().getNeedleCap().setColor(graphProperties.getGaugeNeedleProperties().getNeedleCap().getColor());
			//Needle Cap end
			
			//Gauge Dial start
			graphInfo.getGraphProperties().getGaugeDialProperties().getZoneProperties().getDialConfigProperties().setRadiusFrom(graphProperties.getGaugeDialProperties().getZoneProperties().getDialConfigProperties().getRadiusFrom());
			graphInfo.getGraphProperties().getGaugeDialProperties().getZoneProperties().getDialConfigProperties().setRadiusTo(graphProperties.getGaugeDialProperties().getZoneProperties().getDialConfigProperties().getRadiusTo());
			graphInfo.getGraphProperties().getGaugeDialProperties().getDialBackgroundProperties().setAngleStart(graphProperties.getGaugeDialProperties().getDialBackgroundProperties().getAngleStart());
			graphInfo.getGraphProperties().getGaugeDialProperties().getDialBackgroundProperties().setAngleEnd(graphProperties.getGaugeDialProperties().getDialBackgroundProperties().getAngleEnd());
			//Outer Circle
			graphInfo.getGraphProperties().getGaugeDialProperties().getOuterCircleProperteis().getDialConfigProperties().setVisible(graphProperties.getGaugeDialProperties().getOuterCircleProperteis().getDialConfigProperties().isVisible());
			graphInfo.getGraphProperties().getGaugeDialProperties().getOuterCircleProperteis().getDialConfigProperties().setRadiusFrom(graphProperties.getGaugeDialProperties().getOuterCircleProperteis().getDialConfigProperties().getRadiusFrom());
			graphInfo.getGraphProperties().getGaugeDialProperties().getOuterCircleProperteis().getDialConfigProperties().setRadiusTo(graphProperties.getGaugeDialProperties().getOuterCircleProperteis().getDialConfigProperties().getRadiusTo());
			//Inner Circle
			graphInfo.getGraphProperties().getGaugeDialProperties().getInnerCircleProperties().getDialConfigProperties().setVisible(graphProperties.getGaugeDialProperties().getInnerCircleProperties().getDialConfigProperties().isVisible());
			graphInfo.getGraphProperties().getGaugeDialProperties().getInnerCircleProperties().getDialConfigProperties().setRadiusFrom(graphProperties.getGaugeDialProperties().getInnerCircleProperties().getDialConfigProperties().getRadiusFrom());
			graphInfo.getGraphProperties().getGaugeDialProperties().getInnerCircleProperties().getDialConfigProperties().setRadiusTo(graphProperties.getGaugeDialProperties().getInnerCircleProperties().getDialConfigProperties().getRadiusTo());
			//Gauge Dial end
			
		}
		//Added for Gauge color in settings end
		
		//rangeBucket
		//graphInfo.getGraphProperties().getSmartenProperties().setRangeBucket(graphProperties.getSmartenProperties().getRangeBucket());
		
		//Added for Add Color in Custom
		graphInfo.getGraphProperties().setCustomColorType(graphProperties.getCustomColorType());
		//Added for Add Color in Custom end
		
		//Added for Map in Settings
		if(!isD3Chart)
		{
			graphInfo.getGraphProperties().setMapColorType(graphProperties.getMapColorType());
			graphInfo.getGraphProperties().setSmartenMapShape(graphProperties.getSmartenMapShape());
			graphInfo.getGraphProperties().setSmartenMapSize(graphProperties.getSmartenMapSize());
		}
		//Added for Map in Settings end
		if(isFromFilter) {
			return AppConstants.SUCCESS_STATUS;

		}else {
			return refreshObjectData(null,response, userInfo, map);
		}
	}
	
	@RequestMapping (value = "/saveSmartenMeasureBucket")
	@ResponseBody
	public ModelAndView saveSmartenMeasureBucket(@ModelAttribute GraphProperties graphProperties
			,@RequestParam(value = "measureIndex", required = false) int measureIndex
			,ModelMap map, HttpServletResponse response, HttpServletRequest request,@LoggedInUser UserInfo userInfo)
	{
		//rangeBucket
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH){
			graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+measureIndex).setRangeBucket(graphProperties.getDataValuePropertiesMap().get("M"+measureIndex).getRangeBucket());
		}else if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH){
			graphInfo.getGraphProperties().getCombinedDataValueProperties().setRangeBucket(graphProperties.getCombinedDataValueProperties().getRangeBucket());
		} else if (graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH) {//Added for Bug #15415
			graphInfo.getGraphProperties().getHeatmap().setNoOfRanges(graphProperties.getHeatmap().getNoOfRanges());
			graphInfo.getGraphProperties().getHeatmap().setDataRange(graphProperties.getHeatmap().getDataRange());
			graphInfo.getGraphProperties().getHeatmap().setDataRangeFrom(graphProperties.getHeatmap().isDataRangeFrom());
			graphInfo.getGraphProperties().getHeatmap().setDataRangeTo(graphProperties.getHeatmap().isDataRangeTo());
			graphInfo.getGraphProperties().getHeatmap().setCustomValues(graphProperties.getHeatmap().getCustomValues());
		} else {
			graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+measureIndex).setRangeBucket(graphProperties.getyAxisPropertiesMap().get("M"+measureIndex).getRangeBucket());
		}
				
		return refreshObjectData(null,response, userInfo, map);
	}
	
	@RequestMapping (value = "/getSmartenLabelProperties", method=RequestMethod.POST)//getrtangeColor
	@ResponseBody
	public ModelAndView getSmartenLabelProperties(@ModelAttribute GraphProperties smartenLabelProperties
			,ModelMap map, HttpServletResponse response, HttpServletRequest request,@LoggedInUser UserInfo userInfo)
	{
		getSmartenProperties(map,userInfo);
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return new ModelAndView("smartview/smartenLabelProperties");	
	}
	
	private void getSmartenProperties(ModelMap map,UserInfo userInfo) {

		Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
		if(propertyMap != null) {
			@SuppressWarnings("rawtypes")
			Iterator itr = propertyMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				map.put(key, propertyMap.get(key));
			}
		}
		
		map.put("isSmartenLeftPannel",true);//hide transperency in left pannel smarten
		//--------------------------sampling validation for JS -------------------------------------//
		boolean isD3PieRadar = false;
		boolean isTabular = false;
		boolean isAmchart = false;
		boolean isD3TreemapOrBubble = false;
		boolean isMap = graphInfo.isSmartenMap();
		boolean isAmBubble = graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH;
		
		long mainResultSetCount = graphInfo.getOriginalResultSetSize();

		if(graphInfo.isSmartenTabular())
			isTabular = true;
		else if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
				graphInfo.getGraphType() == GraphConstants.D3_TREEMAP || graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP)
		{
			isD3PieRadar = true;
		}
		else
			isAmchart = true;
		
		if(graphInfo.getGraphType() == SmartenConstants.D3_TREEMAP || graphInfo.getGraphType() == SmartenConstants.D3_BUBBLE)
			isD3TreemapOrBubble = true;

		map.put("isD3PieRadar",isD3PieRadar);
		map.put("selectedGraphType", graphInfo.getGraphType());
		map.put("isD3TreemapOrBubble",isD3TreemapOrBubble);
		map.put("isTabular",isTabular);
		map.put("isAmchart",isAmchart);
		map.put("isMap",isMap);
		map.put("mainResultSetCount",mainResultSetCount);
		map.put("graphTypeForCount",graphInfo.getGraphType());
		map.put("isAmBubble", isAmBubble);
		//--------------------------sampling validation for JS ------------------------------------//
		
		
		
		map.put("getSmartenLabelProperties", true);
		String allMeasureString = "";
		String bucketMeasure = "";
		int noOfMeasure = graphInfo.getDataColLabels3().size();
		boolean showMeasureRange = false;
		boolean isForSplitGraph = false;
		boolean rowsEnable = graphInfo.getGraphData().isSmartenRowsEnable();
		boolean colsEnable = graphInfo.getGraphData().isSmartenColoumnsEnable();
		boolean isSmartenLegend = ((graphInfo.getGraphData().getRowLabel() != null && !graphInfo.getGraphData().getRowLabel().equals("") && !graphInfo.getGraphData().getRowLabel().equalsIgnoreCase("legend"))
									|| graphInfo.getGraphData().isRowMeasure()
									|| graphInfo.getGraphData().getShapeLabel() != null
									|| graphInfo.getGraphData().getSizeLabel() != null);

		boolean serialWithMultipleMeasure = false;
		
		map.put("showPagination",true);
		map.put("showZoom",true);
		
		if((graphInfo.getGraphType() == GraphConstants.STACKED_HBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.STACKED_VBAR_GRAPH) && noOfMeasure >1 && !graphInfo.getGraphData().getRowLabel().equalsIgnoreCase("legend"))
			serialWithMultipleMeasure = true;
		if(graphInfo.getGraphData().getRowLabel()!=null && graphInfo.getGraphData().getRowLabel().equalsIgnoreCase("legend") && noOfMeasure > 1)
			serialWithMultipleMeasure = true;
		if(graphInfo.getGraphData().isRowMeasure() || graphInfo.getGraphData().isSizeMeasure() || graphInfo.getGraphData().isShapeMeasure())
			serialWithMultipleMeasure = true;
		
		if(graphInfo.getGraphType() == SmartenConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == SmartenConstants.DRILLED_STACKED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH)
			serialWithMultipleMeasure = false;
		if(graphInfo.isRowsMeasure() || graphInfo.isColsMeasure())
			serialWithMultipleMeasure = true;
		
		if((graphInfo.getGraphType() == GraphConstants.STACKED_HBAR_GRAPH ||	
				graphInfo.getGraphType() == GraphConstants.STACKED_VBAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.PERCENTAGE_HBAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.PERCENTAGE_VBAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.STACKED_LINE_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.PERCENTAGE_LINE_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.AREA_STACK_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.AREA_PERCENTAGE_GRAPH)
				&& null != graphInfo.getGraphData().getRowLabel()
				&& graphInfo.getGraphData().getRowLabel().equalsIgnoreCase("legend") && noOfMeasure > 1)
		{
			serialWithMultipleMeasure = false;
		}

		for(int k = 0;k<graphInfo.getDataColLabels3().size();k++)
		{
			if(serialWithMultipleMeasure)
			{
				if(k==0)
					allMeasureString = graphInfo.getDataColLabels3().get(k).toString();
				else
					allMeasureString = allMeasureString+ "," + graphInfo.getDataColLabels3().get(k).toString();
			}
			else
				allMeasureString = graphInfo.getDataColLabels3().get(0).toString();
		}
		if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH )
		{
			if(graphInfo.getTheDataColLabels4().size() > 0)
			allMeasureString = allMeasureString + "," + graphInfo.getTheDataColLabels4().get(0).toString();
			if(graphInfo.getGraphData().isRowMeasure() && !allMeasureString.contains(graphInfo.getGraphData().getColorMeasureLabel()))
			{
				allMeasureString = allMeasureString + "," + graphInfo.getGraphData().getColorMeasureLabel(); 
			}
			
		}
		if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
		{
			/*if(graphInfo.getDataColLabels3().size() == 2)//bbl chart
				allMeasureString = graphInfo.getDataColLabels3().get(0).toString();
			if(graphInfo.getDataColLabels3().size() == 3)//XY chart
				allMeasureString = graphInfo.getDataColLabels3().get(1).toString();
			
			if(graphInfo.getGraphData().isRowMeasure())
			{
				allMeasureString = allMeasureString + "," + graphInfo.getGraphData().getColorMeasureLabel(); 
			}*/
			/*for(int k = 0;k<graphInfo.getDataColLabels3().size();k++)
			{
				if(k==0)
					allMeasureString = graphInfo.getDataColLabels3().get(k).toString();
				else
					allMeasureString = allMeasureString+ "," + graphInfo.getDataColLabels3().get(k).toString();
			}*/
			//XY set y,x,size
			if(graphInfo.getDataColLabels3().size() == 3)
			{
				allMeasureString = graphInfo.getDataColLabels3().get(1).toString() + "," + graphInfo.getDataColLabels3().get(0).toString()+","+graphInfo.getDataColLabels3().get(2).toString();
			}
			if(graphInfo.getDataColLabels3().size() == 2)
			{
				allMeasureString = graphInfo.getDataColLabels3().get(0).toString() + "," + graphInfo.getDataColLabels3().get(1).toString();
				if(graphInfo.isXyChart())
					allMeasureString = graphInfo.getDataColLabels3().get(1).toString() + "," + graphInfo.getDataColLabels3().get(0).toString();
			}
			if(graphInfo.getDataColLabels3().size() == 1)
			{
				allMeasureString = graphInfo.getDataColLabels3().get(0).toString();
			}
		}
		
		//--------Aggregation------------------//
		if(!graphInfo.isPerformAggregation())//show all measure 
		{
			allMeasureString = "";
			for(int k = 0;k<graphInfo.getDataColLabels3().size();k++)
			{
				if(k==0)
					allMeasureString = graphInfo.getDataColLabels3().get(k).toString();
				else
					allMeasureString = allMeasureString+ "," + graphInfo.getDataColLabels3().get(k).toString();
			}
		}
		//--------Aggregation------------------//
		
		
		if(allMeasureString.endsWith(",")) 
			allMeasureString = allMeasureString.substring(0, allMeasureString.length() - 1);
		map.put("allMeasures", allMeasureString);
		map.put("selectedMeasureIndex",graphInfo.getSelectedMeasureIndex());
		
		//Add color,size and shape in bucket dropdown if it contains measure
		if(graphInfo.getGraphData().isRowMeasure())
		{
			if(bucketMeasure.equals(""))
				bucketMeasure = graphInfo.getGraphData().getColorMeasureLabel(); 
		}
		if(graphInfo.getGraphData().isSizeMeasure())
		{
			if(bucketMeasure.equals(""))
				bucketMeasure = graphInfo.getGraphData().getSizeLabel();
			else
				bucketMeasure = bucketMeasure +","+graphInfo.getGraphData().getSizeLabel();
		}
		if(graphInfo.getGraphData().isShapeMeasure())
		{
			if(bucketMeasure.equals(""))
				bucketMeasure = graphInfo.getGraphData().getShapeLabel();
			else
				bucketMeasure = bucketMeasure+","+graphInfo.getGraphData().getShapeLabel();
		}
		map.put("showMeasureRange", showMeasureRange);
		
		
		map.put("smartenMeasureCurrentTabName", "M"+0);
		map.put("smartenMeasureSelectedTabNames", "M"+0);
		
		//show ignoreHideOnNull
		boolean serialChart = (graphInfo.getGraphType() == GraphConstants.VBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.STACKED_VBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.PERCENTAGE_VBAR_GRAPH 
				|| graphInfo.getGraphType() == GraphConstants.HBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.STACKED_HBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.PERCENTAGE_HBAR_GRAPH 
				|| graphInfo.getGraphType() == GraphConstants.LINE_GRAPH || graphInfo.getGraphType() == GraphConstants.STACKED_LINE_GRAPH || graphInfo.getGraphType() == GraphConstants.PERCENTAGE_LINE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.AREA_DEPTH_GRAPH || graphInfo.getGraphType() == GraphConstants.AREA_STACK_GRAPH || graphInfo.getGraphType() == GraphConstants.AREA_PERCENTAGE_GRAPH);
		if(serialChart)
			map.put("showHideNullOnCategory", true);
		else
			map.put("showHideNullOnCategory", false);
		
		boolean onlyPositiveValues = false;
		/*if(null != graphInfo.getGraphData().getTempDataList() && !graphInfo.getGraphData().getTempDataList().isEmpty()){
			Object minimumValue = Collections.min(graphInfo.getGraphData().getTempDataList());
			Object firstEle = 
			double minValue = Double.parseDouble(minimumValue.toString());
			if(minValue < 0)
				onlyPositiveValues = false;
		}*/
		map.put("showEnableLogarithmicScale", (onlyPositiveValues && 
				(graphInfo.getGraphType() == GraphConstants.VBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.HBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.LINE_GRAPH || graphInfo.getGraphType() == GraphConstants.AREA_DEPTH_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH || graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.CANDLE_STICK_GRAPH || graphInfo.getGraphType() == GraphConstants.HIGH_LOW_OPEN_CLOSE_GRAPH)));
			
		map.put("showEnableQuartileScale", (graphInfo.getGraphType() == GraphConstants.VBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.HBAR_GRAPH || graphInfo.getGraphType() == GraphConstants.LINE_GRAPH || graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH));
		if(!graphInfo.isPerformAggregation())
			map.put("showEnableQuartileScale", false);
		
		map.put("showGoogleMapSmarten", graphInfo.getGraphType() == SmartenConstants.SMARTENVIEW_MAP);

		//D3
		boolean isD3Graph =false;
		if(graphInfo.getGraphType() == GraphConstants.D3_CHORD
				|| graphInfo.getGraphType() == GraphConstants.D3_TREEMAP
				|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST
				|| graphInfo.getGraphType() == GraphConstants.D3_BUBBLE
				|| graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT)
		{	
			isD3Graph = true;
		}
		map.put("d3Graph", isD3Graph);
		
		//Add custom color list first time which would be editable and the size of the list will depend on autoRangeDivValue(PN=20)
		List customColorList = new ArrayList();
		int autoRangeDivValue = graphInfo.getGraphProperties().getRangeColorDivValue();
		if(autoRangeDivValue > 30)
			autoRangeDivValue=10;
		String[] barColor =new String[]{"#67b7dc","#6794dc","#6771dc","#8067dc","#a367dc","#c767dc","#dc67ce","#dc67ab","#dc6788","#dc6967","#dc8c67","#dcaf67","#dcd267","#c3dc67","#a0dc67","#7ddc67","#67dc75","#67dc98","#67dcbb","#67dadc","#80d0f5","#80adf5","#808af5","#9980f5","#bc80f5","#e080f5","#f580e7", "#f7d584", "#b1fb83", "#50407f", "#64c7cd", "#02adf2", "#828813", "#3ab54a", "#ed008c", "#8daacb", "#fc7362", "#bbd854", "#ffd92f", "#66c296","#e5b694", "#e78ad2", "#b3b3b3", "#a6d8e3", "#abe9bc", "#1b7d9c", "#ffbfc9", "#4da741", "#c4b2d6", "#b22424","#00acac", "#be6c2c", "#695496", "#349152", "#c9a16c", "#2d6396", "#fb2600", "#1596ff", "#fc9400", "#36fa92","#ec8b8b", "#93c2ff", "#f7d584", "#b1fb83", "#50407f", "#64c7cd", "#02adf2", "#828813", "#3ab54a", "#ed008c"};
		String[] d3barColor =new String[]{"#00A1DE", "#C4C4C4"};
		if(graphInfo.getGraphData().isRowMeasure() && graphInfo.getGraphData().getLegendMeasureColorList() != null)
		{
			//barColor = new String[graphInfo.getGraphData().getLegendMeasureColorList()]
					/*barColor = new String[graphInfo.getGraphData().getLegendMeasureColorList().size()];
					graphInfo.getGraphData().getLegendMeasureColorList().toArray(barColor);
					autoRangeDivValue = graphInfo.getGraphData().getLegendMeasureColorList().size();*///graphInfo.getGraphData().getNoOfBucketsForColor();
					int indexInData3 = graphInfo.getDataColLabels3().indexOf(graphInfo.getGraphData().getColorMeasureLabel());
					int noOfRanges = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+indexInData3).getRangeBucket().getNoOfRanges();
					List fadeColorList = smartenService.createRangeColorSmarten("#000099","#ffffff",(noOfRanges*2)+2);
					List tempList = new ArrayList();
					for (int i = 0; i < fadeColorList.size(); i++) {
						if(i%2==0)
							tempList.add(fadeColorList.get(i).toString());
					}
					//fadeColorList=tempList;
					fadeColorList.remove(fadeColorList.size()-1);
					fadeColorList.remove(fadeColorList.size()-2);
					Collections.reverse(fadeColorList);
					barColor = new String[fadeColorList.size()];
					fadeColorList.toArray(barColor);
					autoRangeDivValue = noOfRanges;
		}
		for(int i=0;i<autoRangeDivValue;i++)
		{
			customColorList.add(barColor[i%barColor.length]);
		}
		
		//This adds custom colors for the FIRST TIME ONLY which user can edit.// placed it in saveSmartenLablePropertie[10 May 2018]
		/*if(null == graphInfo.getGraphProperties().getCustomColors() ||(null != graphInfo.getGraphProperties().getCustomColors() && graphInfo.getGraphProperties().getCustomColors().size() == 0))
			graphInfo.getGraphProperties().setCustomColors(customColorList);*/
		
		
		int customColorSize = 0;
		String customColorStr = "";
		String lineCustomColorStr = "";
		String[] heatMapColor =new String[]{"#845EC2","#D65DB1","#FF6F91","#FF9671","#FFC75F","#c767dc","#dc67ce","#dc67ab","#dc6788","#dc6967",
			    "#dc8c67","#dcaf67","#dcd267","#c3dc67","#a0dc67","#7ddc67","#67dc75","#67dc98","#67dcbb","#67dadc",
			    "#80d0f5","#80adf5","#808af5","#9980f5","#bc80f5","#e080f5","#f580e7", "#f7d584", "#b1fb83", "#50407f", 
			    "#64c7cd", "#02adf2", "#828813", "#3ab54a", "#ed008c", "#8daacb", "#fc7362", "#bbd854", "#ffd92f", "#66c296",
			    "#e5b694", "#e78ad2", "#b3b3b3", "#a6d8e3", "#abe9bc", "#1b7d9c", "#ffbfc9", "#4da741", "#c4b2d6", "#b22424",
			    "#00acac", "#be6c2c", "#695496", "#349152", "#c9a16c", "#2d6396", "#fb2600", "#1596ff", "#fc9400", "#36fa92",
			    "#ec8b8b", "#93c2ff", "#f7d584", "#b1fb83", "#50407f", "#64c7cd", "#02adf2", "#828813", "#3ab54a", "#ed008c"};
		if(graphInfo.getGraphProperties().getCustomColors() != null && graphInfo.getGraphProperties().getCustomColors().size() > 0) 
			customColorList = graphInfo.getGraphProperties().getCustomColors();
		if(graphInfo.getGraphType() == GraphConstants.LINE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.STACKED_LINE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.PERCENTAGE_LINE_GRAPH)
		{	
			customColorList = graphInfo.getGraphProperties().getLineCustomColors();
		}
		
		customColorSize = customColorList.size();
		if(customColorSize > 30)
			customColorSize = 10;
		for(int i=0;i<customColorSize;i++) {
			if(customColorList!=null && customColorList.get(i) != null) {
				customColorStr += customColorList.get(i)+",";
			}
		}
		if(customColorStr.length()>0) {
			customColorStr = customColorStr.substring(0,customColorStr.length()-1);
		}
		//}
		int smartenColorAuto = getSmartenColorSizeForAuto();
		if(smartenColorAuto > 30)
			smartenColorAuto = 10;
		                                                   
		if(graphInfo.getGraphProperties().getColorRange() == 1)//custom range color[BUG: 14208]
			smartenColorAuto = graphInfo.getGraphProperties().getRangeColorDivValue();
		//deletes custom color and then,remaining colors shld be shown(in case of pn and 4 custom color other color shld be shown as it is)
		String[] customColor  = customColorStr.split(",");
		
		if(!customColorStr.equals("") && customColor.length > 0 && smartenColorAuto > customColor.length)
		{
			for(int i=customColor.length;i<smartenColorAuto;i++)
			{
				customColorStr =customColorStr +"," +barColor[i%barColor.length];
			}
		}
		if(graphInfo.getGraphType() == GraphConstants.D3_CHORD)
		{
			customColorStr="";
			for(int i=0;i<2;i++)
			{
				customColorStr+= d3barColor[i%d3barColor.length]+",";
			}
		}
		//no custom color provided
		if(graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH && graphInfo.getGraphProperties().getLineCustomColors().size() == 0 && graphInfo.getGraphProperties().getCustomColors().size() == 0)
		{
			customColorStr="";
			for(int i=0;i<heatMapColor.length;i++)
			{
					customColorStr+= heatMapColor[i%heatMapColor.length]+",";
			}
		}
		if(graphInfo.isSmartenMap())//Added for settings color on Map 4 Dec 2017
		{
			customColorStr = "";
			int noOfRanges =0;
			int indexInData3 = graphInfo.getDataColLabels3().indexOf(graphInfo.getGraphData().getColorMeasureLabel());
			if(indexInData3 > 0)
				noOfRanges = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+indexInData3).getRangeBucket().getNoOfRanges();
			else
				noOfRanges = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M0").getRangeBucket().getNoOfRanges();
			
			smartenColorAuto=noOfRanges;
			int colorListForMapSize = graphInfo.getGraphData().getColorListForMap().size();
			if(colorListForMapSize > 30)
				colorListForMapSize=10;
			for(int i = 0; i < colorListForMapSize; i++) {
				customColorStr += graphInfo.getGraphData().getColorListForMap().get(i)+",";
			}
		}
		
		map.put("rangeColorAutoDivValue",smartenColorAuto);
		//map.put("customColorSize", smartenColorAuto);
		map.put("customColorSize", customColorSize);
		map.put("recommendedColorType", graphInfo.getGraphProperties().getRecommendedColorType());
		map.put("customColorStr", customColorStr);
		map.put("bucketMeasure", bucketMeasure);
		if(autoRangeDivValue > 10)
			map.put("rangeColorDivValue", 10);
		else
			map.put("rangeColorDivValue", autoRangeDivValue);
		map.put("smartenProperties", graphInfo.getGraphProperties().getSmartenProperties());
		map.put("graphProperties", graphInfo.getGraphProperties());
		
		map.put("mainOutlinerList", StringUtils.join(graphInfo.getMainOutlinerMeasureAndDimension(), ','));
		
		map.put("isMap", graphInfo.isSmartenMap());
		map.put("isTabular", graphInfo.isSmartenTabular());
		map.put("isDailGauge", graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE);
		map.put("isForSplitGraph", ((graphInfo.getGraphType() == GraphConstants.VBAR_GRAPH
								|| graphInfo.getGraphType() == GraphConstants.HBAR_GRAPH
								|| graphInfo.getGraphType() == GraphConstants.LINE_GRAPH
								|| graphInfo.getGraphType() == GraphConstants.AREA_DEPTH_GRAPH) && graphInfo.getDataColLabels3().size() > 1)
								&& (!rowsEnable && !colsEnable)
								&& !isSmartenLegend);
		
		map.put("isTrendGraphs", (graphInfo.getGraphType() == GraphConstants.VBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.STACKED_VBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.PERCENTAGE_VBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.HBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.STACKED_HBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.PERCENTAGE_HBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.LINE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.STACKED_LINE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.PERCENTAGE_LINE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.AREA_DEPTH_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.AREA_STACK_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.AREA_PERCENTAGE_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH));
		
		//when same scale  show only first measure
		if(graphInfo.getGraphProperties().getSmartenProperties().isSameScale())
		{
			allMeasureString = graphInfo.getDataColLabels3().get(0).toString();
			map.put("allMeasures", allMeasureString);
		}

		if(graphInfo.isSmartenMap() || graphInfo.getGraphType() == GraphConstants.D3_CHORD)//Added for settings color on Map 4 Dec 2017
			map.put("autoColorArray", customColorStr);
		else if(graphInfo.getGraphType() == GraphConstants.HEAT_MAP_GRAPH)
			map.put("autoColorArray", customColorStr);
		else
			map.put("autoColorArray", StringUtils.join(Arrays.asList(barColor), ','));
		map.put("autoColorArraySize", smartenColorAuto);

		if(graphInfo.getGraphData().isRowMeasure())
		{	
			
			//map.put("autoColorArray", StringUtils.join(graphInfo.getGraphData().getLegendMeasureColorList(), ','));
			map.put("autoColorArraySize", graphInfo.getGraphData().getNoOfBucketsForColor());
			//customColorsList.subList(0, graphInfo.getGraphProperties().getRangeColorDivValue());
			map.put("customColorSize", graphInfo.getGraphData().getNoOfBucketsForColor());
			map.put("customColorStr", customColorStr);
		}
			
		map.put("isPieChart",false);
		map.put("isCombinedChart",false);
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
			map.put("isPieChart",true);
		if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH)
		{
			map.put("currentTabName", 1);
			map.put("isCombinedChart",true);
			map.put("lineCustomColorStr", lineCustomColorStr);//Added for UI changes for Bug 15094
			map.put("lineCustomColorSize", 1);//1 because only 1M in Cmb line. Added for UI changes for Bug 15094
		}
			
		//Show hide Quick settings acc to graph type
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{	
			map.put("showPagination",false);
			map.put("showZoom",false);
			map.put("ignoreHideOnCategory", true);
		}
		if(graphInfo.getGraphType() == SmartenConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == SmartenConstants.DRILLED_STACKED_RADAR_GRAPH)
		{
			map.put("showZoom",false);
			map.put("ignoreHideOnCategory", true);
		}
		if(graphInfo.getGraphType() == SmartenConstants.COMBINED_GRAPH || graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
			map.put("ignoreHideOnCategory", true);
			
		HashMap<String, String> dateFormateList = GeneralUtil.getDateFormatList(false);
		boolean qutWeekMonth = false;
		LinkedHashMap<String, String> dateColumnFormateList = new LinkedHashMap<>();
		boolean sameDate = false;
		if (graphInfo.getDateFrequencyMap() != null) {
			Map<String, String> dateFrequencyMap = graphInfo.getDateFrequencyMap();
			String frequency = "";
			if (null != graphInfo.getColColumns() && !graphInfo.getColColumns().isEmpty()) {
				String strCol = graphInfo.getColColumns().elementAt(0).toString();
				if (dateFrequencyMap != null && !dateFrequencyMap.isEmpty() && dateFrequencyMap.get(strCol) != null
						&& !dateFrequencyMap.get(strCol).isEmpty()) {
					frequency = dateFrequencyMap.get(strCol);
					if(frequency != "" )
						qutWeekMonth = true;
				}
			}
			if(frequency != null && !frequency.isEmpty() ) {
				if(frequency.equalsIgnoreCase(KPIConstants.FREQUENCY_QUARTERLY) || frequency.equalsIgnoreCase(KPIConstants.FREQUENCY_MONTHLY) || frequency.equalsIgnoreCase(KPIConstants.FREQUENCY_WEEKLY)) {
					
					StringUtil.getDateColumnFormatMap(dateColumnFormateList, frequency);
					sameDate = true;
				}
			
			}

		}
		map.put("qutWeekMonth", qutWeekMonth);

		
		//Show hide Quick settings acc to graph type
		List<Object> colorTitles = graphInfo.getLovListForColorUnique();;//graphInfo.getLovListForColor();
		/*List<String> capitalizedTitles = colorTitles.stream()
			    .map(s -> s == null ? null : s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
			    .collect(Collectors.toList());
		colorTitles.clear();
		colorTitles.addAll(capitalizedTitles);*/
		if(graphInfo.getDataColLabels3().size()>=2&& graphInfo.getGraphData().getRowLabel()!=null&& (graphInfo.getGraphData().getRowLabel().equals("Legend") || graphInfo.getGraphData().getColLabel() ==null || graphInfo.getGraphData().getColLabel().isEmpty())) {
			colorTitles = new ArrayList<>(graphInfo.getDataColLabels3());
			}
		if(graphInfo.getGraphType() == SmartenConstants.HEAT_MAP_GRAPH && null != graphInfo.getGraphData().getRangeList()) {
			colorTitles = graphInfo.getGraphData().getRangeList();
		}
		if(qutWeekMonth)
		{
			colorTitles = graphInfo.getGraphData().getColList();
		}
		String colorTitlesJson="";
		try {
			colorTitlesJson = new ObjectMapper().writeValueAsString(colorTitles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		map.put("colorTitlesJson", colorTitlesJson);


		if(graphInfo.getGraphType() == GraphConstants.D3_CHORD) 
		{
			map.put("rangeColorAutoDivValue",2);
			map.put("rangeColorDivValue", 2);
			map.put("autoColorArraySize", 2);
		}
		
	}

	@RequestMapping (value = "/setSmartenModeOff")
	@ResponseBody
	public List setSmartenModeOff(@LoggedInUser UserInfo userInfo)
	{
		graphInfo.setSmartenMode(false);
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),null,"Set smarten mode off",Thread.currentThread(),userInfo,null);
		//Returning List to hide respective graph icons in Left Panel when modeOff
		return smartenService.getIconsList(graphInfo.getDimensionTitleList(), graphInfo.getMeasureTitleList(), graphInfo);
	}
	
	@RequestMapping("/showSmartenSampling")
	public ModelAndView showSmartenSampling(ModelMap modelMap,@LoggedInUser UserInfo userInfo, @RequestParam(required=false, defaultValue="0")Long actionInfoId ){

		SmartenSampling smartenSampling= graphInfo.getSmartenSampling();
		modelMap.put("actionInfoId", "");
		long sampleCount  =0;
		long count = 0;
		long originalCount = graphInfo.getOriginalResultSetSize();
		try {
			count = smartenService.getRowCount();
		} catch (CubeException e) {
			// TODO Auto-generated catch block
			//ApplicationLog.error(e);
		}
		//bcz getRowCount will get 100000 olnly when the data is above it(default sampling)
		/*if(graphInfo.isDefaultSampling())//
			count = graphInfo.getDefaultSamplingCount();*/
		
		
		//--------------------------sampling validation for JS -------------------------------------//
		boolean isD3PieRadar = false;
		boolean isTabular = false;
		boolean isAmchart = false;
		long mainResultSetCount = graphInfo.getOriginalResultSetSize();

		if(graphInfo.isSmartenTabular())
			isTabular = true;
		else if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
				graphInfo.getGraphType() == GraphConstants.D3_TREEMAP || graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP)
		{
			isD3PieRadar = true;
		}
		else
			isAmchart = true;

		modelMap.put("isD3PieRadar",isD3PieRadar);
		modelMap.put("selectedGraphType", graphInfo.getGraphType());
		modelMap.put("isTabular",isTabular);
		modelMap.put("isAmchart",isAmchart);
		
		modelMap.put("paginationCB",graphInfo.getGraphProperties().isPaginationCB());
		modelMap.put("samplingCB",graphInfo.getGraphProperties().isSamplingCB());
		modelMap.put("snapShotCB",graphInfo.getGraphProperties().isSnapShotSamplingCB());
		modelMap.put("mainResultSetCount",mainResultSetCount);
		modelMap.put("graphTypeForCount",graphInfo.getGraphType());
		//--------------------------sampling validation for JS ------------------------------------//
		
		//=====================Snaphsot enabled===================================================//
		if(graphInfo.getGraphProperties().isSnapShotSampling())
		{
			try {
				count = smartenService.getRowCount();
			} catch (CubeException e) {
				// TODO Auto-generated catch block
				ApplicationLog.error(e);
			}
			float perc = (float)(count*100)/originalCount;
			smartenSampling.setManualPer(perc);
			smartenSampling.setAutoPer(perc);
			smartenSampling.setSampleSizeAuto((long)count);
			smartenSampling.setSampleSizeManual((long)count);
		}
		//=====================Snaphsot enabled===================================================//
		
		
		
		
		
		
		if(smartenSampling.getManualPer() == 100)//first time manual
		{
		
			double tmp = count;
			smartenSampling.setManualPer(smartenSampling.getManualPer());
			smartenSampling.setSampalingMethodManual(smartenSampling.getSampalingMethodManual());
			smartenSampling.setSampleByManual(smartenSampling.getSampleByManual());
			smartenSampling.setSampleSizeManual((long)tmp);
			count = (long)tmp;
		}
		//-----------------------Auto/Manual------------------------------//
		if(smartenSampling.getSamplingMode() == 1)//Auto
		{
			if(smartenSampling.getAutoPer() >0 && smartenSampling.getAutoPer() <= 1)
			{
				smartenSampling.setPer(smartenSampling.getAutoPer()*100);
				smartenSampling.setAutoPer(smartenSampling.getAutoPer()*100);
			}
			else
				smartenSampling.setPer(smartenSampling.getAutoPer());
			smartenSampling.setSampalingMethod(smartenSampling.getSampalingMethodAuto());
			smartenSampling.setSampleBy(smartenSampling.getSampleByAuto());
			smartenSampling.setSampleSize(smartenSampling.getSampleSizeAuto());
			count = smartenSampling.getSampleSizeAuto();
		}
		else
		{
				if(smartenSampling.getManualPer() >0 && smartenSampling.getManualPer() <= 1)
					smartenSampling.setPer(smartenSampling.getManualPer()*100);
				else
					smartenSampling.setPer(smartenSampling.getManualPer());
				smartenSampling.setSampalingMethod(smartenSampling.getSampalingMethodManual());
				smartenSampling.setSampleBy(smartenSampling.getSampleByManual());
				smartenSampling.setSampleSize(smartenSampling.getSampleSizeManual());
				count = smartenSampling.getSampleSizeManual();
		}
		//-----------------------Auto/Manual------------------------------//
		
	/*	if (smartenSampling == null){
			smartenSampling = new SmartenSampling();
			
			float per = 0;
			per = 50000000.0f / count;
				
			if (per > 30){
				per = 30;
			}
			sampleCount = (long) (count*per / 100);
			
			smartenSampling.setPer(per);;
			smartenSampling.setSampleSize(sampleCount);
		}*/
		/*else
		{
			count = smartenSampling.getSampleSize();
		}*/
		/*modelMap.put("per", per);
		modelMap.put("SampleRecCount", sampleCount);*/
		
		String sampleCount_f = ""+smartenSampling.getSampleSizeAuto();//samplingVo.getSampleSize()+"";
		if (sampleCount >= 1000){
			sampleCount_f = ((int)smartenSampling.getSampleSizeAuto()/1000)+"K";
		}
		modelMap.put("sampleCount_F", sampleCount_f);
		modelMap.put("smartenSampling", smartenSampling);
		modelMap.put("originalCount", originalCount);
		modelMap.put("count", count);
		List<FieldVo> fields= new ArrayList();
		
		for(int i=0;i<graphInfo.getDimensionTitleList().size();i++)
		{
			FieldVo fv = new FieldVo();
			fv.setName(graphInfo.getDimensionTitleList().get(i).toString());
			fields.add(fv);
		}
		modelMap.put("fields", fields);
		return new ModelAndView("smartenSample");
	}
	
	@RequestMapping("/showSmartenSamplingFilter")
	public ModelAndView showSmartenSamplingFilter(ModelMap modelMap,@LoggedInUser UserInfo userInfo, 
			@RequestParam(required=false, defaultValue="0")Long actionInfoId,
			@RequestParam(value="isEdit", defaultValue="false") boolean isEdit){

		SmartenSampling smartenSampling= graphInfo.getSmartenSampling();
		modelMap.put("actionInfoId", "");
		long sampleCount  =0;
		long count = 0;
		long originalCount = graphInfo.getOriginalResultSetSize();
		try {
			count = smartenService.getRowCount();
		} catch (CubeException e) {
			// TODO Auto-generated catch block
			//ApplicationLog.error(e);
		}
		//bcz getRowCount will get 100000 olnly when the data is above it(default sampling)
		/*if(graphInfo.isDefaultSampling())//
			count = graphInfo.getDefaultSamplingCount();*/
		
		
		//--------------------------sampling validation for JS -------------------------------------//
		boolean isD3PieRadar = false;
		boolean isTabular = false;
		boolean isAmchart = false;
		long mainResultSetCount = graphInfo.getOriginalResultSetSize();

		if(graphInfo.isSmartenTabular())
			isTabular = true;
		else if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
				graphInfo.getGraphType() == GraphConstants.D3_TREEMAP || graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP)
		{
			isD3PieRadar = true;
		}
		else
			isAmchart = true;

		modelMap.put("isD3PieRadar",isD3PieRadar);
		modelMap.put("selectedGraphType", graphInfo.getGraphType());
		modelMap.put("isTabular",isTabular);
		modelMap.put("isAmchart",isAmchart);
		
		modelMap.put("paginationCB",graphInfo.getGraphProperties().isPaginationCB());
		modelMap.put("samplingCB",graphInfo.getGraphProperties().isSamplingCB());
		modelMap.put("snapShotCB",graphInfo.getGraphProperties().isSnapShotSamplingCB());
		modelMap.put("mainResultSetCount",mainResultSetCount);
		modelMap.put("graphTypeForCount",graphInfo.getGraphType());
		//--------------------------sampling validation for JS ------------------------------------//
		
		//=====================Snaphsot enabled===================================================//
		if(graphInfo.getGraphProperties().isSnapShotSampling())
		{
			try {
				count = smartenService.getRowCount();
			} catch (CubeException e) {
				// TODO Auto-generated catch block
				ApplicationLog.error(e);
			}
			float perc = (float)(count*100)/originalCount;
			smartenSampling.setManualPer(perc);
			smartenSampling.setAutoPer(perc);
			smartenSampling.setSampleSizeAuto((long)count);
			smartenSampling.setSampleSizeManual((long)count);
		}
		//=====================Snaphsot enabled===================================================//
		
		
		
		
		
		
		if(smartenSampling.getManualPer() == 100)//first time manual
		{
		
			double tmp = count;
			smartenSampling.setManualPer(smartenSampling.getManualPer());
			smartenSampling.setSampalingMethodManual(smartenSampling.getSampalingMethodManual());
			smartenSampling.setSampleByManual(smartenSampling.getSampleByManual());
			smartenSampling.setSampleSizeManual((long)tmp);
			count = (long)tmp;
		}
		//-----------------------Auto/Manual------------------------------//
		if(smartenSampling.getSamplingMode() == 1)//Auto
		{
			if(smartenSampling.getAutoPer() >0 && smartenSampling.getAutoPer() <= 1)
			{
				smartenSampling.setPer(smartenSampling.getAutoPer()*100);
				smartenSampling.setAutoPer(smartenSampling.getAutoPer()*100);
			}
			else
				smartenSampling.setPer(smartenSampling.getAutoPer());
			smartenSampling.setSampalingMethod(smartenSampling.getSampalingMethodAuto());
			smartenSampling.setSampleBy(smartenSampling.getSampleByAuto());
			smartenSampling.setSampleSize(smartenSampling.getSampleSizeAuto());
			count = smartenSampling.getSampleSizeAuto();
		}
		else
		{
				if(smartenSampling.getManualPer() >0 && smartenSampling.getManualPer() <= 1)
					smartenSampling.setPer(smartenSampling.getManualPer()*100);
				else
					smartenSampling.setPer(smartenSampling.getManualPer());
				smartenSampling.setSampalingMethod(smartenSampling.getSampalingMethodManual());
				smartenSampling.setSampleBy(smartenSampling.getSampleByManual());
				smartenSampling.setSampleSize(smartenSampling.getSampleSizeManual());
				count = smartenSampling.getSampleSizeManual();
		}
		//-----------------------Auto/Manual------------------------------//
		
	/*	if (smartenSampling == null){
			smartenSampling = new SmartenSampling();
			
			float per = 0;
			per = 50000000.0f / count;
				
			if (per > 30){
				per = 30;
			}
			sampleCount = (long) (count*per / 100);
			
			smartenSampling.setPer(per);;
			smartenSampling.setSampleSize(sampleCount);
		}*/
		/*else
		{
			count = smartenSampling.getSampleSize();
		}*/
		/*modelMap.put("per", per);
		modelMap.put("SampleRecCount", sampleCount);*/
		
		String sampleCount_f = ""+smartenSampling.getSampleSizeAuto();//samplingVo.getSampleSize()+"";
		if (sampleCount >= 1000){
			sampleCount_f = ((int)smartenSampling.getSampleSizeAuto()/1000)+"K";
		}
		modelMap.put("sampleCount_F", sampleCount_f);
		modelMap.put("smartenSampling", smartenSampling);
		modelMap.put("originalCount", originalCount);
		modelMap.put("count", count);
		List<FieldVo> fields= new ArrayList();
		
		for(int i=0;i<graphInfo.getDimensionTitleList().size();i++)
		{
			FieldVo fv = new FieldVo();
			fv.setName(graphInfo.getDimensionTitleList().get(i).toString());
			fields.add(fv);
		}
		modelMap.put("fields", fields);
		modelMap.put("samplingApplied",graphInfo.getGraphProperties().isSampling());
		
		if(isEdit) {
			modelMap.put("isForEdit", isEdit);
			return new ModelAndView("filters/addEditSampling");
		}else {
			return new ModelAndView("filters/samplingList");
		}
	}
	
	@RequestMapping("/applySampling")
	@ResponseBody
	public Object applySampling(@ModelAttribute SmartenSampling smartenSampling, @LoggedInUser UserInfo userInfo
			,ModelMap map, 
			@RequestParam(value = "autoPer", required = false) float autoPer,
			@RequestParam(value = "manualPer", required = false) float manualPer,
			@RequestParam(value = "sampleSizeManual", required = false) long sampleSizeManual,
			@RequestParam(value = "sampleSizeAuto", required = false) long sampleSizeAuto,
			@RequestParam(value = "sampalingMethodAuto", required = false) short sampalingMethodAuto,
			@RequestParam(value = "sampalingMethodManual", required = false) short sampalingMethodManual,
			@RequestParam(value = "sampleByManual", required = false) short sampleByManual,
			@RequestParam(value = "sampleByAuto", required = false) short sampleByAuto,
			@RequestParam(value = "isFromFilter", required = false, defaultValue="false") boolean isFromFilter,
			HttpServletResponse response, HttpServletRequest request){
		Map allDimMap = graphInfo.getDimensionValueCountMap();
		int allDimMapSize = allDimMap.size();
		int mapCounter = 0;
		String stratifiedColumnName = "";
		if(autoPer <= 1)
			autoPer = autoPer*100; 
		if(manualPer <= 1)
			manualPer = manualPer*100;
		
		graphInfo.setFromApplySampling(true);
		//------------------sampling checkBox start-------------------------------------------------//
		HashMap<String, String> requestParamMap = new HashMap<String, String>();
		Enumeration<String> requestEnum = request.getParameterNames();
		while (requestEnum.hasMoreElements()) {
			String paramName = requestEnum.nextElement();
			String paramValue = StringUtil.null2String(request.getParameter(paramName));
			requestParamMap.put(paramName, paramValue);
		}
		
		if(requestParamMap.get("outlinerPaginationCB") != null && requestParamMap.get("outlinerPaginationCB").equalsIgnoreCase("true"))
			graphInfo.getGraphProperties().setPaginationCB(true);
		else
			graphInfo.getGraphProperties().setPaginationCB(false);
		
		if(requestParamMap.get("outlinerSamplingCB") != null && requestParamMap.get("outlinerSamplingCB").equalsIgnoreCase("true"))
			graphInfo.getGraphProperties().setSamplingCB(true);
		else
			graphInfo.getGraphProperties().setSamplingCB(false);
		
		if(requestParamMap.get("outlinerSnapshotCB") != null && requestParamMap.get("outlinerSnapshotCB").equalsIgnoreCase("true"))
			graphInfo.getGraphProperties().setSnapShotSamplingCB(true);
		else
			graphInfo.getGraphProperties().setSnapShotSamplingCB(false);
		
		/*if(requestParamMap.get("outlinerPaginationCB") != null && requestParamMap.get("outlinerPaginationCB").equalsIgnoreCase("true")
				&& requestParamMap.get("outlinerSamplingCB") != null && requestParamMap.get("outlinerSamplingCB").equalsIgnoreCase("true")
				&& requestParamMap.get("outlinerSnapshotCB") != null && requestParamMap.get("outlinerSnapshotCB").equalsIgnoreCase("true"))
		{
			graphInfo.setSmartSamplingEnable(true);
		}
		else*/
			graphInfo.setSmartSamplingEnable(false);
		//------------------sampling checkBox end-------------------------------------------------//
		
		graphInfo.setSamplingActivity(true);
		graphInfo.getGraphProperties().setSamplingSnapShotChanged(true);
		graphInfo.getGraphProperties().setSnapShotSampling(false);
		
		//----------------------for date dim---------------------------//
		/*
		 * If the smallest dimension is date(numeric) then go for random sampling.
		 */
		Iterator it = (Iterator)allDimMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry pair = (Map.Entry)it.next();
			if(mapCounter == (allDimMapSize - 1))
				stratifiedColumnName = pair.getKey().toString();
			mapCounter++;
		}
		if(stratifiedColumnName != "" && !stratifiedColumnName.equals(""))
		{
			int columnType = CubeUtil.getColumnType(stratifiedColumnName, graphInfo.getCubeInfo());
			boolean isStringData = columnType == Types.VARCHAR || columnType == Types.LONGVARCHAR || columnType == Types.CHAR;
			if(!isStringData)
			{
				smartenSampling.setSampalingMethod(SmartenConstants.SAMPLING_RANDOM_SAMPLING);
			}
		}
		//----------------------for date dim---------------------------//
		
		
		//------------------------Auto/Manual----------------------//
		if(smartenSampling.getSamplingMode() == 1)//Auto
		{
			smartenSampling.setAutoPer(smartenSampling.getPer());
			smartenSampling.setSampalingMethodAuto(smartenSampling.getSampalingMethod());
			smartenSampling.setSampleByAuto(smartenSampling.getSampleBy());
			smartenSampling.setSampleSizeAuto(smartenSampling.getSampleSize());
			
			//-----prev values-------------//
			smartenSampling.setManualPer(manualPer);
			smartenSampling.setSampalingMethodManual(sampalingMethodManual);
			smartenSampling.setSampleByManual(sampleByManual);
			smartenSampling.setSampleSizeManual(sampleSizeManual);
		}
		else
		{
			smartenSampling.setManualPer(smartenSampling.getPer());
			smartenSampling.setSampalingMethodManual(smartenSampling.getSampalingMethod());
			smartenSampling.setSampleByManual(smartenSampling.getSampleBy());
			smartenSampling.setSampleSizeManual(smartenSampling.getSampleSize());
			
			//-----prev values-------------//
			smartenSampling.setAutoPer(autoPer);
			smartenSampling.setSampalingMethodAuto(sampalingMethodAuto);
			smartenSampling.setSampleByAuto(sampleByAuto);
			smartenSampling.setSampleSizeAuto(sampleSizeAuto);
		}
		//------------------------Auto/Manual----------------------//
		graphInfo.getGraphProperties().setSampling(true);
		smartenSampling.setApplied(false);
		graphInfo.setSmartenSampling(smartenSampling);
		graphInfo.setDefaultSampling(false);
		if(isFromFilter) {
			return AppConstants.SUCCESS_STATUS;
		}else {
			return refreshObjectData(null, response, userInfo, map);
		}
	}
	
	
	
	private int getSmartenColorSizeForAuto() {
		int autoCount = graphInfo.getGraphData().getAutoRangeDivValue();
		String row = graphInfo.getGraphData().getRowLabel();
		int noOfMeasure = graphInfo.getDataColLabels3().size();
		boolean isStackedChart = (graphInfo.getGraphType() == GraphConstants.STACKED_HBAR_GRAPH
				|| graphInfo.getGraphType() == GraphConstants.STACKED_VBAR_GRAPH); 
		
		if(row != null && !row.equals("") && row.equalsIgnoreCase("legend") && noOfMeasure > 1)//col with multiple row;
			autoCount = noOfMeasure;
		
		if(graphInfo.getGraphType() == SmartenConstants.HISTOGRAM_GRAPH)
			autoCount = graphInfo.getGraphProperties().getHistogram().getNoOfBars();
		if(graphInfo.getGraphType() == SmartenConstants.HEAT_MAP_GRAPH)
			autoCount = graphInfo.getGraphProperties().getHeatmap().getNoOfRanges();
		
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{	
			if(row != null && !row.equals("") && row.equalsIgnoreCase("legend") && noOfMeasure > 1)//Bcz color
				autoCount = graphInfo.getGraphData().getRowList().size();
		}
		
		if(graphInfo.getGraphType() == SmartenConstants.BUBBLE_GRAPH)
		{
			if(graphInfo.getGraphData().getRowList().size() > 0)
				autoCount = graphInfo.getGraphData().getRowList().size();
			else
				autoCount = graphInfo.getGraphData().getColList().size();
		}
		if(graphInfo.getGraphData().isRowMeasure() && ! (graphInfo.getGraphType() == SmartenConstants.PIE_GRAPH || graphInfo.getGraphType() == SmartenConstants.BUBBLE_GRAPH || graphInfo.getGraphType() == SmartenConstants.COMBINED_GRAPH || graphInfo.getGraphType() == SmartenConstants.HEAT_MAP_GRAPH))
		{
			int indexInData3 = graphInfo.getDataColLabels3().indexOf(graphInfo.getGraphData().getColorMeasureLabel());
			int noOfRanges = graphInfo.getGraphProperties().getyAxisPropertiesMap().get("M"+indexInData3).getRangeBucket().getNoOfRanges();
			autoCount = noOfRanges;
		}
		if(graphInfo.isSmartenMap())//Added for Map 4 Dec 2017
		{
			autoCount = graphInfo.getDimensionValueCountMap().get(graphInfo.getDimensionTitleList().get(0).toString());
		}
		if(graphInfo.getGraphType() == GraphConstants.D3_CHORD
				|| graphInfo.getGraphType() == GraphConstants.D3_TREEMAP
				|| graphInfo.getGraphType() == GraphConstants.D3_SUNBURST
				|| graphInfo.getGraphType() == GraphConstants.D3_BUBBLE
				|| graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT)
		{	
			autoCount = 30; // Bug #14941 Changes By [p.p]
		}
		return autoCount;
	}

	/*@RequestMapping (value = "/saveSmartenCustomColor")
	@ResponseBody
	public ModelAndView saveSmartenCustomColor(@ModelAttribute GraphProperties smartenLabelProperties,
			@RequestParam(required = false, value="rangeColorList") String rangeColorList
			,ModelMap map, HttpServletResponse response, HttpServletRequest request,@LoggedInUser UserInfo userInfo)
	{
		auditUserActionLog("Apply smarten custom color", AppConstants.DETAIL,userInfo);
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		graphInfo.getGraphProperties().setSmartenColorProperties(smartenLabelProperties.getSmartenColorProperties());
		graphInfo.getGraphProperties().setColorType(smartenLabelProperties.getSmartenColorProperties().getColorType());
		graphInfo.getGraphProperties().setColor(smartenLabelProperties.getSmartenColorProperties().getColor());
		graphInfo.getGraphProperties().setCustomColors(smartenLabelProperties.getSmartenColorProperties().getCustomColors());
		graphInfo.getGraphProperties().setTranceperancy(smartenLabelProperties.getSmartenColorProperties().getTranceperancy());
		graphInfo.getGraphProperties().setRangeColorDivValue(smartenLabelProperties.getSmartenColorProperties().getRangeColorDivValue());
		//graphInfo.getGraphProperties().setRangeColorList(smartenLabelProperties.getSmartenColorProperties().getRangeColorList());
		graphInfo.getGraphProperties().setRangeColorList(Arrays.asList(rangeColorList.split(",")));
		graphInfo.getGraphProperties().setRangeEndColor(smartenLabelProperties.getSmartenColorProperties().getRangeEndColor());
		graphInfo.getGraphProperties().setRangeStartColor(smartenLabelProperties.getSmartenColorProperties().getRangeStartColor());
		graphInfo.getGraphProperties().setColorRange(smartenLabelProperties.getSmartenColorProperties().getColorRange());//setAutoRangeColor(smartenLabelProperties.getSmartenColorProperties().isAutoRangeColor());
		return refreshObjectData(null,response, userInfo, map);
	}*/
	
	@RequestMapping (value = "/quickSettings")
	@ResponseBody
	public ModelAndView quickSettings(HttpServletRequest request, HttpServletResponse response,@LoggedInUser UserInfo userInfo,
			ModelMap map, @RequestParam(value = "dataValue", required = false) boolean dataValue
			,@RequestParam(value = "mouseOver", required = false) boolean mouseOver
			,@RequestParam(value = "drillOnLegend", required = false) boolean drillOnLegend
			,@RequestParam(value = "legend", required = false) boolean legend
			,@RequestParam(value = "cmbLegend", required = false) boolean cmbLegend
			,@RequestParam(value = "zoom", required = false) boolean zoom
			,@RequestParam(value = "barDataValue", required = false) boolean barDataValue
			,@RequestParam(value = "drillOnLegendCmb", required = false) boolean drillOnLegendCmb
			,@RequestParam(value = "mouseOverCmb", required = false) boolean mouseOverCmb
			,@RequestParam(value = "lineDataValue", required = false) boolean lineDataValue){
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Quick settings","Set quick settings",Thread.currentThread(),userInfo,null);
		//DataValue and MouseOver
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			for (int i = 0; i < graphInfo.getGraphProperties().getDataValuePropertiesMap().size(); i++) {
				graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+i).getDataValuePoint().setDataValuePointVisible(dataValue);
				//graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+i).getDataValuePoint().setPosition("Inside");
				graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+i).getDataValueMouseOver().setMouseOverTextEnable(mouseOver);
			}
		}
		else
		{
			if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH)
			{
				graphInfo.getGraphProperties().getCombinedDataValueProperties().getBardataValuePoint().setDataValuePointVisible(barDataValue);
				graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinedataValuePoint().setDataValuePointVisible(lineDataValue);
				graphInfo.getGraphProperties().getCombinedDataValueProperties().getBardataValueMouseOver().setMouseOverTextEnable(mouseOverCmb);
				graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinedataValueMouseOver().setMouseOverTextEnable(mouseOverCmb);;
				graphInfo.getGraphProperties().getCombinedGraph().getBarLegendProperties().getLegendPanelProperties().setDrillDown(drillOnLegendCmb);
			}
		}
		
		
		
		//MouseOver
		if(graphInfo.getGraphType() != GraphConstants.COMBINED_GRAPH)
		{
			graphInfo.getGraphProperties().getDataValueProperties().getDataValuePoint().setDataValuePointVisible(dataValue);
			graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().setMouseOverTextEnable(mouseOver);
			//For gauges(as discussed with chintan sir for quick settings)
			graphInfo.getGraphProperties().getGaugeDataValueZone().getDataValueConfiguration().setVisible(dataValue);
		}
		
		
		//Drill on legend
		graphInfo.getGraphProperties().getLegendProperties().getLegendPanelProperties().setDrillDown(drillOnLegend);
	
		//Legend
		if(graphInfo.getGraphType() != GraphConstants.COMBINED_GRAPH)
		{
			graphInfo.getGraphProperties().getLegendProperties().getLegendPanelProperties().setLegendPanelVisible(legend);
		}
		else
		{
			graphInfo.getGraphProperties().getCombinedGraph().getBarLegendProperties().getLegendPanelProperties().setLegendPanelVisible(cmbLegend);
		}
		
		//Zoom
		graphInfo.getGraphProperties().getGraphAreaProperties().getGraphChartCursor().setEnable(zoom);
		
		request.setAttribute("forceRefresh", true);
		return refreshObjectData(request, response,userInfo, map);
	}
	
	@RequestMapping (value = "/editReferenceLinePropObj")
	@ResponseBody
	public ModelAndView editReferenceLinePropObj(ModelMap modelMap,@RequestParam("referencelineobjkey") String strReferencelineobjkey,
			@RequestParam("fromGraph") String strfromGraph,@LoggedInUser UserInfo userInfo)	{
		Map<Integer,ReferenceLine> referencelinePropertiesMap = null;	
		String objectsview = "";
		if(strReferencelineobjkey != null && !strReferencelineobjkey.equalsIgnoreCase("")){	
				if(strfromGraph != null && !strfromGraph.equalsIgnoreCase(""))
				{
					if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_GENERAL)) {
						if(graphInfo.getGraphProperties().getReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getReferencelinePropertiesMap().isEmpty()){
							referencelinePropertiesMap = graphInfo.getGraphProperties().getReferencelinePropertiesMap();
							
							objectsview = "/editReferenceLine";
						}
					}
					else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
						if(graphInfo.getGraphProperties().getBarReferencelinePropertiesMap()!=null && !graphInfo.getGraphProperties().getBarReferencelinePropertiesMap().isEmpty()){
							referencelinePropertiesMap = graphInfo.getGraphProperties().getBarReferencelinePropertiesMap();
							
							objectsview = "/editbarReferenceLine";
						}
					}
					else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
						if(graphInfo.getGraphProperties().getLineReferencelinePropertiesMap()!=null && !graphInfo.getGraphProperties().getLineReferencelinePropertiesMap().isEmpty())
						{
							referencelinePropertiesMap = graphInfo.getGraphProperties().getLineReferencelinePropertiesMap();
							
							objectsview = "/editLineRefenrenceLine";
						}
					}
					String referencelineobjLabel = "";
					String referencelineobjValue = "";
					String referencelineobjStyle = "";
					int referencelineobjWidth = 0;
					String referencelineobjColor = "";
					int referencelineobjKey = 0;
					
					if(referencelinePropertiesMap!=null && !referencelinePropertiesMap.isEmpty()){
						referencelineobjLabel = referencelinePropertiesMap.get(Integer.parseInt(strReferencelineobjkey)).getLabel();
						referencelineobjValue = referencelinePropertiesMap.get(Integer.parseInt(strReferencelineobjkey)).getValue();
						referencelineobjStyle = referencelinePropertiesMap.get(Integer.parseInt(strReferencelineobjkey)).getStyle();
						referencelineobjWidth = referencelinePropertiesMap.get(Integer.parseInt(strReferencelineobjkey)).getWidth();
						referencelineobjColor = referencelinePropertiesMap.get(Integer.parseInt(strReferencelineobjkey)).getColor();
						referencelineobjKey = Integer.parseInt(strReferencelineobjkey);
					}
					
					modelMap.put("referencelineobjLabel",referencelineobjLabel);
					modelMap.put("referencelineobjValue",referencelineobjValue);
					modelMap.put("referencelineobjStyle",referencelineobjStyle);
					modelMap.put("referencelineobjWidth",referencelineobjWidth);
					modelMap.put("referencelineobjColor",referencelineobjColor);
					modelMap.put("referencelineobjKey",referencelineobjKey);
				}
		}
		auditUserActionLog(ResourceManager.getString("LBL_EDIT_GRAPH_REFERENCE_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph"+objectsview);
	}
	
	@RequestMapping (value = "/updateReferenceLineProperties")
	@ResponseBody
	public ModelAndView updateReferenceLineProperties(ModelMap modelMap,@RequestParam("referencelineobjkey") String strReferencelineobjkey
			,@RequestParam("referencelinename") String strReferencelinename
			,@RequestParam("referencelinevalue") String strReferencelinevalue	
			,@RequestParam("referencelinestyle") String strReferencelinestyle
			,@RequestParam("referencelinewidth") String strReferencelinewidth
			,@RequestParam("referencelinecolor") String strReferencelinecolor
			,@RequestParam("fromGraph") String strfromGraph,@LoggedInUser UserInfo userInfo) {
		if(strfromGraph != null && !strfromGraph.equalsIgnoreCase("")) {
			ReferenceLine referencelineProperties = new ReferenceLine();
			String objectsview = "";
			if(strReferencelinename != null && !strReferencelinename.equalsIgnoreCase(""))
			{
				referencelineProperties.setLabel(strReferencelinename);
			}
			if(strReferencelinevalue != null && !strReferencelinevalue.equalsIgnoreCase(""))
			{
				referencelineProperties.setValue(strReferencelinevalue);
			}
			if(strReferencelinestyle != null && !strReferencelinestyle.equalsIgnoreCase(""))
			{
				referencelineProperties.setStyle(strReferencelinestyle);
			}
			if(strReferencelinewidth != null && !strReferencelinewidth.equalsIgnoreCase(""))
			{
				referencelineProperties.setWidth(Integer.parseInt(strReferencelinewidth));
			}
			if(strReferencelinecolor != null && !strReferencelinecolor.equalsIgnoreCase(""))
			{
				referencelineProperties.setColor(strReferencelinecolor);
			}		
			Map<Integer,ReferenceLine> referencelinePropertiesMap  = null;
			
				if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_GENERAL)) {
					if(graphInfo.getGraphProperties().getReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getReferencelinePropertiesMap().isEmpty()) {
						referencelinePropertiesMap = graphInfo.getGraphProperties().getReferencelinePropertiesMap();
					}
					else
					{
						referencelinePropertiesMap = new HashMap<Integer, ReferenceLine>();
					}
				}
				else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR)) {
					if(graphInfo.getGraphProperties().getBarReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getBarReferencelinePropertiesMap().isEmpty()) {
						referencelinePropertiesMap = graphInfo.getGraphProperties().getBarReferencelinePropertiesMap();
					}
					else
					{
						referencelinePropertiesMap = new HashMap<Integer, ReferenceLine>();
					}
				}
				else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE)) {
					if(graphInfo.getGraphProperties().getLineReferencelinePropertiesMap()!= null && !graphInfo.getGraphProperties().getLineReferencelinePropertiesMap().isEmpty()){
						referencelinePropertiesMap = graphInfo.getGraphProperties().getLineReferencelinePropertiesMap();
					}
					else
					{
						referencelinePropertiesMap = new HashMap<Integer, ReferenceLine>();
					}
				}			
			if(referencelinePropertiesMap!= null)
			{
				if(strReferencelineobjkey != null && !strReferencelineobjkey.equalsIgnoreCase(""))
				{
					referencelinePropertiesMap.put(Integer.parseInt(strReferencelineobjkey),referencelineProperties);
				}
			}
			if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_GENERAL)) {						
				modelMap.put("referencelinePropertiesMap",referencelinePropertiesMap);					
				graphInfo.getGraphProperties().setReferencelinePropertiesMap(referencelinePropertiesMap);
				objectsview = "/referencelineobjects";
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_BAR))
			{
				modelMap.put("barreferencelinePropertiesMap",referencelinePropertiesMap);				
				graphInfo.getGraphProperties().setBarReferencelinePropertiesMap(referencelinePropertiesMap);	
				objectsview = "/barReferencelineobjects";
			}
			else if(strfromGraph.equalsIgnoreCase(AppConstants.GRAPH_LINE))
			{
				modelMap.put("linereferencelinePropertiesMap",referencelinePropertiesMap);				
				graphInfo.getGraphProperties().setLineReferencelinePropertiesMap(referencelinePropertiesMap);
				objectsview = "/lineReferencelineobjects";
			}	
			auditUserActionLog(ResourceManager.getString("LBL_ADD_GRAPH_REFERENCE_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
			return new ModelAndView("graph"+objectsview);
		}
		else
		{
			Map<Integer,ReferenceLine> referencelinePropertiesMap  = new HashMap<Integer, ReferenceLine>();
			modelMap.put("referencelinePropertiesMap",referencelinePropertiesMap);
			auditUserActionLog(ResourceManager.getString("LBL_UPDATE_REFERENCE_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
			return new ModelAndView("graph/referencelineobjects");
		}
	}
	
	@RequestMapping (value = "/showDatavalueMobile")
	@ResponseBody
	public ModelAndView showDatavalueMobile(HttpServletRequest request, HttpServletResponse response,@LoggedInUser UserInfo userInfo,
			ModelMap map, @RequestParam(value = "showAllDataValue", required = false) boolean showAllDataValue) {

		boolean dataValue = graphInfo.getGraphProperties().getDataValueProperties().getDataValuePoint().isDataValuePointVisible();
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			for (int i = 0; i < graphInfo.getGraphProperties().getDataValuePropertiesMap().size(); i++) {
				dataValue = graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+i).getDataValuePoint().isDataValuePointVisible();
				graphInfo.getGraphProperties().getDataValuePropertiesMap().get("M"+i).getDataValuePoint().setDataValuePointVisible(!dataValue);
			}
		}
		else if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH)
		{
			dataValue = graphInfo.getGraphProperties().getCombinedDataValueProperties().getBardataValuePoint().isDataValuePointVisible();
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getBardataValuePoint().setDataValuePointVisible(!dataValue);
			graphInfo.getGraphProperties().getCombinedDataValueProperties().getLinedataValuePoint().setDataValuePointVisible(!dataValue);
		}else if(graphInfo.getGraphType() != GraphConstants.COMBINED_GRAPH) {		
			graphInfo.getGraphProperties().getDataValueProperties().getDataValuePoint().setDataValuePointVisible(!dataValue);
		}

		request.setAttribute("forceRefresh", true);
		map.put("showAllDataValue", showAllDataValue);
		return refreshObjectData(null,response, userInfo, map);
		/*return refreshObjectData(request, response,userInfo, map);*/
	}
	
	
	@ResponseBody
	@RequestMapping ("/drillDown")
	public Object performDrillDown(
			@RequestParam (value = "colName", required = false) String columnName,
			@RequestParam (value = "colValue", required = false) String columnValue,
			@RequestParam (value = "isHomePage", required = false) String homePageFlag,
			@RequestParam (value = "shortCutGraph", required = false) String shortCutGraph,
			@RequestParam (value = "isRow", required = false) String rowDrilldownFlag,
			@RequestParam (value = "isColumn", required = false) String columnDrilldownFlag, 
			@RequestParam (value = "type", required = false) String type, 
			@RequestParam (value = "drillType", required = false) String drillType,
			@RequestParam (value = "isApply", required = false) String isApply,
			@RequestParam (value = "isTreeDrillDown", required = false) String drillDownBrowsingFlag,
			@LoggedInUser UserInfo loggedInUser, HttpServletResponse response, ModelMap map) {

		Object responseText = "";

		Map<String, String> params = new HashMap<String, String>();
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"drill down","set drill down",Thread.currentThread(),loggedInUser,null);
		params.put("colName", columnName);
		params.put("colValue", columnValue);
		params.put("isHomePage", homePageFlag);
		params.put("shortCutGraph", shortCutGraph);
		params.put("isRow", rowDrilldownFlag);
		params.put("isColumn", columnDrilldownFlag);
		params.put("type", type);
		params.put("drillType", drillType);
		params.put("isApply", isApply);
		params.put("isTreeDrillDown", drillDownBrowsingFlag);

		try {
			smartenService.setGraphDrillDown(graphInfo, params, loggedInUser);

			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());

			boolean refreshReq = graphInfo.isRefreshReq();
			graphInfo.setRefreshReq(false);

			responseText = refreshObjectData(null,response, loggedInUser, map);

			graphInfo.setRefreshReq(refreshReq);
		} catch (RemoteException e) {
			responseText = ResourceManager.getString("ERROR_FAILED_TO_DRILLDOWN_DRILLUP", new Object[] {e.getMessage()});
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_DRILL_DOWN_OPERATION", new Object[] {
							loggedInUser.getUsername(), getObjectDisplayName() }), e);
		} catch (CubeException e) {
			responseText = ResourceManager.getString("ERROR_FAILED_TO_DRILLDOWN_DRILLUP", new Object[] {e.getMessage()});
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_DRILL_DOWN_OPERATION", new Object[] {
							loggedInUser.getUsername(), getObjectDisplayName() }), e);
		} catch (RScriptException e) {
			responseText = ResourceManager.getString("ERROR_FAILED_TO_DRILLDOWN_DRILLUP", new Object[] {e.getMessage()});
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_DRILL_DOWN_OPERATION", new Object[] {
							loggedInUser.getUsername(), getObjectDisplayName() }), e);
		}
		
		auditUserActionLog(ResourceManager.getString("LBL_PERFORM_GRAPH_DRILLDOWN"), AppConstants.DETAIL,loggedInUser);
		return responseText;
	}

	@ResponseBody
	@RequestMapping ("/drillUp")
	public Object performDrillUp(
			@RequestParam(value = "isDrillUpPosible", required = false) String isDrillUpPosible,
			@RequestParam(value = "isHomePage", required = false) String isHomePage,
			@RequestParam(value = "shortCutGraph", required = false) String shortCutGraph,
			@RequestParam(value = "rowName", required = false) String dimensionName,
			@RequestParam(value = "rowIndex", required = false) String rowIndex,
			@RequestParam(value = "colIndex", required = false) String colIndex,
			@RequestParam(value = "drillType", required = false) String drillType,
			@LoggedInUser UserInfo userInfo, HttpServletResponse response, ModelMap map) {

		Object responseText = "";
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"drill down","set drill down",Thread.currentThread(),userInfo,null);
		try {
			int iDrillType = 0;

			if (drillType != null && drillType.trim().length() > 0) {
				iDrillType = Integer.parseInt(drillType);
			}

			smartenService.setGraphDrillUp(graphInfo, dimensionName,
					Integer.parseInt(rowIndex), Integer.parseInt(colIndex),
					iDrillType, userInfo);

			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());

			boolean refreshReq = graphInfo.isRefreshReq();
			graphInfo.setRefreshReq(false);

			responseText = refreshObjectData(null,response, userInfo, map);

			graphInfo.setRefreshReq(refreshReq);
		} catch (Exception e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_DRILL_UP_OPERATION", new Object[] {
							userInfo.getUsername(), getObjectDisplayName() }), e);

			responseText = ResourceManager.getString("ERROR_FAILED_TO_DRILLDOWN_DRILLUP", new Object[] {e.getMessage()});
		}

		
		auditUserActionLog(ResourceManager.getString("LBL_PERFORM_GRAPH_DRILLUP"), AppConstants.DETAIL,userInfo);
	   	return responseText;
	}

	@Override
	public int getColumnTypeByColumnNameAndCubeId(String strCubeId, String columnName,HttpServletRequest request) throws ALSException, CubeException {
		
		return 0;
	}

	@Override
	public CubeVector getFilterConditionsForMultipleCube(String cubeId, String column,HttpServletRequest request) {
		
		return null;
	}
	
	@ResponseBody
	@RequestMapping ("/changeGraphType")
	public Object changeGraphType(
			@RequestParam(value = "graphType", required = true) Integer graphType,
			@RequestParam(value = "fromAnalysis", required = true) boolean fromAnalysis,
			@LoggedInUser UserInfo userInfo, HttpServletResponse response, ModelMap map) {

		Object responseText = "";
		long startTime = System.currentTimeMillis();
		graphInfo.setGraphType(graphType);
		graphInfo.setGraphTypeChanged(true);
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Change graph type","set Change graph type",Thread.currentThread(),userInfo,null);
				if(graphInfo.getGraphType() == GraphConstants.LINE_GRAPH
						|| graphInfo.getGraphType() == GraphConstants.STACKED_LINE_GRAPH
						|| graphInfo.getGraphType() == GraphConstants.PERCENTAGE_LINE_GRAPH)
				{
					graphInfo.getGraphProperties().setLineColorType(graphInfo.getGraphProperties().getColorType());
					graphInfo.getGraphProperties().setLineCustomColors(graphInfo.getGraphProperties().getCustomColors());
					graphInfo.getGraphProperties().setLinecolor(graphInfo.getGraphProperties().getColor());
				}
		try {
			graphInfo = smartenService.processGraphChangeType(graphInfo, userInfo);
			getDetailInfoMap().put(graphInfo.getGraphId(), graphInfo);
			
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());

			boolean refreshReq = graphInfo.isRefreshReq();
			graphInfo.setRefreshReq(false);

			responseText = refreshObjectData(null,response, userInfo, map);

			graphInfo.setRefreshReq(refreshReq);
		} catch (Exception ex) {
			String graphMode = (graphInfo.getGraphMode() == AppConstants.NEW_MODE) ? "New" : "Open";

			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_FAILED_TO_CHANGE_GRAPH", new Object[] {
							graphInfo.getGraphName(), graphMode }), ex);

			responseText = ResourceManager.getString("ERROR_FAILED_TO_CHANGE_GRAPH_TYPE",new Object[] {ex.getMessage()});
		}
		long endTime = System.currentTimeMillis();

		
		if(graphInfo.getGraphType().equals(GraphConstants.DOUGHNUT_GRAPH))
		{	
			map.put("changetoDoughnut", true);
		}
		
		graphInfo.getGraphData().setFromAnalysis(fromAnalysis);
		
		map.put("fromAnalysis", fromAnalysis);
		auditUserActionLog(ResourceManager.getString("LBL_CHANGE_GRAPH_TYPE"), AppConstants.DETAIL,userInfo);
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Change graph type","set Change graph type",Thread.currentThread(),userInfo,null);
		return responseText;
	}

	@RequestMapping (value = "/addGraphLineProperties")
	@ResponseBody
	public ModelAndView addGraphLineProperties(ModelMap modelMap
			,@RequestParam("style") String strStyle	
			,@RequestParam("thickness") String strThickness
			,@LoggedInUser UserInfo userInfo) {		
		GraphLineSettingProperties graphLineSettingProperties = new GraphLineSettingProperties();
		if(strStyle != null && !strStyle.equalsIgnoreCase(""))
		{			
			graphLineSettingProperties.setStyle(strStyle);
		}
		if(strThickness != null && !strThickness.equalsIgnoreCase(""))
		{
			graphLineSettingProperties.setThickness(strThickness);
		}
		graphInfo.getGraphProperties().getGraphLineProperties().setAllLineCompatibility(true);
		List<GraphLineSettingProperties>  graphlineSettingPropertiesList = null;
		if(graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList()!= null && !graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList().isEmpty())
		{
			graphlineSettingPropertiesList = graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList();
		}
		else
		{
			graphlineSettingPropertiesList = new ArrayList<GraphLineSettingProperties>();
		}
		if(graphlineSettingPropertiesList!= null)
		{
			graphlineSettingPropertiesList.add(graphLineSettingProperties);
		}		
		graphInfo.getGraphProperties().getGraphLineProperties().setGraphlinePropertiesList(graphlineSettingPropertiesList);
		modelMap.put("graphLineProperties",graphInfo.getGraphProperties().getGraphLineProperties());
		auditUserActionLog(ResourceManager.getString("LBL_ADD_GRAPH_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph/graphlineobjects");
	}
	
	@RequestMapping (value = "/removeGraphlinePropObj")
	@ResponseBody
	public ModelAndView removeGraphlinePropObj(ModelMap modelMap,@RequestParam("graphlineobjkey") String strGraphlineobjkey,@LoggedInUser UserInfo userInfo){
		List<GraphLineSettingProperties>  graphlineSettingPropertiesList = null;	
		if(strGraphlineobjkey != null && !strGraphlineobjkey.equalsIgnoreCase("")){
			if(graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList() != null && !graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList().isEmpty())
			{
				graphlineSettingPropertiesList = graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList();
				graphlineSettingPropertiesList.remove(Integer.parseInt(strGraphlineobjkey));								
				graphInfo.getGraphProperties().getGraphLineProperties().setGraphlinePropertiesList(graphlineSettingPropertiesList);
			}
		}
		modelMap.put("graphLineProperties",graphInfo.getGraphProperties().getGraphLineProperties());		
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_GRAPH_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph/graphlineobjects");
	}
	@RequestMapping (value = "/editGraphlinePropObj")
	@ResponseBody
	public ModelAndView editGraphlinePropObj(ModelMap modelMap,@RequestParam("graphlineobjkey") String strGraphlineobjkey,@RequestParam("graphlineobjstyle") String graphlineobjstyle,@RequestParam("graphlineobjthikness") String graphlineobjthikness,@LoggedInUser UserInfo userInfo){
		List<GraphLineSettingProperties>  graphlineSettingPropertiesList = null;	
		if(strGraphlineobjkey != null && !strGraphlineobjkey.equalsIgnoreCase("")){
			if(graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList() != null && !graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList().isEmpty())
			{
				graphlineSettingPropertiesList = graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList();
					
				graphlineSettingPropertiesList.get(Integer.parseInt(strGraphlineobjkey)).setStyle(graphlineobjstyle);
				graphlineSettingPropertiesList.get(Integer.parseInt(strGraphlineobjkey)).setThickness(graphlineobjthikness);
				graphInfo.getGraphProperties().getGraphLineProperties().setGraphlinePropertiesList(graphlineSettingPropertiesList);
			}
		}
		modelMap.put("graphLineProperties",graphInfo.getGraphProperties().getGraphLineProperties());		
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_GRAPH_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph/graphlineobjects");
	}

	@RequestMapping (value = "/editGraphlinePointPropObj")
	@ResponseBody
	public ModelAndView editGraphlinePointPropObj(ModelMap modelMap,@RequestParam("graphlinepointobjkey") String strGraphlineobjkey,@RequestParam("graphlinepointobjstyle") String graphlineobjstyle,@RequestParam("graphlineobjpointthikness") String graphlineobjthikness,@LoggedInUser UserInfo userInfo){
		List<GraphLineSettingProperties>  graphlineSettingPropertiesList = null;	
		if(strGraphlineobjkey != null && !strGraphlineobjkey.equalsIgnoreCase("")){
			if(graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList() != null && !graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList().isEmpty())
			{
				graphlineSettingPropertiesList = graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinePropertiesList();
					
				graphlineSettingPropertiesList.get(Integer.parseInt(strGraphlineobjkey)).setStyle(graphlineobjstyle);
				graphlineSettingPropertiesList.get(Integer.parseInt(strGraphlineobjkey)).setThickness(graphlineobjthikness);
				graphInfo.getGraphProperties().getGraphLineProperties().setGraphlinePropertiesList(graphlineSettingPropertiesList);
			}
		}
		modelMap.put("graphLineProperties",graphInfo.getGraphProperties().getGraphLineProperties());		
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_GRAPH_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph/graphlineobjects");
	}

	
	@Override
	public void applyKPIGroupTimeFilter(Map<String, String> requestParamMap, UserInfo userInfo) {}
	
	/**
	 * This method will saves graph object.
	 * 
	 * @param GraphInfo
	 *            graphInfo
	 * @param strFolderId
	 *            FolderId
	 * @return String Result of operation as Message
	 */
	@ResponseBody
	@RequestMapping(value = "/saveSmarten", method = RequestMethod.POST)
	public Object saveSmarten(
			@ModelAttribute (value = "graphInfo") SmartenInfo saveGraphInfo,
			@RequestParam(value = "destFolderId", required = false) String strFolderId,
			@RequestParam(value = "performSave", required = false) boolean performSave, 
			@RequestParam(value = "smartenHt", required = false) String smartenHt,
			@RequestParam(value = "smartenMEnable", required = false) boolean smartenMEnable,
			@LoggedInUser UserInfo userInfo, ModelMap modelMap, HttpServletResponse response) {

		String strUpdate = "";
		

		boolean smartenRowsEnableTemp = false;
		List smartenRowsListTemp = new ArrayList();
		boolean smartenColumnsEnableTemp= false;
		List smartenColsListTemp = new ArrayList();
		
		boolean smartenShapeEnableTemp= false;
		List smartenShapeListTemp = new ArrayList();
		String smartenShapeLabelTemp =null;
		
		boolean smartenSizeEnableTemp= false;
		List smartenSizeListTemp = new ArrayList();
		String smartenSizeLabelTemp = null;		
		List smartenSizeValueListTemp = new ArrayList();
		
		boolean smartenRowMeasureEnableTemp= false;		
		String smartenRowMeasureLabelTemp = null;		
		List smartenRowMeasureValueListTemp = new ArrayList();
		
		List tabularDataListTemp = new ArrayList();//Added for saving Tabular in SmartenView
		List dimensionListTemp = new ArrayList();
		List measureListTemp = new ArrayList();
		
		NumberFormat mouseOverNumberFormat = new NumberFormat();
		
		
		
		if(graphInfo != null)
		{
			smartenRowsEnableTemp = graphInfo.getGraphData().isSmartenRowsEnable();
			smartenRowsListTemp = graphInfo.getGraphData().getSmartenRowList();
			smartenColumnsEnableTemp = graphInfo.getGraphData().isSmartenColoumnsEnable();
			smartenColsListTemp = graphInfo.getGraphData().getSmartenColList();
			smartenShapeEnableTemp = graphInfo.getGraphData().isShapeMeasure();
			smartenShapeListTemp = graphInfo.getGraphData().getShapeList();
			if(graphInfo.getGraphData().getShapeLabel() != null && !graphInfo.getGraphData().getShapeLabel().equalsIgnoreCase(""))
				smartenShapeLabelTemp = graphInfo.getGraphData().getShapeLabel();
			smartenSizeEnableTemp = graphInfo.getGraphData().isSizeMeasure();
			smartenSizeListTemp = graphInfo.getGraphData().getSizeList();
			if(graphInfo.getGraphData().getSizeLabel() != null && !graphInfo.getGraphData().getSizeLabel().equalsIgnoreCase(""))
				smartenSizeLabelTemp = graphInfo.getGraphData().getSizeLabel();
			smartenSizeValueListTemp = graphInfo.getGraphData().getSizeValueList();
			
			if(graphInfo.getGraphData().getColorMeasureLabel() != null && !graphInfo.getGraphData().getColorMeasureLabel().equalsIgnoreCase(""))
				smartenRowMeasureLabelTemp = graphInfo.getGraphData().getColorMeasureLabel();
			smartenRowMeasureValueListTemp = graphInfo.getGraphData().getSizeValueList();
			
			smartenRowMeasureEnableTemp = graphInfo.getGraphData().isRowMeasure();
			
			
			if(graphInfo.getGraphData().getMeasureListForSmartenview() != null && !graphInfo.getGraphData().getMeasureListForSmartenview().isEmpty())
				measureListTemp = graphInfo.getGraphData().getMeasureListForSmartenview();
			if(graphInfo.getGraphData().getDimensionListForSmartenview() != null && !graphInfo.getGraphData().getDimensionListForSmartenview().isEmpty())
				dimensionListTemp = graphInfo.getGraphData().getDimensionListForSmartenview();
			if(graphInfo.getDimensionTitleList() != null && !graphInfo.getDimensionTitleList().isEmpty())
				dimensionListTemp = graphInfo.getDimensionTitleList();
			if(graphInfo.getMeasureTitleList() != null && !graphInfo.getMeasureTitleList().isEmpty())
				measureListTemp = graphInfo.getMeasureTitleList();
			if(graphInfo.getDataTitleList() != null && !graphInfo.getDataTitleList().isEmpty())
				tabularDataListTemp = graphInfo.getDataTitleList();
			
			graphInfo.setSmartenMode(smartenMEnable);
			
			if(null != graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat()) {
				mouseOverNumberFormat = graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().getMouseOverNumberFormat();
			}
		}
		
		
		int result =  0 ;
		FolderInfo folderInfo = null;
		graphInfo.setVersionId(Double.parseDouble(AppContextUtil.getVersion()));
		try{
			if(strFolderId != null && !strFolderId.isEmpty()){
			 folderInfo = repositoryServiceUtil.getFolder(strFolderId);
			} else {
				folderInfo = graphInfo.getFolderInfo();
			}
			result = accessRightServiceUtil.getFolderPermission(userInfo, folderInfo, false);
			if(result >= 2){
				//2018 merge
				if(graphInfo != null && smartenService.isDrillUpPossible(graphInfo))//12699
				{
					graphInfo.setLovListForColor(new ArrayList());
					graphInfo.setLovListForColorBar(new ArrayList());
					graphInfo.setLovListForColorLine(new ArrayList());
				}
				
				if (performSave) {
					
					try {
						boolean isDrillUpPossible = smartenService.isDrillUpPossible(graphInfo);
						Object responseStatus = null;
						smartenService.checkAndResetDrillUp(graphInfo, true);
						if (smartenService.saveGraph(graphInfo, userInfo, false) != null) {
							boolean refreshReq = graphInfo.isRefreshReq();
							if (isDrillUpPossible) {
								graphInfo.setRefreshReq(true);
							} else {
								graphInfo.setRefreshReq(false);
							}
							
							graphInfo.getGraphData().setDpListStartIndex(0);
							graphInfo.getGraphData().setLineDpListStartIndex(0);
							
							response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
							responseStatus = refreshObjectData(null,response, userInfo, modelMap);
							
							String graphId = graphInfo.getGraphId();
							String cacheFilePath = "";
							if(smartenService.isobjectCacheOnSingleFile()) {
								cacheFilePath = graphId.substring(0, graphId.indexOf(".")) + AppConstants.SMARTEN_CUBE_DATA_FILE_EXT;
							} else {
								cacheFilePath = graphId.substring(0, graphId.indexOf(".")+1) + userInfo.getUserId() + AppConstants.SMARTEN_CUBE_DATA_FILE_EXT;
							}
							smartenService.saveGraphCache(cacheFilePath,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
							
							graphInfo.setRefreshReq(refreshReq);
							
							auditUserActionLog(ResourceManager.getString("LBL_SAVE_OBJECT"), AppConstants.USER_ACCESS,userInfo);
							return responseStatus;
						} else {
							strUpdate = ResourceManager.getString("ERROR_FALITO_SAVE");
						}
					} catch (Exception e) {
						String graphMode = (graphInfo.getGraphMode() == AppConstants.NEW_MODE) ? "New"
								: "Open";
						String message = ResourceManager.getString(
								"LOG_ERROR_FAILED_TO_SAVE_GRAPH", new Object[] {
										graphInfo.getGraphName(), graphMode,
										userInfo.getUsername() })
								+ " " + e.getMessage();
		
						ApplicationLog.error(message, e);
		
						strUpdate = ResourceManager.getString("ERROR_FALITO_SAVE");
					}
				} else {
					if (saveGraphInfo != null) {
						if (saveGraphInfo.getGraphName() == null
								|| saveGraphInfo.getGraphName().trim().equalsIgnoreCase("")) {
							return ResourceManager
									.getString("ERROR_REPORTS_ADD_INVALID_NAME_MSG");
						}
		
						try {
							Map<String, String> graphDisplayNameMap = smartenService
									.getGraphDisplayNameByFolderId(strFolderId);
		
							for (Map.Entry<String, String> entry : graphDisplayNameMap
									.entrySet()) {
								String graphId = entry.getKey();
								String displayName = entry.getValue();
								if (graphId.endsWith(AppConstants.SMARTEN_FILE_EXT)) {
									if (displayName.equals(saveGraphInfo.getGraphName())) {
										return ResourceManager
												.getString("ERROR_OBJECT_ALREADY_EXIST_ENTER_OTHER_NAME");
									}
								}
							}
						} catch (Exception ex) {
							String graphMode = (graphInfo.getGraphMode() == AppConstants.NEW_MODE) ? "New"
									: "Open";
							String message = ResourceManager.getString(
									"LOG_ERROR_FAILED_TO_SAVE_GRAPH", new Object[] {
											graphInfo.getGraphName(), graphMode,
											userInfo.getUsername() })
									+ " " + ex.getMessage();
		
							ApplicationLog.error(message, ex);
		
							return ResourceManager.getString("ERROR_FALITO_SAVE");
						}
		
						boolean newGraph = (graphInfo.getGraphMode() == AppConstants.NEW_MODE);
						setSaveasProcess(true);
						if (saveGraphInfo != null && !newGraph) {
							
							try {
								graphInfo.setGraphName(StringUtil.unescapeHtmlUtil(saveGraphInfo.getGraphName().trim()));
								graphInfo.getGraphProperties()
										.getTitleProperties()
										.setTitle(
												saveGraphInfo.getGraphProperties()
														.getTitleProperties()
														.getTitle());
		
								graphInfo.setFolderInfo(folderInfo);
								graphInfo.setCreatedBy(userInfo);
								graphInfo.setCreatedDate(new Date());
								graphInfo.setModifiedBy(userInfo);
								graphInfo.setModifiedDate(new Date());
		
								Vector outLinerData =  smartenService.getOutLinerDataWithoutSysGeneratedFields(false, graphInfo);
								Vector rowLabeList =(Vector) outLinerData.get(0);
								Vector colLabeList = (Vector)outLinerData.get(1);
								Vector rowLabeList2 = (Vector)outLinerData.get(3);
								graphInfo.setRowColumns(rowLabeList);
								graphInfo.setColColumns(colLabeList);
								graphInfo.setLineGraphRowLabelsForCombinedGraph(rowLabeList2);
								
								
								if(graphInfo != null)
								{
									graphInfo.getGraphData().setSmartenRowsEnable(smartenRowsEnableTemp);
									graphInfo.getGraphData().setSmartenRowList(smartenRowsListTemp);
									graphInfo.getGraphData().setSmartenColoumnsEnable(smartenColumnsEnableTemp);
									graphInfo.getGraphData().setSmartenColList(smartenColsListTemp);
									graphInfo.getGraphData().setShapeMeasure(smartenShapeEnableTemp);
									graphInfo.getGraphData().setShapeList(smartenShapeListTemp);
									graphInfo.getGraphData().setShapeLabel(smartenShapeLabelTemp);
									graphInfo.getGraphData().setSizeMeasure(smartenSizeEnableTemp);
									graphInfo.getGraphData().setSizeList(smartenSizeListTemp);
									graphInfo.getGraphData().setSizeLabel(smartenSizeLabelTemp);
									graphInfo.getGraphData().setSizeValueList(smartenSizeValueListTemp);
									
									graphInfo.getGraphData().setMeasureListForSmartenview(measureListTemp);
									graphInfo.getGraphData().setDimensionListForSmartenview(dimensionListTemp);
									graphInfo.setDataTitleList(tabularDataListTemp);
									graphInfo.setDimensionTitleList(dimensionListTemp);
									graphInfo.setMeasureTitleList(measureListTemp);
									
									graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().setMouseOverNumberFormat(mouseOverNumberFormat);
								}
								
								
								
								SmartenInfo tmpGinfo = null;
								smartenService.checkAndResetDrillUp(graphInfo, true);
								if ((tmpGinfo=smartenService.saveGraph(graphInfo, userInfo, true)) != null) {
									strUpdate = AppConstants.SUCCESS_STATUS + ','
											+ graphInfo.getGraphId()
											+ "," + AppConstants.SMARTEN;
									getDetailInfoMap().put(tmpGinfo.getGraphId(), graphInfo);
									getServiceMap().put(tmpGinfo.getGraphId(), smartenService);
									getDetailInfoMap().remove(graphInfo.getNewGraphId());
									getServiceMap().remove(graphInfo.getNewGraphId());
									
									auditUserActionLog(ResourceManager.getString("LBL_SAVE_NEW_OBJECT"), AppConstants.USER_ACCESS,userInfo);
								} else {
									strUpdate = ResourceManager
											.getString("ERROR_FALITO_SAVE");
								}
							} catch (DatabaseOperationException e) {
								strUpdate = ResourceManager
										.getString("ERROR_FALITO_SAVE");
								String graphMode = (graphInfo.getGraphMode() == AppConstants.NEW_MODE) ? "New"
										: "Open";
								String message = ResourceManager.getString(
										"LOG_ERROR_FAILED_TO_SAVE_GRAPH", new Object[] {
												graphInfo.getGraphName(), graphMode,
												userInfo.getUsername() })
										+ " " + e.getMessage();
		
								ApplicationLog.error(message, e);
							} catch (CubeException e) {
								strUpdate = ResourceManager
										.getString("ERROR_FALITO_SAVE");
								String graphMode = (graphInfo.getGraphMode() == AppConstants.NEW_MODE) ? "New"
										: "Open";
								String message = ResourceManager.getString(
										"LOG_ERROR_FAILED_TO_SAVE_GRAPH", new Object[] {
												graphInfo.getGraphName(), graphMode,
												userInfo.getUsername() })
										+ " " + e.getMessage();
		
								ApplicationLog.error(message, e);
							} catch (Exception ex) {
								String graphMode = (graphInfo.getGraphMode() == AppConstants.NEW_MODE) ? "New"
										: "Open";
								String message = ResourceManager.getString(
										"LOG_ERROR_FAILED_TO_SAVE_GRAPH", new Object[] {
												graphInfo.getGraphName(), graphMode,
												userInfo.getUsername() })
										+ " " + ex.getMessage();
		
								ApplicationLog.error(message, ex);
								strUpdate = ResourceManager
										.getString("ERROR_FALITO_SAVE");
							}
						} else {
							try {
								graphInfo.setGraphName(StringUtil.unescapeHtmlUtil(saveGraphInfo.getGraphName().trim()));
								graphInfo.getGraphProperties()
										.getTitleProperties()
										.setTitle(
												saveGraphInfo.getGraphProperties()
														.getTitleProperties()
														.getTitle());
		
								graphInfo.setFolderInfo(folderInfo);
								graphInfo.setCreatedBy(userInfo);
								graphInfo.setCreatedDate(new Date());
								graphInfo.setModifiedBy(userInfo);
								graphInfo.setModifiedDate(new Date());

								if(graphInfo != null)
								{
									graphInfo.getGraphData().setSmartenRowsEnable(smartenRowsEnableTemp);
									graphInfo.getGraphData().setSmartenRowList(smartenRowsListTemp);
									graphInfo.getGraphData().setSmartenColoumnsEnable(smartenColumnsEnableTemp);
									graphInfo.getGraphData().setSmartenColList(smartenColsListTemp);
									graphInfo.getGraphData().setShapeMeasure(smartenShapeEnableTemp);
									graphInfo.getGraphData().setShapeList(smartenShapeListTemp);
									graphInfo.getGraphData().setShapeLabel(smartenShapeLabelTemp);
									graphInfo.getGraphData().setSizeMeasure(smartenSizeEnableTemp);
									graphInfo.getGraphData().setSizeList(smartenSizeListTemp);
									graphInfo.getGraphData().setSizeLabel(smartenSizeLabelTemp);
									graphInfo.getGraphData().setSizeValueList(smartenSizeValueListTemp);
									
									
									if(graphInfo.getGraphData().getMeasureListForSmartenview() != null && !graphInfo.getGraphData().getMeasureListForSmartenview().isEmpty())
										graphInfo.getGraphData().setMeasureListForSmartenview(measureListTemp);
									if(graphInfo.getGraphData().getDimensionListForSmartenview() != null && !graphInfo.getGraphData().getDimensionListForSmartenview().isEmpty())
										graphInfo.getGraphData().setDimensionListForSmartenview(dimensionListTemp);
									
									if(graphInfo.getDataTitleList() != null && !graphInfo.getDataTitleList().isEmpty())
										graphInfo.setDataTitleList(tabularDataListTemp);
									
									if(graphInfo.getDimensionTitleList() != null && !graphInfo.getDimensionTitleList().isEmpty())
										graphInfo.setDimensionTitleList(dimensionListTemp);
									if(graphInfo.getMeasureTitleList() != null && !graphInfo.getMeasureTitleList().isEmpty())
										graphInfo.setMeasureTitleList(measureListTemp);
									modelMap.put("smartenMeasureCurrentTabName", "M"+0);
									modelMap.put("smartenMeasureSelectedTabNames", "M"+0);
									
									graphInfo.getGraphProperties().getDataValueProperties().getDataValueMouseOver().setMouseOverNumberFormat(mouseOverNumberFormat);
								}
								
								SmartenInfo tmpGinfo = null;
								if ((tmpGinfo =smartenService.saveGraph(graphInfo, userInfo, true)) != null) {
									strUpdate = AppConstants.SUCCESS_STATUS + ','
											+ graphInfo.getGraphId()
											+ "," + AppConstants.SMARTEN;
									getDetailInfoMap().put(tmpGinfo.getGraphId(), tmpGinfo);
									getServiceMap().put(tmpGinfo.getGraphId(), smartenService);
									getDetailInfoMap().remove(graphInfo.getNewGraphId());
									getServiceMap().remove(graphInfo.getNewGraphId());
									
									auditUserActionLog(ResourceManager.getString("LBL_SAVE_NEW_OBJECT"), AppConstants.USER_ACCESS,userInfo);
								} else {
									strUpdate = ResourceManager
											.getString("ERROR_FALITO_SAVE");
								}
							} catch (Exception e) {
								String graphMode = (graphInfo.getGraphMode() == AppConstants.NEW_MODE) ? "New"
										: "Open";
								String message = ResourceManager.getString(
										"LOG_ERROR_FAILED_TO_SAVE_GRAPH", new Object[] {
												graphInfo.getGraphName(), graphMode,
												userInfo.getUsername() })
										+ " " + e.getMessage();
		
								ApplicationLog.error(message, e);
		
								strUpdate = ResourceManager
										.getString("ERROR_FALITO_SAVE");
							}
						}
					}
				}
			} else {
				strUpdate = ResourceManager.getString("ERROR_NO_OBJECT_SAVE_PERMISSION");
			}
		} catch(DatabaseOperationException e){
			ApplicationLog.error(e);
		} catch (ServiceException e1) {
			ApplicationLog.error(e1);
		}
		return strUpdate;
	}

	@Override
	public boolean isFromDashBoard() {return false;}
	
	
	@Override
	public boolean isFromSmarten() {
		return true;
	}

	@Override
	public void setFilterColumnInformation(String[][] filterColumnInformation,HttpServletRequest request) {}

	@RequestMapping ("/refreshGraphFromAnalysis")
	public ModelAndView refreshGraphFromAnalysis(ModelMap map,HttpServletRequest request, @LoggedInUser UserInfo userInfo) {
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("isRefreshReq", "true");
		params.put("loggedInUserId",userInfo.getUserId());

		long currentTimeStamp = System.currentTimeMillis();
		GraphProperties graphProperties = null;

		if (request.getAttribute("smartenServiceFromAnalysis") != null) {
			smartenService = (SmartenService) request.getAttribute("smartenServiceFromAnalysis");
			graphInfo = (SmartenInfo) request.getAttribute("graphInfoFromAnalysis");
		}

		try {
			smartenService.initGraphData(graphInfo, params,userInfo);

			graphProperties = graphInfo.getGraphProperties();
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_REFRESH_GRAPH_FROM_ANALYSIS", new Object[] {
							userInfo.getUsername() }), e);
		} catch (ALSException e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_TO_REFRESH_GRAPH_FROM_ANALYSIS", new Object[] {
							userInfo.getUsername() }), e);
		}

		
		params = new HashMap<String, String>();

		params.put("sCmd", AppMainCommandList.NEW_GRAPH.getM_strCommandName());
		params.put("firstTime" , "true");
		params.put("currentTimeStamp", currentTimeStamp + "");

		generateRequiredItemsForGraph(map, params);

		if (graphInfo.getDrilldownBreadcrumbMap() != null && graphInfo.getDrilldownBreadcrumbMap().size() > 0) {
			map.put("drilldownBreadCrumb", graphInfo.getDrilldownBreadcrumbMap());

			map.put("drillUpLinkToOneLevel", graphInfo.getDrillUpLinkToOneLevel());
		}
		auditUserActionLog(ResourceManager.getString("LBL_REFRESH_GRAPH_FROM_ANALYSIS"), AppConstants.DETAIL,userInfo);
		
		String[] jsonArr =  smartenService.generateGraph(graphInfo,"",false);
		if(jsonArr.length > 0 && jsonArr[0]==null)
			jsonArr[0]="1";
		map.put("jsonData",jsonArr[0]);		
		map.put("chartSize", 2);
		
		map.put("gaugeLegendInfo", graphInfo.getGaugeData());
		int noOfChartsInRow = 0;
		if(graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			noOfChartsInRow =  graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getNoOfGauge();
			map.put("titledist", graphInfo.getGraphProperties().getGaugeTitleProperties().getDistanceFromCenter());
		}
		else if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH && jsonArr[1] != null)
		{
			if(graphInfo.getGraphProperties().getPieGraph().isClustered())
			{
				noOfChartsInRow = Integer.parseInt(jsonArr[1]);
			}
			else
			{	
				noOfChartsInRow =  graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getNumberofpie();
			}
			map.put("pietitle", graphInfo.getTitleData());
			map.put("nestedgraph", graphInfo.getGraphProperties().getPieGraph().isClustered());
		}
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH  ||	graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			if(jsonArr[1] == "1")
			{
				map.put("noOfChartsInRow", jsonArr[1]);
			}
			else
			{	
				map.put("noOfChartsInRow", noOfChartsInRow);
			}	
		}
		else
		{
			map.put("nestedgraph", "false");
			map.put("noOfChartsInRow", jsonArr[1]);
		}
		
		map.put("currentTimeStamp", currentTimeStamp);
		map.put("graphType", graphInfo.getGraphType());
		map.put("graphProperties", graphProperties);
		map.put("Mode", AppConstants.OPEN_MODE);
		
		map.put("analysisGraphInfo", graphInfo);
		map.put("fromAnalysis", true);
		boolean d3Graph = false;
		if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
			d3Graph = true;
		map.put("d3Graph",d3Graph);
		String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
		map.addAttribute("strDateFormat",strDateFormat);
		return new ModelAndView("graphFromAnalysis");
	}

	@Override
	public List<String> getObjectList(HttpServletRequest request) { return null; }

	@Override
	public Map<String, List<String>> getPageAssociateMap(HttpServletRequest request) { return null;}

	@Override
	public void setPageAssociateMap(Map<String, List<String>> pageAssociate,HttpServletRequest request) {}
	
	@RequestMapping(value = "/exportApi")
	@ResponseBody
	public void exportApi(@RequestParam("exportType") String exportType,
			@RequestParam("objectid") String strObjectId,
			@RequestParam (value="exportToken",defaultValue="", required=false)String exportToken,
			HttpServletResponse response, HttpServletRequest request,
			@LoggedInUser UserInfo userInfo) {
		
		smartenService = (SmartenService) AppContext.getApplicationContext().getBean("smartenService");
		try {
			smartenService = (SmartenService)ExportServiceUtil.getExportObjectService().get(exportToken);
			graphInfo = (SmartenInfo)ExportServiceUtil.getExportEntityInfo().get(exportToken);
			//graphInfo = smartenService.getGraphById(strObjectId);
		} catch (Exception  e) {
			ApplicationLog.error(e);
		}
		this.export(exportType, response, request, userInfo,exportToken);
		
	}
	/**
	 * Export Graph with different type JPG, PDF..
	 * 
	 * @param exportType
	 *            export type like 1-JPG, 2-PDF
	 * @param response
	 *            HttpServletResponse object
	 * @param request
	 *            HttpServletRequest object
	 * @param userInfo
	 *            current logged in info.
	 */
	@RequestMapping(value = "/export")
	@ResponseBody
	public void export(@RequestParam("exportType") String exportType,
			HttpServletResponse response, HttpServletRequest request,
			@LoggedInUser UserInfo userInfo,@RequestParam (value="exportToken",defaultValue="", required=false)String exportToken) {
		setExportInProcess(true);
		OutputStream sout = null;
		String useragent = null;
		String strFileName = "";
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Export","export object",Thread.currentThread(),userInfo,null);
		try {			
			useragent = request.getHeader("User-Agent");
			sout = response.getOutputStream();
			strFileName = graphInfo.getGraphName();
			strFileName = formatExportFileName(strFileName);
			
			if (exportType.equals("1")) {
				strFileName = CalendarUtil.getFileSuffix(strFileName)  + AppConstants.JPG_EXT;
			} else if (exportType.equals("2")) {
				strFileName = CalendarUtil.getFileSuffix(strFileName)  + AppConstants.PDF_EXT;
			} else if (exportType.equals("3")) {
				strFileName = CalendarUtil.getFileSuffix(strFileName)  + AppConstants.PNG_EXT;
			} 

			if (useragent.indexOf("Chrome") != -1
					|| useragent.indexOf("Safari") != -1
					|| useragent.indexOf("Opera") != -1) {
				response.setHeader("Content-Disposition",
						"attachment; filename=" + strFileName);
			} else {
				response.setHeader("Content-Disposition",
						"attachment; filename=\"" + strFileName);
			}
			if (exportType.equals("1")) {
				response.setContentType("image/jpg");
				smartenService.exportToJpg(graphInfo.getGraphId(), userInfo, sout, AppConstants.JPG_EXT,false, exportToken);
			} else if (exportType.equals("2")) {
				response.setContentType("application/pdf");
				smartenService.exportToPDF(sout, graphInfo, userInfo.getUserId(),false, exportToken);
			} else if (exportType.equals("3")) {
				response.setContentType("image/png");
				smartenService.exportToPng(graphInfo.getGraphId(), userInfo, sout, AppConstants.PNG_EXT,false, exportToken);
			} 
			auditUserActionLog(ResourceManager.getString("LBL_EXPORT_GRAPH_JPG_PNG_PDF"), AppConstants.DETAIL,userInfo);
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_EXPORT",
					new Object[] { graphInfo.getGraphName(),
							userInfo.getUsername() }), e);
		} catch (Exception e) {
			ApplicationLog.error(ResourceManager.getString(
					"LOG_ERROR_MSG_FAILED_EXPORT",
					new Object[] { graphInfo.getGraphName(),
							userInfo.getUsername() }), e);
		} finally {
			try {
				if (sout != null) {
					sout.close();
					sout = null;
				}
			} catch (Exception ex) {
			}
			setExportInProcess(false);
		}
	}
	
	@Override
	public String setObjectFromDashboard(HttpServletRequest request) {
		smartenService = (SmartenService) request.getAttribute("smartenService");
		graphInfo = (SmartenInfo) request.getAttribute("smartenInfo");
		smartenService.setGraphImages(smartenService.getGraphImages()); 
		smartenService.setDashbordTdId("");
		graphInfo.setGraphFromDashBoard(true);	
		graphInfo.setGraphAreaFitToSection(false);
		graphInfo.setGraphFromDashBoard(true);		
		graphInfo.setWindowScreenHeight((Integer) request.getAttribute("screenHeight"));
		graphInfo.setWindowScreenWidth( (Integer) request.getAttribute("screenWidth"));
		return AppConstants.SUCCESS_STATUS;
	}
	
	@Override
	public ModelAndView openObjectFromDahbaord(ModelMap map,@LoggedInUser UserInfo userInfo) {
		smartenService.setObjectMode(AppConstants.OPEN_MODE);
		if(graphInfo.isNlpOpen())
			smartenService.setObjectMode(AppConstants.NEW_MODE);
		
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		try {
			if(userInfo.isFromAPI() && graphInfo.getDashboardInfo()!=null) {
				String dbId = graphInfo.getDashboardInfo().getDashboardId();
				if(graphInfo.getDashboardInfo().getParentDashboardInfo()!=null) {
					dbId = graphInfo.getDashboardInfo().getParentDashboardInfo().getDashboardId();
				}
				GeneralUtil.setApiParameter(dbId, userInfo, map);
			}
		params.put("objectId", graphInfo.getGraphId());

		Map<String, String> requiredItemsParams = new HashMap<String, String>();
		
		long currentTimestamp = System.currentTimeMillis();
		
		requiredItemsParams.put("sCmd", AppMainCommandList.NEW_GRAPH.getM_strCommandName());
		requiredItemsParams.put("firstTime" , "true");
		requiredItemsParams.put("currentTimeStamp", currentTimestamp + "");
		
		
		generateRequiredItemsForGraph(map, requiredItemsParams);
		smartenService.lockObject(graphInfo.getGraphId(), AppConstants.GRAPH_TITLE, userInfo.getUserId());
		map.put("currentTimeStamp", currentTimestamp);
		graphInfo.setGraphMode(smartenService.getObjectMode());
		auditUserActionLog(ResourceManager.getString("LBL_OPEN_OBJECT_FROM_DASHBOARD"), AppConstants.USER_ACCESS,userInfo);
		if (graphInfo.getGraphType() ==  GraphConstants.NUMERIC_DIAL_GAUGE) {
			map.put("isGaugeGraph", true);
		} else {
			map.put("isGaugeGraph", false);	
		}
		map.put("isSetAsHome",userInfo.getHomePage());
		map.put("isDataValueOn", graphInfo.getGraphProperties().getDataValueProperties().getDataValuePoint().isDataValuePointVisible());
		map.addAttribute("Mode", smartenService.getObjectMode());
		map.addAttribute("isNlpOpen",graphInfo.isNlpOpen());
		map.addAttribute("objectType", AppConstants.SMARTEN);
		map.put("graphProperties", graphInfo.getGraphProperties());
		map.put("isCombinedGraph", graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH ? true : false);
		String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
		map.addAttribute("strDateFormat",strDateFormat);
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		} catch (Exception e) {
			ApplicationLog.error(e);
		}
		graphInfo.setNlpOpen(false);
		showAppliedFilter(map,userInfo, null);// Check whether filter is applied or not.
		map.addAttribute("graphInfo",graphInfo);
		getServiceMap().put(graphInfo.getGraphId(), smartenService);
		getDetailInfoMap().put(graphInfo.getGraphId(), graphInfo);

		List<Map<String, Object>> dpList =  new ArrayList<Map<String,Object>>();
		
		String[] jsonArr = new String[2];
		boolean d3Graph = false;
		
		map.put("d3Graph",d3Graph);
		
		map.put("completeGraphData",graphInfo.getGraphData().isCompleteGraphData());
		
		map.put("gaugeLegendInfo", graphInfo.getGaugeData());
		map.put("isLegendVisible",true);
		int noOfChartsInRow = 0;
		if(graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			noOfChartsInRow =  graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getNoOfGauge();
			map.put("titledist", graphInfo.getGraphProperties().getGaugeTitleProperties().getDistanceFromCenter());
		}
		else if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH)
		{
			if(graphInfo.getGraphProperties().getPieGraph().isClustered())
			{
				noOfChartsInRow = Integer.parseInt(jsonArr[1]);
			}
			else
			{	
				noOfChartsInRow =  graphInfo.getGraphProperties().getGraphAreaProperties().getGeneralGraphArea().getNumberofpie();
			}
			map.put("pietitle", graphInfo.getTitleData());
			map.put("nestedgraph", graphInfo.getGraphProperties().getPieGraph().isClustered());
		}
		
		if(graphInfo.getGraphType() == GraphConstants.PIE_GRAPH  ||	graphInfo.getGraphType() == GraphConstants.NUMERIC_DIAL_GAUGE)
		{
			if(jsonArr[1] == "1")
			{
				map.put("noOfChartsInRow", jsonArr[1]);
			}
			else
			{	
				map.put("noOfChartsInRow", noOfChartsInRow);
			}	
		}
		else
		{
			map.put("nestedgraph", "false");
			map.put("noOfChartsInRow", jsonArr[1]);
		}
		
		map.put("graphType", graphInfo.getGraphType());
		
		map.put("includeSmartenJSP",true);
		map.put("fromShowGraph",true);
		map.put("openObjectFromDashboard", true);
		
		return refreshObjectData(null,null, userInfo, map);
	}
	
	@Override
	public String backToDashboard(){
		graphInfo = null;
		return AppConstants.SUCCESS_STATUS;
	}
	
	/**
	 * Get objectList.
	 * 
	 * @param modelMap
	 *            ModelMap Object
	 * @param strFolderId
	 *            String
	 * @param strNodeType
	 *            String Object
	 * @return ModelAndView Object
	 */
	@RequestMapping(value = "/loadGraphName")
	@ResponseBody
	public ModelAndView loadGraphName(
			ModelMap modelMap,
			@RequestParam(value = "folderId", required = false) String strFolderId,
			@RequestParam(value = "nodetype", required = false) String strNodeType, @LoggedInUser UserInfo userInfo) {

		List<Repository> biObjectList = repositoryServiceUtil.getRepositoryObjectList(
				userInfo, strFolderId, strNodeType, AppConstants.SMARTEN,
				userInfo.isAdmin(), AppConstants.DEFAULT_SORT,
				IApplicationConfigurationService.SORT_ASCENDING, "",false,false);

		modelMap.put("biobjects", biObjectList);
		modelMap.put("folderId", strFolderId);
		return new ModelAndView("/graph/generateGraphNameList");
	}

	/**
	 * Copy Graph Theme
	 * 
	 * @param modelMap
	 *            Model Map Object
	 * @param strGarphId
	 *            graph id
	 * @param userInfo
	 *            user info
	 * @return object of model and view
	 */
	@RequestMapping(value = "/graphCopytheme")
	@ResponseBody
	public ModelAndView copyGraphTheme( ModelMap modelMap, @RequestParam(value = "graphId", required = false) String strGarphId, @LoggedInUser UserInfo userInfo) {

		smartenService.copyTheme(strGarphId, graphInfo);

		Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
		if(propertyMap != null) {
			@SuppressWarnings("rawtypes")
			Iterator itr = propertyMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				modelMap.put(key, propertyMap.get(key));
			}
		}
		if(isFromSmarten())
			modelMap.put("isFromSmarten", true);
		auditUserActionLog(ResourceManager.getString("LBL_COPY_GRAPH_THEME"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("smartenGraphProperties");
	}

	
	@ResponseBody
	@RequestMapping("/getXmlFormatDataForObjectId")
	public String writeXMLString(@RequestParam(value="objectId")String strObjectId,
			@RequestParam(value = AppConstants.API_PAGE_NUMBER,defaultValue="-1") String pageNumber,
			ModelMap map,@LoggedInUser UserInfo userInfo) {
		
		smartenService = (SmartenService) AppContext.getApplicationContext().getBean("smartenService");
		getServiceMap().put(strObjectId, smartenService);

		graphInfo = new SmartenInfo();
		
		getDetailInfoMap().put(strObjectId, graphInfo);
		
		smartenService.setLoggedInUserId(userInfo.getUserId());
		smartenService.setIsFromAnalysis(false);
		Map<String, String> requiredItemsParams = null;
		if (strObjectId != null && strObjectId.trim().length() > 0) {
			smartenService.setObjectMode(AppConstants.OPEN_MODE);
		} else {
			strObjectId = "";
		}
		
		if (smartenService.getObjectMode() == AppConstants.OPEN_MODE) {
			Hashtable<String, Object> params = new Hashtable<>();

			params.put("objectId", strObjectId);
			params.put("isFromAPI", userInfo.isFromAPI());
			try {
				graphInfo = smartenService.initializeGraph(smartenService.getObjectMode(), params, userInfo, true);
				getDetailInfoMap().put(strObjectId, graphInfo);
				
				
				if (graphInfo.getGraphProperties().getTitleProperties().isTitleVisible()) {
					HashtableEx ddvmList = smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());

					smartenService.setObjectPageTitle(graphInfo.getGraphId(),graphInfo
							.getActiveFilterInfo(userInfo.getUserId()),
							smartenService.getPageFilterNew(graphInfo),
							smartenService.getActiveVariableMap(), smartenService.getResultSetMetaData(),
							graphInfo.getCubeInfo(), graphInfo.getGraphProperties().getTitleProperties(),
							userInfo, ddvmList);
				}
			} catch (DatabaseOperationException | CubeException | IOException | NotBoundException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_MSG_FAILED_TO_INITIALIZEGRAPH",
						new Object[] { userInfo.getUsername(), smartenService.getObjectMode() }), e);
			} catch (ObjectAccessException | ObjectNotFoundException | CubeNotFoundException | CubeAccessException  e) {
				return ResourceManager.getString("ERROR_NO_ACCESS_PERMISSION");
			} catch (ALSException e) {
				return e.getMessage();
			} 


			
			long currentTimestamp = System.currentTimeMillis();
			requiredItemsParams = new HashMap<String, String>();
			requiredItemsParams.put("sCmd", AppMainCommandList.NEW_GRAPH.getM_strCommandName());
			requiredItemsParams.put("firstTime" , "true");
			requiredItemsParams.put("currentTimeStamp", currentTimestamp + "");

			generateRequiredItemsForGraph(map, requiredItemsParams);
			map.put("currentTimeStamp", currentTimestamp);
		}

		graphInfo.setGraphMode(smartenService.getObjectMode());
		smartenService.getFormatedDataList(graphInfo, userInfo).clone();
		if (graphInfo.getGraphType() ==  GraphConstants.NUMERIC_DIAL_GAUGE) {
			map.put("isGaugeGraph", true);
		} else {
			map.put("isGaugeGraph", false);	
		}
		String xmlString = "";		
		try (OutputStream output = new OutputStream(){
		        private StringBuilder string = new StringBuilder();
		        @Override
		        public void write(int b) throws IOException {
		            this.string.append((char) b );
		        }
	
		        public String toString(){
		            return this.string.toString();
		        }
		    };){
	    
			smartenService.exportGraphDataXML(output, graphInfo,userInfo,Integer.parseInt(pageNumber));
			xmlString =  output.toString();
		} catch (Exception e) {
			ApplicationLog.error(e);
		}	    
		return xmlString;
	}
	
	@Override
	public List<String> getCommonDimensionList(UserInfo userInfo,HttpServletRequest request) {		
		return null;
	}

	@Override
	public String savePDFPageSetupInfo(PDFPageSetupInfo pdfPageSetupInfo,
			ModelMap modelMap, HttpServletRequest request) {
		UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");
		detailedMonitorEndpoint.setProcessLog(Thread.currentThread().getId(),graphInfo.getGraphName(),graphInfo.getGraphId(),"Pdf Page setup","Save pdf page setup",Thread.currentThread(),userInfo,new Date());
		graphInfo.setPdfPageSetup(pdfPageSetupInfo);
		return AppConstants.SUCCESS_STATUS;
	}

	@Override
	public List<IDataObject> getCubeListFromDifferentObject(UserInfo userInfo,HttpServletRequest request)
			throws DatabaseOperationException {		
		return null;
	}
	
	@Override
	public void addVirtualItems(HashMap<String, Vector<String>> columnMap) {
		smartenService.addVirtualItems(graphInfo, columnMap);
	}

	@Override
	public List<String> getVirtualMeasureList() {
		return smartenService.getVirtualMeasureList(graphInfo);
	}

	@Override
	public List<SelectItem> getItemDDVMApplyName(List<Object> objects,
			String strUserId, String strColumnName) throws ALSException,
			CubeException {
		return  smartenService.getItemDDVMApplyName(graphInfo, objects, strUserId, strColumnName);
		
	}

	@Override
	public boolean isPageFilterToolTip(String objectId) {
		
		return true;
	}

	@Override
	public void auditUserActionLog(String action, String msgLevel, UserInfo user) {
		GeneralUtil.auditUserActionLog(graphInfo, user, getStrParentHierchy(), action, msgLevel);
	}

	@Override
	public void unlockObject(List<String> objList,HttpServletRequest request) {
		try {
			if(smartenService != null){
				smartenService.unLockObject(objList);
				if(!isSaveasProcess()) {
					smartenService.clear();
				}
			}
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		}
	}
	
	@Override
	public boolean checkForUpdate(String objectId, String userId) {
		boolean bUpdate = false;
		try {
			if(smartenService != null){
				bUpdate = smartenService.checkForUpdate(objectId, userId);
			}
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		}
		return bUpdate;
	}
	
	@Override
	public void setUpdateByObjectAndUserId(String objectId, String userId,
			boolean update) {
		try {
			smartenService.setUpdateByObjectAndUserId(objectId, userId,update);
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		}
		
	}
	
	@Override
	public boolean checkForObjectAccessByAnyUser(String objectId, String userId) {
		boolean bUpdate = false;
		try {
			bUpdate = smartenService.checkForObjectAccessByAnyUser(objectId, userId);
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		}
		return bUpdate;
	}

	@Override
	public void setService(ObjectService objectService) {
		if(graphInfo != null &&  graphInfo.getDashboardInfo() == null){
			smartenService = (SmartenService) objectService;
		}
	}

	@Override
	public void setDetailInfo(IEntity entity) {
		if(graphInfo != null &&  graphInfo.getDashboardInfo() == null){
			graphInfo = (SmartenInfo) entity;
		}
	}
	
	public SmartenInfo getGraphObjectFromMap(String objId) {
		SmartenInfo graphObjInfo = graphInfo;
		if(objId != null && !objId.isEmpty() && null != graphInfo  && graphInfo.getDashboardInfo() == null){
			graphObjInfo = (SmartenInfo) getDetailInfoMap().get(objId);
		}
		if(graphObjInfo != null){
			return graphObjInfo;
		} else {
			graphObjInfo = graphInfo;
		}
		return graphObjInfo;
	}

	public SmartenService getGraphServiceFromMap(String objId) {
		SmartenService smartenServiceObj = smartenService;
		if(objId != null && !objId.isEmpty()  && graphInfo.getDashboardInfo() == null){
			smartenServiceObj = (SmartenService) getServiceMap().get(objId);
		}
		if(smartenServiceObj != null){
			return smartenServiceObj;
		} else {
			smartenServiceObj = smartenService;
		}
		return smartenServiceObj;
	}
	
	@Override
	public String checkColumnHeader(String strColumnName) {
		return strColumnName;
		
	}

	@Override
	public boolean isMDXCube() {
		boolean isMDXCube = graphInfo.getCubeInfo().isMdxCube();
		return isMDXCube;
	}
	
	@Override
	public ModelAndView showAppliedFilter(ModelMap modelMap,@LoggedInUser UserInfo uInfo, String objectId) {
		
		smartenService.getAppliedFilterInfo(graphInfo,uInfo,modelMap, getCubeInfo(objectId));
		String logAction = ResourceManager.getString("LBL_APPLIED_FILTER_DIALOG");		
		auditUserActionLog(logAction, AppConstants.DETAIL, uInfo);
		
		return new ModelAndView("smartenAppliedFilterInformation");
	}

	@ResponseBody
	@RequestMapping(value = "/applyGraphsPagination")
	public String applyGraphsPagination(
			@RequestParam(value = "objectId", required = false) String strObjectId,
			@RequestParam(value = "legendIndex", required = false) int legendIndex,
			@RequestParam(value = "legendQuantity", required = false) int legendQuantity,
			@RequestParam(value = "graphCount", required = false) int graphCount,
			@RequestParam(value="graphCurrentIndex", required=false) int graphCurrentIndex,ModelMap map,
			@RequestParam(value = "categoryIndex", required = false,defaultValue="0") int categoryIndex,
			@RequestParam(value = "categoryQuantity", required = false,defaultValue="0") int categoryQuantity,
			@RequestParam(value = "direction", required = false) String direction,
			@LoggedInUser UserInfo userInfo) {
		long getDataStartTime = System.currentTimeMillis();
		
		
				if(graphInfo.getColorInfoList().isEmpty())
				{
					List<Integer> colorInfoListTemp = new ArrayList<Integer>();
					colorInfoListTemp.add(0);
					graphInfo.setColorInfoList(colorInfoListTemp);
				}
				if(graphInfo.getCmbBarColorInfoList().isEmpty())
				{
					List<Integer> cmbBarColorInfoList = new ArrayList<Integer>();
					cmbBarColorInfoList.add(0);
					graphInfo.setCmbBarColorInfoList(cmbBarColorInfoList);
				}
				if(graphInfo.getCmbLineColorInfoList().isEmpty())
				{
					List<Integer> cmbLineColorInfoList = new ArrayList<Integer>();
					cmbLineColorInfoList.add(0);
					graphInfo.setCmbLineColorInfoList(cmbLineColorInfoList);
				}
		ObjectMapper objectMapper = new ObjectMapper();
		graphInfo.getGraphData().setGraphCount(graphCount);
		graphInfo.getGraphData().setGraphCurrentIndex(graphCurrentIndex);
		graphInfo.getGraphData().setPaginationDirection(direction);
		List<Graphs> graphsList=smartenService.generateGraphs(graphInfo,legendIndex,legendQuantity);
		
		String json="";	
		if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH &&  null != graphInfo && graphInfo.getGraphData().isMeasureInColForBubbLe())
		{
			
			if(graphsList != null)
			{
				List<Map<String, Object>>  dataRulesMap=graphsList.get(0).getDataRulesMap();
				if(dataRulesMap.size()==0)
				{
					json="1";
					return json;				
				}
				try {
					json = objectMapper.writeValueAsString(dataRulesMap);
				} catch (IOException e) {
					ApplicationLog.error(e);
				}
			}
			else
			{
				try {
					json = objectMapper.writeValueAsString(new ArrayList<Graphs>());
				} catch (IOException e) {
					ApplicationLog.error(e);
				}
			}
			
		}else{
		
			if(graphsList != null)
			{
				try {
					if(graphsList.size()==0)
					{
						json="1";
						return json;				
					}	
					json = objectMapper.writeValueAsString(graphsList);
				} catch (JsonGenerationException e) {
					ApplicationLog.error(e);
				} catch (JsonMappingException e) {
					ApplicationLog.error(e);
				} catch (IOException e) {
					ApplicationLog.error(e);
				}
			}
			else
			{
				try {
					json = objectMapper.writeValueAsString(new ArrayList<Graphs>());
				} catch (JsonGenerationException e) {
					ApplicationLog.error(e);
				} catch (JsonMappingException e) {
					ApplicationLog.error(e);
				} catch (IOException e) {
					ApplicationLog.error(e);
				}
			}
		}
		long getDataEndTime = System.currentTimeMillis();
		ApplicationLog.debug("SetGraphs ENDS time is ==>"+(getDataEndTime - getDataStartTime));
		//System.out.println("graphs:="+json);
		return json;
				
	}

	@ResponseBody
	@RequestMapping(value = "/applyPagination")
	public String applyPagination(
			@RequestParam(value = "objectId", required = false,defaultValue="") String strObjectId,			
			@RequestParam(value = "legendIndex", required = false,defaultValue="0") int legendIndex,
			@RequestParam(value = "legendQuantity", required = false,defaultValue="0") int legendQuantity,
			@RequestParam(value = "categoryIndex", required = false,defaultValue="0") int categoryIndex,
			@RequestParam(value = "categoryQuantity", required = false,defaultValue="0") int categoryQuantity,ModelMap map,
			@LoggedInUser UserInfo userInfo) {
		long getDataStartTime = System.currentTimeMillis();
		ObjectMapper objectMapper = new ObjectMapper();
		List<Map<String, Object>> dpList =  new ArrayList<Map<String,Object>>();
		
		
		dpList=smartenService.generateDataProviderGraph(graphInfo,legendIndex,legendQuantity,categoryIndex,categoryQuantity);
		
		String json="";	
		if(dpList != null)
		{
			try {
				if(dpList.size()==0)
				{
					json="1";
					return json;				
				}	
				json = objectMapper.writeValueAsString(dpList);
				
			} catch (JsonGenerationException e) {
				ApplicationLog.error(e);
			} catch (JsonMappingException e) {
				ApplicationLog.error(e);
			} catch (IOException e) {
				ApplicationLog.error(e);
			}
		}
		else
		{
			try {
				json = objectMapper.writeValueAsString(new ArrayList<Map<String,Object>>());
			} catch (JsonGenerationException e) {
				ApplicationLog.error(e);
			} catch (JsonMappingException e) {
				ApplicationLog.error(e);
			} catch (IOException e) {
				ApplicationLog.error(e);
			}
		}
		//System.out.println("data-"+json);
		long getDataEndTime = System.currentTimeMillis();
		ApplicationLog.debug(" Setdata Provider ENDS time is ==>"+(getDataEndTime - getDataStartTime));
		return json;
				
	}

	@Override
	public boolean isRealTimeCube() {
		boolean isRealTimeCube = graphInfo.getCubeInfo().isRealTimeCube();
		return isRealTimeCube;
	}

	@Override
	public WhatIfConfigurationInfo getWhatIfConfigurationInfo(String objectId) {
		
		return graphInfo.getWhatIfConfigurationInfo();
	}

	@Override
	public void setWhatIfConfigurationInfo(WhatIfConfigurationInfo whatIfConfigurationInfo, String objectId) {
		
		graphInfo.setWhatIfConfigurationInfo(whatIfConfigurationInfo);
	}
	
	@Override
	public List<SelectItem> getDataDisplayValueMappingOnLOV(HashtableEx ddvmMap, List<Object> cubeDataValueList,
			String userId, int itemType, String strColumnName, String objectId) {
		List<SelectItem> lovMappingValue = smartenService.getDataDisplayValueMappingOnLOV(ddvmMap, cubeDataValueList, userId, itemType, strColumnName);
		return lovMappingValue;
	}


	@Override
	public int getGraphType() {
		return graphInfo.getGraphType();
	}

	@Override
	public HashtableEx getActiveDDVMs(String loggedInUserId) {
		return smartenService.getActiveDDVMs(graphInfo, loggedInUserId);
	}
	@RequestMapping(value = "/loadDataValuesMeasureProperties", method=RequestMethod.POST)
	@ResponseBody
	public Object loadDataValuesMeasureProperties(@ModelAttribute GraphProperties graphProperties,
			@RequestParam(required = false, value="measureSelectedTabNames") String selectedTabNames,
			@RequestParam(required = false, value="measureNextTabName") String nextTabName,
			ModelMap map, HttpServletResponse response, @LoggedInUser UserInfo userInfo) {
		
		Map<String,Object> propertyMap = smartenService.getGraphPropertiesMap(graphInfo, userInfo);
		if(propertyMap != null) {
			@SuppressWarnings("rawtypes")
			Iterator itr = propertyMap.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				map.put(key, propertyMap.get(key));
			}
		}
		if (null == nextTabName) {
			map.put("measureCurrentTabName", "M"+0);
		} else {
			map.put("measureCurrentTabName", nextTabName);
		}
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return new ModelAndView("graph/graphDataValuesPropertiesMultiMeasureProperties");
	
	}
	
	/**
	 * Below function is for Tabular pagination in SmartenView
	 * @param request
	 * @param modelMap
	 * @param pageNo
	 * @param userInfo
	 * @return
	 */
	@RequestMapping (value="/getSmartenReportPage")
	public ModelAndView getSmartenReportPage(HttpServletResponse response,ModelMap modelMap, @RequestParam (value = "pageNo",required = true) int pageNo,@LoggedInUser UserInfo userInfo) {
		this.pageNumer = pageNo;
		isFromGetPage = true;

		int startIndex = 1;
		int endIndex = 0;
		int totalRecord = 0;
		int totalRecPage = 50;

		List tempDataTitleListForSmartenview = new ArrayList();
		if(null != graphInfo.getGraphData().getDataTitleListForSmartenview()
				&&	!graphInfo.getGraphData().getDataTitleListForSmartenview().isEmpty())
		{
			tempDataTitleListForSmartenview.addAll(graphInfo.getGraphData().getDataTitleListForSmartenview());
			totalRecord = graphInfo.getGraphData().getDataTitleListForSmartenview().size();
			
		}
		else if(null != graphInfo.getGraphData().getColListForSmartenview()
				&& !graphInfo.getGraphData().getColListForSmartenview().isEmpty())
		{
			tempDataTitleListForSmartenview.addAll(graphInfo.getGraphData().getColListForSmartenview());
			totalRecord = graphInfo.getGraphData().getColListForSmartenview().size();
			
		}

		if(pageNo == -1) {
			endIndex = totalRecord;
		} else {
			startIndex = (totalRecPage * pageNo ) - totalRecPage + 1;
			endIndex = (totalRecPage * pageNo );
			if(totalRecord < endIndex) {
				endIndex = totalRecord;// + 1;
			}
		}
		
		List tempListClone = ((List) ((ArrayList) tempDataTitleListForSmartenview).clone());
		tempDataTitleListForSmartenview = new ArrayList();
		tempDataTitleListForSmartenview.add(graphInfo.getDataTitleList().get(0));
		smartenService.setDisplayNameForSmartenTabular(graphInfo, (List<String>) tempDataTitleListForSmartenview.get(0),"");
		if(graphInfo.getGraphProperties().isPaginationCB())
			tempDataTitleListForSmartenview.addAll(tempListClone.subList(startIndex, endIndex));
		else
			tempDataTitleListForSmartenview.addAll(tempListClone);
		
		modelMap.put("dataTitleList", tempDataTitleListForSmartenview);
		modelMap.put("dataTitleListSize", tempDataTitleListForSmartenview.size());
		modelMap.put("smartenTotalPage", smartenService.getTotalPages(graphInfo));
		modelMap.put("pageNo", this.pageNumer);
		modelMap.put("isNoDataFound",false);
		modelMap.put("isSmartenMap",false);
		
		String[] jsonArr = new String[2];
		jsonArr[0] = "0";
		jsonArr[1] = "1";
		modelMap.put("jsonData",jsonArr[0]);
		modelMap.put("chartSize", jsonArr[1]);
		modelMap.put("noOfChartsInRow", 0);
		
		String strDateFormat = CalendarUtil.getDataDisplayFormat(userInfo, Types.TIMESTAMP);
		modelMap.addAttribute("strDateFormat",strDateFormat);
		modelMap.addAttribute("graphInfo",graphInfo);
		modelMap.put("isSmartenMode",graphInfo.isSmartenMode());
		
		if(graphInfo.getGraphMode() == AppConstants.NEW_MODE)
			modelMap.put("fromSave",false);
		
		modelMap.put("tabSizeList", graphInfo.getGraphData().getSizeListForTab());
		modelMap.put("tabColorList", graphInfo.getGraphData().getColorListForSmartenview());		
		modelMap.put("dimensionListSize", graphInfo.getDimensionTitleList().size());
		modelMap.put("measureListSize", graphInfo.getMeasureTitleList().size());
		
		modelMap.put("startRowIndex", startIndex);
		if(endIndex == totalRecord)
			modelMap.put("endRowIndex", (endIndex-1));
		else
			modelMap.put("endRowIndex", endIndex);
		modelMap.put("totalRecord", (totalRecord-1));
		
		modelMap.put("graphcss", graphInfo.getGraphProperties().generateSmartenCss(graphInfo, true, true,""));
		modelMap.put("isPieWithMultipleMeasureCount",graphInfo.getDataColLabels3().size());
		
		modelMap.put("tabularLegendInfo", graphInfo.getGraphData().getTabularLegendList());
		modelMap.put("tabularSizeLegendInfo", graphInfo.getGraphData().getTabularSizeLegendList());
		
			modelMap.put("selectedRecommendedGraphType", graphInfo.getRecommendGraphType());
		modelMap.put("recommendedGraphType", graphInfo.getRecommendGraphType());
		modelMap.put("graphType", SmartenConstants.SMARTENVIEW_TABULAR);
		modelMap.put("isSmartenTabular",true);
		if(graphInfo.getGraphData().isSmartenRowsEnable())
			modelMap.put("rowsOutlinerList", StringUtils.join(graphInfo.getOutlinerRows(), ','));
		if(graphInfo.getGraphData().isSmartenColoumnsEnable())
			modelMap.put("colsOutlinerList",  StringUtils.join(graphInfo.getOutlinerCols(), ','));
		modelMap.put("categoryOutlinerList", StringUtils.join(graphInfo.getOutlinerCol(), ','));
		modelMap.put("colorOutlinerList", StringUtils.join(graphInfo.getOutlinerRow(), ','));
		modelMap.put("valueOutlinerList", StringUtils.join(graphInfo.getOutlinerDataColumns(), ','));
		modelMap.put("sizeOutlinerList",  StringUtils.join(graphInfo.getOutlinerSizesColumns(), ','));
		modelMap.put("shapeOutlinerList", StringUtils.join(graphInfo.getOutlinerShapesColumns(), ','));
		modelMap.put("mainOutlinerList", StringUtils.join(graphInfo.getMainOutlinerMeasureAndDimension(), ','));
		modelMap.put("fromSave",false);
		modelMap.put("fromRefreshObjectData",true);
		modelMap.put("d3Graph",false);
		boolean isD3PieRadar = false;
		boolean isTabular = false;
		boolean isAmchart = false;

		if(graphInfo.isSmartenTabular())
			isTabular = true;
		else if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
				graphInfo.getGraphType() == GraphConstants.D3_TREEMAP || graphInfo.getGraphType() == GraphConstants.PIE_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.DRILLED_RADAR_GRAPH || graphInfo.getGraphType() == GraphConstants.DRILLED_STACKED_RADAR_GRAPH ||
				graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP)
		{
			isD3PieRadar = true;
		}
		else
			isAmchart = true;

		modelMap.put("isD3PieRadar",isD3PieRadar);
		modelMap.put("selectedGraphType", graphInfo.getGraphType());
		modelMap.put("isTabular",isTabular);
		modelMap.put("isAmchart",isAmchart);

		modelMap.put("paginationCB",graphInfo.getGraphProperties().isPaginationCB());
		modelMap.put("samplingCB",graphInfo.getGraphProperties().isSamplingCB());
		modelMap.put("snapShotCB",graphInfo.getGraphProperties().isSnapShotSamplingCB());
		modelMap.put("mainResultSetCount",graphInfo.getOriginalResultSetSize());
		
		return new ModelAndView("smartview/smarten");
	}
	
	/**
	 * show Data operation Dialog in Quick Setting SmartenView
	 * @param map
	 * @return ModelAndView
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/showdataoperationssmarten")
	@ResponseBody
	public ModelAndView showDataOperationsDialog(ModelMap map, @LoggedInUser UserInfo userInfo){
		
		Vector dateDimensions = null;
		Vector dimension = null;
		try {
			dateDimensions = metadataServiceUtil.getDateTimension(graphInfo.getCubeInfo(),
					userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			dimension = metadataServiceUtil.getDimensionColumns(graphInfo.getCubeInfo(), userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
		} catch (Exception e) {
			ApplicationLog.error(e);
		}
		Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
		if((graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH || graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR) && !graphInfo.isPerformAggregation())
			dataMap.put(ResourceManager.getString("LBL_NONE"),ICubeConstants.totalTypeNone);
		if(dateDimensions != null && dateDimensions.size() > 0) {
			dataMap.putAll(StringUtil.getDataOperationMap(true, isMDXCube())); 
		} else {
			dataMap.putAll(StringUtil.getDataOperationMap(false, isMDXCube()));
		}
		
		Map<String,String> messureMap = new HashMap<String, String>();
		Map<String,String> postaggregationMap = new HashMap<String, String>();
		Map<String,Integer> yAxisComputationType = new HashMap<String, Integer>();
		Map<String,String> timeDimensionMap = new HashMap<String, String>();
		Map<String,String> agregationMap = new HashMap<String, String>();
		yAxisComputationType = graphInfo.getyAxisComputationType();
		Map<String,String> distinctCountMap = new HashMap<String, String>();
		Vector<String> dataLabels = (Vector<String>) graphInfo.getDataColumns().clone();
		
		if (graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH) {
			if(graphInfo.getLineGraphDataLabelsForCombinedGraph() != null && graphInfo.getLineGraphDataLabelsForCombinedGraph().size() > 0){
				dataLabels.addAll(graphInfo.getLineGraphDataLabelsForCombinedGraph());
			}	
		}
		
		if (graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP && null != graphInfo.getMeasureTitleList() && !graphInfo.getMeasureTitleList().isEmpty()) {
			dataLabels = new Vector<String>();
			dataLabels.addAll(graphInfo.getMeasureTitleList());
		}
		
		List<ActiveUDDCInfo> actUddcInfo = graphInfo.getActiveUDDCInfo(userInfo.getUserId());
		if(actUddcInfo != null && actUddcInfo.size() > 0) {
			for (ActiveUDDCInfo activeUDDCInfo : actUddcInfo) {
				dataLabels.add(activeUDDCInfo.getUddcTemplateInfo().getColumnName());
			}
		}
		if(!graphInfo.isPerformAggregation())
		{
			String measure = dataLabels.get(dataLabels.size() -1);
			if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH && dataLabels.size() >=2)
				measure = dataLabels.get(dataLabels.size() -1);
			dataLabels.clear();
			dataLabels.add(measure);
		}
		
		if (dataLabels != null && dataLabels.size() > 0) {
			for (String dataLabel : dataLabels) {
				String intTotal = "0";
				String postTotal = "0";

				if(yAxisComputationType != null && yAxisComputationType.size() > 0){
					if(yAxisComputationType.containsKey(dataLabel)) {    
						intTotal = ""+yAxisComputationType.get(dataLabel);
					}
				}
				postTotal = intTotal;
				LMRecentInfo lmRecentInfo = null;
				if(graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo() != null) {
					lmRecentInfo = (LMRecentInfo) graphInfo.getObjectCubeDefinition().getLmRecentSettingsInfo().get(dataLabel);
				}
				if (lmRecentInfo == null) {
					lmRecentInfo = new LMRecentInfo();
				} else {
					if(lmRecentInfo.getOperationType() == ICubeConstants.LEASTRECENT) {
						intTotal = Integer.toString(ICubeConstants.LEAST_RECENT);
					} else {
						intTotal = Integer.toString(ICubeConstants.MOST_RECENT);
					}
					postTotal = StringUtil.getDataOperationCMDMap().get(Integer.parseInt(postTotal));
				}
				
				String distinctCountColumn = null;
				if(graphInfo.getObjectCubeDefinition().getDistinctCountMap() != null) {
					distinctCountColumn = (String) graphInfo.getObjectCubeDefinition().getDistinctCountMap().get(dataLabel);
				}
				if(distinctCountColumn == null) {
					distinctCountColumn = "";
				} else {
					intTotal = Integer.toString(ICubeConstants.DISTINCT);
				}
				messureMap.put(dataLabel, intTotal);
				postaggregationMap.put(dataLabel, postTotal);
				String timeDimension = lmRecentInfo.getTimeDimension();
				int agregation = lmRecentInfo.getAggregationType();
				timeDimensionMap.put(dataLabel, timeDimension);
				agregationMap.put(dataLabel, ""+agregation);
				distinctCountMap.put(dataLabel, distinctCountColumn);
			}
		}
		Map<String, Map<String,Object>> measureDataMap = smartenService.getSelectedDataOperation(graphInfo, messureMap, 
				(dateDimensions != null && dateDimensions.size() > 0),false);
		Map<String, Map<String,Object>> measureAggDataMap = smartenService.getSelectedDataOperation(graphInfo, messureMap, 
				(dateDimensions != null && dateDimensions.size() > 0),true);
		map.put("datedimension", dateDimensions);
		map.put("timeDimensionMap", timeDimensionMap);
		map.put("agregationMap", agregationMap);
		map.put("postaggregationMap", postaggregationMap);
		map.put("dataMap", dataMap);
		map.put("messureMap", messureMap);
		
		
		// Create display map for UI alias (backend original keys unchanged)
		Map<String, String> colLabels = graphInfo.getGraphProperties().getColLabelsMap();
		Map<String, String> messureDisplayMap = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : messureMap.entrySet()) {
		    String k = entry.getKey();
		    messureDisplayMap.put(k, (colLabels != null && colLabels.containsKey(k))
		                             ? colLabels.get(k) : k);
		}
		map.put("messureDisplayMap", messureDisplayMap);
		
		map.put("dimension", dimension);
		
		/*
		 * // Dimension display map Map<String, String> dimensionDisplayMap = new
		 * LinkedHashMap<String, String>(); if (dimension != null) { for (Object dim :
		 * dimension) { String d = dim.toString(); dimensionDisplayMap.put(d, (colLabels
		 * != null && colLabels.containsKey(d)) ? colLabels.get(d) : d); } }
		 * map.put("dimensionDisplayMap", dimensionDisplayMap);
		 */
		map.put("distinctCountMap", distinctCountMap);
		map.put("distinctOprMap", graphInfo.getObjectCubeDefinition().getDistinctCountOperationMap());
		map.put("selectedoperation", measureDataMap);
		map.put("selectedpostoperation", measureAggDataMap);
		map.put("isFromSmarten", true);
		
		return new ModelAndView("smartview/dataOperations");
	}
	
	/**
	 * For reloading Columns to add newly added UDDC in measures
	 * @param smartenLabelProperties
	 * @param map
	 * @param response
	 * @param request
	 * @param userInfo
	 * @return
	 */
	@RequestMapping (value = "/getColumnsList")
	@ResponseBody
	public ModelAndView getColumnsList(@ModelAttribute GraphProperties smartenLabelProperties
			,ModelMap map, HttpServletResponse response, HttpServletRequest request,@LoggedInUser UserInfo userInfo)
	{
		try
		{
			String strCubeId="";
			IDataObject cubeInfo = graphInfo.getCubeInfo();

			strCubeId = getGraphInfo().getCubeInfo().getDataObjectId();

			Vector<String> vector = new Vector<String>();
			if (strCubeId == null) {
				List<ActiveGlobalVariableInfo> activeGolbalVariableList = graphInfo.getActiveTemplateProperties().getActiveGlobalVariableInfo(userInfo.getUserId());
				if(activeGolbalVariableList != null && activeGolbalVariableList.size()>0){
					for (ActiveGlobalVariableInfo activeGlobalVariableInfo : activeGolbalVariableList) {
						vector.add(activeGlobalVariableInfo.getGlobalVariableInfo().getGlobalVariableName());
					}
				}	
			}

			HashMap<String, Vector<String>> dimensionMap = metadataServiceUtil
					.getColumnsAndMeasuresMap(cubeInfo, userInfo, vector,true,true,false,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			
			List<String> dimensionList = new ArrayList<String>();
			dimensionList.addAll(dimensionMap.get("1"));
			dimensionList.addAll(dimensionMap.get("2"));
			dimensionList.addAll(dimensionMap.get("3"));
			dimensionList.addAll(dimensionMap.get("5"));
			dimensionList.addAll(dimensionMap.get("6"));
			List<String> measureList = metadataServiceUtil.getMeasureList(cubeInfo, userInfo,true,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			OutlinerBean outlinerBean = new OutlinerBean();
			outlinerBean.setPtreeEnable(true);
			try {
				outlinerBean = smartenService.getOutlinerData(outlinerBean, graphInfo, userInfo, dimensionList, cubeInfo, false);
			} catch (CubeException e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
			} catch( DatabaseOperationException e){
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
			} catch (Exception e) {
				ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
			}
			map.put("dimensionList", dimensionList);			
			map.put("measureList", measureList);
			map.put("outlinerBean", outlinerBean);
			
			List<String> dimensions = new ArrayList<String>();
			List<String> dimensionListWithCount = graphInfo.getDimensionListWithCount();
			dimensions.addAll(dimensionMap.get("1"));
			if(null != dimensionMap.get("6") && !dimensionMap.get("6").isEmpty())
				dimensions.addAll(dimensionMap.get("6"));
			
			if(null != dimensionListWithCount && dimensionListWithCount.isEmpty()) {
				for(int i = 0; i < dimensions.size(); i++) {
					dimensionListWithCount.add((String)dimensions.get(i));
				}
			}
			map.put("dimensionListForCount", dimensions);
			map.put("dimensionListWithCount", dimensionListWithCount);

		}
		catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), e);
		} catch (DatabaseOperationException ex) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_OPEN_OUTLINER"), ex);
		}
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return new ModelAndView("smartview/columns");
	}
	
	/**
	 * This method is use to add Page Filters
	 * @return String
	 * @throws CubeException 
	 */
	@RequestMapping(value = "/addpagefilter")
	public ModelAndView addPageFilter(ModelMap map,@LoggedInUser UserInfo userInfo) {
			Vector dimensionColumns = new Vector();
			String dimensionColumnStr = "";
			String selectedColumnStr = "";
			try {
				if(graphInfo.getFilterColumnInformation() != null && graphInfo.getFilterColumnInformation().length >0) {	
					String temparr[][] = new String[smartenService.getPageFilters(graphInfo).length][];
					temparr = smartenService.getPageFilters(graphInfo);
					for(int i=0;i<temparr.length;i++)
					{
						selectedColumnStr += temparr[i][0]+","; 
					}
				
				}
				
				if(graphInfo.getFilterColumnInformation() == null)
				{
					selectedColumnStr = "";
					String[][] pageFilterColumns = graphInfo.getMultiPageFilterInfo();
					if(pageFilterColumns != null)
					{
						for(int i = 0; i < pageFilterColumns.length; i++)
						{
							selectedColumnStr += pageFilterColumns[i][0]+",";
						}
					}
				}
				
				
				dimensionColumns = cubeMetadataServiceUtil.getDimensionColumns(graphInfo.getCubeInfo(),userInfo,graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo));
			} catch (CubeException e) {
				ApplicationLog.error(e);
			}			
			for(int i=0;i<dimensionColumns.size();i++) {
				dimensionColumnStr += dimensionColumns.get(i).toString()+",";			
			}		
			if(dimensionColumnStr != null && dimensionColumnStr.length()>0){	
				dimensionColumnStr = dimensionColumnStr.substring(0,dimensionColumnStr.length()-1);
			}
			if(selectedColumnStr != null && selectedColumnStr.length()>0){
				selectedColumnStr = selectedColumnStr.substring(0,selectedColumnStr.length()-1);
			}
			map.put("dimensionColumnStr",dimensionColumnStr);
			map.put("selectedColumnStr",selectedColumnStr);
			if(graphInfo.getCubeInfo() instanceof DataSetsInfo)
			{
				DataSetsInfo dataSetsInfo=  (DataSetsInfo) graphInfo.getCubeInfo();
				map.put("cubeId",dataSetsInfo.getDataSetId());	
			}else{
				CubeInfo cubeInfo=  (CubeInfo) graphInfo.getCubeInfo();
				map.put("cubeId",cubeInfo.getCubeId());	
			}
			map.put("cubeInfo",graphInfo.getCubeInfo());
			map.put("isFromSmarten", true);//Added
			return new ModelAndView("addpagefilter");
	}
	
	/**
	 * This method is use to Save Page Filters
	 * @return String
	 * @throws CubeException 
	 */
	@RequestMapping(value = "/savePageFilterColumns")
	@ResponseBody
	public Object savePageFilterColumns(@RequestParam(value = "cubeId", required = false) String strCubeId,
			@RequestParam(value = "filterval", required = false) String strFilterval,ModelMap modelMap,HttpServletResponse response,
			@LoggedInUser UserInfo userInfo) {
		
		try {
			HashMap conditionMap = smartenService.getFilterConditions();
			if(strFilterval != null) {
				String[][] columnInfo = smartenService.getPageFilters(graphInfo);
				String[][] finalColumnInfo = null;

				if (strFilterval.length() > 0) {
					String tempStr[] = strFilterval.split(",");
					finalColumnInfo = new String[tempStr.length][];
					if(columnInfo != null && columnInfo.length > 0) {
						for(int i=0;i<tempStr.length;i++) {
							boolean contains = false;
							for(int j=0; j<columnInfo.length; j++) {
								contains = tempStr[i].equals(columnInfo[j][0]);
								if(contains) {
									finalColumnInfo[i] = new String[8];
									finalColumnInfo[i][0] = columnInfo[j][0];
									finalColumnInfo[i][1] = columnInfo[j][1];
									finalColumnInfo[i][2] = columnInfo[j][2];
									finalColumnInfo[i][4] = columnInfo[j][4];
									finalColumnInfo[i][7] = columnInfo[j][7];
									break;
								}
							}
							if(!contains) {
								int iType = -1;
								try{
									iType = GeneralFiltersUtil.getCubeColumnType(graphInfo.getCubeInfo(),tempStr[i]);
								} catch (CubeException e) {
									ApplicationLog.error(e);
								}	
								finalColumnInfo[i] = new String[8];	
								finalColumnInfo[i][0]= tempStr[i];
								finalColumnInfo[i][1] = String.valueOf(CubeUtil.getColType(iType));
								finalColumnInfo[i][2] = ResourceManager.getString("NONE");
								finalColumnInfo[i][7] = graphInfo.getCubeInfo().getDataObjecName();
							}
						}
					} else {
						for(int i=0;i<tempStr.length;i++) { 
							int iType = -1;
							try{
								iType = GeneralFiltersUtil.getCubeColumnType(graphInfo.getCubeInfo(),tempStr[i]);
							} catch (CubeException e) {
								ApplicationLog.error(e);
							}	
							finalColumnInfo[i] = new String[8];	
							finalColumnInfo[i][0]= tempStr[i];
							finalColumnInfo[i][1] = String.valueOf(CubeUtil.getColType(iType));
							finalColumnInfo[i][2] = ResourceManager.getString("NONE");
							finalColumnInfo[i][7] = graphInfo.getCubeInfo().getDataObjecName();
						}
					}
					
					for(Object key : conditionMap.keySet()) {
						boolean contains = false;
						for(int i=0; i<tempStr.length; i++) {
							contains = tempStr[i].equals(key.toString());
							if(contains)
								break;
						}
						if(!contains)
							smartenService.setPageFilterConditions(key.toString(), new CubeVector<>(), graphInfo);
					}
					
				} else {
					columnInfo = new String[][] {};
					smartenService.setFilterConditions(new HashMap<>(), true, graphInfo);
				}
				graphInfo.setFilterColumnInformation(finalColumnInfo);
				graphInfo.setMultiPageFilterInfo(finalColumnInfo);
			}
		} catch (Exception e) {
			ApplicationLog.error(e);
		}
		String logAction = ResourceManager.getString("LBL_SAVE_PAGE_FILTER_COLUMNS");		
		auditUserActionLog(logAction, AppConstants.DETAIL,userInfo);
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return refreshObjectData(null,response, userInfo, modelMap);
	}
	
	/**
	 * function to populate Measures and Dimensions for Sort (Quick Settings) 
	 * @param map
	 * @param response
	 * @param request
	 * @param userInfo
	 * @return
	 */
	@RequestMapping (value = "/getSmartenSortData", method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView getSmartenSortData(ModelMap map, HttpServletResponse response, 
			@RequestParam(required = false, value="isFromRank") boolean isFromRank,
			HttpServletRequest request,@LoggedInUser UserInfo userInfo)
	{
		String allMeasureString = "";
		String allDimensionString = "";
		for(int k = 0;k<graphInfo.getDataColLabels3().size();k++)
		{
			if(k==0)
				allMeasureString = graphInfo.getDataColLabels3().get(k).toString();
			else
				allMeasureString = allMeasureString+ "," + graphInfo.getDataColLabels3().get(k).toString();
		}
		if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH && graphInfo.getTheDataColLabels4().size() > 0)
		{
			allMeasureString = allMeasureString + "," + graphInfo.getTheDataColLabels4().get(0).toString();
		}
		if(graphInfo.getGraphType() == SmartenConstants.SMARTENVIEW_TABULAR
				&& null != graphInfo.getMeasureTitleList() && !graphInfo.getMeasureTitleList().isEmpty())
		{
			for(int k = 0;k<graphInfo.getMeasureTitleList().size();k++)
			{
				if(k==0)
					allMeasureString = graphInfo.getMeasureTitleList().get(k).toString();
				else
					allMeasureString = allMeasureString+ "," + graphInfo.getMeasureTitleList().get(k).toString();
			}
		}
		if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
		{
			if(graphInfo.getDataColLabels3().size() == 2)
				allMeasureString = graphInfo.getDataColLabels3().get(0).toString();
			if(graphInfo.getDataColLabels3().size() == 3)
				allMeasureString = graphInfo.getDataColLabels3().get(1).toString();
		}
		
		map.put("allMeasures", allMeasureString);
		map.put("allDimension", StringUtils.join(graphInfo.getDimensionTitleList(), ','));
		map.put("mainOutlinerList", StringUtils.join(graphInfo.getMainOutlinerMeasureAndDimension(), ','));
		
		//New
		Map<String, String> colLabelsMap = new HashMap<String, String>();
		if (graphInfo != null && graphInfo.getGraphProperties() != null && graphInfo.getGraphProperties().getColLabelsMap() != null) {
			colLabelsMap = graphInfo.getGraphProperties().getColLabelsMap();
		}
		map.put("colLabelsAliasMap", colLabelsMap);
		map.put("colLabelsMap", colLabelsMap);		
		
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		
		if(isFromRank)
			return new ModelAndView("smartview/smartenRankSort");
		else
			return new ModelAndView("smartview/smartenSort");	
	}
	
	
	/**
	 * function to populate smarten rank
	 * @param map
	 * @param request
	 * @param response
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value="/getSmartenRankData",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView getSmartenRankData(ModelMap map,HttpServletRequest request,HttpServletResponse response,@LoggedInUser UserInfo userInfo)
	{
		int rankValue = 0;
		boolean isTop = true;
		String allMeasureString = "";
		String allDimensionString = "";
		List allRankList = new ArrayList();
		List allSortList = new ArrayList();
		List dimensionList = graphInfo.getDimensionTitleList();
		
		for(int k = 0;k<graphInfo.getDataColLabels3().size();k++)
		{
			if(k==0)
				allMeasureString = graphInfo.getDataColLabels3().get(k).toString();
			else
				allMeasureString = allMeasureString+ "," + graphInfo.getDataColLabels3().get(k).toString();
		}
		if(graphInfo.getGraphType() == GraphConstants.COMBINED_GRAPH && graphInfo.getTheDataColLabels4().size() > 0)
		{
			allMeasureString = allMeasureString + "," + graphInfo.getTheDataColLabels4().get(0).toString();
		}
		if(graphInfo.getGraphType() == GraphConstants.BUBBLE_GRAPH)
		{
			if(graphInfo.getDataColLabels3().size() == 2)
				allMeasureString = graphInfo.getDataColLabels3().get(0).toString();
			if(graphInfo.getDataColLabels3().size() == 3)
				allMeasureString = graphInfo.getDataColLabels3().get(1).toString();
		}
		
		map.put("allMeasures", allMeasureString);
		map.put("allDimension", StringUtils.join(dimensionList, ','));
		map.put("mainOutlinerList", StringUtils.join(graphInfo.getMainOutlinerMeasureAndDimension(), ','));
		
		// New
		Map<String, String> colLabelsMap = new HashMap<String, String>();
		if (graphInfo != null && graphInfo.getGraphProperties() != null && graphInfo.getGraphProperties().getColLabelsMap() != null) {
			colLabelsMap = graphInfo.getGraphProperties().getColLabelsMap();
		}
		map.put("colLabelsAliasMap", colLabelsMap);
		map.put("colLabelsMap", colLabelsMap);
		
		List<CubeRankDataLabel> rankList = graphInfo.getActiveTemplateProperties().getRankList();
		int rank = 0;
		boolean sortOrder = false;
		boolean hasRank;
		if(rankList.size() > 0)
		{
			for(int i=0;i<dimensionList.size();i++)
			{
				hasRank = false;
				for(int j=0;j<rankList.size();j++)
				{
					if(dimensionList.get(i).toString().equalsIgnoreCase(rankList.get(j).getColumnName().toString()))
					{
						hasRank = true;
						rank = rankList.get(j).getRowLimit();
						sortOrder = rankList.get(j).getSortOrder();
						if(rank == 10000)
							rank = 0;
						allRankList.add(rank);
						allSortList.add(sortOrder);
					}
				}
				if(!hasRank)
				{
					allRankList.add(1);
					allSortList.add(false);
				}
			}
		}
		else
		{
			if(dimensionList!=null && dimensionList.size() > 0) {
			for(int i=0;i<dimensionList.size();i++)
			{
				allRankList.add(1);
				allSortList.add(false);
			}
			}
		}
		map.put("allRankList", allRankList);
		map.put("allSortList", allSortList);
		map.put("rankList", rankList);
		
		map.put("paginationCB",graphInfo.getGraphProperties().isPaginationCB());
		map.put("samplingCB",graphInfo.getGraphProperties().isSamplingCB());
		map.put("snapShotCB",graphInfo.getGraphProperties().isSnapShotSamplingCB());
		map.put("mainResultSetCount",graphInfo.getOriginalResultSetSize());
		
		response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		return new ModelAndView("smartview/smartenRankSort");
	}
	
	
	@RequestMapping (value = "/getMeasureBucketData", method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView getMeasureBucketData(ModelMap map, HttpServletResponse response, 
			HttpServletRequest request,@LoggedInUser UserInfo userInfo)
	{
		String bucketMeasure = "";
		if(graphInfo.getGraphData().isRowMeasure())
		{
			if(bucketMeasure.equals(""))
				bucketMeasure = graphInfo.getGraphData().getColorMeasureLabel()+","; 
		}
		if(graphInfo.getGraphData().isSizeMeasure())
		{
			if(bucketMeasure.equals(""))
				bucketMeasure = graphInfo.getGraphData().getSizeLabel();
			else
				bucketMeasure = bucketMeasure +graphInfo.getGraphData().getSizeLabel()+",";
		}
		if(graphInfo.getGraphData().isShapeMeasure())
		{
			if(bucketMeasure.equals(""))
				bucketMeasure = graphInfo.getGraphData().getShapeLabel();
			else
				bucketMeasure = bucketMeasure+graphInfo.getGraphData().getShapeLabel()+",";
		}
		if(graphInfo.isRowsMeasure())
		{
			for(int i=0;i<graphInfo.getRowsMeasureList().size();i++)
			{
				bucketMeasure = bucketMeasure+graphInfo.getRowsMeasureList().get(i).toString()+",";
			}
		}
		if(graphInfo.isColsMeasure())
		{
			for(int i=0;i<graphInfo.getColsMeasureList().size();i++)
			{
				bucketMeasure = bucketMeasure+graphInfo.getColsMeasureList().get(i).toString()+",";
			}
		}
		if(graphInfo.isCategoryMeasure())
		{
			if(bucketMeasure.equals(""))
				bucketMeasure = graphInfo.getCategoryMeasureLabel();
			else
				bucketMeasure = bucketMeasure + graphInfo.getCategoryMeasureLabel()+",";
		}
		if(graphInfo.getGraphType() == SmartenConstants.HEAT_MAP_GRAPH)//Added for Bug #15415
		{
			bucketMeasure = graphInfo.getDataColLabels3().get(0).toString();
		}
		if (bucketMeasure.endsWith(",")) 
			bucketMeasure = bucketMeasure.substring(0, bucketMeasure.length() - 1);
		if(graphInfo.isCategoryMeasure() && !graphInfo.isPerformAggregation())
			bucketMeasure = graphInfo.getCategoryMeasureLabel();
		
		if(!graphInfo.isPerformAggregation())
		{
			bucketMeasure = "";
			for(int i=0;i<graphInfo.getDataColLabels3().size() - 1;i++)
				bucketMeasure = bucketMeasure + graphInfo.getDataColLabels3().get(i).toString() + ","; 
				
			if (bucketMeasure.endsWith(",")) 
				bucketMeasure = bucketMeasure.substring(0, bucketMeasure.length() - 1);
		}
		
		map.put("bucketMeasure", bucketMeasure);
		
		map.put("smartenMeasureCurrentTabName", "M"+0);
		boolean isPie=false;
		map.put("graphType", graphInfo.getGraphType());
		map.put("smartenMeasureSelectedTabNames", "M"+0);
		
		map.put("smartenProperties", graphInfo.getGraphProperties().getSmartenProperties());
		map.put("graphProperties", graphInfo.getGraphProperties());
		map.put("mainOutlinerList", StringUtils.join(graphInfo.getMainOutlinerMeasureAndDimension(), ','));
		
		return new ModelAndView("smartview/smartenMeasureBucket");
	}
	@Override
	public String getJSON(ConcurrentHashMap inputMap,HttpServletRequest request) {
		
		 String values = smartenService.getJSONFromService(inputMap);
			return values;
	}


	@Override
	public HashtableEx getRetrivalParametersWithDDVM(String columnName, UserInfo userInfo) {
		
		HashtableEx ddvmMap = new HashtableEx();
			ddvmMap = (HashtableEx) smartenService.getActiveDDVMs(graphInfo, userInfo.getUserId());
			return ddvmMap;
	}
	@RequestMapping (value = "/addGraphLinePointProperties")
	@ResponseBody
	public ModelAndView addGraphLinePointProperties(ModelMap modelMap
			,@RequestParam("style") String strStyle	
			,@RequestParam("thickness") String strThickness
			,@RequestParam("borderwidth") String strBorderwidth
			,@RequestParam("borderstyle") String strBorderstyle
			,@RequestParam("bordercolor") String strbordercolor
			,@LoggedInUser UserInfo userInfo) {		
		GraphLineSettingProperties graphLinePointSettingProperties = new GraphLineSettingProperties();
		if(strStyle != null && !strStyle.equalsIgnoreCase(""))
		{			
			graphLinePointSettingProperties.setStyle(strStyle);
		}
		if(strThickness != null && !strThickness.equalsIgnoreCase(""))
		{
			graphLinePointSettingProperties.setThickness(strThickness);
		}
		if(strBorderwidth != null && !strBorderwidth.equalsIgnoreCase(""))
		{
			graphLinePointSettingProperties.setBorderwidth(strBorderwidth);
		}
		if(strBorderstyle != null && !strBorderstyle.equalsIgnoreCase(""))
		{
			graphLinePointSettingProperties.setBorderstyle(strBorderstyle);
		}
		if(strbordercolor != null && !strbordercolor.equalsIgnoreCase(""))
		{
			graphLinePointSettingProperties.setBordercolor(strbordercolor);
		}
		
		List<GraphLineSettingProperties>  graphlinepointSettingPropertiesList = null;
		if(graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinepointPropertiesList()!= null && !graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinepointPropertiesList().isEmpty())
		{
			graphlinepointSettingPropertiesList = graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinepointPropertiesList();
		}
		else
		{
			graphlinepointSettingPropertiesList = new ArrayList<GraphLineSettingProperties>();
		}
		if(graphlinepointSettingPropertiesList!= null)
		{
			graphlinepointSettingPropertiesList.add(graphLinePointSettingProperties);
		}		
		graphInfo.getGraphProperties().getGraphLineProperties().setGraphlinepointPropertiesList(graphlinepointSettingPropertiesList);
		modelMap.put("graphLineProperties",graphInfo.getGraphProperties().getGraphLineProperties());
		auditUserActionLog(ResourceManager.getString("LBL_ADD_GRAPH_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph/graphlinepointobjects");
	}
	
	@RequestMapping (value = "/removeGraphlinepointPropObj")
	@ResponseBody
	public ModelAndView removeGraphlinepointPropObj(ModelMap modelMap,@RequestParam("graphlineobjkey") String strGraphlineobjkey,@LoggedInUser UserInfo userInfo){
		List<GraphLineSettingProperties>  graphlinepointSettingPropertiesList = null;	
		if(strGraphlineobjkey != null && !strGraphlineobjkey.equalsIgnoreCase("")){
			if(graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinepointPropertiesList() != null && !graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinepointPropertiesList().isEmpty())
			{
				graphlinepointSettingPropertiesList = graphInfo.getGraphProperties().getGraphLineProperties().getGraphlinepointPropertiesList();
				graphlinepointSettingPropertiesList.remove(Integer.parseInt(strGraphlineobjkey));								
				graphInfo.getGraphProperties().getGraphLineProperties().setGraphlinepointPropertiesList(graphlinepointSettingPropertiesList);
			}
		}
		modelMap.put("graphLineProperties",graphInfo.getGraphProperties().getGraphLineProperties());		
		auditUserActionLog(ResourceManager.getString("LBL_DELETE_GRAPH_LINE_PROPERTIES"), AppConstants.DETAIL,userInfo);
		return new ModelAndView("graph/graphlinepointobjects");
	}

	@RequestMapping (value = "/showObjectloadInfoPage")
	public ModelAndView showObjectloadInfoPage(ModelMap model,@LoggedInUser UserInfo userInfo) {
    	
		try {
			//model.put("strTmp", getGraphInfo().getOnLoadObjectInfo());
			if(getGraphInfo().getOnLoadObjectInfo() != null && !getGraphInfo().getOnLoadObjectInfo().isEmpty() 
					&& getGraphInfo().getOnLoadObjectInfo().get(0) != null && !getGraphInfo().getOnLoadObjectInfo().get(0).isEmpty()) {
				model.put("strTmp", getGraphInfo().getOnLoadObjectInfo().get(0));
			}
			if(getGraphInfo().getOnLoadObjectInfo() != null && !getGraphInfo().getOnLoadObjectInfo().isEmpty() 
					&& getGraphInfo().getOnLoadObjectInfo().get(1) != null && !getGraphInfo().getOnLoadObjectInfo().get(1).isEmpty()) {
				model.put("strTemplate", getGraphInfo().getOnLoadObjectInfo().get(1));
			}
			getGraphInfo().setOnLoadObjectInfo(null);
		} catch (Exception e) {
			ApplicationLog.error(e);
		}
		return  new ModelAndView("showObjectloadInfoPage");
	}
	
	@Override
	public Map<String, RProfileInfo> getDashboardCubeIdAndRProfileMap() {
		
		return null;
	}

	@Override
	public Map<String, IDataObject> getDashboardCubeIdAndInputCubeInfoMap(HttpServletRequest request) {
		return null;
	}


	@ModelAttribute
	public void getCommonModelMap(ModelMap model) {
		if(smartenService != null)
			model.addAttribute("isAdaptive",smartenService.getAdaptiveBehaviour());		
	}

@Override
public List<RScriptInputOutputVO> getrScriptInputVOs() {
	return null;
}

@Override
public void setrScriptInputVOs(List<RScriptInputOutputVO> rScriptInputVOs) {
	
}

@Override
public Map<String, Object> getCubeWiseDimensionMap(List<IDataObject> cubeList, boolean showAll, UserInfo userInfo,HttpServletRequest request) {
	return null;
}

@Override
public Map<String, Object> getCommonColumnListFromCubes(Map cubeDimensionMap, UserInfo userInfo,HttpServletRequest request,List<IDataObject> cubeList) {
	return null;
}

@Override
public boolean isSingleColumnSort() {
	return false;
}

@Override
public List<String> prepareMeasureItemsList(UserInfo userInfo) throws ALSException, CubeException {	
	return graphInfo.getMeasureTitleList();
}

@Override
public Map<String, ArrayList<String>> prepareLatitudeLongitudeMap(UserInfo userInfo)
		throws ALSException, CubeException {
	return null;
}

@Override
public void setViewStruct(CubeVector cubeVector) {
	
}

@Override
public CubeViewInfo getViewStruct() {
	CubeViewInfo info=null;
	try {
		info= smartenService.getViewStruct();
	} catch (CubeException e) {
		ApplicationLog.error(e);
	}
	return info;
}

@Override
public ArrayList<String> getOrderdAllDimensions() {
	return null;
}

@Override
public CubeVector getOrderedDataColumnInfoList() {
	return null;
}

@PreDestroy 
private void preDestoy() {
	//ApplicationLog.info("SmartenContoller == >> Destory Call");
	try {
	if(getServiceMap() != null && getServiceMap().size() > 0) {
		getServiceMap().forEach((k,v)->{
			if(v instanceof SmartenService) {
				((SmartenService)v).clear();
			}
		});
	}
	} catch(Exception e) {
		ApplicationLog.error(e);
	}
}

@Override
public boolean isMeasureSortApply() {
	return false;
}

@Override
public boolean isSkipcubedatasetcolumndataaccesspermission(HttpServletRequest request,UserInfo userInfo) {
	String dashboardId =""+request.getAttribute("objectId");
	boolean isskipcolumnpermission = false;
	if (graphInfo != null) {
		isskipcolumnpermission = graphInfo.isSkipcubedatasetcolumndataaccesspermission(userInfo);
	}
	return isskipcolumnpermission;
}
	@Override
	public Map<String, String> geoMapSpotlighterMap(UserInfo userInfo) throws ALSException, CubeException {
		return null;
	}
	
	@RequestMapping("/smartenViewPageFilterMobile")
	public ModelAndView smartenViewPageFilterMobile(ModelMap map,@LoggedInUser UserInfo userInfo,@RequestParam(value="dimensionName", required = false)String dimensionName){
		String[][] pageFilterInformation = null;
		String selectedValues = "";
		String cubeId = "";
		int currentIndex = 0;
		dimensionName = dimensionName.trim().toString();
		try {
			if(getPageFilters(userInfo, null) != null) {
    			pageFilterInformation = getPageFilters(userInfo, null);
    			CubeVector vect = new CubeVector();
    			for(int i=0; i < pageFilterInformation.length; i++) {
    				if(pageFilterInformation[i][4] != null && pageFilterInformation[i][0].toString().equals(dimensionName)) {
    					cubeId = pageFilterInformation[i][4];
    					currentIndex = i;
    				}
    			}
    			try {
					vect = getFilterConditions(pageFilterInformation[currentIndex][0], null);
				} catch (ALSException e) {
					ApplicationLog.error(e);
				}
    			if(vect != null && !vect.isEmpty()) {
    				for(int i=0; i < vect.size(); i++) {
    					CubeConditionInfo cinfo = (CubeConditionInfo) vect.get(i);
    					if(i == vect.size() - 1) {
    						selectedValues = selectedValues + cinfo.getConditionalValue();
    					}else {
    						selectedValues = selectedValues + cinfo.getConditionalValue()+ ",";
    					}
    				}
    			}
			}
			for(int i=0; i<pageFilterInformation.length;i++) {
    			if(pageFilterInformation[i][2].contains("!NULL") && pageFilterInformation[i][2].contains("!")){
					pageFilterInformation[i][2] = pageFilterInformation[i][0]+"="+"(NOT NULL)" +"@#@#@#" + pageFilterInformation[i][0]+"="+"(NOT NULL)";
				}
    			if(pageFilterInformation[i][0].equals(dimensionName) && !pageFilterInformation[i][2].equals("None")) {
    				map.put("pageFilterField",pageFilterInformation[i]);
    			}
			}
		} catch (CubeException e) {
			ApplicationLog.error(e);
		}
		
		map.put("selectedValues",selectedValues);
		map.put("dimensionName",dimensionName);
		map.put("cubeId",cubeId);
		//System.out.println("TEST");
		return new ModelAndView("smartview/smartPageFilter");
	}
	
	@RequestMapping("getSmartenViewPageFilterValue")
	@ResponseBody
	public HashSet<String> getFilterValue(@LoggedInUser UserInfo userInfo) {
		HashSet<String> filterStatusMap = new HashSet<>();
		/*Added For pageFilterStatus*/
		String[][] pageFilterInformation = null;
		String pageFilterColumn = "";
		String cubeId = "",selectedValues="";
		int currentIndex=0;
		HashSet<String> selectedColumn = new HashSet<String>();
		try {
			if(getPageFilters(userInfo, null) != null) {
    			pageFilterInformation = getPageFilters(userInfo, null);
    			CubeVector vect = new CubeVector();
    			for(int j=0; j < pageFilterInformation.length; j++) {
	    			selectedColumn.add(pageFilterInformation[currentIndex][0]);
	    			currentIndex++;
    			}
			}
			//modelMap.put("selectedFilterColumn", selectedColumn);
			//System.out.println(selectedValues);
		} catch (CubeException e) {
			ApplicationLog.error(e);
		}
		return selectedColumn;
	}
	
	@RequestMapping("getSmartenViewPageFilterStatus")
	@ResponseBody
	public Map<String, Boolean> getFilterStatus(@LoggedInUser UserInfo userInfo) {
		HashSet<String> filterStatusMap = new HashSet<>();
		String[][] pageFilterInformation = null;
		String pageFilterColumn = "";
		String cubeId = "",selectedValues="";
		int currentIndex=0;
		Map<String, Boolean> selectedColumn = new HashMap<String, Boolean>();
		try {
			if(getPageFilters(userInfo, null) != null) {
    			pageFilterInformation = getPageFilters(userInfo, null);
    			CubeVector vect = new CubeVector();
    			for(int j=0; j < pageFilterInformation.length; j++) {
    				
    					currentIndex = j;
    				
    		
	    			try {
						vect = getFilterConditions(pageFilterInformation[currentIndex][0], null);
					} catch (ALSException e) {
						ApplicationLog.error(e);
					}
	    			if(vect != null && !vect.isEmpty()) {
	    				for(int i=0; i < vect.size(); i++) {
	    					CubeConditionInfo cinfo = (CubeConditionInfo) vect.get(i);
	    					
	    					selectedColumn.put(cinfo.getColumnName(),true);
	    				}
	    			}else {
	    					selectedColumn.put(pageFilterInformation[currentIndex][0],false);
	    			}
    			}
			}
			//System.out.println(selectedValues);
		} catch (CubeException e) {
			ApplicationLog.error(e);
		}
		return selectedColumn;
	}


	@Override
	public BIDataset<Row> getSparkBIResultset() {
		return smartenService.getSparkResultset();
	}

	
	@Override
	public String getColumnValue(String strColumnName, String strCellref, boolean isUDDC) {
		Object obj="";
		try {
			obj = smartenService.getColumnValue(strColumnName,graphInfo,isUDDC);
		} catch (Exception e) {
			ApplicationLog.error(e);
		}
		return obj+"";
	}
	
	@Override
	public String getDataOperationName(String columnName , String strCellref, UserInfo userInfo) {

		String strOperationName = "";
		strOperationName = "Sum";
		strOperationName = smartenService.getDataOperationName(strCellref,columnName, graphInfo,userInfo);
		return strOperationName;
	
	}
	
	@Override
	public void setCubeForLineage(){
		smartenService.setCubeforLineage(graphInfo);
	}
	
	@Override
	public Map<SelectItem, Integer> prepareAllOutlinerItemsMap(UserInfo userInfo) throws CubeException {
		return smartenService.prepareAllOutlinerItemsMap(graphInfo, userInfo);
	}
	

	@Override
	public ArrayList<PageFilterNew> getPageFilterNew(UserInfo userInfo, String objectId) throws CubeException {
		if(objectId!=null) {
			SmartenInfo smartenInfo=getGraphObjectFromMap(objectId);
			return getGraphServiceFromMap(objectId).getPageFilterNew(smartenInfo);
		}
		return smartenService.getPageFilterNew(graphInfo);
	}
	
	@Override
	public void setPageFilterInfoNew(ArrayList<PageFilterNew> pageFiter, HttpServletRequest request) {
		graphInfo.setPageFilterColumnInfo(pageFiter);
	}

	@Override
	public SmartenInfo getGraphInformation(UserInfo userInfo) {
		// TODO Auto-generated method stub
		return graphInfo;
	}
	
	public List<String> getDimensionList(UserInfo userInfo){
		
		CubeVector cols = getColLabelNameVector(null);
		CubeVector rows = getRowLabelNameVector(null);
		
		List<String> dimension = new ArrayList<String>();
		
		dimension.addAll(cols);
		dimension.addAll(rows);
		
		if(graphInfo.getGraphType() == GraphConstants.D3_BUBBLE || graphInfo.getGraphType() == GraphConstants.D3_CHORD ||
				graphInfo.getGraphType() == GraphConstants.D3_SUNBURST || graphInfo.getGraphType() == GraphConstants.D3_TREELAYOUT ||
						graphInfo.getGraphType() == GraphConstants.D3_TREEMAP ||
								graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_MAP ||
										graphInfo.getGraphType() == GraphConstants.SMARTENVIEW_TABULAR)
		{
			return new ArrayList<>(graphInfo.getDimensionTitleList());
			
		}else {
			return dimension;
		}	
	}

	@Override
	public Object setDataGroupDetails(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo, ModelMap map) {
		
		Object status = null;
		
		try {
			smartenService.setGroupDetail(graphInfo);
			status = AppConstants.SUCCESS_STATUS;
			response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
		} catch (CubeException e) {
			ApplicationLog.error(ResourceManager.getString("LOG_ERROR_FAILED_TO_SAVE_GROUP", new Object[]{userInfo.getUsername(), getObjectDisplayName() }), e);
			status = ResourceManager.getString("ERROR_MSG_FAILED_TO_SAVE");
		}
		return status;
	}

	@Override
	public List<Group> getDataGroupList(UserInfo userInfo) {
		List<Group> list = null;
		
		if(graphInfo != null) {
			list = graphInfo.getGroupList();
		}
		return list;
	}

	@Override
	public void removeFromPackColumnDDVMDataGroup(String colName, String actualTextValues, UserInfo userInfo)
			throws CubeException {
		removeFromPackColumnDDVM(colName, actualTextValues, graphInfo, userInfo);
	}

	@Override
	public void setActiveSpotLighter(UserInfo userInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getAppliedFilters(ModelMap modelMap, UserInfo userInfo, HttpServletRequest request, HttpSession session) {
		smartenService.showObjectInformation(graphInfo, userInfo,modelMap,getCubeInfo(null));
		showSmartenSamplingFilter(modelMap, userInfo, Long.parseLong(0+""), false);
	}

	@Override
	public void getViewConditionApplied(ModelMap modelMap, UserInfo userInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getSamplingApplied(ModelMap modelMap, UserInfo userInfo) {
		modelMap.put("samplingApplied",graphInfo.getGraphProperties().isSampling());
		modelMap.put("appliedSamplingApplied",graphInfo.getGraphProperties().isSampling() && !graphInfo.getSmartenSampling().isApplied() );
	}	

	/*public void setDefaultTimeFormatForProperties(SmartenInfo graphInfo,UserInfo userInfo) {
		try {
			if(graphInfo != null && graphInfo.getGraphData() != null && graphInfo.getGraphData().getDimensionListForSmartenview() != null) {
				List dimensionsList = graphInfo.getGraphData().getDimensionListForSmartenview();
				for(Object obj : dimensionsList){
					if(obj != null) {
						String dimName = obj.toString();
						int colType = getColumnType(dimName, userInfo);
						if(colType == Types.TIMESTAMP) {
							String userTimeFormat = userInfo.getTimeFormat();
							String timeFormat = graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().getTimeFormat();
							if(timeFormat != null && timeFormat.isEmpty()) {
								graphInfo.getGraphProperties().getxAxisProperties().getLabelProperties().setTimeFormat(userTimeFormat);
							}
							break;
						}
					}
				}
			}
		}catch(Exception e) {
			ApplicationLog.error(e);
		}

	}*/
	
	@Override
	public Hashtable getActiveVariableMap(HttpServletRequest request) {
		try {
			return smartenService.getActiveVariableMap();
		}catch(Exception e) {
			ApplicationLog.error(e);
		}
		return null;
	}

	@Override
	public Map<String, String> getDashboardFilters() {		
		return null;
	}
	
	/**
	 * Show share dialog
	 */
	@RequestMapping(value="/showShareSmarten")
	@ResponseBody
	public ModelAndView showShareSmarten(ModelMap map,@LoggedInUser UserInfo userInfo,@RequestParam(value = "objectid", required = false) String strObjectId){
		//ApplicationLog.info("in showShareSmarten");
		map.put("objectid",strObjectId);
		map.put("isFromShare",true);
		return new ModelAndView("sharesmarten");
	}

	@Override
	public List<Pair> getRangeBucketList(String strDimensionName, String strSearchStr, UserInfo userInfo,
			int lastIndexValue, HttpServletRequest request, String objectId, String strCubeId,boolean isBackEnd) {
		boolean isUDDC = false;
		if(isBackEnd) {
		Vector<CubeColumnInfo> cubeColumnInfoList = graphInfo.getDataColumnInfoList();
		for (int iCnt = 0; iCnt < cubeColumnInfoList.size(); iCnt++) {
			CubeColumnInfo cubeColumnInfo = cubeColumnInfoList.elementAt(iCnt);
			String columnName = cubeColumnInfo.getName();
			String srcColumnName = cubeColumnInfo.getSourceName();
			if(!columnName.equals(srcColumnName)) {
				if (columnName.equals(strDimensionName)) {
					strDimensionName = srcColumnName;
					break;
				}
			}
		}
		} else {
			Vector<CubeColumnInfo> cubeColumnInfoList = graphInfo.getDataColumnInfoList();
			for (int iCnt = 0; iCnt < cubeColumnInfoList.size(); iCnt++) {
				CubeColumnInfo cubeColumnInfo = cubeColumnInfoList.elementAt(iCnt);
				String columnName = cubeColumnInfo.getName();
				String srcColumnName = cubeColumnInfo.getSourceName();
				if(!columnName.equals(strDimensionName)) {
					if (strDimensionName.equals(srcColumnName)) {
						isUDDC = true;
						break;
					}
				} else if(columnName.equals(strDimensionName)) {
					isUDDC = true;
					break;
				}
			}
			if(!isUDDC && graphInfo.getCubeInfo() != null) {
				List<ActiveUDDCInfo> activeUDDCList = getActiveTemplateProperties(null).getActiveUDDCInfo();
				
				for(ActiveUDDCInfo activeUDDC : activeUDDCList) {
					String activeUddcName = activeUDDC.getUddcTemplateInfo().getColumnName();
					
					if(activeUddcName.equals(strDimensionName)) {
						isUDDC = true;
						break;
					}
				}
			}
			if(!isUDDC && graphInfo.getDateFrequencyMap() != null && graphInfo.getDateFrequencyMap().get(strDimensionName) != null &&
					!graphInfo.getDateFrequencyMap().get(strDimensionName).isEmpty()) {
				isUDDC = true;
			}
		}
		
		try {
			List<Pair> values ;
			values = smartenService.getRangeBucket(strDimensionName, graphInfo.getCubeInfo(), lastIndexValue,  strSearchStr, 
					  objectId, userInfo, isUDDC, smartenService.getMainResultset(), smartenService.getDataResultSet());
			return values;
		} catch (Exception e) {
			ApplicationLog.error(e);
			return new ArrayList<>();
		}
	}
	@RequestMapping(value = "/getObjectReferenceData")
	@ResponseBody
	public ModelAndView getObjectReferenceData(ModelMap modelMap, @LoggedInUser UserInfo userinfo) {

		// for associated dashboard
		modelMap.put("associatedDashboardList",
				repositoryService.getDashboardListAssociatedWithObjectMap(graphInfo.getGraphId(), userinfo));
		// for bi object datasource
		modelMap.put("associatedBiObjecttList",
				repositoryService.getBIObjectDataSourcetListAssociatedWithObjectMap(graphInfo.getGraphId()));
		// for Linked object in dashboard
		modelMap.put("linkedDashboardObjMap",
				repositoryService.getObjectsLinkedInDashboard(graphInfo.getGraphId(), userinfo));
		// for Linked object in kpi
		modelMap.put("linkedKPIObjMap", repositoryService.getObjectsLinkedInKPI(graphInfo.getGraphId(), userinfo));
		// for Linked object in  kpi group
		modelMap.put("linkedKPIGroupObjMap", repositoryService.getObjectsLinkedInKPIGroup(graphInfo.getGraphId(), userinfo));
		return new ModelAndView("graph/objectReferenceData");
	}
	
	@RequestMapping (value = "/customLegendSortDimensionValue")
	@ResponseBody
	public ModelAndView getCustomLegendSortDimensionValue(@RequestParam("selectedDimension") String strDimensionName,
			ModelMap map, @LoggedInUser UserInfo userInfo,
			@RequestParam(value="objectId", required=false) String objectId,HttpServletRequest request){
		try {
			List selectedValue =new ArrayList<>(); 
			if(graphInfo.getGraphProperties().getCustomLegendSelectedValueList() != null && !graphInfo.getGraphProperties().getCustomLegendSelectedValueList().isEmpty())
			 selectedValue = new ArrayList<>(graphInfo.getGraphProperties().getCustomLegendSelectedValueList());
			if(graphInfo.getDrilldownBreadcrumbMap() != null && graphInfo.getDrilldownBreadcrumbMap().containsKey("Row")) {
				selectedValue = new ArrayList<>();
			}
			List values = new ArrayList<>(graphInfo.getGraphData().getRowList());
			if(null ==graphInfo.getGraphData().getRowList() || graphInfo.getGraphData().getRowList().isEmpty()) {
				 values = new ArrayList<>(graphInfo.getGraphData().getColList());
			}
			if (graphInfo.getGraphProperties().getCustomLegendSelectedValueList() != null
					&& !graphInfo.getGraphProperties().getCustomLegendSelectedValueList().isEmpty()) {
				values.removeAll(selectedValue);
			}
			if(graphInfo.getGraphType()== GraphConstants.COMBINED_GRAPH && graphInfo.getGraphData().getCmbBarrowList() != null && !graphInfo.getGraphData().getCmbBarrowList().isEmpty() ) {
				values = new ArrayList<>(graphInfo.getGraphData().getCmbBarrowList());
			}
			/*if(graphInfo.getGraphType()== GraphConstants.COMBINED_GRAPH && graphInfo.getGraphData().getCmbBarcolList() != null && !graphInfo.getGraphData().getCmbBarcolList().isEmpty()) {
				values = new ArrayList<>(graphInfo.getGraphData().getCmbBarcolList());
			}*/
		
			map.put("availableLegendValues", values);
			map.put("selectedLegendValues", selectedValue);
		
		}catch (Exception e) {
			ApplicationLog.error(e);
		} 
		return new ModelAndView("analysis/customLegendSortDimensionValue");
	}

	@Override
	public Map<String, String> getDateFrequencyMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convertRelativePeriodToAbsoluteString(UserInfo userInfo, String stringToConvert, int dateType,
			int yearType, int criteriaType, HttpServletRequest request, String columnName, String cubeId, boolean isDisplayRange) {
		List<IDataObject> cubeList = null;
		String result = "";
		try {
			cubeList = getCubeListFromDifferentObject(userInfo,request);
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		}
		
		result = smartenService.convertRelativePeriodToAbsoluteString(getCubeInfo(null), getObjectId(), cubeList, userInfo, stringToConvert, dateType, yearType, criteriaType, request, columnName, cubeId, isDisplayRange);
		
		return result;
	}

	@Override
	public void saveFiltersOnObject(UserInfo userInfo) {
		try {
			if(graphInfo.getGraphMode()!= AppConstants.NEW_MODE) {
				smartenService.saveFiltersOnObject(graphInfo, userInfo, getStrParentHierchy());
			}
		} catch (DatabaseOperationException e) {
			ApplicationLog.error(e);
		}
		
	}

	@Override
	public UserInfo getObjectCreator() {
		// TODO Auto-generated method stub
		return graphInfo.getCreatedBy();
	}
	
	@Override
	public boolean isObjectOpenedFromDashBoard() {
		if(graphInfo.getDashboardInfo()!=null) {
			String dbId = graphInfo.getDashboardInfo().getDashboardId();
			if(dbId != null && !dbId.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	@ResponseBody
	@RequestMapping(value = "/applyLegendPagination")
	public String applyLegendPagination(
			@RequestParam(value = "objectId", required = false) String strObjectId,
			@RequestParam(value = "legendIndex", required = false) int legendIndex,
			@RequestParam(value = "legendQuantity", required = false) int legendQuantity,
			@RequestParam(value = "graphCount", required = false) int graphCount,
			@RequestParam(value="graphCurrentIndex", required=false) int graphCurrentIndex,ModelMap map,
			@RequestParam(value = "categoryIndex", required = false,defaultValue="0") int categoryIndex,
			@RequestParam(value = "categoryQuantity", required = false,defaultValue="0") int categoryQuantity,
			@RequestParam(value = "direction", required = false) String direction,
			@LoggedInUser UserInfo userInfo) {
		
		long getDataStartTime = System.currentTimeMillis();	
		
		graphInfo.getGraphData().setGraphCount(graphCount);
		graphInfo.getGraphData().setGraphCurrentIndex(graphCurrentIndex);
		graphInfo.getGraphData().setPaginationDirection(direction);
		
		String[] jsonArr = new String[2]; 
		try {
			jsonArr =  smartenService.generateGraph(graphInfo,"",false,userInfo, legendIndex);
			
		} catch (Exception e) {
			ApplicationLog.error(e);
			jsonArr[0] = null;
			
		}
		if(jsonArr.length > 0 && jsonArr[0]==null)
			jsonArr[0]="1";
		long getDataEndTime = System.currentTimeMillis();
		ApplicationLog.debug("SetLegend ENDS time is ==>"+(getDataEndTime - getDataStartTime));	
		return jsonArr[0];				
	}

	@Override
	public Map<SelectItem, Integer> prepareCubeAllItemsMapWithType(boolean isAddMeasure, UserInfo uInfo,
			HttpServletRequest request, String strCubeId)
			throws ALSException, CubeException, DatabaseOperationException {
		return prepareCubeAllItemsMap(isAddMeasure, uInfo);
	}

	@Override
	public Map<String, ColumnNameDisplayVo> getDisplayNameMap() {
		// TODO Auto-generated method stub
		return null;
	}
}