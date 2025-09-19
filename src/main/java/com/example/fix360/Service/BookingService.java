package com.example.fix360.Service;

import com.example.fix360.dto.BookingRequestDTO;
import com.example.fix360.dto.BookingResponseDTO;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO dto);
}
