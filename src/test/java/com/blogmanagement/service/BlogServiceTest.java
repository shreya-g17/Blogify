package com.blogmanagement.service;

import com.blogmanagement.dto.BlogRequestDTO;
import com.blogmanagement.dto.BlogResponseDTO;
import com.blogmanagement.entity.BlogPost;
import com.blogmanagement.entity.Role;
import com.blogmanagement.entity.User;
import com.blogmanagement.exception.ResourceNotFoundException;
import com.blogmanagement.exception.UnauthorizedException;
import com.blogmanagement.repository.BlogPostRepository;
import com.blogmanagement.repository.UserRepository;
import com.blogmanagement.util.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlogService blogService;

    private User author;
    private User admin;
    private User otherUser;
    private BlogPost blogPost;
    private BlogRequestDTO blogRequest;
    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .name("Jane Author")
                .email("author@example.com")
                .role(Role.USER)
                .build();

        admin = User.builder()
                .id(2L)
                .name("Admin User")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        otherUser = User.builder()
                .id(3L)
                .name("Other User")
                .email("other@example.com")
                .role(Role.USER)
                .build();

        blogPost = BlogPost.builder()
                .id(10L)
                .title("Original Title")
                .content("Original content of the blog post.")
                .author(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        blogRequest = new BlogRequestDTO("Updated Title", "Updated content of the blog post which is long enough.");
        
        // Open mock for static utility class
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        // Close static mock to prevent resource leaks
        mockedSecurityUtil.close();
    }

    @Test
    void getAllBlogs_ShouldReturnBlogResponseList() {
        when(blogPostRepository.findAllWithAuthor()).thenReturn(List.of(blogPost));

        List<BlogResponseDTO> result = blogService.getAllBlogs();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Original Title", result.get(0).getTitle());
        verify(blogPostRepository, times(1)).findAllWithAuthor();
    }

    @Test
    void getBlogById_ShouldReturnBlog_WhenExists() {
        when(blogPostRepository.findByIdWithAuthor(10L)).thenReturn(Optional.of(blogPost));

        BlogResponseDTO result = blogService.getBlogById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Original Title", result.getTitle());
    }

    @Test
    void getBlogById_ShouldThrowResourceNotFoundException_WhenNotExists() {
        when(blogPostRepository.findByIdWithAuthor(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> blogService.getBlogById(99L));
    }

    @Test
    void createBlog_ShouldCreateBlogPost() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn("author@example.com");
        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(author));
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(blogPost);

        BlogResponseDTO result = blogService.createBlog(blogRequest);

        assertNotNull(result);
        assertEquals("Original Title", result.getTitle()); // matches saved mocked instance
        verify(blogPostRepository, times(1)).save(any(BlogPost.class));
    }

    @Test
    void updateBlog_ShouldUpdate_WhenUserIsOwner() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn("author@example.com");
        when(blogPostRepository.findByIdWithAuthor(10L)).thenReturn(Optional.of(blogPost));
        when(blogPostRepository.save(any(BlogPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogResponseDTO result = blogService.updateBlog(10L, blogRequest);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated content of the blog post which is long enough.", result.getContent());
    }

    @Test
    void updateBlog_ShouldThrowUnauthorizedException_WhenUserIsNotOwner() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn("other@example.com");
        when(blogPostRepository.findByIdWithAuthor(10L)).thenReturn(Optional.of(blogPost));

        assertThrows(UnauthorizedException.class, () -> blogService.updateBlog(10L, blogRequest));
        verify(blogPostRepository, never()).save(any(BlogPost.class));
    }

    @Test
    void deleteBlog_ShouldDelete_WhenUserIsOwner() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn("author@example.com");
        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(author));
        when(blogPostRepository.findByIdWithAuthor(10L)).thenReturn(Optional.of(blogPost));

        assertDoesNotThrow(() -> blogService.deleteBlog(10L));
        verify(blogPostRepository, times(1)).delete(blogPost);
    }

    @Test
    void deleteBlog_ShouldDelete_WhenUserIsAdmin() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(blogPostRepository.findByIdWithAuthor(10L)).thenReturn(Optional.of(blogPost));

        assertDoesNotThrow(() -> blogService.deleteBlog(10L));
        verify(blogPostRepository, times(1)).delete(blogPost);
    }

    @Test
    void deleteBlog_ShouldThrowUnauthorizedException_WhenUserIsNeitherOwnerNorAdmin() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(blogPostRepository.findByIdWithAuthor(10L)).thenReturn(Optional.of(blogPost));

        assertThrows(UnauthorizedException.class, () -> blogService.deleteBlog(10L));
        verify(blogPostRepository, never()).delete(any(BlogPost.class));
    }
}
