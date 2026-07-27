package com.coursehub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions",
        indexes = {
                @Index(name = "idx_questions_quiz_id", columnList = "quiz_id"),
                @Index(name = "idx_questions_quiz_order", columnList = "quiz_id, order_index")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private LessonEntity quiz;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "question_type", nullable = false, length = 20)
    @Builder.Default
    private String questionType = "SINGLE_CHOICE";

    @Column(name = "points", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal points = BigDecimal.ONE;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<AnswerEntity> answers = new ArrayList<>();
}
