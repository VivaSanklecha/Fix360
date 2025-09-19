package com.example.fix360.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import com.example.fix360.dto.BookingRequestDTO;
import com.example.fix360.dto.BookingResponseDTO;
import com.example.fix360.Service.BookingService;
import jakarta.validation.Valid;
import com.example.fix360.Entity.Bookings;
import com.example.fix360.repo.BookingsRepo;
import com.example.fix360.repo.UsersRepo;
import com.example.fix360.Entity.Users;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingsController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingsRepo bookingsRepo;

    @Autowired
    private UsersRepo usersRepo;

    @PostMapping("/add")
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO dto) {
        BookingResponseDTO response = bookingService.createBooking(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SERVICE_PROVIDER')")
    public List<Bookings> getBookingsByUser(@PathVariable int userId) {
        return bookingsRepo.findByUsers_Id(userId);
    }

    // Convenience endpoint: bookings for the authenticated user (no id needed)
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SERVICE_PROVIDER')")
    public List<Bookings> getMyBookings(Authentication auth) {
        Users me = usersRepo.findByEmail(auth.getName());
        if (me == null) {
            return List.of();
        }
        return bookingsRepo.findByUsers_Id(me.getId());
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SERVICE_PROVIDER')")
    public List<Bookings> getBookingsByProvider(@PathVariable int providerId) {
        return bookingsRepo.findByServiceProvider_Id(providerId);
    }

    // Bookings for the currently logged-in provider (owner of services)
    @GetMapping("/provider/requests/mine")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE_PROVIDER')")
    public List<Bookings> getBookingsForMyServices(Authentication auth) {
        Users owner = usersRepo.findByEmail(auth.getName());
        if (owner == null) {
            return List.of();
        }
        return bookingsRepo.findByServiceProvider_Owner_Id(owner.getId());
    }
}

