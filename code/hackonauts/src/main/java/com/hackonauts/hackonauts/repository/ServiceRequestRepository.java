package com.hackonauts.hackonauts.repository;
import com.hackonauts.hackonauts.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Integer> {
}

