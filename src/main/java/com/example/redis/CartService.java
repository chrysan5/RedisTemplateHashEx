package com.example.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class CartService {
    private final String keyString = "cart:%s"; //그냥 자주 써서 따로 빼 놓은 것이다.
    private final RedisTemplate<String, String> cartTemplate;
    private final HashOperations<String, String, Integer> hashOps;

    public CartService(RedisTemplate<String, String> cartTemplate) {
        this.cartTemplate = cartTemplate;
        this.hashOps = this.cartTemplate.opsForHash();
    }

    //여기서 수량 늘리고 줄이기를 동시에 해준다.
    public void modifyCart(String sessionId, CartItemDto dto) {
        hashOps.increment(keyString.formatted(sessionId), dto.getItem(), dto.getCount());

        int count = Optional.ofNullable(hashOps.get(keyString.formatted(sessionId), dto.getItem()))
                .orElse(0); //hashOps.get가 null일 경우 0을 준다

        if (count <= 0) {
            hashOps.delete(keyString.formatted(sessionId), dto.getItem());
        }
    }

    public CartDto getCart(String sessionId) {
        boolean exists = Optional.ofNullable(cartTemplate.hasKey(keyString.formatted(sessionId)))
                .orElse(false); //cartTemplate.hasKey의 반환값은 boolean이므로 null일 경우 false로 값을 준다.

        if (!exists) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Date expireAt = Date.from(Instant.now().plus(30, ChronoUnit.SECONDS)); //현재시간+30초

        cartTemplate.expireAt(keyString.formatted(sessionId), expireAt);

        return CartDto.fromHashPairs(hashOps.entries(keyString.formatted(sessionId)), expireAt);
    }
}
