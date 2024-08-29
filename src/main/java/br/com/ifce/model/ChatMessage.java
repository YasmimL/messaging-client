package br.com.ifce.model;

import java.io.Serializable;
import java.time.LocalTime;

public record ChatMessage(String from, String to, String message, LocalTime time) implements Serializable {

}
