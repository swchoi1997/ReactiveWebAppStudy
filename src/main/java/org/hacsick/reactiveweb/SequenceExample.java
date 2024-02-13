package org.hacsick.reactiveweb;

import ch.qos.logback.core.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class SequenceExample {
    public static void main(String[] args) throws InterruptedException {
//        coldSequence();
        hotSequence();
    }

    private static void coldSequence() {
        Flux<String> coldFlux = Flux.fromIterable(List.of("RED", "YELLOW", "BLUE", "PINK"))
                .map(String::toLowerCase);

        coldFlux.subscribe(color -> log.info("# Color1 : " + color));
        log.info("=================================");
        coldFlux.subscribe(color -> log.info("# Color2 : " + color));
    }

    private static void hotSequence() throws InterruptedException {
        Flux<String> hotFlux = Flux.fromStream(Stream.of("A", "B", "C", "D", "E", "F", "G"))
                .delayElements(Duration.ofSeconds(1)).share(); // share하면 main Thread가 아닌 다른 스레드가 생긴다.

        hotFlux.subscribe(alpha -> log.info("# Subscriber : " + alpha));
        Thread.sleep(2100);
        hotFlux.subscribe(alpha -> log.info("# Subscriber2 : " + alpha));
        Thread.sleep(7000);



    }
}
