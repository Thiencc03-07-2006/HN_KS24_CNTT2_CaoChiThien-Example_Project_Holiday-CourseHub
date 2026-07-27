package com.coursehub.service;

import com.coursehub.dto.response.CourseResponse;

import java.util.List;
import java.util.UUID;

public interface WishlistService {
    void addToWishlist(UUID userId, UUID courseId);
    void removeFromWishlist(UUID userId, UUID courseId);
    boolean checkWishlist(UUID userId, UUID courseId);
    List<CourseResponse> getMyWishlist(UUID userId);
}
