package cn.hfzs.compete;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

@SuppressWarnings("serial")
public class CompletTaskListenerImpl implements TaskListener {

	/**用来指定任务的办理人*/
	public void notify(DelegateTask delegateTask) {
		delegateTask.setVariable("iscompete", true);

		System.out.println("TaskListenerImpl.notify()--->会签执行。。");
	}
}
