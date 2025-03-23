package com.fadedtumi.sillytavernaccount.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
@Data
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String message;

    private String username;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}