package ru.skypro.homework.mapper;

import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateMapper {

    @Named("localDateTimeToLong")
    public static Long asLong(LocalDateTime time) {
        return time == null ? null : time.toEpochSecond(ZoneOffset.UTC);
    }

    @Named("longToLocalDateTime")
    public static LocalDateTime asLocalDateTime(Long value) {
        return value == null ? null : LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC);
    }
}
