package com.yizhaoqi.knowflow.repository;

import com.yizhaoqi.knowflow.model.RateLimitConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateLimitConfigRepository extends JpaRepository<RateLimitConfig, String> {
}
