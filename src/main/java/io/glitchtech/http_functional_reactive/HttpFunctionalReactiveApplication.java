package io.glitchtech.http_functional_reactive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
@RequiredArgsConstructor
public class HttpFunctionalReactiveApplication {

    @Bean
    RouterFunction<ServerResponse> routes(GreetingService greetingService) {
        return route()
                .GET("/greeting/{name}", request -> {
                    // Create the Greeting request with the json from the incoming request
                    GreetingsRequest greetingsRequest =
                            new GreetingsRequest(request.pathVariable("name"));

                    Mono<GreetingResponse> greetingResponseMono =
                            greetingService.greetingResponseMono(greetingsRequest);
                    return ServerResponse.ok().body(greetingResponseMono, GreetingResponse.class);
                })
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(HttpFunctionalReactiveApplication.class, args);
    }

}

//==================SERVICE LAYER=========================================

// Service to produce GreetingResponse and GreetingsRequest when requested
@Service
class GreetingService {

    // Business logic to create a Greeting response
    private GreetingResponse greet(String name) {
        return new GreetingResponse("Hello " + name + " at " + Instant.now());
    }

    // Pass the name from the request
    Mono<GreetingResponse> greetingResponseMono(GreetingsRequest greetingsRequest) {
        return Mono.just(greet(greetingsRequest.getName()));
    }
}


//===================MODEL-ENTITY=========================================

// Model for Greetings response
@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse {
    private String message;
}

//Model for Greetings request
@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingsRequest {
    private String name;
}
