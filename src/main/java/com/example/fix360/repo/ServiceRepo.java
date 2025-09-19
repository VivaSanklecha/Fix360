package com.example.fix360.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.fix360.Entity.ServiceProvider;
import com.example.fix360.Entity.Users;

public interface ServiceRepo extends JpaRepository<ServiceProvider,Integer>{

    Page<ServiceProvider> findByServiceType(String serviceType, Pageable pageable);
    Page<ServiceProvider> findByRatingGreaterThanEqual(float rating, Pageable pageable);
    Page<ServiceProvider> findByServiceTypeAndRatingGreaterThanEqual(String serviceType, float rating, Pageable pageable);
    Page<ServiceProvider> findByPriceGreaterThanEqual(double price, Pageable pageable);
    Page<ServiceProvider> findByServiceTypeAndRatingGreaterThanEqualAndPriceGreaterThanEqual(String serviceType, float rating, double price, Pageable pageable);
    Page<ServiceProvider> findByServiceTypeAndPriceGreaterThanEqual(String serviceType, double price, Pageable pageable);
    Page<ServiceProvider> findByRatingGreaterThanEqualAndPriceGreaterThanEqual(float rating, double price, Pageable pageable);
    Page<ServiceProvider> findByOwner(Users owner, Pageable pageable);

}
