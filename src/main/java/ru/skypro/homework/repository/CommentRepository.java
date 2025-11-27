package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.AdEntity;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByAdOrderByCreatedAtDesc(AdEntity ad);
}
