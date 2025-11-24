package com.ucop.dinh_admin;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Dinh_Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "role_name", unique = true, nullable = false)
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private Set<Dinh_User> users;

    public Dinh_Role() {}
    public Dinh_Role(String roleName) { this.roleName = roleName; }
    
    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Set<Dinh_User> getUsers() { return users; }
    public void setUsers(Set<Dinh_User> users) { this.users = users; }
}