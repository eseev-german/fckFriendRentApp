package fck.friend.rent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fck.friend.rent.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class JacksonConfigurationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ignoreRootElement() throws IOException {
        UserDTO expected = new UserDTO();
        expected.setItems(Arrays.asList("207856510", "545687768"));


        UserDTO userDTO = objectMapper.readValue("{\"response\":{\"count\":2,\"items\":[207856510,545687768]}}", UserDTO.class);

        assertEquals(expected, userDTO);
    }
}