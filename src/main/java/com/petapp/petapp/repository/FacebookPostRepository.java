package com.petapp.petapp.repository;

import com.petapp.petapp.entity.FacebookPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacebookPostRepository extends JpaRepository<FacebookPost, Long> {

    Optional<FacebookPost> findByFacebookPostId(String facebookPostId);

    boolean existsByFacebookPostId(String facebookPostId);
}
