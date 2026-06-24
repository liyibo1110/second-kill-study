package com.github.liyibo1110.secondkill.base.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间类型序列化/反序列化模块
 * @author liyibo
 * @date 2026-06-23 15:20
 */
public class SecondKillJavaTimeModule extends SimpleModule {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    public SecondKillJavaTimeModule() {
        super("SeckillJavaTimeModule");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN);

        addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));

        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
    }
}
