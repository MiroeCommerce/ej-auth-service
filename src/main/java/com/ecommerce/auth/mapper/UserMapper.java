package com.ecommerce.auth.mapper;

import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

//@Mapper(componentModel = "spring")
@Mapper(builder = @Builder(disableBuilder = true) ,componentModel = "spring" )
@Slf4j
public abstract class UserMapper {
    // Does not work with field injection for some reason
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Ignore to handle the mapping for these fields by ourselves
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(source = "password", target = "password", ignore = true)
    public abstract User toUser(RegisterRequest request);

    @AfterMapping
    public void afterToUser(@MappingTarget User user, RegisterRequest request) {
        user.setPassword(passwordEncoder.encode(request.getPassword()));
    }
}
