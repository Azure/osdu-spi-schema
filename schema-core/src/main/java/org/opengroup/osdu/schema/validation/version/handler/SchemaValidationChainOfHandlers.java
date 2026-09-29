package org.opengroup.osdu.schema.validation.version.handler;

import java.util.Collections;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;

@Service
public class SchemaValidationChainOfHandlers {


	@Autowired
	private List<SchemaValidationHandler> chainOfValidationHandlers;


	@PostConstruct
	private void initHandlers(){
		Collections.sort(chainOfValidationHandlers, AnnotationAwareOrderComparator.INSTANCE);


		int size = chainOfValidationHandlers.size();

		for (int i = 0; i < size; i++) {
			if (i == size-1){
				chainOfValidationHandlers.get(i).setNextHandler(null);
			}else {
				chainOfValidationHandlers.get(i).setNextHandler(chainOfValidationHandlers.get(i+1));
			}
		}
	}

	public SchemaValidationHandler getFirstHandler() {
		return chainOfValidationHandlers.get(0);
	}

}
