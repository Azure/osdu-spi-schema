package org.opengroup.osdu.schema.validation.request;

import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component()
public class SchemaInfoRequestValidator {

    public void validateRequest(Set<String> queryParams){
        List<String> validInputParams = Arrays.asList("authority","source","entityType","schemaVersionMajor","schemaVersionMinor","schemaVersionPatch","status","scope","latestVersion","limit","offset");
        for (String paramName : queryParams) {
            if (!validInputParams.contains(paramName)) {
                throw new BadRequestException(paramName + " is not a valid input param!");
            }
        }
    }
    public static Set<String> extractQueryParamsFromRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        return new HashSet<>(httpServletRequest.getParameterMap().keySet());
    }
}
