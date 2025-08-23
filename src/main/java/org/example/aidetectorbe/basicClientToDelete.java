package org.example.aidetectorbe;

import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public  class basicClientToDelete {

    public static void saveRandomUser() {
        ConfigurableApplicationContext context = SpringApplication.run(AiDetectorBeApplication.class);

        Integer randomId = (int) (Math.random() * 1000);
        User user = new User(
                "login" + randomId,
                "password" + randomId,
                "email" + randomId + "@example.com"
        );

        UserRepository userRepository = context.getBean(UserRepository.class);
        userRepository.save(user);
        System.out.println("User saved: " + user);
    }
}
