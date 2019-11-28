package fck.friend.rent.dto.post;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public final class PostDto {
    private String id;
    private String date;
    private String ownerId;
    private String fromId;
    private String text;
}