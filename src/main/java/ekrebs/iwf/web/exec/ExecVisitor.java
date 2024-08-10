package ekrebs.iwf.web.exec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import ekrebs.iwf.web.exec.Exec.Data.VariableAccess;
import ekrebs.iwf.web.exec.Exec.Instruction.Add;
import ekrebs.iwf.web.exec.Exec.Instruction.And;
import ekrebs.iwf.web.exec.Exec.Instruction.AwaitPromise;
import ekrebs.iwf.web.exec.Exec.Instruction.Branch;
import ekrebs.iwf.web.exec.Exec.Instruction.Copy;
import ekrebs.iwf.web.exec.Exec.Instruction.Eq;
import ekrebs.iwf.web.exec.Exec.Instruction.Exit;
import ekrebs.iwf.web.exec.Exec.Instruction.Gt;
import ekrebs.iwf.web.exec.Exec.Instruction.Label;
import ekrebs.iwf.web.exec.Exec.Instruction.Lt;
import ekrebs.iwf.web.exec.Exec.Instruction.MakeDisplay;
import ekrebs.iwf.web.exec.Exec.Instruction.MakePromise;
import ekrebs.iwf.web.exec.Exec.Instruction.Minus;
import ekrebs.iwf.web.exec.Exec.Instruction.Or;
import ekrebs.iwf.web.exec.Exec.Instruction.Print;
import ekrebs.iwf.web.exec.Exec.Instruction.UpdateDisplay;
import ekrebs.iwf.web.exec.Exec.Instruction.DeadEnd;
import ekrebs.iwf.web.exec.Exec.Instruction.Parallel;
import ekrebs.iwf.web.exec.Exec.Instruction.ResPromise;
import ekrebs.iwf.web.exec.ExecVisitor.VariableChanges.Update;
import ekrebs.iwf.web.workflows.TestWorkflow.PersistenceVar;
import ekrebs.iwf.web.workflows.TestWorkflow.Display;
import ekrebs.iwf.web.workflows.TestWorkflow.InternalCommsPromise;
import ekrebs.iwf.web.workflows.TestWorkflow.PersistanceDisp;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

public class ExecVisitor implements Exec.InstructionVisitor<ExecVisitor.ExecutionResult> {

	public record ExecutionContext(int execPointer, String SourceKey, String execEnv, Persistence persistence,
			ObjectMapper objectMapper, Map<String, Integer> labeledBookmarks, CommandResults commandResults,
			Communication communication) {

	}

	final ExecutionContext executionContext;

	public ExecVisitor(ExecutionContext executionContext) {
		this.executionContext = executionContext;

	}

	public sealed interface VariableChanges {
		record Update(String name, String scopeKey, JsonNode value) implements VariableChanges {
		};

		record Delete(String name, String scopeKey, JsonNode value) implements VariableChanges {
		};

		record Ref(String name, String scopeKey, String targetname, String targetscopeKey)
				implements VariableChanges {
		};
	}

	public record PromiseWrite(String id, JsonNode value){}

	public sealed interface ExecutionResult {
		List<State> states();

		List<VariableChanges> variableChanges();

		boolean shouldExit();
		List<PromiseWrite> promiseWrites();

		public final class SimpleExecutionResult implements ExecutionResult {
			private final ExecutionContext executionContext;
			private final List<VariableChanges> variableChanges;
			private final List<State> states;
			private List<PromiseWrite> promiseWrites;
			final boolean shouldExit;

			public SimpleExecutionResult(ExecutionContext executionContext, boolean shouldExit) {
				this.executionContext = executionContext;
				this.variableChanges = List.of();
				this.shouldExit = shouldExit;
				this.states = defaultNextState(executionContext);
				this.promiseWrites=List.of();

			}

			public SimpleExecutionResult(ExecutionContext executionContext,
					List<VariableChanges> variableChanges) {
				this.executionContext = executionContext;
				this.variableChanges = variableChanges;
				this.shouldExit = false;
				this.states = defaultNextState(executionContext);
				this.promiseWrites=List.of();

			}

			public SimpleExecutionResult(ExecutionContext executionContext, List<VariableChanges> variableChanges,
					List<State> states) {
				this.executionContext = executionContext;
				this.variableChanges = variableChanges;
				this.shouldExit = false;
				this.states = states;
				this.promiseWrites=List.of();

			}
			public SimpleExecutionResult(ExecutionContext executionContext, List<VariableChanges> variableChanges,
					List<State> states,List<PromiseWrite> promiseWrites) {
				this.executionContext = executionContext;
				this.variableChanges = variableChanges;
				this.shouldExit = false;
				this.states = states;
				this.promiseWrites=promiseWrites;

			}

			public SimpleExecutionResult(ExecutionContext executionContext) {
				this.executionContext = executionContext;
				this.variableChanges = List.of();
				this.shouldExit = false;
				this.states = defaultNextState(executionContext);
				this.promiseWrites=List.of();

			}

