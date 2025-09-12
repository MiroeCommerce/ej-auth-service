package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @Column(name = "role_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "role_name", unique = true, nullable = false)
    private String name;
}
