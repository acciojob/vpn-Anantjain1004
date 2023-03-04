package com.driver.services;

import com.driver.model.User;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public interface UserService {

    public User register(String username, String password, String countryName) throws Exception;
    public User subscribe(Integer userId, Integer serviceProviderId);
}
