package org.hacsick.reactiveweb;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Locale;

@Slf4j
public class HelloReactor {

    public static void main(String[] args) {
//        HelloReactor.monoEx();
//        HelloReactor.monoEx2();
//        HelloReactor.monoEx3();
//        HelloReactor.fluxEx();
//        HelloReactor.fluxEx2();
//        HelloReactor.fluxEx3();
//        HelloReactor.fluxEx4();
        HelloReactor.fluxEx5();
    }


    private static void monoEx() {
        Mono.just("Hello Reactor")
                .subscribe(System.out::println);
    }

    private static void monoEx2(){
        Mono.empty()
                .subscribe(
                        data -> log.info("# emitted data : {}", data), // 상위 Upstreamd로 부터 emit 된 데이터 전달받는 부분
                        error -> {}, // Upstream에서 에러가 발생했을 경우 에러를 전닯다는 경우
                        () -> log.info("# Emitted OnComplete Signal") // upsteram으로부터 onComplete 시그널을 받는 부분
                );
    }

    private static void monoEx3(){
        URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
                .host("worldtimeapi.org")
                .port(80)
                .path("/api/timezone/Asia/Seoul")
                .build()
                .encode()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Mono.just(
                restTemplate.exchange(worldTimeUri, HttpMethod.GET, new HttpEntity<String>(headers), String.class)
        )
                .map(response ->{
                    final DocumentContext jsonContext = JsonPath.parse(response.getBody());
                    return jsonContext.read("$.datetime");
                })
                .subscribe(
                        data -> log.info("# emitted data : {}", data),
                        error -> log.error(String.valueOf(error)),
                        () -> log.info("# emitted onComplete signal")
                );

        //Mono는 Http Request를 처리하기에 적합하다.
    }

    private static void fluxEx() {
        Flux<String> sequence = Flux.just("Hello", "Reactor");
        sequence.map(String::toLowerCase)
                .subscribe(log::info);

    }

    private static void fluxEx2(){
        Flux.just(3, 6, 9, 14) // 3,6,9,14 를 data source라 부른다.
                .map(num -> num % 2)
                .subscribe(remainder -> log.info("# remainder : {} ", remainder));
    }

    private static void fluxEx3(){
        Flux.fromArray(new Integer[]{3, 6, 9, 14}) // 3,6,9,14 를 data source라 부른다.
                .filter(num -> num > 6)
                .map(num -> num * 2)
                .subscribe(remainder -> log.info("# remainder : {} ", remainder));
    }

    private static void fluxEx4(){
        Flux<Object> flux = Mono.justOrEmpty(null)
                .concatWith(Mono.justOrEmpty("Jobs"));

        //Mono + Mono 는 flux이다.
        flux.subscribe(data -> log.info("# Result : {}", data));
    }

    private static void fluxEx5(){
        Flux.concat(
                Flux.just("Venus"),
                Flux.just("Earth"),
                Flux.just("Mars")
        )
                .collectList()
                .subscribe(planetList -> log.info("# Planet : {}", planetList));

        Flux.concat(
                        Flux.just("Venus"),
                        Flux.just("Earth"),
                        Flux.just("Mars")
                )
                .subscribe(planet -> log.info("# Planet : {}", planet));
    }


}
