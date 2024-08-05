package ekrebs.iwf.web.exec;


import com.fasterxml.jackson.databind.ObjectMapper;

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
import ekrebs.iwf.web.exec.Exec.Instruction.Parallel;
import ekrebs.iwf.web.exec.Exec.Instruction.DeadEnd;
import ekrebs.iwf.web.exec.Exec.Instruction.ResPromise;
import ekrebs.iwf.web.workflows.TestWorkflow.InternalCommsPromise;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.communication.InternalChannelCommand;
import io.iworkflow.core.persistence.Persistence;
import java.util.Map;


public class SuspendedExecutionVisitor implements Exec.InstructionVisitor<CommandRequest> { 

	public record  SuspendedExecutionContext(int execPointer, String SourceKey,String execEnv, Persistence persistence,ObjectMapper objectMapper,Map<String,Integer> labeledBookmarks) {

	}
	private final SuspendedExecutionContext executionContext;

	public SuspendedExecutionVisitor(SuspendedExecutionContext executionContext){
		this.executionContext=executionContext;
	}

	CommandRequest defaulCommandRequest(){
		return CommandRequest.empty;
	}
	@Override
	public CommandRequest vist(Copy inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Exit inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Print inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Label inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Branch inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(MakePromise inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(AwaitPromise inst) {

		var argValue=ExecVisitor.getValue(inst.arg(), this.executionContext.persistence,this.executionContext.execEnv );
		

		return CommandRequest.forAllCommandCompleted(InternalChannelCommand.create(InternalCommsPromise.getKey(argValue.asText())));
		
	}
	@Override
	public CommandRequest vist(MakeDisplay inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(UpdateDisplay inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Add inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Minus inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Eq inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Gt inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Lt inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(And inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Or inst) {
		return defaulCommandRequest();
	}

	@Override
	public CommandRequest vist(DeadEnd inst) {
		return defaulCommandRequest();
	}
	@Override
	public CommandRequest vist(Parallel inst) {
		return defaulCommandRequest();
	}

	@Override
	public CommandRequest vist(ResPromise inst) {
		return defaulCommandRequest();
	}

}