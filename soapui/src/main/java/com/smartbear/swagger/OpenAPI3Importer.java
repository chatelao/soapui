package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import com.eviware.soapui.impl.rest.RestRepresentation;


import java.util.Collections;
import java.util.Map;

public class OpenAPI3Importer implements SwaggerImporter {
    private final WsdlProject project;

    public OpenAPI3Importer(WsdlProject project) {
        this.project = project;
    }

    @Override
    public RestService[] importSwagger(String url) {
        return importSwagger(url, null);
    }

    @Override
    public RestService[] importSwagger(String url, String apiKey) {
        OpenAPI openAPI = new OpenAPIV3Parser().read(url);
        if (openAPI == null) {
            return new RestService[0];
        }

        RestService service = (RestService) project.addNewInterface(openAPI.getInfo().getTitle(), RestServiceFactory.REST_TYPE);

        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();

            RestResource resource = service.addNewResource(path, path);

            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : pathItem.readOperationsMap().entrySet()) {
                PathItem.HttpMethod httpMethod = operationEntry.getKey();
                Operation operation = operationEntry.getValue();

                RestMethod method = resource.addNewMethod(operation.getOperationId());
                method.setMethod(RestRequestInterface.HttpMethod.valueOf(httpMethod.name()));

                if (operation.getParameters() != null) {
                    for (Parameter parameter : operation.getParameters()) {
                        RestParamsPropertyHolder.ParameterStyle style = RestParamsPropertyHolder.ParameterStyle.valueOf(parameter.getIn().toUpperCase());
                        method.addProperty(parameter.getName()).setStyle(style);
                    }
                }

                RequestBody requestBody = operation.getRequestBody();
                if (requestBody != null) {
                    for (Map.Entry<String, MediaType> contentEntry : requestBody.getContent().entrySet()) {
                        String mediaType = contentEntry.getKey();
                        RestRequest request = method.addNewRequest("Request 1");
                        request.setMediaType(mediaType);
                        if (contentEntry.getValue().getExample() != null) {
                            request.setRequestContent(contentEntry.getValue().getExample().toString());
                        }
                    }
                }

                ApiResponses responses = operation.getResponses();
                if (responses != null) {
                    for (Map.Entry<String, ApiResponse> responseEntry : responses.entrySet()) {
                        String httpStatusCode = responseEntry.getKey();
                        ApiResponse response = responseEntry.getValue();
                        if (response.getContent() != null) {
                            for (String mediaType : response.getContent().keySet()) {
                                RestRepresentation representation = method.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                                representation.setMediaType(mediaType);
                                representation.setStatus(Collections.singletonList(httpStatusCode));
                            }
                        }
                    }
                }
            }
        }

        return new RestService[]{service};
    }
}
