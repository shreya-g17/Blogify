package com.blogmanagement.controller;

import com.blogmanagement.dto.BlogRequestDTO;
import com.blogmanagement.dto.BlogResponseDTO;
import com.blogmanagement.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/blogs", "/api/v1/blogs"})
@Tag(name = "Blogs", description = "Endpoints for managing blog posts")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    @Operation(summary = "Get all blog posts", description = "Retrieves a list of all blog posts along with their authors. Public access.")
    public ResponseEntity<List<BlogResponseDTO>> getAllBlogs() {
        List<BlogResponseDTO> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get blog post by ID", description = "Retrieves a single blog post's details by ID. Public access.")
    public ResponseEntity<BlogResponseDTO> getBlogById(@PathVariable Long id) {
        BlogResponseDTO blog = blogService.getBlogById(id);
        return ResponseEntity.ok(blog);
    }

    @PostMapping
    @Operation(summary = "Create a new blog post", description = "Creates a blog post for the authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BlogResponseDTO> createBlog(@Valid @RequestBody BlogRequestDTO request) {
        BlogResponseDTO createdBlog = blogService.createBlog(request);
        return new ResponseEntity<>(createdBlog, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a blog post", description = "Updates title and content of a blog post. Only the owner of the blog post can update it.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BlogResponseDTO> updateBlog(@PathVariable Long id, @Valid @RequestBody BlogRequestDTO request) {
        BlogResponseDTO updatedBlog = blogService.updateBlog(id, request);
        return ResponseEntity.ok(updatedBlog);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a blog post", description = "Deletes a blog post by its ID. Can only be triggered by the owner of the blog or a user with the ADMIN role.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}
