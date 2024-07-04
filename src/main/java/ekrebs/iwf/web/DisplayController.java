package ekrebs.iwf.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.ServerResponse.SseBuilder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ekrebs.iwf.web.exec.Exec;
import ekrebs.iwf.web.workflows.TestWorkflow;
import ekrebs.iwf.web.workflows.TestWorkflow.SetPromiseValueInput;
import io.iworkflow.core.Client;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController()
@RequestMapping(value="/display")
public class DisplayController {
	private final ObjectMapper objectMapper;

	private final Client client;



	private ExecutorService executerService=Executors.newCachedThreadPool();

	public DisplayController(ObjectMapper objectMapper, Client client){
		this.objectMapper = objectMapper;
		this.client = client;
		
	}


	record DisplaySubmit(String displayId,JsonNode data,String workflowId){}
	@PostMapping("/submit")
	public Object start(@RequestBody DisplaySubmit body) throws JsonMappingException, JsonProcessingException {


		var m=new HashMap<String,Object>();


	
		var rpcstub=client.newRpcStub(TestWorkflow.class, body.workflowId);
	

		var disp=client.invokeRPC(rpcstub::getDisplayState,body.displayId);
		System.out.println(disp);
		System.out.println(body.data());
		client.invokeRPC(rpcstub::setPromiseValue,new SetPromiseValueInput(disp.promiseIdForInput(),body.data()));

		
		return m;


	}

	@GetMapping("/test")
	public SseEmitter displaySSE(@RequestParam String workflowId,String displayId){
		SseEmitter sse=new SseEmitter();

		executerService.execute(()->{
			try {
				for (var i=0;i<10;i++){

					var rpcstub=client.newRpcStub(TestWorkflow.class, workflowId);
	

				var disp=client.invokeRPC(rpcstub::getDisplayState,displayId);
					sse.send(SseEmitter.event().name("displaystatus").data(objectMapper.writeValueAsString(disp)).build());
					
					Thread.sleep(1000);
				}
				
				sse.complete();
				
			} catch (Exception e) {
				sse.complete();
			}

		});
		return sse;
	}

	@GetMapping("/test2")
	public String test2(){
		
		return """
				<!doctype html>
		<html>
		<head>
		<script type="importmap">
		{
			"imports":{
				"sendData":"/js/sendData.js"
			}
		}
		
		</script>



		<script type="module" src="/js/render.js">
		
		

		</script>
		</head>
		<body>test
		<get-display></get-display>

		
		</body>
		</html>

		
				""";
	}
}
