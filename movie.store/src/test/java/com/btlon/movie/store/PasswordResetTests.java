package com.btlon.movie.store;

import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;
import com.btlon.movie.store.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PasswordResetTests {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper mapper;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void forgotPasswordNeedsMatchingEmailAndPhoneAndLogsOutEverywhere() throws Exception {
        createUser("reset-me@example.test", "oldpass");
        String oldSession = login("reset-me@example.test", "oldpass");

        assertEquals(400, reset("reset-me@example.test", "0911111111", "newpass", "newpass").statusCode());
        assertEquals(400, reset("nobody@example.test", "0912345678", "newpass", "newpass").statusCode());
        assertEquals(400, reset("reset-me@example.test", "0912345678", "newpass", "different").statusCode());
        // Same message for wrong phone and unknown email, so the form cannot reveal which accounts exist.
        assertEquals(mapper.readTree(reset("nobody@example.test", "0912345678", "newpass", "newpass").body()).get("message"),
                mapper.readTree(reset("reset-me@example.test", "0911111111", "newpass", "newpass").body()).get("message"));

        assertEquals(204, reset(" reset-me@example.test ", "+84 912 345 678", "newpass", "newpass").statusCode());
        assertEquals(401, request("GET", "/api/users/me", null, oldSession).statusCode());
        assertEquals(401, request("POST", "/api/users/login",
                "{\"email\":\"reset-me@example.test\",\"password\":\"oldpass\"}", null).statusCode());
        login("reset-me@example.test", "newpass");
    }

    @Test
    void accountWithoutPhoneCannotBeResetByPhone() throws Exception {
        User legacy = new User();
        legacy.setFullName("Legacy");
        legacy.setEmail("legacy@example.test");
        legacy.setPasswordHash(passwordEncoder.encode("oldpass"));
        users.save(legacy);
        assertEquals(400, reset("legacy@example.test", "0912345678", "newpass", "newpass").statusCode());
    }

    @Test
    void changePasswordNeedsCurrentPasswordAndKeepsOnlyThisSession() throws Exception {
        createUser("change-me@example.test", "oldpass");
        String phone = login("change-me@example.test", "oldpass");
        String laptop = login("change-me@example.test", "oldpass");

        assertEquals(401, request("PUT", "/api/users/me/password",
                "{\"currentPassword\":\"oldpass\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}", null).statusCode());
        assertEquals(400, request("PUT", "/api/users/me/password",
                "{\"currentPassword\":\"wrong\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}", laptop).statusCode());
        assertEquals(400, request("PUT", "/api/users/me/password",
                "{\"currentPassword\":\"oldpass\",\"newPassword\":\"oldpass\",\"confirmPassword\":\"oldpass\"}", laptop).statusCode());
        assertEquals(204, request("PUT", "/api/users/me/password",
                "{\"currentPassword\":\"oldpass\",\"newPassword\":\"newpass\",\"confirmPassword\":\"newpass\"}", laptop).statusCode());

        assertEquals(200, request("GET", "/api/users/me", null, laptop).statusCode());
        assertEquals(401, request("GET", "/api/users/me", null, phone).statusCode());
        assertTrue(passwordEncoder.matches("newpass", users.findByEmail("change-me@example.test").orElseThrow().getPasswordHash()));
    }

    private void createUser(String email, String password) {
        User user = new User();
        user.setFullName(email);
        user.setEmail(email);
        user.setPhone("0912345678");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.CUSTOMER);
        users.save(user);
    }

    private String login(String email, String password) throws Exception {
        HttpResponse<String> response = request("POST", "/api/users/login",
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}", null);
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).get("token").asString();
    }

    private HttpResponse<String> reset(String email, String phone, String password, String confirm) throws Exception {
        return request("POST", "/api/users/reset-password", "{\"email\":\"" + email + "\",\"phone\":\"" + phone
                + "\",\"newPassword\":\"" + password + "\",\"confirmPassword\":\"" + confirm + "\"}", null);
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
