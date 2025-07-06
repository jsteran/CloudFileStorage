package dev.anton_kulakov.model;

import dev.anton_kulakov.controller.UserListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "Users")
@Setter
@Getter
@NoArgsConstructor
@EntityListeners(UserListener.class)
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
}
