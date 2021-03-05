package org.geektimes.projects.user.service;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.sql.DBConnectionManager;

public class UserServiceImpl implements UserService {

    @Override
    public boolean register(User user) {
        return false;
    }

/**
 * 用户服务
 */
//public class UserServiceImpl implements UserService {
//    DBConnectionManager dbConnectionManager = new DBConnectionManager();
//    UserRepository userRepository = new DatabaseUserRepository(dbConnectionManager);
//
//    @Override
//    public boolean register(User user) {
//        return userRepository.save(user);
//    }

    @Override
    public boolean deregister(User user) {
        return false;
    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public User queryUserById(Long id) {
        return null;
    }

    @Override
    public User queryUserByNameAndPassword(String name, String password) {
        return null;
    }
}
