package br.com.desafio.cartoes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenApiConfig implements GlobalOpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) return;

        var schemas = openApi.getComponents().getSchemas();

        // Renomeia propriedades para snake_case em todos os schemas
        for (var entry : schemas.entrySet()) {
            renamePropertiesToSnakeCase(entry.getValue());
        }

        // Cria schema SolicitacaoRequestDTO com wrapper "cliente"
        if (schemas.containsKey("ClienteRequestDTO") && !schemas.containsKey("SolicitacaoRequestDTO")) {
            ObjectSchema wrapperSchema = new ObjectSchema();
            Schema<?> clienteRef = new Schema<>();
            clienteRef.set$ref("#/components/schemas/ClienteRequestDTO");
            wrapperSchema.addProperty("cliente", clienteRef);
            wrapperSchema.setRequired(List.of("cliente"));
            schemas.put("SolicitacaoRequestDTO", wrapperSchema);

            // Atualiza o requestBody do POST /cartoes para usar o wrapper
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem -> {
                    if (pathItem.getPost() != null && pathItem.getPost().getRequestBody() != null) {
                        var content = pathItem.getPost().getRequestBody().getContent();
                        if (content != null && content.get("application/json") != null) {
                            Schema<?> ref = new Schema<>();
                            ref.set$ref("#/components/schemas/SolicitacaoRequestDTO");
                            content.get("application/json").setSchema(ref);
                        }
                    }
                });
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void renamePropertiesToSnakeCase(Schema<?> schema) {
        Map<String, Schema> properties = schema.getProperties();
        if (properties == null) return;

        Map<String, Schema> renamed = new LinkedHashMap<>();
        for (var entry : properties.entrySet()) {
            renamed.put(camelToSnake(entry.getKey()), entry.getValue());
        }
        schema.setProperties(renamed);

        List<String> required = schema.getRequired();
        if (required != null) {
            schema.setRequired(required.stream()
                .map(this::camelToSnake)
                .toList());
        }
    }

    private String camelToSnake(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
