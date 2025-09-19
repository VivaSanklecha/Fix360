package com.example.fix360.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fix360.Entity.Users;
import com.example.fix360.repo.UsersRepo;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {
	@Autowired
	private UsersRepo repo;
	
	@GetMapping("/")
	public List<Users> getUsers(){
		return repo.findAll();
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Users> getUsersById(@PathVariable int id) {
	    Users user = repo.findById(id).orElse(null);
	    return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
	}

	@PostMapping("/add")
	public Users addPost(@Valid @RequestBody Users user)
	{
		return repo.save(user);
	}

	@DeleteMapping("/del/{id}")
	public void delete(@PathVariable int id)
	{
		repo.deleteById(id);
	}
}
