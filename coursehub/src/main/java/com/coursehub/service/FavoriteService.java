package com.coursehub.service;

import com.coursehub.dto.response.CourseResponse;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {
    void addFavorite(UUID userId, UUID courseId);
    void removeFavorite(UUID userId, UUID courseId);
    boolean checkFavorite(UUID userId, UUID courseId);
    List<CourseResponse> getMyFavorites(UUID userId);
}
