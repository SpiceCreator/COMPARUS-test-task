package ua.comparus.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) List<String> dataSourceName,
            @RequestParam(required = false) List<String> id,
            @RequestParam(required = false) List<String> username,
            @RequestParam(required = false) List<String> name,
            @RequestParam(required = false) List<String> surname
    ) {
        Map<String, List<String>> userDataParameters = new HashMap<>() {{
            if (dataSourceName != null) put("id", id);
            put("username", username);
            put("surname", surname);
            put("name", name);
        }};

        return ResponseEntity.ok(userService.findAll(dataSourceName, userDataParameters));
    }
}
