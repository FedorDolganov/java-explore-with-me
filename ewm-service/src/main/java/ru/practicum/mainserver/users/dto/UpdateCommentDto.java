package ru.practicum.mainserver.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateCommentDto {

    @Length(min = 10, max = 400)
    private String text;

}
