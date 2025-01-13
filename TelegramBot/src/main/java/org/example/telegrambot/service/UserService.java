package org.example.telegrambot.service;

import org.example.telegrambot.entity.User;
import org.example.telegrambot.entity.enums.Role;
import org.example.telegrambot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUserIfNotExists(String chatId,String username) {
        User existingUser = userRepository.findByChatId(chatId);
        if (existingUser == null) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setChatId(chatId);
            newUser.setRole(Role.USER);
            userRepository.save(newUser);
        }
    }

    public User find(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public User findByChatId(Long chatId){
        return userRepository.findByChatId(chatId.toString());
    }

    public long getCountUsers(){
        return userRepository.count();
    }

    public boolean assignAdminById(Long userId) {
        User optionalUser = userRepository.findById(userId).orElse(null);
        if (optionalUser != null) {
            optionalUser.setRole(Role.ADMIN);
            userRepository.save(optionalUser);
            return true;
        } else {
            System.out.println("Foydalanuvchi topilmadi.");
        }
        return false;
    }

    public void removeAdmin(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setRole(Role.USER);
            userRepository.save(user);
        }
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public void deleteUser(Long chatId) {
        User user = userRepository.findByChatId(chatId.toString());
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public List<User> getAdmins(){
        return userRepository.findByRole();
    }
}
