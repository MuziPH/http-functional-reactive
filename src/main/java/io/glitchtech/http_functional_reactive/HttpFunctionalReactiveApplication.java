package io.glitchtech.http_functional_reactive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
@RequiredArgsConstructor
public class HttpFunctionalReactiveApplication {

    @Bean
    RouterFunction<ServerResponse> routes(GreetingService greetingService) {
        return route()
                .GET("/greeting/{name}", request ->
                        ServerResponse.ok()
                                .body(
                                        greetingService.greetOnce(
                                                new GreetingsRequest(request.pathVariable("name")))
                                        , GreetingResponse.class
                                )
                )
                .GET("/greetings/{name}", request ->
                        ServerResponse.ok()
                                .contentType(MediaType.TEXT_EVENT_STREAM)
                                .body(
                                        greetingService.greetMany(
                                                new GreetingsRequest(request.pathVariable("name")))
                                        , GreetingResponse.class
                                )
                )
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

    // Create an infinite stream of greetings
    Flux<GreetingResponse> greetMany(GreetingsRequest greetingsRequest) {
        return Flux.fromStream(Stream.generate(() -> greet(greetingsRequest.getName())))
                .delayElements(Duration.ofSeconds(1))// Make supplier stream human-readable for debugging
                .subscribeOn(Schedulers.boundedElastic()); // Give new thread if there is none in case of IO blocking
    }

    // Pass the name from the request
    Mono<GreetingResponse> greetOnce(GreetingsRequest greetingsRequest) {
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
