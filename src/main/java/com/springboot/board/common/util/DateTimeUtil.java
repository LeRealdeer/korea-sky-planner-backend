package com.springboot.board.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtil {
    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE_SEOUL);
    }
}