			@Override
			public List<State> states() {
				return this.states;
			}

			public static List<State> defaultNextState(ExecutionContext executionContext) {
				return List.of(new State(executionContext.execPointer() + 1, executionContext.SourceKey(),
						executionContext.execEnv()));
			}

			@Override
			public boolean shouldExit() {
				return shouldExit;
			}

			@Override
			public List<VariableChanges> variableChanges() {
				return this.variableChanges;
			}

			@Override
			public List<PromiseWrite> promiseWrites() {
				return this.promiseWrites;
			}

		}
	}

	public record State(int execPointer, String sourceKey, String execEnv) {

	};
//
	public static JsonNode getValue(Exec.Data value, Persistence persistence, String scopeKey) {
		return switch (value) {
			case Exec.Data.DirectValue dv -> {
				yield dv.value();
			}
			case Exec.Data.VariableAccess va -> {

				var variable = persistence.getDataAttribute(PersistenceVar.varKey(scopeKey, va.name()),
						PersistenceVar.class);

				var pathValue = getValue(va.path(), persistence, scopeKey);
				yield Exec.getValueAtPath(variable.getValueFromPersistnace(persistence), pathValue);
			}
		};
	}

	public static JsonNode getFullValue(Exec.Data value, Persistence persistence, String scopeKey) {
		return switch (value) {
			case Exec.Data.DirectValue dv -> {
				yield dv.value();
			}
			case Exec.Data.VariableAccess va -> {

				var variable = persistence.getDataAttribute(PersistenceVar.varKey(scopeKey, va.name()),
						PersistenceVar.class);
				if (variable == null) {
					yield JsonNodeFactory.instance.objectNode();
				} else {
					yield variable.getValueFromPersistnace(persistence);
				}

			}
		};
	}

