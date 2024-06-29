package ekrebs.iwf.web.iwfInternals;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.iworkflow.core.WorkerService;
import io.iworkflow.gen.models.WorkflowStateExecuteRequest;
import io.iworkflow.gen.models.WorkflowStateExecuteResponse;
import io.iworkflow.gen.models.WorkflowStateWaitUntilRequest;
import io.iworkflow.gen.models.WorkflowStateWaitUntilResponse;
import io.iworkflow.gen.models.WorkflowWorkerRpcRequest;
import io.iworkflow.gen.models.WorkflowWorkerRpcResponse;
import static io.iworkflow.core.WorkerService.WORKFLOW_STATE_EXECUTE_API_PATH;
import static io.iworkflow.core.WorkerService.WORKFLOW_STATE_WAIT_UNTIL_API_PATH;
import static io.iworkflow.core.WorkerService.WORKFLOW_WORKER_RPC_API_PATH;

@Controller
@RequestMapping("/worker")
public class IwfController {
	 private final WorkerService workerService;

    public IwfController(final WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping(WORKFLOW_STATE_WAIT_UNTIL_API_PATH)
    public ResponseEntity<WorkflowStateWaitUntilResponse> handleWorkflowStateWaitUntil(
            final @RequestBody WorkflowStateWaitUntilRequest request
    ) {
        WorkflowStateWaitUntilResponse body = workerService.handleWorkflowStateWaitUntil(request);
        return ResponseEntity.ok(body);
    }

    @PostMapping(WORKFLOW_STATE_EXECUTE_API_PATH)
    public ResponseEntity<WorkflowStateExecuteResponse> apiV1WorkflowStateDecidePost(
            final @RequestBody WorkflowStateExecuteRequest request
    ) {
        return ResponseEntity.ok(workerService.handleWorkflowStateExecute(request));
    }

    @PostMapping(WORKFLOW_WORKER_RPC_API_PATH)
    public ResponseEntity<WorkflowWorkerRpcResponse> apiV1WorkflowStateDecidePost(
            final @RequestBody WorkflowWorkerRpcRequest request
    ) {
        return ResponseEntity.ok(workerService.handleWorkflowWorkerRpc(request));
    }

}
