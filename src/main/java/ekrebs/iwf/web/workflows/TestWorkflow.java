package ekrebs.iwf.web.workflows;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ekrebs.iwf.web.exec.Exec;
import ekrebs.iwf.web.exec.ExecVisitor;
import ekrebs.iwf.web.exec.Exec.ParseResult;
import ekrebs.iwf.web.exec.ExecVisitor.ExecutionResult;
import ekrebs.iwf.web.exec.ExecVisitor.VariableChanges;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map;



public class TestWorkflow implements ObjectWorkflow {
	public static final String VAR_PER_PREFIX = "VAR";
	public static final String SRC_PER_PREFIX = "SRC";


	@JsonTypeInfo(
		use=JsonTypeInfo.Id.NAME,
		include=JsonTypeInfo.As.PROPERTY,
		property = "type"
	)
	@JsonSubTypes(
		{
			@Type(value=PersistenceVar.Value.class,name="value"),
			@Type(value=PersistenceVar.Ref.class,name="ref")
		}
	)
	public sealed interface PersistenceVar {
		public static String varKey(String execEnv, String name) {
			return VAR_PER_PREFIX + ":" + execEnv + ":" + name;
		}

		JsonNode getValueFromPersistnace(Persistence persistence);

		Value getVariable(Persistence persistence);

		record Value(JsonNode value) implements PersistenceVar {

			@Override
			public JsonNode getValueFromPersistnace(Persistence persistence) {
				return this.value;
			}

			@Override
			public Value getVariable(Persistence persistence) {
				return this;
			}
		};

		record Ref(String name, String ExecEnv) implements PersistenceVar {

			@Override
			public JsonNode getValueFromPersistnace(Persistence persistence) {
				return persistence.getDataAttribute(
						varKey(this.ExecEnv, this.name), PersistenceVar.class)
						.getValueFromPersistnace(persistence);
			}

			@Override
			public Value getVariable(Persistence persistence) {
				return persistence.getDataAttribute(
						varKey(this.ExecEnv, this.name), PersistenceVar.class).getVariable(persistence);
			}
		};
	}

	record PersistenceSrc(List<Exec.Instruction> instructions,Map<String,Integer> labeledBookmarks) {

		public static String sourceKey(String name) {
			return SRC_PER_PREFIX + ":" + name;
		}

		public static Map<String,Integer> getLabledBookmarks(Persistence persistence,String name){
			return persistence.getDataAttribute(sourceKey(name),PersistenceSrc.class).labeledBookmarks;
		}
	};

	@Override
	public List<StateDef> getWorkflowStates() {

		return Arrays.asList(StateDef.startingState(new Start()), StateDef.nonStartingState(new State()));
	}

	@Override
	public List<PersistenceFieldDef> getPersistenceSchema() {
		return Arrays.asList(
				DataAttributeDef.createByPrefix(PersistenceVar.class, VAR_PER_PREFIX),
				DataAttributeDef.createByPrefix(PersistenceSrc.class, SRC_PER_PREFIX));
	}

	record CurrentExecContextInput(String src, int instructNum, String scope) {
	}

	public class Start implements WorkflowState<ParseResult> {

		@Override
		public Class<ParseResult> getInputType() {
			return ParseResult.class;
		}

		@Override
		public StateDecision execute(Context context, ParseResult input, CommandResults commandResults,
				Persistence persistence, Communication communication) {
			System.out.println("executing");
			var sourceuuid = UUID.randomUUID().toString();
			var scopeUUID = UUID.randomUUID().toString();
			persistence.setDataAttribute(PersistenceSrc.sourceKey(sourceuuid),  new PersistenceSrc( input.instructions(),input.labeledBookmarks() ));
			var currentExecContextInput = new CurrentExecContextInput(sourceuuid, 0, scopeUUID);

			return StateDecision.singleNextState(State.class, currentExecContextInput);
		}

	}

	public class State implements WorkflowState<CurrentExecContextInput> {

		@Override
		public Class<CurrentExecContextInput> getInputType() {
			return CurrentExecContextInput.class;
		}

		@Override
		public StateDecision execute(Context context, CurrentExecContextInput input, CommandResults commandResults,
				Persistence persistence, Communication communication) {
			var om = new ObjectMapper();
			var x = new ExecVisitor.ExecutionContext(input.instructNum, input.src, input.scope, persistence, om,PersistenceSrc.getLabledBookmarks(persistence, input.src));

			var visitor = new ExecVisitor(x);

			var result = persistence.getDataAttribute(PersistenceSrc.sourceKey(input.src),
					PersistenceSrc.class).instructions.get(input.instructNum).accept(visitor);

			switch (result) {
				case ExecutionResult.SimpleExecutionResult ser -> {
					if (ser.shouldExit()) {
						return StateDecision.gracefulCompleteWorkflow();
					}
					var vc=ser.variableChanges();
					for (VariableChanges vch : vc) {
						
						switch (vch) {
							case ExecVisitor.VariableChanges.Update u -> {
								System.out.println(u.name());
								persistence.setDataAttribute(
										PersistenceVar.varKey(u.scopeKey(), u.name())
										
										
										
										, new PersistenceVar.Value(u.value()));

							}
							default -> {
								System.out.println("not implimented yet");
							}

						}

					}
					var nextStates = new ArrayList<StateMovement>();
					var s=ser.states();
					for (var z :s) {

						var movment = StateMovement.create(State.class, new CurrentExecContextInput(
								z.sourceKey(), z.execPointer(), z.execEnv()));
						nextStates.add(movment);
					}
					return StateDecision.multiNextStates(nextStates);

				}

			}

		}

	}
}
