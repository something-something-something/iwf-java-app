package ekrebs.iwf.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ekrebs.iwf.web.exec.Exec;
import ekrebs.iwf.web.workflows.TestWorkflow;
import io.iworkflow.core.Client;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController()
@RequestMapping(value="/")
public class Controller {
	private final ObjectMapper objectMapper;

	private final Client client;
	public Controller(ObjectMapper objectMapper, Client client){
		this.objectMapper = objectMapper;
		this.client = client;
		
	}
	@RequestMapping("/")
	public String something() throws JsonMappingException, JsonProcessingException {

		var j="""
		{
		"test":"a value"
		
		}
		
		""";
				

		var t=objectMapper.readTree(j);
		return  "hello world "+t.get("test").asText();
	}


	@GetMapping("/start")
	public String startForm() throws JsonMappingException, JsonProcessingException {
		//var uuid=UUID.randomUUID().toString();
		
		//client.startWorkflow(TestWorkflow.class, uuid, 0);
		var j="""
		{
		"test":"a value"
		
		}
		
		""";
				

		var t=objectMapper.readTree(j);
		return  """
				




		<!doctype html>
		<html>
		<head>
		<script type="module" src="/js/index.js"></script>
		</head>
		<body>test
		<start-form></start-form>
		</body>
		</html>
				""";
	}


	@PostMapping("/start")
	public Object start(@RequestBody JsonNode body) throws JsonMappingException, JsonProcessingException {
		var uuid=UUID.randomUUID().toString();
		
		// client.startWorkflow(TestWorkflow.class, uuid, 0);
		var j="""
		{
		"test":"a value"
		
		}
		
		""";
				

		var t=objectMapper.readTree(j);
		
		var z="hello world test"+uuid;

		var m=new HashMap<String,Object>();


		var parse=Exec.parse(body.path("instructions"));

		m.put("result",Exec.parse(body.path("instructions")));

		if(parse.errors().size()==0){
			client.startWorkflow(TestWorkflow.class, uuid, 0,parse);
			m.put("uuid",uuid);
		}
		
			
		

		
		return m;


	}
}
