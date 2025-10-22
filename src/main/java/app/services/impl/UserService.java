package app.services.impl;

import app.daos.impl.UserDAO;
import app.entities.User;
import app.security.utils.PasswordUtil;
import app.services.dto.UserDTO;

public class UserService {

    private final UserDAO dao = new UserDAO();

    public UserDTO register(String email, String rawPassword, String role){
        var existing = dao.findByEmail(email);
        if (existing != null) return null;

        String hash = PasswordUtil.hash(rawPassword);

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setRole(role);

        var saved = dao.create(u);
        return new UserDTO(saved.getId(), saved.getEmail(), saved.getRole());
    }

    public UserDTO verify(String email, String rawPassword){
        var u = dao.findByEmail(email);
        if (u == null || u.getPasswordHash() == null) return null;

        boolean ok = PasswordUtil.verify(rawPassword, u.getPasswordHash());
        if (!ok) return null;

        return new UserDTO(u.getId(), u.getEmail(), u.getRole());
    }
}
