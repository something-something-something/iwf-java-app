package ekrebs.iwf.web.config;

import ekrebs.iwf.web.workflows.TestWorkflow;
import io.iworkflow.core.Client;
import io.iworkflow.core.ClientOptions;
import io.iworkflow.core.JacksonJsonObjectEncoder;
import io.iworkflow.core.Registry;
import io.iworkflow.core.WorkerOptions;
import io.iworkflow.core.WorkerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class Config {

	@Bean
	public Registry registry() {
		var reg=new Registry();
		reg.addWorkflow(new TestWorkflow());
		return reg;

	}

	@Bean
	public WorkerService workerService(Registry registry) {
		return new WorkerService(registry, WorkerOptions.defaultOptions);
	}

	@Bean
	public Client client(Registry registry) {
		return new Client(registry, ClientOptions.builder().workerUrl("http://localhost:8080/worker")
				.serverUrl("http://iwf:8801/").objectEncoder(new JacksonJsonObjectEncoder()).build());
	}
}
