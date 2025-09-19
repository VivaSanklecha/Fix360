package com.example.fix360.Service;

import com.example.fix360.repo.BookingsRepo;
import com.example.fix360.repo.UsersRepo;
import com.example.fix360.repo.ServiceRepo;
import com.example.fix360.dto.BookingRequestDTO;
import com.example.fix360.dto.BookingResponseDTO;
import com.example.fix360.Entity.Bookings;
import com.example.fix360.Entity.Users;
import com.example.fix360.Entity.ServiceProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingServiceImp implements BookingService {

    @Autowired
    private BookingsRepo bookingsRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private ServiceRepo serviceRepo;

    @Override
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        Users user = usersRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ServiceProvider provider = serviceRepo.findById(dto.getServiceProviderId())
                .orElseThrow(() -> new RuntimeException("Service Provider not found"));

        Bookings booking = new Bookings();
        booking.setUsers(user);
        booking.setServiceProvider(provider);
        booking.setBookingDate(dto.getBookingDate());
        booking.setStatus(dto.getStatus());

        Bookings saved = bookingsRepo.save(booking);

        BookingResponseDTO response = new BookingResponseDTO();
        response.setBookingId(saved.getId());
        response.setUserName(user.getName());
        response.setProviderName(provider.getName());
        response.setBookingDate(saved.getBookingDate());
        response.setStatus(saved.getStatus());

        return response;
    }
}
