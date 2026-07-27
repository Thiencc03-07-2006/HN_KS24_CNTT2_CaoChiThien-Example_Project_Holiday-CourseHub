package com.coursehub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDto {
    private String content;

    /**
     * FIX: Changed from primitive `boolean` to wrapper `Boolean`.
     *
     * ROOT CAUSE EXPLANATION:
     * When using Lombok @Getter/@Setter on `private boolean isCorrect` (primitive):
     *   - Lombok generates getter: isCorrect()    (strips nothing — keeps `is` prefix for boolean)
     *   - Lombok generates setter: setCorrect(boolean)  ← PROBLEM: Lombok STRIPS the `is` prefix
     *
     * Jackson by default discovers setters for deserialization. Since the setter is
     * `setCorrect(boolean val)` and NOT `setIsCorrect(boolean val)`, Jackson cannot match
     * the JSON property "isCorrect" to the setter "setCorrect", even with @JsonProperty("isCorrect")
     * on the field (field-level annotation takes lower priority than method-level by default).
     *
     * RESULT: isCorrect is NEVER set during deserialization → always stays `false` →
     * all answers are saved as incorrect → grading always marks correct answers as wrong.
     *
     * FIX: Using `Boolean` (wrapper type):
     *   - Lombok generates getter: getIsCorrect()
     *   - Lombok generates setter: setIsCorrect(Boolean)
     * Jackson correctly maps JSON "isCorrect" → setIsCorrect() ← This works properly.
     *
     * The @JsonProperty("isCorrect") annotation ensures explicit binding in both directions.
     */
    @JsonProperty("isCorrect")
    private Boolean isCorrect;

    private Integer orderIndex;

    // Convenience method for backward compatibility with existing service code using aDto.isCorrect()
    public boolean isCorrect() {
        return Boolean.TRUE.equals(isCorrect);
    }
}
