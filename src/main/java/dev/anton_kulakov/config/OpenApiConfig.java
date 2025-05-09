package dev.anton_kulakov.config;

import dev.anton_kulakov.dto.UserRequestDto;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Map;

@Configuration
public class OpenApiConfig {
    public static final String TAG_AUTH = "Authentication";

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Cloud file storage API").version("1.0.0")
                        .description("API documentation for the cloud file storage project"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("SESSION")));
    }

    @Bean
    public OpenApiCustomizer authenticationEndpoints() {
        return openApi -> {
            PathItem signInPath = new PathItem();
            Operation signInOperation = new Operation();

            RequestBody signInRequestBody = new RequestBody()
                    .description("User credentials for authentication")
                    .required(true)
                    .content(new Content().addMediaType(
                            MediaType.APPLICATION_JSON_VALUE,
                            new io.swagger.v3.oas.models.media.MediaType().schema(new Schema<UserRequestDto>().$ref("#/components/schemas/UserRequestDto"))
                    ));

            ApiResponses signInResponses = new ApiResponses()
                    .addApiResponse(String.valueOf(HttpStatus.OK.value()), new ApiResponse()
                            .description("Authentication successful, session is created")
                            .content(new Content().addMediaType(
                                    MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                            .schema(new Schema<Map<String, String>>()
                                                    .addProperty("username", new StringSchema().example("test_user"))))))
                    .addApiResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                            new ApiResponse()
                                    .description("Invalid credentials")
                                    .content(new Content().addMediaType(
                                            MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                                    .schema(new Schema<Map<String, String>>()
                                                            .addProperty("message", new StringSchema().example("Invalid credentials"))))));

            signInOperation
                    .addTagsItem(TAG_AUTH)
                    .summary("User sign-in")
                    .description("Authenticates a user and establishes a session. The session ID is returned in the 'SESSION' cookie")
                    .operationId("signInUser")
                    .requestBody(signInRequestBody)
                    .responses(signInResponses);

            signInPath.post(signInOperation);
            openApi.getPaths().addPathItem("/api/auth/sign-in", signInPath);

            PathItem signOutPath = new PathItem();
            Operation signOutOperation = new Operation();

            ApiResponses signOutResponses = new ApiResponses()
                    .addApiResponse(String.valueOf(HttpStatus.OK.value()), new ApiResponse()
                            .description("Logout is successful, session is invalidated")
                            .content(new Content().addMediaType(
                                    MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                            .schema(new Schema<Map<String, String>>()
                                                    .addProperty("message", new StringSchema().example("Logout is successful"))))))
                    .addApiResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                            new ApiResponse()
                                    .description("user is not authenticated (there is no active session to log out)"));

            signOutOperation
                    .addTagsItem(TAG_AUTH)
                    .summary("User sign-out")
                    .description("Invalidates the current user's session. Requires an active session (SESSION cookie).")
                    .operationId("signOutUser")
                    .responses(signOutResponses)
                    .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));

            signOutPath.post(signOutOperation);
            openApi.getPaths().addPathItem("/api/auth/sign-out", signOutPath);
        };
    }
}
