package ru.yandex.masterbot.application;

/** Описание проекта и требования к заявке. */
public record Brief(
    String goal,
    String audience,
    String product,
    String description,
    String requirements,
    String budget,
    String deadline) {}
