package ua.comparus.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
public class User {

    private String id;
    private String username;
    private String name;
    private String surname;

    public User(String id, String username, String name, String surname) {
        this.id = id == null ? username : id;
        this.username = username;
        this.name = name;
        this.surname = surname;
    }
}
