package com.farmora.repository;

import com.farmora.model.User;
import com.farmora.model.VideoCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoCallRepository extends JpaRepository<VideoCall, Long> {

    @Query("SELECT v FROM VideoCall v WHERE v.caller = :user OR v.receiver = :user ORDER BY v.createdAt DESC")
    List<VideoCall> findMyCalls(@Param("user") User user);

    Optional<VideoCall> findByRoomCode(String roomCode);
}
