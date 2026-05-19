package br.ufrn.chat.domain;

public record ChatMessage(
    String remetente, 
    String sala, 
    String texto, 
    long timestamp
) {}