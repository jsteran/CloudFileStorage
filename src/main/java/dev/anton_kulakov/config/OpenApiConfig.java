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
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Map;

@Configuration
public class OpenApiConfig {
    public static final String AUTHENTICATION_TAG = "Authentication";
    public static final String USER_TAG = "User controller";
    public static final String REGISTRATION_TAG = "Registration controller";
    public static final String RESOURCE_TAG = "Resource controller";
    public static final String FOLDER_TAG = "Folder controller";

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Cloud file storage API").version("1.0.0")
                        .description("API documentation for the cloud file storage project"))
                .addTagsItem(new Tag()
                        .name(REGISTRATION_TAG)
                        .description("A controller for user registration"))
                .addTagsItem(new Tag()
                        .name(AUTHENTICATION_TAG)
                        .description("Endpoints for authentication and session management"))
                .addTagsItem(new Tag()
                        .name(USER_TAG)
                        .description("A controller for managing user accounts in an application"))
                .addTagsItem(new Tag()
                        .name(RESOURCE_TAG)
                        .description("A controller for managing files and folders"))
                .addTagsItem(new Tag()
                        .name(FOLDER_TAG)
                        .description("A controller for managing folder-specific features"))
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
                    .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                            .schema(new Schema<UserRequestDto>().$ref("#/components/schemas/UserRequestDto"))));

            ApiResponses signInResponses = new ApiResponses()
                    .addApiResponse(String.valueOf(HttpStatus.OK.value()), new ApiResponse()
                            .description("Authentication successful, session is created")
                            .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(new Schema<Map<String, String>>().addProperty("username", new StringSchema().example("test_user")))
                            )))
                    .addApiResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()), new ApiResponse()
                            .description("Validation error")
                            .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(new Schema<Map<String, String>>().addProperty("message", new StringSchema().example("There is a validation error. username: Username should be longer than 5 characters")))
                            )))
                    .addApiResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()), new ApiResponse()
                            .description("Invalid credentials")
                            .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(new Schema<Map<String, String>>().addProperty("message", new StringSchema().example("Invalid credentials")))
                            )))
                    .addApiResponse(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), new ApiResponse()
                            .description("Unexpected error")
                            .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(new Schema<Map<String, String>>().addProperty("message", new StringSchema().example("We're sorry, but an unexpected error has occurred. Please try again later")))
                            )));

            signInOperation
                    .addTagsItem(AUTHENTICATION_TAG)
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
                    .addApiResponse(String.valueOf(HttpStatus.NO_CONTENT.value()), new ApiResponse()
                            .description("Logout is successful, session is invalidated"))
                    .addApiResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()), new ApiResponse()
                            .description("User is not authenticated (there is no active session to sign out)")
                            .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(new Schema<Map<String, String>>().addProperty("message", new StringSchema().example("Invalid credentials")))
                            )))
                    .addApiResponse(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), new ApiResponse()
                            .description("Unexpected error")
                            .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(new Schema<Map<String, String>>().addProperty("message", new StringSchema().example("We're sorry, but an unexpected error has occurred. Please try again later")))
                            )));

            signOutOperation
                    .addTagsItem(AUTHENTICATION_TAG)
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
