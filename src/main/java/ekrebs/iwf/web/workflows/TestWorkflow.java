package ekrebs.iwf.web.workflows;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ekrebs.iwf.web.exec.Exec;
import ekrebs.iwf.web.exec.ExecVisitor;
import ekrebs.iwf.web.exec.SuspendedExecutionVisitor;
import ekrebs.iwf.web.exec.Exec.ParseResult;
import ekrebs.iwf.web.exec.ExecVisitor.ExecutionResult;
import ekrebs.iwf.web.exec.ExecVisitor.VariableChanges;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.InternalChannelCommand;
import io.iworkflow.core.communication.InternalChannelDef;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class TestWorkflow implements ObjectWorkflow {
	public static final String VAR_PER_PREFIX = "VAR";
	public static final String SRC_PER_PREFIX = "SRC";
	public static final String DISP_PER_PREFIX = "DISP";
	public static final String PROMISE_IC_PREFIX = "PROMISE";

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@Type(value = PersistenceVar.Value.class, name = "value"),
			@Type(value = PersistenceVar.Ref.class, name = "ref")
	})
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

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({
			@Type(value = Display.CustomElement.class, name = "customElement")

	})
	public sealed interface Display {
		record CustomElement(String elementName, String module, Map<String, String> attributes,
				Map<String, JsonNode> properties) implements Display {
		}
	}

	public record PersistanceDisp(String promiseIdForInput, Display display, String displayUpdateuuid) {

		public static String getKey(String id) {
			return DISP_PER_PREFIX + ":" + id;
		}

		public record PersistanceDispSetup(String uuid, PersistanceDisp persistanceDisp) {
		};

		public static PersistanceDispSetup setup(Persistence persistence) {
			var setup=new PersistanceDispSetup(UUID.randomUUID().toString(),
					new PersistanceDisp(InternalCommsPromise.createPromise(), new Display.CustomElement("div",
							"/indev.js", new HashMap<String, String>(), new HashMap<String, JsonNode>()),
							UUID.randomUUID().toString()));
							persistence.setDataAttribute(getKey(setup.uuid),setup.persistanceDisp);
			return setup;
		}

		public static void update(String displayId,String promiseinputid,Display displayvalue,Persistence persistence) {
			persistence.setDataAttribute(getKey(displayId),new PersistanceDisp(promiseinputid,displayvalue,UUID.randomUUID().toString()));
			
		}

	};

	@RPC
	public PersistanceDisp getDisplayState(Context context, String input, Persistence persistence,
			Communication communication) {
		return persistence.getDataAttribute(PersistanceDisp.getKey(input), PersistanceDisp.class);

	}

	public record SetPromiseValueInput(String promiseId, JsonNode data) {
	};

	@RPC
	public void setPromiseValue(Context context, SetPromiseValueInput input, Persistence persistence,
			Communication communication) {
				System.out.println(input);
		communication.publishInternalChannel(InternalCommsPromise.getKey(input.promiseId), new InternalCommsPromise(input.data));

	}

	record PersistenceSrc(List<Exec.Instruction> instructions, Map<String, Integer> labeledBookmarks) {

		public static String sourceKey(String name) {
			return SRC_PER_PREFIX + ":" + name;
		}

		public static Map<String, Integer> getLabledBookmarks(Persistence persistence, String name) {
			return persistence.getDataAttribute(sourceKey(name), PersistenceSrc.class).labeledBookmarks;
		}
	};

	@Override
	public List<StateDef> getWorkflowStates() {

		return Arrays.asList(StateDef.startingState(new Start()), StateDef.nonStartingState(new InstructionState()),
				StateDef.nonStartingState(new PromiseAwiterState()));
	}

	@Override
	public List<PersistenceFieldDef> getPersistenceSchema() {
		return Arrays.asList(
				DataAttributeDef.createByPrefix(PersistenceVar.class, VAR_PER_PREFIX),
				DataAttributeDef.createByPrefix(PersistenceSrc.class, SRC_PER_PREFIX),
				DataAttributeDef.createByPrefix(PersistanceDisp.class, DISP_PER_PREFIX));
	}

	public record InternalCommsPromise(JsonNode value) {
		public static String getKey(String id) {
			return PROMISE_IC_PREFIX + ":" + id;
		}

		public static String createPromise() {
			var uuid = UUID.randomUUID().toString();
			return uuid;
		}
	};

	@Override
	public List<CommunicationMethodDef> getCommunicationSchema() {
		return Arrays.asList(InternalChannelDef.createByPrefix(InternalCommsPromise.class, PROMISE_IC_PREFIX));
	}

	record PromiseAwiterStateInput(String promiseid) {
	};

	public class PromiseAwiterState implements WorkflowState<PromiseAwiterStateInput> {

		@Override
		public Class<PromiseAwiterStateInput> getInputType() {

			return PromiseAwiterStateInput.class;
		}

		@Override
		public CommandRequest waitUntil(Context context, PromiseAwiterStateInput input, Persistence persistence,
				Communication communication) {
			return CommandRequest.forAllCommandCompleted(
					InternalChannelCommand.create(InternalCommsPromise.getKey(input.promiseid())));
		}

		@Override
		public StateDecision execute(Context context, PromiseAwiterStateInput input, CommandResults commandResults,
				Persistence persistence, Communication communication) {
			return StateDecision.deadEnd();
		}

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
			persistence.setDataAttribute(PersistenceSrc.sourceKey(sourceuuid),
					new PersistenceSrc(input.instructions(), input.labeledBookmarks()));
			var currentExecContextInput = new CurrentExecContextInput(sourceuuid, 0, scopeUUID);

			return StateDecision.singleNextState(InstructionState.class, currentExecContextInput);
		}

	}

	record CurrentExecContextInput(String src, int instructNum, String scope) {
	}

	public class InstructionState implements WorkflowState<CurrentExecContextInput> {

		@Override
		public Class<CurrentExecContextInput> getInputType() {
			return CurrentExecContextInput.class;
		}

		@Override
		public CommandRequest waitUntil(Context context, CurrentExecContextInput input, Persistence persistence,
				Communication communication) {
			var om = new ObjectMapper();
			var execContext = new SuspendedExecutionVisitor.SuspendedExecutionContext(input.instructNum,
					input.src, input.scope, persistence, om,
					PersistenceSrc.getLabledBookmarks(persistence, input.src));

			var visitor = new SuspendedExecutionVisitor(execContext);

			var result = persistence.getDataAttribute(PersistenceSrc.sourceKey(input.src),
					PersistenceSrc.class).instructions.get(input.instructNum).accept(visitor);
			return result;
		}

		@Override
		public StateDecision execute(Context context, CurrentExecContextInput input, CommandResults commandResults,
				Persistence persistence, Communication communication) {
			var om = new ObjectMapper();
			var x = new ExecVisitor.ExecutionContext(input.instructNum, input.src, input.scope, persistence, om,
					PersistenceSrc.getLabledBookmarks(persistence, input.src), commandResults, communication);

			var visitor = new ExecVisitor(x);

			var result = persistence.getDataAttribute(PersistenceSrc.sourceKey(input.src),
					PersistenceSrc.class).instructions.get(input.instructNum).accept(visitor);

			//switch (result) {
				//case ExecutionResult.SimpleExecutionResult ser -> {
				var ser=result;
					if (ser.shouldExit()) {
						return StateDecision.gracefulCompleteWorkflow();
					}
					var vc = ser.variableChanges();
					for (VariableChanges vch : vc) {

						switch (vch) {
							case ExecVisitor.VariableChanges.Update u -> {
								//System.out.println(u.name());
								persistence.setDataAttribute(
										PersistenceVar.varKey(u.scopeKey(), u.name())

										, new PersistenceVar.Value(u.value()));

							}
							case ExecVisitor.VariableChanges.Ref r->{
								persistence.setDataAttribute(PersistenceVar.varKey(r.scopeKey(), r.name()), new PersistenceVar.Ref(r.targetname(),r.targetscopeKey()));
							}
							default -> {
								System.out.println("not implimented yet");
							}

						}

					}
					for(var promiseWrite: ser.promiseWrites()){
						communication.publishInternalChannel(InternalCommsPromise.getKey(promiseWrite.id()), new InternalCommsPromise(promiseWrite.value()));
						//System.out.println(promiseWrite.value());

					}

					var nextStates = new ArrayList<StateMovement>();
					var s = ser.states();
					for (var z : s) {

						var movment = StateMovement.create(InstructionState.class,
								new CurrentExecContextInput(
										z.sourceKey(), z.execPointer(), z.execEnv()));
						nextStates.add(movment);
					}
					if(nextStates.size()==0){
						return StateDecision.deadEnd();
					}
					return StateDecision.multiNextStates(nextStates);

				//}

			//}

		}

	}

}
