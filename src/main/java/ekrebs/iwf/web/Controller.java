package ekrebs.iwf.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ekrebs.iwf.web.workflows.TestWorkflow;
import io.iworkflow.core.Client;
@RestController
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
		return  "hello world test"+t.get("test").asText();
	}

	@RequestMapping("/start")
	public String start() throws JsonMappingException, JsonProcessingException {
		var uuid=UUID.randomUUID().toString();
		
		client.startWorkflow(TestWorkflow.class, uuid, 0);
		var j="""
		{
		"test":"a value"
		
		}
		
		""";
				

		var t=objectMapper.readTree(j);
		return  "hello world test"+uuid;
	}
}
