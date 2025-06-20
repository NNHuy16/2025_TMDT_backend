package com.example.TMDT_Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailChangeToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String newEmail;

    private LocalDateTime expiryDate;

    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
    }

    public EmailChangeToken(String token, String newEmail, LocalDateTime expiryDate, User user) {
        this.token = token;
        this.newEmail = newEmail;
        this.expiryDate = expiryDate;
        this.user = user;
    }
}
