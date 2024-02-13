package org.hacsick.reactiveweb;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Slf4j
public class HelloReactor {

    public static void main(String[] args) {
        HelloReactor.fluxEx();
    }


    private static void monoEx() {
        Mono.just("Hello Reactor")
                .subscribe(System.out::println);
    }

    private static void fluxEx() {
        Flux<String> sequence = Flux.just("Hello", "Reactor");
        sequence.map(data -> data.toLowerCase())
                .subscribe(data -> log.info(data));

    }
}
