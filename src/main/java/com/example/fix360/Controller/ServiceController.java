package com.example.fix360.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.fix360.Entity.ServiceProvider;
import com.example.fix360.Entity.Users;
import com.example.fix360.repo.UsersRepo;
import com.example.fix360.repo.ServiceRepo;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/services")
public class ServiceController {
	@Autowired
	ServiceRepo repos;

	@Autowired
	UsersRepo usersRepo;

	@GetMapping("")
	public List<ServiceProvider> get()
	{
		return  repos.findAll();
	}
	
	@PostMapping("/add")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_PROVIDER')")
	public ServiceProvider add(@Valid @RequestBody ServiceProvider sp, Authentication auth)
	{
		Users owner = usersRepo.findByEmail(auth.getName());
		if (owner == null) {
			throw new IllegalArgumentException("Owner not found for authenticated user");
		}
		sp.setOwner(owner);
		return repos.save(sp);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> deleteServiceProvider(@PathVariable int id, Authentication auth) {
		return repos.findById(id)
			.map(sp -> {
				Users current = usersRepo.findByEmail(auth.getName());
				boolean isAdmin = current.getRole() != null && current.getRole().equalsIgnoreCase("ADMIN");
				boolean isOwner = sp.getOwner() != null && sp.getOwner().getId() == current.getId();
                if (!isAdmin && !isOwner) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
				repos.delete(sp);
				return ResponseEntity.noContent().build();
			})
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ServiceProvider> getServiceProviderById(@PathVariable int id) {
		Optional<ServiceProvider> sp = repos.findById(id);
		return sp.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_PROVIDER')")
	public ResponseEntity<ServiceProvider> updateServiceProvider(@PathVariable int id, @Valid @RequestBody ServiceProvider updatedSp, Authentication auth) {
		return repos.findById(id)
			.map(existingSp -> {
				Users current = usersRepo.findByEmail(auth.getName());
                if (current == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<ServiceProvider>build();
                }
				boolean isAdmin = current.getRole() != null && current.getRole().equalsIgnoreCase("ADMIN");
				boolean isOwner = existingSp.getOwner() != null && existingSp.getOwner().getId() == current.getId();
                if (!isAdmin && !isOwner) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).<ServiceProvider>build();
                }
				if (updatedSp.getName() != null) existingSp.setName(updatedSp.getName());
				if (updatedSp.getPhone() != null) existingSp.setPhone(updatedSp.getPhone());
				if (updatedSp.getEmail() != null) existingSp.setEmail(updatedSp.getEmail());
				if (updatedSp.getServiceType() != null) existingSp.setServiceType(updatedSp.getServiceType());
				if (updatedSp.getPrice() != null) existingSp.setPrice(updatedSp.getPrice());
				if (updatedSp.getStatus() != null) existingSp.setStatus(updatedSp.getStatus());
				existingSp.setRating(updatedSp.getRating());
				ServiceProvider saved = repos.save(existingSp);
				return ResponseEntity.ok(saved);
			})
			.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<ServiceProvider>build());
	}

	/**
	 * Get paginated, optionally filtered and sorted list of service providers.
	 * Example: /services/paged?page=0&size=5&serviceType=plumber&minRating=4&minPrice=200&sortBy=price&sortDir=asc
	 * @param page which page to fetch (0-based)
	 * @param size how many records per page
	 * @param serviceType optional filter by service type
	 * @param minRating optional filter by minimum rating
	 * @param minPrice optional filter by minimum price
	 * @param sortBy field to sort by (rating or price)
	 * @param sortDir direction to sort (asc or desc)
	 * @return a page of ServiceProvider
	 */
	@GetMapping("/paged")
	public Page<ServiceProvider> getPagedServiceProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) Double minPrice,
			@RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "false") boolean mine,
            Authentication auth
    ) {
        // Allow sorting by rating, price, createdAt, updatedAt (default)
        String sortField;
        if ("price".equalsIgnoreCase(sortBy)) {
            sortField = "price";
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            sortField = "rating";
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            sortField = "createdAt";
        } else {
            sortField = "updatedAt";
        }
		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (mine && auth != null) {
            Users owner = usersRepo.findByEmail(auth.getName());
            return repos.findByOwner(owner, pageable);
        }
		if (serviceType != null && !serviceType.isEmpty() && minRating != null && minPrice != null) {
			return repos.findByServiceTypeAndRatingGreaterThanEqualAndPriceGreaterThanEqual(serviceType, minRating, minPrice, pageable);
		} else if (serviceType != null && !serviceType.isEmpty() && minRating != null) {
			return repos.findByServiceTypeAndRatingGreaterThanEqual(serviceType, minRating, pageable);
		} else if (serviceType != null && !serviceType.isEmpty() && minPrice != null) {
			return repos.findByServiceTypeAndPriceGreaterThanEqual(serviceType, minPrice, pageable);
		} else if (minRating != null && minPrice != null) {
			return repos.findByRatingGreaterThanEqualAndPriceGreaterThanEqual(minRating, minPrice, pageable);
		} else if (serviceType != null && !serviceType.isEmpty()) {
			return repos.findByServiceType(serviceType, pageable);
		} else if (minRating != null) {
			return repos.findByRatingGreaterThanEqual(minRating, pageable);
		} else if (minPrice != null) {
			return repos.findByPriceGreaterThanEqual(minPrice, pageable);
		} else {
			return repos.findAll(pageable);
		}
	}

	@GetMapping("/mine")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_PROVIDER')")
	public Page<ServiceProvider> getMyServices(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "updatedAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir,
			Authentication auth
	) {
		String sortField = ("asc".equalsIgnoreCase(sortDir)) ? sortBy : sortBy;
		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Users owner = usersRepo.findByEmail(auth.getName());
		return repos.findByOwner(owner, pageable);
	}

}
