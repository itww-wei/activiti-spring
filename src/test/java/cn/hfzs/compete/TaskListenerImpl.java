package cn.hfzs.compete;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

@SuppressWarnings("serial")
public class TaskListenerImpl implements TaskListener {

	/**用来指定任务的办理人*/
	public void notify(DelegateTask delegateTask) {
		//指定个人任务的办理人，也可以指定组任务的办理人
		//个人任务：通过类去查询数据库，将下一个任务的办理人查询获取，然后通过setAssignee()的方法指定任务的办理人
		//delegateTask.setAssignee("灭绝师太");
		//组任务：
		//delegateTask.addCandidateUser("郭靖");
		//delegateTask.addCandidateUser("黄蓉");
		//delegateTask.addCandidateGroup("");
		
		String eventName=delegateTask.getEventName();
		if(EVENTNAME_CREATE.equals(eventName)){//任务创建时 create
			System.out.println("任务创建时 create");
			List<String> assigneeList=new ArrayList<String>();
			assigneeList.add("gys1");
			assigneeList.add("gys2");
			//assigneeList.add("gys3");
			//assigneeList.add("gys4");
			delegateTask.setVariable("assigneeList", assigneeList);
			
			//设置是否接受多实例任务
			delegateTask.setVariable("iscompete", false);

			// 修改多任务参数
			delegateTask.setVariable("nrOfInstances", Long.MAX_VALUE);
		}
		
		if(EVENTNAME_COMPLETE.equals(eventName)){//完成任务时complete
			int approve=Integer.valueOf((String) delegateTask.getVariable("approve"));//是否审核通过
			if (approve==1) {//通过设置boundarytimer定时器执行时间
				delegateTask.setVariable("startbidtime", "PT50S");
			}
			System.out.println("完成任务时 complete");
		}
		System.out.println("TaskListenerImpl.notify()--->设置会签");
	}

}
