package com.blogmanagement.repository;

import com.blogmanagement.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query("SELECT b FROM BlogPost b JOIN FETCH b.author")
    List<BlogPost> findAllWithAuthor();

    @Query("SELECT b FROM BlogPost b JOIN FETCH b.author WHERE b.id = :id")
    Optional<BlogPost> findByIdWithAuthor(@Param("id") Long id);
}
