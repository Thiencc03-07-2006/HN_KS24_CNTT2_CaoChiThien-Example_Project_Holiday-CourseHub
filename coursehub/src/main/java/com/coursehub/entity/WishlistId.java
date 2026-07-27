package com.coursehub.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.io.Serializable;
import java.sql.Types;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistId implements Serializable {
    @JdbcTypeCode(Types.VARCHAR)
    private UUID userId;
    @JdbcTypeCode(Types.VARCHAR)
    private UUID courseId;
}
