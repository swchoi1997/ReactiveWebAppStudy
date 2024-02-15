package org.hacsick.reactiveweb;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class BackpressureExample {

    private static int cnt = 0;

    public static void main(String[] args) {
//        ex1();
//        ex2();
//        errorStrategy();
//        dropStrategy();
//        latestStrategy();
        bufferDropLatestAndOldestStrategy();
    }

    private static void ex1() {
        // 데이터 요청 개수를 제어하는 로직
        Flux.range(1, 10) // publisher쪽 에서 1 ~ 5까지 downstream으로 데이터를 emit함
                .doOnNext(next -> log.info("Data -> : " + String.valueOf(next))) // downstream 으로 내리는 데이터를 출력
                .doOnRequest(req -> log.info("Req Cnt -> : " + String.valueOf(req))) // subscriber에서 요청한 req 개수를 출력
                .subscribe(new BaseSubscriber<>() { // 요청 데이터의 개수를 조정함
                    @Override
                    protected void hookOnSubscribe(final Subscription subscription) {
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(final Integer value) {
                        try {
                            Thread.sleep(1000);
                            log.info(String.valueOf(value));
                            request(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
    }

    private static void ex2() {
        // 데이터 요청 개수를 제어하는 로직
        Flux.range(1, 10) // publisher쪽 에서 1 ~ 5까지 downstream으로 데이터를 emit함
                .doOnNext(next -> log.info("Data -> : " + String.valueOf(next))) // downstream 으로 내리는 데이터를 출력
                .doOnRequest(req -> log.info("Req Cnt -> : " + String.valueOf(req))) // subscriber에서 요청한 req 개수를 출력
                .subscribe(new BaseSubscriber<>() { // 요청 데이터의 개수를 조정함
                    @Override
                    protected void hookOnSubscribe(final Subscription subscription) {
                        request(2);
                    }

                    @Override
                    protected void hookOnNext(final Integer value) {
                        try {
                            cnt++;
                            log.info(String.valueOf(value));
                            if (cnt == 2){
                                Thread.sleep(2000);
                                request(2);
                                cnt = 0;
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
    }

    /**
     * UnBounded request 일 경우, Downstream에 Backpressure Error 전략을 적용하는 예제
     *  - Downstream으로 전달 할 데이터가 버퍼에 가득찰 경우, Exception을 발생시키는 전략
     */
    private static void errorStrategy() {

        Flux.interval(Duration.ofMillis(1))
                .onBackpressureError()
                .doOnNext(n -> log.info(String.valueOf(n)))
                .publishOn(Schedulers.parallel()) // Thread 추가
                .subscribe(data -> {
                    try {
                        Thread.sleep(5);
                        log.info(String.valueOf(data));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, error -> log.error(String.valueOf(error)));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unbounded request 일 경우 Downstream에 Backpressure Drop 전략을 적용하는 예제
     *   - Downstream으로 전달 할 데이터가 버퍼에 가득 찰 경우, 버퍼 밖에서 대기하는 먼저 emit 된 데이터를 Drop 시키는 전략
     */
    private static void dropStrategy() {
        Flux.interval(Duration.ofMillis(1))
                .onBackpressureDrop(dropped -> log.info("dropped -> " + dropped))
                .publishOn(Schedulers.parallel()) // Thread 추가
                .subscribe(data -> {
                    try {
                        Thread.sleep(5);
                        log.info(String.valueOf(data));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, error -> log.error(String.valueOf(error)));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unbounded request 일 경우, Downstream에 Backpressure Latest 전략을 적용하는 예제
     *   - Downstream으로 전달 할 데이터가 버퍼에 가득 찰 경우,
     *     버퍼 밖에서 폐기되지 않고 대기하는 가장 나중에(최근에) emit 된 데이터부터 버퍼에 채우는 전략
     */

    private static void latestStrategy() {
        Flux.interval(Duration.ofMillis(1))
                .onBackpressureLatest()
                .publishOn(Schedulers.parallel())
                .subscribe(data -> {
                    try {
                        Thread.sleep(5);
                        log.info("Data : " + data);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, error -> log.info(String.valueOf(error)));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void bufferDropLatestAndOldestStrategy() {
        Flux.interval(Duration.ofMillis(300))
                .doOnNext(data -> log.info("# Emitted by original Flux : " + data)) // Publisher가 Emit한 데이터 출력
                .onBackpressureBuffer(2,
                        dropped -> log.info("# Overflow & Dropped : " + dropped),
//                        BufferOverflowStrategy.DROP_LATEST)
                        BufferOverflowStrategy.DROP_OLDEST)
                .doOnNext(data -> log.info("# Emitted By Buffer : " + data)) // Buffer에서 Emit된 데이터 출력
                .publishOn(Schedulers.parallel(), false, 1) // prefetch 는 추가되는 스레드에서 사용하는 buffer같은 것
                .subscribe(data -> {
                    try {
                        Thread.sleep(1000);
                        log.info(String.valueOf(data));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
