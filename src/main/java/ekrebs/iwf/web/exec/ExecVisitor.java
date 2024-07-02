package ekrebs.iwf.web.exec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import ekrebs.iwf.web.exec.Exec.Data.VariableAccess;
import ekrebs.iwf.web.exec.Exec.Instruction.Copy;
import ekrebs.iwf.web.exec.Exec.Instruction.Exit;
import ekrebs.iwf.web.exec.Exec.Instruction.Print;
import ekrebs.iwf.web.exec.ExecVisitor.VariableChanges.Update;
import ekrebs.iwf.web.workflows.TestWorkflow.PersistenceVar;
import io.iworkflow.core.persistence.Persistence;
import java.util.List;

public class ExecVisitor implements Exec.InstructionVisitor<ExecVisitor.ExecutionResult> {

	public record ExecutionContext(int execPointer, String SourceKey,String execEnv, Persistence persistence,ObjectMapper objectMapper) {

	}

	final ExecutionContext executionContext;

	public ExecVisitor(ExecutionContext executionContext) {
		this.executionContext = executionContext;

	}


	public sealed interface VariableChanges{
		record Update(String name,String scopeKey,JsonNode value) implements VariableChanges{};
		record Delete(String name,String scopeKey,JsonNode value) implements VariableChanges{};
		record Ref(String name,String scopeKey,String targetname, String targetscopeKey) implements VariableChanges{};
	} 

	public sealed interface ExecutionResult {
		List<State> states();
		List<VariableChanges> variableChanges();
		boolean shouldExit();

		

		public final class SimpleExecutionResult implements ExecutionResult {
			final ExecutionContext executionContext;
			final List<VariableChanges> variableChanges;

			final boolean shouldExit;
			
			public SimpleExecutionResult(ExecutionContext executionContext,boolean shouldExit) {
				this.executionContext = executionContext;
				this.variableChanges = List.of();
				this.shouldExit=shouldExit;
		
			}

			public SimpleExecutionResult(ExecutionContext executionContext,List<VariableChanges> variableChanges) {
				this.executionContext = executionContext;
				this.variableChanges = variableChanges;
				this.shouldExit=false;
		
			}

			public SimpleExecutionResult(ExecutionContext executionContext) {
				this.executionContext = executionContext;
				this.variableChanges = List.of();
				this.shouldExit=false;
		
			}


			@Override
			public List<State> states() {
				return List.of(new State(executionContext.execPointer()+1, executionContext.SourceKey(),executionContext.execEnv()));
			}

			@Override
			public boolean shouldExit() {
				return shouldExit;
			}

			@Override
			public List<VariableChanges> variableChanges() {return this.variableChanges;
			}


		}
	}

	public record State(int execPointer, String sourceKey,String execEnv) {
	
	};


	public static JsonNode getValue(Exec.Data value, Persistence persistence,String scopeKey){
		return switch(value){
			case Exec.Data.DirectValue dv->{yield dv.value();}
			case Exec.Data.VariableAccess va->{
				
				var variable= persistence.getDataAttribute( PersistenceVar.varKey(scopeKey,va.name()),PersistenceVar.class );

				var pathValue=getValue(va.path(),persistence,scopeKey);
				yield  Exec.getValueAtPath( variable.getValueFromPersistnace(persistence),pathValue);
				}
		};
	}

	public static JsonNode getFullValue(Exec.Data value, Persistence persistence,String scopeKey){
		return switch(value){
			case Exec.Data.DirectValue dv->{yield dv.value();}
			case Exec.Data.VariableAccess va->{
				
				var variable= persistence.getDataAttribute( PersistenceVar.varKey(scopeKey,va.name()),PersistenceVar.class );
				if(variable==null){
					yield  JsonNodeFactory.instance.objectNode();
				}
				else{
					yield  variable.getValueFromPersistnace(persistence);
				}
				
				}
		};
	}


	



	@Override
	public ExecutionResult vist(Copy inst) {

		var argValue=getValue(inst.arg(), this.executionContext.persistence,this.executionContext.execEnv );



		var variableAccess = inst.res();



		var variableChange = getVariableChange(argValue, variableAccess,executionContext);


		return new ExecutionResult.SimpleExecutionResult(executionContext,List.of(variableChange));
		
	}

	public static Update getVariableChange(JsonNode argValue, VariableAccess variableAccess,ExecutionContext executionContext) {


		var fullValueOfOldVariable = getFullValue(variableAccess,executionContext.persistence,executionContext.execEnv);
		var pathToReplace = getValue(variableAccess.path(), executionContext.persistence(), executionContext.execEnv());
		var newValue=Exec.replaceValueAtPath(fullValueOfOldVariable, argValue, pathToReplace,executionContext.objectMapper());
		var variableChange=new VariableChanges.Update( variableAccess.name(),executionContext.execEnv,newValue);
		return variableChange;
	}

	@Override
	public ExecutionResult vist(Exit inst) {
		return new  ExecutionResult.SimpleExecutionResult(executionContext,true);
	}

	@Override
	public ExecutionResult vist(Print inst) {

		System.out.println( getValue(inst.arg(), this.executionContext.persistence, this.executionContext.execEnv));
		return new  ExecutionResult.SimpleExecutionResult(executionContext);
	}

}
