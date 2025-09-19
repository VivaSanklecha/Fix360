package com.example.fix360.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fix360.Entity.Bookings;
import java.util.List;

public interface BookingsRepo extends JpaRepository<Bookings,Integer>{

    List<Bookings> findByUsers_Id(int userId);
    List<Bookings> findByServiceProvider_Id(int providerId);
    List<Bookings> findByServiceProvider_Owner_Id(int ownerId);

}
