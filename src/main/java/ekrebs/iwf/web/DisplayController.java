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
import ekrebs.iwf.web.workflows.TestWorkflow.PersistanceDisp;
import ekrebs.iwf.web.workflows.TestWorkflow.SetPromiseValueInput;
import io.iworkflow.core.Client;
import io.iworkflow.core.persistence.Persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController()
@RequestMapping(value="/display")
public class DisplayController {
	private final ObjectMapper objectMapper;

	private final Client client;



	private ExecutorService executerService=Executors.newVirtualThreadPerTaskExecutor();

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
				var currentid="";
				
				for(var i=0;i<(1000*10)/10;i++){
					
					//var rpcstub=client.newRpcStub(TestWorkflow.class, workflowId);
	

				//var disp=client.invokeRPC(rpcstub::getDisplayState,displayId);

				var blah=client.getWorkflowDataAttributes(TestWorkflow.class, workflowId, List.of(PersistanceDisp.getKey(displayId)));
				

				
				var disp=(PersistanceDisp)blah.get(PersistanceDisp.getKey(displayId));
					if(!currentid.equals(disp.displayUpdateuuid())){
						currentid=disp.displayUpdateuuid();
					
					sse.send(SseEmitter.event().name("displaystatus").data(objectMapper.writeValueAsString(disp)).build());
					}
					
					Thread.sleep(10);
				}
				sse.send(SseEmitter.event().name("closedByServer").data("{}").build());
				
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
				"sendData":"/js/sendData.js",
				"htmlv1":"/js/htmlv1.js"
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



	@GetMapping("/listDisplays")
	public String listDisplays(@RequestParam String workflowId){
		
		var dataAtrs=client.getAllDataAttributes(TestWorkflow.class, workflowId);
		var listOfDisplaysAhrefs=new ArrayList<String>();
		for(var attr:dataAtrs.entrySet()){
			
			if(attr.getKey().startsWith(TestWorkflow.DISP_PER_PREFIX)){

				var displayId=attr.getKey().substring(TestWorkflow.DISP_PER_PREFIX.length()+1);
				listOfDisplaysAhrefs.add(STR."""
				<a href="/display/test2?workflowId=\{workflowId}&displayId=\{displayId}" target="_blank">\{displayId}</a>
				"""
						
				);
			}
		
		}
		
		return STR."""
				<!doctype html>
		<html>
		<head>
		



		
		

		</script>
		</head>
		<body>test
		
		\{String.join("<br>",listOfDisplaysAhrefs)}
		
		</body>
		</html>

		
				""";
	}
}
