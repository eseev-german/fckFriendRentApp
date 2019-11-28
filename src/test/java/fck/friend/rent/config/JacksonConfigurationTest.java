package fck.friend.rent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fck.friend.rent.dto.post.PostBunchDto;
import fck.friend.rent.dto.post.PostDto;
import fck.friend.rent.dto.user.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class JacksonConfigurationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void readUserResponse() throws IOException {
        UserDto expected = new UserDto(Arrays.asList("1312", "111"));

        UserDto userDTO = objectMapper.readValue(readResponseJson("user.json"),
                UserDto.class);

        assertEquals(expected, userDTO);
    }

    @Test
    void readPostBunchResponse() throws IOException {
        PostDto firstExpected = new PostDto();
        firstExpected.setId("11");
        firstExpected.setDate("1572633558");
        firstExpected.setOwnerId("2");
        firstExpected.setFromId("1");
        firstExpected.setText("First text");

        PostDto secondExpected = new PostDto();
        secondExpected.setId("12");
        secondExpected.setDate("1571337916");
        secondExpected.setOwnerId("5");
        secondExpected.setFromId("4");
        secondExpected.setText("second text");

        PostBunchDto expected = new PostBunchDto(Arrays.asList(firstExpected, secondExpected));

        PostBunchDto result = objectMapper.readValue(readResponseJson("post_bunch.json"), PostBunchDto.class);

        assertEquals(expected, result);
    }

    private String readResponseJson(String jsonFileName) throws IOException {
        return Files.readString(Paths.get("src", "test", "resources", "json", jsonFileName));
    }
}