	@Override
	public ExecutionResult vist(Copy inst) {

		var argValue = getValue(inst.arg(), this.executionContext.persistence, this.executionContext.execEnv);

		var variableAccess = inst.res();

		var variableChange = getVariableChange(argValue, variableAccess, executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));

	}

	public static Update getVariableChange(JsonNode argValue, VariableAccess variableAccess,
			ExecutionContext executionContext) {

		var fullValueOfOldVariable = getFullValue(variableAccess, executionContext.persistence,
				executionContext.execEnv);
		var pathToReplace = getValue(variableAccess.path(), executionContext.persistence(),
				executionContext.execEnv());
		var newValue = Exec.replaceValueAtPath(fullValueOfOldVariable, argValue, pathToReplace,
				executionContext.objectMapper());

		var persistenceOriginValueLocation=PersistenceVar.getPersistenceOriginValueLocation(executionContext.persistence, executionContext.execEnv, variableAccess.name());
		var variableChange = new VariableChanges.Update(persistenceOriginValueLocation.name(), persistenceOriginValueLocation.execEnv(), newValue);
		return variableChange;
	}

	@Override
	public ExecutionResult vist(Exit inst) {
		return new ExecutionResult.SimpleExecutionResult(executionContext, true);
	}

	@Override
	public ExecutionResult vist(Print inst) {

		System.out.println(getValue(inst.arg(), this.executionContext.persistence, this.executionContext.execEnv));
		return new ExecutionResult.SimpleExecutionResult(executionContext);
	}

	@Override
	public ExecutionResult vist(Label inst) {
		return new ExecutionResult.SimpleExecutionResult(executionContext);
	}

	@Override
	public ExecutionResult vist(Branch inst) {

		var argValue = getValue(inst.arg(), this.executionContext.persistence, this.executionContext.execEnv);

		var state = new State(executionContext.labeledBookmarks.get(inst.label()), executionContext.SourceKey,
				executionContext.execEnv);
		if (argValue.asBoolean()) {
			return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(), List.of(state));
		}
		return new ExecutionResult.SimpleExecutionResult(executionContext);

	}

	@Override
	public ExecutionResult vist(MakePromise inst) {
		var promiseid = InternalCommsPromise.createPromise();
		var variableAccess = inst.res();
		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(promiseid), variableAccess,
				executionContext);
		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));
	}

	@Override
	public ExecutionResult vist(AwaitPromise inst) {

		var promiseValue = (InternalCommsPromise)
				executionContext.commandResults.getAllInternalChannelCommandResult().get(0).getValue().get();

		var variableAccess = inst.res();

		var variableChange = getVariableChange(promiseValue.value(), variableAccess, executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));

	}

	@Override
	public ExecutionResult vist(MakeDisplay inst) {

		var variableAccess = inst.res();

		var dispsetup = PersistanceDisp.setup(this.executionContext.persistence);

		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(dispsetup.uuid()),
				variableAccess, executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));

	}

	@Override
	public ExecutionResult vist(UpdateDisplay inst) {
		var inputPromiseArg = getValue(inst.inputPromiseArg(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var dispArg = getValue(inst.dispArg(), this.executionContext.persistence, this.executionContext.execEnv);

		var dispIdArg = getValue(inst.dispIdArg(), this.executionContext.persistence,
				this.executionContext.execEnv);

		try {
			var display = executionContext.objectMapper().treeToValue(dispArg, Display.class);

			PersistanceDisp.update(dispIdArg.asText(), inputPromiseArg.asText(), display,
					executionContext.persistence());

		} catch (JsonProcessingException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new ExecutionResult.SimpleExecutionResult(executionContext);
	}

	@Override
	public ExecutionResult vist(Add inst) {
		var arg1 = getValue(inst.arg1(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var arg2 = getValue(inst.arg2(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var variableAccess = inst.res();

		var result = arg1.asDouble() + arg2.asDouble();
		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(result), variableAccess,
				executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));

	}

	@Override
	public ExecutionResult vist(Minus inst) {
		var arg1 = getValue(inst.arg1(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var arg2 = getValue(inst.arg2(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var variableAccess = inst.res();

		var result = arg1.asDouble() - arg2.asDouble();
		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(result), variableAccess,
				executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));
	}

	@Override
	public ExecutionResult vist(Eq inst) {
		var arg1 = getValue(inst.arg1(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var arg2 = getValue(inst.arg2(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var variableAccess = inst.res();
		boolean result;
		if(arg1.isNumber()&&arg2.isNumber()){
			result=arg1.asDouble()==arg2.asDouble();
		}
		else{
			 result = arg1.equals(arg2);
		}
		




		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(result), variableAccess,
				executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));
	}

	@Override
	public ExecutionResult vist(Gt inst) {
		var arg1 = getValue(inst.arg1(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var arg2 = getValue(inst.arg2(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var variableAccess = inst.res();

		var result = arg1.asDouble() > arg2.asDouble();
		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(result), variableAccess,
				executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));
	}

	@Override
	public ExecutionResult vist(Lt inst) {
		var arg1 = getValue(inst.arg1(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var arg2 = getValue(inst.arg2(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var variableAccess = inst.res();

		var result = arg1.asDouble() < arg2.asDouble();
		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(result), variableAccess,
				executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));
	}

	@Override
	public ExecutionResult vist(And inst) {
		var arg1 = getValue(inst.arg1(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var arg2 = getValue(inst.arg2(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var variableAccess = inst.res();

		var result = arg1.asBoolean()&&arg2.asBoolean();
		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(result), variableAccess,
				executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));
	}

	@Override
	public ExecutionResult vist(Or inst) {
		var arg1 = getValue(inst.arg1(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var arg2 = getValue(inst.arg2(), this.executionContext.persistence,
				this.executionContext.execEnv);

		var variableAccess = inst.res();

		var result = arg1.asBoolean()||arg2.asBoolean();
		var variableChange = getVariableChange(executionContext.objectMapper.valueToTree(result), variableAccess,
				executionContext);

		return new ExecutionResult.SimpleExecutionResult(executionContext, List.of(variableChange));
	}

	@Override
	public ExecutionResult vist(DeadEnd inst) {
		return new ExecutionResult.SimpleExecutionResult(executionContext,List.of(),List.of());
	}
	@Override
	public ExecutionResult vist(Parallel inst) {
		var altScope=UUID.randomUUID().toString();
		var variableChanges=new ArrayList<VariableChanges>();
		for(var copyItem:inst.copy()){
			var valueToCopy=getValue(copyItem.arg(), this.executionContext.persistence,
		this.executionContext.execEnv);
			variableChanges.add(new VariableChanges.Update(copyItem.res(), altScope, valueToCopy));
		}
		for (var refItem:inst.ref()){
			variableChanges.add(new VariableChanges.Ref(refItem.refName(), altScope, refItem.targetName(), executionContext.execEnv));
		}
		var states=new ArrayList<State>();
		states.addAll(ExecutionResult.SimpleExecutionResult.defaultNextState(executionContext));
		states.add(new State(executionContext.labeledBookmarks.get(inst.label()), executionContext.SourceKey, altScope));
		return new ExecutionResult.SimpleExecutionResult(executionContext,variableChanges,states);

	}
	@Override
	public ExecutionResult vist(ResPromise inst) {
		var promiseId = getValue(inst.promiseIdArg(), this.executionContext.persistence,
		this.executionContext.execEnv);

		var value = getValue(inst.valueArg(), this.executionContext.persistence,
		this.executionContext.execEnv);
		return new ExecutionResult.SimpleExecutionResult(executionContext,List.of(),ExecutionResult.SimpleExecutionResult.defaultNextState(executionContext),List.of(new PromiseWrite(promiseId.asText(), value)));
		
	}
}
