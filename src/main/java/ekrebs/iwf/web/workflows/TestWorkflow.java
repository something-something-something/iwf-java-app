package ekrebs.iwf.web.workflows;


import java.util.Arrays;
import java.util.List;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class TestWorkflow implements ObjectWorkflow{

	@Override
	public List<StateDef> getWorkflowStates() {
		
		return Arrays.asList(StateDef.startingState(new State()));
	}
	

	public class State implements WorkflowState<Object>{

		@Override
		public Class<Object> getInputType() {
			return Object.class;
		}

		@Override
		public StateDecision execute(Context context, Object input, CommandResults commandResults,
				Persistence persistence, Communication communication) {
					System.out.println("executing");
					return StateDecision.gracefulCompleteWorkflow();
		}

	}
}
