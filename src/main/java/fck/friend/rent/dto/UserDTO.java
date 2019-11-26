package fck.friend.rent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonRootName("response")
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class UserDTO {
    private List<String> items;
}