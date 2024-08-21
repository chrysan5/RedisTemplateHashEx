package com.example.redis;

import lombok.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//현재 장바구니 상태 조회 dto

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Set<CartItemDto> items; //item과 count를 Set에 담다준다.
    private Date expireAt;

    //entries는 redis에 저장된 애를 불러오는데 사용함
    //레디스 정보와 시간을 dto로 만들어주는 코드
    public static CartDto fromHashPairs(Map<String, Integer> entries, Date expireAt) {
        return CartDto.builder()
                .items(entries.entrySet().stream()
                        .map(entry -> CartItemDto.builder()
                                .item(entry.getKey())
                                .count(entry.getValue())
                                .build())
                        .collect(Collectors.toUnmodifiableSet())) //Set을 읽기전용으로 바꿔줌
                .expireAt(expireAt)
                .build();
    }
}
