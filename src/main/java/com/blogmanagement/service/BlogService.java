package com.blogmanagement.service;

import com.blogmanagement.dto.BlogRequestDTO;
import com.blogmanagement.dto.BlogResponseDTO;
import com.blogmanagement.dto.UserResponseDTO;
import com.blogmanagement.entity.BlogPost;
import com.blogmanagement.entity.Role;
import com.blogmanagement.entity.User;
import com.blogmanagement.exception.ResourceNotFoundException;
import com.blogmanagement.exception.UnauthorizedException;
import com.blogmanagement.repository.BlogPostRepository;
import com.blogmanagement.repository.UserRepository;
import com.blogmanagement.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    public BlogService(BlogPostRepository blogPostRepository, UserRepository userRepository) {
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs() {
        return blogPostRepository.findAllWithAuthor().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BlogResponseDTO getBlogById(Long id) {
        BlogPost blogPost = blogPostRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));
        return mapToResponseDTO(blogPost);
    }

    public BlogResponseDTO createBlog(BlogRequestDTO request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found in database"));

        BlogPost blogPost = BlogPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .build();

        BlogPost savedBlogPost = blogPostRepository.save(blogPost);
        return mapToResponseDTO(savedBlogPost);
    }

    public BlogResponseDTO updateBlog(Long id, BlogRequestDTO request) {
        String email = SecurityUtil.getCurrentUserEmail();
        BlogPost blogPost = blogPostRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        // Check if current user is the owner
        if (!blogPost.getAuthor().getEmail().equals(email)) {
            throw new UnauthorizedException("Only the owner of this blog post can edit it.");
        }

        blogPost.setTitle(request.getTitle());
        blogPost.setContent(request.getContent());

        BlogPost updatedBlogPost = blogPostRepository.save(blogPost);
        return mapToResponseDTO(updatedBlogPost);
    }

    public void deleteBlog(Long id) {
        String email = SecurityUtil.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found in database"));

        BlogPost blogPost = blogPostRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        boolean isOwner = blogPost.getAuthor().getEmail().equals(email);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        // Owner or ADMIN can delete
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Only the blog owner or an admin can delete this post.");
        }

        blogPostRepository.delete(blogPost);
    }

    private BlogResponseDTO mapToResponseDTO(BlogPost blogPost) {
        UserResponseDTO authorDTO = UserResponseDTO.builder()
                .id(blogPost.getAuthor().getId())
                .name(blogPost.getAuthor().getName())
                .email(blogPost.getAuthor().getEmail())
                .role(blogPost.getAuthor().getRole())
                .build();

        return BlogResponseDTO.builder()
                .id(blogPost.getId())
                .title(blogPost.getTitle())
                .content(blogPost.getContent())
                .createdAt(blogPost.getCreatedAt())
                .updatedAt(blogPost.getUpdatedAt())
                .author(authorDTO)
                .build();
    }
}
