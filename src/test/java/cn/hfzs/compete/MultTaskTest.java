package cn.hfzs.compete;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//整合
@ContextConfiguration(locations = "classpath:testApplicationContext.xml")
//加载配置
public class MultTaskTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	ProcessEngine processEngine;
	
	@Autowired
	RepositoryService repositoryService;
	
	private static String driver = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://127.0.0.1:3306/kad-ww?characterEncoding=utf-8";
	private static String username = "root";
	private static String userpwd = "123";

	@Test
	public void createUser() {
		System.out.println(repositoryService);
	}

	@Test
	public void createUserGroup() {
		/** 添加用户角色组 */
		IdentityService identityService = processEngine.getIdentityService();//
		// 创建角色
		identityService.saveGroup(new GroupEntity("gys"));
		// 创建用户
		identityService.saveUser(new UserEntity("gys1"));
		identityService.saveUser(new UserEntity("gys2"));
		// 建立用户和角色的关联关系
		identityService.createMembership("gys1", "gys");
		identityService.createMembership("gys2", "gys");
		System.out.println("添加组织机构成功");

	}

	/** 部署流程定义（从inputStream） */
	@Test
	public void deploymentProcessDefinition_inputStream() {
		InputStream inputStreamBpmn = this.getClass().getResourceAsStream(
				"multtask.bpmn");
		InputStream inputStreamPng = this.getClass().getResourceAsStream(
				"multtask.png");
		Deployment deployment =repositoryService// 与流程定义和部署对象相关的Service
				.createDeployment()// 创建一个部署对象
				.name("会签")// 添加部署的名称
				.addInputStream("multtask.bpmn", inputStreamBpmn)//
				.addInputStream("multtask.png", inputStreamPng)//
				.deploy();// 完成部署
		System.out.println("部署ID：" + deployment.getId());//
		System.out.println("部署名称：" + deployment.getName());//
	}

	/** 启动流程实例 */
	@Test
	public void startProcessInstance() {
//		Map<String, Object> vr = new HashMap<String, Object>();
//		List<String> assigneeList = new ArrayList<String>();
//		assigneeList.add("gys1");
//		assigneeList.add("gys2");
//		// assigneeList.add("gys3");
//		// assigneeList.add("gys4");
//		vr.put("assigneeList", assigneeList);

		// 流程定义的key
		String processDefinitionKey = "multtask";
		ProcessInstance pi = processEngine.getRuntimeService()// 与正在执行的流程实例和执行对象相关的Service
				.startProcessInstanceByKey(processDefinitionKey);// 使用流程定义的key启动流程实例，key对应helloworld.bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动

		System.out.println("流程实例ID:" + pi.getId());// 流程实例ID 101
		System.out.println("流程定义ID:" + pi.getProcessDefinitionId());// 流程定义ID

		// 修改多任务参数
		/*Connection conn = null;
		PreparedStatement pst = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, username, userpwd);
			String sql = "update act_ru_variable arv set LONG_ = ?, TEXT_ = ? where NAME_ = ? and PROC_INST_ID_=?";
			pst = conn.prepareStatement(sql);
			pst.setLong(1, Long.MAX_VALUE);
			pst.setString(2, Long.MAX_VALUE + "");
			pst.setString(3, "nrOfInstances");
			pst.setString(4, pi.getId());

			pst.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (pst != null) {
			try {
				pst.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
	}

	/** 查询当前人的个人任务 */
	@Test
	public void findMyPersonalTask() {
		//String assignee = "审核员";// 审核员 供应商1 供应商2 采购方
		String assignee = "gys1";// 审核 供应商1 供应商2 采购方
		//String assignee = "采购方";// 审核 供应商1 供应商2 采购方
		// String assignee = "供应商";//审核 供应商1 供应商2 采购方
		List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.createTaskQuery()// 创建任务查询对象
				/** 查询条件（where部分） */
				.taskAssignee(assignee)// 指定个人任务查询，指定办理人
				// .taskCandidateUser(assignee)//组任务的办理人查询
				// .processDefinitionId(processDefinitionId)//使用流程定义ID查询
				// .processInstanceId(processInstanceId)//使用流程实例ID查询
				// .executionId(executionId)//使用执行对象ID查询
				/** 排序 */
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				/** 返回结果集 */
				// .singleResult()//返回惟一结果集
				// .count()//返回结果集的数量
				// .listPage(firstResult, maxResults);//分页查询
				.list();// 返回列表
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				System.out.println("任务ID:" + task.getId());
				System.out.println("任务名称:" + task.getName());
				System.out.println("任务的创建时间:" + task.getCreateTime());
				System.out.println("任务的办理人:" + task.getAssignee());
				System.out.println("流程实例ID：" + task.getProcessInstanceId());
				System.out.println("执行对象ID:" + task.getExecutionId());
				System.out.println("流程定义ID:" + task.getProcessDefinitionId());
				System.out
						.println("########################################################");
			}
		}
		System.out.println("multtaskProcessTest.findMyPersonalTask()");
	}

	/** 查询当前人的组任务 */
	@Test
	public void findMyGroupTask() {
		String candidateUser = "gys2";
		List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.createTaskQuery()// 创建任务查询对象
				.taskCandidateUser(candidateUser)// 组任务的办理人查询
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				.list();// 返回列表
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				System.out.println("任务ID:" + task.getId());
				System.out.println("任务名称:" + task.getName());
				System.out.println("任务的创建时间:" + task.getCreateTime());
				System.out.println("任务的办理人:" + task.getAssignee());
				System.out.println("流程实例ID：" + task.getProcessInstanceId());
				System.out.println("执行对象ID:" + task.getExecutionId());
				System.out.println("流程定义ID:" + task.getProcessDefinitionId());
				System.out
						.println("########################################################");
			}
		}
		System.out.println("MultTaskTest.findMyGroupTask()");
	}

	/** 完成我的任务 确认 */
	@Test
	public void completeMyPersonalTask() {
		// 任务ID
		String taskId = "3204";
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("approve", "1");// 0: 不通过，1： 通过
		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId, variables);
		System.out.println("完成任务：任务ID：" + taskId);
	}

	/** 完成我的任务 报价 */
	@Test
	public void completeMyPersonalTaskShenhe() {
		// 任务ID
		String taskId = "3413";// 2717 2722 2727 2732
		Map<String, Object> ve = new HashMap<String, Object>();
		ve.put("pirce2", "200");
		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId, ve);
		System.out.println("完成任务：任务ID：" + taskId);
	}

	@Test
	public void complete() {
		// 任务ID
		String taskId = "4206";
		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId);
		System.out.println("完成任务：任务ID：" + taskId);
	}

	/** 完成我的任务 竞价确认 */
	@Test
	public void okCompleteMyPersonalTask() {

		// 任务ID
		String taskId = "4403";
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("isok", "yes");// 是否同意：

		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId, variables);
		System.out.println("完成任务：任务ID：" + taskId);
	}

	/**
	 * 查看历史变量
	 */
	@Test
	public void findHisVariables() {
		// 执行的流程id
		String processInstanceId = "4101";
		// 查看供应商报价
		List<HistoricVariableInstance> list = processEngine.getHistoryService()
				.createHistoricVariableInstanceQuery()//
				.processInstanceId(processInstanceId)//
				.orderByVariableName().asc()//
				.list();
		if (list != null) {
			System.out.println("流程实例ID		参数名		参数值		");
			for (HistoricVariableInstance his : list) {
				System.out.print("" + his.getProcessInstanceId());
				System.out.print("			" + his.getVariableName());
				System.out.print("		" + his.getValue());
				System.out.println();
			}
		}
	}

	/**
	 * 查看历史任务
	 */
	@Test
	public void findHisTask() {
		// 执行的流程id
		String taskId = "2732";
		// 查看供应商报价
		HistoricTaskInstance task = processEngine.getHistoryService()
				.createHistoricTaskInstanceQuery().taskId(taskId)
				.singleResult();

		System.out.println("任务ID:" + task.getId());
		System.out.println("任务名称:" + task.getName());
		System.out.println("任务的创建时间:" + task.getStartTime());
		System.out.println("任务的办理人:" + task.getAssignee());
		System.out.println("流程实例ID：" + task.getProcessInstanceId());
		System.out.println("执行对象ID:" + task.getExecutionId());
		System.out.println("流程定义ID:" + task.getProcessDefinitionId());
	}

	/**
	 * 获取待跟踪的流程图
	 * 
	 * @return
	 * @throws Exception
	 */
	@Test
	public void viewProcessPic() throws Exception {
		String executionId = "3201";

		// 不使用spring请使用下面的两行代码 解决乱码问题
		ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines
				.getDefaultProcessEngine();
		Context.setProcessEngineConfiguration(defaultProcessEngine
				.getProcessEngineConfiguration());

		ProcessInstance processInstance = defaultProcessEngine//
				.getRuntimeService()//
				.createProcessInstanceQuery()//
				.processInstanceId(executionId)//
				.singleResult();

		BpmnModel bpmnModel = defaultProcessEngine//
				.getRepositoryService()//
				.getBpmnModel(processInstance.getProcessDefinitionId());

		List<String> activeActivityIds = processEngine.getRuntimeService()//
				.getActiveActivityIds(executionId);

		// 使用spring注入引擎请使用下面的这行代码
		// Context.setProcessEngineConfiguration(processEngine.getProcessEngineConfiguration());

		InputStream imageStream = ProcessDiagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds);

		// 将图片生成到D盘的目录下
		File file = new File("D:/" + "multtask.png");
		// 将输入流的图片写到D盘下
		FileUtils.copyInputStreamToFile(imageStream, file);
		System.out.println("multtaskProcessTest.findProcessPic()");
	}

	/** 查询流程定义 */
	@Test
	public void findProcessDefinition() {
		List<ProcessDefinition> list = processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
				.createProcessDefinitionQuery()// 创建一个流程定义的查询
				/** 指定查询条件,where条件 */
				// .deploymentId(deploymentId)//使用部署对象ID查询
				// .processDefinitionId(processDefinitionId)//使用流程定义ID查询
				// .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
				// .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

				/** 排序 */
				.orderByProcessDefinitionVersion().asc()// 按照版本的升序排列
				// .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

				/** 返回的结果集 */
				.list();// 返回一个集合列表，封装流程定义
		// .singleResult();//返回惟一结果集
		// .count();//返回结果集数量
		// .listPage(firstResult, maxResults);//分页查询
		if (list != null && list.size() > 0) {
			for (ProcessDefinition pd : list) {
				System.out.println("流程定义ID:" + pd.getId());// 流程定义的key+版本+随机生成数
				System.out.println("流程定义的名称:" + pd.getName());// 对应helloworld.bpmn文件中的name属性值
				System.out.println("流程定义的key:" + pd.getKey());// 对应helloworld.bpmn文件中的id属性值
				System.out.println("流程定义的版本:" + pd.getVersion());// 当流程定义的key值相同的相同下，版本升级，默认1
				System.out.println("资源名称bpmn文件:" + pd.getResourceName());
				System.out.println("资源名称png文件:" + pd.getDiagramResourceName());
				System.out.println("部署对象ID：" + pd.getDeploymentId());
				System.out
						.println("#########################################################");
			}
		}
	}

	/** 查询流程状态（判断流程正在执行，还是结束） */
	@Test
	public void isProcessEnd() {
		String processInstanceId = "801";
		ProcessInstance pi = processEngine.getRuntimeService()// 表示正在执行的流程实例和执行对象
				.createProcessInstanceQuery()// 创建流程实例查询
				.processInstanceId(processInstanceId)// 使用流程实例ID查询
				.singleResult();

		if (pi == null) {
			System.out.println("流程已经结束");
		} else {
			System.out.println("流程没有结束");
		}
	}

	/** 查询执行的所有的流程 */
	@Test
	public void findAllProcess() {
		List<ProcessInstance> list = processEngine.getRuntimeService()// 表示正在执行的流程实例和执行对象
				.createProcessInstanceQuery()// 创建流程实例查询
				.list();

		if (list != null && list.size() > 0) {
			for (ProcessInstance pi : list) {
				System.out.println("ID:" + pi.getId());
				System.out.println("ProcessInstanceId:"
						+ pi.getProcessInstanceId());
				System.out.println("ProcessInstanceId:"
						+ pi.getProcessDefinitionId());
				System.out
						.println("#########################################################");
			}
		}
	}

	/**
	 * 查看流程图
	 * 
	 * @throws IOException
	 */
	@Test
	public void viewPic() throws IOException {
		/** 将生成图片放到文件夹下 */
		String deploymentId = "3501";
		// 获取图片资源名称
		List<String> list = processEngine.getRepositoryService()//
				.getDeploymentResourceNames(deploymentId);
		// 定义图片资源的名称
		String resourceName = "";
		if (list != null && list.size() > 0) {
			for (String name : list) {
				if (name.indexOf(".png") >= 0) {
					resourceName = name;
				}
			}
		}

		// 获取图片的输入流
		InputStream in = processEngine.getRepositoryService()//
				.getResourceAsStream(deploymentId, resourceName);

		// 将图片生成到D盘的目录下
		File file = new File("D:/" + resourceName);
		// 将输入流的图片写到D盘下
		FileUtils.copyInputStreamToFile(in, file);
	}

	/** 获取流程变量 */
	@Test
	public void getVariables() {
		/** 与任务（正在执行） */
		TaskService taskService = processEngine.getTaskService();
		// 任务ID
		String taskId = "2708";
		String processInstanceId = "801";
		String executionId = "1506";
		String variableName = "nrOfInstances";

		// int nrOfInstances=(Integer)
		// processEngine.getRuntimeService().getVariable(executionId,
		// variableName);
		// processEngine.getRuntimeService() VariableInstanceEntity
		// System.out.println("数量：" + nrOfInstances);

		// processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessVariables();
		// processEngine.getRuntimeService().setVariable(executionId,
		// variableName, Integer.MAX_VALUE);

		List<Execution> execution = processEngine.getRuntimeService()
				.createExecutionQuery().processInstanceId(processInstanceId)
				.list();
		for (Execution e : execution) {
			ExecutionEntity en = (ExecutionEntity) e;
			// en.getVariableNames();
			// System.out.println( en.);
			// Map<String, Object> map =en.getVariables();
			// for (Entry<String, Object> m : map.entrySet()) {
			// System.out.println(m.getKey()+" ：" + m.getValue());
			// }
		}
		// String price = (String) taskService.getVariable(taskId, "price");
		// System.out.println("报价：" + price);
		System.out.println("MultTaskTest.getVariables()");
	}

	
	/**
	 * 加签
	 */
	@Test
	public void addUserTaskByMultiTask() {
		String processInstanceId = "1601";
		// 创建服务对象
		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		HistoryService historyService = processEngine.getHistoryService();
		ProcessEngineImpl pei=(ProcessEngineImpl)ProcessEngines.getDefaultProcessEngine();
		//String id=pei.getDbSqlSessionFactory().getIdGenerator().getNextId();
		
		TaskDto originTask = new TaskDto();

		// 验证任务实例
		List<Task> list = taskService.createTaskQuery()//
				.processInstanceId(processInstanceId)//
				.list();
		
		// 判断当前没有任务时
		if (list != null && list.size() > 0) {
			Task task = list.get(0);
			originTask.setExecutionId(task.getExecutionId());
			originTask.setName(task.getName());
			originTask.setProcessDefinitionId(task.getProcessDefinitionId());
			originTask.setProcessInstanceId(task.getProcessInstanceId());
			originTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
		} else {
			List<HistoricTaskInstance> hisList = historyService
					.createHistoricTaskInstanceQuery()//
					.processInstanceId(processInstanceId).list();
			HistoricTaskInstance task = hisList.get(0);
			originTask.setExecutionId(task.getExecutionId());
			originTask.setName(task.getName());
			originTask.setProcessDefinitionId(task.getProcessDefinitionId());
			originTask.setProcessInstanceId(task.getProcessInstanceId());
			originTask.setTaskDefinitionKey(task.getTaskDefinitionKey());
		}
		
		// 创建新任务
		Task newTask = taskService.newTask();
		newTask.setAssignee("gys8");//任务处理者
		newTask.setName(originTask.getName());
		taskService.saveTask(newTask);

		// 加签
		Connection conn = null;
		PreparedStatement pst = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, username, userpwd);
			String t_sql = "update act_ru_task art set EXECUTION_ID_ = ?, PROC_INST_ID_ = ?, PROC_DEF_ID_  = ?, TASK_DEF_KEY_ = ?, SUSPENSION_STATE_ = ? where ID_ = ?";
			pst = conn.prepareStatement(t_sql);
			pst.setString(1, originTask.getExecutionId());
			pst.setString(2, originTask.getProcessInstanceId());
			pst.setString(3, originTask.getProcessDefinitionId());
			pst.setString(4, originTask.getTaskDefinitionKey());
			pst.setString(5, "1");
			pst.setString(6, newTask.getId());
			pst.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (pst != null) {
			try {
				pst.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("MultTaskTest.test()---->加签成功");
	}
	
	
	/**
	 * 根据executeId查询任务 (处理流程图没有高亮显示问题)
	 */
	@Test
	public void getTaskByExecuteId(){
		String executionId="1611";
		List<Task> list = processEngine.getTaskService().createTaskQuery().executionId(executionId).list();
		
		//打印任务
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				System.out.println("任务ID:" + task.getId());
				System.out.println("任务名称:" + task.getName());
				System.out.println("任务的创建时间:" + task.getCreateTime());
				System.out.println("任务的办理人:" + task.getAssignee());
				System.out.println("流程实例ID：" + task.getProcessInstanceId());
				System.out.println("执行对象ID:" + task.getExecutionId());
				System.out.println("流程定义ID:" + task.getProcessDefinitionId());
				System.out.println("########################################################");
			}
		}
		
		//判断当前是否有执行的execute数据，如果有激活IS_ACTIVE_，能处理加签不能高亮显示跟踪任务流程问题
		if(list != null && list.size() > 0){
			Connection conn = null;
			PreparedStatement pst = null;
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(url, username, userpwd);
				String t_sql = "UPDATE act_ru_execution SET IS_ACTIVE_=1 WHERE ID_=?";
				pst = conn.prepareStatement(t_sql); 
				pst.setString(1, executionId);
				pst.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (pst != null) {
				try {
					pst.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("激活execute....");
		}
		
		
		System.out.println("MultTaskTest.gettaskByExecuteId()");
	}

}
