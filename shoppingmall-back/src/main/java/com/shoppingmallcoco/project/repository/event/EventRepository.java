package com.shoppingmallcoco.project.repository.event;

import org.springframework.stereotype.Repository;

import com.shoppingmallcoco.project.entity.event.EventEntity;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
	
}
